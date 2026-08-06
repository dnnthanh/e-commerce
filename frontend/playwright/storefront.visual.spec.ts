import { expect, Page, Route, test } from '@playwright/test';
import { mkdirSync } from 'node:fs';

const product = {
  id: 101,
  sellerId: 10001,
  categoryId: 501,
  name: 'Nova X1 Pro',
  description: 'Điện thoại flagship dành cho khách hàng muốn hiệu năng mạnh và trải nghiệm gọn gàng.',
  status: 'PUBLISHED',
  updatedAt: '2026-08-06T08:00:00Z',
};

const secondProduct = {
  id: 102,
  sellerId: 10002,
  categoryId: 502,
  name: 'NovaBook Air 14',
  description: 'Laptop mỏng nhẹ cho học tập và công việc hằng ngày.',
  status: 'PUBLISHED',
  updatedAt: '2026-08-06T08:05:00Z',
};

const offers = [
  {
    skuId: 1001,
    productId: 101,
    sellerId: 10001,
    productName: 'Nova X1 Pro',
    sellerSku: 'NOVA-X1-128-BLK',
    variantName: '128 GB · Đen',
    purchaseLimit: 5,
  },
  {
    skuId: 1002,
    productId: 101,
    sellerId: 10001,
    productName: 'Nova X1 Pro',
    sellerSku: 'NOVA-X1-256-BLU',
    variantName: '256 GB · Xanh',
    purchaseLimit: 3,
  },
];

const order = {
  orderNo: 'ORD-20260806-001',
  grossAmount: 18990000,
  discountAmount: 1000000,
  payableAmount: 17990000,
  status: 'FULFILLING',
  sellerOrders: [
    {
      sellerOrderNo: 'ORD-20260806-001-S10001',
      sellerId: 10001,
      grossAmount: 18990000,
      discountAmount: 1000000,
      payableAmount: 17990000,
      status: 'FULFILLING',
    },
  ],
  createdAt: '2026-08-06T09:15:00Z',
  updatedAt: '2026-08-06T10:20:00Z',
};

const pageMetadata = {
  page: 0,
  size: 24,
  totalElements: 2,
  totalPages: 1,
  hasNext: false,
  hasPrevious: false,
};

const jsonHeaders = {
  'access-control-allow-origin': 'http://localhost:4200',
  'access-control-allow-headers': 'authorization,content-type',
  'access-control-allow-methods': 'GET,POST,PUT,DELETE,OPTIONS',
  'content-type': 'application/json',
};

function envelope(data: unknown, metadata?: unknown): string {
  return JSON.stringify(metadata === undefined ? { data } : { data, metadata });
}

async function fulfillJson(route: Route, data: unknown, metadata?: unknown): Promise<void> {
  await route.fulfill({
    status: 200,
    headers: jsonHeaders,
    body: envelope(data, metadata),
  });
}

