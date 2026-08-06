from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
base=ROOT/'backend/services/be-catalog-api'
checks=[]
def expect(name, ok):
    checks.append((name,ok)); print(('PASS' if ok else 'FAIL')+': '+name)

pom=(base/'pom.xml').read_text()
expect('catalog declares spring data jpa', 'spring-boot-starter-data-jpa' in pom)
expect('legacy jdbc product repository removed', not (base/'src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/JdbcProductRepositoryAdapter.java').exists())
entity=base/'src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/entity/ProductJpaEntity.java'
repo=base/'src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/repository/ProductJpaRepository.java'
adapter=base/'src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/ProductPersistenceAdapter.java'
expect('ProductJpaEntity exists', entity.exists())
expect('Product JpaRepository exists', repo.exists())
expect('Product persistence adapter exists', adapter.exists())
if entity.exists(): expect('product entity optimistic version', '@Version' in entity.read_text())
if repo.exists():
    repo_text=repo.read_text(); expect('product search uses native SQL', '@Query' in repo_text and 'nativeQuery = true' in repo_text and 'JpaSpecificationExecutor' not in repo_text)
if adapter.exists():
    text=adapter.read_text(); expect('product CRUD uses JpaRepository', 'ProductJpaRepository' in text)
    expect('JDBC retained only for outbox/native support', 'JdbcClient' in text and 'appendChangedEvent' in text)
failed=sum(not x for _,x in checks)
print(f'Summary: {len(checks)-failed} passed, {failed} failed')
raise SystemExit(1 if failed else 0)
