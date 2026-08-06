#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0
pass(){ echo "PASS: $1"; }
failcheck(){ echo "FAIL: $1"; fail=$((fail+1)); }
if python3 "$ROOT/verification/verify_hexagonal_naming_v6.py"; then pass "hexagonal naming"; else failcheck "hexagonal naming"; fi
if grep -RIE 'https?://be-[A-Za-z0-9_-]+' "$ROOT/backend" --include='*.java' | grep -q .; then failcheck "no hard-coded internal service URL in Java"; else pass "no hard-coded internal service URL in Java"; fi
if grep -RIE '^[[:space:]]*transport:' "$ROOT/backend" "$ROOT/ci-cdconfigs" --include='*.yml' --include='*.yaml' 2>/dev/null | grep -q .; then failcheck "no runtime transport switch"; else pass "no runtime transport switch"; fi
if grep -RIE '\$\{[A-Z0-9_]*(PASSWORD|SECRET|TOKEN|ACCESS_KEY|PRIVATE_KEY)[A-Z0-9_]*:[^}]+' "$ROOT/backend/services" --include='application*.yml' --include='application*.yaml' | grep -q .; then failcheck "no committed secret defaults"; else pass "no committed secret defaults"; fi
if grep -RIE '\$\{[^}]*(PASSWORD|SECRET|TOKEN|ACCESS_KEY|PRIVATE_KEY)[^}]*:[^}]+' "$ROOT/backend" --include='*.java' | grep -q .; then failcheck "no secret defaults in Java configuration"; else pass "no secret defaults in Java configuration"; fi
if grep -RIE 'https?://be-[A-Za-z0-9_-]+' "$ROOT/backend/services" --include='application*.yml' --include='application*.yaml' | grep -q .; then failcheck "no environment-specific internal URL in application.yml"; else pass "no environment-specific internal URL in application.yml"; fi
missing=0
for d in "$ROOT"/backend/services/be-*; do n="$(basename "$d")"; test -d "$ROOT/ci-cdconfigs/$n" || { echo "MISSING: $n"; missing=1; }; done
if [ "$missing" -eq 0 ]; then pass "one ci-cdconfigs folder per be deployable"; else failcheck "one ci-cdconfigs folder per be deployable"; fi
count_services=$(find "$ROOT/backend/services" -mindepth 1 -maxdepth 1 -type d -name 'be-*' | wc -l | tr -d ' ')
count_configs=$(find "$ROOT/ci-cdconfigs" -mindepth 1 -maxdepth 1 -type d -name 'be-*' | wc -l | tr -d ' ')
if [ "$count_services" = "$count_configs" ]; then pass "ci config count $count_configs/$count_services"; else failcheck "ci config count $count_configs/$count_services"; fi
echo "FAILURES=$fail"
exit "$fail"
