package com.dnnthanh.marketplace.be.authorization.api.adapter.out.external.keycloak;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses canonical Keycloak seller/shop group paths into resource-scope membership. */
public final class KeycloakGroupScopeResolver {
    private static final Pattern SELLER = Pattern.compile("^/sellers/seller-(\\d+)$");
    private static final Pattern SHOP =
            Pattern.compile("^/sellers/seller-(\\d+)/shops/shop-(\\d+)$");

    private KeycloakGroupScopeResolver() {}

    public static Map<Long, SellerScope> resolve(Set<String> groupPaths) {
        Map<Long, MutableScope> values = new LinkedHashMap<>();
        if (groupPaths == null) return Map.of();
        for (String path : groupPaths) {
            if (path == null) continue;
            Matcher seller = SELLER.matcher(path);
            if (seller.matches()) {
                Long sellerId = Long.valueOf(seller.group(1));
                values.computeIfAbsent(sellerId, ignored -> new MutableScope()).allShops = true;
                continue;
            }
            Matcher shop = SHOP.matcher(path);
            if (shop.matches()) {
                Long sellerId = Long.valueOf(shop.group(1));
                Long shopId = Long.valueOf(shop.group(2));
                values.computeIfAbsent(sellerId, ignored -> new MutableScope()).shopIds.add(shopId);
            }
        }
        Map<Long, SellerScope> resolved = new LinkedHashMap<>();
        values.forEach(
                (sellerId, scope) ->
                        resolved.put(
                                sellerId,
                                new SellerScope(
                                        scope.allShops,
                                        scope.allShops ? Set.of() : Set.copyOf(scope.shopIds))));
        return Map.copyOf(resolved);
    }

    public record SellerScope(boolean allShops, Set<Long> shopIds) {
        public SellerScope {
            shopIds = shopIds == null ? Set.of() : Set.copyOf(shopIds);
        }
    }

    private static final class MutableScope {
        private boolean allShops;
        private final Set<Long> shopIds = new LinkedHashSet<>();
    }
}
