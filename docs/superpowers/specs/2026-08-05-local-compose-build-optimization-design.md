# Local Compose Build Optimization Design

## Goal

Make local Docker development reliable on a 24 GB Apple Silicon workstation by removing the 51-independent-Maven-build fan-out and by not starting the entire 99-service topology by default.

## Build architecture

Local Compose builds one `e-commerce-backend-runtime:local` image from `backend/Dockerfile.runtime`. The Dockerfile runs the Maven reactor once with tests skipped, collects one executable JAR for every `backend/services/be-*` module, and copies those JARs into the runtime image under `/opt/marketplace/services/<module>/app.jar`. Each backend container uses the same image and selects its application with `SERVICE_MODULE`.

A lightweight `backend-runtime-build` Compose service owns the build definition. Backend application services have no Maven build definition and use `pull_policy: never`, preventing accidental Docker Hub pulls. They depend on the build service completing successfully before startup.

## Runtime profiles

The no-profile default is the local developer stack: core infrastructure plus the synchronous marketplace APIs needed for catalog, search, cart, checkout, order/payment, fulfillment, seller/review, return, audit/operations, gateway, and the two frontends. Asynchronous jobs/outbox/workers and nonessential bounded contexts are profile-gated.

- `full`: enables every optional bounded context and data store; observability tooling stays separate.
- `heavy`: enables Oracle/settlement-specific components that are expensive and not required for the normal dev loop.
- `observability`: enables LGTM, Alloy, Kafka UI, dashboards, exporters, cAdvisor, node exporter, blackbox exporter, and DB UI tooling.
- `seed-large`: keeps the existing large seed datasets opt-in.

Backend local JVMs receive bounded heap defaults so a large number of Spring processes cannot consume the Docker VM without a ceiling.

## Frontend build correctness

Angular 22 uses TypeScript 6.0.x. Both workspace applications explicitly define `production` and `development` build configurations, with `production` as the default. Dockerfiles continue to execute production builds.

## Compatibility

`backend/Dockerfile.service` remains available for CI/production-style per-service images. The root Compose local path uses the shared runtime image. Split Compose backend fragments are updated to the same shared runtime contract so they do not reintroduce per-service Maven builds.

## Verification

Static verification must prove: exactly one local backend Maven build owner, 51 backend services use the shared runtime image with matching `SERVICE_MODULE`, optional heavy/observability services have profiles, frontend Angular/TypeScript compatibility is coherent, and previous runtime-network/JMX/database/front-end gates remain green.
