package com.example.payment_orchestrator.mapper;

import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.dto.PaymentRequestDto;
import com.example.payment_orchestrator.model.dto.PaymentResponseDto;
import com.example.payment_orchestrator.model.enums.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequestDto request) {
        if (request == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setStatus(PaymentStatus.PENDING);

        return payment;
    }

    public PaymentResponseDto toResponseDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponseDto(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency().name(),
                payment.getCreatedAt()
        );
    }
}