import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminStateComponent } from '../shared/admin-state.component';

@Component({
  standalone: true,
  selector: 'admin-forbidden',
  imports: [RouterLink, AdminStateComponent],
  template: `
    <section class="auth-state-page">
      <admin-state
        kind="forbidden"
        title="Permission required"
        message="Your account is authenticated, but it does not have permission for this Admin workspace."
      />
      <a class="button-link" routerLink="/">Back to dashboard</a>
    </section>
  `,
})
export class ForbiddenComponent {}
