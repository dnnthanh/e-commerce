#!/usr/bin/env python3
import subprocess,sys
from pathlib import Path
root=Path(__file__).resolve().parents[1]
result=subprocess.run([sys.executable,str(root/'verification/verify_hexagonal_naming_v6.py')])
if result.returncode:
    print('USECASE_NAMING_FAIL')
    sys.exit(result.returncode)
print('USECASE_NAMING_PASS')
