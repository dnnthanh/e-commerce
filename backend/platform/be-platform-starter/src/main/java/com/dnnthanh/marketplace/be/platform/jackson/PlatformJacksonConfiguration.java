package com.dnnthanh.marketplace.be.platform.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

/** Shared Jackson 3 JSON boundary configuration. */
@Configuration
public class PlatformJacksonConfiguration {

    /**
     * Omits null object properties and null container content from API JSON.
     *
     * @return JsonMapper builder customizer
     */
    @Bean
    public JsonMapperBuilderCustomizer nonNullPropertyCustomizer() {
        return builder ->
                builder.changeDefaultPropertyInclusion(
                        inclusion ->
                                inclusion
                                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                                        .withContentInclusion(JsonInclude.Include.NON_NULL));
    }

    /**
     * Trims normal incoming string values once at the JSON boundary.
     *
     * @return JsonMapper builder customizer
     */
    @Bean
    public JsonMapperBuilderCustomizer stringTrimmingCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("marketplace-string-trimming");
            module.addDeserializer(String.class, new TrimmedStringDeserializer());
            builder.addModule(module);
        };
    }
}
