import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Routes } from '@angular/router';
import { AuthService } from '../../../../shared/auth.service';
import { authGuard } from '../../../../shared/auth.guard';
import { HomeComponent } from './features/home.component';
import { SearchComponent } from './features/search.component';
import { ProductDetailComponent } from './features/product-detail.component';
import { CartComponent } from './features/cart.component';
import { CheckoutComponent } from './features/checkout.component';
import { OrdersComponent } from './features/orders.component';
import { ReturnsComponent } from './features/returns.component';
import { NotificationsComponent } from './features/notifications.component';
import { OrderDetailComponent } from './features/order-detail.component';
import { CommunityComponent } from './features/community.component';
import { AccountComponent } from './features/account.component';
import { SellerShopComponent } from './features/seller-shop.component';
/** Storefront application shell. */
@Component({
    standalone: true,
    selector: 'app-root',
    imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
    template: `
    <header class="topbar">
      <a routerLink="/" class="brand">NOVA MARKET</a>
      <nav><a routerLink="/search" routerLinkActive="active">Tìm kiếm</a><a routerLink="/cart" routerLinkActive="active">Giỏ hàng</a><a routerLink="/orders" routerLinkActive="active">Đơn hàng</a><a routerLink="/returns" routerLinkActive="active">Trả hàng</a><a routerLink="/notifications" routerLinkActive="active">Thông báo</a><a routerLink="/account" routerLinkActive="active">Tài khoản</a></nav>
      <div class="auth"><span *ngIf="auth.authenticated()">{{auth.authorization().roles.join(', ')}}</span><button *ngIf="!auth.authenticated()" (click)="auth.login()">Đăng nhập</button><button class="ghost" *ngIf="auth.authenticated()" (click)="auth.logout()">Đăng xuất</button></div>
    </header>
    <main><router-outlet/></main>
    <footer><strong>NOVA MARKET</strong><span>Keycloak · Kafka · Multi-DB · OpenSearch · Realtime notification</span></footer>`
})
export class AppComponent {
    /** Keycloak authentication facade. */
    readonly auth = inject(AuthService);
}
/** Storefront routes. */
export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'search', component: SearchComponent },
    { path: 'product/:id', component: ProductDetailComponent },
    { path: 'cart', component: CartComponent, canActivate: [authGuard] },
    { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
    { path: 'orders', component: OrdersComponent, canActivate: [authGuard] },
    { path: 'orders/:orderNo', component: OrderDetailComponent, canActivate: [authGuard] },
    { path: 'returns', component: ReturnsComponent, canActivate: [authGuard] },
    { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
    { path: 'product/:id/community', component: CommunityComponent },
    { path: 'shops/:shopId', component: SellerShopComponent },
    { path: 'account', component: AccountComponent, canActivate: [authGuard] },
    { path: '**', redirectTo: '' }
];
