package com.dnnthanh.marketplace.be.returns.api.api;

import com.dnnthanh.marketplace.be.returns.api.api.request.CreateReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.DisputeRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.InspectReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.ReceiveReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.ResolveDisputeRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.SellerReturnAction;
import com.dnnthanh.marketplace.be.returns.api.api.response.ReturnView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Customer, seller and operations return/refund contract. */
@RequestMapping("/private/returns")
public interface ReturnApi {

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('RETURN_CREATE')")
    ReturnView create(@Valid @RequestBody CreateReturnRequest request);

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('RETURN_VIEW')")
    List<ReturnView> list();

    @PostMapping("/{returnKey}/approve")
    @PreAuthorize("@authorizationService.hasSellerPermission('RETURN_VIEW', #request.sellerId())")
    ReturnView approve(
            @PathVariable String returnKey, @Valid @RequestBody SellerReturnAction request);

    @PostMapping("/{returnKey}/reject")
    @PreAuthorize("@authorizationService.hasSellerPermission('RETURN_VIEW', #request.sellerId())")
    ReturnView reject(
            @PathVariable String returnKey, @Valid @RequestBody SellerReturnAction request);

    @PostMapping("/{returnKey}/receive")
    @PreAuthorize("@authorizationService.hasPermission('RETURN_RECEIVE')")
    ReturnView receive(
            @PathVariable String returnKey, @Valid @RequestBody ReceiveReturnRequest request);

    @PostMapping("/{returnKey}/inspect")
    @PreAuthorize("@authorizationService.hasPermission('RETURN_RECEIVE')")
    ReturnView inspect(
            @PathVariable String returnKey, @Valid @RequestBody InspectReturnRequest request);

    @PostMapping("/{returnKey}/disputes")
    @PreAuthorize("@authorizationService.hasPermission('RETURN_CREATE')")
    ReturnView dispute(@PathVariable String returnKey, @Valid @RequestBody DisputeRequest request);

    @PostMapping("/{returnKey}/disputes/resolve")
    @PreAuthorize("@authorizationService.hasPermission('RETURN_RECEIVE')")
    ReturnView resolveDispute(
            @PathVariable String returnKey, @Valid @RequestBody ResolveDisputeRequest request);

    @PostMapping("/{returnKey}/refund")
    @PreAuthorize("@authorizationService.hasPermission('PAYMENT_REFUND')")
    ReturnView refund(@PathVariable String returnKey);
}
