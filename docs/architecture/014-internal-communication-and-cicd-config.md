# Internal communication and CI/CD configuration

## Naming

```text
CartController
  -> CartUseCase                         (input-port interface)
  -> CartServiceImplement                (application implementation)
  -> CartPersistencePort                 (output-port interface)
  -> CartPersistenceAdapter              (JPA/Mongo/JDBC adapter)
```

Internal synchronous dependency example:

```text
CheckoutServiceImplement
  -> PricingClientPort
  -> adapter/out/internal/pricing/rest/PricingRestAdapter
  -> be-pricing-api
```

The application layer never knows whether the current adapter uses REST or gRPC.

## Package rules

- `adapter/out/internal/<target>/rest`: outbound REST call to another marketplace service.
- `adapter/out/internal/<target>/grpc`: outbound gRPC call to another marketplace service.
- `adapter/in/internal/rest|grpc`: private service-to-service entry point.
- `adapter/in/webhook`: callback pushed into us.
- `adapter/in/messaging/kafka`: Kafka event/command consumer.
- `adapter/out/external/<provider>/<protocol>`: third-party integration.

## Protocol choice

REST and gRPC are request/response mechanisms; Kafka events and work queues are asynchronous but have different fan-out semantics; webhook is an inbound callback model; GraphQL is primarily query composition; SOAP is retained only for contracts that require it. There is intentionally no `transport:` config switch.

## Configuration flow

```text
application.yml
  property: ${ENV_VARIABLE}
          |
          v
ci-cdconfigs/<be-name>/env/<environment>.env.example
          |
          v
CI/CD protected variables / Secret Manager
          |
          v
Kubernetes ConfigMap + Secret / External Secret
```

Environment-specific endpoints and credentials never appear as Java literals.

## Secret rules

Passwords, access keys, client secrets, tokens and private keys have no committed default. `secret.example.yaml` is documentation only and is deliberately excluded from Kustomize resources. A deployment pipeline or External Secrets controller materializes the real `<be-name>-secret` object.

## Local Docker Compose addressing

Compose service names are the local service-discovery abstraction. A Java service must not depend on a Compose-generated container name such as `e-commerce-marketplace-be-payment-api-1`; that name is an implementation detail and changes with project name/scaling.

```text
Developer host                         Docker network
---------------                        --------------
localhost:8080   -> be-gateway:8080     be-gateway -> be-catalog-api:8080
localhost:8090   -> be-checkout-api     checkout   -> be-inventory-api:8080
localhost:8092   -> be-payment-api      payment    -> be-payment-simulator:8080
```

The container port for Spring HTTP services is standardized at `8080`. Published host ports are developer-facing adapters only and are never copied into `*_BASE_URL` configuration.

### Public identity URL vs backchannel URL

Keycloak is the important exception to the simplistic rule "there must never be localhost in a container environment". The issuer is part of the JWT security contract, not merely a socket destination. Local browser login issues tokens with:

```text
iss = http://localhost:8180/realms/marketplace
```

Resource servers therefore keep that issuer value but use an explicit internal JWK endpoint:

```text
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=
  http://keycloak:8080/realms/marketplace/protocol/openid-connect/certs
```

Token acquisition/admin calls also use `keycloak:8080`. The same distinction applies to MinIO: SDK traffic uses `minio:9000`, while a URL intentionally returned to the browser can use `localhost:9000` in the local profile.

### Local runtime configuration completeness

For local Compose, every `${ENV}` placeholder without a default in each service `application.yml` must be supplied by Compose. The verification gate compares the application configuration against `docker-compose.yml`; this prevents a service from building successfully and then exiting immediately because `DB_URL`, `MONGODB_URI`, a downstream base URL, or a Keycloak admin setting is missing.


## Local backend build architecture

The local Compose topology intentionally differs from CI image packaging. Building 51 independent Maven images creates dozens of compiler JVMs and duplicate dependency downloads, which can exhaust Docker Desktop even on a high-end development workstation.

Local build flow:

```text
backend-runtime-build
  -> backend/Dockerfile.runtime
  -> mvn package (one reactor)
  -> /opt/marketplace/services/<module>/app.jar

be-catalog-api      -- SERVICE_MODULE=be-catalog-api ----\
be-order-api        -- SERVICE_MODULE=be-order-api -------+--> e-commerce-backend-runtime:local
be-payment-worker   -- SERVICE_MODULE=be-payment-worker --/
```

The shared image is a local-development optimization, not a service-deployment coupling. CI/production can still use `backend/Dockerfile.service` to produce a single-service artifact/image. The application boundaries, ports, service DNS and database ownership do not change.

### Compose profiles

No-profile services form the normal developer loop. `full` enables all optional bounded contexts, `heavy` enables the Oracle/settlement path, `observability` enables monitoring/tooling, and `seed-large` enables large datasets. A default service must never depend on a profile-disabled service; the static gate validates that profile dependency graph.
