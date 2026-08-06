import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../../../shared/api.service';
import { errorMessage } from '../../../../../shared/ui-state';
/** Permission administration page writing role/seller scope changes back to Keycloak through Authorization service. */
@Component({ standalone: true, selector: 'admin-security', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>KEYCLOAK AUTHORIZATION</small><h1>Roles, permissions & seller scope</h1></div></div><div class="two-col"><div class="panel"><h2>User authorization</h2><label>Keycloak user ID<input [(ngModel)]="userId"></label><button (click)="load()">Load snapshot</button><div class="error" *ngIf="error()">{{error()}}</div><pre class="json" *ngIf="snapshot()">{{snapshot()|json}}</pre></div><div class="panel"><h2>Mutations</h2><label>Role<input [(ngModel)]="role" placeholder="SELLER_MANAGER"></label><div class="row"><button (click)="assignRole()">Assign role</button><button class="ghost" (click)="removeRole()">Remove</button></div><label>Seller scope<input type="number" [(ngModel)]="sellerId"></label><div class="row"><button (click)="assignSeller()">Assign seller</button><button class="ghost" (click)="removeSeller()">Remove</button></div><p class="hint">Thay đổi đi qua Keycloak Admin API, local history + Outbox, sau đó invalidate authorization cache.</p></div></div></section>` })
export class SecurityComponent {
    userId = '';
    role = 'SELLER_MANAGER';
    sellerId = 10001;
    readonly snapshot = signal<any>(undefined);
    readonly error = signal('');
    constructor(private readonly api: ApiService) { }
    async load() { if (!this.userId)
        return; try {
        this.snapshot.set(await this.api.get<any>(`/private/admin/security/users/${this.userId}`, true));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async assignRole() { await this.api.send('PUT', `/private/admin/security/users/${this.userId}/roles`, { role: this.role }); await this.load(); }
    async removeRole() { await this.api.send('DELETE', `/private/admin/security/users/${this.userId}/roles/${encodeURIComponent(this.role)}`, undefined); await this.load(); }
    async assignSeller() { await this.api.send('PUT', `/private/admin/security/users/${this.userId}/seller-scopes`, { sellerId: this.sellerId }); await this.load(); }
    async removeSeller() { await this.api.send('DELETE', `/private/admin/security/users/${this.userId}/seller-scopes/${this.sellerId}`, undefined); await this.load(); }
}
