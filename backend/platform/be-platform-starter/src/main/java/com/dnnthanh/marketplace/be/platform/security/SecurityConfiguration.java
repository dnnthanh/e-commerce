package com.dnnthanh.marketplace.be.platform.security;

import com.dnnthanh.marketplace.be.platform.web.security.ApiAccessDeniedHandler;
import com.dnnthanh.marketplace.be.platform.web.security.ApiAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/** Common resource-server security baseline. */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMethodSecurity
public class SecurityConfiguration {

    /**
     * Protects private/internal routes while leaving explicitly public marketplace routes
     * anonymous.
     *
     * @param http Spring Security HTTP builder
     * @param authenticationEntryPoint shared 401 response writer
     * @param accessDeniedHandler shared 403 response writer
     * @return configured filter chain
     * @throws Exception when security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler)
            throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        registry ->
                                registry.requestMatchers(
                                                "/actuator/health/**",
                                                "/actuator/prometheus",
                                                "/error")
                                        .permitAll()
                                        .requestMatchers("/private/**", "/internal/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(authenticationEntryPoint)
                                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(
                        resource ->
                                resource.authenticationEntryPoint(authenticationEntryPoint)
                                        .accessDeniedHandler(accessDeniedHandler)
                                        .jwt(Customizer.withDefaults()))
                .build();
    }
}
