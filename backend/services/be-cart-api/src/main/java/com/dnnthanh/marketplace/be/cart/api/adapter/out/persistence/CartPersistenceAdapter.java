package com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity.CartJpaEntity;
import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.mapper.CartPersistenceMapper;
import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.repository.CartJpaRepository;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.CartPersistencePort;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.CartVersionConflictException;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

/** Ordinary Cart CRUD uses JPA; optimistic concurrency is enforced by `@Version`. */
@Persistence
@RequiredArgsConstructor
public class CartPersistenceAdapter implements CartPersistencePort {
    private static final String ACTIVE = "ACTIVE";

    private final CartJpaRepository repository;
    private final CartPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findByCartId(String cartId) {
        return repository.findByCartKeyAndStatus(cartId, ACTIVE).map(mapper::toDomain);
    }

    @Override
    public ShoppingCart create(ShoppingCart cart) {
        try {
            CartJpaEntity entity = CartJpaEntity.newActive(cart.cartId(), cart.ownerId());
            mapper.synchronize(cart, entity);
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException concurrentCreate) {
            return findByCartId(cart.cartId()).orElseThrow(() -> concurrentCreate);
        }
    }

    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart, long expectedVersion) {
        CartJpaEntity entity =
                repository
                        .findByCartKeyAndStatus(cart.cartId(), ACTIVE)
                        .orElseThrow(() -> new CartVersionConflictException(expectedVersion, -1));
        if (entity.getVersion() != expectedVersion) {
            throw new CartVersionConflictException(expectedVersion, entity.getVersion());
        }
        mapper.synchronize(cart, entity);
        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException conflict) {
            long actual =
                    repository
                            .findByCartKeyAndStatus(cart.cartId(), ACTIVE)
                            .map(CartJpaEntity::getVersion)
                            .orElse(-1L);
            throw new CartVersionConflictException(expectedVersion, actual);
        }
    }

    @Override
    @Transactional
    public void delete(String cartId) {
        repository.deleteByCartKey(cartId);
    }
}
