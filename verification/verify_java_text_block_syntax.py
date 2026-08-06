#!/usr/bin/env python3
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1] / "backend"
pattern = re.compile(r'"""[^\r\n]*"""')
violations = []
for path in root.rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    for match in pattern.finditer(text):
        line = text.count("\n", 0, match.start()) + 1
        violations.append((path.relative_to(root.parent), line, match.group(0)))

if violations:
    print("JAVA_TEXT_BLOCK_SYNTAX=FAIL")
    print(f"single_line_text_blocks={len(violations)}")
    for path, line, value in violations:
        print(f"{path}:{line}: {value}")
    sys.exit(1)

print("JAVA_TEXT_BLOCK_SYNTAX=PASS")
print("single_line_text_blocks=0")
