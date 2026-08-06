from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
checks=[]
def require(rel,*tokens):
 p=root/rel
 if not p.exists():
  checks.append((False,f'missing {rel}')); return
 txt=p.read_text(errors='ignore')
 for token in tokens: checks.append((token in txt,f'{rel} contains {token}'))
require('backend/services/be-payment-outbox/src/main/java/com/dnnthanh/marketplace/be/payment/outbox/publisher/PaymentOutboxPublisher.java',
        'FOR UPDATE SKIP LOCKED','PUBLISHING','locked_until','attempt_count','OutboxRetryPolicy')
require('backend/services/be-order-outbox/src/main/java/com/dnnthanh/marketplace/be/order/outbox/publisher/OrderOutboxPublisher.java',
        'UPDLOCK','READPAST','PUBLISHING','locked_until','attempt_count','OutboxRetryPolicy')
require('backend/services/be-payment-api/src/main/resources/db/changelog/002-outbox-delivery.sql',
        'attempt_count','locked_until','next_attempt_at','last_error')
require('backend/services/be-order-api/src/main/resources/db/changelog/002-outbox-delivery.sql',
        'attempt_count','locked_until','next_attempt_at','last_error')
require('backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/outbox/OutboxRetryPolicy.java',
        'nextDelay','terminal')
failed=[m for ok,m in checks if not ok]
for ok,m in checks: print(('PASS' if ok else 'FAIL')+': '+m)
print(f'Summary: {len(checks)-len(failed)} passed, {len(failed)} failed')
if failed: sys.exit(1)
