package com.example.payment.infrastructure.adapter.in.web;

import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.in.ProcessPaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase) {this.processPaymentUseCase = processPaymentUseCase;}

    @PostMapping
    // @PreAuthorize("hasAuthority('SCOPE_payment:write')") --- Desativando para testar localmente
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Money money = new Money(request.amount(), request.currency());
        Payment payment = processPaymentUseCase.execute(request.customerId(), money);

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getMoney().amount(),
                payment.getMoney().currency(),
                payment.getStatus().name()
        );

        return ResponseEntity.ok(response);
    }

    public record CreatePaymentRequest(String customerId, BigDecimal amount, String currency) {}
    public record PaymentResponse(UUID id, String customerId, BigDecimal amount, String currency, String status) {}
}
