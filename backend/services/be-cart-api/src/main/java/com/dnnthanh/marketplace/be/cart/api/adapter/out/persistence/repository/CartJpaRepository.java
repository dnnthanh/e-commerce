package com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity.CartJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for ordinary cart aggregate persistence. */
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<CartJpaEntity> findByCartKeyAndStatus(String cartKey, String status);

    void deleteByCartKey(String cartKey);
}
