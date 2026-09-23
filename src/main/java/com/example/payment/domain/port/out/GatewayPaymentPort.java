package com.example.payment.domain.port.out;

import com.example.payment.domain.model.Payment;

public interface GatewayPaymentPort {
    boolean process(Payment payment);
}
