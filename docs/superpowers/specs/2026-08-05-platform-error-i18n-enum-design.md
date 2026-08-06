# Platform Error, i18n, and Enum Contract Design

## Scope

Refactor `be-platform-starter` as the shared technical core without adding a new module. Group platform classes by responsibility, replace hard-coded API error strings/statuses with polymorphic error-code enums, centralize locale-aware message resolution, and standardize enum JSON codes without putting business rules into MapStruct.

## Core package shape

- `platform.api`: generic response/error/pagination contracts and response advice.
- `platform.context`: request/system execution identity.
- `platform.exception`: shared exception/error-code contracts and platform error enum.
- `platform.i18n`: message-resolvable contract and locale-aware resolver.
- `platform.jackson`: JSON boundary configuration and trimming.
- `platform.security`: authorization/security/service-token infrastructure.
- `platform.trace`: trace header and outbound propagation.
- `platform.web.error`: servlet exception translation.
- `platform.web.logging`: structured HTTP logging.
- `platform.event`: shared event-envelope/outbox conversion contracts.

## Exception and i18n model

`ErrorCode` is a polymorphic contract exposing stable machine code, i18n message key, and HTTP status. `PlatformErrorCode` owns framework/common errors. Each bounded context that exposes `BusinessException` owns a small service-specific error enum implementing the same contract. `BusinessException` stores only an `ErrorCode`, optional message arguments/details and cause; it does not hard-code code/message/status strings.

`MessageResolvable` exposes a message key and fallback. `MessageResolver` supports default-locale resolution, explicit locale resolution, and message arguments. The Spring implementation delegates to `MessageSource`; explicit locale wins, otherwise `LocaleContextHolder`/JVM default is used deterministically.

## Mapping and enum policy

Closed business/API sets expose a stable `code` through `CodeEnum`; `@JsonValue` is applied at that contract so Jackson serializes the enum code directly. Existing API values remain unchanged during this refactor to avoid breaking clients.

MapStruct priority: automatic field/enum mapping first; mapper default methods only for technical composition that MapStruct cannot express clearly; `java(expression)` only for tiny stable cases. Business rules stay in domain/application models. For Comment, response fields use enum types directly instead of String conversion; deleted-content masking is resolved before/within the model boundary, not by `.name()`/enum conversion in the mapper.

## Compatibility

Database enum persistence is not changed. Existing API enum wire values remain uppercase codes matching current `Enum.name()` output. Error response envelope shape remains stable while implementation becomes typed/i18n-driven.
