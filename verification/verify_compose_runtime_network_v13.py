from __future__ import annotations

import pathlib
import re
import sys
import yaml

ROOT = pathlib.Path(__file__).resolve().parents[1]
COMPOSE = ROOT / "docker-compose.yml"
SPRING_PUBLIC_SERVICES = set()


def required_env(module: str) -> set[str]:
    app = ROOT / "backend" / "services" / module / "src" / "main" / "resources" / "application.yml"
    if not app.exists():
        return set()
    text = app.read_text(encoding="utf-8")
    found: set[str] = set()
    for m in re.finditer(r"\$\{([A-Z0-9_.-]+)(?::([^}]*))?\}", text):
        if m.group(2) is None:
            found.add(m.group(1))
    return found


def fail(msg: str, errors: list[str]) -> None:
    errors.append(msg)


def main() -> int:
    data = yaml.safe_load(COMPOSE.read_text(encoding="utf-8"))
    services = data["services"]
    errors: list[str] = []

    backend_modules = {p.name for p in (ROOT / "backend" / "services").iterdir() if p.is_dir()}
    compose_backend = {n for n in services if n.startswith("be-")}

    # Every composed backend module must satisfy required env placeholders.
    for name in sorted(compose_backend & backend_modules):
        svc = services[name]
        env = svc.get("environment") or {}
        if not isinstance(env, dict):
            fail(f"{name}: environment must be a mapping", errors)
            continue
        missing = sorted(required_env(name) - set(env))
        if missing:
            fail(f"{name}: missing required env {missing}", errors)

        # Actual container-to-container BE HTTP endpoints always use :8080.
        for key, value in env.items():
            if not isinstance(value, str):
                continue
            if re.match(r"^https?://be-[a-z0-9-]+:\d+", value):
                port = int(re.search(r":(\d+)(?:/|$)", value).group(1))
                if port != 8080:
                    fail(f"{name}: {key} uses internal BE port {port}, expected 8080 ({value})", errors)

        # All local images are built, not pulled from Docker Hub.
        image = svc.get("image")
        if isinstance(image, str) and image.startswith("e-commerce-") and svc.get("build"):
            if svc.get("pull_policy") != "build":
                fail(f"{name}: local build image missing pull_policy: build", errors)

        # Exposed Spring services keep host port but container port is 8080.
        ports = svc.get("ports") or []
        if ports:
            for p in ports:
                raw = str(p)
                if ":" not in raw:
                    continue
                container_port = raw.rsplit(":", 1)[-1]
                if container_port != "8080":
                    fail(f"{name}: published port must target container 8080, got {raw}", errors)
            hc = svc.get("healthcheck") or {}
            test = " ".join(map(str, hc.get("test") or []))
            if test and "localhost:8080" not in test:
                fail(f"{name}: Spring healthcheck must use localhost:8080, got {test}", errors)
            if str(env.get("SERVER_PORT")) != "8080":
                fail(f"{name}: SERVER_PORT must be 8080", errors)

    # Gateway must route to every BE via service DNS/8080.
    gateway = services.get("be-gateway", {})
    genv = gateway.get("environment") or {}
    for key in [
        "CATALOG_BASE_URL","MEDIA_BASE_URL","SEARCH_BASE_URL","SELLER_BASE_URL","PRICING_BASE_URL",
        "PROMOTION_BASE_URL","INVENTORY_BASE_URL","CART_BASE_URL","CHECKOUT_BASE_URL","ORDER_BASE_URL",
        "PAYMENT_BASE_URL","FULFILLMENT_BASE_URL","REVIEW_BASE_URL","COMMENT_BASE_URL","NOTIFICATION_BASE_URL",
        "NOTIFICATION_REALTIME_BASE_URL","RETURN_BASE_URL","SETTLEMENT_BASE_URL","AUDIT_BASE_URL",
        "OPERATIONS_BASE_URL","AUTHORIZATION_BASE_URL",
    ]:
        value = genv.get(key)
        if not (isinstance(value, str) and re.match(r"^http://be-[a-z0-9-]+:8080$", value)):
            fail(f"be-gateway: {key} must use compose service DNS on :8080, got {value!r}", errors)

    # Exceptions are deliberate: issuer/public browser URLs may contain localhost.
    allowed_localhost_keys = {
        "KEYCLOAK_ISSUER", "KEYCLOAK_ISSUER_URI", "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI",
        "MINIO_PUBLIC_BASE_URL", "MEDIA_PUBLIC_BASE_URL", "PAYMENT_RESULT_URL",
    }
    for name, svc in services.items():
        if not name.startswith("be-"):
            continue
        env = svc.get("environment") or {}
        if not isinstance(env, dict):
            continue
        for key, value in env.items():
            if isinstance(value, str) and "localhost" in value and key not in allowed_localhost_keys:
                fail(f"{name}: unexpected localhost in {key}={value}", errors)

    # Split backend fragments must carry the same runtime contract as the root entrypoint.
    split_files = [ROOT / "compose" / "backend" / "all.yml", *sorted((ROOT / "compose" / "backend").glob("be-*.yml"))]
    for split in split_files:
        split_data = yaml.safe_load(split.read_text(encoding="utf-8")) or {}
        for name, svc in (split_data.get("services") or {}).items():
            if name not in backend_modules:
                continue
            env = svc.get("environment") or {}
            missing = sorted(required_env(name) - set(env))
            if missing:
                fail(f"{split.relative_to(ROOT)}:{name}: missing required env {missing}", errors)
            for key, value in env.items():
                if isinstance(value, str) and re.match(r"^https?://be-[a-z0-9-]+:(?!8080(?:/|$))\d+", value):
                    fail(f"{split.relative_to(ROOT)}:{name}: stale internal URL {key}={value}", errors)
            image = svc.get("image")
            if isinstance(image, str) and image.startswith("e-commerce-") and svc.get("build") and svc.get("pull_policy") != "build":
                fail(f"{split.relative_to(ROOT)}:{name}: local build image missing pull_policy: build", errors)

    # Frontend and seed images that are built locally must not trigger Docker Hub pulls either.
    for compose_path in [ROOT / "docker-compose.yml", ROOT / "compose" / "frontend.yml", ROOT / "compose" / "seeds.yml"]:
        d = yaml.safe_load(compose_path.read_text(encoding="utf-8")) or {}
        for name, svc in (d.get("services") or {}).items():
            image = svc.get("image") if isinstance(svc, dict) else None
            if isinstance(image, str) and image.startswith("e-commerce-") and svc.get("build") and svc.get("pull_policy") != "build":
                fail(f"{compose_path.relative_to(ROOT)}:{name}: local build image missing pull_policy: build", errors)

    if errors:
        print("COMPOSE_RUNTIME_NETWORK_V13=FAIL")
        for e in errors:
            print(" -", e)
        return 1

    print("COMPOSE_RUNTIME_NETWORK_V13=PASS")
    print(f"backend_services={len(compose_backend)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
