package com.dnnthanh.marketplace.be.pricing.api.configuration;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.dnnthanh.marketplace.be.pricing.api.domain.service.EffectivePriceResolver;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Regression coverage for Spring wiring of framework-free pricing domain services. */
class PricingDomainConfigurationTest {

    @Test
    void exposesEffectivePriceResolverAsSpringBean() {
        try (var context =
                new AnnotationConfigApplicationContext(PricingDomainConfiguration.class)) {
            EffectivePriceResolver resolver = context.getBean(EffectivePriceResolver.class);
            assertSame(resolver, context.getBean("effectivePriceResolver"));
        }
    }
}
