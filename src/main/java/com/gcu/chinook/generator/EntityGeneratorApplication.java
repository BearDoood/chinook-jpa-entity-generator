package com.gcu.chinook.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EntityGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntityGeneratorApplication.class, args);
    }
}
