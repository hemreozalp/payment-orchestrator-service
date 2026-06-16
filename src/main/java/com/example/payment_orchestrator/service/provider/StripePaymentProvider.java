package com.example.payment_orchestrator.service.provider;

import com.example.payment_orchestrator.model.dto.PaymentRequestDto;
import com.example.payment_orchestrator.model.enums.PaymentAttemptStatus;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentProvider implements PaymentProvider {

    @Override
    public ProviderResult processPayment(PaymentRequestDto request) {
        boolean isSuccess = Math.random() < 0.95;

        if (isSuccess) {
            return new ProviderResult(PaymentAttemptStatus.SUCCESS, null);
        }
        return new ProviderResult(PaymentAttemptStatus.FAILED, "STRIPE_401");
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }
}
