package com.example.payment_orchestrator.service;

import com.example.payment_orchestrator.mapper.PaymentMapper;
import com.example.payment_orchestrator.model.Merchant;
import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.PaymentAttempt;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentProviderFactory paymentProviderFactory;

    public PaymentService(MerchantRepository merchantRepository,
                          PaymentRepository paymentRepository,
                          PaymentAttemptRepository paymentAttemptRepository,
                          PaymentMapper paymentMapper,
                          PaymentProviderFactory paymentProviderFactory) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentMapper = paymentMapper;
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @Transactional
    public Payment createPayment(PaymentRequestDto request) {

        Merchant merchant = merchantRepository.findById(request.merchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found!"));

        Payment payment = paymentMapper.toEntity(request);
        payment.setMerchant(merchant);
        payment = paymentRepository.save(payment);

        String chosenProvider;
        if (request.currency() == Currency.TRY){
            chosenProvider = "IYZICO";
        } else {
            chosenProvider = "STRIPE";
        }

        PaymentProvider provider = paymentProviderFactory.getProvider(chosenProvider);

        ProviderResult result = provider.processPayment(request);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setProviderName(provider.getProviderName());
        attempt.setStatus(result.status());

        if (result.status() == PaymentAttemptStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            attempt.setErrorCode(result.errorCode());
        }

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
