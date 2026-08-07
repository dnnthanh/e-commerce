package com.dnnthanh.marketplace.be.media.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "minio")
@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinioProperties {

    @NotBlank private String endpoint;
    @NotBlank private String publicBaseUrl;
    @NotBlank private String accessKey;
    @NotBlank private String secretKey;
    @NotBlank private String bucket;
}
