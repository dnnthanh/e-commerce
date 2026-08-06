package com.dnnthanh.marketplace.be.payment.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.payment.api.api.request.CreatePaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.InternalPaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.search.PaymentSearchRequest;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentView;
import com.dnnthanh.marketplace.be.payment.api.api.response.RefundResponse;
import com.dnnthanh.marketplace.be.payment.api.application.command.CreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.application.command.InternalCreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentUseCase.Result;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.RefundUseCase.RefundResult;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSummary;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps payment HTTP contracts to application commands/results. */
@Mapper(config = PlatformMapperConfig.class)
public interface PaymentApiMapper extends MapperContract {

    CreatePaymentCommand toCommand(CreatePaymentRequest request);

    InternalCreatePaymentCommand toCommand(InternalPaymentRequest request);

    PaymentSearchCriteria toCriteria(PaymentSearchRequest request);

    default PaymentResponse toResponse(Result result) {
        var payment = result.payment();
        return new PaymentResponse(
                payment.paymentKey(),
                payment.orderId(),
                payment.provider(),
                payment.amount(),
                payment.status(),
                result.redirectUrl(),
                payment.updatedAt());
    }

    PaymentView toView(PaymentSummary summary);

    RefundResponse toResponse(RefundResult result);
}
