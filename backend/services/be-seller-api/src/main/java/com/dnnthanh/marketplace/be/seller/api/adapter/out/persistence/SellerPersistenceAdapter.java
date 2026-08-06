package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.exception.SellerPersistenceException;
import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.mapper.SellerPersistenceMapper;
import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.repository.SellerJpaRepository;
import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.repository.ShopJpaRepository;
import com.dnnthanh.marketplace.be.seller.api.application.port.out.SellerAccountPort;
import com.dnnthanh.marketplace.be.seller.api.application.port.out.ShopRepositoryPort;
import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.model.SellerAccount;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Seller persistence adapter.
 *
 * <p>Shop/seller/staff CRUD is JPA. Seller list reads are native SQL through Spring Data. Raw JDBC
 * remains only for vendor JSON profile-history and transactional-outbox inserts.
 */
@Persistence
@RequiredArgsConstructor
public class SellerPersistenceAdapter implements ShopRepositoryPort, SellerAccountPort {
    private static final String OUTBOX_PENDING = "PENDING";

    private final ShopJpaRepository shops;
    private final SellerJpaRepository sellers;
    private final SellerPersistenceMapper mapper;
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    @Transactional(readOnly = true)
    public Optional<Shop> find(Long id) {
        return shops.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shop> findBySeller(SellerShopSearchCriteria criteria) {
        return shops.findBySellerNative(criteria).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Shop saveMaterialChange(Shop before, Shop after, String actor) {
        if (!Objects.equals(before.id(), after.id())
                || !Objects.equals(before.sellerId(), after.sellerId())) {
            throw new SellerPersistenceException("Shop identity changed during material update");
        }
        var entity =
                shops.findById(after.id())
                        .orElseThrow(
                                () ->
                                        new SellerPersistenceException(
                                                "Shop does not exist: " + after.id()));
        if (!Objects.equals(entity.getUpdatedAt(), before.updatedAt())) {
            throw new SellerPersistenceException("Concurrent shop update detected: " + after.id());
        }
        try {
            String beforeJson =
                    json.writeValueAsString(
                            Map.of(
                                    "name", before.name(),
                                    "description", Objects.toString(before.description(), "")));
            String afterJson =
                    json.writeValueAsString(
                            Map.of(
                                    "name", after.name(),
                                    "description", Objects.toString(after.description(), "")));
            mapper.copy(after, entity);
            Shop saved = mapper.toDomain(shops.saveAndFlush(entity));
            persistProfileHistory(
                    after.sellerId(), actor, beforeJson, afterJson, after.updatedAt());
            emit(
                    after.sellerId(),
                    "SELLER_MATERIAL_INFO_CHANGED",
                    Map.of(
                            "sellerId",
                            after.sellerId(),
                            "shopId",
                            after.id(),
                            "name",
                            after.name()));
            emit(
                    after.sellerId(),
                    "AUDIT_EVENT",
                    Map.of(
                            "actorId",
                            actor,
                            "actorType",
                            "USER",
                            "action",
                            "SELLER_MATERIAL_INFO_CHANGED",
                            "resourceType",
                            "SHOP",
                            "resourceId",
                            after.id(),
                            "sourceService",
                            "be-seller-api",
                            "before",
                            beforeJson,
                            "after",
                            afterJson));
            return saved;
        } catch (SellerPersistenceException known) {
            throw known;
        } catch (Exception failure) {
            throw new SellerPersistenceException("Seller change cannot be serialized", failure);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellerAccount> findBySellerId(Long sellerId) {
        return sellers.findById(sellerId)
                .map(
                        seller ->
                                SellerAccount.rehydrate(
                                        seller.getId(), SellerStatus.valueOf(seller.getStatus())));
    }

    @Override
    @Transactional
    public SellerAccount save(SellerAccount seller) {
        var entity =
                sellers.findById(seller.sellerId())
                        .orElseThrow(
                                () ->
                                        new SellerPersistenceException(
                                                "Seller does not exist: " + seller.sellerId()));
        entity.setStatus(seller.status().name());
        entity.setUpdatedAt(LocalDateTime.now());
        sellers.save(entity);
        emit(
                seller.sellerId(),
                "SELLER_STATUS_CHANGED",
                Map.of("sellerId", seller.sellerId(), "status", seller.status().name()));
        return seller;
    }

    private void persistProfileHistory(
            Long sellerId,
            String actor,
            String beforeJson,
            String afterJson,
            LocalDateTime changedAt) {
        jdbc.sql(
                        """
            INSERT INTO seller_profile_history(
                seller_id, changed_by, before_json, after_json, changed_at)
            VALUES(:sellerId, :actor, CAST(:beforeJson AS JSON), CAST(:afterJson AS JSON), :changedAt)
            """)
                .param("sellerId", sellerId)
                .param("actor", actor)
                .param("beforeJson", beforeJson)
                .param("afterJson", afterJson)
                .param("changedAt", changedAt)
                .update();
    }

    private void emit(Long sellerId, String eventType, Map<String, Object> payload) {
        try {
            jdbc.sql(
                            """
              INSERT INTO outbox_event(
                  event_id, aggregate_id, event_type, payload_json, status, created_at)
              VALUES(:eventId, :aggregateId, :eventType, CAST(:payload AS JSON), :status, :createdAt)
              """)
                    .param("eventId", UUID.randomUUID().toString())
                    .param("aggregateId", sellerId.toString())
                    .param("eventType", eventType)
                    .param("payload", json.writeValueAsString(payload))
                    .param("status", OUTBOX_PENDING)
                    .param("createdAt", LocalDateTime.now())
                    .update();
        } catch (Exception failure) {
            throw new SellerPersistenceException(
                    "Unable to serialize Seller outbox payload", failure);
        }
    }
}
