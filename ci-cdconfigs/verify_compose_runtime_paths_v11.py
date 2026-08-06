from pathlib import Path
import sys
import yaml

root = Path(__file__).resolve().parents[1]
files = [
    root / "docker-compose.yml",
    *sorted((root / "compose").glob("*.yml")),
    *sorted((root / "compose/backend").glob("*.yml")),
]
errors: list[str] = []
builds = 0
binds = 0


def validate_repo_path(source: str, where: str) -> None:
    global binds
    # Named volumes and container-only paths are not host bind paths.
    if not source.startswith((".", "/")):
        return
    if source.startswith("/"):
        # Explicit host mounts are required by observability agents such as cAdvisor/Alloy.
        # They do not cause Compose to traverse the project parent directory.
        return
    binds += 1
    parts = Path(source).parts
    if ".." in parts:
        errors.append(f"{where} parent traversal forbidden: {source}")
        return
    resolved = (root / source).resolve()
    try:
        resolved.relative_to(root.resolve())
    except ValueError:
        errors.append(f"{where} path escapes repository: {source}")
        return
    if not resolved.exists():
        errors.append(f"{where} missing host path: {source}")


for f in files:
    try:
        data = yaml.safe_load(f.read_text()) or {}
    except Exception as exc:
        errors.append(f"{f.relative_to(root)} yaml: {exc}")
        continue

    for name, svc in (data.get("services") or {}).items():
        if not isinstance(svc, dict):
            continue

        build = svc.get("build")
        if build:
            builds += 1
            if isinstance(build, str):
                context = build
                dockerfile = "Dockerfile"
            else:
                context = build.get("context", ".")
                dockerfile = build.get("dockerfile", "Dockerfile")
            validate_repo_path(str(context), f"{f.relative_to(root)}:{name}:build")
            resolved_context = (root / context).resolve()
            dockerfile_path = (resolved_context / dockerfile).resolve()
            if not dockerfile_path.exists():
                errors.append(
                    f"{f.relative_to(root)}:{name} missing Dockerfile {dockerfile_path}"
                )

        for volume in svc.get("volumes") or []:
            if isinstance(volume, str):
                source = volume.split(":", 1)[0]
                validate_repo_path(source, f"{f.relative_to(root)}:{name}:volume")
            elif isinstance(volume, dict) and volume.get("type") == "bind":
                validate_repo_path(
                    str(volume.get("source", "")),
                    f"{f.relative_to(root)}:{name}:volume",
                )


        networks = svc.get("networks") or []
        if isinstance(networks, dict):
            networks = list(networks)
        if "marketplace" in networks:
            declared = (data.get("networks") or {}).get("marketplace") or {}
            if isinstance(declared, dict):
                if declared.get("name") not in (None, "marketplace-network"):
                    errors.append(f"{f.relative_to(root)} marketplace network name must be marketplace-network")
                if declared.get("external") is True:
                    errors.append(f"{f.relative_to(root)} marketplace network must not be external in composable fragments")

        env_file = svc.get("env_file") or []
        if isinstance(env_file, str):
            env_file = [env_file]
        for entry in env_file:
            if isinstance(entry, str):
                validate_repo_path(entry, f"{f.relative_to(root)}:{name}:env_file")
            elif isinstance(entry, dict) and entry.get("path"):
                validate_repo_path(
                    str(entry["path"]), f"{f.relative_to(root)}:{name}:env_file"
                )

root_data = yaml.safe_load((root / "docker-compose.yml").read_text())
for name, svc in root_data["services"].items():
    if name.startswith("be-") and "build" in svc:
        if svc["build"].get("context") != "./backend":
            errors.append(f"root:{name} backend context must be ./backend")
    if name in {"storefront", "admin"}:
        if svc.get("build", {}).get("context") != "./frontend":
            errors.append(f"root:{name} frontend context must be ./frontend")

if errors:
    print("COMPOSE_RUNTIME_PATHS_V11=FAIL")
    for error in errors:
        print("-", error)
    sys.exit(1)

print(
    f"COMPOSE_RUNTIME_PATHS_V11=PASS files={len(files)} builds={builds} host_paths={binds}"
)
