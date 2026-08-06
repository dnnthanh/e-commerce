#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel):
    p=ROOT/rel
    return p.read_text(errors='ignore') if p.exists() else ''
def exists(rel): return (ROOT/rel).exists()
def check(name, ok): checks.append((name,bool(ok)))

notif_port=read('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/application/port/out/NotificationInboxPort.java')
notif_adapter=read('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/adapter/out/mongo/MongoNotificationPersistenceAdapter.java')
notif_uc=read('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/application/service/NotificationDeliveryServiceImplement.java')
check('notification port has atomic claimDue', 'claimDue(' in notif_port)
check('notification mongo uses findAndModify for claim', 'findAndModify' in notif_adapter and 'SENDING' in notif_adapter)
check('notification delivery consumes claimed work', 'claimDue(' in notif_uc and 'findDue(' not in notif_uc)
check('notification delivery scheduler exists', exists('backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/adapter/in/scheduler/NotificationDeliveryScheduler.java'))

cart=read('backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/adapter/out/persistence/CartPersistenceAdapter.java')
check('cart concurrent create handles unique winner', 'DataIntegrityViolationException' in cart and 'findByCartId(cart.cartId())' in cart and '@Transactional\n  public ShoppingCart create' not in cart)

ret=read('backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/adapter/out/persistence/ReturnPersistenceAdapter.java')
check('return disposition events only emitted on inspection transition', 'before != ReturnStatus.INSPECTED' in ret and 'request.status() == ReturnStatus.INSPECTED' in ret)

settlement=read('backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/adapter/out/persistence/repository/SettlementJpaRepository.java')
join_segment=settlement[settlement.find('LEFT JOIN seller_settlement_ledger_entry'):settlement.find('GROUP BY s.settlement_no', settlement.find('LEFT JOIN seller_settlement_ledger_entry'))]
normalized=' '.join(join_segment.split())
check('settlement period filter remains in LEFT JOIN ON', 'ON l.seller_id = s.seller_id' in normalized and 'AND l.created_at' in normalized.split('WHERE',1)[0])

inv_worker=read('backend/services/be-inventory-worker/src/main/java/com/dnnthanh/marketplace/be/inventory/worker/ReturnRestockConsumer.java')
audit_worker=read('backend/services/be-audit-worker/src/main/java/com/dnnthanh/marketplace/be/audit/worker/AuditEventConsumer.java')
auth_client=read('backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/adapter/out/external/keycloak/rest/KeycloakAuthorizationRestAdapter.java')
check('inventory worker uses named event exception', 'IllegalArgumentException' not in inv_worker and 'InvalidReturnRestockEventException' in inv_worker)
check('audit worker uses named event exception', 'IllegalArgumentException' not in audit_worker and 'InvalidAuditEventException' in audit_worker)
check('keycloak adapter uses named provider exception', 'IllegalArgumentException' not in auth_client and 'AuthorizationProviderException' in auth_client)

failed=[name for name,ok in checks if not ok]
for name,ok in checks: print(('PASS' if ok else 'FAIL')+': '+name)
print(f'Summary: {len(checks)-len(failed)} passed, {len(failed)} failed')
sys.exit(1 if failed else 0)
