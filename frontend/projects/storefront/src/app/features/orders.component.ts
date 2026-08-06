import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { OrderView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-orders', imports: [CommonModule, RouterLink], template: `<section class="page"><div class="page-title"><div><small>ORDER HISTORY</small><h1>Đơn hàng của tôi</h1></div></div><div class="error" *ngIf="error()">{{error()}}</div><div class="order-card" *ngFor="let o of orders()"><div><strong>{{o.orderNo}}</strong><small>{{o.status}} · {{o.createdAt|date:'medium'}}</small><span>{{o.sellerOrders.length}} seller orders</span></div><div><span>Tạm tính {{o.grossAmount|number}} ₫</span><span>Giảm {{o.discountAmount|number}} ₫</span><b>{{o.payableAmount|number}} ₫</b><a [routerLink]="['/orders',o.orderNo]">Chi tiết →</a></div></div><div class="pager"><button class="ghost" [disabled]="page()===0" (click)="load(page()-1)">Trước</button><span>Trang {{page()+1}} / {{totalPages()}}</span><button class="ghost" [disabled]="!hasNext()" (click)="load(page()+1)">Sau</button></div></section>` })
export class OrdersComponent implements OnInit {
    readonly orders = signal<OrderView[]>([]);
    readonly error = signal('');
    readonly page = signal(0);
    readonly totalPages = signal(1);
    readonly hasNext = signal(false);
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(0); }
    async load(page: number) { try {
        const r = await this.marketplace.orders(page, 20);
        this.orders.set(r.data);
        this.page.set(r.metadata.page);
        this.totalPages.set(r.metadata.totalPages);
        this.hasNext.set(r.metadata.hasNext);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
