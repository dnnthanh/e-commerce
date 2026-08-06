from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
errors = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


root_text = (BACKEND / "pom.xml").read_text(errors="ignore")
root_tree = ET.parse(BACKEND / "pom.xml")
root = root_tree.getroot()
parent = root.find("m:parent", NS)
require(parent is not None, "backend parent POM must inherit Spring Boot parent")
if parent is not None:
    require(parent.findtext("m:groupId", namespaces=NS) == "org.springframework.boot", "root parent groupId must be org.springframework.boot")
    require(parent.findtext("m:artifactId", namespaces=NS) == "spring-boot-starter-parent", "root parent must be spring-boot-starter-parent")
    require(parent.findtext("m:version", namespaces=NS) == "4.1.0", "Spring Boot parent must be 4.1.0")

require(root.findtext("m:properties/m:java.version", namespaces=NS) == "25", "java.version must be 25")

managed = set()
for dependency in root.findall("m:dependencyManagement/m:dependencies/m:dependency", NS):
    group_id = dependency.findtext("m:groupId", namespaces=NS)
    artifact_id = dependency.findtext("m:artifactId", namespaces=NS)
    if group_id and artifact_id:
        managed.add((group_id, artifact_id))

for artifact in ("be-platform-starter", "be-platform-cache-starter"):
    require(("com.dnnthanh.marketplace", artifact) in managed, f"root dependencyManagement missing {artifact}")

module_paths = [node.text for node in root.findall("m:modules/m:module", NS) if node.text]
require(bool(module_paths), "backend reactor has no modules")
for module in module_paths:
    require((BACKEND / module / "pom.xml").exists(), f"reactor module missing POM: {module}")

all_poms = list(BACKEND.rglob("pom.xml"))
for pom in all_poms:
    try:
        tree = ET.parse(pom)
    except Exception as exc:
        errors.append(f"invalid POM XML {pom.relative_to(ROOT)}: {exc}")
        continue
    project = tree.getroot()
    if pom != BACKEND / "pom.xml":
        child_parent = project.find("m:parent", NS)
        require(child_parent is not None, f"child POM has no parent: {pom.relative_to(ROOT)}")
        if child_parent is not None:
            require(
                child_parent.findtext("m:groupId", namespaces=NS) == "com.dnnthanh.marketplace"
                and child_parent.findtext("m:artifactId", namespaces=NS) == "marketplace-backend"
                and child_parent.findtext("m:version", namespaces=NS) == "1.0.0-SNAPSHOT",
                f"child POM does not inherit marketplace-backend: {pom.relative_to(ROOT)}",
            )
    for dependency in project.findall("m:dependencies/m:dependency", NS):
        group_id = dependency.findtext("m:groupId", namespaces=NS)
        artifact_id = dependency.findtext("m:artifactId", namespaces=NS)
        version = dependency.findtext("m:version", namespaces=NS)
        if group_id == "com.dnnthanh.marketplace" and not version:
            require((group_id, artifact_id) in managed, f"unmanaged internal dependency {artifact_id} in {pom.relative_to(ROOT)}")

all_pom_text = "\n".join(p.read_text(errors="ignore") for p in all_poms)
require("resilience4j-spring-boot3" not in all_pom_text, "Boot-3-specific Resilience4j starter remains on Boot 4")
require("spring-boot-starter-aop" not in all_pom_text, "obsolete Boot 3 AOP starter remains")
require("<artifactId>testcontainers-junit-jupiter</artifactId>" in root_text, "Testcontainers 2.x junit artifact missing")
require("<testcontainers.version>2.0.5</testcontainers.version>" in root_text, "Testcontainers must be pinned to 2.0.5")
parent_dependencies = root_text.split("<dependencyManagement>", 1)[0]
require("testcontainers-junit-jupiter" not in parent_dependencies and "<artifactId>testcontainers</artifactId>" not in parent_dependencies, "Testcontainers must not be inherited by every module")


# Modules compiling MapStruct mappers need the MapStruct API on their compile classpath.
mapper_modules = []
for module in (BACKEND / 'services').iterdir():
    if not module.is_dir() or not (module / 'pom.xml').exists():
        continue
    java_root = module / 'src/main/java'
    if java_root.exists() and any('@Mapper' in source.read_text(errors='ignore') for source in java_root.rglob('*.java')):
        mapper_modules.append(module)
        require(
            '<artifactId>mapstruct</artifactId>' in (module / 'pom.xml').read_text(errors='ignore'),
            f'MapStruct API dependency missing in {module.name}',
        )
require(
    re.search(r'<artifactId>mapstruct</artifactId>\s*<version>\$\{mapstruct.version\}</version>', root_text, re.S) is not None,
    'root dependencyManagement must manage MapStruct API version',
)

if errors:
    print("MAVEN_MODEL_V5=FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print(f"MAVEN_MODEL_V5=PASS poms={len(all_poms)} modules={len(module_paths)}")
