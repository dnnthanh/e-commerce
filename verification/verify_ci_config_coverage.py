from pathlib import Path
import re
import sys
import yaml

ROOT = Path(__file__).resolve().parents[1]
SERVICES = ROOT / "backend" / "services"
CI = ROOT / "ci-cdconfigs"
SECRET_PATTERN = re.compile(
    r"(PASSWORD|SECRET|TOKEN(?!_URI)|ACCESS_KEY|PRIVATE_KEY|CREDENTIAL)", re.IGNORECASE
)
ENV_PATTERN = re.compile(r"\$\{([A-Z0-9_]+)(?::[^}]*)?\}")


def env_keys(path: Path) -> set[str]:
    return {
        line.split("=", 1)[0]
        for line in path.read_text(encoding="utf-8").splitlines()
        if line and not line.lstrip().startswith("#") and "=" in line
    }


def yaml_keys(path: Path, key: str) -> set[str]:
    document = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    return set((document.get(key) or {}).keys())


def main() -> int:
    failures: list[str] = []
    checked = 0
    for service in sorted(SERVICES.glob("be-*")):
        application = service / "src" / "main" / "resources" / "application.yml"
        if not application.exists():
            continue
        checked += 1
        variables = set(ENV_PATTERN.findall(application.read_text(encoding="utf-8")))
        config_dir = CI / service.name
        if not config_dir.is_dir():
            failures.append(f"{service.name}: missing ci-cdconfigs folder")
            continue
        for variable in sorted(variables):
            if SECRET_PATTERN.search(variable):
                secret_env = config_dir / "env" / "secrets.env.example"
                secret_yaml = config_dir / "kubernetes" / "base" / "secret.example.yaml"
                if variable not in env_keys(secret_env):
                    failures.append(f"{service.name}: secrets.env.example missing {variable}")
                if variable not in yaml_keys(secret_yaml, "stringData"):
                    failures.append(f"{service.name}: Kubernetes Secret example missing {variable}")
                continue
            for environment in ("local", "dev", "staging", "prod"):
                env_file = config_dir / "env" / f"{environment}.env.example"
                if variable not in env_keys(env_file):
                    failures.append(f"{service.name}: {environment} env missing {variable}")
            base_config_map = config_dir / "kubernetes" / "base" / "configmap.yaml"
            if base_config_map.exists():
                failures.append(
                    f"{service.name}: reusable Kubernetes base must not commit configmap.yaml; "
                    "materialize it from deployment-time env"
                )

    if failures:
        print(f"CI_CONFIG_COVERAGE_FAIL services={checked} failures={len(failures)}")
        for failure in failures:
            print("FAIL:", failure)
        return 1
    print(f"CI_CONFIG_COVERAGE_PASS services={checked}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
