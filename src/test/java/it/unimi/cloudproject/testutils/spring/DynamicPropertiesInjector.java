package it.unimi.cloudproject.testutils.spring;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class DynamicPropertiesInjector {
    public static void injectDatasourceFromPostgresContainer(DynamicPropertyRegistry registry, PostgreSQLContainer<?> db) {
        registry.add("spring.datasource.url", db::getJdbcUrl);
        registry.add("spring.datasource.password", db::getPassword);
        registry.add("spring.datasource.username", db::getUsername);
    }
}
