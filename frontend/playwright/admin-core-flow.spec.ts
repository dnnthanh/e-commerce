import { expect, test, type Page } from '@playwright/test';
import { mkdir } from 'node:fs/promises';

const ARTIFACT_DIR = 'playwright-artifacts/admin-core-flow';
const SELLER_USERNAME = process.env.ADMIN_SELLER_USERNAME ?? 'seller.demo';
const SELLER_PASSWORD = process.env.ADMIN_SELLER_PASSWORD ?? 'Seller@123';
const ADMIN_USERNAME = process.env.ADMIN_PLATFORM_USERNAME ?? 'admin.demo';
const ADMIN_PASSWORD = process.env.ADMIN_PLATFORM_PASSWORD ?? 'Admin@123';

async function capture(page: Page, name: string): Promise<void> {
  await mkdir(ARTIFACT_DIR, { recursive: true });
  await page.screenshot({ path: `${ARTIFACT_DIR}/${name}.png`, fullPage: true });
}

async function login(page: Page, username: string, password: string): Promise<void> {
  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Admin login' })).toBeVisible();
  await page.getByRole('button', { name: 'Admin login' }).click();
  await expect(page.locator('#username')).toBeVisible();
  await page.locator('#username').fill(username);
  await page.locator('#password').fill(password);
  await page.locator('#kc-login').click();
  await expect(page.getByRole('heading', { name: 'Operations dashboard' })).toBeVisible();
}

test.describe('Admin Console Feature 015 real-service core flow', () => {
  test('permission-aware navigation and permitted Catalog action use real services', async ({ page }) => {
    await login(page, SELLER_USERNAME, SELLER_PASSWORD);

    await expect(page.getByRole('link', { name: 'Catalog', exact: true })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Security', exact: true })).toHaveCount(0);
    await expect(page.getByRole('link', { name: 'Moderation', exact: true })).toHaveCount(0);
    await expect(page.getByText('Incident feed unavailable')).toBeVisible();

    await page.getByRole('link', { name: 'Catalog', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();

    const productName = `PW Admin ${Date.now()}`;
    await page.getByLabel('Name').fill(productName);
    await page.getByLabel('Description').fill('Feature 015 real-service catalog evidence');
    await page.getByRole('button', { name: 'Create draft' }).click();

    await page.getByLabel('Search').fill(productName);
    await page.getByRole('button', { name: 'Search' }).click();
    const productRow = page.locator('.mini-row').filter({ hasText: productName });
    await expect(productRow).toBeVisible();
    await expect(productRow).toContainText('DRAFT');

    page.once('dialog', dialog => dialog.accept());
    await productRow.getByRole('button', { name: 'Publish' }).click();
    await expect(productRow).toContainText('PUBLISHED');
    await capture(page, 'catalog-real-action-desktop');
  });

  test('forbidden route is explicit for authenticated operators without permission', async ({ page }) => {
    await login(page, SELLER_USERNAME, SELLER_PASSWORD);
    await page.goto('/security');
    await expect(page).toHaveURL(/\/forbidden$/);
    await expect(page.getByText('Permission required')).toBeVisible();
    await capture(page, 'forbidden-seller-desktop');
  });

  test('moderation hides and restores a real seeded Comment through private commands', async ({ page }) => {
    await login(page, ADMIN_USERNAME, ADMIN_PASSWORD);
    await page.goto('/moderation');
    await expect(page.getByRole('heading', { name: 'Community triage' })).toBeVisible();
    await page.getByLabel('Product ID').fill('1002');
    await page.getByRole('button', { name: 'Load triage' }).click();

    const commentsPanel = page.locator('.panel').filter({ has: page.getByRole('heading', { name: 'Comments' }) });
    const firstComment = commentsPanel.locator('.mini-row').filter({ has: page.getByRole('button', { name: 'Hide' }) }).first();
    await expect(firstComment).toBeVisible();

    page.once('dialog', dialog => dialog.accept());
    await firstComment.getByRole('button', { name: 'Hide' }).click();
    await expect(firstComment).toContainText('HIDDEN');
    await expect(firstComment.getByRole('button', { name: 'Unhide' })).toBeVisible();
    await capture(page, 'moderation-hidden-comment-desktop');

    page.once('dialog', dialog => dialog.accept());
    await firstComment.getByRole('button', { name: 'Unhide' }).click();
    await expect(firstComment).toContainText('ACTIVE');
    await expect(firstComment.getByRole('button', { name: 'Hide' })).toBeVisible();
  });

  test('order drill-down carries real order context into fulfillment', async ({ page }) => {
    await login(page, ADMIN_USERNAME, ADMIN_PASSWORD);
    await page.goto('/orders');
    await expect(page.getByRole('heading', { name: 'Orders' })).toBeVisible();

    const firstOrderRow = page.locator('tbody tr').first();
    await expect(firstOrderRow).toBeVisible();
    const orderNo = (await firstOrderRow.locator('td').first().locator('b').innerText()).trim();
    expect(orderNo).toMatch(/^ORD-/);
    await firstOrderRow.getByRole('button', { name: 'Details' }).click();
    await expect(page.getByRole('heading', { name: orderNo })).toBeVisible();

    await page.getByRole('link', { name: 'Fulfillment', exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`/fulfillment\\?orderNo=${orderNo}$`));
    await expect(page.getByLabel('Order number')).toHaveValue(orderNo);
    await capture(page, 'order-to-fulfillment-desktop');
  });

  test('mobile Admin shell opens permission-aware navigation without overflow', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await login(page, ADMIN_USERNAME, ADMIN_PASSWORD);
    await page.getByRole('button', { name: 'Toggle administration navigation' }).click();
    await expect(page.getByRole('navigation', { name: 'Administration' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Moderation', exact: true })).toBeVisible();
    const hasHorizontalOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
    );
    expect(hasHorizontalOverflow).toBe(false);
    await capture(page, 'mobile-admin-navigation');
  });
});
