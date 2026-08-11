import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { PaymentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-payments',
  imports: [CommonModule, FormsModule, RouterLink, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="MOMO · VNPAY · VIETQR"
        title="Payments & ambiguous outcomes"
        description="Read payment status and route ambiguous outcomes to Operations. No browser retry command is exposed."
      >
        <a actions class="button-link ghost" routerLink="/operations">Open Operations</a>
      </admin-page-header>

      <form class="filters" (ngSubmit)="load(0)">
        <label>Status
          <select [(ngModel)]="status" name="status">
            <option value="">All statuses</option>
            <option>CREATED</option><option>PENDING</option><option>PAID</option><option>UNKNOWN</option>
            <option>FAILED</option><option>PARTIALLY_REFUNDED</option><option>REFUNDED</option>
          </select>
        </label>
        <button type="submit" [disabled]="loading()">Filter</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Payments unavailable"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load(page())"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading payments" message="Reading payment state from the payment service." />
      <admin-state *ngIf="!loading() && !failure() && payments().length === 0" kind="empty" title="No payments" message="No payments matched this status filter." />

      <div class="panel" *ngIf="payments().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>Payment</th><th>Order</th><th>User</th><th>Provider</th><th>Amount</th><th>Status</th><th>Updated</th></tr></thead>
            <tbody>
              <tr *ngFor="let payment of payments()">
                <td>{{ payment.paymentKey }}</td><td>{{ payment.orderId }}</td><td>{{ payment.userId }}</td>
                <td>{{ payment.provider }}</td><td>{{ payment.amount | number }} ₫</td>
                <td><admin-status-badge [status]="payment.status" /></td><td>{{ payment.updatedAt | date:'short' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="row">
          <button type="button" class="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">Prev</button>
          <span>page {{ page() + 1 }}</span>
          <button type="button" class="ghost" [disabled]="!hasNext()" (click)="load(page() + 1)">Next</button>
        </div>
      </div>
    </section>
  `,
})
export class PaymentsComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  status = '';
  readonly payments = signal<PaymentView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly page = signal(0);
  readonly hasNext = signal(false);

  async ngOnInit(): Promise<void> { await this.load(0); }

  async load(targetPage: number): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.payments(Math.max(0, targetPage), 50, this.status);
      this.payments.set(result.data);
      this.page.set(result.metadata.page);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.payments.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
