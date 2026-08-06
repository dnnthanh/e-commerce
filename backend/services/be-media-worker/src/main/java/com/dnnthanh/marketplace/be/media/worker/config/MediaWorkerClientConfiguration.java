package com.dnnthanh.marketplace.be.media.worker.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class MediaWorkerClientConfiguration {

    @Bean
    RestClient imgproxyRestClient(RestClient.Builder builder, ImgproxyProperties properties) {
        return builder.baseUrl(properties.getUrl()).build();
    }

    @Bean
    MinioClient mediaWorkerMinioClient(MediaWorkerStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
