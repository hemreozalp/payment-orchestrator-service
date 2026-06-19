package com.example.payment_orchestrator.service;

import com.example.payment_orchestrator.config.RabbitMqConfig;
import com.example.payment_orchestrator.mapper.PaymentMapper;
import com.example.payment_orchestrator.model.Merchant;
import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.PaymentAttempt;
import com.example.payment_orchestrator.model.dto.PaymentNotification;
import com.example.payment_orchestrator.model.dto.PaymentRequestDto;
import com.example.payment_orchestrator.model.enums.Currency;
import com.example.payment_orchestrator.model.enums.PaymentAttemptStatus;
import com.example.payment_orchestrator.model.enums.PaymentStatus;
import com.example.payment_orchestrator.repository.MerchantRepository;
import com.example.payment_orchestrator.repository.PaymentAttemptRepository;
import com.example.payment_orchestrator.repository.PaymentRepository;
import com.example.payment_orchestrator.service.provider.PaymentProvider;
import com.example.payment_orchestrator.service.provider.PaymentProviderFactory;
import com.example.payment_orchestrator.service.provider.ProviderResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProviderFactory paymentProviderFactory;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public PaymentService(MerchantRepository merchantRepository,
                          PaymentRepository paymentRepository,
                          PaymentAttemptRepository paymentAttemptRepository,
                          PaymentMapper paymentMapper,
                          PaymentProviderFactory paymentProviderFactory,
                          RabbitTemplate rabbitTemplate,
                          StringRedisTemplate redisTemplate) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentMapper = paymentMapper;
        this.paymentProviderFactory = paymentProviderFactory;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Payment createPayment(PaymentRequestDto request) {

        String lockKey = "lock:payment:" + request.idempotencyKey();
        Boolean isLocked = false;

        int maxAttempts = 20;
        int attempts = 0;

        while (!isLocked && attempts < maxAttempts) {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    "PROCESSING",
                    java.time.Duration.ofSeconds(30)
            );
            if (Boolean.TRUE.equals(result)) {
                isLocked = true;
            } else {
                attempts++;
                try {
                    Thread.sleep(250);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("While waiting lock thread interrupted", exception);
                }
            }
        }

        if (!isLocked) {
            throw new RuntimeException("System busy (Lock Timeout)");
        }

        try {
            try {
                System.out.println("🚀 [TEST] İlk istek kilidi aldı, 5 saniye uyutuluyor...");
                Thread.sleep(5000);
            } catch (InterruptedException e) { }
            if (paymentRepository.existsByIdempotencyKey(request.idempotencyKey())) {
                return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                        .orElseThrow(() -> new RuntimeException("Payment record not found!"));
            }

            Merchant merchant = merchantRepository.findById(request.merchantId())
                    .orElseThrow(() -> new RuntimeException("Merchant not found!"));

            Payment payment = paymentMapper.toEntity(request);
            payment.setMerchant(merchant);
            payment.setIdempotencyKey(request.idempotencyKey());
            payment = paymentRepository.save(payment);

            List<String> providerOrder = new ArrayList<>();
            if (request.currency() == Currency.TRY){
                providerOrder.add("IYZICO");
                providerOrder.add("STRIPE");
            } else {
                providerOrder.add("STRIPE");
                providerOrder.add("IYZICO");
            }

            ProviderResult finalResult = null;
            int attemptCount = 0;

            for (String providerName : providerOrder) {
                attemptCount++;

                if (attemptCount > 1) {
                    long waitTimeInSeconds = (long) Math.pow(2, attemptCount - 2);
                    System.out.println("Provider error detected. Waiting " + waitTimeInSeconds + " seconds... (Exponential Backoff)");

                    try {
                        Thread.sleep(waitTimeInSeconds * 1000);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during wait period", exception);
                    }
                }

                PaymentProvider provider = paymentProviderFactory.getProvider(providerName);

                PaymentAttempt attempt = new PaymentAttempt();
                attempt.setPayment(payment);
                attempt.setProviderName(provider.getProviderName());

                finalResult = provider.processPayment(request);
                attempt.setStatus(finalResult.status());

                if (finalResult.status() == PaymentAttemptStatus.FAILED) {
                    attempt.setErrorCode(finalResult.errorCode());
                }

                paymentAttemptRepository.save(attempt);

                if (finalResult.status() == PaymentAttemptStatus.SUCCESS) {
                    break;
                }
            }

            if (finalResult != null && finalResult.status() == PaymentAttemptStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }

            PaymentNotification paymentNotification = new PaymentNotification(
                    payment.getId(),
                    merchant.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    finalResult != null ? finalResult.errorCode() : null
            );

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE,
                    RabbitMqConfig.ROUTING_KEY,
                    paymentNotification
            );

            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional(readOnly = true)
    public Payment getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
    }
}
