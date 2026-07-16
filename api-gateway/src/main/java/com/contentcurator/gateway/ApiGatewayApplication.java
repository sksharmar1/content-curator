package com.contentcurator.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — single entry point for the Content Curator platform.
 *
 * Routes (configured in application.yml):
 *   /api/auth/**, /api/users/**      -> user-profile      (8081)
 *   /api/feeds/**                    -> content-ingestion (8082)
 *   /api/recommendations/**, /api/admin/** -> recommendation (8083)
 *   /api/analytics/**                -> analytics         (8084)
 *   /api/ml/**                       -> ml-service        (5001)
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
