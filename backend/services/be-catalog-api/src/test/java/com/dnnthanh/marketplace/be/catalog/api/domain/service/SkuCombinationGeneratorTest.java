package com.dnnthanh.marketplace.be.catalog.api.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SkuCombinationGeneratorTest {
    @Test
    void createsCartesianProductWithoutDuplicateValues() {
        Map<String, List<String>> a = new LinkedHashMap<>();
        a.put("size", List.of("S", "M", "M"));
        a.put("color", List.of("black", "white"));
        assertEquals(4, new SkuCombinationGenerator().generate(a).size());
    }
}
