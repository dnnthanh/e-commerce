package com.dnnthanh.marketplace.be.settlement.api.api;

import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.settlement.api.api.request.ApprovalRequest;
import com.dnnthanh.marketplace.be.settlement.api.api.request.search.SettlementSearchRequest;
import com.dnnthanh.marketplace.be.settlement.api.api.response.SettlementView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/settlements")
public interface SettlementApi {

    @GetMapping
    @PreAuthorize("@authorizationService.hasSellerPermission('SETTLEMENT_VIEW', #request.sellerId)")
    ApiResponse<List<SettlementView>> list(
            @Valid @ModelAttribute SettlementSearchRequest request,
            @PageableDefault(size = 100) Pageable pageable);

    @PostMapping("/{settlementNo}/approve")
    @PreAuthorize("@authorizationService.hasPermission('SETTLEMENT_APPROVE')")
    void approve(@PathVariable String settlementNo, @RequestBody ApprovalRequest request);
}
