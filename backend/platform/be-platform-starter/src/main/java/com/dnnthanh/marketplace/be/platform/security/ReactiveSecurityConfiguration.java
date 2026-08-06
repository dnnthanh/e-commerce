package com.dnnthanh.marketplace.be.platform.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/** Common reactive security baseline for Gateway and WebFlux search endpoints. */
@Configuration
@EnableWebFluxSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveSecurityConfiguration {

    /**
     * Leaves public marketplace routes anonymous and requires JWT authentication for
     * private/internal routes.
     *
     * @param http reactive security builder
     * @return configured reactive filter chain
     */
    @Bean
    public SecurityWebFilterChain reactiveSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(
                        exchange ->
                                exchange.pathMatchers(
                                                "/actuator/health/**",
                                                "/actuator/prometheus",
                                                "/error")
                                        .permitAll()
                                        .pathMatchers("/private/**", "/internal/**")
                                        .authenticated()
                                        .anyExchange()
                                        .permitAll())
                .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()))
                .build();
    }
}
