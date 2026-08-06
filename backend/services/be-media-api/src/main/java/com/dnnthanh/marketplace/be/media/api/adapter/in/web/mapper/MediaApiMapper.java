package com.dnnthanh.marketplace.be.media.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.media.api.api.request.CreateUploadRequest;
import com.dnnthanh.marketplace.be.media.api.api.response.Readiness;
import com.dnnthanh.marketplace.be.media.api.api.response.UploadSession;
import com.dnnthanh.marketplace.be.media.api.api.response.VariantView;
import com.dnnthanh.marketplace.be.media.api.application.command.CreateMediaUploadCommand;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaReadinessResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaUploadSessionResult;
import com.dnnthanh.marketplace.be.media.api.application.dto.MediaVariantResult;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps Media HTTP transport contracts to/from application objects. */
@Mapper(config = PlatformMapperConfig.class)
public interface MediaApiMapper extends MapperContract {
    CreateMediaUploadCommand toCommand(CreateUploadRequest request);

    UploadSession toResponse(MediaUploadSessionResult result);

    VariantView toResponse(MediaVariantResult result);

    Readiness toResponse(MediaReadinessResult result);
}
