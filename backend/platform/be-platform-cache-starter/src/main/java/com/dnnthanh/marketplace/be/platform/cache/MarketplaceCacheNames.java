package com.dnnthanh.marketplace.be.platform.cache;

import lombok.experimental.UtilityClass;

/** Shared cache names used across independently deployable marketplace services. */
@UtilityClass
public class MarketplaceCacheNames {

    /** Hot cart snapshots; MySQL remains authoritative. */
    public final String CARTS = "carts";

    /** Effective authorization snapshots resolved from Keycloak. */
    public final String AUTHORIZATION_SNAPSHOTS = "authorization-snapshots";
}
