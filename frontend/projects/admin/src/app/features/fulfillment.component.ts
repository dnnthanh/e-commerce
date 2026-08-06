import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ShipmentView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-fulfillment', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>FULFILLMENT STATE MACHINE</small><h1>Shipments</h1></div></div><div class="filters"><input [(ngModel)]="orderNo" placeholder="Order no"><button (click)="load()">Load shipments</button></div><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><div class="mini-row" *ngFor="let s of shipments()"><div><b>{{s.shipmentNo}}</b><small>Seller {{s.sellerId}} / warehouse {{s.warehouseId}}</small><p>{{s.carrierCode}} {{s.trackingNo}}</p></div><select [(ngModel)]="nextStatus[s.shipmentNo]"><option>PICKING</option><option>PACKED</option><option>READY_TO_SHIP</option><option>HANDED_OVER</option><option>IN_TRANSIT</option><option>DELIVERED</option></select><button (click)="advance(s)">Transition</button></div></div><p class="hint">The backend domain rejects illegal transitions; this console deliberately sends one transition at a time instead of mutating status locally.</p></section>` })
export class FulfillmentComponent {
    orderNo = '';
    readonly shipments = signal<ShipmentView[]>([]);
    readonly error = signal('');
    nextStatus: Record<string, string> = {};
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async load() { try {
        const rows = await this.marketplace.shipments(this.orderNo);
        this.shipments.set(rows);
        for (const s of rows)
            this.nextStatus[s.shipmentNo] = s.status;
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async advance(s: ShipmentView) { try {
        await this.marketplace.updateShipmentStatus(s.shipmentNo, s.sellerId, this.nextStatus[s.shipmentNo], s.carrierCode, s.trackingNo);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
