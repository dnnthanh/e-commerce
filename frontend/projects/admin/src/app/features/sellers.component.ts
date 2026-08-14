import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ShopView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-sellers',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="SELLER CONTEXT"
        title="Shop management"
        description="Seller-scoped operators choose from their effective authorization scopes; global operators can enter an explicit seller ID and remain subject to backend authorization."
      />

      <div class="two-col">
        <div class="panel">
          <form class="filters" (ngSubmit)="load()">
            <label *ngIf="sellerScopes.length; else globalSellerLookup">
              Seller scope
              <select [(ngModel)]="sellerId" name="sellerId">
                <option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option>
              </select>
            </label>
            <ng-template #globalSellerLookup>
              <label>Seller ID<input type="number" min="1" [(ngModel)]="sellerId" name="sellerId"></label>
            </ng-template>
            <button type="submit" [disabled]="loading() || !sellerId">Load shops</button>
          </form>

          <admin-state
            *ngIf="failure() as problem"
            kind="error"
            title="Seller shops unavailable"
            [message]="problem.message"
            [traceId]="problem.traceId"
            [retryable]="true"
            (retry)="load()"
          />
          <admin-state *ngIf="loading()" kind="loading" title="Loading shops" message="Reading seller shop material data." />
          <admin-state *ngIf="!loading() && !failure() && shops().length === 0" kind="empty" title="No shops" message="No shop records were returned for this seller." />

          <div class="mini-row" *ngFor="let shop of shops()">
            <div>
              <b>{{ shop.name }}</b>
              <small>#{{ shop.id }} · {{ shop.slug }}</small>
              <p><admin-status-badge [status]="shop.status" /> · {{ shop.description || 'No description' }}</p>
            </div>
            <button *ngIf="auth.has('SELLER_UPDATE')" class="ghost" type="button" (click)="edit(shop)">Edit</button>
          </div>
        </div>

        <div class="panel" *ngIf="editing() as shop">
          <h2>Edit public material data</h2>
          <p class="hint">Shop #{{ shop.id }} · seller {{ shop.sellerId }}</p>
          <label>Name<input [(ngModel)]="editName" maxlength="180"></label>
          <label>Description<textarea [(ngModel)]="editDescription"></textarea></label>
          <div class="actions">
            <button type="button" [disabled]="saving() || !editName.trim()" (click)="save(shop)">Save</button>
            <button type="button" class="ghost" [disabled]="saving()" (click)="editing.set(undefined)">Cancel</button>
          </div>
          <p class="hint">Material changes stay in Seller service and can fan out through its existing outbox workflows.</p>
        </div>
      </div>
    </section>
  `,
})
export class SellersComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  sellerId?: number;
  readonly shops = signal<ShopView[]>([]);
  readonly editing = signal<ShopView | undefined>(undefined);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly saving = signal(false);
  editName = '';
  editDescription = '';

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  async ngOnInit(): Promise<void> {
    if (this.sellerScopes.length) this.sellerId = this.sellerScopes[0];
    if (this.sellerId) await this.load();
  }

  async load(): Promise<void> {
    if (!this.sellerId) return;
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      this.shops.set(await this.marketplace.sellerShops(this.sellerId));
    } catch (error) {
      this.shops.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  edit(shop: ShopView): void {
    this.editing.set(shop);
    this.editName = shop.name;
    this.editDescription = shop.description ?? '';
  }

  async save(shop: ShopView): Promise<void> {
    if (!this.editName.trim()) return;
    this.saving.set(true);
    this.failure.set(undefined);
    try {
      const updated = await this.marketplace.updateShop(
        shop.id,
        shop.sellerId,
        this.editName.trim(),
        this.editDescription.trim(),
      );
      this.shops.update(rows => rows.map(row => row.id === updated.id ? updated : row));
      this.editing.set(undefined);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.saving.set(false);
    }
  }
}
