package ru.tenderhack.cte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CteGroupingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CteGroupingApplication.class, args);
    }
}

