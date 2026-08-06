import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CursorMetadata } from '../../../../../shared/api-contract';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { SearchResponse } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { ProductCardComponent } from '../shared/product-card.component';
import { StatePanelComponent } from '../shared/state-panel.component';

@Component({
  standalone: true,
  selector: 'market-search',
  imports: [CommonModule, FormsModule, ProductCardComponent, StatePanelComponent],
  template: `
    <section class="page">
      <div class="page-title">
        <div><small>KHÁM PHÁ</small><h1>Tìm sản phẩm</h1></div>
      </div>

      <form class="search-bar" (ngSubmit)="runNewSearch()">
        <input
          [(ngModel)]="query"
          name="query"
          aria-label="Từ khóa tìm kiếm"
          placeholder="Điện thoại, laptop, phụ kiện…"
        />
        <select [(ngModel)]="size" name="size" aria-label="Số sản phẩm mỗi trang">
          <option [ngValue]="12">12 sản phẩm</option>
          <option [ngValue]="24">24 sản phẩm</option>
          <option [ngValue]="48">48 sản phẩm</option>
        </select>
        <button>Tìm kiếm</button>
      </form>

      <market-state-panel *ngIf="loading()" kind="loading" message="Đang tìm sản phẩm…" />
      <market-state-panel
        *ngIf="!loading() && error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />
      <market-state-panel
        *ngIf="!loading() && !error() && result() && result()!.items.length === 0"
        kind="empty"
        message="Không tìm thấy sản phẩm phù hợp với từ khóa hiện tại."
      />

      <div class="search-layout" *ngIf="!loading() && result() as result">
        <aside *ngIf="result.items.length">
          <h3>Bộ lọc kết quả</h3>
          <p>Tổng <b>{{ metadata()?.total | number }}</b></p>
          <div *ngFor="let facet of entries(result.facets)">
            <strong>{{ facet[0] }}</strong>
            <p *ngFor="let value of entries(facet[1])">
              {{ value[0] }} <span>{{ value[1] }}</span>
            </p>
          </div>
        </aside>

        <section class="product-grid" *ngIf="result.items.length">
          <market-product-card
            *ngFor="let product of result.items"
            [productId]="product.productId"
            [name]="product.name"
            [sellerId]="product.sellerId"
            [price]="product.price"
            [rating]="product.rating"
          />
        </section>
      </div>

      <div class="pager" *ngIf="result()?.items?.length">
        <button class="ghost" [disabled]="cursorHistory.length === 0" (click)="previous()">← Trước</button>
        <span>{{ metadata()?.total | number }} kết quả</span>
        <button class="ghost" [disabled]="!metadata()?.hasNext" (click)="next()">Sau →</button>
      </div>
    </section>
  `,
})
export class SearchComponent implements OnInit {
  query = '';
  size = 24;
  readonly result = signal<SearchResponse | undefined>(undefined);
  readonly metadata = signal<CursorMetadata | undefined>(undefined);
  readonly loading = signal(false);
  readonly error = signal<UiErrorView | undefined>(undefined);
  cursorHistory: Array<string | undefined> = [];
  private currentCursor?: string;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly marketplace: MarketplaceApiService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.query = this.route.snapshot.queryParamMap.get('q') ?? '';
    const routeSize = Number(this.route.snapshot.queryParamMap.get('size'));
    if ([12, 24, 48].includes(routeSize)) this.size = routeSize;
    await this.search();
  }

  async runNewSearch(): Promise<void> {
    this.cursorHistory = [];
    this.currentCursor = undefined;
    await this.router.navigate(['/search'], {
      queryParams: { q: this.query || null, size: this.size },
    });
    await this.search();
  }

  async next(): Promise<void> {
    const nextCursor = this.metadata()?.nextCursor;
    if (!nextCursor) return;
    this.cursorHistory.push(this.currentCursor);
    this.currentCursor = nextCursor;
    await this.search();
  }

  async previous(): Promise<void> {
    if (!this.cursorHistory.length) return;
    this.currentCursor = this.cursorHistory.pop();
    await this.search();
  }

  entries<T>(value?: Record<string, T>): Array<[string, T]> {
    return Object.entries(value ?? {});
  }

  private async search(): Promise<void> {
    this.loading.set(true);
    try {
      const page = await this.marketplace.search(this.query, this.currentCursor, this.size);
      this.result.set(page.data);
      this.metadata.set(page.metadata);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
