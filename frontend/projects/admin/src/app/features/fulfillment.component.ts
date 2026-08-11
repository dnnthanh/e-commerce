import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ShipmentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

const SHIPMENT_TRANSITIONS: Record<string, string[]> = {
  ALLOCATED: ['PICKING', 'CANCELLED'],
  PICKING: ['PACKED', 'CANCELLED'],
  PACKED: ['READY_TO_SHIP', 'CANCELLED'],
  READY_TO_SHIP: ['HANDED_OVER', 'CANCELLED'],
  HANDED_OVER: ['IN_TRANSIT'],
  IN_TRANSIT: ['DELIVERED', 'DELIVERY_FAILED'],
  DELIVERY_FAILED: ['IN_TRANSIT', 'RETURN_TO_SENDER'],
  DELIVERED: [],
  RETURN_TO_SENDER: [],
  CANCELLED: [],
};

@Component({
  standalone: true,
  selector: 'admin-fulfillment',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="FULFILLMENT STATE MACHINE"
        title="Shipments"
        description="Only transitions allowed by the current ShipmentStatus state machine are offered. Backend domain validation remains authoritative."
      />
      <form class="filters" (ngSubmit)="load()">
        <label>Order number<input [(ngModel)]="orderNo" name="orderNo" placeholder="Order no"></label>
        <button type="submit" [disabled]="loading() || !orderNo.trim()">Load shipments</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Fulfillment request failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load()"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading shipments" message="Reading fulfillment state for this order." />
      <admin-state *ngIf="!loading() && loaded() && !failure() && shipments().length === 0" kind="empty" title="No shipments" message="No fulfillment packages were returned for this order." />

      <div class="panel" *ngIf="shipments().length">
        <div class="mini-row" *ngFor="let shipment of shipments()">
          <div>
            <b>{{ shipment.shipmentNo }}</b>
            <small>Seller {{ shipment.sellerId }} · warehouse {{ shipment.warehouseId }}</small>
            <p><admin-status-badge [status]="shipment.status" /> · {{ shipment.carrierCode || 'carrier pending' }} · {{ shipment.trackingNo || 'tracking pending' }}</p>
          </div>
          <div class="actions" *ngIf="nextStatuses(shipment.status).length; else terminalState">
            <select [(ngModel)]="nextStatus[shipment.shipmentNo]" [ngModelOptions]="{ standalone: true }">
              <option *ngFor="let status of nextStatuses(shipment.status)" [value]="status">{{ status }}</option>
            </select>
            <input [(ngModel)]="carrierCode[shipment.shipmentNo]" [ngModelOptions]="{ standalone: true }" placeholder="Carrier">
            <input [(ngModel)]="trackingNo[shipment.shipmentNo]" [ngModelOptions]="{ standalone: true }" placeholder="Tracking no">
            <button
              type="button"
              [disabled]="pendingShipment() === shipment.shipmentNo || !nextStatus[shipment.shipmentNo]"
              (click)="advance(shipment)"
            >Transition</button>
          </div>
          <ng-template #terminalState><span class="hint">Terminal state</span></ng-template>
        </div>
      </div>
    </section>
  `,
})
export class FulfillmentComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  private readonly route = inject(ActivatedRoute);
  orderNo = '';
  readonly shipments = signal<ShipmentView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly loaded = signal(false);
  readonly pendingShipment = signal<string | undefined>(undefined);
  nextStatus: Record<string, string> = {};
  carrierCode: Record<string, string> = {};
  trackingNo: Record<string, string> = {};

  async ngOnInit(): Promise<void> {
    this.orderNo = this.route.snapshot.queryParamMap.get('orderNo') ?? '';
    if (this.orderNo) await this.load();
  }

  async load(): Promise<void> {
    const key = this.orderNo.trim();
    if (!key) return;
    this.loading.set(true);
    this.loaded.set(true);
    this.failure.set(undefined);
    try {
      const rows = await this.marketplace.shipments(key);
      this.shipments.set(rows);
      for (const shipment of rows) {
        this.nextStatus[shipment.shipmentNo] = this.nextStatuses(shipment.status)[0] ?? '';
        this.carrierCode[shipment.shipmentNo] = shipment.carrierCode ?? '';
        this.trackingNo[shipment.shipmentNo] = shipment.trackingNo ?? '';
      }
    } catch (error) {
      this.shipments.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  nextStatuses(status: string): string[] {
    return SHIPMENT_TRANSITIONS[status] ?? [];
  }

  async advance(shipment: ShipmentView): Promise<void> {
    const next = this.nextStatus[shipment.shipmentNo];
    if (!next || !this.nextStatuses(shipment.status).includes(next)) return;
    if (!confirm(`Transition shipment ${shipment.shipmentNo} from ${shipment.status} to ${next}?`)) return;
    this.pendingShipment.set(shipment.shipmentNo);
    this.failure.set(undefined);
    try {
      const updated = await this.marketplace.updateShipmentStatus(
        shipment.shipmentNo,
        shipment.sellerId,
        next,
        this.carrierCode[shipment.shipmentNo]?.trim() || undefined,
        this.trackingNo[shipment.shipmentNo]?.trim() || undefined,
      );
      this.shipments.update(rows => rows.map(row => row.shipmentNo === updated.shipmentNo ? updated : row));
      this.nextStatus[updated.shipmentNo] = this.nextStatuses(updated.status)[0] ?? '';
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.pendingShipment.set(undefined);
    }
  }
}
