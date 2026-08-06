package com.dnnthanh.marketplace.be.checkout.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.persistence.exception.CheckoutPersistenceException;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutSagaRepositoryPort;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL durable Checkout Saga store. */
@Persistence
@RequiredArgsConstructor
public class JdbcCheckoutSagaRepositoryAdapter implements CheckoutSagaRepositoryPort {

    private static final String FIND_SQL =
            """
      SELECT checkout_key,user_id,status,order_id,payment_id,retry_count,next_retry_at
      FROM checkout_saga
      WHERE checkout_key=:key
      """;

    private static final String SNAPSHOT_SQL =
            "SELECT CAST(cart_snapshot_json AS text) FROM checkout_saga WHERE checkout_key=:key";

    private static final String UPSERT_SQL =
            """
      INSERT INTO checkout_saga(
        checkout_key,user_id,status,cart_snapshot_json,order_id,payment_id,
        retry_count,next_retry_at,created_at,updated_at
      ) VALUES(
        :key,:user,:status,CAST(:json AS jsonb),:order,:payment,:retry,:next,now(),now()
      )
      ON CONFLICT(checkout_key) DO UPDATE SET
        status=:status,
        order_id=:order,
        payment_id=:payment,
        retry_count=:retry,
        next_retry_at=:next,
        updated_at=now()
      """;

    private final JdbcClient jdbc;

    @Override
    public Optional<CheckoutSaga> find(String key) {
        return jdbc.sql(FIND_SQL)
                .param("key", key)
                .query(
                        (rs, row) ->
                                CheckoutSaga.rehydrate(
                                        rs.getString(1),
                                        rs.getString(2),
                                        CheckoutSaga.State.valueOf(rs.getString(3)),
                                        rs.getString(4),
                                        rs.getString(5),
                                        rs.getInt(6),
                                        rs.getTimestamp(7) == null
                                                ? null
                                                : rs.getTimestamp(7).toLocalDateTime()))
                .optional();
    }

    @Override
    public Optional<String> findRequestSnapshot(String key) {
        return jdbc.sql(SNAPSHOT_SQL).param("key", key).query(String.class).optional();
    }

    @Override
    @Transactional
    public CheckoutSaga save(CheckoutSaga saga, String snapshotJson) {
        jdbc.sql(UPSERT_SQL)
                .param("key", saga.checkoutKey())
                .param("user", saga.userId())
                .param("status", saga.state().name())
                .param("json", snapshotJson)
                .param("order", saga.orderNo())
                .param("payment", saga.paymentKey())
                .param("retry", saga.retryCount())
                .param("next", saga.nextRetryAt())
                .update();

        return find(saga.checkoutKey())
                .orElseThrow(
                        () ->
                                new CheckoutPersistenceException(
                                        "Checkout saga was not visible after upsert: "
                                                + saga.checkoutKey()));
    }
}
