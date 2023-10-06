package it.unimi.cloudproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// TODO: https://docs.spring.io/spring-cloud-function/docs/current/reference/html/aws.html
// to speed up cold starts
@SpringBootApplication
@ComponentScan(basePackages = "it.unimi.cloudproject")
public class ShopApi {

	public static void main(String[] args) {
		SpringApplication.run(ShopApi.class, args);
	}

}
