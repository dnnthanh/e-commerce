package com.dnnthanh.marketplace.be.authorization.api.api.response;

import java.util.Set;

public record AuthorizationSnapshot(
        Set<String> roles, Set<String> permissions, Set<Long> sellerIds) {}
