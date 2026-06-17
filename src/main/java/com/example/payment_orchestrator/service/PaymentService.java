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
import java.util.ArrayList;
import java.util.List;

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

        List<String> providerOrder = new ArrayList<>();
        if (request.currency() == Currency.TRY){
            providerOrder.add("IYZICO");
            providerOrder.add("STRIPE");
        } else {
            providerOrder.add("STRIPE");
            providerOrder.add("IYZICO");
        }

        ProviderResult finalResult = null;

        for (String providerName : providerOrder) {
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

        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
    }
}
