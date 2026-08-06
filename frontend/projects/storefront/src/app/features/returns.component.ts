import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ReturnView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-returns', imports: [CommonModule, FormsModule], template: `<section class="page"><div class="page-title"><div><small>RETURN / REFUND</small><h1>Trả hàng</h1></div></div><div class="two-col"><div class="panel"><h2>Tạo yêu cầu</h2><label>Order no<input [(ngModel)]="orderNo"></label><label>Order line IDs<input [(ngModel)]="lineIds" placeholder="101,102"></label><label>Lý do<textarea [(ngModel)]="reason"></textarea></label><button (click)="create()">Tạo return</button><div class="error" *ngIf="error()">{{error()}}</div></div><div class="panel"><h2>Lifecycle</h2><div class="mini-row" *ngFor="let r of returns()"><div><b>{{r.returnKey}}</b><small>{{r.orderId}} · {{r.createdAt|date:'short'}}</small><p>{{r.refundableAmount|number}} ₫</p></div><span class="status-chip">{{r.status}}</span><button class="ghost" *ngIf="canDispute(r)" (click)="dispute(r)">Dispute</button></div></div></div></section>` })
export class ReturnsComponent implements OnInit {
    readonly returns = signal<ReturnView[]>([]);
    readonly error = signal('');
    orderNo = '';
    lineIds = '';
    reason = 'damaged';
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(); }
    async load() { try {
        this.returns.set(await this.marketplace.returns());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async create() { const ids = this.lineIds.split(',').map(x => Number(x.trim())).filter(Number.isFinite); try {
        await this.marketplace.createReturn({ requestKey: crypto.randomUUID(), orderNo: this.orderNo, orderLineIds: ids, reason: this.reason });
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    canDispute(r: ReturnView) { return ['REJECTED', 'INSPECTED', 'REFUND_PENDING'].includes(r.status); }
    async dispute(r: ReturnView) { try {
        await this.marketplace.disputeReturn(r.returnKey, 'Customer disputes inspection result');
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
