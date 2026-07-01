package com.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.tienda.repository")
@EntityScan(basePackages = "com.tienda.domain")
public class TechShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechShopApplication.class, args);
    }
}
