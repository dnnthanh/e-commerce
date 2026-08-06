package com.dnnthanh.marketplace.be.catalog.api.application.service;

import com.dnnthanh.marketplace.be.catalog.api.domain.model.AttributeDefinition;
import com.dnnthanh.marketplace.be.catalog.api.domain.service.SkuCombinationGenerator;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates dynamic attribute values and generates deterministic SKU variant combinations. */
@UseCase
public class VariantGenerationService {
    private final SkuCombinationGenerator combinationGenerator = new SkuCombinationGenerator();

    public List<Map<String, String>> generate(Map<AttributeDefinition, List<String>> attributes) {
        Map<String, List<String>> variants = new LinkedHashMap<>();
        for (var entry : attributes.entrySet()) {
            AttributeDefinition definition = entry.getKey();
            for (String value : entry.getValue()) definition.validate(value);
            if (definition.variantGenerating()) variants.put(definition.code(), entry.getValue());
        }
        return combinationGenerator.generate(variants);
    }
}
