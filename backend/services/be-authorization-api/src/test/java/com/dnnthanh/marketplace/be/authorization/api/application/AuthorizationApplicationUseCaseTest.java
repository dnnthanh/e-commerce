package com.dnnthanh.marketplace.be.authorization.api.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedSellerScopeDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedSellerScopeDto.Access;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserProfileDto;
import com.dnnthanh.marketplace.be.authorization.api.application.port.in.AuthorizationUseCase;
import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationProviderPort;
import com.dnnthanh.marketplace.be.authorization.api.application.service.AuthorizationServiceImplement;
import com.dnnthanh.marketplace.be.authorization.api.domain.model.AuthorizationChangeType;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/** Unit tests for Keycloak-backed authorization orchestration. */
class AuthorizationApplicationUseCaseTest {

    @Test
    void currentViewsAreProjectedFromOneKeycloakOwnedContext() {
        AuthorizationProviderPort provider = mock(AuthorizationProviderPort.class);
        AuthorizationChangeRecorder recorder = mock(AuthorizationChangeRecorder.class);
        UserAccessContextDto context =
                new UserAccessContextDto(
                        new UserProfileDto(
                                "user-1",
                                "seller.demo",
                                "seller@example.test",
                                "Seller",
                                "Demo",
                                true,
                                true,
                                Map.of()),
                        Set.of("SELLER_MANAGER"),
                        Set.of("PRODUCT_VIEW", "ORDER_VIEW"),
                        List.of(
                                new ManagedSellerScopeDto(
                                        10001L, Access.ALL_SHOPS, Set.of(11001L))),
                        Set.of("/sellers/seller-10001"));
        when(provider.context("user-1")).thenReturn(context);
        AuthorizationUseCase useCase =
                new AuthorizationServiceImplement(
                        provider,
                        recorder,
                        new UserContext(
                                "user-1", "seller.demo", UserContext.ActorType.USER, Set.of()));

        assertEquals("seller.demo", useCase.currentProfile().username());
        assertEquals(
                Set.of("PRODUCT_VIEW", "ORDER_VIEW"), useCase.currentPermissions().permissions());
        assertEquals(Set.of(11001L), useCase.currentManagedShops().sellers().getFirst().shopIds());
        assertEquals(Set.of(10001L), useCase.current().sellerIds());
    }

    @Test
    void persistsIntentBeforeProviderMutationAndMarksItAppliedAfterSuccess() {
        AuthorizationProviderPort provider = mock(AuthorizationProviderPort.class);
        AuthorizationChangeRecorder recorder = mock(AuthorizationChangeRecorder.class);
        when(recorder.prepareChange(
                        "user-1",
                        AuthorizationChangeType.ROLE_ASSIGNED,
                        "admin-1",
                        Map.of("role", "SELLER_MANAGER")))
                .thenReturn("change-1");
        AuthorizationUseCase useCase =
                new AuthorizationServiceImplement(
                        provider,
                        recorder,
                        new UserContext(
                                "admin-1",
                                "admin",
                                UserContext.ActorType.USER,
                                Set.of("PLATFORM_ADMIN")));

        useCase.assignRole("user-1", "SELLER_MANAGER");

        InOrder order = inOrder(recorder, provider);
        order.verify(recorder)
                .prepareChange(
                        "user-1",
                        AuthorizationChangeType.ROLE_ASSIGNED,
                        "admin-1",
                        Map.of("role", "SELLER_MANAGER"));
        order.verify(provider).assignRole("user-1", "SELLER_MANAGER");
        order.verify(recorder).markApplied("change-1");
    }

    @Test
    void shopScopeMutationIsDelegatedOnlyToKeycloakProvider() {
        AuthorizationProviderPort provider = mock(AuthorizationProviderPort.class);
        AuthorizationChangeRecorder recorder = mock(AuthorizationChangeRecorder.class);
        when(recorder.prepareChange(
                        "user-1",
                        AuthorizationChangeType.SHOP_SCOPE_ASSIGNED,
                        "admin-1",
                        Map.of("sellerId", 10001L, "shopId", 11001L)))
                .thenReturn("change-2");
        AuthorizationUseCase useCase =
                new AuthorizationServiceImplement(
                        provider,
                        recorder,
                        new UserContext(
                                "admin-1",
                                "admin",
                                UserContext.ActorType.USER,
                                Set.of("PLATFORM_ADMIN")));

        useCase.assignShop("user-1", 10001L, 11001L);

        verify(provider).assignShop("user-1", 10001L, 11001L);
    }
}
