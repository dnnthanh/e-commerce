import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { AdminStateComponent } from '../shared/admin-state.component';

@Component({
  standalone: true,
  selector: 'admin-unauthorized',
  imports: [AdminStateComponent],
  template: `
    <section class="auth-state-page">
      <admin-state
        kind="unauthorized"
        title="Admin sign-in required"
        message="Sign in with an authorized marketplace operator account to continue."
      />
      <button type="button" (click)="auth.login()">Admin login</button>
    </section>
  `,
})
export class UnauthorizedComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  async ngOnInit(): Promise<void> {
    await this.auth.ensureInitialized();
    if (this.auth.authenticated()) await this.router.navigateByUrl('/');
  }
}
