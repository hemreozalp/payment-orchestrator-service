package com.example.payment_orchestrator.consumer;

import com.example.payment_orchestrator.config.RabbitMqConfig;
import com.example.payment_orchestrator.model.dto.PaymentNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CompensateConsumer {

    @RabbitListener(queues = RabbitMqConfig.COMPENSATE_QUEUE)
    public void handlePaymentFailureAndCompensate(PaymentNotification message) {

        System.out.println("[SAGA COMPENSATION] Payment failure detected. Starting compensation flow.");

        System.out.println("Payment ID: " + message.paymentId() + " | Status: " + message.status());
        System.out.println("Merchant ID: " + message.merchantId() + " | Sending cancellation webhook...");

        System.out.println("[SAGA COMPENSATION] Compensation process completed successfully.");
    }
}