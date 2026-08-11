import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

export type AdminStateKind = 'loading' | 'empty' | 'error' | 'forbidden' | 'unauthorized';

/** Consistent operational state panel used by Admin feature pages. */
@Component({
  standalone: true,
  selector: 'admin-state',
  imports: [CommonModule],
  template: `
    <div class="admin-state" [attr.data-kind]="kind" role="status">
      <span class="admin-state__icon" aria-hidden="true">{{ icon }}</span>
      <div>
        <strong>{{ title }}</strong>
        <p>{{ message }}</p>
        <small *ngIf="traceId">Trace: {{ traceId }}</small>
      </div>
      <button *ngIf="retryable" class="ghost" type="button" (click)="retry.emit()">Retry</button>
    </div>
  `,
})
export class AdminStateComponent {
  @Input({ required: true }) kind!: AdminStateKind;
  @Input({ required: true }) title = '';
  @Input() message = '';
  @Input() traceId?: string;
  @Input() retryable = false;
  @Output() readonly retry = new EventEmitter<void>();

  get icon(): string {
    return {
      loading: '…',
      empty: '○',
      error: '!',
      forbidden: '×',
      unauthorized: '↗',
    }[this.kind];
  }
}
