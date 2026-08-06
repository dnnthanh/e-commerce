package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InspectReturnRequest(@NotEmpty List<@Valid InspectLineRequest> lines) {}
