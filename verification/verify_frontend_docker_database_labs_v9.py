#!/usr/bin/env python3
from pathlib import Path
import re, sys, yaml

root=Path(__file__).resolve().parents[1]
errors=[]

def check(cond,msg):
    if not cond: errors.append(msg)

api=(root/'frontend/shared/api.service.ts').read_text()
contract=(root/'frontend/shared/api-contract.ts').read_text()
check('getPage<T>' in api,'frontend ApiService must expose getPage<T>')
check('getCursor<T>' in api,'frontend ApiService must expose getCursor<T>')
check("response.headers.get('trace-id')" in api,'frontend must preserve backend trace-id')
check('interface ApiEnvelope' in contract and 'interface PageMetadata' in contract and 'interface CursorMetadata' in contract,'typed API envelope/page/cursor contracts missing')

frontend='\n'.join(p.read_text() for p in (root/'frontend').rglob('*.ts'))
legacy_limits=[m.group(0) for m in re.finditer(r'[^\n]*[?&]limit=[^\n]*',frontend) if '/private/notifications?limit=' not in m.group(0)]
check(not legacy_limits, 'frontend still uses legacy limit= pagination outside notification feed')
search=(root/'frontend/projects/storefront/src/app/features/search.component.ts').read_text()
check("q.set('page'" not in search, 'OpenSearch UI must not use page offset')
check('marketplace.search(' in search and 'nextCursor' in search, 'OpenSearch UI must use cursor response metadata')

compose_files=[root/'docker-compose.yml',*(root/'compose').rglob('*.yml')]
for p in compose_files:
    try: yaml.safe_load(p.read_text())
    except Exception as exc: errors.append(f'{p.relative_to(root)} YAML invalid: {exc}')
    check(not re.search(r'test:\s*\n\s*-',p.read_text()), f'{p.relative_to(root)} uses IDE-hostile block healthcheck.test')

root_compose=yaml.safe_load((root/'docker-compose.yml').read_text())
backend={k:v for k,v in root_compose['services'].items() if k.startswith('be-')}
check(len(backend)>=50, 'expected at least 50 be-* deployables')
shared_builder=root_compose['services'].get('backend-runtime-build')
if shared_builder:
    check(shared_builder.get('image')=='e-commerce-backend-runtime:local','shared backend runtime image mismatch')
    build=shared_builder.get('build',{})
    check(build.get('context')=='./backend','shared backend runtime must use backend-scoped context')
    check(build.get('dockerfile')=='Dockerfile.runtime','shared backend runtime must use Dockerfile.runtime')
    for name,svc in backend.items():
        check(svc.get('image')=='e-commerce-backend-runtime:local',f'{name} must use shared runtime image')
        check('build' not in svc,f'{name} must not run an independent Maven build in local compose')
        check((svc.get('environment') or {}).get('SERVICE_MODULE')==name,f'{name} SERVICE_MODULE mismatch')
        check((root/f'compose/backend/{name}.yml').exists(),f'missing compose fragment for {name}')
else:
    for name,svc in backend.items():
        check(svc.get('image')==f'e-commerce-{name}:local',f'{name} must have independent image')
        build=svc.get('build',{})
        check(build.get('context')=='./backend',f'{name} must use backend-scoped build context')
        check(build.get('dockerfile')=='Dockerfile.service',f'{name} must use backend/Dockerfile.service')
        check(build.get('args',{}).get('MODULE')==name,f'{name} build MODULE mismatch')
        check((root/f'compose/backend/{name}.yml').exists(),f'missing compose fragment for {name}')

check((root/'frontend/Dockerfile.storefront').exists(),'storefront Dockerfile missing')
check((root/'frontend/Dockerfile.admin').exists(),'admin Dockerfile missing')
check((root/'Dockerfile.backend').exists() and (root/'Dockerfile.frontend').exists(),'aggregate/backward-compatible root Dockerfiles must remain')

labs=root/'database-labs'
check(not any('nifi' in p.read_text(errors='ignore').lower() for p in labs.rglob('*') if p.is_file()),'database labs must not contain NiFi')
pg_cases=list((labs/'postgresql/07-production-cases').glob('*.sql'))
check(len(pg_cases)>=17,f'expected >=17 PostgreSQL production SQL assets, found {len(pg_cases)}')
readme=(labs/'postgresql/07-production-cases/README.md').read_text()
for term in ['actual time','loops','wide rows','SKIP LOCKED','write amplification','connection-pool']:
    check(term.lower() in readme.lower(),f'PostgreSQL production guide missing {term}')
for engine in ['oracle','sqlserver','mysql']:
    check((labs/engine/'07-production-cases/README.md').exists(),f'{engine} production case guide missing')

if errors:
    print('FRONTEND_DOCKER_DATABASE_LABS_V9: FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print(f'FRONTEND_DOCKER_DATABASE_LABS_V9: PASS ({len(backend)} backend services, {len(compose_files)} compose files, {len(pg_cases)} PostgreSQL SQL cases)')
