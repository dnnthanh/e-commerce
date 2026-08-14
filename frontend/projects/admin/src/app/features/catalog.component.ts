import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ProductView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-catalog',
  imports: [CommonModule, FormsModule, RouterLink, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="CATALOG LIFECYCLE"
        title="Products"
        description="Search deterministic pages, create seller-scoped drafts and publish only through the existing catalog command."
      >
        <button actions type="button" class="ghost" [disabled]="loading()" (click)="load(page())">Refresh</button>
      </admin-page-header>

      <div class="two-col">
        <div class="panel" *ngIf="auth.has('PRODUCT_CREATE')">
          <h2>Create product draft</h2>
          <label *ngIf="sellerScopes.length; else manualSeller">
            Seller scope
            <select [(ngModel)]="sellerId">
              <option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option>
            </select>
          </label>
          <ng-template #manualSeller>
            <label>Seller ID<input type="number" min="1" [(ngModel)]="sellerId"></label>
          </ng-template>
          <label>Category ID<input type="number" min="1" [(ngModel)]="categoryId"></label>
          <label>Name<input [(ngModel)]="name" maxlength="220"></label>
          <label>Description<textarea [(ngModel)]="description"></textarea></label>
          <button type="button" [disabled]="creating() || !canCreate" (click)="create()">Create draft</button>
          <p class="hint">Category remains ID-based because Feature 015 does not invent category metadata that the current browser contract does not provide.</p>
        </div>

        <div class="panel">
          <h2>Catalog query</h2>
          <form class="filters" (ngSubmit)="load(0)">
            <label>Search<input [(ngModel)]="query" name="query" placeholder="Product name"></label>
            <button type="submit" [disabled]="loading()">Search</button>
          </form>

          <admin-state
            *ngIf="failure() as problem"
            kind="error"
            title="Catalog request failed"
            [message]="problem.message"
            [traceId]="problem.traceId"
            [retryable]="true"
            (retry)="load(page())"
          />
          <admin-state *ngIf="loading()" kind="loading" title="Loading products" message="Reading the current catalog page." />
          <admin-state *ngIf="!loading() && !failure() && products().length === 0" kind="empty" title="No products" message="No products matched the current search." />

          <div class="mini-row" *ngFor="let product of products()">
            <div>
              <b>{{ product.name }}</b>
              <small>#{{ product.id }} · seller {{ product.sellerId }} · category {{ product.categoryId }}</small>
              <p><admin-status-badge [status]="product.status" /> · {{ product.updatedAt | date:'short' }}</p>
            </div>
            <div class="actions">
              <a class="button-link ghost" [routerLink]="['/media']" [queryParams]="{ productId: product.id, sellerId: product.sellerId }">Media</a>
              <button
                *ngIf="product.status !== 'PUBLISHED' && auth.has('PRODUCT_PUBLISH')"
                type="button"
                [disabled]="publishingId() === product.id"
                (click)="publish(product)"
              >Publish</button>
            </div>
          </div>

          <div class="row" *ngIf="!loading() && !failure()">
            <button type="button" class="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">Prev</button>
            <span>page {{ page() + 1 }} / {{ totalPages() }}</span>
            <button type="button" class="ghost" [disabled]="!hasNext()" (click)="load(page() + 1)">Next</button>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class CatalogComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  readonly products = signal<ProductView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly creating = signal(false);
  readonly publishingId = signal<number | undefined>(undefined);
  readonly page = signal(0);
  readonly totalPages = signal(1);
  readonly hasNext = signal(false);
  sellerId?: number;
  categoryId = 1;
  name = '';
  description = '';
  query = '';

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  get canCreate(): boolean {
    return !!this.sellerId && this.categoryId > 0 && !!this.name.trim();
  }

  async ngOnInit(): Promise<void> {
    if (this.sellerScopes.length) this.sellerId = this.sellerScopes[0];
    await this.load(0);
  }

  async load(targetPage: number): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.products(Math.max(0, targetPage), 25, this.query.trim());
      this.products.set(result.data);
      this.page.set(result.metadata.page);
      this.totalPages.set(result.metadata.totalPages);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async create(): Promise<void> {
    if (!this.canCreate) return;
    this.creating.set(true);
    this.failure.set(undefined);
    try {
      await this.marketplace.createProduct({
        sellerId: this.sellerId!,
        categoryId: this.categoryId,
        name: this.name.trim(),
        description: this.description.trim(),
      });
      this.name = '';
      this.description = '';
      await this.load(0);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.creating.set(false);
    }
  }

  async publish(product: ProductView): Promise<void> {
    if (!confirm(`Publish product #${product.id} “${product.name}”?`)) return;
    this.publishingId.set(product.id);
    this.failure.set(undefined);
    try {
      const updated = await this.marketplace.publishProduct(product.id);
      this.products.update(rows => rows.map(row => row.id === updated.id ? updated : row));
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.publishingId.set(undefined);
    }
  }
}
