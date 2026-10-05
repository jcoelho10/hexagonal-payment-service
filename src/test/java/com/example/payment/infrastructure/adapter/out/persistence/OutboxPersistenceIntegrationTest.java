package com.example.payment.infrastructure.adapter.out.persistence;

import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;
import com.example.payment.infrastructure.adapter.out.persistence.outbox.SpringDataOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OutboxPersistenceIntegrationTest {

    @Autowired
    private PaymentRepositoryAdapter paymentRepositoryAdapter;

    @Autowired
    private SpringDataOutboxRepository outboxRepository;

    @Test
    @DisplayName("Deve gravar o pagamento e o evento no Outbox na mesma transação ACID")
    void shouldSavePaymentAndOutboxEventAtomically() {
        Payment payment = Payment.createNew("cli-test", new Money(new BigDecimal("500.00"), "BRL"));

        paymentRepositoryAdapter.save(payment);

        assertNotNull(payment.getId());
        assertEquals(1, outboxRepository.count(), "A tabela 'payments_outbox' deve ter exatamente 1 evento gravado");
    }
}
