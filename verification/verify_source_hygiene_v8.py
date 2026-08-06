#!/usr/bin/env python3
from pathlib import Path
import re
import sys
from collections import defaultdict

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / 'backend'
SERVICES = BACKEND / 'services'
errors = []

java_files = list(BACKEND.rglob('*.java'))

# 1) Duplicate simple-name imports make source ambiguous/uncompilable.
for f in java_files:
    text = f.read_text(errors='ignore')
    imports = re.findall(r'^import\s+(?!static)([\w.]+);', text, re.M)
    by_simple = defaultdict(set)
    for imp in imports:
        by_simple[imp.rsplit('.', 1)[-1]].add(imp)
    for simple, fqns in by_simple.items():
        if len(fqns) > 1:
            errors.append(f'DUPLICATE_IMPORT {f.relative_to(ROOT)} {simple}: {sorted(fqns)}')

# 2) Hexagonal dependency direction: application and outbound adapters cannot import HTTP transport models.
for f in java_files:
    rel = f.relative_to(ROOT).as_posix()
    if '/application/' not in rel and '/adapter/out/' not in rel:
        continue
    text = f.read_text(errors='ignore')
    for imp in re.findall(r'^import\s+([\w.]+);', text, re.M):
        if '.api.api.' in imp:
            errors.append(f'TRANSPORT_IMPORT_LEAK {rel}: {imp}')

# 3) Public API interfaces are contracts only; no nested records/enums/classes.
for f in SERVICES.rglob('*Api.java'):
    text = f.read_text(errors='ignore')
    if re.search(r'\b(record|enum|class)\s+\w+', text):
        errors.append(f'NESTED_API_MODEL {f.relative_to(ROOT)}')

# 4) RestClient modules must opt into Boot 4 rest-client auto-configuration.
restclient_modules = set()
for f in java_files:
    if 'RestClient' not in f.read_text(errors='ignore'):
        continue
    cur = f.parent
    while cur != BACKEND and not (cur / 'pom.xml').exists():
        cur = cur.parent
    if (cur / 'pom.xml').exists():
        restclient_modules.add(cur)
for mod in sorted(restclient_modules):
    pom = (mod / 'pom.xml').read_text(errors='ignore')
    if 'spring-boot-starter-restclient' not in pom:
        errors.append(f'MISSING_RESTCLIENT_STARTER {mod.relative_to(ROOT)}')

# 5) Worker/job modules with no HTTP inbound adapter should not pull full MVC starter merely for RestClient.
for mod in sorted(restclient_modules):
    rel = mod.relative_to(BACKEND).as_posix()
    if not ('-worker' in rel or '-job' in rel):
        continue
    all_java = '\n'.join(p.read_text(errors='ignore') for p in mod.rglob('*.java'))
    inbound = any(token in all_java for token in ('@RestController', '@RequestMapping', '@GetMapping', '@PostMapping', '@PutMapping', '@PatchMapping', '@DeleteMapping'))
    pom = (mod / 'pom.xml').read_text(errors='ignore')
    if not inbound and 'spring-boot-starter-web' in pom:
        errors.append(f'UNNECESSARY_WEB_STARTER {mod.relative_to(ROOT)}')

# 6) Non-persistence outbound adapters must use architectural @Adapter, not generic @Component.
for f in SERVICES.rglob('*Adapter.java'):
    rel = f.relative_to(ROOT).as_posix()
    if '/adapter/out/persistence/' in rel or '/adapter/out/mongo/' in rel:
        continue
    if '/adapter/out/' not in rel:
        continue
    text = f.read_text(errors='ignore')
    if '@Component' in text:
        errors.append(f'GENERIC_COMPONENT_ON_ADAPTER {rel}')

# 7) No raw Map wire contracts in Checkout internal service-to-service adapters.
checkout_internal = SERVICES / 'be-checkout-api' / 'src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal'
if checkout_internal.exists():
    for f in checkout_internal.rglob('*RestAdapter.java'):
        text = f.read_text(errors='ignore')
        if 'Map.class' in text or 'Map.of(' in text:
            errors.append(f'RAW_MAP_INTERNAL_CONTRACT {f.relative_to(ROOT)}')

# 8) Known regression: Catalog state invariant test must expect the domain-specific exception.
product_test = SERVICES / 'be-catalog-api/src/test/java/com/dnnthanh/marketplace/be/catalog/api/domain/model/ProductTest.java'
if product_test.exists():
    text = product_test.read_text(errors='ignore')
    if 'assertThrows(BusinessException.class' in text:
        errors.append('STALE_CATALOG_EXCEPTION_TEST be-catalog-api ProductTest')

# 9) Obvious generated/stale comment debris in API interfaces.
for f in SERVICES.rglob('*Api.java'):
    text = f.read_text(errors='ignore')
    if re.search(r'/\*\*\s*@param[^*]*\*/\s*/\*\*', text, re.S) or 'Supported payment providers at the Checkout contract boundary' in text:
        errors.append(f'STALE_API_COMMENT {f.relative_to(ROOT)}')

if errors:
    print(f'SOURCE_HYGIENE_V8=FAIL count={len(errors)}')
    for e in errors:
        print(e)
    sys.exit(1)
print('SOURCE_HYGIENE_V8=PASS')
print(f'java_files={len(java_files)} restclient_modules={len(restclient_modules)}')
