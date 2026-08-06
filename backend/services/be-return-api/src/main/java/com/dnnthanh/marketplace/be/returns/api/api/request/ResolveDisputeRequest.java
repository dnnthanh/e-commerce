package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ResolveDisputeRequest(
        boolean accepted, @Size(max = 500) String reason, List<@Valid InspectLineRequest> lines) {}
