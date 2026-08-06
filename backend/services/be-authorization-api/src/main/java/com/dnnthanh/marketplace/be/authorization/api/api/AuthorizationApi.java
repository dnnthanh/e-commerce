package com.dnnthanh.marketplace.be.authorization.api.api;

import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignRoleRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignSellerScopeRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignShopScopeRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.response.AuthorizationSnapshot;
import com.dnnthanh.marketplace.be.authorization.api.api.response.ManagedShopsView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.PermissionView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserAccessContextView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserProfileView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** HTTP contract for Keycloak-owned identity, roles, permissions and seller/shop scopes. */
public interface AuthorizationApi {

    @GetMapping("/private/me/authorization")
    AuthorizationSnapshot me();

    @GetMapping("/private/me/profile")
    UserProfileView myProfile();

    @GetMapping("/private/me/permissions")
    PermissionView myPermissions();

    @GetMapping("/private/me/managed-shops")
    ManagedShopsView myManagedShops();

    @GetMapping("/private/me/context")
    UserAccessContextView myContext();

    @GetMapping("/internal/authorization/users/{userId}")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    AuthorizationSnapshot internal(@PathVariable String userId);

    @GetMapping("/internal/authorization/users/{userId}/profile")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    UserProfileView internalProfile(@PathVariable String userId);

    @GetMapping("/internal/authorization/users/{userId}/permissions")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    PermissionView internalPermissions(@PathVariable String userId);

    @GetMapping("/internal/authorization/users/{userId}/managed-shops")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    ManagedShopsView internalManagedShops(@PathVariable String userId);

    @GetMapping("/internal/authorization/users/{userId}/context")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    UserAccessContextView internalContext(@PathVariable String userId);

    @GetMapping("/private/admin/security/users/{userId}")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_VIEW')")
    AuthorizationSnapshot admin(@PathVariable String userId);

    @GetMapping("/private/admin/security/users/{userId}/context")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_VIEW')")
    UserAccessContextView adminContext(@PathVariable String userId);

    @PutMapping("/private/admin/security/users/{userId}/roles")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void assignRole(@PathVariable String userId, @RequestBody AssignRoleRequest request);

    @DeleteMapping("/private/admin/security/users/{userId}/roles/{role}")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void removeRole(@PathVariable String userId, @PathVariable String role);

    @PutMapping("/private/admin/security/users/{userId}/seller-scopes")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void assignSellerScope(
            @PathVariable String userId, @RequestBody AssignSellerScopeRequest request);

    @DeleteMapping("/private/admin/security/users/{userId}/seller-scopes/{sellerId}")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void removeSellerScope(@PathVariable String userId, @PathVariable Long sellerId);

    @PutMapping("/private/admin/security/users/{userId}/shop-scopes")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void assignShopScope(@PathVariable String userId, @RequestBody AssignShopScopeRequest request);

    @DeleteMapping("/private/admin/security/users/{userId}/shop-scopes/{sellerId}/{shopId}")
    @PreAuthorize("@authorizationService.hasPermission('SECURITY_MANAGE')")
    void removeShopScope(
            @PathVariable String userId, @PathVariable Long sellerId, @PathVariable Long shopId);
}
