import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ReturnView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-returns',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="RETURN INSPECTION / REFUND"
        title="Returns operations"
        description="Seller decisions, warehouse inspection and payment refund remain separate permissioned actions with explicit operator context."
      >
        <button actions type="button" class="ghost" [disabled]="loading()" (click)="load()">Refresh</button>
      </admin-page-header>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Return operation failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load()"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading returns" message="Reading the current return workflow queue." />
      <admin-state *ngIf="!loading() && !failure() && returns().length === 0" kind="empty" title="No returns" message="The return queue is currently empty." />

      <div class="panel" *ngIf="returns().length">
        <div class="mini-row" *ngFor="let item of returns()">
          <div>
            <b>{{ item.returnKey }}</b>
            <small>{{ item.orderId }} · {{ item.createdAt | date:'short' }}</small>
            <p><admin-status-badge [status]="item.status" /> · refundable {{ item.refundableAmount | number }} ₫</p>
          </div>
          <div class="actions">
            <button
              *ngIf="item.status === 'REQUESTED' && auth.has('RETURN_VIEW')"
              type="button"
              [disabled]="pendingKey() === item.returnKey || !sellerId || !reason.trim()"
              (click)="approve(item)"
            >Approve</button>
            <button
              *ngIf="item.status === 'REQUESTED' && auth.has('RETURN_VIEW')"
              class="danger"
              type="button"
              [disabled]="pendingKey() === item.returnKey || !sellerId || !reason.trim()"
              (click)="reject(item)"
            >Reject</button>
            <button
              *ngIf="item.status === 'APPROVED' && auth.has('RETURN_RECEIVE')"
              type="button"
              [disabled]="pendingKey() === item.returnKey || warehouseId < 1"
              (click)="receive(item)"
            >Receive</button>
            <button
              *ngIf="item.status === 'RECEIVED' && auth.has('RETURN_RECEIVE')"
              class="ghost"
              type="button"
              (click)="selectForInspection(item)"
            >Inspect</button>
            <button
              *ngIf="canRefund(item) && auth.has('PAYMENT_REFUND')"
              class="danger"
              type="button"
              [disabled]="pendingKey() === item.returnKey"
              (click)="refund(item)"
            >Refund</button>
          </div>
        </div>
      </div>

      <div class="two-col">
        <div class="panel">
          <h2>Seller / receiving context</h2>
          <label *ngIf="sellerScopes.length; else manualSeller">
            Seller scope
            <select [(ngModel)]="sellerId"><option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option></select>
          </label>
          <ng-template #manualSeller><label>Seller ID<input type="number" min="1" [(ngModel)]="sellerId"></label></ng-template>
          <label>Seller decision reason<input [(ngModel)]="reason" maxlength="250" placeholder="Required for approve/reject"></label>
          <label>Receiving warehouse<input type="number" min="1" [(ngModel)]="warehouseId"></label>
        </div>

        <div class="panel">
          <h2>Inspection line</h2>
          <p class="hint" *ngIf="selected() as current">Selected return: {{ current.returnKey }}</p>
          <label>Order line ID<input type="number" min="1" [(ngModel)]="orderLineId"></label>
          <label>Accepted quantity<input type="number" min="0" [(ngModel)]="acceptedQuantity"></label>
          <label>Disposition
            <select [(ngModel)]="disposition"><option>RESTOCK</option><option>QUARANTINE</option><option>SCRAP</option></select>
          </label>
          <button
            type="button"
            class="ghost"
            [disabled]="!selected() || pendingKey() === selected()?.returnKey || orderLineId < 1 || acceptedQuantity < 0"
            (click)="inspectSelected()"
          >Submit inspection</button>
        </div>
      </div>
    </section>
  `,
})
export class ReturnsAdminComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  readonly returns = signal<ReturnView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly pendingKey = signal<string | undefined>(undefined);
  readonly selected = signal<ReturnView | undefined>(undefined);
  sellerId?: number;
  reason = '';
  warehouseId = 1;
  orderLineId = 1;
  acceptedQuantity = 1;
  disposition = 'RESTOCK';

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  async ngOnInit(): Promise<void> {
    if (this.sellerScopes.length) this.sellerId = this.sellerScopes[0];
    await this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      this.returns.set(await this.marketplace.returns());
    } catch (error) {
      this.returns.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  canRefund(item: ReturnView): boolean {
    return ['INSPECTED', 'REFUND_PENDING', 'REFUND_UNKNOWN', 'REFUND_FAILED'].includes(item.status);
  }

  selectForInspection(item: ReturnView): void {
    this.selected.set(item);
  }

  async approve(item: ReturnView): Promise<void> {
    if (!this.sellerId || !this.reason.trim() || !confirm(`Approve return ${item.returnKey}?`)) return;
    await this.mutate(item, () => this.marketplace.approveReturn(item.returnKey, this.sellerId!, this.reason.trim()));
  }

  async reject(item: ReturnView): Promise<void> {
    if (!this.sellerId || !this.reason.trim() || !confirm(`Reject return ${item.returnKey}?`)) return;
    await this.mutate(item, () => this.marketplace.rejectReturn(item.returnKey, this.sellerId!, this.reason.trim()));
  }

  async receive(item: ReturnView): Promise<void> {
    if (this.warehouseId < 1 || !confirm(`Receive return ${item.returnKey} into warehouse ${this.warehouseId}?`)) return;
    await this.mutate(item, () => this.marketplace.receiveReturn(item.returnKey, this.warehouseId));
  }

  async inspectSelected(): Promise<void> {
    const item = this.selected();
    if (!item || this.orderLineId < 1 || this.acceptedQuantity < 0) return;
    if (!confirm(`Submit inspection for return ${item.returnKey}?`)) return;
    await this.mutate(item, () => this.marketplace.inspectReturn(item.returnKey, [{
      orderLineId: this.orderLineId,
      acceptedQuantity: this.acceptedQuantity,
      disposition: this.disposition,
    }]));
  }

  async refund(item: ReturnView): Promise<void> {
    if (!confirm(`Start or retry refund for return ${item.returnKey} (${item.refundableAmount} ₫)?`)) return;
    await this.mutate(item, () => this.marketplace.refundReturn(item.returnKey));
  }

  private async mutate(item: ReturnView, command: () => Promise<ReturnView>): Promise<void> {
    this.pendingKey.set(item.returnKey);
    this.failure.set(undefined);
    try {
      const updated = await command();
      this.returns.update(rows => rows.map(row => row.returnKey === updated.returnKey ? updated : row));
      if (this.selected()?.returnKey === updated.returnKey) this.selected.set(updated);
      this.reason = '';
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.pendingKey.set(undefined);
    }
  }
}
