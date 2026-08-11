import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { AuditView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';

/** Immutable, typed audit history browser. */
@Component({
  standalone: true,
  selector: 'admin-audit',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="FORENSICS"
        title="Audit history"
        description="Read-only protected-operation history using the filters currently supported by the typed browser facade."
      />
      <form class="filters" (ngSubmit)="load(0)">
        <label>Resource type<input [(ngModel)]="resourceType" name="resourceType" placeholder="COMMENT, INCIDENT, ROLE…"></label>
        <button type="submit" [disabled]="loading()">Search</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Audit history unavailable"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load(page())"
      />
      <admin-state *ngIf="loading()" kind="loading" title="Loading audit history" message="Reading immutable audit records." />
      <admin-state *ngIf="!loading() && !failure() && events().length === 0" kind="empty" title="No audit events" message="No records matched the current resource filter." />

      <div class="panel" *ngIf="events().length">
        <div class="table-wrap">
          <table>
            <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Resource</th><th>Source</th><th>Trace</th></tr></thead>
            <tbody>
              <tr *ngFor="let event of events()">
                <td>{{ event.occurredAt | date:'short' }}</td>
                <td>{{ event.actorType }} · {{ event.actorId }}</td>
                <td><b>{{ event.action }}</b></td>
                <td>{{ event.resourceType }}/{{ event.resourceId }}</td>
                <td>{{ event.sourceService }}</td>
                <td><code>{{ event.traceId || '—' }}</code></td>
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
export class AuditComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  resourceType = '';
  readonly events = signal<AuditView[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly page = signal(0);
  readonly hasNext = signal(false);

  async ngOnInit(): Promise<void> {
    await this.load(0);
  }

  async load(targetPage: number): Promise<void> {
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const result = await this.marketplace.audit(Math.max(0, targetPage), 50, this.resourceType.trim());
      this.events.set(result.data);
      this.page.set(result.metadata.page);
      this.hasNext.set(result.metadata.hasNext);
    } catch (error) {
      this.events.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
