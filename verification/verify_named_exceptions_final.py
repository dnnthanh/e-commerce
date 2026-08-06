from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SERVICE_ROOT = ROOT / 'backend/services'
pattern = re.compile(r'new\s+(IllegalArgumentException|IllegalStateException|RuntimeException)\s*\(')
violations = []
for path in SERVICE_ROOT.rglob('*.java'):
    if '/src/test/' in path.as_posix():
        continue
    text = path.read_text(encoding='utf-8')
    for line_no, line in enumerate(text.splitlines(), start=1):
        if pattern.search(line):
            violations.append(f'{path.relative_to(ROOT)}:{line_no}: {line.strip()}')

if violations:
    print('NAMED_EXCEPTION_GATE=FAIL')
    print('\n'.join(violations))
    raise SystemExit(1)
print('NAMED_EXCEPTION_GATE=PASS')
