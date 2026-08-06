import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../../../shared/api.service';
import { errorMessage } from '../../../../../shared/ui-state';
/** Immutable audit history browser. */
@Component({ standalone: true, selector: 'admin-audit', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>FORENSICS</small><h1>Audit history</h1></div></div><form class="filters" (ngSubmit)="load()"><input [(ngModel)]="actor" name="actor" placeholder="Actor ID"><input [(ngModel)]="action" name="action" placeholder="Action"><input [(ngModel)]="resource" name="resource" placeholder="Resource type"><button>Search</button></form><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><table><thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Resource</th><th>Source</th><th>Trace</th></tr></thead><tbody><tr *ngFor="let e of events()"><td>{{e.occurredAt|date:'short'}}</td><td>{{e.actorType}} · {{e.actorId}}</td><td><b>{{e.action}}</b></td><td>{{e.resourceType}}/{{e.resourceId}}</td><td>{{e.sourceService}}</td><td><code>{{e.traceId||'—'}}</code></td></tr></tbody></table></div></section>` })
export class AuditComponent {
    actor = '';
    action = '';
    resource = '';
    readonly events = signal<any[]>([]);
    readonly error = signal('');
    constructor(private readonly api: ApiService) { void this.load(); }
    async load() { const q = new URLSearchParams({ page: '0', size: '100' }); if (this.actor)
        q.set('actorId', this.actor); if (this.action)
        q.set('action', this.action); if (this.resource)
        q.set('resourceType', this.resource); try {
        this.events.set((await this.api.getPage<any>('/private/audit?' + q, true)).data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
