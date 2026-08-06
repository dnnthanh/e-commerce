"""Aggregate v11 acceptance gate; detailed checks live in focused verifiers."""
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
checks = [
    "verify_compose_runtime_paths_v11.py",
    "verify_frontend_backend_coverage_v11.py",
    "verify_frontend_typescript_syntax_v11.js",
    "verify_database_lab_depth_v11.py",
]
errors = 0
for check in checks:
    path = root / "ci-cdconfigs" / check
    command = ["node", str(path)] if path.suffix == ".js" else [sys.executable, str(path)]
    result = subprocess.run(command, cwd=root, check=False)
    if result.returncode:
        errors += 1
if errors:
    print(f"FRONTEND_DBA_COMPOSE_V11=FAIL failed_checks={errors}")
    sys.exit(1)
print("FRONTEND_DBA_COMPOSE_V11=PASS")
