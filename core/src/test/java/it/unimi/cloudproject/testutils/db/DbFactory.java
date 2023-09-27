package it.unimi.cloudproject.testutils.db;

import org.testcontainers.containers.PostgreSQLContainer;

public class DbFactory {
    public static PostgreSQLContainer<?> getPostgresContainer() {
        return new PostgreSQLContainer<>("postgres:15")
                .withUsername("sa")
                .withPassword("password")
                .withDatabaseName("testdb")
                .withExposedPorts(5432);
    }
}
