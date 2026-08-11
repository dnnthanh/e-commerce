package com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.ProductOffer;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.ProductOfferPort;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Read-only projection over Product + SKU for customer-safe sellable offers. JdbcClient is retained
 * because this is a join projection rather than aggregate CRUD.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcProductOfferAdapter implements ProductOfferPort {

    private static final String SELECT_COLUMNS =
            """
            SELECT s.id AS sku_id,
                   p.id AS product_id,
                   p.seller_id,
                   p.name AS product_name,
                   s.seller_sku,
                   s.variant_name,
                   s.purchase_limit
            FROM sku s
            JOIN product p ON p.id = s.product_id
            """;

    private final JdbcClient jdbc;

    @Override
    public List<ProductOffer> findSellableByProductId(Long productId) {
        return jdbc.sql(
                        SELECT_COLUMNS
                                + """
                                WHERE p.id = :productId
                                  AND p.status = 'PUBLISHED'
                                  AND s.active = TRUE
                                ORDER BY s.id
                                """)
                .param("productId", productId)
                .query(this::mapOffer)
                .list();
    }

    @Override
    public Optional<ProductOffer> findSellableBySkuId(Long skuId) {
        return jdbc.sql(
                        SELECT_COLUMNS
                                + """
                                WHERE s.id = :skuId
                                  AND p.status = 'PUBLISHED'
                                  AND s.active = TRUE
                                """)
                .param("skuId", skuId)
                .query(this::mapOffer)
                .optional();
    }

    private ProductOffer mapOffer(ResultSet resultSet, int rowNum) throws SQLException {
        return new ProductOffer(
                resultSet.getLong("sku_id"),
                resultSet.getLong("product_id"),
                resultSet.getLong("seller_id"),
                resultSet.getString("product_name"),
                resultSet.getString("seller_sku"),
                resultSet.getString("variant_name"),
                resultSet.getInt("purchase_limit"));
    }
}
