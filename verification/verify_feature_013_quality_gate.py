from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SERVICES = ROOT / 'backend/services'
failures = []
passes = []


def check(name: str, condition: bool, detail: str = '') -> None:
    (passes if condition else failures).append((name, detail))

# 1) No compressed Java in bounded-context source. A three-line source is not maintainable project code.
compressed = []
for path in SERVICES.rglob('src/main/java/**/*.java'):
    line_count = len(path.read_text(encoding='utf-8', errors='ignore').splitlines())
    if line_count <= 3:
        compressed.append(f'{path.relative_to(ROOT)}:{line_count}')
check('no compressed <=3-line Java source', not compressed, ', '.join(compressed[:20]))

# 2) Application/use-case layer cannot own SQL/JdbcClient.
application_jdbc = []
for path in SERVICES.rglob('src/main/java/**/*.java'):
    rel = path.relative_to(ROOT).as_posix()
    if '/application/' not in rel:
        continue
    text = path.read_text(encoding='utf-8', errors='ignore')
    if 'JdbcClient' in text or 'NamedParameterJdbcTemplate' in text:
        application_jdbc.append(rel)
check('application layer contains no direct JDBC', not application_jdbc, ', '.join(application_jdbc[:20]))

# 3) No manual HTTP/business input trimming in domain/application.
manual_trim = []
for path in SERVICES.rglob('src/main/java/**/*.java'):
    rel = path.relative_to(ROOT).as_posix()
    if '/domain/' not in rel and '/application/' not in rel:
        continue
    if '.trim()' in path.read_text(encoding='utf-8', errors='ignore'):
        manual_trim.append(rel)
check('domain/application contains no manual trim', not manual_trim, ', '.join(manual_trim[:20]))

# 4) Generic business exceptions are forbidden in domain/application layers.
generic_business_ex = []
for path in SERVICES.rglob('src/main/java/**/*.java'):
    rel = path.relative_to(ROOT).as_posix()
    if '/domain/' not in rel and '/application/' not in rel:
        continue
    text = path.read_text(encoding='utf-8', errors='ignore')
    if 'new IllegalArgumentException' in text or 'new IllegalStateException' in text:
        generic_business_ex.append(rel)
check('domain/application contains no generic business exceptions', not generic_business_ex,
      ', '.join(generic_business_ex[:30]))

# 5) Generic @Service must be replaced by architecture stereotypes.
service_annotations = []
for path in SERVICES.rglob('src/main/java/**/*.java'):
    if '@Service' in path.read_text(encoding='utf-8', errors='ignore'):
        service_annotations.append(path.relative_to(ROOT).as_posix())
check('no generic @Service stereotypes', not service_annotations, ', '.join(service_annotations[:20]))

# 6) Important context architecture docs are mandatory.
contexts = [
    'catalog', 'media', 'search', 'pricing', 'promotion', 'inventory', 'cart', 'checkout', 'order',
    'payment', 'fulfillment', 'return', 'review', 'comment', 'notification', 'seller', 'authorization',
    'audit', 'settlement', 'operations'
]
missing_docs = []
for context in contexts:
    path = ROOT / f'docs/architecture/contexts/{context}.md'
    if not path.exists():
        missing_docs.append(str(path.relative_to(ROOT)))
check('all major contexts have architecture/use-case mapping docs', not missing_docs, ', '.join(missing_docs))

# 7) Shared test stack required by spec must be managed by parent build.
pom = (ROOT / 'backend/pom.xml').read_text(encoding='utf-8')
for artifact in ['archunit-junit5', 'awaitility', 'testcontainers', 'resilience4j-circuitbreaker', 'resilience4j-bulkhead']:
    check(f'parent build declares {artifact}', artifact in pom)

# 8) Maven wrapper entrypoint is part of reproducible build.
check('mvnw exists', (ROOT / 'mvnw').exists())
check('maven-wrapper.properties exists', (ROOT / '.mvn/wrapper/maven-wrapper.properties').exists())

for name, detail in passes:
    print(f'PASS: {name}' + (f' [{detail}]' if detail else ''))
for name, detail in failures:
    print(f'FAIL: {name}' + (f' [{detail}]' if detail else ''))
print(f'Summary: {len(passes)} passed, {len(failures)} failed')
sys.exit(1 if failures else 0)
