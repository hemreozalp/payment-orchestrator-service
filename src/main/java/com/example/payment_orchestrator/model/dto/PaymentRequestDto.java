package com.example.payment_orchestrator.model.dto;

import com.example.payment_orchestrator.model.enums.Currency;

import java.math.BigDecimal;

public record PaymentRequestDto(
        Long merchantId,
        BigDecimal amount,
        Currency currency,
        String cardHolderName,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvv,
        String idempotencyKey
) {}