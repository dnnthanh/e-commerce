package com.dnnthanh.marketplace.be.search.api.api.response;

import java.util.List;
import java.util.Map;

public record SearchResponse(List<ProductHit> items, Map<String, Map<String, Long>> facets) {}
