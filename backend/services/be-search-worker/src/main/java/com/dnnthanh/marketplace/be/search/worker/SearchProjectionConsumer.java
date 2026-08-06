package com.dnnthanh.marketplace.be.search.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.search.worker.config.SearchWorkerClientProperties;
import com.dnnthanh.marketplace.be.search.worker.config.SearchWorkerProperties;
import com.dnnthanh.marketplace.be.search.worker.exception.SearchProjectionException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

/** Maintains the OpenSearch product read model from typed durable domain events. */
@Adapter
@RequiredArgsConstructor
public class SearchProjectionConsumer extends BaseDomainEventConsumer {

    private final RestClient.Builder restClientBuilder;
    private final SearchWorkerProperties searchProperties;
    private final SearchWorkerClientProperties clientProperties;

    @KafkaListener(topics = "marketplace.catalog.events", groupId = "be-search-worker-catalog-v2")
    public void onCatalog(DomainEvent event) {
        if (!accepts(event, "PRODUCT_CHANGED")) {
            return;
        }
        Map<String, Object> product = payload(event);
        String status = String.valueOf(product.getOrDefault("status", "DRAFT"));
        if (!"PUBLISHED".equals(status)) {
            openSearch()
                    .delete()
                    .uri(
                            "/{index}/_doc/{productId}",
                            searchProperties.getIndex(),
                            requiredString(event, "productId"))
                    .retrieve()
                    .toBodilessEntity();
            return;
        }
        openSearch()
                .put()
                .uri(
                        "/{index}/_doc/{productId}",
                        searchProperties.getIndex(),
                        requiredString(event, "productId"))
                .body(product)
                .retrieve()
                .toBodilessEntity();
    }

    @KafkaListener(topics = "marketplace.review.events", groupId = "be-search-worker-review-v2")
    public void onReview(DomainEvent event) {
        if (!accepts(event, "REVIEW_CHANGED")) {
            return;
        }
        long productId = requiredLong(event, "productId");
        ReviewSummary summary =
                reviewApi()
                        .get()
                        .uri("/reviews/summary?productId={productId}", productId)
                        .retrieve()
                        .body(ReviewSummary.class);
        if (summary == null) {
            throw new SearchProjectionException(
                    "Review summary was not returned for product " + productId);
        }
        openSearch()
                .post()
                .uri("/{index}/_update/{productId}", searchProperties.getIndex(), productId)
                .body(
                        Map.of(
                                "doc",
                                Map.of(
                                        "rating", summary.averageRating(),
                                        "reviewCount", summary.reviewCount())))
                .retrieve()
                .toBodilessEntity();
    }

    private RestClient openSearch() {
        return restClientBuilder.clone().baseUrl(searchProperties.getUrl()).build();
    }

    private RestClient reviewApi() {
        return restClientBuilder.clone().baseUrl(clientProperties.getReview()).build();
    }

    private record ReviewSummary(Long productId, double averageRating, long reviewCount) {}
}
