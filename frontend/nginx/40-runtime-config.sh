#!/usr/bin/env sh
set -eu
cat > /usr/share/nginx/html/assets/runtime-config.js <<CFG
window.__MARKETPLACE_CONFIG__ = { apiBaseUrl: '${API_BASE_URL:-http://localhost:8080}', keycloakUrl: '${KEYCLOAK_PUBLIC_URL:-http://localhost:8180}', keycloakClientId: '${KEYCLOAK_CLIENT_ID:-storefront}' };
CFG
