# Feature Spec 001 — Keycloak Authentication, Permission Authorization and User Context

## Status

Approved for implementation on 2026-08-05.

## Goals

- Keycloak/OIDC is the only source of authentication and authorization truth.
- Roles aggregate permissions through Keycloak composite roles.
- JWT stays intentionally small and is used as identity, not as the effective permission store.
- Effective roles, permissions, seller scope and shop scope are resolved through `be-authorization-api` from Keycloak Admin APIs.
- Seller/shop relational databases own business data only; they do not own staff permissions.
- Resource scope is represented by a Keycloak group tree.
- Current-user profile, permissions, managed shops and full context have dedicated APIs.
- Internal service accounts can resolve the same context for a target user.
- Authorization mutations remain durable/auditable through the authorization change log/outbox, but that database is not consulted to decide access.

## Keycloak authority model

### Role composition

Business roles are realm composite roles. Permissions are realm roles composed into business roles, for example:

```text
SELLER_MANAGER
  -> PRODUCT_VIEW
  -> PRODUCT_UPDATE
  -> INVENTORY_VIEW
  -> INVENTORY_UPDATE
  -> ORDER_VIEW
  -> ...
```

Backend authorization decisions are permission-based. Controllers/use cases must not authorize with hard-coded business role names.

### JWT contract

The browser/user token identifies the actor (`sub`, username and identity claims required by Spring Security). Backend services must not rely on expanded composite permission roles in the JWT for business authorization. Effective authorization is read from `be-authorization-api` and cached with bounded TTL/invalidation.

### Seller/shop tree

```text
/sellers
  /seller-10001                         attributes: type=SELLER, sellerId=10001
    /shops                              attributes: type=SHOP_CONTAINER
      /shop-10001                       attributes: type=SHOP, sellerId=10001, shopId=10001
      /shop-10002                       attributes: type=SHOP, sellerId=10001, shopId=10002
  /seller-10002
    /shops
      /shop-10003
```

Membership semantics:

- Membership on `/sellers/seller-{sellerId}` grants seller-wide scope (`ALL_SHOPS`) for that seller.
- Membership only on one or more `/sellers/seller-{sellerId}/shops/shop-{shopId}` groups grants `SELECTED_SHOPS` scope for those shops.
- A child shop scope never grants another seller's scope.
- `PLATFORM_ADMIN` may have no seller/shop memberships; platform-wide permission checks remain permission-based and do not fabricate seller scope.

Keycloak tree mutation must not update a parent group's `subGroups` payload and assume Keycloak will persist nested changes. Resolve groups by canonical path/id and create children with the Keycloak child-group endpoint. This avoids partial-subgroup representations and middle-tree insertion bugs.

## API contract

Authenticated current-user endpoints:

```text
GET /private/me/authorization
GET /private/me/profile
GET /private/me/permissions
GET /private/me/managed-shops
GET /private/me/context
```

Internal service-account endpoints:

```text
GET /internal/authorization/users/{userId}
GET /internal/authorization/users/{userId}/profile
GET /internal/authorization/users/{userId}/permissions
GET /internal/authorization/users/{userId}/managed-shops
GET /internal/authorization/users/{userId}/context
```

Admin security screen endpoint `/private/admin/security/users/{userId}` continues to return authorization state and requires `SECURITY_VIEW`.

Scope mutation endpoints support both seller-wide and shop-specific assignment:

```text
PUT    /private/admin/security/users/{userId}/seller-scopes
DELETE /private/admin/security/users/{userId}/seller-scopes/{sellerId}
PUT    /private/admin/security/users/{userId}/shop-scopes
DELETE /private/admin/security/users/{userId}/shop-scopes/{sellerId}/{shopId}
```

## Response model

- `UserProfile`: Keycloak subject, username, email, first/last name, enabled/emailVerified and safe user attributes.
- `PermissionView`: high-level roles and effective permissions.
- `ManagedShopsView`: seller scopes with `ALL_SHOPS` or `SELECTED_SHOPS` and selected shop ids.
- `UserAccessContext`: profile + roles + permissions + managed sellers/shops + canonical group paths.
- Existing `AuthorizationSnapshot` remains a compatibility projection with `roles`, `permissions`, and `sellerIds`.

## Seller service boundary

`be-seller-api` keeps seller lifecycle and shop business data. Remove `permissions_csv`, staff permission evaluation and permission persistence from the seller aggregate/persistence path. A Liquibase migration drops the obsolete `seller_staff` authorization table. Historical migration files remain immutable.

## Docker/runtime

- The new APIs are methods of `be-authorization-api`; they are not separate containers.
- `be-authorization-api` must remain in the default dev Compose topology.
- OpenSearch dev mode disables demo security installation explicitly so the container does not fail before dependent services start.
- Heavy/observability profiles remain optional.

## GitHub Actions

PR/push checks must include:

- Java 25 backend Maven test/package gate.
- Angular admin/storefront production builds.
- project verification scripts.
- `docker compose config` for default/full profiles.
- Docker build of shared backend runtime, storefront/admin and seed images.
- lightweight infrastructure smoke for core dependencies; heavy Oracle/SQL Server runtime is config-validated rather than fully started on hosted runners.
- on failure, upload useful Compose/service logs as an artifact where practical.

## Acceptance tests

- composite role yields expected effective permissions from Keycloak provider response;
- seller-parent membership resolves `ALL_SHOPS`;
- shop-leaf memberships resolve only selected shop ids;
- profile is resolved from Keycloak user representation;
- full context combines identity, permissions and scopes without DB authorization reads;
- seller domain has no permission evaluation or permission persistence;
- OpenSearch Compose uses explicit no-security Docker bootstrap settings;
- authorization service is present in default Compose;
- CI validates backend, frontend and Docker configuration.
