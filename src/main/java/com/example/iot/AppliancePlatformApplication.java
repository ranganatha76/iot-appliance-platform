package com.example.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppliancePlatformApplication {
    /** Starts Spring Boot and enables the configured metric collection scheduler. */
    public static void main(String[] args) {
        SpringApplication.run(AppliancePlatformApplication.class, args);
    }
}