# be-media-outbox

CI/CD ownership for `be-media-outbox` only.

- Non-secret environment mappings: `env/*.env.example`
- Secret keys only: `env/secrets.env.example`
- Kubernetes base/overlays: `kubernetes/`
- No real secret belongs in this directory or in `application.yml`.
