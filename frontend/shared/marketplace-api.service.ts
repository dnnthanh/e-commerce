import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { CursorResult, PageResult } from './api-contract';
import {
  AuditView,
  AuthorizationSnapshot,
  BalanceView,
  CartValidation,
  CartView,
  CheckoutItemRequest,
  CheckoutResponse,
  CommentReply,
  CommentThread,
  CreateReturnRequest,
  IncidentView,
  MediaVariant,
  NotificationPreference,
  NotificationView,
  OrderView,
  PaymentView,
  PriceView,
  ProductDraft,
  ProductOfferView,
  ProductView,
  PromotionResult,
  ReturnView,
  ReviewSummary,
  ReviewView,
  SearchResponse,
  SettlementView,
  ShipmentView,
  ShopView,
  UploadDraft,
  UploadSession,
} from './marketplace-types';

export interface ProductSearchFilters {
  keyword?: string;
  sellerId?: number;
  categoryId?: number;
}

/** Bounded-context-aware API facade. Components should not hand-build backend paths. */
@Injectable({ providedIn: 'root' })
export class MarketplaceApiService {
  constructor(private readonly api: ApiService) {}

  // Discovery / catalog / pricing / media
  products(
    page = 0,
    size = 24,
    criteria: ProductSearchFilters | string = {},
  ): Promise<PageResult<ProductView>> {
    const filters: ProductSearchFilters =
      typeof criteria === 'string' ? { keyword: criteria } : criteria;
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (filters.keyword) params.set('keyword', filters.keyword);
    if (filters.sellerId !== undefined) params.set('sellerId', String(filters.sellerId));
    if (filters.categoryId !== undefined) params.set('categoryId', String(filters.categoryId));
    return this.api.getPage(`/products?${params}`);
  }

  product(productId: number): Promise<ProductView> {
    return this.api.get(`/products/${productId}`);
  }

  productOffers(productId: number): Promise<ProductOfferView[]> {
    return this.api.get(`/products/${productId}/offers`);
  }

  offer(skuId: number): Promise<ProductOfferView> {
    return this.api.get(`/products/offers/${skuId}`);
  }

  productMedia(productId: number): Promise<MediaVariant[]> {
    return this.api.get(`/media/products/${productId}/assets`);
  }

  price(skuId: number, sellerId: number, channel = 'WEB'): Promise<PriceView> {
    return this.api.get(
      `/prices?skuId=${skuId}&sellerId=${sellerId}&channel=${encodeURIComponent(channel)}`,
    );
  }

  search(query: string, cursor?: string, size = 24): Promise<CursorResult<SearchResponse>> {
    const params = new URLSearchParams({ query, size: String(size) });
    if (cursor) params.set('cursor', cursor);
    return this.api.getCursor(`/search?${params}`);
  }

  shop(shopId: number): Promise<ShopView> {
    return this.api.get(`/shops/${shopId}`);
  }

  // Cart / checkout / orders
  cart(): Promise<CartView> {
    return this.api.get('/private/cart', true);
  }

  putCartItem(item: {
    sellerId: number;
    skuId: number;
    quantity: number;
    priceSnapshot: number;
    selected: boolean;
    expectedVersion: number;
  }): Promise<CartView> {
    return this.api.send('PUT', '/private/cart/items', item);
  }

  removeCartItem(sellerId: number, skuId: number): Promise<CartView> {
    return this.api.send('DELETE', `/private/cart/items/${sellerId}/${skuId}`);
  }

  saveForLater(sellerId: number, skuId: number): Promise<CartView> {
    return this.api.send('POST', `/private/cart/items/${sellerId}/${skuId}/save-for-later`);
  }

  moveToCart(sellerId: number, skuId: number): Promise<CartView> {
    return this.api.send('POST', `/private/cart/items/${sellerId}/${skuId}/move-to-cart`);
  }

  validateCart(channel = 'WEB'): Promise<CartValidation> {
    return this.api.send('POST', `/private/cart/validate?channel=${encodeURIComponent(channel)}`);
  }

  checkout(
    checkoutKey: string,
    items: CheckoutItemRequest[],
    promotionCodes: string[],
    provider: string,
  ): Promise<CheckoutResponse> {
    return this.api.send('POST', '/private/checkout', {
      checkoutKey,
      items,
      promotionCodes,
      provider,
    });
  }

  orders(page = 0, size = 20): Promise<PageResult<OrderView>> {
    return this.api.getPage(
      `/private/orders?page=${page}&size=${size}&sort=createdAt,desc`,
      true,
    );
  }

