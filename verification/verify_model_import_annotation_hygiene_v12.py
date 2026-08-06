#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
SERVICES = BACKEND / "services"
errors: list[str] = []


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


# 1. Convention/spec must not force noise comments and must define architectural stereotypes broadly.
agents = read(ROOT / "AGENTS.MD")
conventions = read(ROOT / ".agent" / "CONVENTIONS.MD")
spec = read(ROOT / ".agent" / "specs" / "013-codebase-deep-refactor.md")
for name, text in [("AGENTS.MD", agents), ("CONVENTIONS.MD", conventions), ("spec013", spec)]:
    if "All class fields must have meaningful JavaDoc" in text:
        errors.append(f"{name}: forces JavaDoc on every field")
    if re.search(r"@Adapter[^\n]*non-persistence outbound", text, flags=re.IGNORECASE):
        errors.append(f"{name}: @Adapter is documented as outbound-only")

# 1b. Custom Adapter must support an explicit Spring bean name so SpEL-facing adapters do not need a second @Component annotation.
adapter_source = read(BACKEND / "platform" / "be-platform-starter" / "src" / "main" / "java" / "com" / "dnnthanh" / "marketplace" / "be" / "platform" / "stereotype" / "Adapter.java")
if "AliasFor" not in adapter_source or "String value() default """ not in adapter_source:
    errors.append("Adapter stereotype must expose Component.value through @AliasFor")

for relative in [
    "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/security/AuthorizationService.java",
    "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/security/ServiceTokenProvider.java",
    "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/web/logging/StructuredHttpLoggingFilter.java",
    "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/kafka/DomainEventProducer.java",
]:
    path = BACKEND / relative
    source = read(path)
    if "@Component" in source or "@Adapter" not in source:
        errors.append(f"{path.relative_to(ROOT)}: infrastructure boundary must use @Adapter, not @Component")

# 2. Build a source FQN index and validate internal imports after DTO/package moves.
known: set[str] = set()
java_files = list(BACKEND.rglob("*.java"))
for path in java_files:
    text = read(path)
    package = re.search(r"^package\s+([\w.]+);", text, flags=re.MULTILINE)
    if package:
        known.add(f"{package.group(1)}.{path.stem}")

for path in java_files:
    text = read(path)
    for imported in re.findall(r"^import\s+(com\.dnnthanh\.marketplace\.[\w.]+);", text, flags=re.MULTILINE):
        if imported.endswith(".*"):
            continue
        candidate = imported
        valid = candidate in known
        # Static/nested type imports may append members/classes after a real top-level FQN.
        if not valid:
            parts = candidate.split(".")
            for end in range(len(parts) - 1, 3, -1):
                if ".".join(parts[:end]) in known:
                    valid = True
                    break
        if not valid:
            errors.append(f"{path.relative_to(ROOT)}: unresolved internal import {imported}")

# 3. API root should contain contracts, not transport data classes/records.
for api_root in SERVICES.glob("*/src/main/java/**/api/api"):
    if not api_root.is_dir():
        continue
    for path in api_root.glob("*.java"):
        if path.name.endswith("Api.java") or path.name.endswith("Controller.java"):
            continue
        text = read(path)
        if re.search(r"\b(record|class)\s+\w+", text):
            errors.append(f"{path.relative_to(ROOT)}: transport model is stored beside API contracts; move to request/response")

# 4. Application/outbound code must not import transport request/response models.
for path in SERVICES.rglob("*.java"):
    rel = str(path.relative_to(ROOT)).replace("\\", "/")
    if "/application/" not in rel and "/adapter/out/" not in rel:
        continue
    text = read(path)
    for imported in re.findall(r"^import\s+([\w.]+);", text, flags=re.MULTILINE):
        if ".api.request." in imported or ".api.response." in imported:
            errors.append(f"{path.relative_to(ROOT)}: lower layer imports transport model {imported}")

# 5. Named mapper classes/interfaces must use MapStruct, except generated/marker-free mapping is not allowed.
for path in SERVICES.rglob("*Mapper.java"):
    text = read(path)
    if "org.mapstruct.Mapper" not in text or "@Mapper" not in text:
        errors.append(f"{path.relative_to(ROOT)}: *Mapper must use MapStruct")

# 6. Boundary role annotations: obvious inbound/outbound infrastructure should not fall back to @Component.
role_suffixes = ("Consumer.java", "Publisher.java", "Scheduler.java", "Job.java", "Worker.java", "RestAdapter.java", "GrpcAdapter.java", "WebhookAdapter.java")
for path in SERVICES.rglob("*.java"):
    if not path.name.endswith(role_suffixes):
        continue
    text = read(path)
    if "@Component" in text:
        errors.append(f"{path.relative_to(ROOT)}: boundary class uses generic @Component")

# 7. Avoid Spring @Value string injection in services; use typed configuration properties.
for path in SERVICES.rglob("*.java"):
    text = read(path)
    if "org.springframework.beans.factory.annotation.Value" in text:
        errors.append(f"{path.relative_to(ROOT)}: use typed configuration properties instead of @Value")

# 8. Exception names must be unique within a module and exceptions should be in an exception package.
for module in SERVICES.iterdir():
    if not module.is_dir():
        continue
    by_name: dict[str, list[Path]] = defaultdict(list)
    for path in module.rglob("*Exception.java"):
        by_name[path.name].append(path)
        rel = str(path.relative_to(module)).replace("\\", "/")
        # Framework main class names such as Application may live elsewhere, exception types should not.
        if "/exception/" not in f"/{rel}":
            errors.append(f"{path.relative_to(ROOT)}: exception type must live under an exception package")
    for name, paths in by_name.items():
        if len(paths) > 1:
            errors.append(f"{module.name}: duplicate exception {name}: " + ", ".join(str(p.relative_to(ROOT)) for p in paths))

# 8b. Ban repeated generator-style JavaDoc that only restates architectural plumbing.
noise_comments = [
    "Inbound application port. HTTP/jobs/consumers depend on this abstraction, not the handler.",
    "Publishes one bounded batch and marks each row only after Kafka acknowledges the send.",
    "Database identifier.",
]
for path in java_files:
    source = read(path)
    for comment in noise_comments:
        if comment in source:
            errors.append(f"{path.relative_to(ROOT)}: redundant generated comment: {comment}")

# 9. Duplicate ordinary imports and wildcard imports create move/refactor noise.
for path in java_files:
    text = read(path)
    imports = re.findall(r"^import\s+(static\s+)?([\w.*]+);", text, flags=re.MULTILINE)
    normalized = [(prefix or "") + target for prefix, target in imports]
    if len(normalized) != len(set(normalized)):
        errors.append(f"{path.relative_to(ROOT)}: duplicate import")
    for _, target in imports:
        if target.endswith(".*"):
            errors.append(f"{path.relative_to(ROOT)}: wildcard import {target}")

if errors:
    print("MODEL_IMPORT_ANNOTATION_HYGIENE_V12=FAIL")
    for error in errors[:200]:
        print("-", error)
    if len(errors) > 200:
        print(f"... {len(errors) - 200} more")
    sys.exit(1)

print("MODEL_IMPORT_ANNOTATION_HYGIENE_V12=PASS")
print(f"java_files={len(java_files)} internal_types={len(known)}")
