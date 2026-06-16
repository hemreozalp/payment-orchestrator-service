package com.example.payment_orchestrator.controller;

import com.example.payment_orchestrator.mapper.PaymentMapper;
import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.dto.PaymentRequestDto;
import com.example.payment_orchestrator.model.dto.PaymentResponseDto;
import com.example.payment_orchestrator.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService,
                             PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentRequestDto request) {
        Payment payment = paymentService.createPayment(request);
        PaymentResponseDto response = paymentMapper.toResponseDto(payment);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentStatus(paymentId);
        PaymentResponseDto response = paymentMapper.toResponseDto(payment);

        return ResponseEntity.ok(response);
    }
}
