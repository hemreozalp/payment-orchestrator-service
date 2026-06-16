package com.example.payment_orchestrator.repository;

import com.example.payment_orchestrator.model.Payment;
import com.example.payment_orchestrator.model.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    List<PaymentAttempt> findByPayment(Payment payment);
}
