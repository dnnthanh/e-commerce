from pathlib import Path
import re,sys
root=Path(__file__).resolve().parents[1]
errors=[]
feature_files=list((root/'frontend/projects/storefront/src/app/features').glob('*.ts'))+list((root/'frontend/projects/admin/src/app/features').glob('*.ts'))
chars=sum(len(p.read_text()) for p in feature_files)
if len(feature_files)<25:errors.append(f'feature components {len(feature_files)} <25')
if chars<60000:errors.append(f'feature implementation chars {chars}<60000')
alltext='\n'.join(p.read_text() for p in [*feature_files,root/'frontend/shared/marketplace-api.service.ts'])
if '/internal/' in alltext:errors.append('browser code calls /internal/** contract')
required_paths=[
 '/products','/prices','/search','/private/cart','/private/checkout','/private/orders','/private/returns',
 '/reviews','/comments','/private/notifications','/private/products','/private/media/uploads','/private/inventory/balances',
 '/private/payments','/private/settlements','/private/seller/shops','/private/admin/security/users','/private/audit',
 '/private/operations/incidents','/promotions/evaluate','/private/fulfillment/orders']
for path in required_paths:
 if path not in alltext:errors.append(f'missing API coverage {path}')
required_features=['order-detail.component.ts','community.component.ts','account.component.ts','seller-shop.component.ts','media.component.ts','pricing.component.ts','promotion.component.ts','orders.component.ts','fulfillment.component.ts','returns.component.ts','moderation.component.ts']
for name in required_features:
 if not any(p.name==name for p in feature_files):errors.append(f'missing feature {name}')
# Ensure cart/checkout don't regress to manual item ids as the primary checkout flow.
checkout=(root/'frontend/projects/storefront/src/app/features/checkout.component.ts').read_text()
if 'marketplace.cart()' not in checkout or 'selected()' not in checkout:errors.append('checkout is not cart-driven')
# Enum/status values stay typed strings from backend; no ad-hoc name conversion.
if '.name()' in alltext:errors.append('frontend contains backend-style enum name() conversion')
actions=alltext.count('marketplace.')
if actions<60:errors.append(f'bounded-context actions {actions}<60')
if errors:
 print('FRONTEND_BACKEND_COVERAGE_V11=FAIL');[print('-',e) for e in errors];sys.exit(1)
print(f'FRONTEND_BACKEND_COVERAGE_V11=PASS features={len(feature_files)} chars={chars} actions={actions}')
