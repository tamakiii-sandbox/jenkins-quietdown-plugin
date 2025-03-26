.PHONY: all up down bash clean package test run

# Default target
all: up

# Start the development environment
up:
	docker-compose up -d

# Stop the development environment
down:
	docker-compose down

# Open a shell in the container
bash:
	docker-compose exec maven /bin/bash

# Maven targets
clean:
	docker-compose exec maven mvn clean

package:
	docker-compose exec maven mvn package

test:
	docker-compose exec maven mvn test

run:
	docker-compose exec maven mvn hpi:run
