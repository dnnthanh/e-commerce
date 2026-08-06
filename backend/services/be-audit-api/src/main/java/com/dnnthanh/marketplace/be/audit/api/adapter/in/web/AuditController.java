package com.dnnthanh.marketplace.be.audit.api.adapter.in.web;

import com.dnnthanh.marketplace.be.audit.api.adapter.in.web.mapper.AuditApiMapper;
import com.dnnthanh.marketplace.be.audit.api.api.AuditApi;
import com.dnnthanh.marketplace.be.audit.api.api.request.search.AuditSearchRequest;
import com.dnnthanh.marketplace.be.audit.api.api.response.AuditResponse;
import com.dnnthanh.marketplace.be.audit.api.application.port.in.AuditQueryUseCase;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuditController implements AuditApi {
    private final AuditQueryUseCase auditQueryUseCase;
    private final AuditApiMapper auditApiMapper;

    @Override
    public ApiResponse<List<AuditResponse>> search(AuditSearchRequest request, Pageable pageable) {
        Page<AuditResponse> page =
                auditQueryUseCase
                        .search(auditApiMapper.requestToModel(request), pageable)
                        .map(auditApiMapper::modelToResponse);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }
}
