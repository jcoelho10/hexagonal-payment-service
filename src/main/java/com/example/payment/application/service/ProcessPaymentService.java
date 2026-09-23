package com.example.payment.application.service;

import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.in.ProcessPaymentUseCase;
import com.example.payment.domain.port.out.GatewayPaymentPort;
import com.example.payment.domain.port.out.PaymentNotificationPort;
import com.example.payment.domain.port.out.PaymentRepositoryPort;

public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final PaymentRepositoryPort repositoryPort;
    private final GatewayPaymentPort gatewayPort;
    private final PaymentNotificationPort notificationPort;

    public ProcessPaymentService(PaymentRepositoryPort repositoryPort,
                                 GatewayPaymentPort gatewayPort,
                                 PaymentNotificationPort notificationPort) {
        this.repositoryPort = repositoryPort;
        this.gatewayPort = gatewayPort;
        this.notificationPort = notificationPort;
    }

    @Override
    public Payment execute(String customerId, Money money) {
        Payment payment = Payment.createNew(customerId, money);
        payment = repositoryPort.save(payment);

        boolean isSucess = gatewayPort.process(payment);

        if (isSucess) {
            payment.markAsApproved();
        } else {
            payment.markAsFailed();
        }

        Payment updatedPayment = repositoryPort.save(payment);
        notificationPort.notifyPaymentProcessed(updatedPayment);

        return updatedPayment;
    }
}
