from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
violations=[]
patterns=(re.compile(r'\bJdbcClient\b'),re.compile(r'\bMongoTemplate\b'),re.compile(r'\bJpaRepository\b'))
for path in (ROOT/'backend/services').glob('*-api/src/main/java/**/*Controller.java'):
    text=path.read_text(encoding='utf-8')
    if any(p.search(text) for p in patterns):
        violations.append(str(path.relative_to(ROOT)))
if violations:
    print('CONTROLLER_PERSISTENCE_BOUNDARY=FAIL')
    print('\n'.join(violations))
    raise SystemExit(1)
print('CONTROLLER_PERSISTENCE_BOUNDARY=PASS')
