import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView, ShipmentView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-order-detail', imports: [CommonModule, FormsModule, RouterLink], template: `<section class="page"><div class="page-title"><div><small>ORDER 360</small><h1>{{order()?.orderNo}}</h1></div><a routerLink="/orders">← Lịch sử</a></div><div class="error" *ngIf="error()">{{error()}}</div><div class="detail-columns" *ngIf="order() as o"><div class="panel"><h2>{{o.status}}</h2><p>Payable <strong>{{o.payableAmount|number}} ₫</strong></p><div class="mini-row" *ngFor="let s of o.sellerOrders"><div><b>Seller {{s.sellerId}}</b><small>{{s.status}}</small></div><strong>{{s.payableAmount|number}} ₫</strong></div><label>Lý do hủy<select [(ngModel)]="cancelReason"><option>CUSTOMER_REQUEST</option><option>ADDRESS_ISSUE</option><option>PAYMENT_ISSUE</option></select></label><button class="ghost" (click)="cancel()" [disabled]="o.status==='COMPLETED'||o.status==='CANCELLED'">Yêu cầu hủy</button></div><div class="panel"><h2>Shipment tracking</h2><div class="mini-row" *ngFor="let s of shipments()"><div><b>{{s.shipmentNo}}</b><small>{{s.carrierCode}} {{s.trackingNo}}</small></div><span class="status-chip">{{s.status}}</span></div><p class="hint" *ngIf="!shipments().length">Chưa tạo shipment.</p><a routerLink="/returns">Tạo yêu cầu trả hàng →</a></div></div></section>` })
export class OrderDetailComponent implements OnInit {
    readonly order = signal<OrderView | undefined>(undefined);
    readonly shipments = signal<ShipmentView[]>([]);
    readonly error = signal('');
    cancelReason = 'CUSTOMER_REQUEST';
    private orderNo = '';
    constructor(private readonly route: ActivatedRoute, private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { this.orderNo = this.route.snapshot.paramMap.get('orderNo') ?? ''; await this.load(); }
    async load() { try {
        this.order.set(await this.marketplace.order(this.orderNo));
        this.shipments.set(await this.marketplace.shipments(this.orderNo));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async cancel() { try {
        this.order.set(await this.marketplace.cancelOrder(this.orderNo, this.cancelReason));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
