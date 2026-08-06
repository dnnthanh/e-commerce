package com.dnnthanh.marketplace.be.settlement.api.adapter.in.web;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import com.dnnthanh.marketplace.be.settlement.api.adapter.in.web.mapper.SettlementApiMapper;
import com.dnnthanh.marketplace.be.settlement.api.api.SettlementApi;
import com.dnnthanh.marketplace.be.settlement.api.api.request.ApprovalRequest;
import com.dnnthanh.marketplace.be.settlement.api.api.request.search.SettlementSearchRequest;
import com.dnnthanh.marketplace.be.settlement.api.api.response.SettlementView;
import com.dnnthanh.marketplace.be.settlement.api.application.port.in.SettlementUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettlementController implements SettlementApi {
    private final SettlementUseCase useCase;
    private final SettlementApiMapper mapper;

    @Override
    public ApiResponse<List<SettlementView>> list(
            SettlementSearchRequest request, Pageable pageable) {
        Page<SettlementView> page =
                useCase.list(mapper.requestToModel(request), pageable).map(mapper::modelToResponse);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }

    @Override
    public void approve(String settlementNo, ApprovalRequest request) {
        useCase.approve(settlementNo, request.reason());
    }
}
