package com.example.payment.infrastructure.adapter.out.persistence.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments_outbox")
public class PaymentOutboxEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean processed;

    public PaymentOutboxEntity() {
    }

    public PaymentOutboxEntity(UUID id, String aggregateId, String eventType, String payload, Instant createdAt, boolean processed) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.processed = processed;
    }

    public UUID getId() {
        return id;
    }
    public String getAggregateId() {
        return aggregateId;
    }
    public String getEventType() {
        return eventType;
    }
    public String getPayload() {
        return payload;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public boolean isProcessed() {
        return processed;
    }
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
