# Chinook JPA Entity Generator

CST-339 Lab 2 Part 2. A console app that reads Chinook metadata over JDBC and writes one JPA entity per table.

## Run

1. Load the Chinook script into a database named `chinook`.
2. Set `DB_URL`, `DB_USER` and `DB_PASSWORD` if yours differ from the defaults in `application.properties`.
3. `mvn spring-boot:run`

The log lists every table and column. The entities land in `generated_entities`.

## Check the output

Copy `generated_entities/*.java` into a Spring Boot JPA project, for example under `src/main/java/com/gcu/chinook/generated`, then run `mvn compile`.
Set `spring.jpa.hibernate.ddl-auto=validate` and start the app to confirm the mappings match the tables.

## Settings

`generator.schema`, `generator.output-dir` and `generator.package-name` in `application.properties`.
