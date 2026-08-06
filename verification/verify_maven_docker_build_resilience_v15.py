from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
errors = []

for rel in ["backend/Dockerfile.service", "docker/backend/Dockerfile.service", "Dockerfile.backend"]:
    p = ROOT / rel
    text = p.read_text()
    for token in [
        "# syntax=docker/dockerfile:1.7",
        "--mount=type=cache,id=marketplace-maven-repository,target=/root/.m2",
        "-Daether.connector.http.retryHandler.count=8",
        "-Daether.connector.http.retryHandler.interval=2000",
        "-B -ntp",
    ]:
        if token not in text:
            errors.append(f"{rel}: missing {token}")

# .env is intentionally local-only and gitignored. Validate an explicit local override
# when present, but CI must also work from a clean checkout with no .env file.
env_path = ROOT / ".env"
env_limit = None
if env_path.exists():
    env = env_path.read_text()
    env_match = re.search(r"(?m)^COMPOSE_PARALLEL_LIMIT=(\d+)$", env)
    if not env_match or int(env_match.group(1)) > 4:
        errors.append(".env: COMPOSE_PARALLEL_LIMIT must be present and <= 4 when .env exists")
    else:
        env_limit = env_match.group(1)

wrapper = (ROOT / "compose-up.sh").read_text()
wrapper_match = re.search(
    r'COMPOSE_PARALLEL_LIMIT="\$\{COMPOSE_PARALLEL_LIMIT:-([1-4])\}"',
    wrapper,
)
if not wrapper_match:
    errors.append("compose-up.sh: missing conservative default parallel limit")

effective_limit = env_limit or (wrapper_match.group(1) if wrapper_match else "missing")

if errors:
    print("MAVEN_DOCKER_BUILD_RESILIENCE_V15=FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)

print("MAVEN_DOCKER_BUILD_RESILIENCE_V15=PASS")
print("maven_cache=shared")
print("resolver_retries=8")
print(f"compose_parallel_limit={effective_limit}")
