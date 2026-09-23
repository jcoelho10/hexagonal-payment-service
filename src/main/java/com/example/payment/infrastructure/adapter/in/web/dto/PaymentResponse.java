package com.example.payment.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    String customerId,
    BigDecimal amount,
    String currency,
    String status) {}
