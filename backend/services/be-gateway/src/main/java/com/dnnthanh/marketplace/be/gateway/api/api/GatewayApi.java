package com.dnnthanh.marketplace.be.gateway.api.api;

import com.dnnthanh.marketplace.be.gateway.api.api.response.ServiceStatusResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Interface-driven HTTP contract for the gateway bounded context. */
@RequestMapping("/private/gateway")
public interface GatewayApi {

    /**
     * Returns authenticated service-state metadata for operational UI integration.
     *
     * @return private service status
     */
    @GetMapping("/status")
    @PreAuthorize(
            """
        @authorizationService.hasPermission('PLATFORM_VIEW')
        """)
    ServiceStatusResponse privateStatus();
}
