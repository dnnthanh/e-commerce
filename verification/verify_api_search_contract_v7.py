from pathlib import Path
import re, sys

ROOT=Path(__file__).resolve().parents[1]
SERVICES=ROOT/'backend/services'
errors=[]
passes=[]

def ok(cond,msg):
    (passes if cond else errors).append(msg)

# 1. API interfaces own signatures only, no nested record DTOs.
nested=[]
for p in SERVICES.rglob('*Api.java'):
    t=p.read_text(errors='ignore')
    if re.search(r'(?m)^\s+record\s+\w+\s*\(',t): nested.append(str(p.relative_to(ROOT)))
ok(not nested, 'API interfaces contain no nested record DTOs')
if nested: errors.extend('nested DTO: '+x for x in nested)

# 2. The main filter/list endpoints that previously exposed loose params are grouped.
expected={
 'be-audit-api':'AuditSearchRequest',
 'be-search-api':'ProductSearchRequest',
 'be-inventory-api':'InventoryBalanceSearchRequest',
 'be-pricing-api':'PriceQueryRequest',
 'be-review-api':'ReviewSearchRequest',
 'be-settlement-api':'SettlementSearchRequest',
 'be-payment-api':'PaymentSearchRequest',
 'be-notification-api':'NotificationSearchRequest',
 'be-comment-api':'CommentSearchRequest',
 'be-seller-api':'SellerShopSearchRequest',
}
for service,request in expected.items():
    hits=list((SERVICES/service/'src/main/java').rglob(request+'.java'))
    ok(bool(hits),f'{service} owns grouped {request}')

# 3. Relational search repositories bind grouped criteria with SpEL.
repo_expect={
 'be-audit-api':'AuditSearchJpaRepository.java',
 'be-catalog-api':'ProductJpaRepository.java',
 'be-inventory-api':'InventoryQueryJpaRepository.java',
 'be-order-api':'OrderJpaRepository.java',
 'be-payment-api':'PaymentQueryJpaRepository.java',
 'be-pricing-api':'PriceRuleJpaRepository.java',
 'be-review-api':'ProductReviewJpaRepository.java',
 'be-settlement-api':'SettlementJpaRepository.java',
 'be-seller-api':'ShopJpaRepository.java',
}
for service,name in repo_expect.items():
    hits=list((SERVICES/service/'src/main/java').rglob(name))
    ok(bool(hits),f'{service} has {name}')
    if hits:
        t=hits[0].read_text(errors='ignore')
        ok('nativeQuery = true' in t,f'{name} uses native SQL')
        # relevant search repos must include grouped criteria SpEL somewhere.
        ok(':#{#criteria.' in t or ':#{#criteria.' in t.replace('()',''),f'{name} binds grouped criteria using SpEL')

# 4. No relational business search reintroduced through Specification/Criteria API.
spec=[]
for p in SERVICES.rglob('*.java'):
    t=p.read_text(errors='ignore')
    if 'JpaSpecificationExecutor' in t or 'org.springframework.data.jpa.domain.Specification' in t:
        spec.append(str(p.relative_to(ROOT)))
ok(not spec,'no JPA Specification business search')
if spec: errors.extend('specification search: '+x for x in spec)

# 5. Testcontainers model uses a real 2.x JUnit artifact and is not inherited as a parent test dependency.
pom=(ROOT/'backend/pom.xml').read_text()
ok('<testcontainers.version>2.0.5</testcontainers.version>' in pom,'Testcontainers pinned to 2.0.5')
ok('<artifactId>testcontainers-junit-jupiter</artifactId>' in pom,'Testcontainers JUnit 2.x artifact is testcontainers-junit-jupiter')
parent_deps=pom.split('<dependencyManagement>',1)[0]
ok('testcontainers-junit-jupiter' not in parent_deps and '<artifactId>testcontainers</artifactId>' not in parent_deps,
   'Testcontainers is not inherited by every reactor module')

# 6. Project-local imports must resolve to a top-level project type or a nested type of one.
java=list((ROOT/'backend').rglob('*.java'))
classes={}
for p in java:
    t=p.read_text(errors='ignore')
    m=re.search(r'package\s+([\w.]+);',t)
    if m: classes[m.group(1)+'.'+p.stem]=p
missing=[]
for p in java:
    t=p.read_text(errors='ignore')
    for imp in re.findall(r'import\s+(com\.dnnthanh\.marketplace\.[\w.]+);',t):
        if imp in classes: continue
        parts=imp.split('.')
        if not any('.'.join(parts[:i]) in classes for i in range(len(parts)-1,3,-1)):
            missing.append(f'{p.relative_to(ROOT)} -> {imp}')
ok(not missing,'all project-local Java imports resolve')
if missing: errors.extend('missing import: '+x for x in missing[:50])

for m in passes: print('PASS:',m)
for m in errors: print('FAIL:',m)
print(f'API_SEARCH_CONTRACT_V7: {len(passes)} passed, {len(errors)} failed')
sys.exit(1 if errors else 0)
