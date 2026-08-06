import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../../../shared/api.service';
import { errorMessage } from '../../../../../shared/ui-state';
interface Product {
    id?: number;
    productId?: number;
    name: string;
    sellerId?: number;
    status?: string;
    basePrice?: number;
}
/** Marketplace landing page backed by the public Catalog API. */
@Component({
    standalone: true,
    selector: 'market-home',
    imports: [CommonModule, RouterLink],
    template: `
    <section class="hero">
      <div>
        <span class="eyebrow">MULTI-SELLER MARKETPLACE</span>
        <h1>Mua sắm từ nhiều nhà bán trong một checkout.</h1>
        <p>Giá được tính lại lúc checkout, tồn kho được reservation, thanh toán sandbox và notification realtime.</p>
        <div class="hero-actions"><a class="primary" routerLink="/search">Khám phá sản phẩm</a><a routerLink="/notifications">Xem thông báo</a></div>
      </div>
      <div class="hero-metrics"><strong>3</strong><span>Payment providers</span><strong>Realtime</strong><span>Notification inbox</span></div>
    </section>
    <section class="section-head"><div><small>DISCOVERY</small><h2>Sản phẩm nổi bật</h2></div><a routerLink="/search">Xem tất cả →</a></section>
    <div class="loading" *ngIf="loading()">Đang tải catalog…</div>
    <div class="error" *ngIf="error()">{{error()}}</div>
    <section class="product-grid" *ngIf="!loading()">
      <a class="product-card" *ngFor="let p of products()" [routerLink]="['/product', p.id ?? p.productId]">
        <div class="thumb"><span>{{p.name.slice(0,2).toUpperCase()}}</span></div>
        <small>Seller {{p.sellerId ?? '—'}}</small><h3>{{p.name}}</h3><strong>{{(p.basePrice ?? 0) | number}} ₫</strong>
      </a>
      <div class="empty" *ngIf="products().length===0">Chưa có sản phẩm seed. Chạy <code>data/seed-demo</code> để xem dữ liệu demo.</div>
    </section>`
})
export class HomeComponent implements OnInit {
    /** Products rendered on the landing page. */ readonly products = signal<Product[]>([]);
    /** Loading indicator. */ readonly loading = signal(true);
    /** Loading error. */ readonly error = signal('');
    constructor(private readonly api: ApiService) { }
    async ngOnInit(): Promise<void> { try {
        this.products.set((await this.api.getPage<Product>('/products?page=0&size=12')).data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    }
    finally {
        this.loading.set(false);
    } }
}
