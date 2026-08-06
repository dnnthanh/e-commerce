#!/usr/bin/env python3
"""Verify generated media assets match the deterministic catalog object-key contract."""

from pathlib import Path
from PIL import Image
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
MEDIA = ROOT / "infrastructure" / "media-seed"
MANIFEST = MEDIA / "asset-manifest.txt"
GENERATOR = MEDIA / "generate-assets.sh"
EXPECTED = {
    "home-480.avif": (480, 480, "AVIF"),
    "search-640.avif": (640, 640, "AVIF"),
    "detail-1600.webp": (1600, 1600, "WEBP"),
}

if not MANIFEST.exists() or not GENERATOR.exists():
    raise SystemExit("generated media seed manifest/generator is missing")

paths = [line.strip() for line in MANIFEST.read_text().splitlines() if line.strip()]
if len(paths) != 900 or len(set(paths)) != 900:
    raise SystemExit(f"unexpected media manifest cardinality: {len(paths)}")

with tempfile.TemporaryDirectory(prefix="marketplace-media-seed-") as tmp:
    root = Path(tmp)
    subprocess.run([str(GENERATOR), str(root), str(MANIFEST)], check=True, stdout=subprocess.DEVNULL)
    failures: list[str] = []
    for index in range(1, 101):
        directory = root / "products" / f"{index:04d}"
        for name, (width, height, expected_format) in EXPECTED.items():
            path = directory / name
            if not path.exists():
                failures.append(f"missing generated {path.relative_to(root)}")
                continue
            with Image.open(path) as image:
                if image.size != (width, height):
                    failures.append(f"wrong dimensions {path.relative_to(root)}={image.size}")
                if image.format != expected_format:
                    failures.append(f"wrong format {path.relative_to(root)}={image.format}")
    if failures:
        raise SystemExit("\n".join(failures))

print("MEDIA_SEED_GENERATED_FILES_PASS=900")
