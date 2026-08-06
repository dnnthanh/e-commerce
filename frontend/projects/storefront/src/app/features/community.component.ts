import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CommentThread, ReviewSummary, ReviewView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-community', imports: [CommonModule, FormsModule], template: `<section class="page"><div class="page-title"><div><small>COMMUNITY</small><h1>Đánh giá & hỏi đáp sản phẩm #{{productId}}</h1></div></div><div class="error" *ngIf="error()">{{error()}}</div><div class="detail-columns"><div class="panel"><h2>Reviews <span *ngIf="summary() as s">{{s.averageRating|number:'1.1-1'}}★ / {{s.reviewCount}}</span></h2><div class="review" *ngFor="let r of reviews()"><strong>{{r.rating}}★ {{r.title}}</strong><p>{{r.content}}</p><small>{{r.createdAt|date:'medium'}}</small><button class="ghost" (click)="helpful(r)">Helpful</button></div><h3>Viết review từ order line</h3><label>Order line ID<input type="number" [(ngModel)]="orderLineId"></label><label>Rating<input type="number" min="1" max="5" [(ngModel)]="rating"></label><label>Title<input [(ngModel)]="title"></label><textarea [(ngModel)]="reviewContent"></textarea><button (click)="createReview()">Đăng review</button></div><div class="panel"><h2>Comments / Q&A</h2><div class="comment" *ngFor="let c of comments()"><strong>{{c.authorId}}</strong><span class="status-chip">{{c.status}}</span><p>{{c.content||'[deleted]'}}</p><small>{{c.replyCount}} replies</small><div class="actions"><button class="ghost" (click)="react(c)">LIKE</button><button class="ghost" (click)="report(c)">Report</button></div></div><label>Seller ID<input type="number" [(ngModel)]="sellerId"></label><textarea [(ngModel)]="commentContent" placeholder="Hỏi người bán về sản phẩm"></textarea><button (click)="createComment()">Đăng câu hỏi</button></div></div></section>` })
export class CommunityComponent implements OnInit {
    productId = 0;
    sellerId = 10001;
    orderLineId = 0;
    rating = 5;
    title = '';
    reviewContent = '';
    commentContent = '';
    readonly summary = signal<ReviewSummary | undefined>(undefined);
    readonly reviews = signal<ReviewView[]>([]);
    readonly comments = signal<CommentThread[]>([]);
    readonly error = signal('');
    constructor(private readonly route: ActivatedRoute, private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { this.productId = Number(this.route.snapshot.paramMap.get('id')); await this.load(); }
    async load() { try {
        const [s, r, c] = await Promise.all([this.marketplace.reviewSummary(this.productId), this.marketplace.reviews(this.productId), this.marketplace.comments(this.productId)]);
        this.summary.set(s);
        this.reviews.set(r.data);
        this.comments.set(c.data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async createReview() { try {
        await this.marketplace.createReview(this.orderLineId, this.rating, this.title, this.reviewContent);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async helpful(r: ReviewView) { try {
        await this.marketplace.markHelpful(r.id, true);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async createComment() { try {
        await this.marketplace.createComment(this.productId, this.sellerId, this.commentContent);
        this.commentContent = '';
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async react(c: CommentThread) { try {
        await this.marketplace.react(c.id, 'LIKE');
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async report(c: CommentThread) { try {
        await this.marketplace.reportComment(c.id, 'SPAM', 'Reported from storefront');
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
