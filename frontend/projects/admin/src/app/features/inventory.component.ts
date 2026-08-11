import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { BalanceView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';

@Component({
  standalone: true,
  selector: 'admin-inventory',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="INVENTORY / RESERVATION"
        title="Balances"
        description="Observe stock balances and optimistic version contention without exposing internal reservation commands to the browser."
      >
        <button actions type="button" class="ghost" [disabled]="loading()" (click)="load(page())">Refresh</button>
      </admin-page-header>

      <form class="filters" (ngSubmit)="load(0)">
        <label>SKU<input type="number" min="1" [(ngModel)]="skuId" name="sku" placeholder="Optional"></label>
        <label>Warehouse<input type="number" min="1" [(ngModel)]="warehouseId" name="warehouse" placeholder="Optional"></label>
        <button type="submit" [disabled]="loading()">Filter</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Inventory balances unavailable"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load(page())"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading balances" message="Reading current inventory balances." />
      <admin-state *ngIf="!loading() && !failure() && balances().length === 0" kind="empty" title="No balances" message="No balance rows matched the current filters." />

      <div class="panel" *ngIf="balances().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>SKU</th><th>Warehouse</th><th>On hand</th><th>Reserved</th><th>Available</th><th>Version</th></tr></thead>
            <tbody>
              <tr *ngFor="let balance of balances()">
                <td>{{ balance.skuId }}</td>
                <td>{{ balance.warehouseId }}</td>
                <td>{{ balance.onHand }}</td>
                <td>{{ balance.reserved }}</td>
                <td><b>{{ balance.available }}</b></td>
                <td>{{ balance.version }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="row">
          <button type="button" class="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">Prev</button>
          <span>page {{ page() + 1 }} / {{ totalPages() }}</span>
          <button type="button" class="ghost" [disabled]="!hasNext()" (click)="load(page() + 1)">Next</button>
        </div>
      </div>
    </section>
  `,
})
export class InventoryComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  skuId?: number;
  warehouseId?: number;
  readonly balances = signal<BalanceView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly page = signal(0);
  readonly totalPages = signal(1);
  readonly hasNext = signal(false);

  async ngOnInit(): Promise<void> {
    await this.load(0);
  }

  async load(targetPage: number): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.balances(
        Math.max(0, targetPage),
        50,
        this.skuId,
        this.warehouseId,
      );
      this.balances.set(result.data);
      this.page.set(result.metadata.page);
      this.totalPages.set(result.metadata.totalPages);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.balances.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
