package com.community.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/user/**")
                        .uri("lb://user-service"))
                .route("order-service", r -> r
                        .path("/api/order/**")
                        .uri("lb://order-service"))
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .uri("lb://payment-service"))
                .route("message-service", r -> r
                        .path("/api/message/**")
                        .uri("lb://message-service"))
                .route("credit-service", r -> r
                        .path("/api/credit/**")
                        .uri("lb://credit-service"))
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .uri("lb://admin-service"))
                .route("service-service", r -> r
                        .path("/api/service/**")
                        .uri("lb://service-service"))
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .uri("lb://search-service"))
                .build();
    }
}
