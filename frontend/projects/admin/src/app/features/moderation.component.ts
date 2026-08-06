import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CommentThread, ReviewView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-moderation', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>REVIEW / COMMENT MODERATION</small><h1>Community triage</h1></div></div><div class="filters"><input type="number" [(ngModel)]="productId" placeholder="Product"><input type="number" [(ngModel)]="sellerId" placeholder="Seller"><button (click)="load()">Load</button></div><div class="error" *ngIf="error()">{{error()}}</div><div class="two-col"><div class="panel"><h2>Reviews</h2><div class="mini-row" *ngFor="let r of reviews()"><div><b>{{r.rating}}★ {{r.title}}</b><small>#{{r.id}} · {{r.createdAt|date:'short'}}</small><p>{{r.content}}</p></div></div></div><div class="panel"><h2>Comments</h2><div class="mini-row" *ngFor="let c of comments()"><div><b>{{c.authorId}}</b><small>{{c.status}} · {{c.replyCount}} replies</small><p>{{c.content||'[masked]'}}</p></div><button class="ghost" (click)="report(c)">Report</button></div></div></div><p class="hint">Hide/unhide comment endpoints are internal service contracts. The browser does not call internal service paths; an admin-facing moderation command should be added to the backend if direct moderator actions are required.</p></section>` })
export class ModerationComponent {
    productId = 1;
    sellerId = 10001;
    readonly reviews = signal<ReviewView[]>([]);
    readonly comments = signal<CommentThread[]>([]);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async load() { try {
        const [r, c] = await Promise.all([this.marketplace.reviews(this.productId), this.marketplace.comments(this.productId, this.sellerId)]);
        this.reviews.set(r.data);
        this.comments.set(c.data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async report(c: CommentThread) { try {
        await this.marketplace.reportComment(c.id, 'ABUSE', 'Admin triage report');
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
