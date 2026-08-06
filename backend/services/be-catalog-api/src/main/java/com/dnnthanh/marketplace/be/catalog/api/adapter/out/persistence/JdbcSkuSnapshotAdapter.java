package com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.SkuCheckoutSnapshot;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.SkuSnapshotPort;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Projection adapter for a join-heavy internal SKU snapshot. JdbcClient is retained deliberately:
 * this is a read projection over SKU + Product, not ordinary aggregate CRUD.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcSkuSnapshotAdapter implements SkuSnapshotPort {

    private final JdbcClient jdbc;

    /** {@inheritDoc} */
    @Override
    public Optional<SkuCheckoutSnapshot> findBySkuId(Long skuId) {
        return jdbc.sql(
                        """
            SELECT s.id AS sku_id,p.id AS product_id,p.seller_id,
                   (s.active AND p.status='PUBLISHED') AS active,
                   s.purchase_limit
            FROM sku s
            JOIN product p ON p.id=s.product_id
            WHERE s.id=:sku
            """)
                .param("sku", skuId)
                .query(
                        (rs, row) ->
                                new SkuCheckoutSnapshot(
                                        rs.getLong("sku_id"),
                                        rs.getLong("product_id"),
                                        rs.getLong("seller_id"),
                                        rs.getBoolean("active"),
                                        rs.getInt("purchase_limit")))
                .optional();
    }
}
