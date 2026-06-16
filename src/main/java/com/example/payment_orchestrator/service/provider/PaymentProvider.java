package com.example.payment_orchestrator.service.provider;

import com.example.payment_orchestrator.model.dto.PaymentRequestDto;

public interface PaymentProvider {
    ProviderResult processPayment(PaymentRequestDto request);

    String getProviderName();
}
