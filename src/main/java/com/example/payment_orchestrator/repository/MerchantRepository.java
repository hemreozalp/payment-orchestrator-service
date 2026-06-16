package com.example.payment_orchestrator.repository;

import com.example.payment_orchestrator.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByApiKey(String apiKey);
}
