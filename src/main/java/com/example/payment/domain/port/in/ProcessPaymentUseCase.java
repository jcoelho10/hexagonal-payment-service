package com.example.payment.domain.port.in;

import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;

public interface ProcessPaymentUseCase {

    Payment execute(String customerId, Money money);
}
