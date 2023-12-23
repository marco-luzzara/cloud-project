package it.unimi.cloudproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.function.Consumer;

/**
 * This Lambda Functions creates the necessary database table by importing the
 * liquibase runtime package. It is invoked before any other Lambda when the database is ready
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {Initializer.class})
public class Initializer {

	public static void main(String[] args) {
		SpringApplication.run(Initializer.class, args);
	}

	private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
	@Bean
	public Consumer<Configuration> initialize() {
		return (configs) -> logger.info("Initialization complete");
	}

	public record Configuration() {}
}
