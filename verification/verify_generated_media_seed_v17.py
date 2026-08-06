from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
base=root/'infrastructure/media-seed'
errors=[]
manifest=base/'asset-manifest.txt'
generator=base/'generate-assets.sh'
dockerfile=(base/'Dockerfile').read_text()
if not manifest.exists(): errors.append('missing media asset manifest')
if not generator.exists(): errors.append('missing media asset generator')
if 'asset-manifest.txt' not in dockerfile or 'generate-assets.sh' not in dockerfile:
    errors.append('Dockerfile does not materialize generated media assets')
if (base/'assets').exists(): errors.append('binary media asset tree should be generated, not committed')
if errors:
    print('GENERATED_MEDIA_SEED_V17=FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('GENERATED_MEDIA_SEED_V17=PASS')
