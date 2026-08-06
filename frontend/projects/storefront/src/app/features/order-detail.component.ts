import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView, ShipmentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { StatePanelComponent } from '../shared/state-panel.component';

@Component({
  standalone: true,
  selector: 'market-order-detail',
  imports: [CommonModule, RouterLink, StatePanelComponent],
  template: `
    <section class="page">
      <div class="page-title">
        <div><small>CHI TIẾT ĐƠN HÀNG</small><h1>{{ order()?.orderNo || 'Đơn hàng' }}</h1></div>
        <a routerLink="/orders">← Lịch sử đơn hàng</a>
      </div>

      <market-state-panel *ngIf="loading()" kind="loading" message="Đang tải đơn hàng và vận chuyển…" />
      <market-state-panel
        *ngIf="!loading() && error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />

      <div class="detail-columns" *ngIf="!loading() && order() as currentOrder">
        <section class="panel">
          <div class="row wrap-row">
            <span class="status-chip">{{ statusLabel(currentOrder.status) }}</span>
            <small>{{ currentOrder.createdAt | date: 'medium' }}</small>
          </div>
          <h2>Tổng quan thanh toán</h2>
          <div class="summary-row"><span>Tạm tính</span><span>{{ currentOrder.grossAmount | number: '1.0-0' }} ₫</span></div>
          <div class="summary-row"><span>Khuyến mại</span><span>-{{ currentOrder.discountAmount | number: '1.0-0' }} ₫</span></div>
          <div class="summary-row total-row"><span>Thành tiền</span><strong>{{ currentOrder.payableAmount | number: '1.0-0' }} ₫</strong></div>

          <h3>Đơn theo cửa hàng</h3>
          <div class="seller-order-card" *ngFor="let sellerOrder of currentOrder.sellerOrders">
            <div>
              <strong>Cửa hàng #{{ sellerOrder.sellerId }}</strong>
              <small *ngIf="sellerOrder.sellerOrderNo">{{ sellerOrder.sellerOrderNo }}</small>
            </div>
            <div class="order-money-inline">
              <span class="status-chip">{{ statusLabel(sellerOrder.status || currentOrder.status) }}</span>
              <strong>{{ sellerOrder.payableAmount || 0 | number: '1.0-0' }} ₫</strong>
            </div>
          </div>

          <div class="cancel-box" *ngIf="canCancel(currentOrder)">
            <p class="hint">
              Đơn chưa thanh toán có thể hủy theo yêu cầu khách hàng. Backend sẽ từ chối nếu trạng thái
              thay đổi trước khi yêu cầu được xử lý.
            </p>
            <button class="ghost" (click)="cancel()">Yêu cầu hủy đơn</button>
          </div>
          <p class="hint" *ngIf="!canCancel(currentOrder)">
            Trạng thái hiện tại không cho phép khách hàng hủy đơn.
          </p>
        </section>

        <section class="panel">
          <small>VẬN CHUYỂN</small>
          <h2>Theo dõi giao hàng</h2>
          <div class="shipment-timeline" *ngIf="shipments().length; else waitingShipment">
            <div class="shipment-step" *ngFor="let shipment of shipments()">
              <span class="timeline-dot"></span>
              <div>
                <strong>{{ shipment.shipmentNo }}</strong>
                <p>{{ statusLabel(shipment.status) }}</p>
                <small *ngIf="shipment.carrierCode || shipment.trackingNo">
                  {{ shipment.carrierCode || 'Đơn vị vận chuyển' }}
                  <span *ngIf="shipment.trackingNo"> · {{ shipment.trackingNo }}</span>
                </small>
                <small>Cập nhật {{ shipment.updatedAt | date: 'medium' }}</small>
              </div>
            </div>
          </div>
          <ng-template #waitingShipment>
            <div class="empty-inline">
              <strong>Đang chuẩn bị hàng</strong>
              <p>Shipment sẽ xuất hiện tại đây sau khi fulfillment bắt đầu xử lý.</p>
            </div>
          </ng-template>
          <a class="link-row" routerLink="/returns">Cần trả hàng? Mở trung tâm trả hàng →</a>
        </section>
      </div>
    </section>
  `,
})
export class OrderDetailComponent implements OnInit {
  readonly order = signal<OrderView | undefined>(undefined);
  readonly shipments = signal<ShipmentView[]>([]);
  readonly loading = signal(true);
  readonly error = signal<UiErrorView | undefined>(undefined);
  private orderNo = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly marketplace: MarketplaceApiService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.orderNo = this.route.snapshot.paramMap.get('orderNo') ?? '';
    await this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    try {
      const [order, shipments] = await Promise.all([
        this.marketplace.order(this.orderNo),
        this.marketplace.shipments(this.orderNo),
      ]);
      this.order.set(order);
      this.shipments.set(shipments);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  canCancel(order: OrderView): boolean {
    return ['CREATED', 'PAYMENT_PENDING'].includes(order.status);
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      CREATED: 'Đã tạo',
      PAYMENT_PENDING: 'Chờ thanh toán',
      PAID: 'Đã thanh toán',
      FULFILLING: 'Đang thực hiện',
      ALLOCATED: 'Đã phân bổ kho',
      PICKING: 'Đang lấy hàng',
      PACKED: 'Đã đóng gói',
      IN_TRANSIT: 'Đang vận chuyển',
      DELIVERED: 'Đã giao',
      COMPLETED: 'Hoàn tất',
      CANCELLED: 'Đã hủy',
      EXPIRED: 'Đã hết hạn thanh toán',
    };
    return labels[status] ?? status;
  }

  async cancel(): Promise<void> {
    try {
      this.order.set(await this.marketplace.cancelOrder(this.orderNo, 'CUSTOMER_REQUEST'));
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }
}