async function installBusinessApiMocks(page: Page): Promise<void> {
  let cart = {
    cartKey: 'customer.demo',
    version: 3,
    items: [
      {
        sellerId: 10001,
        skuId: 1001,
        quantity: 1,
        priceSnapshot: 18990000,
        selected: true,
        savedForLater: false,
      },
    ],
  };

  await page.route('http://localhost:8080/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;

    if (request.method() === 'OPTIONS') {
      await route.fulfill({ status: 204, headers: jsonHeaders });
      return;
    }

    if (path === '/private/me/authorization') {
      await fulfillJson(route, {
        roles: ['CUSTOMER'],
        permissions: [
          'PRODUCT_VIEW',
          'SELLER_VIEW',
          'CART_VIEW',
          'CHECKOUT_VIEW',
          'ORDER_VIEW',
          'ORDER_CANCEL',
        ],
        sellerIds: [],
      });
      return;
    }

    if (path === '/products') {
      const categoryId = url.searchParams.get('categoryId');
      const data = categoryId === '501' ? [product] : [product, secondProduct];
      await fulfillJson(route, data, {
        ...pageMetadata,
        size: Number(url.searchParams.get('size') ?? 24),
        totalElements: data.length,
      });
      return;
    }

    if (path === '/products/101') {
      await fulfillJson(route, product);
      return;
    }

    if (path === '/products/101/offers') {
      await fulfillJson(route, offers);
      return;
    }

    if (path === '/products/offers/1001') {
      await fulfillJson(route, offers[0]);
      return;
    }

    if (path === '/products/offers/1002') {
      await fulfillJson(route, offers[1]);
      return;
    }

    if (path === '/media/products/101/assets') {
      await fulfillJson(route, [
        { placement: 'GALLERY', width: 1200, height: 1200, format: 'webp', objectKey: 'nova-x1-front.webp' },
        { placement: 'GALLERY', width: 1200, height: 1200, format: 'webp', objectKey: 'nova-x1-back.webp' },
      ]);
      return;
    }

    if (path === '/prices') {
      const skuId = Number(url.searchParams.get('skuId') ?? 1001);
      await fulfillJson(route, {
        skuId,
        sellerId: 10001,
        amount: skuId === 1002 ? 21990000 : 18990000,
        currency: 'VND',
        ruleId: 9001,
        resolvedAt: '2026-08-06T10:00:00Z',
      });
      return;
    }

    if (path === '/search') {
      await fulfillJson(
        route,
        {
          items: [
            { productId: 101, sellerId: 10001, name: 'Nova X1 Pro', price: 18990000, rating: 4.8 },
            { productId: 102, sellerId: 10002, name: 'NovaBook Air 14', price: 22990000, rating: 4.6 },
          ],
          facets: {
            category: { 'Điện thoại': 1, Laptop: 1 },
            seller: { 'Nova Official': 1, 'Nova Computing': 1 },
          },
          total: 2,
        },
        { total: 2, size: 24, hasNext: false },
      );
      return;
    }

    if (path === '/reviews/summary') {
      await fulfillJson(route, { productId: 101, averageRating: 4.8, reviewCount: 126 });
      return;
    }

    if (path === '/reviews') {
      await fulfillJson(
        route,
        [
          {
            id: 701,
            productId: 101,
            rating: 5,
            title: 'Máy đẹp và mượt',
            content: 'Màn hình sáng, máy phản hồi nhanh và cầm khá chắc tay.',
            createdAt: '2026-08-05T04:00:00Z',
          },
        ],
        { ...pageMetadata, totalElements: 1 },
      );
      return;
    }

    if (path === '/comments') {
      await fulfillJson(
        route,
        [
          {
            id: 'comment-1',
            productId: 101,
            sellerId: 10001,
            authorId: 'customer-42',
            content: 'Phiên bản 256 GB có hỗ trợ eSIM không?',
            status: 'VISIBLE',
            replyCount: 2,
            createdAt: '2026-08-05T05:30:00Z',
            updatedAt: '2026-08-05T06:00:00Z',
          },
        ],
        { ...pageMetadata, totalElements: 1 },
      );
      return;
    }

    if (path === '/private/cart' && request.method() === 'GET') {
      await fulfillJson(route, cart);
      return;
    }

    if (path === '/private/cart/items' && request.method() === 'PUT') {
      const body = request.postDataJSON() as {
        sellerId: number;
        skuId: number;
        quantity: number;
        priceSnapshot: number;
        selected: boolean;
      };
      const nextItem = { ...body, savedForLater: false };
      const remaining = cart.items.filter(
        (item) => !(item.sellerId === body.sellerId && item.skuId === body.skuId),
      );
      cart = { ...cart, version: cart.version + 1, items: [...remaining, nextItem] };
      await fulfillJson(route, cart);
      return;
    }

    if (path === '/private/cart/validate' && request.method() === 'POST') {
      await fulfillJson(route, { valid: true, violations: [] });
      return;
    }

    if (path === '/private/checkout' && request.method() === 'POST') {
      const body = request.postDataJSON() as { checkoutKey: string };
      await fulfillJson(route, {
        checkoutKey: body.checkoutKey,
        status: 'ORDER_CREATED',
        orderNo: order.orderNo,
        paymentStatus: 'PENDING',
      });
      return;
    }

    if (path === '/private/orders' && request.method() === 'GET') {
      await fulfillJson(route, [order], { ...pageMetadata, size: 20, totalElements: 1 });
      return;
    }

    if (path === `/private/orders/${order.orderNo}` && request.method() === 'GET') {
      await fulfillJson(route, order);
      return;
    }

    if (path === `/private/fulfillment/orders/${order.orderNo}` && request.method() === 'GET') {
      await fulfillJson(route, [
        {
          shipmentNo: 'SHP-20260806-001',
          sellerId: 10001,
          warehouseId: 1,
          carrierCode: 'NOVA EXPRESS',
          trackingNo: 'NX20260806001',
          status: 'IN_TRANSIT',
          updatedAt: '2026-08-06T10:18:00Z',
        },
      ]);
      return;
    }

    await route.fulfill({
      status: 404,
      headers: jsonHeaders,
      body: JSON.stringify({
        error: {
          code: 'PLAYWRIGHT_FIXTURE_MISSING',
          message: `No Playwright fixture for ${request.method()} ${path}`,
        },
      }),
    });
  });
}

