package com.dnnthanh.marketplace.be.payment.api.api;

import com.dnnthanh.marketplace.be.payment.api.api.request.CreatePaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.search.PaymentSearchRequest;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentView;
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

/** Authenticated payment API for customer creation and operations visibility. */
@RequestMapping("/private/payments")
public interface PaymentPrivateApi {

    /** Creates an idempotent customer payment. */
    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('PAYMENT_VIEW')")
    PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request);

    /** Lists recent payments using Spring-managed pagination. */
    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('PAYMENT_VIEW')")
    ApiResponse<List<PaymentView>> list(
            @Valid @ModelAttribute PaymentSearchRequest request,
            @PageableDefault(size = 100) Pageable pageable);
}
