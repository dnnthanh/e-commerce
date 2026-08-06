from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

java_files = list((ROOT / "backend").rglob("*.java"))
for path in java_files:
    text = path.read_text(encoding="utf-8")
    rel = path.relative_to(ROOT)
    if "StringRedisTemplate" in text:
        errors.append(f"{rel}: StringRedisTemplate is forbidden for object cache data")
    if re.search(r"RedisTemplate\s*<\s*String\s*,\s*String\s*>", text):
        errors.append(f"{rel}: RedisTemplate<String,String> is forbidden for object cache data")
    is_business_cache_adapter = "/adapter/out/cache/" in str(rel).replace("\\", "/")
    if is_business_cache_adapter and re.search(r"ObjectMapper\s+\w+|\.readValue\(|\.writeValueAsString\(", text):
        errors.append(f"{rel}: cache adapter must not manually convert String/JSON to objects")

starter = ROOT / "backend/platform/be-platform-cache-starter"
if not starter.exists():
    errors.append("backend/platform/be-platform-cache-starter: missing dedicated cache starter")
else:
    starter_text = "\n".join(p.read_text(encoding="utf-8") for p in starter.rglob("*.java"))
    for token in ["@EnableCaching", "RedisCacheManager", "JacksonJsonRedisSerializer", "transactionAware", "allowCreateOnMissingCache(false)", "enableStatistics()"]:
        if token not in starter_text:
            errors.append(f"cache starter missing {token}")

root_pom = (ROOT / "backend/pom.xml").read_text(encoding="utf-8")
if "platform/be-platform-cache-starter" not in root_pom:
    errors.append("backend/pom.xml: cache starter module is not registered")

cart_adapter = ROOT / "backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/cache/RedisCartCacheAdapter.java"
if cart_adapter.exists():
    cart_text = cart_adapter.read_text(encoding="utf-8")
    for token in ["CacheManager", "CartCacheDocument"]:
        if token not in cart_text:
            errors.append(f"RedisCartCacheAdapter must use {token}")

for module in ["be-cart-api", "be-authorization-api"]:
    pom = ROOT / f"backend/services/{module}/pom.xml"
    if pom.exists() and "be-platform-cache-starter" not in pom.read_text(encoding="utf-8"):
        errors.append(f"{module}: must depend on be-platform-cache-starter")

auth_service = ROOT / "backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/application/AuthorizationUseCase.java"
if auth_service.exists():
    text = auth_service.read_text(encoding="utf-8")
    if "@Cacheable" not in text:
        errors.append("AuthorizationUseCase.snapshot must be @Cacheable")
    if text.count("@CacheEvict") < 4:
        errors.append("Authorization mutations must evict authorization snapshot cache")

if errors:
    print("TYPED_REDIS_CACHE_FAIL")
    for error in errors:
        print(f"- {error}")
    sys.exit(1)

print("TYPED_REDIS_CACHE_PASS")
