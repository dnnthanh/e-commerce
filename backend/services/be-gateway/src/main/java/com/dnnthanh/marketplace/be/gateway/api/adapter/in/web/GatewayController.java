package com.dnnthanh.marketplace.be.gateway.api.adapter.in.web;

import com.dnnthanh.marketplace.be.gateway.api.api.GatewayApi;
import com.dnnthanh.marketplace.be.gateway.api.api.response.ServiceStatusResponse;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController implements GatewayApi {

    @Override
    public ServiceStatusResponse privateStatus() {
        return new ServiceStatusResponse("be-gateway", "gateway", LocalDateTime.now(), true);
    }
}
