#!/usr/bin/env python3
from pathlib import Path
import re, sys
ROOT=Path(__file__).resolve().parents[1]
SERVICES=ROOT/'backend/services'
violations=[]

def interface_name(p):
    m=re.search(r'public\s+interface\s+(\w+)',p.read_text(errors='ignore'))
    return m.group(1) if m else None

# Input ports are interfaces; exactly one concrete implementation must be XServiceImplement.
for p in SERVICES.rglob('application/port/in/*.java'):
    contract=interface_name(p)
    if not contract:
        violations.append(f'input port must be interface: {p.relative_to(ROOT)}'); continue
    stem=contract
    for suffix in ('UseCase','Query','Command'):
        if stem.endswith(suffix):
            stem=stem[:-len(suffix)]
            break
    expected=stem+'ServiceImplement'
    pattern=re.compile(r'implements\s+[^\{]*\b'+re.escape(contract)+r'\b',re.S)
    impl=[]
    for q in SERVICES.rglob('*.java'):
        if q==p: continue
        t=q.read_text(errors='ignore')
        if pattern.search(t):
            m=re.search(r'public\s+(?:final\s+)?class\s+(\w+)',t)
            if m: impl.append((m.group(1),q))
    if len(impl)!=1:
        violations.append(f'{contract}: expected one implementation, found {[(n,str(q.relative_to(ROOT))) for n,q in impl]}')
    elif impl[0][0]!=expected:
        violations.append(f'{contract}: expected {expected}, found {impl[0][0]} ({impl[0][1].relative_to(ROOT)})')
    elif '/application/service/' not in str(impl[0][1]):
        violations.append(f'{contract}: implementation must live in application/service: {impl[0][1].relative_to(ROOT)}')

# A concrete class must never use the UseCase suffix.
for q in SERVICES.rglob('*.java'):
    t=q.read_text(errors='ignore')
    if re.search(r'public\s+(?:final\s+)?class\s+\w+UseCase\b',t):
        violations.append(f'concrete class may not end UseCase: {q.relative_to(ROOT)}')

# Output ports are interfaces under application/port/out, concrete implementors are Adapters.
for p in SERVICES.rglob('application/port/out/*.java'):
    contract=interface_name(p)
    if not contract:
        violations.append(f'output port must be interface: {p.relative_to(ROOT)}'); continue
    if not contract.endswith('Port'):
        violations.append(f'output port must end Port: {contract} ({p.relative_to(ROOT)})')
        continue
    pattern=re.compile(r'implements\s+[^\{]*\b'+re.escape(contract)+r'\b',re.S)
    for q in SERVICES.rglob('*.java'):
        if q==p: continue
        t=q.read_text(errors='ignore')
        if pattern.search(t):
            m=re.search(r'(?:public\s+)?(?:final\s+)?class\s+(\w+)',t)
            if m and not m.group(1).endswith('Adapter'):
                violations.append(f'{contract}: implementation must end Adapter, found {m.group(1)} ({q.relative_to(ROOT)})')

# No legacy flat/domain port packages.
for p in SERVICES.rglob('*.java'):
    s=str(p)
    if '/domain/port/' in s:
        violations.append(f'domain/port is legacy; move to application/port/out: {p.relative_to(ROOT)}')
    if '/application/port/' in s and '/application/port/in/' not in s and '/application/port/out/' not in s:
        violations.append(f'flat application/port is legacy: {p.relative_to(ROOT)}')

if violations:
    print('HEXAGONAL_NAMING_V6_FAIL')
    print('\n'.join('- '+v for v in violations))
    sys.exit(1)
print('HEXAGONAL_NAMING_V6_PASS')
