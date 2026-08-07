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
  url: string;
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
  body?: string;
  read: boolean;
  createdAt: IsoDateTime;
}

export interface NotificationPreference {
  email: boolean;
  push: boolean;
  inApp: boolean;
}

export interface SearchItem {
  productId: number;
  sellerId: number;
  name: string;
  price?: Money;
  rating?: number;
}

export interface SearchResponse {
  items: SearchItem[];
  facets: Record<string, Record<string, number>>;
  total: number;
}

export interface UploadDraft {
  productId: number;
  sellerId: number;
  fileName: string;
  contentType: string;
  size: number;
  sha256: string;
}

export interface UploadSession {
  assetId: number;
  uploadUrl: string;
  objectKey: string;
  expiresAt: IsoDateTime;
}

export interface ProductDraft {
  sellerId: number;
  categoryId: number;
  name: string;
  description?: string;
}

export interface PromotionResult {
  code: string;
  discountAmount: Money;
  description?: string;
}

export interface PaymentView {
  paymentId: string;
  orderNo: string;
  status: string;
  amount: Money;
}

export interface BalanceView {
  accountId: string;
  available: Money;
  pending: Money;
  currency: string;
}

export interface SettlementView {
  settlementId: string;
  sellerId: number;
  amount: Money;
  status: string;
  createdAt: IsoDateTime;
}

export interface IncidentView {
  id: string;
  type: string;
  status: string;
  details?: string;
  createdAt: IsoDateTime;
}

export interface AuditView {
  id: string;
  action: string;
  actorId?: string;
  resourceType?: string;
  resourceId?: string;
  createdAt: IsoDateTime;
}

export interface AuthorizationSnapshot {
  roles: string[];
  permissions: string[];
  sellerIds: number[];
}

export interface CreateReturnRequest {
  orderId: string;
  orderLineIds: string[];
  reason: string;
}
