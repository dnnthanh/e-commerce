import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CommentThread, ReviewView } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';

@Component({
  standalone: true,
  selector: 'admin-moderation',
  imports: [
    CommonModule,
    FormsModule,
    AdminPageHeaderComponent,
    AdminStateComponent,
    StatusBadgeComponent,
  ],
  template: `
    <section>
      <admin-page-header
        eyebrow="REVIEW / COMMENT MODERATION"
        title="Community triage"
        description="Reviews are read-only in Feature 015. Comment visibility uses the existing COMMENT_MODERATE domain command through a browser-safe private API."
      />

      <form class="filters" (ngSubmit)="load()">
        <label>Product ID<input type="number" min="1" [(ngModel)]="productId" name="productId"></label>
        <label *ngIf="sellerScopes.length">
          Seller scope
          <select [(ngModel)]="sellerId" name="sellerId">
            <option [ngValue]="undefined">All permitted / visible</option>
            <option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option>
          </select>
        </label>
        <button type="submit" [disabled]="loading() || productId < 1">Load triage</button>
      </form>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Moderation data unavailable"
        [message]="problem.message"
        [traceId]="problem.traceId"
        [retryable]="true"
        (retry)="load()"
      />

      <div class="two-col">
        <div class="panel">
          <div class="panel-title"><h2>Reviews</h2><span class="status-chip">read-only triage</span></div>
          <admin-state *ngIf="loading()" kind="loading" title="Loading reviews" message="Reading current product reviews." />
          <admin-state *ngIf="!loading() && !failure() && reviews().length === 0" kind="empty" title="No reviews" message="No review rows matched this product." />
          <div class="mini-row" *ngFor="let review of reviews()">
            <div>
              <b>{{ review.rating }}★ {{ review.title }}</b>
              <small>#{{ review.id }} · {{ review.createdAt | date:'short' }}</small>
              <p>{{ review.content }}</p>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title"><h2>Comments</h2><span class="status-chip">visibility moderation</span></div>
          <admin-state *ngIf="loading()" kind="loading" title="Loading comments" message="Reading current comment threads." />
          <admin-state *ngIf="!loading() && !failure() && comments().length === 0" kind="empty" title="No comments" message="No comment threads matched the current filter." />
          <div class="mini-row" *ngFor="let comment of comments()">
            <div>
              <b>{{ comment.authorId }}</b>
              <small><admin-status-badge [status]="comment.status" /> · {{ comment.replyCount }} replies</small>
              <p>{{ comment.content || '[masked]' }}</p>
            </div>
            <div class="actions" *ngIf="auth.has('COMMENT_MODERATE')">
              <button
                *ngIf="comment.status !== 'HIDDEN'"
                class="danger"
                type="button"
                [disabled]="pendingThread() === comment.id"
                (click)="hide(comment)"
              >Hide</button>
              <button
                *ngIf="comment.status === 'HIDDEN'"
                type="button"
                [disabled]="pendingThread() === comment.id"
                (click)="unhide(comment)"
              >Unhide</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class ModerationComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly marketplace = inject(MarketplaceApiService);
  productId = 1;
  sellerId?: number;
  readonly reviews = signal<ReviewView[]>([]);
  readonly comments = signal<CommentThread[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly loading = signal(false);
  readonly pendingThread = signal<string | undefined>(undefined);

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  async ngOnInit(): Promise<void> {
    if (this.sellerScopes.length === 1) this.sellerId = this.sellerScopes[0];
    await this.load();
  }

  async load(): Promise<void> {
    if (this.productId < 1) return;
    this.loading.set(true);
    this.failure.set(undefined);
    try {
      const [reviews, comments] = await Promise.all([
        this.marketplace.reviews(this.productId),
        this.marketplace.comments(this.productId, this.sellerId),
      ]);
      this.reviews.set(reviews.data);
      this.comments.set(comments.data);
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async hide(comment: CommentThread): Promise<void> {
    if (!confirm(`Hide comment ${comment.id}? It will disappear from normal listing.`)) return;
    await this.mutate(comment, () => this.marketplace.hideComment(comment.id));
  }

  async unhide(comment: CommentThread): Promise<void> {
    if (!confirm(`Restore comment ${comment.id} to normal listing?`)) return;
    await this.mutate(comment, () => this.marketplace.unhideComment(comment.id));
  }

  private async mutate(comment: CommentThread, command: () => Promise<CommentThread>): Promise<void> {
    this.pendingThread.set(comment.id);
    this.failure.set(undefined);
    try {
      const updated = await command();
      this.comments.update(rows => rows.map(row => row.id === updated.id ? updated : row));
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.pendingThread.set(undefined);
    }
  }
}
