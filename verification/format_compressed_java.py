from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'backend'


def inject_breaks(source: str) -> str:
    out: list[str] = []
    i = 0
    state = 'normal'
    while i < len(source):
        ch = source[i]
        nxt = source[i + 1] if i + 1 < len(source) else ''
        tri = source[i:i+3]

        if state == 'normal':
            if tri == '"""':
                out.append(tri)
                i += 3
                state = 'textblock'
                continue
            if ch == '/' and nxt == '*':
                out.append('/*')
                i += 2
                state = 'blockcomment'
                continue
            if ch == '/' and nxt == '/':
                out.append('//')
                i += 2
                state = 'linecomment'
                continue
            if ch == '"':
                out.append(ch)
                i += 1
                state = 'string'
                continue
            if ch == "'":
                out.append(ch)
                i += 1
                state = 'char'
                continue
            if ch == ';':
                out.append(';\n')
                i += 1
                continue
            if ch == '{':
                out.append('{\n')
                i += 1
                continue
            if ch == '}':
                out.append('\n}\n')
                i += 1
                continue
            out.append(ch)
            i += 1
            continue

        if state == 'blockcomment':
            out.append(ch)
            if ch == '*' and nxt == '/':
                out.append('/')
                i += 2
                out.append('\n')
                state = 'normal'
            else:
                i += 1
            continue

        if state == 'linecomment':
            out.append(ch)
            i += 1
            if ch == '\n':
                state = 'normal'
            continue

        if state == 'string':
            out.append(ch)
            i += 1
            if ch == '\\' and i < len(source):
                out.append(source[i])
                i += 1
            elif ch == '"':
                state = 'normal'
            continue

        if state == 'char':
            out.append(ch)
            i += 1
            if ch == '\\' and i < len(source):
                out.append(source[i])
                i += 1
            elif ch == "'":
                state = 'normal'
            continue

        if state == 'textblock':
            if tri == '"""':
                out.append(tri)
                i += 3
                state = 'normal'
            else:
                out.append(ch)
                i += 1
            continue

    return ''.join(out)


def indent(text: str) -> str:
    lines = text.splitlines()
    result: list[str] = []
    level = 0
    for raw in lines:
        stripped = raw.strip()
        if not stripped:
            if result and result[-1] != '':
                result.append('')
            continue
        if stripped.startswith('}'):
            level = max(0, level - 1)
        result.append('  ' * level + stripped)
        # Count braces only on this already lexically-separated line. Text blocks in compressed
        # sources are rare; brace changes inside them are not introduced by inject_breaks.
        opens = stripped.count('{')
        closes = stripped.count('}')
        delta = opens - closes
        if stripped.startswith('}'):
            delta += 1  # leading close already applied above
        level = max(0, level + delta)
    while result and result[-1] == '':
        result.pop()
    return '\n'.join(result) + '\n'


changed = []
for path in BASE.rglob('*.java'):
    source = path.read_text(encoding='utf-8')
    if len(source.splitlines()) > 3:
        continue
    formatted = indent(inject_breaks(source))
    if formatted != source:
        path.write_text(formatted, encoding='utf-8')
        changed.append(path.relative_to(ROOT).as_posix())

print(f'Formatted {len(changed)} compressed Java files')
for item in changed:
    print(item)
