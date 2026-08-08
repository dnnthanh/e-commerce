import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ProductView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { ProductCardComponent } from '../shared/product-card.component';
import { StatePanelComponent } from '../shared/state-panel.component';

/** Marketplace landing page backed by the public Catalog API. */
@Component({
  standalone: true,
  selector: 'market-home',
  imports: [CommonModule, RouterLink, ProductCardComponent, StatePanelComponent],
  template: `
    <section class="hero">
      <div>
        <span class="eyebrow">NOVA MARKET</span>
        <h1>Khám phá sản phẩm từ nhiều cửa hàng trong một nơi.</h1>
        <p>
          Chọn phiên bản thật từ Catalog, xem giá hiện hành và hoàn tất giỏ hàng với kiểm tra
          giá/tồn kho lại ở checkout.
        </p>
        <div class="hero-actions">
          <a class="primary" routerLink="/search">Khám phá sản phẩm</a>
          <a routerLink="/orders">Đơn hàng của tôi</a>
        </div>
      </div>
      <div class="hero-metrics">
        <strong>{{ products().length }}</strong><span>Sản phẩm đang hiển thị</span>
        <strong>{{ categoryIds().length }}</strong><span>Danh mục trong kết quả</span>
      </div>
    </section>

    <section class="category-strip" *ngIf="categoryIds().length">
      <div class="section-head compact"><div><small>DANH MỤC</small><h2>Mua theo danh mục</h2></div></div>
      <div class="category-links">
        <a *ngFor="let categoryId of categoryIds()" [routerLink]="['/category', categoryId]">
          Danh mục #{{ categoryId }}
        </a>
      </div>
    </section>

    <section class="section-head">
      <div><small>DISCOVERY</small><h2>Sản phẩm mới</h2></div>
      <a routerLink="/search">Xem tất cả →</a>
    </section>

    <market-state-panel *ngIf="loading()" kind="loading" message="Đang tải sản phẩm…" />
    <market-state-panel
      *ngIf="!loading() && error() as e"
      [kind]="e.kind"
      [message]="e.message"
      [traceId]="e.traceId"
    />
    <market-state-panel
      *ngIf="!loading() && !error() && products().length === 0"
      kind="empty"
      message="Chưa có sản phẩm được xuất bản trong catalog."
    />

    <section class="product-grid" *ngIf="!loading() && products().length">
      <market-product-card
        *ngFor="let product of products()"
        [productId]="product.id"
        [name]="product.name"
        [sellerId]="product.sellerId"
      />
    </section>
  `,
})
export class HomeComponent implements OnInit {
  readonly products = signal<ProductView[]>([]);
  readonly loading = signal(true);
  readonly error = signal<UiErrorView | undefined>(undefined);
  readonly categoryIds = computed(() => [
    ...new Set(this.products().map((product) => product.categoryId)),
  ]);

  constructor(private readonly marketplace: MarketplaceApiService) {}

  async ngOnInit(): Promise<void> {
    try {
      const page = await this.marketplace.products(0, 12);
      this.products.set(page.data);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
