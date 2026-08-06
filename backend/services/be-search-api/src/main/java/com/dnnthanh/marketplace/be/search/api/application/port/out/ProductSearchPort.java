package com.dnnthanh.marketplace.be.search.api.application.port.out;

import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria;
import java.util.List;
import java.util.Map;

/** Search-engine adapter boundary. */
public interface ProductSearchPort {
    SearchPage search(ProductSearchCriteria criteria);

    void index(String productId, long sourceVersion, Map<String, Object> document);

    void delete(String productId, long sourceVersion);

    record SearchPage(
            List<Map<String, Object>> hits,
            long total,
            String nextCursor,
            Map<String, Map<String, Long>> facets) {}
}
