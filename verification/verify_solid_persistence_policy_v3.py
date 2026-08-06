from pathlib import Path
import re, sys, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / 'backend'
errors=[]

def require(cond,msg):
    if not cond: errors.append(msg)

# Maven model must be structurally parseable and internal platform versions centrally managed.
for pom in BACKEND.rglob('pom.xml'):
    try:
        ET.parse(pom)
    except Exception as exc:
        errors.append(f'invalid pom XML {pom.relative_to(ROOT)}: {exc}')
root_pom=(BACKEND/'pom.xml').read_text(errors='ignore')
for artifact in ('be-platform-starter','be-platform-cache-starter'):
    require(re.search(rf'<artifactId>{artifact}</artifactId>\s*<version>\$\{{project.version\}}</version>', root_pom, re.S) is not None,
            f'root dependencyManagement must manage {artifact}')
require('spring-boot-starter-aop' not in '\n'.join(p.read_text(errors='ignore') for p in BACKEND.rglob('pom.xml')),
        'obsolete spring-boot-starter-aop remains')

# SOLID/DIP: inbound HTTP adapters must not import concrete application implementations.
for controller in BACKEND.glob('services/*/src/main/java/**/*Controller.java'):
    s=controller.read_text(errors='ignore')
    concrete_imports=[]
    for line in s.splitlines():
        if not line.strip().startswith('import '):
            continue
        if '.application.usecase.' in line:
            concrete_imports.append(line.strip())
        elif re.search(r'\.application\.[A-Z]\w*(UseCase|Orchestrator);', line):
            concrete_imports.append(line.strip())
    require(not concrete_imports,
            f'{controller.relative_to(BACKEND)} imports concrete application implementation: {concrete_imports}')

# User policy: obvious aggregate CRUD must not remain raw JDBC.
for rel in (
    'services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/adapter/out/persistence/JdbcProductReviewPersistenceAdapter.java',
    'services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/adapter/out/persistence/JdbcShopRepositoryAdapter.java',
    'services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/adapter/out/persistence/JdbcMediaMetadataPersistenceAdapter.java',
    'services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/adapter/out/persistence/JdbcAuthorizationChangePersistenceAdapter.java',
    'services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/adapter/out/persistence/JdbcSettlementPersistenceAdapter.java',
):
    require(not (BACKEND/rel).exists(), f'easy aggregate persistence still raw JDBC: {rel}')

# Non-trivial transport/application mapping should be MapStruct-owned.
for rel in (
    'services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/adapter/in/web/mapper/PaymentApiMapper.java',
    'services/be-pricing-api/src/main/java/com/dnnthanh/marketplace/be/pricing/api/adapter/in/web/mapper/PricingApiMapper.java',
    'services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/adapter/in/web/mapper/FulfillmentApiMapper.java',
    'services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/adapter/in/web/mapper/ReturnApiMapper.java',
):
    p=BACKEND/rel
    require(p.exists() and '@Mapper' in p.read_text(errors='ignore'), f'MapStruct API mapper missing: {rel}')

# Utility-class policy: known static holders must be Lombok utility classes, no hand-written ctor.
for p in BACKEND.glob('services/*/src/main/java/**/*.java'):
    s=p.read_text(errors='ignore')
    name=p.stem
    if name.endswith(('Constants','Utils','Util')) and re.search(r'\bstatic\b',s):
        if re.search(r'public\s+(?:final\s+)?class\s+'+re.escape(name)+r'\b',s):
            require('@UtilityClass' in s, f'static utility holder must use @UtilityClass: {p.relative_to(BACKEND)}')

if errors:
    print('SOLID_PERSISTENCE_POLICY_V3=FAIL')
    for e in errors: print(' -', e)
    sys.exit(1)
print('SOLID_PERSISTENCE_POLICY_V3=PASS')
