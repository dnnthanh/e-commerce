#!/usr/bin/env python3
import json
import sys
from pathlib import Path
import yaml

ROOT = Path(__file__).resolve().parents[1]
errors = []
compose = yaml.safe_load((ROOT / 'docker-compose.yml').read_text())
services = compose.get('services', {})
backend_names = sorted(n for n in services if n.startswith('be-'))

builder = services.get('backend-runtime-build')
if not builder:
    errors.append('missing backend-runtime-build service')
else:
    build = builder.get('build') or {}
    if build.get('dockerfile') != 'Dockerfile.runtime':
        errors.append('backend-runtime-build must use backend/Dockerfile.runtime')
    if builder.get('image') != 'e-commerce-backend-runtime:local':
        errors.append('backend-runtime-build image mismatch')

for name in backend_names:
    svc = services[name]
    if 'build' in svc:
        errors.append(f'{name}: per-service build must be removed from root compose')
    if svc.get('image') != 'e-commerce-backend-runtime:local':
        errors.append(f'{name}: not using shared runtime image')
    env = svc.get('environment') or {}
    if env.get('SERVICE_MODULE') != name:
        errors.append(f'{name}: SERVICE_MODULE missing/mismatch')
    if svc.get('pull_policy') != 'never':
        errors.append(f'{name}: pull_policy must be never for local shared image')

runtime_dockerfile = ROOT / 'backend' / 'Dockerfile.runtime'
if not runtime_dockerfile.exists():
    errors.append('missing backend/Dockerfile.runtime')
else:
    text = runtime_dockerfile.read_text()
    for token in ['mvn -B -ntp', 'services/be-*', '/opt/marketplace/services']:
        if token not in text:
            errors.append(f'Dockerfile.runtime missing {token}')

for name in ['oracle', 'seed-oracle-demo', 'be-settlement-api']:
    if name in services and not {'heavy','full'}.intersection(set(services[name].get('profiles') or [])):
        errors.append(f'{name}: must be heavy/full profiled')

for name in ['lgtm','alloy','cloudbeaver','kafka-ui','opensearch-dashboards','cadvisor','node-exporter','blackbox-exporter']:
    if name in services and not {'observability','full'}.intersection(set(services[name].get('profiles') or [])):
        errors.append(f'{name}: must be observability/full profiled')

pkg = json.loads((ROOT / 'frontend' / 'package.json').read_text())
ts = pkg.get('devDependencies',{}).get('typescript','')
if not ts.startswith('~6.0'):
    errors.append(f'frontend TypeScript must be ~6.0.x, got {ts}')

angular = json.loads((ROOT / 'frontend' / 'angular.json').read_text())
for app in ['storefront','admin']:
    build = angular['projects'][app]['architect']['build']
    configs = build.get('configurations',{})
    if 'production' not in configs:
        errors.append(f'{app}: production configuration missing')
    if build.get('defaultConfiguration') != 'production':
        errors.append(f'{app}: defaultConfiguration must be production')

if len(backend_names) != 51:
    errors.append(f'expected 51 backend services, got {len(backend_names)}')

# The optimization must materially reduce default startup fan-out.
default_active = [n for n, svc in services.items() if not (svc.get('profiles') or [])]
default_backend = [n for n in default_active if n.startswith('be-')]
if len(default_active) > 40:
    errors.append(f'default topology too large: {len(default_active)} services (expected <= 40)')
if len(default_backend) > 20:
    errors.append(f'default backend topology too large: {len(default_backend)} services (expected <= 20)')

# Runtime Dockerfile must package every Maven be-* deployable, including modules not currently composed.
module_poms = sorted((ROOT / 'backend' / 'services').glob('be-*/pom.xml'))
if len(module_poms) < len(backend_names):
    errors.append(f'backend runtime module discovery too small: {len(module_poms)} modules')
if runtime_dockerfile.exists():
    rt = runtime_dockerfile.read_text()
    for token in ['MAVEN_OPTS=', '-Dmaven.test.skip=true', 'Missing executable JAR for $module']:
        if token not in rt:
            errors.append(f'Dockerfile.runtime missing {token}')

# Profile dependency graph must be valid for common invocation modes.
for profile in [None, 'full', 'heavy', 'observability', 'seed-large']:
    active = {n for n, svc in services.items() if not (svc.get('profiles') or []) or (profile and profile in (svc.get('profiles') or []))}
    for name in sorted(active):
        deps = services[name].get('depends_on') or {}
        if isinstance(deps, list):
            deps = {d: {} for d in deps}
        for dep in deps:
            if dep not in active:
                errors.append(f'profile={profile or "default"}: {name} depends on disabled {dep}')

# Container-shell MSSQL variables must be escaped from Compose host interpolation.
compose_text = (ROOT / 'docker-compose.yml').read_text()
if '"$MSSQL_SA_PASSWORD"' in compose_text.replace('"$$MSSQL_SA_PASSWORD"', ''):
    errors.append('root compose contains unescaped $MSSQL_SA_PASSWORD')

# Local runtime budgets are deliberate safeguards against Docker Desktop OOM.
for name in backend_names:
    opts = (services[name].get('environment') or {}).get('JAVA_OPTS','')
    if '-Xmx' not in opts:
        errors.append(f'{name}: JAVA_OPTS must bound heap')
for name, token in [('kafka','KAFKA_HEAP_OPTS'),('opensearch','OPENSEARCH_JAVA_OPTS'),('keycloak','JAVA_OPTS_APPEND')]:
    if token not in (services.get(name,{}).get('environment') or {}):
        errors.append(f'{name}: missing local memory budget {token}')
if 'MSSQL_MEMORY_LIMIT_MB' not in (services.get('sqlserver',{}).get('environment') or {}):
    errors.append('sqlserver: missing MSSQL_MEMORY_LIMIT_MB')

for rel in ['frontend/Dockerfile.admin','frontend/Dockerfile.storefront']:
    text = (ROOT / rel).read_text()
    if '--mount=type=cache,id=marketplace-npm-cache' not in text:
        errors.append(f'{rel}: missing shared npm cache')

if errors:
    print('LOCAL_COMPOSE_OPTIMIZATION_V16=FAIL')
    for e in errors:
        print(' -', e)
    sys.exit(1)
print('LOCAL_COMPOSE_OPTIMIZATION_V16=PASS')
print(f'backend_services={len(backend_names)}')
