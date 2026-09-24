package com.example.payment.infrastructure.adapter.out.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    List<PaymentOutboxEntity> findByProcessedFalseOrderByCreatedAtAsc();
}
