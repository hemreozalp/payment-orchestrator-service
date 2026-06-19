package com.example.payment_orchestrator.repository;

import com.example.payment_orchestrator.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
