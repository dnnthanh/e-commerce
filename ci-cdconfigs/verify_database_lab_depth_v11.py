from pathlib import Path
import hashlib,sys
root=Path(__file__).resolve().parents[1]
errors=[];total_cases=0
for engine in ['postgresql','mysql','sqlserver','oracle']:
 d=root/'database-labs'/engine/'08-deep-incidents'
 if not d.exists():errors.append(f'{engine}: missing 08-deep-incidents');continue
 bootstrap=d/'00-bootstrap.sql'
 if not bootstrap.exists() or len(bootstrap.read_text())<500:errors.append(f'{engine}: bootstrap too shallow')
 cases=sorted(p for p in d.iterdir() if p.is_dir());total_cases+=len(cases)
 if len(cases)<8:errors.append(f'{engine}: {len(cases)} deep incidents <8')
 hashes=set()
 for c in cases:
  for fn in ['README.md','baseline.sql','solutions.sql','regression.sql']:
   p=c/fn
   if not p.exists():errors.append(f'{engine}/{c.name}: missing {fn}');continue
   if fn=='README.md':
    txt=p.read_text().lower()
    if len(txt)<2500:errors.append(f'{engine}/{c.name}: README too shallow')
    for sec in ['symptom','data shape','baseline','evidence','root cause','trade-offs','regression','acceptance criteria','production decision']:
     if sec not in txt:errors.append(f'{engine}/{c.name}: missing section {sec}')
   else:
    if len(p.read_text())<250:errors.append(f'{engine}/{c.name}: {fn} too shallow')
  b=c/'baseline.sql'
  if b.exists():hashes.add(hashlib.sha256(b.read_bytes()).hexdigest())
 if len(hashes)!=len(cases):errors.append(f'{engine}: duplicate/generic baseline SQL detected')
# no NiFi at all
for p in (root/'database-labs').rglob('*'):
 if p.is_file() and 'nifi' in p.read_text(errors='ignore').lower():errors.append(f'NiFi reference {p.relative_to(root)}')
if errors:
 print('DATABASE_LAB_DEPTH_V11=FAIL');[print('-',e) for e in errors[:200]];sys.exit(1)
print(f'DATABASE_LAB_DEPTH_V11=PASS deep_cases={total_cases}')
