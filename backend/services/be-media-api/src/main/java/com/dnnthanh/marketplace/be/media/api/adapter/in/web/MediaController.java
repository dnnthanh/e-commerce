package com.dnnthanh.marketplace.be.media.api.adapter.in.web;

import com.dnnthanh.marketplace.be.media.api.adapter.in.web.mapper.MediaApiMapper;
import com.dnnthanh.marketplace.be.media.api.api.MediaApi;
import com.dnnthanh.marketplace.be.media.api.api.request.CreateUploadRequest;
import com.dnnthanh.marketplace.be.media.api.api.response.Readiness;
import com.dnnthanh.marketplace.be.media.api.api.response.UploadSession;
import com.dnnthanh.marketplace.be.media.api.api.response.VariantView;
import com.dnnthanh.marketplace.be.media.api.application.port.in.MediaUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MediaController implements MediaApi {
    private final MediaUseCase useCase;
    private final MediaApiMapper mapper;

    @Override
    public UploadSession createUpload(CreateUploadRequest request) {
        return mapper.toResponse(useCase.create(mapper.toCommand(request)));
    }

    @Override
    public void complete(Long assetId) {
        useCase.complete(assetId);
    }

    @Override
    public List<VariantView> productAssets(Long productId) {
        return useCase.variants(productId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public Readiness readiness(Long productId) {
        return mapper.toResponse(useCase.readiness(productId));
    }
}
