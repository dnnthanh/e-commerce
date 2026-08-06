package com.dnnthanh.marketplace.be.authorization.api.adapter.out.external.keycloak.rest;

import com.dnnthanh.marketplace.be.authorization.api.adapter.out.external.keycloak.KeycloakGroupScopeResolver;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedSellerScopeDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedSellerScopeDto.Access;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserProfileDto;
import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationProviderPort;
import com.dnnthanh.marketplace.be.authorization.api.config.KeycloakAdminProperties;
import com.dnnthanh.marketplace.be.authorization.api.exception.infrastructure.AuthorizationProviderException;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Keycloak Admin API adapter; browser tokens are never used for administration. */
@Adapter
@RequiredArgsConstructor
public class KeycloakAuthorizationRestAdapter implements AuthorizationProviderPort {

    private static final String SERVICE_ACCOUNT_ROLE = "SERVICE_ACCOUNT";

    private final RestClient.Builder restClientBuilder;
    private final KeycloakAdminProperties properties;

    @Override
    public UserAccessContextDto context(String userId) {
        String bearer = token();
        UserRepresentation user = user(userId, bearer);
        RoleRepresentation[] roles = effectiveRealmRoles(userId, bearer);
        GroupRepresentation[] groups = userGroups(userId, bearer);

        Set<String> highLevelRoles = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();
        if (roles != null) {
            for (RoleRepresentation role : roles) {
                if (role == null || role.name() == null) continue;
                String name = role.name();
                if (!name.matches("[A-Z][A-Z0-9_]+")) continue;
                if (Boolean.TRUE.equals(role.composite()) || SERVICE_ACCOUNT_ROLE.equals(name)) {
                    highLevelRoles.add(name);
                } else {
                    permissions.add(name);
                }
            }
        }

        Set<String> groupPaths = new LinkedHashSet<>();
        if (groups != null) {
            for (GroupRepresentation group : groups) {
                if (group != null && group.path() != null) groupPaths.add(group.path());
            }
        }

        Map<Long, KeycloakGroupScopeResolver.SellerScope> rawScopes =
                KeycloakGroupScopeResolver.resolve(groupPaths);
        List<ManagedSellerScopeDto> sellerScopes = new ArrayList<>();
        rawScopes.forEach(
                (sellerId, scope) -> {
                    if (scope.allShops()) {
                        sellerScopes.add(
                                new ManagedSellerScopeDto(
                                        sellerId, Access.ALL_SHOPS, allShopIds(sellerId, bearer)));
                    } else {
                        sellerScopes.add(
                                new ManagedSellerScopeDto(
                                        sellerId, Access.SELECTED_SHOPS, scope.shopIds()));
                    }
                });

        return new UserAccessContextDto(
                new UserProfileDto(
                        user.id(),
                        user.username(),
                        user.email(),
                        user.firstName(),
                        user.lastName(),
                        Boolean.TRUE.equals(user.enabled()),
                        Boolean.TRUE.equals(user.emailVerified()),
                        user.attributes()),
                Set.copyOf(highLevelRoles),
                Set.copyOf(permissions),
                sellerScopes,
                Set.copyOf(groupPaths));
    }

