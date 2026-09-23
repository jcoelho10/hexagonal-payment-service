package com.example.payment.infrastructure.config;

import com.example.payment.application.service.ProcessPaymentService;
import com.example.payment.domain.port.in.ProcessPaymentUseCase;
import com.example.payment.domain.port.out.GatewayPaymentPort;
import com.example.payment.domain.port.out.PaymentNotificationPort;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            PaymentRepositoryPort repositoryPort,
            GatewayPaymentPort gatewayPort,
            PaymentNotificationPort notificationPort) {
        return new ProcessPaymentService(repositoryPort, gatewayPort, notificationPort);
    }
}
