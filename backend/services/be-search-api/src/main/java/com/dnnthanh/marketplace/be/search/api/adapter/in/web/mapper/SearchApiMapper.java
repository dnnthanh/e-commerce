package com.dnnthanh.marketplace.be.search.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.search.api.api.request.search.ProductSearchRequest;
import com.dnnthanh.marketplace.be.search.api.api.response.ProductHit;
import com.dnnthanh.marketplace.be.search.api.api.response.SearchResponse;
import com.dnnthanh.marketplace.be.search.api.application.port.out.ProductSearchPort;
import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria;
import java.math.BigDecimal;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps HTTP search requests/results without leaking transport objects into application ports. */
@Mapper(config = PlatformMapperConfig.class)
public interface SearchApiMapper extends MapperContract {
    @Mapping(target = "keyword", source = "query")
    @Mapping(target = "sellerIds", source = "sellerId")
    @Mapping(target = "categoryIds", source = "categoryId")
    @Mapping(target = "brandIds", source = "brandId")
    ProductSearchCriteria toCriteria(ProductSearchRequest request);

    @Mapping(target = "items", source = "hits")
    SearchResponse toResponse(ProductSearchPort.SearchPage page);

    default ProductHit toHit(Map<String, Object> source) {
        return new ProductHit(
                longValue(source.get("productId")),
                longValue(source.get("sellerId")),
                String.valueOf(source.getOrDefault("name", "")),
                decimalValue(source.get("price")),
                doubleValue(source.get("rating")));
    }

    private static Long longValue(Object value) {
        return value instanceof Number number
                ? number.longValue()
                : Long.valueOf(String.valueOf(value));
    }

    private static BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    private static double doubleValue(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0D;
    }
}
