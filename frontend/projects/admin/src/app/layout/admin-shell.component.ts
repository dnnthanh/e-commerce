import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { ADMIN_NAVIGATION, AdminNavItem } from '../admin-navigation';

@Component({
  standalone: true,
  selector: 'admin-shell',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="shell" [class.nav-open]="navigationOpen()">
      <button
        class="nav-toggle"
        type="button"
        aria-label="Toggle administration navigation"
        [attr.aria-expanded]="navigationOpen()"
        (click)="navigationOpen.update(open => !open)"
      >☰</button>

      <aside class="admin-sidebar">
        <div class="brand-row">
          <a class="logo" routerLink="/" (click)="closeNavigation()">NOVA OPS</a>
          <button class="sidebar-close" type="button" aria-label="Close navigation" (click)="closeNavigation()">×</button>
        </div>
        <small>MARKETPLACE CONTROL PLANE</small>

        <nav aria-label="Administration">
          <section class="nav-group" *ngFor="let group of navigation">
            <p>{{ group.label }}</p>
            <a
              *ngFor="let item of visibleItems(group.items)"
              [routerLink]="item.path"
              [routerLinkActiveOptions]="{ exact: item.path === '/' }"
              routerLinkActive="active"
              (click)="closeNavigation()"
            >{{ item.label }}</a>
          </section>
        </nav>
      </aside>

      <div class="nav-backdrop" aria-hidden="true" (click)="closeNavigation()"></div>

      <main class="admin-main">
        <header class="admin-topbar">
          <div>
            <span class="env">LOCAL · PRODUCTION-LIKE</span>
          </div>
          <div class="header-actions">
            <div class="identity" *ngIf="auth.authenticated()">
              <strong>Operator</strong>
              <span>{{ roleSummary }}</span>
            </div>
            <button *ngIf="!auth.authenticated()" type="button" (click)="auth.login()">Admin login</button>
            <button *ngIf="auth.authenticated()" class="ghost" type="button" (click)="auth.logout()">Logout</button>
          </div>
        </header>
        <router-outlet />
      </main>
    </div>
  `,
})
export class AdminShellComponent {
  readonly auth = inject(AuthService);
  readonly navigation = ADMIN_NAVIGATION;
  readonly navigationOpen = signal(false);

  visibleItems(items: readonly AdminNavItem[]): readonly AdminNavItem[] {
    return items.filter(item => !item.permission || this.auth.has(item.permission));
  }

  get roleSummary(): string {
    const roles = this.auth.authorization().roles;
    return roles.length ? roles.join(' · ') : 'Authenticated';
  }

  closeNavigation(): void {
    this.navigationOpen.set(false);
  }
}
