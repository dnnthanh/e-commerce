import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../../shared/auth.service';
import { StorefrontCartStore } from './shared/storefront-cart.store';

/** Storefront application shell. Feature pages and route declarations live outside the root. */
@Component({
  standalone: true,
  selector: 'app-root',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <header class="topbar">
      <a routerLink="/" class="brand" aria-label="NOVA MARKET - Trang chủ">NOVA MARKET</a>
      <nav class="desktop-nav" aria-label="Điều hướng chính">
        <a routerLink="/search" routerLinkActive="active">Khám phá</a>
        <a routerLink="/orders" routerLinkActive="active" *ngIf="auth.authenticated()">Đơn hàng</a>
        <a routerLink="/notifications" routerLinkActive="active" *ngIf="auth.authenticated()">Thông báo</a>
      </nav>
      <div class="auth">
        <a class="cart-link" routerLink="/cart" *ngIf="auth.authenticated()">
          Giỏ hàng <span class="cart-count" *ngIf="cartStore.count()">{{ cartStore.count() }}</span>
        </a>
        <a routerLink="/account" *ngIf="auth.authenticated()">Tài khoản</a>
        <button *ngIf="!auth.authenticated()" (click)="auth.login()">Đăng nhập</button>
        <button class="ghost" *ngIf="auth.authenticated()" (click)="auth.logout()">Đăng xuất</button>
      </div>
    </header>

    <nav class="mobile-nav" aria-label="Điều hướng di động">
      <a routerLink="/">Trang chủ</a>
      <a routerLink="/search">Khám phá</a>
      <a routerLink="/cart" *ngIf="auth.authenticated()">Giỏ hàng ({{ cartStore.count() }})</a>
      <a routerLink="/orders" *ngIf="auth.authenticated()">Đơn hàng</a>
    </nav>

    <main><router-outlet /></main>

    <footer>
      <strong>NOVA MARKET</strong>
      <span>Marketplace đa nhà bán · giá và tồn kho được xác nhận lại khi checkout.</span>
    </footer>
  `,
})
export class AppComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly cartStore = inject(StorefrontCartStore);

  async ngOnInit(): Promise<void> {
    await this.auth.ensureInitialized();
    if (this.auth.authenticated()) {
      await this.cartStore.refresh().catch(() => undefined);
    }
  }
}
