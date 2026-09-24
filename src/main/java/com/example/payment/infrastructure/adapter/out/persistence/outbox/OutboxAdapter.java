package com.example.payment.infrastructure.adapter.out.persistence.outbox;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.PaymentEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxAdapter implements PaymentEventPublisherPort {

    private final SpringDataOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxAdapter(SpringDataOutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishPaymentProcessedEvent(Payment payment) {
        try {
            String payload =  objectMapper.writeValueAsString(payment);
            PaymentOutboxEntity outboxEntity = new PaymentOutboxEntity(
                    UUID.randomUUID(),
                    payment.getId().toString(),
                    "PAYMENT_PROCESSED",
                    payload,
                    Instant.now(),
                    false
            );
            outboxRepository.save(outboxEntity);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar evento para o Outbox", e);
        }
    }
}
