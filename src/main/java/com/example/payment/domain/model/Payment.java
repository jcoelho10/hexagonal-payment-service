package com.example.payment.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Payment {
    private final UUID id;
    private final String customerId;
    private final Money money;
    private PaymentStatus status;
    private final Instant createdAt;

    public Payment(UUID id, String customerId, Money money, PaymentStatus status, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.customerId = customerId;
        this.money = money;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Payment createNew(String customerId, Money money) {
        return new Payment(null, customerId, money, PaymentStatus.PENDING, null);
    }

    public void markAsApproved() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment can only be approved if it is PENDING");
        }
        this.status = PaymentStatus.APPROVED;
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public UUID getId() {return id;}
    public String getCustomerId() {return customerId;}
    public Money getMoney() {return money;}
    public PaymentStatus getStatus() {return status;}
    public Instant getCreatedAt() {return createdAt;}
}
