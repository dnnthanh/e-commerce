#!/usr/bin/env bash
set -euo pipefail

KC="${KEYCLOAK_INTERNAL_URL:-http://keycloak:8080}"
REALM="${KEYCLOAK_REALM:-marketplace}"
SERVICE_CLIENT_ID="${SERVICE_CLIENT_ID:-marketplace-internal}"
AUTH_ADMIN_CLIENT_ID="${AUTH_ADMIN_CLIENT_ID:-authorization-admin}"

/opt/keycloak/bin/kcadm.sh config credentials \
  --server "$KC" \
  --realm master \
  --user "${KEYCLOAK_ADMIN:-admin}" \
  --password "${KEYCLOAK_ADMIN_PASSWORD:-admin}"

ensure_service_account_enabled() {
  local client_id="$1"
  local client_uuid
  client_uuid=$(/opt/keycloak/bin/kcadm.sh get clients -r "$REALM" -q clientId="$client_id" --fields id --format csv --noquotes | head -1)
  if [ -n "$client_uuid" ]; then
    /opt/keycloak/bin/kcadm.sh update "clients/$client_uuid" -r "$REALM" -s serviceAccountsEnabled=true >/dev/null
  fi
}

ensure_realm_role() {
  local role_name="$1"
  local description="$2"
  if ! /opt/keycloak/bin/kcadm.sh get "roles/$role_name" -r "$REALM" >/dev/null 2>&1; then
    /opt/keycloak/bin/kcadm.sh create roles -r "$REALM" -s name="$role_name" -s description="$description" >/dev/null
  fi
}

ensure_service_account_enabled "$SERVICE_CLIENT_ID"
ensure_service_account_enabled "$AUTH_ADMIN_CLIENT_ID"

# Feature 015 exposes the existing Comment moderation use case to browser operators. Keep the
# permission seed idempotent for existing local volumes as well as fresh realm imports.
ensure_realm_role COMMENT_MODERATE "Hide or restore marketplace comments during moderation"
/opt/keycloak/bin/kcadm.sh add-roles -r "$REALM" \
  --rname PLATFORM_ADMIN \
  --rolename COMMENT_MODERATE >/dev/null 2>&1 || true

# Internal service calls authenticate with a technical identity, never a human admin role.
/opt/keycloak/bin/kcadm.sh add-roles -r "$REALM" \
  --uusername "service-account-$SERVICE_CLIENT_ID" \
  --rolename SERVICE_ACCOUNT >/dev/null 2>&1 || true

# authorization-admin is the only service account allowed to query/mutate Keycloak authorization state.
for role in manage-users view-users query-users query-groups view-realm; do
  /opt/keycloak/bin/kcadm.sh add-roles -r "$REALM" \
    --uusername "service-account-$AUTH_ADMIN_CLIENT_ID" \
    --cclientid realm-management \
    --rolename "$role" >/dev/null 2>&1 || true
done
