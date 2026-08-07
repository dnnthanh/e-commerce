import { expect, test } from '@playwright/test';
import { mkdir } from 'node:fs/promises';

const ARTIFACT_DIR = 'playwright-artifacts';

async function capture(page: import('@playwright/test').Page, name: string): Promise<void> {
  await mkdir(ARTIFACT_DIR, { recursive: true });
  await page.screenshot({ path: `${ARTIFACT_DIR}/${name}.png`, fullPage: true });
}

async function expectLoadedProductImages(page: import('@playwright/test').Page): Promise<void> {
  const cards = page.locator('market-product-card');
  await expect(cards.first()).toBeVisible();
  const cardCount = await cards.count();
  expect(cardCount).toBeGreaterThanOrEqual(8);

  const images = cards.locator('img.product-image');
  await expect(images.first()).toBeVisible();
  const imageCount = await images.count();
  expect(imageCount).toBeGreaterThanOrEqual(8);

  for (let index = 0; index < Math.min(imageCount, 12); index += 1) {
    const image = images.nth(index);
    await expect(image).toHaveAttribute('src', /localhost:9000\/marketplace-media\/seed\/products\//);
    await expect
      .poll(() =>
        image.evaluate((element: HTMLImageElement) => element.complete && element.naturalWidth > 0),
      )
      .toBe(true);
  }
}

test.describe('Storefront real demo visual evidence', () => {
  test('home renders real Catalog products with MinIO-backed media', async ({ page }) => {
    const failedImages: string[] = [];
    page.on('response', (response) => {
      if (response.request().resourceType() === 'image' && !response.ok()) {
        failedImages.push(`${response.status()} ${response.url()}`);
      }
    });

    await page.goto('/');
    await expect(page.getByRole('heading', { name: /Khám phá sản phẩm/i })).toBeVisible();
    await expectLoadedProductImages(page);
    expect(failedImages).toEqual([]);
    await capture(page, 'home-real-data-desktop');
  });

  test('category route keeps real images and real catalog data', async ({ page }) => {
    await page.goto('/');
    const firstCategory = page.locator('.category-links a').first();
    await expect(firstCategory).toBeVisible();
    await firstCategory.click();
    await expect(page).toHaveURL(/\/category\/\d+/);
    await expect(page.getByRole('heading', { name: /Danh mục #/ })).toBeVisible();
    await expectLoadedProductImages(page);
    await capture(page, 'category-real-data-desktop');
  });

  test('product detail uses real Media variants from MinIO', async ({ page }) => {
    await page.goto('/');
    const firstCard = page.locator('market-product-card a.product-card').first();
    await expect(firstCard).toBeVisible();
    await firstCard.click();
    await expect(page).toHaveURL(/\/product\/\d+/);

    const mainImage = page.locator('.product-gallery-main img');
    await expect(mainImage).toBeVisible();
    await expect(mainImage).toHaveAttribute(
      'src',
      /localhost:9000\/marketplace-media\/seed\/products\//,
    );
    await expect
      .poll(() =>
        mainImage.evaluate((element: HTMLImageElement) => element.complete && element.naturalWidth > 0),
      )
      .toBe(true);

    const thumbnails = page.locator('.media-thumb img');
    expect(await thumbnails.count()).toBeGreaterThanOrEqual(3);
    await capture(page, 'product-detail-real-data-desktop');
  });

  test('mobile home renders real product media without horizontal overflow', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/');
    await expectLoadedProductImages(page);
    const hasHorizontalOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
    );
    expect(hasHorizontalOverflow).toBe(false);
    await capture(page, 'home-real-data-mobile');
  });
});
