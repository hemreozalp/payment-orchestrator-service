package com.example.payment_orchestrator.service;

import com.example.payment_orchestrator.config.RabbitMqConfig;
import com.example.payment_orchestrator.model.dto.PaymentNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void consumePaymentNotification(PaymentNotification notification) {

        System.out.println("[RabbitMQ Consumer] A new payment result has been received from the queue!");
        System.out.println("Payment ID: " + notification.paymentId() + " | Status: " + notification.status());
        System.out.println("Sending webhook to Merchant ID: " + notification.merchantId() + "...");
    }
}
