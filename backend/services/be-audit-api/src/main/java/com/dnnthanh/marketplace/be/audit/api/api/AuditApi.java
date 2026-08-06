package com.dnnthanh.marketplace.be.audit.api.api;

import com.dnnthanh.marketplace.be.audit.api.api.request.search.AuditSearchRequest;
import com.dnnthanh.marketplace.be.audit.api.api.response.AuditResponse;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Immutable administration/security audit query contract. */
@RequestMapping("/private/audit")
public interface AuditApi {

    /** Searches audit history using domain filters plus Spring-managed pagination. */
    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('AUDIT_VIEW')")
    ApiResponse<List<AuditResponse>> search(
            @Valid @ModelAttribute AuditSearchRequest request,
            @PageableDefault(size = 100) Pageable pageable);
}
