import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { AuthorizationSnapshot } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';

/** Permission administration page writing role/seller scope changes through Authorization service. */
@Component({
  standalone: true,
  selector: 'admin-security',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="KEYCLOAK AUTHORIZATION"
        title="Roles, permissions & seller scope"
        description="Security mutations are explicit, confirmed and re-read from the backend after each change."
      />
      <div class="two-col">
        <div class="panel">
          <h2>User authorization</h2>
          <label>Keycloak user ID<input [(ngModel)]="userId" placeholder="User UUID"></label>
          <button type="button" [disabled]="busy() || !userId.trim()" (click)="load()">Load snapshot</button>
          <admin-state
            *ngIf="failure() as problem"
            kind="error"
            title="Security operation failed"
            [message]="problem.message"
            [traceId]="problem.traceId"
          />
          <pre class="json" *ngIf="snapshot() as current">{{ current | json }}</pre>
        </div>

        <div class="panel">
          <h2>Mutations</h2>
          <label>Role<input [(ngModel)]="role" placeholder="SELLER_MANAGER"></label>
          <div class="actions">
            <button type="button" [disabled]="busy() || !canMutateRole" (click)="assignRole()">Assign role</button>
            <button type="button" class="danger" [disabled]="busy() || !canMutateRole" (click)="removeRole()">Remove role</button>
          </div>

          <label>Seller scope<input type="number" min="1" [(ngModel)]="sellerId"></label>
          <div class="actions">
            <button type="button" [disabled]="busy() || !canMutateSeller" (click)="assignSeller()">Assign seller</button>
            <button type="button" class="danger" [disabled]="busy() || !canMutateSeller" (click)="removeSeller()">Remove seller</button>
          </div>
          <p class="hint">Changes go through the Authorization service/Keycloak integration, preserve backend audit/outbox behavior, then refresh effective browser authorization.</p>
        </div>
      </div>
    </section>
  `,
})
export class SecurityComponent {
  private readonly marketplace = inject(MarketplaceApiService);
  private readonly auth = inject(AuthService);
  userId = '';
  role = 'SELLER_MANAGER';
  sellerId?: number;
  readonly snapshot = signal<AuthorizationSnapshot | undefined>(undefined);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly busy = signal(false);

  get canMutateRole(): boolean {
    return !!this.userId.trim() && !!this.role.trim();
  }

  get canMutateSeller(): boolean {
    return !!this.userId.trim() && !!this.sellerId;
  }

  async load(): Promise<void> {
    if (!this.userId.trim()) return;
    this.failure.set(undefined);
    try {
      this.snapshot.set(await this.marketplace.authorization(this.userId.trim()));
    } catch (error) {
      this.failure.set(uiError(error));
    }
  }

  async assignRole(): Promise<void> {
    if (!this.canMutateRole || !confirm(`Assign role ${this.role.trim()} to ${this.userId.trim()}?`)) return;
    await this.mutate(() => this.marketplace.assignRole(this.userId.trim(), this.role.trim()));
  }

  async removeRole(): Promise<void> {
    if (!this.canMutateRole || !confirm(`Remove role ${this.role.trim()} from ${this.userId.trim()}?`)) return;
    await this.mutate(() => this.marketplace.removeRole(this.userId.trim(), this.role.trim()));
  }

  async assignSeller(): Promise<void> {
    if (!this.canMutateSeller || !confirm(`Assign seller scope ${this.sellerId} to ${this.userId.trim()}?`)) return;
    await this.mutate(() => this.marketplace.assignSellerScope(this.userId.trim(), this.sellerId!));
  }

  async removeSeller(): Promise<void> {
    if (!this.canMutateSeller || !confirm(`Remove seller scope ${this.sellerId} from ${this.userId.trim()}?`)) return;
    await this.mutate(() => this.marketplace.removeSellerScope(this.userId.trim(), this.sellerId!));
  }

  private async mutate(command: () => Promise<void>): Promise<void> {
    this.busy.set(true);
    this.failure.set(undefined);
    try {
      await command();
      await Promise.all([this.load(), this.auth.refreshAuthorization()]);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.busy.set(false);
    }
  }
}
