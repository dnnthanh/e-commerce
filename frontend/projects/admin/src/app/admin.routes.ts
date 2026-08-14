import { Routes } from '@angular/router';
import { permissionGuard } from '../../../../shared/auth.guard';
import { adminAuthGuard } from './admin-auth.guard';
import { AuditComponent } from './features/audit.component';
import { CatalogComponent } from './features/catalog.component';
import { DashboardComponent } from './features/dashboard.component';
import { ForbiddenComponent } from './features/forbidden.component';
import { FulfillmentComponent } from './features/fulfillment.component';
import { InventoryComponent } from './features/inventory.component';
import { MediaComponent } from './features/media.component';
import { ModerationComponent } from './features/moderation.component';
import { OperationsComponent } from './features/operations.component';
import { OrdersAdminComponent } from './features/orders.component';
import { PaymentsComponent } from './features/payments.component';
import { PricingComponent } from './features/pricing.component';
import { PromotionComponent } from './features/promotion.component';
import { ReturnsAdminComponent } from './features/returns.component';
import { SecurityComponent } from './features/security.component';
import { SellersComponent } from './features/sellers.component';
import { SettlementsComponent } from './features/settlements.component';
import { UnauthorizedComponent } from './features/unauthorized.component';

const guarded = (permission?: string) =>
  permission
    ? [adminAuthGuard, permissionGuard(permission, '/forbidden')]
    : [adminAuthGuard];

/** Admin routes preserve backend permission names while providing explicit auth states. */
export const ADMIN_ROUTES: Routes = [
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'forbidden', component: ForbiddenComponent, canActivate: [adminAuthGuard] },
  { path: '', component: DashboardComponent, canActivate: guarded() },
  { path: 'sellers', component: SellersComponent, canActivate: guarded('SELLER_VIEW') },
  { path: 'catalog', component: CatalogComponent, canActivate: guarded('PRODUCT_PUBLISH') },
  { path: 'media', component: MediaComponent, canActivate: guarded('MEDIA_UPLOAD') },
  { path: 'pricing', component: PricingComponent, canActivate: guarded() },
  { path: 'promotions', component: PromotionComponent, canActivate: guarded() },
  { path: 'inventory', component: InventoryComponent, canActivate: guarded('INVENTORY_VIEW') },
  { path: 'orders', component: OrdersAdminComponent, canActivate: guarded('ORDER_VIEW') },
  { path: 'fulfillment', component: FulfillmentComponent, canActivate: guarded('FULFILLMENT_VIEW') },
  { path: 'payments', component: PaymentsComponent, canActivate: guarded('PAYMENT_VIEW') },
  { path: 'returns', component: ReturnsAdminComponent, canActivate: guarded('RETURN_VIEW') },
  { path: 'moderation', component: ModerationComponent, canActivate: guarded('COMMENT_MODERATE') },
  { path: 'settlements', component: SettlementsComponent, canActivate: guarded('SETTLEMENT_VIEW') },
  { path: 'security', component: SecurityComponent, canActivate: guarded('SECURITY_VIEW') },
  { path: 'audit', component: AuditComponent, canActivate: guarded('AUDIT_VIEW') },
  { path: 'operations', component: OperationsComponent, canActivate: guarded('OPERATIONS_VIEW') },
  { path: '**', redirectTo: '' },
];
