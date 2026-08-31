package com.valorant.devopshub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the VALORANT Tactical Hub.
 *
 * This is intentionally a small, standard Spring Boot application. The
 * complexity of this project lives in the DevOps tooling around it
 * (Docker, Nginx, Jenkins, systemd, scripts) - not in the Java code itself.
 */
@SpringBootApplication
public class DevopsHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsHubApplication.class, args);
    }
}
