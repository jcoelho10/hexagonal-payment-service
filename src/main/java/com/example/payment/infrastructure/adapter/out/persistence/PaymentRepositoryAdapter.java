package com.example.payment.infrastructure.adapter.out.persistence;

import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository springDataRepository;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
                payment.getId(),
                payment.getCustomerId(),
                payment.getMoney().amount(),
                payment.getMoney().currency(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
        PaymentEntity saved = springDataRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return springDataRepository.findById(id).map(this::mapToDomain);
    }

    private Payment mapToDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getCustomerId(),
                new Money(entity.getAmount(), entity.getCurrency()),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
