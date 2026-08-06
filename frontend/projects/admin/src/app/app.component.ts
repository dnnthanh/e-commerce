import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Routes } from '@angular/router';
import { AuthService } from '../../../../shared/auth.service';
import { authGuard, permissionGuard } from '../../../../shared/auth.guard';
import { DashboardComponent } from './features/dashboard.component';
import { OperationsComponent } from './features/operations.component';
import { AuditComponent } from './features/audit.component';
import { SecurityComponent } from './features/security.component';
import { CatalogComponent } from './features/catalog.component';
import { InventoryComponent } from './features/inventory.component';
import { PaymentsComponent } from './features/payments.component';
import { SellersComponent } from './features/sellers.component';
import { SettlementsComponent } from './features/settlements.component';
import { MediaComponent } from './features/media.component';
import { PricingComponent } from './features/pricing.component';
import { PromotionComponent } from './features/promotion.component';
import { OrdersAdminComponent } from './features/orders.component';
import { FulfillmentComponent } from './features/fulfillment.component';
import { ReturnsAdminComponent } from './features/returns.component';
import { ModerationComponent } from './features/moderation.component';
/** Administration application shell. */
@Component({
    standalone: true,
    selector: 'app-root',
    imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
    template: `<div class="shell">
    <aside><a class="logo" routerLink="/">NOVA OPS</a><small>CONTROL PLANE</small>
      <nav>
        <a routerLink="/" [routerLinkActiveOptions]="{exact:true}" routerLinkActive="active">Dashboard</a>
        <a routerLink="/sellers" routerLinkActive="active">Sellers</a>
        <a routerLink="/catalog" routerLinkActive="active">Catalog</a>
        <a routerLink="/media" routerLinkActive="active">Media</a>
        <a routerLink="/pricing" routerLinkActive="active">Pricing</a>
        <a routerLink="/promotions" routerLinkActive="active">Promotions</a>
        <a routerLink="/inventory" routerLinkActive="active">Inventory</a>
        <a routerLink="/orders" routerLinkActive="active">Orders</a>
        <a routerLink="/fulfillment" routerLinkActive="active">Fulfillment</a>
        <a routerLink="/payments" routerLinkActive="active">Payments</a>
        <a routerLink="/returns" routerLinkActive="active">Returns</a>
        <a routerLink="/moderation" routerLinkActive="active">Moderation</a>
        <a routerLink="/settlements" routerLinkActive="active">Settlement</a>
        <a routerLink="/security" routerLinkActive="active">Security</a>
        <a routerLink="/audit" routerLinkActive="active">Audit</a>
        <a routerLink="/operations" routerLinkActive="active">Operations</a>
      </nav></aside>
    <main><header><div><span class="env">LOCAL PRODUCTION-LIKE</span></div><div class="header-actions"><span *ngIf="auth.authenticated()">{{auth.authorization().roles.join(', ')}}</span><button *ngIf="!auth.authenticated()" (click)="auth.login()">Admin login</button><button class="ghost" *ngIf="auth.authenticated()" (click)="auth.logout()">Logout</button></div></header><router-outlet/></main>
  </div>`
})
export class AppComponent {
    readonly auth = inject(AuthService);
}
/** Admin routes with backend-matching permission guards. */
export const routes: Routes = [
    { path: '', component: DashboardComponent, canActivate: [authGuard] },
    { path: 'sellers', component: SellersComponent, canActivate: [authGuard, permissionGuard('SELLER_VIEW')] },
    { path: 'catalog', component: CatalogComponent, canActivate: [authGuard, permissionGuard('PRODUCT_PUBLISH')] },
    { path: 'media', component: MediaComponent, canActivate: [authGuard, permissionGuard('MEDIA_UPLOAD')] },
    { path: 'pricing', component: PricingComponent, canActivate: [authGuard] },
    { path: 'promotions', component: PromotionComponent, canActivate: [authGuard] },
    { path: 'inventory', component: InventoryComponent, canActivate: [authGuard, permissionGuard('INVENTORY_VIEW')] },
    { path: 'orders', component: OrdersAdminComponent, canActivate: [authGuard, permissionGuard('ORDER_VIEW')] },
    { path: 'fulfillment', component: FulfillmentComponent, canActivate: [authGuard, permissionGuard('FULFILLMENT_VIEW')] },
    { path: 'payments', component: PaymentsComponent, canActivate: [authGuard, permissionGuard('PAYMENT_VIEW')] },
    { path: 'returns', component: ReturnsAdminComponent, canActivate: [authGuard, permissionGuard('RETURN_VIEW')] },
    { path: 'moderation', component: ModerationComponent, canActivate: [authGuard, permissionGuard('COMMENT_MODERATE')] },
    { path: 'settlements', component: SettlementsComponent, canActivate: [authGuard, permissionGuard('SETTLEMENT_VIEW')] },
    { path: 'security', component: SecurityComponent, canActivate: [authGuard, permissionGuard('SECURITY_VIEW')] },
    { path: 'audit', component: AuditComponent, canActivate: [authGuard, permissionGuard('AUDIT_VIEW')] },
    { path: 'operations', component: OperationsComponent, canActivate: [authGuard, permissionGuard('OPERATIONS_VIEW')] },
    { path: '**', redirectTo: '' }
];
