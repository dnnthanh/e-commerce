import { Component, Input } from '@angular/core';

/** Status always includes readable text so meaning never depends on color alone. */
@Component({
  standalone: true,
  selector: 'admin-status-badge',
  template: `<span class="status-badge" [attr.data-status]="normalized">{{ status }}</span>`,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status = '';

  get normalized(): string {
    return this.status.toLowerCase().replaceAll('_', '-');
  }
}
