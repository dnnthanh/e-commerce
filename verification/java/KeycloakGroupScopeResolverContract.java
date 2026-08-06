import com.dnnthanh.marketplace.be.authorization.api.adapter.out.external.keycloak.KeycloakGroupScopeResolver;
import java.util.Set;

public final class KeycloakGroupScopeResolverContract {
    public static void main(String[] args) {
        var all = KeycloakGroupScopeResolver.resolve(Set.of("/sellers/seller-10001"));
        require(all.size() == 1, "expected one seller scope");
        var allScope = all.get(10001L);
        require(allScope != null && allScope.allShops(), "seller parent must grant ALL_SHOPS");
        require(allScope.shopIds().isEmpty(), "ALL_SHOPS membership should not fabricate selected shops");

        var selected = KeycloakGroupScopeResolver.resolve(Set.of(
                "/sellers/seller-10001/shops/shop-11001",
                "/sellers/seller-10001/shops/shop-11009",
                "/unrelated/group"));
        var selectedScope = selected.get(10001L);
        require(selectedScope != null && !selectedScope.allShops(), "leaf membership must be SELECTED_SHOPS");
        require(selectedScope.shopIds().equals(Set.of(11001L, 11009L)), "selected shops mismatch");

        var mixed = KeycloakGroupScopeResolver.resolve(Set.of(
                "/sellers/seller-10001/shops/shop-11001",
                "/sellers/seller-10001"));
        var mixedScope = mixed.get(10001L);
        require(mixedScope != null && mixedScope.allShops(), "seller parent must override selected leaves");
        require(mixedScope.shopIds().isEmpty(), "ALL_SHOPS must normalize selected leaves away");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
