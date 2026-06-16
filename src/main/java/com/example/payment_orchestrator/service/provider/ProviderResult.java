package com.example.payment_orchestrator.service.provider;

import com.example.payment_orchestrator.model.enums.PaymentAttemptStatus;

public record ProviderResult(
        PaymentAttemptStatus status,
        String errorCode
) {
}
