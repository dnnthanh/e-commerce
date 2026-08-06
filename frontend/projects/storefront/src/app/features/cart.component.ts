import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CartItem, CartView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
/** Authenticated cart with optimistic version updates, save-for-later and server-side validation. */
@Component({
    standalone: true, selector: 'market-cart', imports: [CommonModule, FormsModule, RouterLink],
    template: `<section class="page">
    <div class="page-title"><div><small>CART / OPTIMISTIC VERSION</small><h1>Giỏ hàng</h1></div><a class="primary" routerLink="/checkout" *ngIf="selected().length">Checkout {{selected().length}} dòng</a></div>
    <div class="error" *ngIf="error()">{{error()}}</div>
    <div class="panel" *ngIf="cart() as c">
      <div class="cart-line" *ngFor="let item of c.items">
        <div><strong>SKU #{{item.skuId}}</strong><small>Seller {{item.sellerId}}</small><label><input type="checkbox" [ngModel]="item.selected!==false" (ngModelChange)="toggle(item,$event)"> Chọn mua</label></div>
        <label>Số lượng<input type="number" min="1" [ngModel]="item.quantity" (ngModelChange)="quantity(item,$event)"></label>
        <b>{{item.priceSnapshot|number}} ₫</b>
        <div class="actions"><button class="ghost" (click)="save(item)">Để sau</button><button class="ghost" (click)="remove(item)">Xóa</button></div>
      </div>
      <div class="empty" *ngIf="!c.items.length">Giỏ hàng đang trống.</div>
      <div class="panel" *ngIf="validation() as v"><strong>{{v.valid?'Cart hợp lệ':'Cart cần cập nhật'}}</strong><pre class="result" *ngIf="!v.valid">{{v|json}}</pre></div>
      <div class="actions"><button (click)="validate()">Kiểm tra giá/tồn kho</button><button class="ghost" (click)="reload()">Reload version</button></div>
      <p class="hint">Giá trong cart là snapshot. Checkout vẫn re-price, re-evaluate promotion và reserve inventory trên backend.</p>
    </div>
  </section>`
})
export class CartComponent implements OnInit {
    readonly cart = signal<CartView | undefined>(undefined);
    readonly validation = signal<any>(undefined);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    selected() { return (this.cart()?.items ?? []).filter(i => i.selected !== false); }
    async ngOnInit() { await this.reload(); }
    async reload() { try {
        this.cart.set(await this.marketplace.cart());
        this.error.set('');
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async quantity(item: CartItem, quantity: number) { if (quantity < 1)
        return; await this.put(item, { quantity }); }
    async toggle(item: CartItem, selected: boolean) { await this.put(item, { selected }); }
    private async put(item: CartItem, patch: Partial<CartItem>) { try {
        const c = this.cart();
        if (!c)
            return;
        this.cart.set(await this.marketplace.putCartItem({ sellerId: item.sellerId, skuId: item.skuId, quantity: patch.quantity ?? item.quantity, priceSnapshot: item.priceSnapshot, selected: patch.selected ?? item.selected !== false, expectedVersion: c.version }));
    }
    catch (e) {
        this.error.set(errorMessage(e));
        await this.reload();
    } }
    async remove(item: CartItem) { try {
        this.cart.set(await this.marketplace.removeCartItem(item.sellerId, item.skuId));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async save(item: CartItem) { try {
        this.cart.set(await this.marketplace.saveForLater(item.sellerId, item.skuId));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async validate() { try {
        this.validation.set(await this.marketplace.validateCart());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
