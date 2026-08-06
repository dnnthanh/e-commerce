import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { PaymentView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-payments', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>MOMO · VNPAY · VIETQR</small><h1>Payments & ambiguous outcomes</h1></div></div><form class="filters" (ngSubmit)="load(0)"><select [(ngModel)]="status" name="status"><option value="">All statuses</option><option>CREATED</option><option>PENDING</option><option>PAID</option><option>UNKNOWN</option><option>FAILED</option><option>PARTIALLY_REFUNDED</option><option>REFUNDED</option></select><button>Filter</button></form><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><table><thead><tr><th>Payment</th><th>Order</th><th>User</th><th>Provider</th><th>Amount</th><th>Status</th><th>Updated</th></tr></thead><tbody><tr *ngFor="let p of payments()"><td>{{p.paymentKey}}</td><td>{{p.orderId}}</td><td>{{p.userId}}</td><td>{{p.provider}}</td><td>{{p.amount|number}} ₫</td><td><span class="badge" [class.danger]="p.status==='UNKNOWN'">{{p.status}}</span></td><td>{{p.updatedAt|date:'short'}}</td></tr></tbody></table><div class="row"><button class="ghost" [disabled]="page()===0" (click)="load(page()-1)">Prev</button><span>page {{page()+1}}</span><button class="ghost" [disabled]="!hasNext()" (click)="load(page()+1)">Next</button></div></div><p class="hint">Provider reconciliation is an internal/operations path. UNKNOWN is surfaced here so operators can correlate it with Operations incidents.</p></section>` })
export class PaymentsComponent implements OnInit {
    status = '';
    readonly payments = signal<PaymentView[]>([]);
    readonly error = signal('');
    readonly page = signal(0);
    readonly hasNext = signal(false);
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(0); }
    async load(page: number) { try {
        const r = await this.marketplace.payments(page, 50, this.status);
        this.payments.set(r.data);
        this.page.set(r.metadata.page);
        this.hasNext.set(r.metadata.hasNext);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
