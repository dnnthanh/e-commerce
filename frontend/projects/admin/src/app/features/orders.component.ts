import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView, ShipmentView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-orders', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>ORDER 360</small><h1>Order diagnostics</h1></div></div><div class="panel"><div class="filters"><input [(ngModel)]="orderNo" placeholder="Order no"><button (click)="load()">Load</button></div><div class="error" *ngIf="error()">{{error()}}</div><div *ngIf="order() as o"><div class="cards"><article><span>Status</span><strong>{{o.status}}</strong></article><article><span>Gross</span><strong>{{o.grossAmount|number}}</strong></article><article><span>Discount</span><strong>{{o.discountAmount|number}}</strong></article><article><span>Payable</span><strong>{{o.payableAmount|number}}</strong></article></div><h3>Seller orders</h3><pre class="json">{{o.sellerOrders|json}}</pre><h3>Shipments</h3><div class="mini-row" *ngFor="let s of shipments()"><div><b>{{s.shipmentNo}}</b><small>seller {{s.sellerId}} · warehouse {{s.warehouseId}}</small></div><span>{{s.status}}</span></div></div></div></section>` })
export class OrdersAdminComponent {
    orderNo = '';
    readonly order = signal<OrderView | undefined>(undefined);
    readonly shipments = signal<ShipmentView[]>([]);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async load() { try {
        this.order.set(await this.marketplace.order(this.orderNo));
        this.shipments.set(await this.marketplace.shipments(this.orderNo));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
