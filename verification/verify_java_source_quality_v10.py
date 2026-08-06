#!/usr/bin/env python3
from pathlib import Path
from collections import Counter
import re, sys
ROOT=Path(__file__).resolve().parents[1]
BACKEND=ROOT/'backend'
errors=[]
main_files=list(BACKEND.rglob('src/main/java/**/*.java'))
# exact duplicate imports and packed import lines
for f in main_files:
    text=f.read_text(errors='ignore')
    lines=text.splitlines()
    imports=[line.strip() for line in lines if line.strip().startswith('import ')]
    for imp,count in Counter(imports).items():
        if count>1: errors.append(f'DUPLICATE_IMPORT {f.relative_to(ROOT)} {imp}')
    if any(re.match(r'^\s*import\s+.*;\s+import\s+', line) for line in lines):
        errors.append(f'PACKED_IMPORTS {f.relative_to(ROOT)}')
    for line in imports:
        if line.endswith('.*;'):
            errors.append(f'WILDCARD_IMPORT {f.relative_to(ROOT)} {line}')
# application/outbound may not import HTTP transport model
for f in main_files:
    rel=f.relative_to(ROOT).as_posix(); text=f.read_text(errors='ignore')
    if '/application/' in rel or '/adapter/out/' in rel:
        for imp in re.findall(r'^import\s+([\w.]+);', text, re.M):
            if '.api.api.request.' in imp or '.api.api.response.' in imp:
                errors.append(f'TRANSPORT_LEAK {rel} {imp}')
# Internal/external REST classes should bind structured properties rather than scattered Spring @Value.
for f in main_files:
    text=f.read_text(errors='ignore'); rel=f.relative_to(ROOT).as_posix()
    if 'RestClient' in text and 'org.springframework.beans.factory.annotation.Value' in text:
        errors.append(f'RESTCLIENT_VALUE_INJECTION {rel}')
# Architectural stereotypes
for f in main_files:
    rel=f.relative_to(ROOT).as_posix(); text=f.read_text(errors='ignore')
    if '/adapter/out/' in rel and f.name.endswith('Adapter.java') and '/persistence/' not in rel and '/mongo/' not in rel:
        if 'abstract class ' not in text and '@Adapter' not in text:
            errors.append(f'MISSING_ADAPTER_STEREOTYPE {rel}')
    if f.name.endswith('PersistenceAdapter.java') and '@Persistence' not in text:
        errors.append(f'MISSING_PERSISTENCE_STEREOTYPE {rel}')
    if f.name.endswith('ServiceImplement.java') and '/application/' in rel and '@UseCase' not in text:
        errors.append(f'MISSING_USECASE_STEREOTYPE {rel}')
# Obvious generator-comments: comments that only state constructor/field plumbing.
noise=(
    'Creates the client.', 'Internal REST client.', 'Internal HTTP client.',
    'Keycloak client-credentials provider.', 'Keycloak client-credentials token provider.',
    'Transactional local projection writer.', 'Transactional shipment materializer.',
    'Internal Checkout REST client.', 'Internal Payment REST client.'
)
for f in main_files:
    text=f.read_text(errors='ignore')
    for marker in noise:
        if marker in text:
            errors.append(f'REDUNDANT_COMMENT {f.relative_to(ROOT)} {marker}')
            break
# POM compile dependency for RestClient and MapStruct API
for pom in BACKEND.rglob('pom.xml'):
    if pom == BACKEND/'pom.xml': continue
    java='\n'.join(p.read_text(errors='ignore') for p in pom.parent.rglob('src/main/java/**/*.java'))
    ptext=pom.read_text(errors='ignore')
    if 'RestClient' in java and 'spring-boot-starter-restclient' not in ptext:
        errors.append(f'MISSING_RESTCLIENT_STARTER {pom.relative_to(ROOT)}')
    if '@Mapper' in java and '<artifactId>mapstruct</artifactId>' not in ptext:
        errors.append(f'MISSING_MAPSTRUCT_API {pom.relative_to(ROOT)}')
if errors:
    print(f'JAVA_SOURCE_QUALITY_V10=FAIL count={len(errors)}')
    for e in errors: print(e)
    sys.exit(1)
print(f'JAVA_SOURCE_QUALITY_V10=PASS files={len(main_files)}')
