package com.example.payment.infrastructure.adapter.out.nosql;

import com.example.payment.domain.model.Payment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PaymentCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "payment:";

    public PaymentCacheService(RedisTemplate<String, Object> redisTemplate) {this.redisTemplate = redisTemplate;}

    public void cachePayment(Payment payment) {
        String key = CACHE_PREFIX + payment.getId().toString();
        redisTemplate.opsForValue().set(key, payment, Duration.ofMinutes(10));
    }

    public Object getCachePayment(String paymentId) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX + paymentId);
    }
}
