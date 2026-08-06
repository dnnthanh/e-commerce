package com.dnnthanh.marketplace.be.comment.api.api.request.search;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped root-thread search request. */
@Getter
@Setter
@NoArgsConstructor
public class CommentSearchRequest {
    @NotNull private Long productId;
}
