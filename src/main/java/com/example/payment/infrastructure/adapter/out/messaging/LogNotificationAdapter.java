package com.example.payment.infrastructure.adapter.out.messaging;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.PaymentNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogNotificationAdapter implements PaymentNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationAdapter.class);

    @Override
    public void notifyPaymentProcessed(Payment payment) {
        log.info("EVENTO DE DOMÍNIO: Notificação emitida para o pagamento ID: {} com status: {}",
                payment.getId(),
                payment.getStatus());
    }
}
