package com.dnnthanh.marketplace.be.returns.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.returns.api.api.request.CreateReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.api.request.InspectLineRequest;
import com.dnnthanh.marketplace.be.returns.api.api.response.ReturnView;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.CreateReturnCommand;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.InspectLineCommand;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.ReturnResult;
import java.util.List;
import org.mapstruct.Mapper;

/** Maps HTTP return DTOs to application commands/results. */
@Mapper(config = PlatformMapperConfig.class)
public interface ReturnApiMapper extends MapperContract {
    CreateReturnCommand toCommand(CreateReturnRequest request);

    InspectLineCommand toCommand(InspectLineRequest request);

    List<InspectLineCommand> toCommands(List<InspectLineRequest> requests);

    ReturnView toView(ReturnResult result);
}
