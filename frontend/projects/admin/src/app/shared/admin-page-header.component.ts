import { Component, Input } from '@angular/core';

/** Compact reusable page heading matching the Admin console visual hierarchy. */
@Component({
  standalone: true,
  selector: 'admin-page-header',
  template: `
    <div class="page-title admin-page-header">
      <div>
        <small>{{ eyebrow }}</small>
        <h1>{{ title }}</h1>
        @if (description) { <p>{{ description }}</p> }
      </div>
      <div class="actions"><ng-content select="[actions]" /></div>
    </div>
  `,
})
export class AdminPageHeaderComponent {
  @Input({ required: true }) eyebrow = '';
  @Input({ required: true }) title = '';
  @Input() description = '';
}
