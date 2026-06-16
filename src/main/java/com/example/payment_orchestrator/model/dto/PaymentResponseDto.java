package com.example.payment_orchestrator.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long paymentId,
        String status,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt
) {}