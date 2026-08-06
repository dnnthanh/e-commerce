import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ReturnView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-returns', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>RETURN INSPECTION / REFUND</small><h1>Returns operations</h1></div><button (click)="load()">Refresh</button></div><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><div class="mini-row" *ngFor="let r of returns()"><div><b>{{r.returnKey}}</b><small>{{r.orderId}} · {{r.status}}</small><p>{{r.refundableAmount|number}} ₫</p></div><div class="row"><button *ngIf="r.status==='REQUESTED'" (click)="approve(r)">Approve</button><button class="ghost" *ngIf="r.status==='REQUESTED'" (click)="reject(r)">Reject</button><button *ngIf="r.status==='APPROVED'" (click)="receive(r)">Receive</button><button *ngIf="r.status==='REFUND_PENDING'" (click)="refund(r)">Refund</button></div></div></div><div class="two-col"><div class="panel"><h2>Seller action context</h2><label>Seller ID<input type="number" [(ngModel)]="sellerId"></label><label>Reason<input [(ngModel)]="reason"></label><label>Receiving warehouse<input type="number" [(ngModel)]="warehouseId"></label></div><div class="panel"><h2>Inspection line</h2><label>Order line ID<input type="number" [(ngModel)]="orderLineId"></label><label>Accepted qty<input type="number" [(ngModel)]="acceptedQuantity"></label><label>Disposition<select [(ngModel)]="disposition"><option>RESTOCK</option><option>QUARANTINE</option><option>SCRAP</option></select></label><button class="ghost" (click)="inspectSelected()" [disabled]="!selected">Inspect selected return</button></div></div></section>` })
export class ReturnsAdminComponent implements OnInit {
    readonly returns = signal<ReturnView[]>([]);
    readonly error = signal('');
    selected?: ReturnView;
    sellerId = 10001;
    reason = 'Approved after seller review';
    warehouseId = 1;
    orderLineId = 1;
    acceptedQuantity = 1;
    disposition = 'RESTOCK';
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(); }
    async load() { try {
        this.returns.set(await this.marketplace.returns());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async approve(r: ReturnView) { this.selected = r; try {
        await this.marketplace.approveReturn(r.returnKey, this.sellerId, this.reason);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async reject(r: ReturnView) { this.selected = r; try {
        await this.marketplace.rejectReturn(r.returnKey, this.sellerId, this.reason);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async receive(r: ReturnView) { this.selected = r; try {
        await this.marketplace.receiveReturn(r.returnKey, this.warehouseId);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async inspectSelected() { if (!this.selected)
        return; try {
        await this.marketplace.inspectReturn(this.selected.returnKey, [{ orderLineId: this.orderLineId, acceptedQuantity: this.acceptedQuantity, disposition: this.disposition }]);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async refund(r: ReturnView) { try {
        await this.marketplace.refundReturn(r.returnKey);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
