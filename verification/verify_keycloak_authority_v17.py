from pathlib import Path
import json
import sys

root = Path(__file__).resolve().parents[1]
errors = []

def require(condition, message):
    if not condition:
        errors.append(message)

auth_api = (root / 'backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/api/AuthorizationApi.java').read_text()
for endpoint in [
    '/private/me/profile', '/private/me/permissions', '/private/me/managed-shops', '/private/me/context',
    '/internal/authorization/users/{userId}/profile', '/internal/authorization/users/{userId}/permissions',
    '/internal/authorization/users/{userId}/managed-shops', '/internal/authorization/users/{userId}/context',
    '/private/admin/security/users/{userId}/shop-scopes'
]:
    require(endpoint in auth_api, f'missing authorization endpoint: {endpoint}')

seller_root = root / 'backend/services/be-seller-api/src'
seller_text = '\n'.join(p.read_text(errors='ignore') for p in seller_root.rglob('*') if p.is_file() and p.suffix in {'.java','.yaml','.yml'} )
# Historical Liquibase 002 created seller_staff in older snapshots; 003 must remove it.
drop_migration = seller_root / 'main/resources/db/changelog/003-drop-seller-staff-authorization.sql'
require(drop_migration.exists() and 'DROP TABLE IF EXISTS seller_staff' in drop_migration.read_text(), 'missing seller_staff removal migration')
for forbidden in ['permissions_csv', 'permissionsCsv', 'hasPermission(', 'addStaff(', 'StaffMembership']:
    require(forbidden not in seller_text, f'seller DB/domain still owns authorization: {forbidden}')

realm = json.loads((root / 'infrastructure/keycloak/realm-marketplace.json').read_text())
sellers = next((g for g in realm.get('groups', []) if g.get('name') == 'sellers'), None)
require(sellers is not None, 'missing /sellers Keycloak root')
if sellers:
    seller = next((g for g in sellers.get('subGroups', []) if g.get('name') == 'seller-10001'), None)
    require(seller is not None, 'missing seller-10001 group')
    if seller:
        require(seller.get('attributes', {}).get('type') == ['SELLER'], 'seller group missing type=SELLER')
        shops = next((g for g in seller.get('subGroups', []) if g.get('name') == 'shops'), None)
        require(shops is not None, 'missing shops container under seller-10001')
        if shops:
            leaf = next((g for g in shops.get('subGroups', []) if g.get('name') == 'shop-11001'), None)
            require(leaf is not None, 'missing shop-11001 leaf')
            if leaf:
                attrs = leaf.get('attributes', {})
                require(attrs.get('type') == ['SHOP'], 'shop group missing type=SHOP')
                require(attrs.get('shopId') == ['11001'], 'shop group missing shopId')

compose = (root / 'docker-compose.yml').read_text()
require('DISABLE_INSTALL_DEMO_CONFIG: \'true\'' in compose or 'DISABLE_INSTALL_DEMO_CONFIG: "true"' in compose, 'OpenSearch demo config is not disabled')
require('DISABLE_SECURITY_PLUGIN: \'true\'' in compose or 'DISABLE_SECURITY_PLUGIN: "true"' in compose, 'OpenSearch security plugin is not explicitly disabled')
require('be-authorization-api:' in compose, 'authorization service missing from root compose')

workflow = root / '.github/workflows/ci.yml'
require(workflow.exists(), 'missing GitHub Actions CI workflow')
context = root / 'docs/PROJECT-CONTEXT.md'
require(context.exists(), 'missing persistent project context document')

if errors:
    print('KEYCLOAK_AUTHORITY_V17=FAIL')
    for error in errors:
        print('-', error)
    sys.exit(1)
print('KEYCLOAK_AUTHORITY_V17=PASS')
