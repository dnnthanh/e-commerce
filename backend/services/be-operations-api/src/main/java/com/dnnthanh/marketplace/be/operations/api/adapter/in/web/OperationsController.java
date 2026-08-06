package com.dnnthanh.marketplace.be.operations.api.adapter.in.web;

import com.dnnthanh.marketplace.be.operations.api.adapter.in.web.mapper.OperationsApiMapper;
import com.dnnthanh.marketplace.be.operations.api.api.OperationsApi;
import com.dnnthanh.marketplace.be.operations.api.api.request.RecoverRequest;
import com.dnnthanh.marketplace.be.operations.api.api.response.IncidentView;
import com.dnnthanh.marketplace.be.operations.api.application.port.in.OperationsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OperationsController implements OperationsApi {
    private final OperationsUseCase useCase;
    private final OperationsApiMapper mapper;

    @Override
    public List<IncidentView> open() {
        return useCase.open().stream().map(mapper::modelToResponse).toList();
    }

    @Override
    public void recover(Long id, RecoverRequest request) {
        useCase.recover(id, request.reason());
    }

    @Override
    public void resolve(Long id, RecoverRequest request) {
        useCase.resolve(id, request.reason());
    }
}
