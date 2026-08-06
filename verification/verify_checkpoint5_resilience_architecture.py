from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
checks=[]
def contains(rel,*tokens):
    p=root/rel
    if not p.exists():
        checks.append((False,f'missing {rel}')); return
    text=p.read_text(errors='ignore')
    for t in tokens: checks.append((t in text,f'{rel} contains {t}'))

contains('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/config/CheckoutRemoteProperties.java',
         '@ConfigurationProperties','pricingBaseUrl','promotionBaseUrl','inventoryBaseUrl','orderBaseUrl','paymentBaseUrl')
contains('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/pricing/rest/PricingRestAdapter.java',
         'CheckoutRemoteProperties','CheckoutInternalRestExchange')
contains('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/common/rest/CheckoutRemoteCallExecutor.java',
         'CircuitBreaker.decorateSupplier','Bulkhead.decorateSupplier')
contains('backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/config/CheckoutResilienceConfiguration.java',
         'CircuitBreakerRegistry','BulkheadRegistry','CheckoutRemoteResilienceProperties')
pom=(root/'backend/services/be-checkout-api/pom.xml').read_text(errors='ignore')
checks += [('resilience4j-circuitbreaker' in pom,'checkout declares resilience4j circuit breaker core'),('resilience4j-bulkhead' in pom,'checkout declares resilience4j bulkhead core'),('resilience4j-spring-boot3' not in pom,'checkout does not use Boot-3-specific starter')]
contains('backend/services/be-checkout-api/src/test/java/com/dnnthanh/marketplace/be/checkout/api/architecture/CheckoutArchitectureTest.java',
         'ArchTest','application_must_not_depend_on_adapter_out','controllers_must_not_depend_on_repositories')
contains('backend/services/be-checkout-api/src/test/java/com/dnnthanh/marketplace/be/checkout/api/application/CheckoutOrchestratorTest.java',
         'releasesPromotionWhenInventoryReservationFails','doesNotReleaseReservationsAfterOrderExists')

failed=[m for ok,m in checks if not ok]
for ok,m in checks: print(('PASS' if ok else 'FAIL')+': '+m)
print(f'Summary: {len(checks)-len(failed)} passed, {len(failed)} failed')
if failed: sys.exit(1)
