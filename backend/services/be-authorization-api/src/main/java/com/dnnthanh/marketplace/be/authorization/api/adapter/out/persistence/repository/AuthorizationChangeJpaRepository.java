package com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.entity.AuthorizationChangeJpaEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** JPA repository for mutation lifecycle plus native SQL for the pending reconciliation search. */
public interface AuthorizationChangeJpaRepository
        extends JpaRepository<AuthorizationChangeJpaEntity, Long> {

    Optional<AuthorizationChangeJpaEntity> findByChangeId(String changeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
      update AuthorizationChangeJpaEntity change
         set change.status = 'APPLIED',
             change.appliedAt = :now,
             change.lastError = null,
             change.attemptCount = change.attemptCount + 1
       where change.changeId = :changeId
         and change.status <> 'APPLIED'
      """)
    int markApplied(@Param("changeId") String changeId, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
      update AuthorizationChangeJpaEntity change
         set change.status = 'PENDING',
             change.lastError = :error,
             change.attemptCount = change.attemptCount + 1,
             change.lastAttemptAt = :now
       where change.changeId = :changeId
         and change.status <> 'APPLIED'
      """)
    int markFailed(
            @Param("changeId") String changeId,
            @Param("error") String error,
            @Param("now") LocalDateTime now);

    @Query(
            value =
                    """
          SELECT *
          FROM authorization_change_log
          WHERE status = 'PENDING'
          ORDER BY changed_at, id
          LIMIT :limit
          """,
            nativeQuery = true)
    List<AuthorizationChangeJpaEntity> findPendingNative(@Param("limit") int limit);
}
