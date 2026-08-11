from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
errors = []


def require(condition, message):
    if not condition:
        errors.append(message)


def text(path):
    target = root / path
    if not target.exists():
        errors.append(f'missing file: {path}')
        return ''
    return target.read_text(errors='ignore')


navigation = text('frontend/projects/admin/src/app/admin-navigation.ts')
for snippet in ['AdminNavGroup', 'ADMIN_NAVIGATION', 'Marketplace', 'Commerce', 'Community', 'Governance']:
    require(snippet in navigation, f'admin centralized navigation missing: {snippet}')

shell = text('frontend/projects/admin/src/app/layout/admin-shell.component.ts')
for snippet in ['ADMIN_NAVIGATION', 'auth.has', 'router-outlet']:
    require(snippet in shell, f'admin shell missing permission-aware boundary: {snippet}')

routes = text('frontend/projects/admin/src/app/admin.routes.ts')
for snippet in ["path: 'unauthorized'", "path: 'forbidden'", 'permissionGuard']:
    require(snippet in routes, f'admin routes missing auth state contract: {snippet}')

dashboard = text('frontend/projects/admin/src/app/features/dashboard.component.ts')
require("from '../../../../../shared/api.service'" not in dashboard, 'dashboard imports raw ApiService')
require('inject(ApiService)' not in dashboard, 'dashboard injects raw ApiService')
require('signal<any' not in dashboard, 'dashboard still uses any-backed feature state')
require('<strong>53</strong>' not in dashboard, 'dashboard still hard-codes service count 53')
require('MarketplaceApiService' in dashboard, 'dashboard does not use MarketplaceApiService')
require('IncidentView' in dashboard, 'dashboard is not typed with IncidentView')

marketplace_api = text('frontend/shared/marketplace-api.service.ts')
for snippet in [
    'hideComment(threadId: string)',
    'unhideComment(threadId: string)',
    '/private/comments/${threadId}/hide',
    '/private/comments/${threadId}/unhide',
]:
    require(snippet in marketplace_api, f'missing browser-safe comment moderation facade: {snippet}')

moderation = text('frontend/projects/admin/src/app/features/moderation.component.ts')
for snippet in ['hideComment', 'unhideComment', 'COMMENT_MODERATE']:
    require(snippet in moderation, f'admin moderation missing real moderator action: {snippet}')
require('reportComment(' not in moderation, 'admin moderation still substitutes report API for moderator action')

admin_root = root / 'frontend/projects/admin/src'
admin_text = '\n'.join(
    path.read_text(errors='ignore')
    for path in admin_root.rglob('*.ts')
    if path.is_file()
)
require("'/internal/" not in admin_text, 'admin TypeScript calls /internal/**')
require('"/internal/' not in admin_text, 'admin TypeScript calls /internal/**')

playwright = text('frontend/playwright/admin-core-flow.spec.ts')
for snippet in ['permission-aware navigation', 'moderation', 'forbidden', 'mobile']:
    require(snippet.lower() in playwright.lower(), f'Admin Playwright core flow missing coverage marker: {snippet}')
for forbidden in ['page.route(', 'installBusinessApiMocks']:
    require(forbidden not in playwright, f'Admin core Playwright must use real services, found: {forbidden}')

coverage = text('frontend/COVERAGE-MATRIX.md')
require('Feature 015' in coverage, 'coverage matrix does not record Feature 015 Admin core flow')

comment_api = text(
    'backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApi.java'
)
for snippet in [
    '/private/comments/{threadId}/hide',
    '/private/comments/{threadId}/unhide',
    'COMMENT_MODERATE',
]:
    require(snippet in comment_api, f'Comment API missing Admin moderation contract: {snippet}')

if errors:
    print('ADMIN_CONSOLE_V15=FAIL')
    for error in errors:
        print('-', error)
    sys.exit(1)

print('ADMIN_CONSOLE_V15=PASS')
