import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CartView, CheckoutResponse } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
/** Cart-driven persistent-saga checkout. The browser does not ask users to type seller/SKU identifiers. */
@Component({ standalone: true, selector: 'market-checkout', imports: [CommonModule, FormsModule, RouterLink], template: `<section class="page narrow">
  <div class="page-title"><div><small>PERSISTENT SAGA / IDEMPOTENT</small><h1>Checkout</h1></div><a routerLink="/cart">← Giỏ hàng</a></div>
  <div class="error" *ngIf="error()">{{error()}}</div>
  <div class="panel" *ngIf="cart() as c">
    <h2>{{selected().length}} sản phẩm được chọn</h2>
    <div class="cart-line" *ngFor="let i of selected()"><div><strong>SKU #{{i.skuId}}</strong><small>Seller {{i.sellerId}}</small></div><span>x{{i.quantity}}</span><b>{{i.priceSnapshot|number}} ₫</b></div>
    <label>Warehouse cho demo local<input type="number" [(ngModel)]="warehouseId"></label>
    <label>Mã khuyến mại<input [(ngModel)]="promotionText" placeholder="WELCOME10, FREESHIP"></label>
    <label>Nhà cung cấp thanh toán<select [(ngModel)]="provider"><option>MOMO</option><option>VNPAY</option><option>VIETQR</option></select></label>
    <div class="panel"><small>IDEMPOTENCY KEY</small><code>{{checkoutKey}}</code><button class="ghost" (click)="regenerate()">Đổi key</button></div>
    <button class="primary" (click)="submit()" [disabled]="loading()||!selected().length">{{loading()?'Đang orchestrate…':'Xác nhận checkout'}}</button>
  </div>
  <div class="panel" *ngIf="result() as r"><h2>Checkout {{r.status}}</h2><p>Order: <strong>{{r.orderNo||'chưa tạo'}}</strong></p><p>Payment: {{r.paymentStatus}}</p><a *ngIf="r.orderNo" [routerLink]="['/orders',r.orderNo]">Theo dõi đơn hàng →</a><a *ngIf="r.redirectUrl" [href]="r.redirectUrl">Mở payment redirect →</a></div>
</section>` })
export class CheckoutComponent implements OnInit {
    readonly cart = signal<CartView | undefined>(undefined);
    readonly result = signal<CheckoutResponse | undefined>(undefined);
    readonly loading = signal(false);
    readonly error = signal('');
    checkoutKey = crypto.randomUUID();
    warehouseId = 1;
    provider = 'MOMO';
    promotionText = '';
    constructor(private readonly marketplace: MarketplaceApiService) { }
    selected() { return (this.cart()?.items ?? []).filter(i => i.selected !== false); }
    async ngOnInit() { try {
        this.cart.set(await this.marketplace.cart());
        await this.marketplace.validateCart();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    regenerate() { this.checkoutKey = crypto.randomUUID(); }
    async submit() { this.loading.set(true); this.error.set(''); try {
        const items = this.selected().map(i => ({ sellerId: i.sellerId, skuId: i.skuId, warehouseId: this.warehouseId, quantity: i.quantity }));
        const codes = this.promotionText.split(',').map(x => x.trim()).filter(Boolean);
        this.result.set(await this.marketplace.checkout(this.checkoutKey, items, codes, this.provider));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    }
    finally {
        this.loading.set(false);
    } }
}
