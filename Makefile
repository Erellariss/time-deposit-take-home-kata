.PHONY: run stop test
run:
	docker compose up -d --build

stop:
	docker compose down --volumes

test:
	cd java && ./mvnw test
