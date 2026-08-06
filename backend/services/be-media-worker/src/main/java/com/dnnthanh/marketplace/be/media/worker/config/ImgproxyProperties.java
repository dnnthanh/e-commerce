package com.dnnthanh.marketplace.be.media.worker.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "imgproxy")
@Validated
@Getter
@Setter
public class ImgproxyProperties {

    @NotBlank private String url;
}
