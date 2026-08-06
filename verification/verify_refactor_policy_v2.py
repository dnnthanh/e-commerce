from pathlib import Path
import re, sys, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / 'backend'
errors=[]

def require(cond,msg):
    if not cond: errors.append(msg)

pom=(BACKEND/'pom.xml').read_text()
require('<artifactId>spring-boot-starter-parent</artifactId>' in pom,
        'backend parent must inherit spring-boot-starter-parent')
for artifact in ('be-platform-starter','be-platform-cache-starter'):
    pattern=rf'<artifactId>{artifact}</artifactId>\s*<version>\$\{{project.version\}}</version>'
    require(re.search(pattern,pom,re.S) is not None,
            f'dependencyManagement must manage {artifact} at project.version')

all_poms='\n'.join(p.read_text(errors='ignore') for p in BACKEND.rglob('pom.xml'))
require('resilience4j-spring-boot3' not in all_poms,
        'Boot 4 baseline must not use resilience4j-spring-boot3')

spec=(ROOT/'.agent/specs/013-codebase-deep-refactor.md').read_text()
for phrase in (
    'relational search/filter/pagination must use native SQL',
    'simple CRUD must use Spring Data JPA',
    'JDBC is reserved for complex',
    'MapStruct is mandatory for non-trivial mapping',
    'Lombok `@UtilityClass`',
    'input-port interface',
):
    require(phrase.lower() in spec.lower(), f'spec missing policy: {phrase}')

# Search policy: the two relational business search paths must not use Specification.
for rel in (
    'services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/ProductPersistenceAdapter.java',
    'services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/adapter/out/persistence/OrderPersistenceAdapter.java',
):
    s=(BACKEND/rel).read_text()
    require('Specification<' not in s and 'JpaSpecificationExecutor' not in s,
            f'{rel} must use native SQL for search, not Specification')

# User-called Cart application boundary must implement an input port and controller must depend on it.
cart=(BACKEND/'services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/application/service/CartServiceImplement.java').read_text()
controller=(BACKEND/'services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/in/web/CartController.java').read_text()
require(re.search(r'class\s+CartServiceImplement\s+implements\s+CartUseCase',cart) is not None,
        'CartServiceImplement must implement CartUseCase input port')
require('private final CartUseCase carts;' in controller,
        'CartController must depend on CartUseCase interface')
require('private final CartCheckoutValidationQuery validation;' in controller,
        'CartController must depend on CartCheckoutValidationQuery interface')

# True constant/static helpers should use Lombok UtilityClass consistently.
for rel in (
    'services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/config/CheckoutRemoteResilienceNames.java',
    'services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/domain/constant/ReturnConstants.java',
    'services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/domain/constant/OrderConstants.java',
    'services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/constant/CommentConstants.java',
    'services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/application/query/SearchCursor.java',
):
    s=(BACKEND/rel).read_text()
    require('@UtilityClass' in s, f'{rel} must use Lombok @UtilityClass')

# These adapters are ordinary aggregate persistence and must no longer be raw JDBC.
for rel in (
    'services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/persistence/JdbcCartRepositoryAdapter.java',
    'services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/adapter/out/persistence/JdbcMediaAssetPersistenceAdapter.java',
    'services/be-pricing-api/src/main/java/com/dnnthanh/marketplace/be/pricing/api/adapter/out/persistence/JdbcPriceRuleRepositoryAdapter.java',
):
    require(not (BACKEND/rel).exists(), f'basic JDBC adapter still exists: {rel}')

# MapStruct must cover the newly-refactored boundaries.
for rel in (
    'services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/in/web/mapper/CartApiMapper.java',
    'services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/adapter/out/persistence/mapper/MediaAssetPersistenceMapper.java',
    'services/be-pricing-api/src/main/java/com/dnnthanh/marketplace/be/pricing/api/adapter/out/persistence/mapper/PriceRulePersistenceMapper.java',
):
    p=BACKEND/rel
    require(p.exists() and '@Mapper' in p.read_text(errors='ignore'), f'MapStruct mapper missing: {rel}')

if errors:
    print('REFACTOR_POLICY_V2=FAIL')
    for e in errors: print(' -',e)
    sys.exit(1)
print('REFACTOR_POLICY_V2=PASS')
