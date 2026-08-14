import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { IncidentView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

/** Truthful operations landing page backed by browser-safe runtime data. */
@Component({
  standalone: true,
  selector: 'admin-dashboard',
  imports: [
    CommonModule,
    RouterLink,
    AdminPageHeaderComponent,
    AdminStateComponent,
    StatusBadgeComponent,
  ],
  template: `
    <section>
      <admin-page-header
        eyebrow="MARKETPLACE CONTROL PLANE"
        title="Operations dashboard"
        description="Operational state, authorization context and recovery signals from live backend contracts."
      />

      <div class="cards">
        <article>
          <span>Open incidents</span>
          <strong>{{ loading() || loadError() ? '—' : incidents().length }}</strong>
          <small>Current recovery queue</small>
        </article>
        <article>
          <span>Critical / high</span>
          <strong>{{ loading() || loadError() ? '—' : high() }}</strong>
          <small>Needs operator attention</small>
        </article>
        <article>
          <span>Effective permissions</span>
          <strong>{{ auth.authorization().permissions.length }}</strong>
          <small>Loaded from authorization service</small>
        </article>
        <article>
          <span>Seller scopes</span>
          <strong>{{ auth.authorization().sellerIds.length }}</strong>
          <small>Explicit seller access in this session</small>
        </article>
      </div>

      <div class="panel">
        <div class="panel-title"><h2>System tools</h2></div>
        <div class="tool-grid">
          <a href="http://localhost:3001" target="_blank" rel="noopener"><strong>Grafana</strong><span>Metrics, Loki logs, Tempo traces</span></a>
          <a href="http://localhost:8185" target="_blank" rel="noopener"><strong>Kafka UI</strong><span>Topics, partitions, messages, consumer groups</span></a>
          <a href="http://localhost:5601" target="_blank" rel="noopener"><strong>OpenSearch Dashboards</strong><span>Search read-model inspection</span></a>
          <a href="http://localhost:8180" target="_blank" rel="noopener"><strong>Keycloak</strong><span>Identity administration</span></a>
        </div>
      </div>

      <div class="panel">
        <div class="panel-title">
          <h2>Latest incidents</h2>
          <a *ngIf="auth.has('OPERATIONS_VIEW')" routerLink="/operations">Open recovery center →</a>
        </div>

        <admin-state
          *ngIf="loading()"
          kind="loading"
          title="Loading operational state"
          message="Reading the current incident queue."
        />
        <admin-state
          *ngIf="loadError() as failure"
          kind="error"
          title="Incident feed unavailable"
          [message]="failure.message"
          [traceId]="failure.traceId"
          [retryable]="true"
          (retry)="load()"
        />
        <admin-state
          *ngIf="!loading() && !loadError() && incidents().length === 0"
          kind="empty"
          title="No open incidents"
          message="The recovery queue is currently empty."
        />

        <div class="table-wrap" *ngIf="!loading() && !loadError() && incidents().length">
          <table>
            <thead><tr><th>Type</th><th>Source</th><th>Severity</th><th>Status</th><th>Aggregate</th></tr></thead>
            <tbody>
              <tr *ngFor="let incident of incidents().slice(0, 8)">
                <td><b>{{ incident.type }}</b><small>{{ incident.firstSeenAt | date:'short' }}</small></td>
                <td>{{ incident.sourceService }} → {{ incident.recoveryTarget }}</td>
                <td><admin-status-badge [status]="incident.severity" /></td>
                <td><admin-status-badge [status]="incident.status" /></td>
                <td>{{ incident.aggregateId }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  `,
})
export class DashboardComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  readonly incidents = signal<IncidentView[]>([]);
  readonly loading = signal(true);
  readonly loadError = signal<UiErrorView | undefined>(undefined);

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.loadError.set(undefined);
    try {
      this.incidents.set(await this.marketplace.incidents());
    } catch (error) {
      this.incidents.set([]);
      this.loadError.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  high(): number {
    return this.incidents().filter(incident => ['CRITICAL', 'HIGH'].includes(incident.severity)).length;
  }
}
