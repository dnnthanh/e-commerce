package com.dnnthanh.marketplace.be.search.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "opensearch")
@Validated
@Getter
@Setter
public class SearchOpenSearchProperties {

    @NotBlank private String url;
    @NotBlank private String index;
}
