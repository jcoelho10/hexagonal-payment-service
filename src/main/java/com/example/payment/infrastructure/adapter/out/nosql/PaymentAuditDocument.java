package com.example.payment.infrastructure.adapter.out.nosql;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "payment_audit_logs")
public record PaymentAuditDocument(
        @Id String id,
        UUID paymentId,
        String customerId,
        String status,
        Instant timestamp
) {}
