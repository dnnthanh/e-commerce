export interface AdminNavItem {
  label: string;
  path: string;
  permission?: string;
}

export interface AdminNavGroup {
  label: string;
  items: readonly AdminNavItem[];
}

/** One source of truth for Admin information architecture and navigation permissions. */
export const ADMIN_NAVIGATION: readonly AdminNavGroup[] = [
  {
    label: 'Overview',
    items: [{ label: 'Dashboard', path: '/' }],
  },
  {
    label: 'Marketplace',
    items: [
      { label: 'Sellers', path: '/sellers', permission: 'SELLER_VIEW' },
      { label: 'Catalog', path: '/catalog', permission: 'PRODUCT_PUBLISH' },
      { label: 'Media', path: '/media', permission: 'MEDIA_UPLOAD' },
      { label: 'Pricing', path: '/pricing' },
      { label: 'Promotions', path: '/promotions' },
      { label: 'Inventory', path: '/inventory', permission: 'INVENTORY_VIEW' },
    ],
  },
  {
    label: 'Commerce',
    items: [
      { label: 'Orders', path: '/orders', permission: 'ORDER_VIEW' },
      { label: 'Fulfillment', path: '/fulfillment', permission: 'FULFILLMENT_VIEW' },
      { label: 'Payments', path: '/payments', permission: 'PAYMENT_VIEW' },
      { label: 'Returns', path: '/returns', permission: 'RETURN_VIEW' },
      { label: 'Settlement', path: '/settlements', permission: 'SETTLEMENT_VIEW' },
    ],
  },
  {
    label: 'Community',
    items: [{ label: 'Moderation', path: '/moderation', permission: 'COMMENT_MODERATE' }],
  },
  {
    label: 'Governance',
    items: [
      { label: 'Security', path: '/security', permission: 'SECURITY_VIEW' },
      { label: 'Audit', path: '/audit', permission: 'AUDIT_VIEW' },
      { label: 'Operations', path: '/operations', permission: 'OPERATIONS_VIEW' },
    ],
  },
];
