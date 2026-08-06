import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { StatePanelComponent } from '../shared/state-panel.component';

@Component({
  standalone: true,
  selector: 'market-orders',
  imports: [CommonModule, RouterLink, StatePanelComponent],
  template: `
    <section class="page">
      <div class="page-title"><div><small>ĐƠN HÀNG</small><h1>Lịch sử mua hàng</h1></div></div>

      <market-state-panel *ngIf="loading()" kind="loading" message="Đang tải đơn hàng…" />
      <market-state-panel
        *ngIf="!loading() && error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />
      <market-state-panel
        *ngIf="!loading() && !error() && orders().length === 0"
        kind="empty"
        message="Bạn chưa có đơn hàng nào."
      />

      <div class="order-card" *ngFor="let order of orders()">
        <div class="order-main">
          <div class="row wrap-row">
            <strong>{{ order.orderNo }}</strong>
            <span class="status-chip">{{ statusLabel(order.status) }}</span>
          </div>
          <small>{{ order.createdAt | date: 'medium' }}</small>
          <span>{{ order.sellerOrders.length }} cửa hàng trong đơn</span>
        </div>
        <div class="order-money">
          <span>Tạm tính {{ order.grossAmount | number: '1.0-0' }} ₫</span>
          <span *ngIf="order.discountAmount">Giảm {{ order.discountAmount | number: '1.0-0' }} ₫</span>
          <b>{{ order.payableAmount | number: '1.0-0' }} ₫</b>
          <a [routerLink]="['/orders', order.orderNo]">Xem chi tiết & tracking →</a>
        </div>
      </div>

      <div class="pager" *ngIf="orders().length">
        <button class="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">← Trước</button>
        <span>Trang {{ page() + 1 }} / {{ totalPages() }}</span>
        <button class="ghost" [disabled]="!hasNext()" (click)="load(page() + 1)">Sau →</button>
      </div>
    </section>
  `,
})
export class OrdersComponent implements OnInit {
  readonly orders = signal<OrderView[]>([]);
  readonly error = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(true);
  readonly page = signal(0);
  readonly totalPages = signal(1);
  readonly hasNext = signal(false);

  constructor(private readonly marketplace: MarketplaceApiService) {}

  async ngOnInit(): Promise<void> {
    await this.load(0);
  }

  async load(page: number): Promise<void> {
    this.loading.set(true);
    try {
      const result = await this.marketplace.orders(page, 20);
      this.orders.set(result.data);
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

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      CREATED: 'Đã tạo',
      PAYMENT_PENDING: 'Chờ thanh toán',
      PAID: 'Đã thanh toán',
      FULFILLING: 'Đang thực hiện',
      COMPLETED: 'Hoàn tất',
      CANCELLED: 'Đã hủy',
      EXPIRED: 'Đã hết hạn thanh toán',
    };
    return labels[status] ?? status;
  }
}
