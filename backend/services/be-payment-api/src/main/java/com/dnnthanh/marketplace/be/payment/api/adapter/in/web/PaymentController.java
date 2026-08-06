package com.dnnthanh.marketplace.be.payment.api.adapter.in.web;

import com.dnnthanh.marketplace.be.payment.api.adapter.in.web.mapper.PaymentApiMapper;
import com.dnnthanh.marketplace.be.payment.api.api.PaymentInternalApi;
import com.dnnthanh.marketplace.be.payment.api.api.PaymentPrivateApi;
import com.dnnthanh.marketplace.be.payment.api.api.request.CreatePaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.InternalPaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.InternalRefundRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.search.PaymentSearchRequest;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentView;
import com.dnnthanh.marketplace.be.payment.api.api.response.RefundResponse;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentQueryUseCase;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentUseCase;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.RefundUseCase;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentPrivateApi, PaymentInternalApi {
    private final PaymentUseCase payments;
    private final RefundUseCase refunds;
    private final PaymentQueryUseCase queries;
    private final PaymentApiMapper mapper;

    @Override
    public PaymentResponse create(CreatePaymentRequest request) {
        return mapper.toResponse(payments.createForCurrentUser(mapper.toCommand(request)));
    }

    @Override
    public ApiResponse<List<PaymentView>> list(PaymentSearchRequest request, Pageable pageable) {
        Page<PaymentView> page =
                queries.list(mapper.toCriteria(request), pageable).map(mapper::toView);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }

    @Override
    public PaymentResponse createInternal(InternalPaymentRequest request) {
        return mapper.toResponse(payments.create(mapper.toCommand(request)));
    }

    @Override
    public RefundResponse refund(InternalRefundRequest request) {
        return mapper.toResponse(
                refunds.refund(
                        request.refundKey(),
                        request.orderId(),
                        request.returnKey(),
                        request.amount()));
    }

    @Override
    public PaymentResponse reconcile(String paymentKey) {
        return mapper.toResponse(payments.reconcile(paymentKey));
    }
}
