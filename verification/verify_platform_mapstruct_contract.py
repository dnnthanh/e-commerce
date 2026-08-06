from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1] / 'backend'
platform = root / 'platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/mapping'
errors = []

required = {
    'PlatformMapperConfig.java': [
        'unmappedTargetPolicy = ReportingPolicy.IGNORE',
        'nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE',
        'componentModel = MappingConstants.ComponentModel.SPRING',
    ],
    'MapperContract.java': ['interface MapperContract'],
    'RequestModelMapper.java': ['requestToModel', 'requestsToModels', 'extends MapperContract'],
    'ModelEntityMapper.java': ['modelToEntity', 'entityToModel', 'modelsToEntities', 'entitiesToModels', 'extends MapperContract'],
    'ModelResponseMapper.java': ['modelToResponse', 'modelsToResponses', 'extends MapperContract'],
    'PatchMapper.java': ['@MappingTarget', 'void update', 'extends MapperContract'],
    'BaseMapper.java': ['RequestModelMapper', 'ModelEntityMapper', 'ModelResponseMapper'],
}
for name, needles in required.items():
    path = platform / name
    if not path.exists():
        errors.append(f'missing {path.relative_to(root)}')
        continue
    text = path.read_text()
    for needle in needles:
        if needle not in text:
            errors.append(f'{name}: missing {needle}')

mappers = list((root / 'services').rglob('*Mapper.java'))
managed = 0
specialized = 0
for path in mappers:
    text = path.read_text()
    if '@Mapper' not in text:
        continue
    if 'config = PlatformMapperConfig.class' not in text:
        errors.append(f'{path.relative_to(root)}: missing PlatformMapperConfig')
    if 'componentModel = MappingConstants.ComponentModel.SPRING' in text:
        errors.append(f'{path.relative_to(root)}: duplicates componentModel instead of common config')
    if not re.search(r'(extends|implements)\s+[^\{]*(MapperContract|RequestModelMapper|ModelEntityMapper|ModelResponseMapper|BaseMapper|PatchMapper)', text, re.S):
        errors.append(f'{path.relative_to(root)}: mapper does not extend a platform mapper contract')
    else:
        managed += 1
    if any(token in text for token in ('RequestModelMapper<', 'ModelEntityMapper<', 'ModelResponseMapper<', 'BaseMapper<', 'PatchMapper<')):
        specialized += 1

if specialized < 8:
    errors.append(f'only {specialized} service mappers use typed generic mapper contracts; expected at least 8')

pom = (root / 'platform/be-platform-starter/pom.xml').read_text()
if '<artifactId>mapstruct</artifactId>' not in pom:
    errors.append('be-platform-starter/pom.xml: missing mapstruct dependency')

if errors:
    print('PLATFORM_MAPSTRUCT_CONTRACT: FAIL')
    for e in errors:
        print(' -', e)
    sys.exit(1)
print(f'PLATFORM_MAPSTRUCT_CONTRACT: PASS ({managed} managed mappers, {specialized} typed generic mappers)')
