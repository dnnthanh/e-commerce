package com.dnnthanh.marketplace.be.authorization.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.authorization.api.api.response.AuthorizationSnapshot;
import com.dnnthanh.marketplace.be.authorization.api.api.response.ManagedSellerScopeView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.ManagedShopsView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.PermissionView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserAccessContextView;
import com.dnnthanh.marketplace.be.authorization.api.api.response.UserProfileView;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.AuthorizationSnapshotDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedSellerScopeDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedShopsDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.PermissionViewDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserProfileDto;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps authorization application results to HTTP views. */
@Mapper(config = PlatformMapperConfig.class)
public interface AuthorizationApiMapper
        extends ModelResponseMapper<AuthorizationSnapshotDto, AuthorizationSnapshot> {
    @Override
    AuthorizationSnapshot modelToResponse(AuthorizationSnapshotDto result);

    UserProfileView profileToResponse(UserProfileDto result);

    PermissionView permissionsToResponse(PermissionViewDto result);

    ManagedSellerScopeView sellerScopeToResponse(ManagedSellerScopeDto result);

    List<ManagedSellerScopeView> sellerScopesToResponse(List<ManagedSellerScopeDto> result);

    default ManagedShopsView managedShopsToResponse(ManagedShopsDto result) {
        return new ManagedShopsView(sellerScopesToResponse(result.sellers()));
    }

    @Mapping(target = "managedShops", source = "sellerScopes")
    UserAccessContextView contextToResponse(UserAccessContextDto result);
}
