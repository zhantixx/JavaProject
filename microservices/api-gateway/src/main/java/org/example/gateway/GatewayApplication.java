package org.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("API Gateway запущен на порту 8080");
        System.out.println("Маршруты:");
        System.out.println("   /api/auth/** → user-service (8081)");
        System.out.println("   /api/tasks/** → task-service (8082)");
        System.out.println("   /api/chat/** → ai-service (8083)");
        System.out.println("   /api/notifications/** → notification-service (8084)");
        System.out.println("   /api/analytics/** → analytics-service (8085)");
    }
}