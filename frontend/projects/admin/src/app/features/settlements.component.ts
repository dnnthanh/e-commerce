import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { SettlementView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-settlements',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="ORACLE FINANCE LEDGER"
        title="Settlement & payout"
        description="Read seller-scoped settlement pages and require an explicit finance reason before approval."
      />

      <form class="filters" (ngSubmit)="load(0)">
        <label *ngIf="sellerScopes.length; else manualSeller">
          Seller scope
          <select [(ngModel)]="sellerId" name="seller"><option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option></select>
        </label>
        <ng-template #manualSeller><label>Seller ID<input type="number" min="1" [(ngModel)]="sellerId" name="seller"></label></ng-template>
        <label>Approval reason<input [(ngModel)]="reason" name="reason" maxlength="250" placeholder="Required before approval"></label>
        <button type="submit" [disabled]="loading() || !sellerId">Load</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Settlement request failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load(page())"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading settlements" message="Reading seller-scoped settlement data." />
      <admin-state *ngIf="!loading() && !failure() && settlements().length === 0" kind="empty" title="No settlements" message="No settlement rows were returned for this seller." />

      <div class="panel" *ngIf="settlements().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>Settlement</th><th>Gross</th><th>Commission</th><th>Payable</th><th>Status</th><th></th></tr></thead>
            <tbody>
              <tr *ngFor="let settlement of settlements()">
                <td>{{ settlement.settlementNo }}<small>seller {{ settlement.sellerId }}</small></td>
                <td>{{ settlement.gross | number }}</td><td>{{ settlement.commission | number }}</td>
                <td><b>{{ settlement.payable | number }}</b></td><td><admin-status-badge [status]="settlement.status" /></td>
                <td>
                  <button
                    *ngIf="settlement.status === 'OPEN' && auth.has('SETTLEMENT_APPROVE')"
                    type="button"
                    [disabled]="pendingNo() === settlement.settlementNo || !reason.trim()"
                    (click)="approve(settlement)"
                  >Approve</button>
                </td>
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
export class SettlementsComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  sellerId?: number;
  reason = '';
  readonly settlements = signal<SettlementView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly pendingNo = signal<string | undefined>(undefined);
  readonly page = signal(0);
  readonly hasNext = signal(false);

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  async ngOnInit(): Promise<void> {
    if (this.sellerScopes.length) this.sellerId = this.sellerScopes[0];
    if (this.sellerId) await this.load(0);
  }

  async load(targetPage: number): Promise<void> {
    if (!this.sellerId) return;
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.settlements(this.sellerId, Math.max(0, targetPage), 50);
      this.settlements.set(result.data);
      this.page.set(result.metadata.page);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.settlements.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async approve(settlement: SettlementView): Promise<void> {
    const operatorReason = this.reason.trim();
    if (!operatorReason || !confirm(`Approve settlement ${settlement.settlementNo} for ${settlement.payable} ₫?`)) return;
    this.pendingNo.set(settlement.settlementNo);
    this.failure.set(undefined);
    try {
      await this.marketplace.approveSettlement(settlement.settlementNo, operatorReason);
      this.reason = '';
      await this.load(this.page());
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.pendingNo.set(undefined);
    }
  }
}
