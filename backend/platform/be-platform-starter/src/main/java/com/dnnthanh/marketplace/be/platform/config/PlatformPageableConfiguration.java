package com.dnnthanh.marketplace.be.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/** Shared MVC pagination policy for relational list/search endpoints. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class PlatformPageableConfiguration {
    @Bean
    PageableHandlerMethodArgumentResolverCustomizer
            pageableHandlerMethodArgumentResolverCustomizer() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
