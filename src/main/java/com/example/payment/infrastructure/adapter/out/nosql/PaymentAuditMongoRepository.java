package com.example.payment.infrastructure.adapter.out.nosql;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAuditMongoRepository extends MongoRepository<PaymentAuditDocument, String> {
   List<PaymentAuditDocument> findByPaymentId(UUID paymentId);
}
