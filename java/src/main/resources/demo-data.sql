WITH seeded_deposits AS (
    INSERT INTO time_deposits (plan_type, days, balance)
    SELECT plan_type, days, balance
    FROM (VALUES
        ('basic', 31, 1200.00),
        ('student', 365, 2400.00),
        ('premium', 46, 3600.00)
    ) AS samples(plan_type, days, balance)
    WHERE NOT EXISTS (SELECT 1 FROM time_deposits)
    RETURNING id, plan_type
)
INSERT INTO withdrawals (time_deposit_id, amount, date)
SELECT seeded_deposits.id, sample_withdrawals.amount, sample_withdrawals.date
FROM seeded_deposits
JOIN (VALUES
    ('basic', 25.50, DATE '2026-10-21'),
    ('basic', 10.00, DATE '2026-10-29'),
    ('premium', 50.00, DATE '2026-10-30')
) AS sample_withdrawals(plan_type, amount, date) USING (plan_type);
