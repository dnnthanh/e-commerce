package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputeRequest(@NotBlank @Size(max = 500) String reason) {}
