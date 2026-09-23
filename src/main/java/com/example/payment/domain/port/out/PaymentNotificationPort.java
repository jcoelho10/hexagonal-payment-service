package com.example.payment.domain.port.out;

import com.example.payment.domain.model.Payment;

public interface PaymentNotificationPort {
    void notifyPaymentProcessed(Payment payment);
}
