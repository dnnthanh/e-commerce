import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { IncidentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

/** Operator recovery center for DLT/Saga/job/payment ambiguous outcomes. */
@Component({
  standalone: true,
  selector: 'admin-operations',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent, StatusBadgeComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="RECOVERY"
        title="Operations & incidents"
        description="Recovery actions require an operator reason and stay within the existing operations contracts."
      >
        <button actions type="button" class="ghost" [disabled]="loading()" (click)="load()">Refresh</button>
      </admin-page-header>

      <label>Operator reason
        <input [(ngModel)]="reason" maxlength="250" placeholder="Why is this recovery or resolution appropriate?">
      </label>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Operations request failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load()"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading incidents" message="Reading the current recovery queue." />
      <admin-state *ngIf="!loading() && !failure() && incidents().length === 0" kind="empty" title="No incidents" message="The operations recovery queue is empty." />

      <div class="panel" *ngIf="!loading() && incidents().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>Incident</th><th>Source → Target</th><th>Aggregate</th><th>Status</th><th>Error</th><th>Actions</th></tr></thead>
            <tbody>
              <tr *ngFor="let incident of incidents()">
                <td><b>{{ incident.type }}</b><small><admin-status-badge [status]="incident.severity" /> · {{ incident.firstSeenAt | date:'short' }}</small></td>
                <td>{{ incident.sourceService }} → {{ incident.recoveryTarget }}</td>
                <td>{{ incident.aggregateId }}</td>
                <td><admin-status-badge [status]="incident.status" /></td>
                <td>{{ incident.lastError || '—' }}</td>
                <td>
                  <div class="actions">
                    <button type="button" [disabled]="pendingId() === incident.id || !reason.trim()" (click)="recover(incident)">Recover</button>
                    <button type="button" class="ghost" [disabled]="pendingId() === incident.id || !reason.trim()" (click)="resolve(incident)">Resolve</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  `,
})
export class OperationsComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  readonly incidents = signal<IncidentView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly pendingId = signal<number | undefined>(undefined);
  reason = '';

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      this.incidents.set(await this.marketplace.incidents());
    } catch (error) {
      this.incidents.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async recover(incident: IncidentView): Promise<void> {
    if (!this.reason.trim() || !confirm(`Recover incident ${incident.id}?`)) return;
    await this.mutate(incident, () => this.marketplace.recoverIncident(incident.id, this.reason.trim()));
  }

  async resolve(incident: IncidentView): Promise<void> {
    if (!this.reason.trim() || !confirm(`Resolve incident ${incident.id}?`)) return;
    await this.mutate(incident, () => this.marketplace.resolveIncident(incident.id, this.reason.trim()));
  }

  private async mutate(incident: IncidentView, command: () => Promise<void>): Promise<void> {
    this.pendingId.set(incident.id);
    this.failure.set(undefined);
    try {
      await command();
      this.reason = '';
      await this.load();
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.pendingId.set(undefined);
    }
  }
}
