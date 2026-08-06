/** Frontend DTOs mirror stable public/private backend contracts. Enum values stay as backend @JsonValue codes. */
export type Money = number;
export type IsoDateTime = string;

export interface ProductView {
  id: number;
  sellerId: number;
  categoryId: number;
  name: string;
  description?: string;
  status: string;
  updatedAt: IsoDateTime;
}

export interface ProductOfferView {
  skuId: number;
  productId: number;
  sellerId: number;
  productName: string;
  sellerSku: string;
  variantName: string;
  purchaseLimit: number;
}

export interface PriceView {
  skuId: number;
  sellerId: number;
  amount: Money;
  currency: string;
  ruleId?: number;
  resolvedAt: IsoDateTime;
}

export interface MediaVariant {
  placement: string;
  width: number;
  height: number;
  format: string;
  objectKey: string;
}

export interface ReviewView {
  id: number;
  productId: number;
  rating: number;
  title: string;
  content: string;
  createdAt: IsoDateTime;
}

export interface ReviewSummary {
  productId: number;
  averageRating: number;
  reviewCount: number;
}

export interface CommentThread {
  id: string;
  productId: number;
  sellerId: number;
  authorId: string;
  content?: string;
  status: string;
  replyCount: number;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface CommentReply {
  id: string;
  threadId: string;
  authorId: string;
  content: string;
  createdAt: IsoDateTime;
}

export interface ShopView {
  id: number;
  sellerId: number;
  slug: string;
  name: string;
  description?: string;
  status: string;
  updatedAt: IsoDateTime;
}

export interface CartItem {
  sellerId: number;
  skuId: number;
  quantity: number;
  priceSnapshot: Money;
  selected?: boolean;
  savedForLater?: boolean;
}

export interface CartView {
  cartKey: string;
  version: number;
  items: CartItem[];
}

export interface CartValidation {
  valid: boolean;
  violations?: Array<{
    code: string;
    message: string;
    sellerId?: number;
    skuId?: number;
  }>;
}

export interface CheckoutResponse {
  checkoutKey: string;
  status: string;
  orderNo?: string;
  paymentStatus: string;
  redirectUrl?: string;
}

export interface SellerOrder {
  sellerOrderNo?: string;
  sellerId: number;
  grossAmount?: Money;
  discountAmount?: Money;
  payableAmount?: Money;
  status?: string;
}

export interface OrderView {
  orderNo: string;
  grossAmount: Money;
  discountAmount: Money;
  payableAmount: Money;
  status: string;
  cancellationReason?: string;
  sellerOrders: SellerOrder[];
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface ShipmentView {
  shipmentNo: string;
  sellerId: number;
  warehouseId: number;
  carrierCode?: string;
  trackingNo?: string;
  status: string;
  updatedAt: IsoDateTime;
}

export interface ReturnView {
  returnKey: string;
  orderId: string;
  status: string;
  refundableAmount: Money;
  receivingWarehouseId?: number;
  createdAt: IsoDateTime;
}

export interface NotificationView {
  id: string;
  type: string;
  title: string;
  message: string;
  payload: Record<string, unknown>;
  readAt?: IsoDateTime;
  createdAt: IsoDateTime;
}

export interface NotificationPreference {
  realtime: boolean;
  email: boolean;
  push: boolean;
  sellerUpdates: boolean;
}

export interface BalanceView {
  skuId: number;
  warehouseId: number;
  onHand: number;
  reserved: number;
  available: number;
  version: number;
}

export interface PaymentView {
  paymentKey: string;
  orderId: string;
  userId: string;
  provider: string;
  amount: Money;
  status: string;
  updatedAt: IsoDateTime;
}

export interface SettlementView {
  settlementNo: string;
  sellerId: number;
  gross: Money;
  commission: Money;
  payable: Money;
  status: string;
}

export interface IncidentView {
  id: number;
  type: string;
  sourceService: string;
  recoveryTarget: string;
  aggregateId: string;
  status: string;
  severity: string;
  lastError?: string;
  firstSeenAt: IsoDateTime;
}

export interface AuditView {
  eventId: string;
  actorId: string;
  actorType: string;
  action: string;
  resourceType: string;
  resourceId: string;
  sourceService: string;
  traceId: string;
  occurredAt: IsoDateTime;
}

export interface AuthorizationSnapshot {
  roles: string[];
  permissions: string[];
  sellerIds: number[];
}

export interface UploadSession {
  assetId: number;
  objectKey: string;
  uploadUrl: string;
  expiresAt: IsoDateTime;
}

export interface PromotionResult {
  subtotal: Money;
  totalDiscount: Money;
  payable: Money;
  applied: Array<{
    code?: string;
    promotionId?: number;
    discount?: Money;
    [key: string]: unknown;
  }>;
}

export interface SearchHit {
  productId: number;
  sellerId?: number;
  name: string;
  price?: Money;
  rating?: number;
  [key: string]: unknown;
}

export interface SearchResponse {
  items: SearchHit[];
  facets?: Record<string, Record<string, number>>;
  total?: number;
  nextCursor?: string;
}

export interface CheckoutItemRequest {
  sellerId: number;
  skuId: number;
  warehouseId: number;
  quantity: number;
}

export interface CreateReturnRequest {
  requestKey: string;
  orderNo: string;
  orderLineIds: number[];
  reason: string;
}

export interface ProductDraft {
  sellerId: number;
  categoryId: number;
  name: string;
  description?: string;
}

export interface UploadDraft {
  productId: number;
  sellerId: number;
  contentType: string;
  filename: string;
  checksum: string;
}
