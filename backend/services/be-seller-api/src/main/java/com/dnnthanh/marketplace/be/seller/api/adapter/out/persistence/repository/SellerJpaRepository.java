package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.entity.SellerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Seller aggregate root repository. */
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {}
