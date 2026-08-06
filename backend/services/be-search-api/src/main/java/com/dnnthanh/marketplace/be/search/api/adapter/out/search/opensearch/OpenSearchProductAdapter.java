package com.dnnthanh.marketplace.be.search.api.adapter.out.search.opensearch;

import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.search.api.application.exception.InvalidSearchCriteriaException;
import com.dnnthanh.marketplace.be.search.api.application.port.out.ProductSearchPort;
import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.search.api.application.query.SearchCursor;
import com.dnnthanh.marketplace.be.search.api.config.SearchOpenSearchProperties;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** OpenSearch adapter keeping query/index DSL outside application and domain code. */
@Adapter
@RequiredArgsConstructor
public class OpenSearchProductAdapter implements ProductSearchPort {
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper mapper;
    private final SearchOpenSearchProperties properties;

    @Override
    public SearchPage search(ProductSearchCriteria criteria) {
        ObjectNode request = mapper.createObjectNode();
        request.put("size", criteria.size());
        request.set("query", buildQuery(criteria));
        request.set("sort", buildSort(criteria.sort()));
        request.set("aggs", buildAggregations());
        appendSearchAfter(request, criteria);

        JsonNode response =
                client().post()
                        .uri("/{index}/_search", properties.getIndex())
                        .body(request)
                        .retrieve()
                        .body(JsonNode.class);
        return mapResponse(response, criteria);
    }

    @Override
    public void index(String productId, long sourceVersion, Map<String, Object> document) {
        client().put()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/{index}/_doc/{id}")
                                        .queryParam("version", sourceVersion)
                                        .queryParam("version_type", "external_gte")
                                        .build(properties.getIndex(), productId))
                .body(document)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void delete(String productId, long sourceVersion) {
        client().delete()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/{index}/_doc/{id}")
                                        .queryParam("version", sourceVersion)
                                        .queryParam("version_type", "external_gte")
                                        .build(properties.getIndex(), productId))
                .retrieve()
                .toBodilessEntity();
    }

    private ObjectNode buildQuery(ProductSearchCriteria criteria) {
        ObjectNode bool = mapper.createObjectNode();
        ArrayNode filters = mapper.createArrayNode();
        addTerms(filters, "categoryId", criteria.categoryIds());
        addTerms(filters, "brandId", criteria.brandIds());
        addTerms(filters, "sellerId", criteria.sellerIds());
        if (criteria.inStockOnly()) {
            filters.addObject().set("term", mapper.createObjectNode().put("inStock", true));
        }
        if (criteria.minPrice() != null || criteria.maxPrice() != null) {
            ObjectNode bounds = mapper.createObjectNode();
            if (criteria.minPrice() != null) {
                bounds.put("gte", criteria.minPrice());
            }
            if (criteria.maxPrice() != null) {
                bounds.put("lte", criteria.maxPrice());
            }
            filters.addObject().set("range", mapper.createObjectNode().set("price", bounds));
        }
        bool.set("filter", filters);
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            ObjectNode multiMatch = mapper.createObjectNode().put("query", criteria.keyword());
            multiMatch.set(
                    "fields",
                    mapper.createArrayNode().add("name^4").add("brandName^2").add("description"));
            multiMatch.put("fuzziness", "AUTO");
            bool.set("must", mapper.createArrayNode().addObject().set("multi_match", multiMatch));
        }
        return mapper.createObjectNode().set("bool", bool);
    }

    private ArrayNode buildSort(ProductSearchCriteria.Sort sort) {
        ArrayNode values = mapper.createArrayNode();
        switch (sort) {
            case RELEVANCE -> values.addObject().put("_score", "desc");
            case PRICE_ASC -> values.addObject().put("price", "asc");
            case PRICE_DESC -> values.addObject().put("price", "desc");
            case NEWEST -> values.addObject().put("updatedAt", "desc");
            case POPULARITY -> values.addObject().put("popularity", "desc");
        }
        values.addObject().put("productId", "asc");
        return values;
    }

    private ObjectNode buildAggregations() {
        ObjectNode aggs = mapper.createObjectNode();
        aggs.set("categories", termsAggregation("categoryId"));
        aggs.set("brands", termsAggregation("brandId"));
        aggs.set("sellers", termsAggregation("sellerId"));
        return aggs;
    }

    private ObjectNode termsAggregation(String field) {
        return mapper.createObjectNode()
                .set("terms", mapper.createObjectNode().put("field", field).put("size", 50));
    }

    private void appendSearchAfter(ObjectNode request, ProductSearchCriteria criteria) {
        List<String> values = SearchCursor.decode(criteria.cursor());
        if (values.isEmpty()) {
            return;
        }
        if (values.size() != 2) {
            throw new InvalidSearchCriteriaException(
                    "Search cursor does not match sort definition");
        }
        ArrayNode searchAfter = mapper.createArrayNode();
        switch (criteria.sort()) {
            case RELEVANCE -> searchAfter.add(Double.parseDouble(values.get(0)));
            case PRICE_ASC, PRICE_DESC -> searchAfter.add(new BigDecimal(values.get(0)));
            case NEWEST -> searchAfter.add(values.get(0));
            case POPULARITY -> searchAfter.add(Long.parseLong(values.get(0)));
        }
        searchAfter.add(Long.parseLong(values.get(1)));
        request.set("search_after", searchAfter);
    }

    private SearchPage mapResponse(JsonNode root, ProductSearchCriteria criteria) {
        if (root == null) {
            return new SearchPage(List.of(), 0, null, Map.of());
        }
        List<Map<String, Object>> hits = new ArrayList<>();
        String nextCursor = null;
        for (JsonNode hit : root.path("hits").path("hits")) {
            hits.add(mapper.convertValue(hit.path("_source"), Map.class));
            JsonNode sort = hit.path("sort");
            if (sort.isArray() && sort.size() == 2) {
                nextCursor =
                        SearchCursor.encode(List.of(sort.get(0).asText(), sort.get(1).asText()));
            }
        }
        if (hits.size() < criteria.size()) {
            nextCursor = null;
        }
        long total = root.path("hits").path("total").path("value").asLong();
        return new SearchPage(hits, total, nextCursor, mapFacets(root.path("aggregations")));
    }

    private Map<String, Map<String, Long>> mapFacets(JsonNode aggregations) {
        Map<String, Map<String, Long>> facets = new LinkedHashMap<>();
        for (String name : List.of("categories", "brands", "sellers")) {
            Map<String, Long> buckets = new LinkedHashMap<>();
            for (JsonNode bucket : aggregations.path(name).path("buckets")) {
                buckets.put(
                        bucket.path("key_as_string").asText(bucket.path("key").asText()),
                        bucket.path("doc_count").asLong());
            }
            facets.put(name, buckets);
        }
        return facets;
    }

    private void addTerms(ArrayNode filters, String field, Iterable<Long> values) {
        ArrayNode terms = mapper.createArrayNode();
        values.forEach(terms::add);
        if (!terms.isEmpty()) {
            filters.addObject().set("terms", mapper.createObjectNode().set(field, terms));
        }
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getUrl()).build();
    }
}
