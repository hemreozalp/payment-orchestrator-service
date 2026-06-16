package com.example.payment_orchestrator.model;

import com.example.payment_orchestrator.model.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "merchants", indexes = {
        @Index( name = "idx_merchant_api_key",
                columnList = "api_key",
                unique = true)
})
@AllArgsConstructor
@Data
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status;
}
