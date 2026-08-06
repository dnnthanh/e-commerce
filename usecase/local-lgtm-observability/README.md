# Local LGTM bundle vs production observability

## Problem
A local marketplace stack already contains many databases, Kafka, Keycloak, MinIO and dozens of deployables. Running separate Grafana, Prometheus, Loki, Tempo and OpenTelemetry Collector containers adds local orchestration noise.

## Chosen local design
Use `grafana/otel-lgtm` as one local-development container bundling Grafana, Prometheus, Loki, Tempo, OpenTelemetry Collector and Pyroscope. Mount marketplace Prometheus scrape config and Grafana dashboard provisioning into the paths supported by the image. Keep exporters as separate lightweight containers because they run next to the systems being observed.

## Production boundary
The bundle is for local development/demo/testing. Production Kubernetes should use independently scalable/managed observability components so retention, HA, storage, tenancy and upgrades can be operated independently.
