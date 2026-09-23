package com.example.payment.infrastructure.adapter.out.gateway;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.GatewayPaymentPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ExternalGatewayAdapter implements GatewayPaymentPort {

    private static final Logger log = LoggerFactory.getLogger(ExternalGatewayAdapter.class);

    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallbackProcess")
    @Retry(name = "paymentGateway")
    public boolean process(Payment payment) {
        log.info("Chamando gateway externo para o pagamento do ID: {}", payment.getId());

        // Simulação de chamada externa
        if (Math.random() < 0.3) {
            throw new RuntimeException("Instabilidade momentânea no provedor de pagamento");
        }
        return true;
    }

    // Fallback executado caso o Circuit Breaker abra ou o Retry falhe
    public boolean fallbackProcess(Payment payment, Throwable t) {
        log.error("Fallback ativado para pagamento ID: {}. Motivo: {}", payment.getId(), t.getMessage());
        return false;
    }
}
