package org.ikigaidigital.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "springdoc.api-docs.enabled=true")
class TimeDepositApiTest {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String DEPOSITS = "/time-deposits";
    private static final String ACCRUALS = DEPOSITS + "/accruals";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper json;

    @BeforeEach
    void clearTables() {
        jdbc.execute("TRUNCATE withdrawals, time_deposits RESTART IDENTITY");
    }

    @Test
    void listsDepositsAndAllWithdrawalsInOrder() throws Exception {
        jdbc.update("""
                INSERT INTO time_deposits (id, plan_type, balance, days)
                VALUES (2, 'student', 2400.00, 365), (1, 'basic', 1200.125, 31)
                """);
        jdbc.update("""
                INSERT INTO withdrawals (id, time_deposit_id, amount, date)
                SELECT n, 1, 12.345, DATE '2026-01-15' FROM generate_series(513, 1, -1) AS n
                """);

        var response = send(request(DEPOSITS).GET().build());
        assertThat(response.statusCode()).isEqualTo(200);
        var deposits = json.readTree(response.body());
        assertThat(deposits.isArray()).isTrue();
        assertThat(deposits.size()).isEqualTo(2);
        var basic = deposits.get(0);
        assertThat(basic.path("id").asInt()).isEqualTo(1);
        assertThat(basic.path("planType").asText()).isEqualTo("basic");
        assertThat(basic.path("balance").decimalValue()).isEqualByComparingTo("1200.125");
        assertThat(basic.path("days").asInt()).isEqualTo(31);
        var withdrawals = basic.path("withdrawals");
        assertThat(withdrawals.size()).isEqualTo(513);
        assertThat(withdrawals.get(0).path("id").asInt()).isEqualTo(1);
        assertThat(withdrawals.get(0).path("amount").decimalValue()).isEqualByComparingTo("12.345");
        assertThat(withdrawals.get(0).path("date").asText()).isEqualTo("2026-01-15");
        assertThat(withdrawals.get(512).path("id").asInt()).isEqualTo(513);
        assertThat(deposits.get(1).path("withdrawals").isArray()).isTrue();
        assertThat(deposits.get(1).path("withdrawals").size()).isZero();
    }

    @Test
    void returnsAnEmptyArrayWhenNoDepositsExist() throws Exception {
        var response = send(request(DEPOSITS).GET().build());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[]");
    }

    @Test
    void persistsLegacyCalculatorResultsAcrossBatches() throws Exception {
        insertBasicDeposits(1001);
        jdbc.update("""
                INSERT INTO time_deposits (id, plan_type, balance, days)
                VALUES (-2147483648, 'basic', 6.02, 31), (0, 'basic', 18.000000000000001, 31),
                       (2147483646, 'student', 3000.000000000000001, 366), (2147483647, 'premium', 3600.00, 46)
                """);

        var response = send(accrualRequest());

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body()).isEmpty();
        assertThat(balance(-2147483648)).isEqualByComparingTo("6.029999999999999");
        assertThat(balance(0)).isEqualByComparingTo("18.01");
        assertThat(balance(2147483646)).isEqualByComparingTo("3000.000000000000001");
        assertThat(balance(2147483647)).isEqualByComparingTo("3615.00");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM time_deposits WHERE balance = 1201.125", Long.class))
                .isEqualTo(1001L);
    }

    @Test
    void rollsBackEarlierBatchesWhenALaterDepositHasAnInvalidPlan() throws Exception {
        insertBasicDeposits(1001);
        jdbc.update("INSERT INTO time_deposits (plan_type, balance, days) VALUES ('unknown', 2000.00, 31)");

        var response = send(accrualRequest());

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM time_deposits WHERE balance = 1200.125", Long.class))
                .isEqualTo(1001L);
    }

    @Test
    void concurrentAccrualsBothUseTheLatestCommittedBalance() throws Exception {
        jdbc.update("INSERT INTO time_deposits (plan_type, balance, days) VALUES ('student', 40000.00, 31)");

        try (var connection = jdbc.getDataSource().getConnection(); var statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            // Hold the deposit row until both requests are waiting on PostgreSQL locks.
            statement.execute("SELECT id FROM time_deposits WHERE id = 1 FOR UPDATE");
            var first = HTTP.sendAsync(accrualRequest(), HttpResponse.BodyHandlers.discarding());
            var second = HTTP.sendAsync(accrualRequest(), HttpResponse.BodyHandlers.discarding());
            await().atMost(Duration.ofSeconds(10)).until(() -> jdbc.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity
                    WHERE datname = current_database() AND wait_event_type = 'Lock'
                    """, Integer.class) >= 2);
            connection.commit();

            assertThat(first.join().statusCode()).isEqualTo(204);
            assertThat(second.join().statusCode()).isEqualTo(204);
        }

        assertThat(balance(1)).isEqualByComparingTo("40200.25");
    }

    @Test
    void publishesTheTwoBusinessOperationsAndTheArraySchema() throws Exception {
        var response = send(request("/v3/api-docs").GET().build());

        assertThat(response.statusCode()).isEqualTo(200);
        var spec = json.readTree(response.body());
        assertThat(spec.path("paths").size()).isEqualTo(2);
        assertThat(spec.path("paths").path(DEPOSITS).size()).isEqualTo(1);
        assertThat(spec.path("paths").path(ACCRUALS).size()).isEqualTo(1);
        assertThat(spec.path("paths").path(ACCRUALS).at("/post/responses/204").isObject()).isTrue();
        assertThat(spec.path("paths").path(ACCRUALS).path("post").has("requestBody")).isFalse();
        var schema = spec.path("paths").path(DEPOSITS).at("/get/responses/200/content/application~1json/schema");
        assertThat(schema.path("type").asText()).isEqualTo("array");
        assertThat(schema.path("items").path("$ref").asText()).isEqualTo("#/components/schemas/TimeDepositResponse");
        assertThat(spec.at("/components/schemas/TimeDepositResponse/properties/withdrawals/type").asText())
                .isEqualTo("array");
    }

    private void insertBasicDeposits(int count) {
        jdbc.update("""
                INSERT INTO time_deposits (plan_type, balance, days)
                SELECT 'basic', 1200.125, 31 FROM generate_series(1, ?)
                """, count);
    }

    private BigDecimal balance(int id) {
        return jdbc.queryForObject("SELECT balance FROM time_deposits WHERE id = ?", BigDecimal.class, id);
    }

    private HttpRequest accrualRequest() {
        return request(ACCRUALS).POST(HttpRequest.BodyPublishers.noBody()).build();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(Duration.ofSeconds(20));
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfiguration {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:17-alpine");
        }
    }
}
