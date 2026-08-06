package com.dnnthanh.marketplace.be.returns.api.adapter.in.web;

import com.dnnthanh.marketplace.be.returns.api.adapter.in.web.mapper.ReturnApiMapper;
import com.dnnthanh.marketplace.be.returns.api.api.ReturnApi;
import com.dnnthanh.marketplace.be.returns.api.api.request.CreateReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.DisputeRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.InspectReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.ReceiveReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.ResolveDisputeRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.SellerReturnAction;
import com.dnnthanh.marketplace.be.returns.api.api.response.ReturnView;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReturnController implements ReturnApi {
    private final ReturnUseCase returns;
    private final ReturnApiMapper mapper;

    @Override
    public ReturnView create(CreateReturnRequest request) {
        return mapper.toView(returns.create(mapper.toCommand(request)));
    }

    @Override
    public List<ReturnView> list() {
        return returns.list().stream().map(mapper::toView).toList();
    }

    @Override
    public ReturnView approve(String returnKey, SellerReturnAction request) {
        return mapper.toView(returns.approve(returnKey, request.sellerId()));
    }

    @Override
    public ReturnView reject(String returnKey, SellerReturnAction request) {
        return mapper.toView(returns.reject(returnKey, request.sellerId(), request.reason()));
    }

    @Override
    public ReturnView receive(String returnKey, ReceiveReturnRequest request) {
        return mapper.toView(returns.receive(returnKey, request.warehouseId()));
    }

    @Override
    public ReturnView inspect(String returnKey, InspectReturnRequest request) {
        return mapper.toView(returns.inspect(returnKey, mapper.toCommands(request.lines())));
    }

    @Override
    public ReturnView dispute(String returnKey, DisputeRequest request) {
        return mapper.toView(returns.openDispute(returnKey, request.reason()));
    }

    @Override
    public ReturnView resolveDispute(String returnKey, ResolveDisputeRequest request) {
        return mapper.toView(
                returns.resolveDispute(
                        returnKey,
                        request.accepted(),
                        mapper.toCommands(request.lines()),
                        request.reason()));
    }

    @Override
    public ReturnView refund(String returnKey) {
        return mapper.toView(returns.refund(returnKey));
    }
}
