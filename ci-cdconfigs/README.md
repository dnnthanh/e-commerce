# ci-cdconfigs

Deployment/configuration project kept inside the e-commerce repository.

## Ownership rule

- One backend deployable = one folder directly under `ci-cdconfigs/`.
- Do not create a global service endpoint/secrets map. Each deployable owns only the variables it consumes.
- `application.yml` contains property-to-environment-variable binding, not environment-specific URLs/secrets.
- Internal protocol (REST/gRPC/Kafka/Webhook/...) is an adapter/code decision and is **not** a runtime `transport` property.

## Secrets

- `*.env.example` and `secret.example.yaml` contain names/placeholders only.
- Real secrets come from protected CI/CD variables, Vault/cloud secret manager, or Kubernetes External Secrets.
- Real `.env`, `secret.yaml`, credentials, tokens, passwords and private keys are ignored and must never be committed.

## Kubernetes

Each deployable has `kubernetes/base` plus `overlays/dev|staging|prod`. The reusable base **references** a runtime ConfigMap and, only when the service declares secret keys, a mandatory Secret. It does not commit a ConfigMap containing environment-specific values.

Materialize non-secret runtime configuration from the selected environment file or protected CI variables before applying the Kustomize overlay:

```bash
./ci-cdconfigs/apply-service-config.sh be-audit-api /secure/generated/be-audit-api.env marketplace-prod
kubectl apply -k ci-cdconfigs/be-audit-api/kubernetes/overlays/prod
```

The file passed to `apply-service-config.sh` is a deployment-time artifact. Do not commit the rendered production file. Secrets remain outside this helper and must come from protected CI/CD variables, Vault/cloud secret manager, or External Secrets. CI is also expected to set the image/tag before deployment.
