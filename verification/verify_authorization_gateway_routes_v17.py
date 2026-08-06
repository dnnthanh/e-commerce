from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
yml = (root / 'backend/services/be-gateway/src/main/resources/application.yml').read_text()
errors=[]
for route in ['/private/me/**','/private/admin/security/**','/internal/authorization/**']:
    if route not in yml:
        errors.append(f'missing gateway authorization route {route}')
if errors:
    print('AUTHORIZATION_GATEWAY_ROUTES_V17=FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('AUTHORIZATION_GATEWAY_ROUTES_V17=PASS')
