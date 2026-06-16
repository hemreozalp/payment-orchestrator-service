package com.example.payment_orchestrator.service.provider;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentProviderFactory {

    private final List<PaymentProvider> providers;
    private Map<String, PaymentProvider> providerMap;

    public PaymentProviderFactory(List<PaymentProvider> providers) {
        this.providers = providers;
    }

    @PostConstruct
    public void init() {
        providerMap = providers.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getProviderName().toUpperCase(),
                        provider -> provider
                ));
    }

    public PaymentProvider getProvider(String providerName) {
        PaymentProvider provider = providerMap.get(providerName.toUpperCase());

        if (provider == null) {
            throw new IllegalArgumentException("Unsupported payment provider: " + providerName);
        }
        return provider;
    }
}