test.beforeAll(() => {
  mkdirSync('playwright-artifacts', { recursive: true });
});

test.beforeEach(async ({ page }) => {
  await installBusinessApiMocks(page);
});

test('anonymous customer can inspect discovery and product-detail UI', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Khám phá sản phẩm từ nhiều cửa hàng trong một nơi.' })).toBeVisible();
  await expect(page.getByText('Nova X1 Pro')).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/01-home-desktop.png', fullPage: true });

  await page.goto('/search?q=nova');
  await expect(page.getByRole('heading', { name: 'Tìm sản phẩm' })).toBeVisible();
  await expect(page.getByText('2 kết quả')).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/02-search-results.png', fullPage: true });

  await page.goto('/category/501');
  await expect(page.getByRole('heading', { name: 'Danh mục #501' })).toBeVisible();
  await expect(page.getByText('Nova X1 Pro')).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/03-category.png', fullPage: true });

  await page.goto('/product/101');
  await expect(page.getByRole('heading', { name: 'Nova X1 Pro' })).toBeVisible();
  await expect(page.getByRole('button', { name: '128 GB · Đen NOVA-X1-128-BLK' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Đăng nhập để mua' })).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/04-product-anonymous.png', fullPage: true });
});

test('authenticated customer can review product, cart, checkout and order tracking UI', async ({ page }) => {
  await page.goto('/product/101');
  await expect(page.getByRole('button', { name: 'Đăng nhập để mua' })).toBeVisible();
  await page.getByRole('button', { name: 'Đăng nhập để mua' }).click();

  await expect(page).toHaveURL(/localhost:8180/);
  await page.locator('#username').fill('customer.demo');
  await page.locator('#password').fill('Customer@123');
  await page.locator('#kc-login').click();

  await expect(page).toHaveURL(/localhost:4200\/product\/101/);
  await expect(page.getByRole('button', { name: 'Thêm vào giỏ' })).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/05-product-authenticated.png', fullPage: true });

  await page.getByRole('button', { name: 'Thêm vào giỏ' }).click();
  await expect(page.getByText('Đã thêm sản phẩm vào giỏ hàng.')).toBeVisible();

  await page.getByRole('link', { name: /Giỏ hàng/ }).first().click();
  await expect(page.getByRole('heading', { name: 'Giỏ hàng của bạn' })).toBeVisible();
  await expect(page.getByText('128 GB · Đen')).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/06-cart.png', fullPage: true });

  await page.getByRole('link', { name: /Tiếp tục checkout/ }).click();
  await expect(page.getByRole('heading', { name: 'Xác nhận đơn hàng' })).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/07-checkout.png', fullPage: true });

  await page.getByRole('button', { name: 'Đặt hàng' }).click();
  await expect(page.getByRole('heading', { name: 'Đơn hàng đã được tạo' })).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/08-checkout-result.png', fullPage: true });

  await page.getByRole('link', { name: 'Xem đơn hàng' }).click();
  await expect(page.getByRole('heading', { name: order.orderNo })).toBeVisible();
  await expect(page.getByText('NX20260806001')).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/09-order-tracking.png', fullPage: true });
});

test('mobile storefront shell remains usable', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/');
  await expect(page.getByRole('navigation', { name: 'Điều hướng di động' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Khám phá sản phẩm từ nhiều cửa hàng trong một nơi.' })).toBeVisible();
  await page.screenshot({ path: 'playwright-artifacts/10-home-mobile.png', fullPage: true });
});
