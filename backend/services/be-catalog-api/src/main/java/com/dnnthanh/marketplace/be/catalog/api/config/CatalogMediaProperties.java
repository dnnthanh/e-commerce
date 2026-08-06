package com.dnnthanh.marketplace.be.catalog.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "media")
@Validated
@Getter
@Setter
public class CatalogMediaProperties {

    @NotBlank private String baseUrl;
}
