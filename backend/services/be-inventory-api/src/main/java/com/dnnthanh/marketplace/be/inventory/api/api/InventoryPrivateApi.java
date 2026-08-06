package com.dnnthanh.marketplace.be.inventory.api.api;

import com.dnnthanh.marketplace.be.inventory.api.api.request.ReserveInventoryRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.search.InventoryBalanceSearchRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.response.BalanceView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.ReservationResponse;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Authenticated inventory management contract. */
@RequestMapping("/private/inventory")
public interface InventoryPrivateApi {
    @PostMapping("/reservations")
    @PreAuthorize("@authorizationService.hasPermission('INVENTORY_UPDATE')")
    ReservationResponse reserve(@Valid @RequestBody ReserveInventoryRequest request);

    @GetMapping("/balances")
    @PreAuthorize("@authorizationService.hasPermission('INVENTORY_VIEW')")
    ApiResponse<List<BalanceView>> balances(
            @Valid @ModelAttribute InventoryBalanceSearchRequest request,
            @PageableDefault(size = 100) Pageable pageable);
}
