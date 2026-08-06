import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../../../shared/api.service';
import { errorMessage } from '../../../../../shared/ui-state';
/** Operator recovery center for DLT/Saga/job/payment ambiguous outcomes. */
@Component({ standalone: true, selector: 'admin-operations', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>RECOVERY</small><h1>Operations & incidents</h1></div><button (click)="load()">Refresh</button></div><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><table><thead><tr><th>Incident</th><th>Source → Target</th><th>Aggregate</th><th>Error</th><th>Actions</th></tr></thead><tbody><tr *ngFor="let i of incidents()"><td><b>{{i.type}}</b><small>{{i.severity}} · {{i.firstSeenAt|date:'short'}}</small></td><td>{{i.sourceService}} → {{i.recoveryTarget}}</td><td>{{i.aggregateId}}</td><td>{{i.lastError}}</td><td><button (click)="recover(i)">Recover</button><button class="ghost" (click)="resolve(i)">Resolve</button></td></tr></tbody></table></div></section>` })
export class OperationsComponent implements OnInit {
    readonly incidents = signal<any[]>([]);
    readonly error = signal('');
    constructor(private readonly api: ApiService) { }
    async ngOnInit() { await this.load(); }
    async load() { try {
        this.incidents.set(await this.api.get<any[]>('/private/operations/incidents', true));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async recover(i: any) { await this.api.send('POST', `/private/operations/incidents/${i.id}/recover`, { reason: 'Manual recovery from admin console' }); await this.load(); }
    async resolve(i: any) { await this.api.send('POST', `/private/operations/incidents/${i.id}/resolve`, { reason: 'Resolved by operator' }); await this.load(); }
}