  order(orderNo: string): Promise<OrderView> {
    return this.api.get(`/private/orders/${encodeURIComponent(orderNo)}`, true);
  }

  cancelOrder(orderNo: string, reason: string): Promise<OrderView> {
    return this.api.send('POST', `/private/orders/${encodeURIComponent(orderNo)}/cancel`, {
      reason,
    });
  }

  shipments(orderNo: string): Promise<ShipmentView[]> {
    return this.api.get(`/private/fulfillment/orders/${encodeURIComponent(orderNo)}`, true);
  }

  // Returns
  returns(): Promise<ReturnView[]> {
    return this.api.get('/private/returns', true);
  }

  createReturn(request: CreateReturnRequest): Promise<ReturnView> {
    return this.api.send('POST', '/private/returns', request);
  }

  disputeReturn(returnKey: string, reason: string): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/disputes`, { reason });
  }

  // Review/comment community
  reviewSummary(productId: number): Promise<ReviewSummary> {
    return this.api.get(`/reviews/summary?productId=${productId}`);
  }

  reviews(productId: number, page = 0, size = 20): Promise<PageResult<ReviewView>> {
    return this.api.getPage(`/reviews?productId=${productId}&page=${page}&size=${size}`);
  }

  createReview(
    orderLineId: number,
    rating: number,
    title: string,
    content: string,
  ): Promise<ReviewView> {
    return this.api.send('POST', '/private/reviews', { orderLineId, rating, title, content });
  }

  markHelpful(reviewId: number, active: boolean): Promise<ReviewView> {
    return this.api.send('POST', `/private/reviews/${reviewId}/helpful?active=${active}`);
  }

  comments(
    productId: number,
    sellerId?: number,
    page = 0,
    size = 20,
  ): Promise<PageResult<CommentThread>> {
    const seller = sellerId ? `&sellerId=${sellerId}` : '';
    return this.api.getPage(`/comments?productId=${productId}${seller}&page=${page}&size=${size}`);
  }

  replies(threadId: string, page = 0, size = 20): Promise<PageResult<CommentReply>> {
    return this.api.getPage(`/comments/${threadId}/replies?page=${page}&size=${size}`);
  }

  createComment(productId: number, sellerId: number, content: string): Promise<CommentThread> {
    return this.api.send('POST', '/private/comments', { productId, sellerId, content });
  }

  reply(threadId: string, content: string): Promise<CommentReply> {
    return this.api.send('POST', `/private/comments/${threadId}/replies`, { content });
  }

  react(threadId: string, type: string): Promise<unknown> {
    return this.api.send('PUT', `/private/comments/${threadId}/reaction`, { type });
  }

  reportComment(threadId: string, reason: string, detail: string): Promise<unknown> {
    return this.api.send('POST', `/private/comments/${threadId}/reports`, {
      reason,
      details: detail,
    });
  }

  hideComment(threadId: string): Promise<CommentThread> {
    return this.api.send('POST', `/private/comments/${threadId}/hide`);
  }

  unhideComment(threadId: string): Promise<CommentThread> {
    return this.api.send('POST', `/private/comments/${threadId}/unhide`);
  }

  // Notifications
  notifications(limit = 100): Promise<NotificationView[]> {
    return this.api.get(`/private/notifications?limit=${limit}`, true);
  }

  notificationPreferences(): Promise<NotificationPreference> {
    return this.api.get('/private/notifications/preferences', true);
  }

  saveNotificationPreferences(p: NotificationPreference): Promise<NotificationPreference> {
    return this.api.send('PUT', '/private/notifications/preferences', p);
  }

  followSeller(sellerId: number): Promise<void> {
    return this.api.send('PUT', `/private/notifications/subscriptions/sellers/${sellerId}`);
  }

  unfollowSeller(sellerId: number): Promise<void> {
    return this.api.send('DELETE', `/private/notifications/subscriptions/sellers/${sellerId}`);
  }

  markRead(id: string): Promise<void> {
    return this.api.send('POST', `/private/notifications/${id}/read`);
  }

  markAllRead(): Promise<void> {
    return this.api.send('POST', '/private/notifications/read-all');
  }

  // Admin catalog/media
  createProduct(draft: ProductDraft): Promise<ProductView> {
    return this.api.send('POST', '/private/products', draft);
  }

  publishProduct(productId: number): Promise<ProductView> {
    return this.api.send('POST', `/private/products/${productId}/publish`);
  }

  createUpload(draft: UploadDraft): Promise<UploadSession> {
    return this.api.send('POST', '/private/media/uploads', draft);
  }

  completeUpload(assetId: number): Promise<void> {
    return this.api.send('POST', `/private/media/uploads/${assetId}/complete`);
  }

  // Admin inventory/payment/settlement
  balances(
    page = 0,
    size = 50,
    skuId?: number,
    warehouseId?: number,
  ): Promise<PageResult<BalanceView>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (skuId) params.set('skuId', String(skuId));
    if (warehouseId) params.set('warehouseId', String(warehouseId));
    return this.api.getPage(`/private/inventory/balances?${params}`, true);
  }

  payments(page = 0, size = 50, status = ''): Promise<PageResult<PaymentView>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (status) params.set('status', status);
    return this.api.getPage(`/private/payments?${params}`, true);
  }

  settlements(sellerId: number, page = 0, size = 50): Promise<PageResult<SettlementView>> {
    return this.api.getPage(
      `/private/settlements?sellerId=${sellerId}&page=${page}&size=${size}`,
      true,
    );
  }

  approveSettlement(no: string, reason: string): Promise<void> {
    return this.api.send('POST', `/private/settlements/${no}/approve`, { reason });
  }

  // Admin seller/security/audit/operations
  sellerShops(sellerId: number): Promise<ShopView[]> {
    return this.api.get(`/private/seller/shops?sellerId=${sellerId}`, true);
  }

  updateShop(shopId: number, sellerId: number, name: string, description: string): Promise<ShopView> {
    return this.api.send('PUT', `/private/seller/shops/${shopId}`, {
      sellerId,
      name,
      description,
    });
  }

  authorization(userId: string): Promise<AuthorizationSnapshot> {
    return this.api.get(`/private/admin/security/users/${userId}`, true);
  }

  assignRole(userId: string, role: string): Promise<void> {
    return this.api.send('PUT', `/private/admin/security/users/${userId}/roles`, { role });
  }

  removeRole(userId: string, role: string): Promise<void> {
    return this.api.send(
      'DELETE',
      `/private/admin/security/users/${userId}/roles/${encodeURIComponent(role)}`,
    );
  }

  assignSellerScope(userId: string, sellerId: number): Promise<void> {
    return this.api.send('PUT', `/private/admin/security/users/${userId}/seller-scopes`, { sellerId });
  }

  removeSellerScope(userId: string, sellerId: number): Promise<void> {
    return this.api.send(
      'DELETE',
      `/private/admin/security/users/${userId}/seller-scopes/${sellerId}`,
    );
  }

  audit(page = 0, size = 50, resourceType = ''): Promise<PageResult<AuditView>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (resourceType) params.set('resourceType', resourceType);
    return this.api.getPage(`/private/audit?${params}`, true);
  }

  incidents(): Promise<IncidentView[]> {
    return this.api.get('/private/operations/incidents', true);
  }

  recoverIncident(id: number, reason: string): Promise<void> {
    return this.api.send('POST', `/private/operations/incidents/${id}/recover`, { reason });
  }

  resolveIncident(id: number, reason: string): Promise<void> {
    return this.api.send('POST', `/private/operations/incidents/${id}/resolve`, { reason });
  }

  // Admin promotion / fulfillment / return workflows
  evaluatePromotion(
    subtotal: number,
    codes: string[],
    lines: Array<Record<string, unknown>> = [],
    channel = 'WEB',
    customerSegment = 'DEFAULT',
  ): Promise<PromotionResult> {
    return this.api.send('POST', '/promotions/evaluate', {
      subtotal,
      codes,
      lines,
      channel,
      customerSegment,
    });
  }

  updateShipmentStatus(
    shipmentNo: string,
    sellerId: number,
    status: string,
    carrierCode?: string,
    trackingNo?: string,
  ): Promise<ShipmentView> {
    return this.api.send('PUT', `/private/fulfillment/shipments/${shipmentNo}/status`, {
      sellerId,
      status,
      carrierCode,
      trackingNo,
    });
  }

  approveReturn(returnKey: string, sellerId: number, reason: string): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/approve`, { sellerId, reason });
  }

  rejectReturn(returnKey: string, sellerId: number, reason: string): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/reject`, { sellerId, reason });
  }

  receiveReturn(returnKey: string, warehouseId: number): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/receive`, { warehouseId });
  }

  inspectReturn(
    returnKey: string,
    lines: Array<{ orderLineId: number; acceptedQuantity: number; disposition: string }>,
  ): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/inspect`, { lines });
  }

  refundReturn(returnKey: string): Promise<ReturnView> {
    return this.api.send('POST', `/private/returns/${returnKey}/refund`);
  }
}
