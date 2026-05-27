package com.community.common.config;

import com.community.common.circuitbreaker.CircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceGovernanceConfig {

    @Bean
    public CircuitBreaker userServiceCircuitBreaker() {
        return CircuitBreaker.getOrCreate("userService", 5, 3, 30000);
    }

    @Bean
    public CircuitBreaker orderServiceCircuitBreaker() {
        return CircuitBreaker.getOrCreate("orderService", 5, 3, 30000);
    }

    @Bean
    public CircuitBreaker paymentServiceCircuitBreaker() {
        return CircuitBreaker.getOrCreate("paymentService", 3, 2, 60000);
    }

    @Bean
    public CircuitBreaker messageServiceCircuitBreaker() {
        return CircuitBreaker.getOrCreate("messageService", 10, 5, 15000);
    }

    @Bean
    public CircuitBreaker externalServiceCircuitBreaker() {
        return CircuitBreaker.getOrCreate("externalService", 3, 2, 60000);
    }
}
