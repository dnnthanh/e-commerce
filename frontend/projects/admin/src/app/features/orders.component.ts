import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView, ShipmentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-orders',
  imports: [CommonModule, FormsModule, RouterLink, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="ORDER 360"
        title="Orders"
        description="Browse deterministic order pages, drill into seller-order context and follow the same order into fulfillment."
      >
        <button actions type="button" class="ghost" [disabled]="loading()" (click)="loadPage(page())">Refresh</button>
      </admin-page-header>

      <form class="filters" (ngSubmit)="loadDetail(orderNo)">
        <label>Exact order number<input [(ngModel)]="orderNo" name="orderNo" placeholder="Order no"></label>
        <button type="submit" [disabled]="!orderNo.trim() || detailLoading()">Open order</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Order request failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="loadPage(page())"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading orders" message="Reading the current order page." />
      <admin-state *ngIf="!loading() && !failure() && orders().length === 0" kind="empty" title="No orders" message="No orders were returned for this page." />

      <div class="panel" *ngIf="orders().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>Order</th><th>Status</th><th>Gross</th><th>Discount</th><th>Payable</th><th>Created</th><th></th></tr></thead>
            <tbody>
              <tr *ngFor="let row of orders()">
                <td><b>{{ row.orderNo }}</b><small>{{ row.sellerOrders.length }} seller order(s)</small></td>
                <td><admin-status-badge [status]="row.status" /></td>
                <td>{{ row.grossAmount | number }} ₫</td>
                <td>{{ row.discountAmount | number }} ₫</td>
                <td><b>{{ row.payableAmount | number }} ₫</b></td>
                <td>{{ row.createdAt | date:'short' }}</td>
                <td><button type="button" class="ghost" (click)="loadDetail(row.orderNo)">Details</button></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="row">
          <button type="button" class="ghost" [disabled]="page() === 0" (click)="loadPage(page() - 1)">Prev</button>
          <span>page {{ page() + 1 }}</span>
          <button type="button" class="ghost" [disabled]="!hasNext()" (click)="loadPage(page() + 1)">Next</button>
        </div>
      </div>

      <div class="panel" *ngIf="selected() as current">
        <div class="panel-title">
          <div><h2>{{ current.orderNo }}</h2><admin-status-badge [status]="current.status" /></div>
          <div class="actions">
            <a class="button-link ghost" [routerLink]="['/fulfillment']" [queryParams]="{ orderNo: current.orderNo }">Fulfillment</a>
            <a class="button-link ghost" routerLink="/payments">Payments</a>
            <a class="button-link ghost" routerLink="/returns">Returns</a>
          </div>
        </div>
        <div class="cards">
          <article><span>Gross</span><strong>{{ current.grossAmount | number }}</strong><small>VND</small></article>
          <article><span>Discount</span><strong>{{ current.discountAmount | number }}</strong><small>VND</small></article>
          <article><span>Payable</span><strong>{{ current.payableAmount | number }}</strong><small>VND</small></article>
          <article><span>Updated</span><strong>{{ current.updatedAt | date:'shortTime' }}</strong><small>{{ current.updatedAt | date:'mediumDate' }}</small></article>
        </div>
        <h3>Seller orders</h3>
        <div class="mini-row" *ngFor="let sellerOrder of current.sellerOrders">
          <div><b>{{ sellerOrder.sellerOrderNo || 'Seller order' }}</b><small>seller {{ sellerOrder.sellerId }}</small></div>
          <admin-status-badge [status]="sellerOrder.status || 'UNKNOWN'" />
        </div>
        <h3>Shipments</h3>
        <admin-state *ngIf="detailLoading()" kind="loading" title="Loading fulfillment" message="Reading shipments for this order." />
        <admin-state *ngIf="!detailLoading() && shipments().length === 0" kind="empty" title="No shipments yet" message="Fulfillment has not returned a shipment for this order." />
        <div class="mini-row" *ngFor="let shipment of shipments()">
          <div><b>{{ shipment.shipmentNo }}</b><small>seller {{ shipment.sellerId }} · warehouse {{ shipment.warehouseId }}</small></div>
          <admin-status-badge [status]="shipment.status" />
        </div>
      </div>
    </section>
  `,
})
export class OrdersAdminComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  orderNo = '';
  readonly orders = signal<OrderView[]>([]);
  readonly selected = signal<OrderView | undefined>(undefined);
  readonly shipments = signal<ShipmentView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly detailLoading = signal(false);
  readonly page = signal(0);
  readonly hasNext = signal(false);

  async ngOnInit(): Promise<void> {
    await this.loadPage(0);
  }

  async loadPage(targetPage: number): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.orders(Math.max(0, targetPage), 25);
      this.orders.set(result.data);
      this.page.set(result.metadata.page);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async loadDetail(value: string): Promise<void> {
    const key = value.trim();
    if (!key) return;
    this.orderNo = key;
    this.detailLoading.set(true);
    this.failure.set(undefined);
    try {
      const [order, shipments] = await Promise.all([
        this.marketplace.order(key),
        this.marketplace.shipments(key),
      ]);
      this.selected.set(order);
      this.shipments.set(shipments);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.detailLoading.set(false);
    }
  }
}