    @Override
    public void assignRole(String userId, String role) {
        String bearer = token();
        RoleRepresentation representation = role(role, bearer);
        adminClient()
                .post()
                .uri(
                        "/admin/realms/{realm}/users/{userId}/role-mappings/realm",
                        properties.getRealm(),
                        userId)
                .headers(headers -> headers.setBearerAuth(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(representation))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void removeRole(String userId, String role) {
        String bearer = token();
        RoleRepresentation representation = role(role, bearer);
        adminClient()
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri(
                        "/admin/realms/{realm}/users/{userId}/role-mappings/realm",
                        properties.getRealm(),
                        userId)
                .headers(headers -> headers.setBearerAuth(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(representation))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void assignSeller(String userId, Long sellerId) {
        String bearer = token();
        ensureSellerTree(sellerId, bearer);
        changeGroupMembership(userId, sellerPath(sellerId), true, bearer);
    }

    @Override
    public void removeSeller(String userId, Long sellerId) {
        String bearer = token();
        changeGroupMembership(userId, sellerPath(sellerId), false, bearer);
    }

    @Override
    public void assignShop(String userId, Long sellerId, Long shopId) {
        String bearer = token();
        ensureShopGroup(sellerId, shopId, bearer);
        changeGroupMembership(userId, shopPath(sellerId, shopId), true, bearer);
    }

    @Override
    public void removeShop(String userId, Long sellerId, Long shopId) {
        String bearer = token();
        changeGroupMembership(userId, shopPath(sellerId, shopId), false, bearer);
    }

    private void changeGroupMembership(
            String userId, String groupPath, boolean assign, String bearer) {
        GroupRepresentation group = groupByPath(groupPath, bearer);
        RestClient.RequestHeadersSpec<?> request =
                assign
                        ? adminClient()
                                .put()
                                .uri(
                                        "/admin/realms/{realm}/users/{userId}/groups/{groupId}",
                                        properties.getRealm(),
                                        userId,
                                        group.id())
                                .headers(headers -> headers.setBearerAuth(bearer))
                        : adminClient()
                                .delete()
                                .uri(
                                        "/admin/realms/{realm}/users/{userId}/groups/{groupId}",
                                        properties.getRealm(),
                                        userId,
                                        group.id())
                                .headers(headers -> headers.setBearerAuth(bearer));
        request.retrieve().toBodilessEntity();
    }

    private void ensureSellerTree(Long sellerId, String bearer) {
        GroupRepresentation sellers = ensureSellersRoot(bearer);
        String sellerPath = sellerPath(sellerId);
        GroupRepresentation seller = findGroupByPath(sellerPath, bearer);
        if (seller == null) {
            createChildGroup(
                    sellers.id(),
                    new GroupCreateRequest(
                            "seller-" + sellerId,
                            Map.of(
                                    "type", List.of("SELLER"),
                                    "sellerId", List.of(String.valueOf(sellerId)))),
                    bearer);
            seller = groupByPath(sellerPath, bearer);
        }

        String shopsPath = sellerPath + "/shops";
        if (findGroupByPath(shopsPath, bearer) == null) {
            createChildGroup(
                    seller.id(),
                    new GroupCreateRequest(
                            "shops",
                            Map.of(
                                    "type", List.of("SHOP_CONTAINER"),
                                    "sellerId", List.of(String.valueOf(sellerId)))),
                    bearer);
            groupByPath(shopsPath, bearer);
        }
    }

    private void ensureShopGroup(Long sellerId, Long shopId, String bearer) {
        ensureSellerTree(sellerId, bearer);
        String shopPath = shopPath(sellerId, shopId);
        if (findGroupByPath(shopPath, bearer) != null) return;

        GroupRepresentation shops = groupByPath(sellerPath(sellerId) + "/shops", bearer);
        createChildGroup(
                shops.id(),
                new GroupCreateRequest(
                        "shop-" + shopId,
                        Map.of(
                                "type", List.of("SHOP"),
                                "sellerId", List.of(String.valueOf(sellerId)),
                                "shopId", List.of(String.valueOf(shopId)))),
                bearer);
        groupByPath(shopPath, bearer);
    }

    private GroupRepresentation ensureSellersRoot(String bearer) {
        GroupRepresentation sellers = findGroupByPath("/sellers", bearer);
        if (sellers != null) return sellers;

        adminClient()
                .post()
                .uri("/admin/realms/{realm}/groups", properties.getRealm())
                .headers(headers -> headers.setBearerAuth(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GroupCreateRequest("sellers", Map.of("type", List.of("SELLER_ROOT"))))
                .retrieve()
                .toBodilessEntity();
        return groupByPath("/sellers", bearer);
    }

    private void createChildGroup(String parentId, GroupCreateRequest request, String bearer) {
        adminClient()
                .post()
                .uri(
                        "/admin/realms/{realm}/groups/{parentId}/children",
                        properties.getRealm(),
                        parentId)
                .headers(headers -> headers.setBearerAuth(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private UserRepresentation user(String userId, String bearer) {
        UserRepresentation value =
                adminClient()
                        .get()
                        .uri("/admin/realms/{realm}/users/{userId}", properties.getRealm(), userId)
                        .headers(headers -> headers.setBearerAuth(bearer))
                        .retrieve()
                        .body(UserRepresentation.class);
        if (value == null)
            throw new AuthorizationProviderException("Keycloak user not found: " + userId);
        return value;
    }

    private RoleRepresentation[] effectiveRealmRoles(String userId, String bearer) {
        return adminClient()
                .get()
                .uri(
                        "/admin/realms/{realm}/users/{userId}/role-mappings/realm/composite",
                        properties.getRealm(),
                        userId)
                .headers(headers -> headers.setBearerAuth(bearer))
                .retrieve()
                .body(RoleRepresentation[].class);
    }

    private GroupRepresentation[] userGroups(String userId, String bearer) {
        return adminClient()
                .get()
                .uri(
                        builder ->
                                builder.path("/admin/realms/{realm}/users/{userId}/groups")
                                        .queryParam("briefRepresentation", false)
                                        .build(properties.getRealm(), userId))
                .headers(headers -> headers.setBearerAuth(bearer))
                .retrieve()
                .body(GroupRepresentation[].class);
    }

    private Set<Long> allShopIds(Long sellerId, String bearer) {
        GroupRepresentation shops = groupByPath(sellerPath(sellerId) + "/shops", bearer);
        GroupRepresentation[] children = children(shops.id(), bearer);
        Set<Long> result = new HashSet<>();
        if (children == null) return Set.of();
        for (GroupRepresentation child : children) {
            Long shopId = attributeLong(child.attributes(), "shopId");
            if (shopId == null && child.name() != null && child.name().matches("shop-\\d+")) {
                shopId = Long.valueOf(child.name().substring("shop-".length()));
            }
            if (shopId != null) result.add(shopId);
        }
        return Set.copyOf(result);
    }

    private GroupRepresentation[] children(String groupId, String bearer) {
        return adminClient()
                .get()
                .uri(
                        builder ->
                                builder.path("/admin/realms/{realm}/groups/{groupId}/children")
                                        .queryParam("briefRepresentation", false)
                                        .build(properties.getRealm(), groupId))
                .headers(headers -> headers.setBearerAuth(bearer))
                .retrieve()
                .body(GroupRepresentation[].class);
    }

    private GroupRepresentation groupByPath(String path, String bearer) {
        GroupRepresentation group = findGroupByPath(path, bearer);
        if (group == null)
            throw new AuthorizationProviderException("Keycloak group not found: " + path);
        return group;
    }

    private GroupRepresentation findGroupByPath(String path, String bearer) {
        URI uri =
                UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                        .path("/admin/realms/")
                        .pathSegment(properties.getRealm())
                        .path("/group-by-path/")
                        .pathSegment(path)
                        .build()
                        .encode()
                        .toUri();
        try {
            return adminClient()
                    .get()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(bearer))
                    .retrieve()
                    .body(GroupRepresentation.class);
        } catch (HttpClientErrorException.NotFound ignored) {
            return null;
        }
    }

    private static Long attributeLong(Map<String, List<String>> attributes, String key) {
        if (attributes == null || attributes.get(key) == null || attributes.get(key).isEmpty())
            return null;
        String value = attributes.get(key).getFirst();
        return value == null || value.isBlank() ? null : Long.valueOf(value);
    }

    private static String sellerPath(Long sellerId) {
        return "/sellers/seller-" + sellerId;
    }

    private static String shopPath(Long sellerId, Long shopId) {
        return sellerPath(sellerId) + "/shops/shop-" + shopId;
    }

    private RestClient adminClient() {
        return restClientBuilder.clone().baseUrl(properties.getBaseUrl()).build();
    }

    private RestClient tokenClient() {
        return restClientBuilder
                .clone()
                .baseUrl(
                        properties.getBaseUrl()
                                + "/realms/"
                                + properties.getRealm()
                                + "/protocol/openid-connect/token")
                .build();
    }

    private RoleRepresentation role(String roleName, String bearer) {
        RoleRepresentation value =
                adminClient()
                        .get()
                        .uri("/admin/realms/{realm}/roles/{role}", properties.getRealm(), roleName)
                        .headers(headers -> headers.setBearerAuth(bearer))
                        .retrieve()
                        .body(RoleRepresentation.class);
        if (value == null)
            throw new AuthorizationProviderException("Unknown Keycloak role: " + roleName);
        return value;
    }

    private String token() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        TokenResponse response =
                tokenClient()
                        .post()
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(TokenResponse.class);
        if (response == null || response.access_token() == null) {
            throw new AuthorizationProviderException("Keycloak administration token unavailable");
        }
        return response.access_token();
    }

    private record RoleRepresentation(String id, String name, Boolean composite) {}

    private record GroupRepresentation(
            String id, String name, String path, Map<String, List<String>> attributes) {}

    private record GroupCreateRequest(String name, Map<String, List<String>> attributes) {}

    private record UserRepresentation(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            Boolean emailVerified,
            Map<String, List<String>> attributes) {}

    private record TokenResponse(String access_token) {}
}
