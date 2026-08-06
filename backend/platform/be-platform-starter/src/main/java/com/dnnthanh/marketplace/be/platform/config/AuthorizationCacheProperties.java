package com.dnnthanh.marketplace.be.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "authorization")
@Getter
@Setter
public class AuthorizationCacheProperties {

    private long cacheTtlSeconds = 10;
}
