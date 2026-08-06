package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReturnRequest(
        @NotBlank @Size(max = 128) String requestKey,
        @NotBlank @Size(max = 64) String orderNo,
        @NotEmpty List<@NotNull @Positive Long> orderLineIds,
        @NotBlank @Size(max = 255) String reason) {}
