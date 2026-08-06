from __future__ import annotations

import json
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = ROOT / "docker-compose.yml"

REQUIRED_SERVICES = {
    "lgtm",
    "alloy",
    "alertmanager",
    "kafka-ui",
    "kafka-exporter",
    "kafka-jmx-exporter",
    "postgres-exporter",
    "mysql-exporter",
    "mongodb-exporter",
    "redis-exporter",
    "mssql-exporter",
    "oracle-exporter",
    "opensearch-exporter",
    "cadvisor",
    "node-exporter",
    "blackbox-exporter",
}

FORBIDDEN_SPLIT_LGTM_SERVICES = {"prometheus", "grafana", "loki", "tempo", "otel-collector"}

REQUIRED_FILES = [
    "infrastructure/lgtm/prometheus.yaml",
    "infrastructure/prometheus/rules/alerts.yml",
    "infrastructure/prometheus/targets/backend-services.json",
    "infrastructure/alertmanager/alertmanager.yml",
    "infrastructure/grafana/provisioning/dashboards/dashboards.yml",
    "infrastructure/grafana/dashboards/marketplace-overview.json",
    "infrastructure/grafana/dashboards/kafka-monitoring.json",
    "infrastructure/grafana/dashboards/database-monitoring.json",
    "infrastructure/grafana/dashboards/infrastructure-monitoring.json",
    "infrastructure/alloy/config.alloy",
    "infrastructure/kafka/jmx-exporter/exporter.yml",
    "infrastructure/kafka/jmx-exporter/Dockerfile",
    "infrastructure/mysql/exporter/.my.cnf",
    "infrastructure/oracle-exporter/Dockerfile",
    "infrastructure/oracle-exporter/config.yaml",
    "infrastructure/blackbox/blackbox.yml",
]


def load_yaml(path: Path):
    with path.open(encoding="utf-8") as handle:
        return yaml.safe_load(handle)


def main() -> None:
    compose = load_yaml(COMPOSE)
    services = compose["services"]

    missing_services = sorted(REQUIRED_SERVICES - set(services))
    assert not missing_services, f"Missing compose monitoring services: {missing_services}"

    split_services = sorted(FORBIDDEN_SPLIT_LGTM_SERVICES & set(services))
    assert not split_services, f"Local stack must use grafana/otel-lgtm, not split services: {split_services}"

    assert services["kafka-ui"]["image"].startswith("ghcr.io/kafbat/kafka-ui"), "Kafka UI must remain Kafbat Kafka UI"
    assert services["lgtm"]["image"] == "grafana/otel-lgtm:0.29.2", "LGTM image must be pinned"
    assert "GF_USERS_DEFAULT_THEME" not in services["lgtm"].get("environment", {}), (
        "Grafana theme must remain a user/system preference"
    )

    missing_files = [path for path in REQUIRED_FILES if not (ROOT / path).exists()]
    assert not missing_files, f"Missing observability files: {missing_files}"

    for path in ROOT.glob("infrastructure/**/*.yml"):
        load_yaml(path)
    for path in ROOT.glob("infrastructure/**/*.yaml"):
        load_yaml(path)

    for path in ROOT.glob("infrastructure/grafana/dashboards/*.json"):
        with path.open(encoding="utf-8") as handle:
            json.load(handle)

    targets = json.loads(
        (ROOT / "infrastructure/prometheus/targets/backend-services.json").read_text(encoding="utf-8")
    )
    backend_targets = targets[0]["targets"]
    backend_services = sorted(name for name in services if name.startswith("be-"))
    expected_targets = sorted(f"{name}:8080" for name in backend_services)
    assert sorted(backend_targets) == expected_targets, "Backend Prometheus targets are stale"

    lgtm_text = (ROOT / "infrastructure/lgtm/prometheus.yaml").read_text(encoding="utf-8")
    for job in (
        "spring-backends",
        "kafka-exporter",
        "kafka-jmx",
        "postgres",
        "mysql",
        "mongodb",
        "redis",
        "sqlserver",
        "oracle",
        "opensearch",
        "keycloak",
        "minio",
        "cadvisor",
        "node-exporter",
        "blackbox-http",
    ):
        assert f"job_name: {job}" in lgtm_text, f"Missing Prometheus job: {job}"

    assert services["kafka"]["environment"].get("KAFKA_JMX_PORT") == "9999", "Kafka remote JMX is not enabled"
    assert "http://lgtm:3100" in (ROOT / "infrastructure/alloy/config.alloy").read_text(encoding="utf-8"), (
        "Alloy must forward Docker logs to Loki inside LGTM"
    )

    for service_name, service in services.items():
        if not service_name.startswith("be-"):
            continue
        env = service.get("environment", {})
        otlp_values = " ".join(str(value) for key, value in env.items() if "OTLP" in key or "TRACING" in key)
        assert "otel-collector" not in otlp_values, f"{service_name} still targets removed otel-collector"

    print("OBSERVABILITY_ASSETS_PASS")
    print(f"compose_services={len(services)}")
    print(f"backend_prometheus_targets={len(backend_targets)}")
    print("grafana_theme=USER_OR_SYSTEM_PREFERENCE")
    print("kafka_operations=KAFKA_UI")
    print("kafka_monitoring=KAFKA_EXPORTER+JMX_EXPORTER")
    print("observability=GRAFANA_OTEL_LGTM+ALLOY+ALERTMANAGER+EXPORTERS")


if __name__ == "__main__":
    main()
