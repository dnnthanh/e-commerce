package com.dnnthanh.marketplace.be.catalog.api.domain.service;

import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidVariantDefinitionException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Generates deterministic variant combinations from variant-generating attributes. */
public final class SkuCombinationGenerator {
    public List<Map<String, String>> generate(Map<String, List<String>> attributes) {
        List<Map<String, String>> result = new ArrayList<>();
        result.add(new LinkedHashMap<>());
        for (var entry : attributes.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                throw new InvalidVariantDefinitionException(entry.getKey());
            List<Map<String, String>> next = new ArrayList<>();
            for (Map<String, String> partial : result) {
                for (String value : entry.getValue().stream().distinct().toList()) {
                    Map<String, String> combination = new LinkedHashMap<>(partial);
                    combination.put(entry.getKey(), value);
                    next.add(Map.copyOf(combination));
                }
            }
            result = next;
        }
        return List.copyOf(result);
    }
}
