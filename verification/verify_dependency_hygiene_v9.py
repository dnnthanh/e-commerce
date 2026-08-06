#!/usr/bin/env python3
from pathlib import Path
import re, sys
ROOT=Path(__file__).resolve().parents[1]
BACKEND=ROOT/'backend'
errors=[]
parent=(BACKEND/'pom.xml').read_text(errors='ignore')
if not re.search(r'<artifactId>lombok</artifactId>.*?<scope>provided</scope>', parent, re.S):
    errors.append('PARENT_LOMBOK_PROVIDED_MISSING')
for pom in BACKEND.rglob('pom.xml'):
    text=pom.read_text(errors='ignore')
    for m in re.finditer(r'<dependency>\s*<groupId>org\.springframework\.boot</groupId>\s*<artifactId>spring-boot-starter-test</artifactId>(.*?)</dependency>',text,re.S):
        if '<scope>test</scope>' not in m.group(1):
            errors.append(f'TEST_STARTER_SCOPE {pom.relative_to(ROOT)}')
    if pom != BACKEND/'pom.xml' and '<artifactId>lombok</artifactId>' in text:
        errors.append(f'REDUNDANT_CHILD_LOMBOK {pom.relative_to(ROOT)}')
    if pom.parent==BACKEND:
        continue
    all_java='\n'.join(f.read_text(errors='ignore') for f in pom.parent.rglob('*.java'))
    name=pom.parent.name
    if any(name.endswith(s) for s in ('-worker','-job','-outbox')):
        inbound=any(x in all_java for x in ('@RestController','@RequestMapping','@GetMapping','@PostMapping','@PutMapping','@PatchMapping','@DeleteMapping'))
        if not inbound and ('spring-boot-starter-web</artifactId>' in text or 'spring-boot-starter-webflux</artifactId>' in text):
            errors.append(f'UNNECESSARY_SERVER_WEB {pom.parent.relative_to(ROOT)}')
for f in BACKEND.rglob('*.java'):
    text=f.read_text(errors='ignore')
    body=re.sub(r'^import\s+[^;]+;\s*','',text,flags=re.M)
    for imp in re.findall(r'^import\s+(?!static)([\w.]+);',text,re.M):
        simple=imp.rsplit('.',1)[-1]
        if simple!='*' and not re.search(r'\b'+re.escape(simple)+r'\b',body):
            errors.append(f'UNUSED_IMPORT {f.relative_to(ROOT)} {imp}')
if errors:
    print(f'DEPENDENCY_HYGIENE_V9=FAIL count={len(errors)}')
    for e in errors: print(e)
    sys.exit(1)
print('DEPENDENCY_HYGIENE_V9=PASS')
