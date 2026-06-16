package com.example.payment_orchestrator.service;

import com.example.payment_orchestrator.mapper.PaymentMapper;
import com.example.payment_orchestrator.model.Merchant;
import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.PaymentAttempt;
import com.example.payment_orchestrator.model.dto.PaymentRequestDto;
import com.example.payment_orchestrator.model.enums.PaymentAttemptStatus;
import com.example.payment_orchestrator.model.enums.PaymentStatus;
import com.example.payment_orchestrator.repository.MerchantRepository;
import com.example.payment_orchestrator.repository.PaymentAttemptRepository;
import com.example.payment_orchestrator.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(MerchantRepository merchantRepository,
                          PaymentRepository paymentRepository,
                          PaymentAttemptRepository paymentAttemptRepository,
                          PaymentMapper paymentMapper) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public Payment createPayment(PaymentRequestDto request) {

        Merchant merchant = merchantRepository.findById(request.merchantId())
                .orElseThrow(() -> new RuntimeException("Merchant bulunamadı!"));

        Payment payment = paymentMapper.toEntity(request);
        payment.setMerchant(merchant);

        payment = paymentRepository.save(payment);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setProviderName("STRIPE");

        boolean isMockSuccess = request.amount().longValue() % 2 == 0;

        if (isMockSuccess) {
            attempt.setStatus(PaymentAttemptStatus.SUCCESS);
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            attempt.setStatus(PaymentAttemptStatus.FAILED);
            attempt.setErrorCode("ERR_1001");
            payment.setStatus(PaymentStatus.FAILED);
        }

        // 4. VERİ TABANI KAYITLARI
        paymentAttemptRepository.save(attempt);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
    }
}
