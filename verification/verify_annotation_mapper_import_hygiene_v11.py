#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "backend" / "services"
issues = []

for path in JAVA_ROOT.rglob("*Mapper.java"):
    text = path.read_text(encoding="utf-8")
    if "@Mapper" not in text:
        issues.append(f"mapper-not-mapstruct:{path.relative_to(ROOT)}")
    if "org.springframework.stereotype.Component" in text or "@Component" in text:
        issues.append(f"mapper-generic-component:{path.relative_to(ROOT)}")

for path in JAVA_ROOT.rglob("*PersistenceException.java"):
    if "/exception/" not in str(path).replace("\\", "/"):
        issues.append(f"persistence-exception-package:{path.relative_to(ROOT)}")

for module in [p for p in JAVA_ROOT.iterdir() if p.is_dir()]:
    by_name = {}
    for path in module.rglob("*Exception.java"):
        by_name.setdefault(path.name, []).append(path)
    for name, paths in by_name.items():
        if len(paths) > 1:
            joined = ",".join(str(p.relative_to(ROOT)) for p in paths)
            issues.append(f"duplicate-exception:{module.name}:{name}:{joined}")

for path in JAVA_ROOT.rglob("*Materializer.java"):
    text = path.read_text(encoding="utf-8")
    if "JdbcClient" in text:
        if "@Persistence" not in text:
            issues.append(f"jdbc-materializer-not-persistence:{path.relative_to(ROOT)}")
        if "@RequiredArgsConstructor" not in text:
            issues.append(f"jdbc-materializer-no-lombok:{path.relative_to(ROOT)}")
        if "@Component" in text:
            issues.append(f"jdbc-materializer-generic-component:{path.relative_to(ROOT)}")

for path in JAVA_ROOT.rglob("*.java"):
    normalized = str(path).replace("\\", "/")
    if "/application/" not in normalized and "/adapter/out/" not in normalized:
        continue
    text = path.read_text(encoding="utf-8")
    if re.search(r"^import\s+com\.dnnthanh\.marketplace\..*\.api\.api\.(request|response)\.", text, re.M):
        issues.append(f"transport-import-leak:{path.relative_to(ROOT)}")

for path in JAVA_ROOT.rglob("src/main/java/**/*.java"):
    text = path.read_text(encoding="utf-8")
    imports = re.findall(r"^import\s+([^;]+);", text, re.M)
    if len(imports) != len(set(imports)):
        issues.append(f"duplicate-import:{path.relative_to(ROOT)}")
    if any(item.endswith(".*") for item in imports):
        issues.append(f"wildcard-import:{path.relative_to(ROOT)}")

if issues:
    print("ANNOTATION_MAPPER_IMPORT_HYGIENE_V11=FAIL")
    for issue in issues:
        print(issue)
    sys.exit(1)

print("ANNOTATION_MAPPER_IMPORT_HYGIENE_V11=PASS")
