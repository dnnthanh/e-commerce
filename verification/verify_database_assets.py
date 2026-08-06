#!/usr/bin/env python3
"""Static verification for Liquibase ownership and separated seed SQL assets."""

from pathlib import Path
import re
import xml.etree.ElementTree as ET
import yaml

ROOT = Path(__file__).resolve().parents[1]
failures: list[str] = []
changelogs = sorted(ROOT.glob("backend/services/be-*-api/src/main/resources/db/changelog/db.changelog-master.yaml"))

for changelog in changelogs:
    module = changelog.parents[5]
    pom = module / "pom.xml"
    pom_text = pom.read_text()
    if "liquibase-core" not in pom_text:
        failures.append(f"{module.name}: changelog exists but liquibase-core dependency is missing")

    raw = changelog.read_text()
    if re.search(r"^\s+- insert:", raw, flags=re.MULTILINE):
        failures.append(f"{changelog.relative_to(ROOT)}: inline insert found; seed DML must use sqlFile")
    if re.search(r"^\s+- sql:\s*$", raw, flags=re.MULTILINE):
        failures.append(f"{changelog.relative_to(ROOT)}: inline SQL found; use a dedicated SQL file")

    document = yaml.safe_load(raw)
    if not isinstance(document, dict) or "databaseChangeLog" not in document:
        failures.append(f"{changelog.relative_to(ROOT)}: invalid Liquibase YAML root")
        continue

    for change in document["databaseChangeLog"]:
        change_set = change.get("changeSet", {}) if isinstance(change, dict) else {}
        for item in change_set.get("changes", []) or []:
            if not isinstance(item, dict) or "sqlFile" not in item:
                continue
            sql_file = item["sqlFile"]
            relative_path = sql_file.get("path")
            relative_to_changelog = sql_file.get("relativeToChangelogFile", False)
            if relative_to_changelog:
                resolved = changelog.parent / relative_path
            else:
                resources_root = next(parent for parent in changelog.parents if parent.name == "resources")
                resolved = resources_root / relative_path
            if not resolved.exists():
                failures.append(f"missing sqlFile {resolved.relative_to(ROOT)}")

for pom in ROOT.rglob("pom.xml"):
    try:
        ET.parse(pom)
    except ET.ParseError as error:
        failures.append(f"invalid XML {pom.relative_to(ROOT)}: {error}")

if failures:
    raise SystemExit("\n".join(failures))

print(f"LIQUIBASE_CHANGELOGS_PASS={len(changelogs)}")
print("RELATIONAL_SEED_SQL_SEPARATION_PASS=true")
