import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CommentThread, MediaVariant, PriceView, ProductView, ReviewSummary, ReviewView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
/** Product 360 page combining Catalog, Pricing, Media, Review, Comment, Seller subscription and Cart. */
@Component({ standalone: true, selector: 'market-product-detail', imports: [CommonModule, FormsModule, RouterLink], template: `<section class="page" *ngIf="product() as p">
  <div class="detail-grid"><section><div class="gallery-main">{{p.name.slice(0,2).toUpperCase()}}</div><div class="variant-strip"><div *ngFor="let v of variants()"><small>{{v.placement}}</small><span>{{v.width}}×{{v.height}} {{v.format}}</span><code>{{v.objectKey}}</code></div></div></section>
  <section class="panel product-info"><small>Seller {{p.sellerId}} · Category {{p.categoryId}}</small><h1>{{p.name}}</h1><p>{{p.description}}</p><div class="status-chip">{{p.status}}</div><div *ngIf="price() as pr"><h2>{{pr.amount|number}} {{pr.currency}}</h2><small>effective rule #{{pr.ruleId||'base'}} · {{pr.resolvedAt|date:'short'}}</small></div><label>SKU dùng cho pricing/cart<input type="number" [(ngModel)]="skuId"></label><label>Số lượng<input type="number" min="1" [(ngModel)]="quantity"></label><div class="actions"><button class="primary" (click)="addToCart()" *ngIf="auth.authenticated()">Thêm vào giỏ</button><button class="ghost" (click)="resolvePrice()">Resolve price</button><button class="ghost" (click)="followSeller()" *ngIf="auth.authenticated()">Theo dõi seller</button></div><div class="actions"><a [routerLink]="['/product',productId,'community']">Mở community đầy đủ →</a><span class="hint">Seller #{{p.sellerId}} · public shop lookup requires shopId from the seller projection.</span></div></section></div>
  <section class="detail-columns"><div class="panel"><h2>Hỏi đáp gần đây</h2><div class="comment" *ngFor="let c of comments()"><strong>{{c.authorId}}</strong><span class="status-chip">{{c.status}}</span><p>{{c.content||'[deleted]'}}</p><small>{{c.replyCount}} replies · {{c.createdAt|date:'short'}}</small></div><div class="composer" *ngIf="auth.authenticated()"><textarea [(ngModel)]="question" placeholder="Hỏi seller về sản phẩm…"></textarea><button (click)="ask()">Gửi câu hỏi</button></div></div><div class="panel"><h2>Đánh giá <span *ngIf="summary() as s">{{s.averageRating|number:'1.1-1'}}★ ({{s.reviewCount}})</span></h2><div class="review" *ngFor="let r of reviews()"><strong>{{r.rating}}★ {{r.title}}</strong><p>{{r.content}}</p><small>{{r.createdAt|date:'short'}}</small></div></div></section>
  <div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class ProductDetailComponent implements OnInit {
    readonly product = signal<ProductView | undefined>(undefined);
    readonly variants = signal<MediaVariant[]>([]);
    readonly comments = signal<CommentThread[]>([]);
    readonly reviews = signal<ReviewView[]>([]);
    readonly summary = signal<ReviewSummary | undefined>(undefined);
    readonly price = signal<PriceView | undefined>(undefined);
    readonly error = signal('');
    question = '';
    productId = 0;
    skuId = 1000001;
    quantity = 1;
    constructor(private readonly route: ActivatedRoute, private readonly marketplace: MarketplaceApiService, readonly auth: AuthService) { }
    async ngOnInit() { this.productId = Number(this.route.snapshot.paramMap.get('id')); try {
        const [p, m, c, r, s] = await Promise.all([this.marketplace.product(this.productId), this.marketplace.productMedia(this.productId), this.marketplace.comments(this.productId), this.marketplace.reviews(this.productId), this.marketplace.reviewSummary(this.productId)]);
        this.product.set(p);
        this.variants.set(m);
        this.comments.set(c.data);
        this.reviews.set(r.data);
        this.summary.set(s);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async resolvePrice() { const p = this.product(); if (!p)
        return; try {
        this.price.set(await this.marketplace.price(this.skuId, p.sellerId));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async addToCart() { const p = this.product(); if (!p)
        return; try {
        const pr = this.price() ?? await this.marketplace.price(this.skuId, p.sellerId);
        const cart = await this.marketplace.cart();
        await this.marketplace.putCartItem({ sellerId: p.sellerId, skuId: this.skuId, quantity: this.quantity, priceSnapshot: pr.amount, selected: true, expectedVersion: cart.version });
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async ask() { const p = this.product(); if (!p || !this.question.trim())
        return; try {
        await this.marketplace.createComment(this.productId, p.sellerId, this.question);
        this.question = '';
        this.comments.set((await this.marketplace.comments(this.productId)).data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async followSeller() { const p = this.product(); if (!p)
        return; try {
        await this.marketplace.followSeller(p.sellerId);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
