package com.example.payment_orchestrator.model.dto;

import com.example.payment_orchestrator.model.enums.Currency;
import com.example.payment_orchestrator.model.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentNotification(
        Long paymentId,
        Long merchantId,
        BigDecimal amount,
        Currency currency,
        PaymentStatus status,
        String errorCode
) {
}
