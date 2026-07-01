package com.aichange.reviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main entry point for the AI Change Reviewer Spring Boot application.
 * A stateless service that generates AI-powered change reports from GitHub PRs and branches.
 */
@SpringBootApplication
public class AiChangeReviewerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AiChangeReviewerApplication.class, args);
        logApplicationStartup(context);
    }

    private static void logApplicationStartup(ConfigurableApplicationContext context) {
        String protocol = context.getEnvironment().getProperty("server.ssl.key-store") != null ? "https" : "http";
        String port = context.getEnvironment().getProperty("server.port", "8080");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path", "");

        System.out.println("\n========================================");
        System.out.println("🤖 AI Change Reviewer Started Successfully");
        System.out.println("========================================");
        System.out.println("Application URL: " + protocol + "://localhost:" + port + contextPath);
        System.out.println("Frontend URL:    " + protocol + "://localhost:" + port + "/");
        System.out.println("API Endpoint:    " + protocol + "://localhost:" + port + "/api/changelog/generate");
        System.out.println("========================================\n");
    }

}
