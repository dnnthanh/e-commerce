package com.dnnthanh.marketplace.be.authorization.api.adapter.in.web;

import com.dnnthanh.marketplace.be.authorization.api.adapter.in.web.mapper.AuthorizationApiMapper;
import com.dnnthanh.marketplace.be.authorization.api.api.AuthorizationApi;
import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignRoleRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignSellerScopeRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.request.AssignShopScopeRequest;
import com.dnnthanh.marketplace.be.authorization.api.api.response.AuthorizationSnapshot;
import com.dnnthanh.marketplace.be.authorization.api.api.response.ManagedShopsView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.PermissionView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserAccessContextView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserProfileView;
import com.dnnthanh.marketplace.be.authorization.api.application.port.in.AuthorizationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorizationController implements AuthorizationApi {
    private final AuthorizationUseCase useCase;
    private final AuthorizationApiMapper mapper;

    @Override
    public AuthorizationSnapshot me() {
        return mapper.modelToResponse(useCase.current());
    }

    @Override
    public UserProfileView myProfile() {
        return mapper.profileToResponse(useCase.currentProfile());
    }

    @Override
    public PermissionView myPermissions() {
        return mapper.permissionsToResponse(useCase.currentPermissions());
    }

    @Override
    public ManagedShopsView myManagedShops() {
        return mapper.managedShopsToResponse(useCase.currentManagedShops());
    }

    @Override
    public UserAccessContextView myContext() {
        return mapper.contextToResponse(useCase.currentContext());
    }

    @Override
    public AuthorizationSnapshot internal(String userId) {
        return mapper.modelToResponse(useCase.snapshot(userId));
    }

    @Override
    public UserProfileView internalProfile(String userId) {
        return mapper.profileToResponse(useCase.profile(userId));
    }

    @Override
    public PermissionView internalPermissions(String userId) {
        return mapper.permissionsToResponse(useCase.permissions(userId));
    }

    @Override
    public ManagedShopsView internalManagedShops(String userId) {
        return mapper.managedShopsToResponse(useCase.managedShops(userId));
    }

    @Override
    public UserAccessContextView internalContext(String userId) {
        return mapper.contextToResponse(useCase.context(userId));
    }

    @Override
    public AuthorizationSnapshot admin(String userId) {
        return mapper.modelToResponse(useCase.snapshot(userId));
    }

    @Override
    public UserAccessContextView adminContext(String userId) {
        return mapper.contextToResponse(useCase.context(userId));
    }

    @Override
    public void assignRole(String userId, AssignRoleRequest request) {
        useCase.assignRole(userId, request.role());
    }

    @Override
    public void removeRole(String userId, String role) {
        useCase.removeRole(userId, role);
    }

    @Override
    public void assignSellerScope(String userId, AssignSellerScopeRequest request) {
        useCase.assignSeller(userId, request.sellerId());
    }

    @Override
    public void removeSellerScope(String userId, Long sellerId) {
        useCase.removeSeller(userId, sellerId);
    }

    @Override
    public void assignShopScope(String userId, AssignShopScopeRequest request) {
        useCase.assignShop(userId, request.sellerId(), request.shopId());
    }

    @Override
    public void removeShopScope(String userId, Long sellerId, Long shopId) {
        useCase.removeShop(userId, sellerId, shopId);
    }
}
