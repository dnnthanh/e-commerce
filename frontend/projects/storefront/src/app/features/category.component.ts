import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ProductView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { ProductCardComponent } from '../shared/product-card.component';
import { StatePanelComponent } from '../shared/state-panel.component';

/** Public Catalog category browsing backed by the existing grouped ProductSearchRequest. */
@Component({
  standalone: true,
  selector: 'market-category',
  imports: [CommonModule, RouterLink, ProductCardComponent, StatePanelComponent],
  template: `
    <section class="page">
      <div class="page-title">
        <div><small>DANH MỤC</small><h1>Danh mục #{{ categoryId }}</h1></div>
        <a routerLink="/search">Tìm kiếm nâng cao →</a>
      </div>

      <market-state-panel *ngIf="loading()" kind="loading" message="Đang tải danh mục…" />
      <market-state-panel
        *ngIf="!loading() && error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />
      <market-state-panel
        *ngIf="!loading() && !error() && products().length === 0"
        kind="empty"
        message="Danh mục này chưa có sản phẩm được xuất bản."
      />

      <section class="product-grid" *ngIf="!loading() && products().length">
        <market-product-card
          *ngFor="let product of products()"
          [productId]="product.id"
          [name]="product.name"
          [sellerId]="product.sellerId"
        />
      </section>

      <div class="pager" *ngIf="products().length">
        <button class="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">← Trước</button>
        <span>Trang {{ page() + 1 }} / {{ totalPages() }}</span>
        <button class="ghost" [disabled]="!hasNext()" (click)="load(page() + 1)">Sau →</button>
      </div>
    </section>
  `,
})
export class CategoryComponent implements OnInit {
  categoryId = 0;
  readonly products = signal<ProductView[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(1);
  readonly hasNext = signal(false);
  readonly loading = signal(true);
  readonly error = signal<UiErrorView | undefined>(undefined);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly marketplace: MarketplaceApiService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.categoryId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(this.categoryId) || this.categoryId <= 0) {
      this.loading.set(false);
      this.error.set({ kind: 'not-found', message: 'Danh mục không hợp lệ.' });
      return;
    }
    await this.load(0);
  }

  async load(page: number): Promise<void> {
    this.loading.set(true);
    try {
      const result = await this.marketplace.products(page, 24, { categoryId: this.categoryId });
      this.products.set(result.data);
      this.page.set(result.metadata.page);
      this.totalPages.set(Math.max(1, result.metadata.totalPages));
      this.hasNext.set(result.metadata.hasNext);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
