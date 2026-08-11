package com.dnnthanh.marketplace.be.pricing.api.configuration;

import com.dnnthanh.marketplace.be.pricing.api.domain.service.EffectivePriceResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring wiring for pricing domain services that intentionally remain framework-free. */
@Configuration(proxyBeanMethods = false)
public class PricingDomainConfiguration {

    @Bean
    EffectivePriceResolver effectivePriceResolver() {
        return new EffectivePriceResolver();
    }
}
