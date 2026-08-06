package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity.ReturnRequestJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for normal Return CRUD/query persistence. */
public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequestJpaEntity, Long> {
    Optional<ReturnRequestJpaEntity> findByReturnKey(String returnKey);

    @Query(
            value =
                    """
                    SELECT *
                    FROM return_request
                    WHERE user_id = :userId
                    ORDER BY id DESC
                    LIMIT 100
                    """,
            nativeQuery = true)
    List<ReturnRequestJpaEntity> findRecentByUserNative(@Param("userId") String userId);
}
