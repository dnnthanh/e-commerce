package com.dnnthanh.marketplace.be.returns.api.application.port.out;

import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import java.util.Optional;

/** Canonical aggregate persistence boundary backed by Spring Data JPA. */
public interface ReturnRepositoryPort {
    Optional<ReturnRequest> loadByKey(String returnKey);

    ReturnRequest save(ReturnRequest request);
}
