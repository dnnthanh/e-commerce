from pathlib import Path
import sys
p=Path('backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/adapter/out/external/keycloak/rest/KeycloakAuthorizationRestAdapter.java')
s=p.read_text()
errors=[]
for token in [
    'ensureSellerTree(',
    'ensureShopGroup(',
    '/admin/realms/{realm}/groups/{parentId}/children',
    'GroupCreateRequest('
]:
    if token not in s: errors.append(f'missing safe Keycloak tree mutation token: {token}')
if 'subGroups' in s:
    errors.append('Keycloak adapter must not persist nested subGroups payloads')
if errors:
    print('KEYCLOAK_GROUP_TREE_MUTATION_V17=FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('KEYCLOAK_GROUP_TREE_MUTATION_V17=PASS')
