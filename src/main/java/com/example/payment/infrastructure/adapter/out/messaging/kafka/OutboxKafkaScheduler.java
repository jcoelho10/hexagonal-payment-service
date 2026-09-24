package com.example.payment.infrastructure.adapter.out.messaging.kafka;

import com.example.payment.infrastructure.adapter.out.persistence.outbox.PaymentOutboxEntity;
import com.example.payment.infrastructure.adapter.out.persistence.outbox.SpringDataOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxKafkaScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxKafkaScheduler.class);
    private static final String TOPIC = "payment-events";

    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxKafkaScheduler(SpringDataOutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 3000) // Executa a cada 3 segundos
    @Transactional
    public void processOutbox() {
        List<PaymentOutboxEntity> pendingEvents = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (PaymentOutboxEntity event : pendingEvents) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload());
                event.setProcessed(true);
                outboxRepository.save(event);
                log.info("KAFKA OUTBOX: Evento {} publicado no tópico {} com sucesso!", event.getId(), TOPIC);
            } catch (Exception e) {
                log.error("Erro ao publicar evento {} do Outbox para o Kafka: {}", event.getId(), e.getMessage());
            }
        }
    }

}
