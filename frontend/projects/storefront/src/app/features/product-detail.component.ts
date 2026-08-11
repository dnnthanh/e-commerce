import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import {
  CommentThread,
  MediaVariant,
  PriceView,
  ProductOfferView,
  ProductView,
  ReviewSummary,
  ReviewView,
} from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { PriceDisplayComponent } from '../shared/price-display.component';
import { StatePanelComponent } from '../shared/state-panel.component';
import { StorefrontCartStore } from '../shared/storefront-cart.store';

/** Product 360 page combining public Catalog offers with Pricing, Media, community and Cart. */
@Component({
  standalone: true,
  selector: 'market-product-detail',
  imports: [CommonModule, FormsModule, RouterLink, PriceDisplayComponent, StatePanelComponent],
  template: `
    <section class="page">
      <market-state-panel *ngIf="loading()" kind="loading" message="Đang tải thông tin sản phẩm…" />
      <market-state-panel
        *ngIf="!loading() && error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />

      <ng-container *ngIf="!loading() && product() as product">
        <div class="detail-grid">
          <section>
            <div class="gallery-main product-gallery-main">
              <img
                *ngIf="selectedMedia() as selected; else galleryFallback"
                [src]="selected.url"
                [alt]="product.name"
                decoding="async"
                (error)="selectedMedia.set(undefined)"
              />
              <ng-template #galleryFallback>{{ product.name.slice(0, 2).toUpperCase() }}</ng-template>
            </div>
            <div class="variant-strip" *ngIf="mediaVariants().length">
              <button
                type="button"
                class="media-thumb"
                *ngFor="let media of mediaVariants(); let index = index"
                [class.selected]="selectedMedia()?.objectKey === media.objectKey"
                (click)="selectedMedia.set(media)"
                [attr.aria-label]="'Xem ảnh ' + (index + 1)"
              >
                <img [src]="media.url" [alt]="product.name + ' - ảnh ' + (index + 1)" loading="lazy" />
                <small>{{ media.placement }}</small>
              </button>
            </div>
          </section>

          <section class="panel product-info">
            <small>Cửa hàng #{{ product.sellerId }} · Danh mục #{{ product.categoryId }}</small>
            <h1>{{ product.name }}</h1>
            <p>{{ product.description || 'Sản phẩm chưa có mô tả.' }}</p>

            <div class="rating-row" *ngIf="summary() as reviewSummary">
              <strong>{{ reviewSummary.averageRating | number: '1.1-1' }} ★</strong>
              <span>{{ reviewSummary.reviewCount }} đánh giá</span>
            </div>

            <div class="purchase-price" *ngIf="price() as currentPrice">
              <market-price-display [amount]="currentPrice.amount" [currency]="currentPrice.currency" />
              <small>Giá hiện hành · cập nhật {{ currentPrice.resolvedAt | date: 'short' }}</small>
            </div>

            <div class="offer-section">
              <h3>Chọn phiên bản</h3>
              <market-state-panel
                *ngIf="offers().length === 0"
                kind="empty"
                message="Sản phẩm hiện chưa có phiên bản sẵn sàng để bán."
              />
              <div class="offer-grid" *ngIf="offers().length">
                <button
                  type="button"
                  class="offer-option"
                  *ngFor="let offer of offers()"
                  [class.selected]="selectedOffer()?.skuId === offer.skuId"
                  (click)="selectOffer(offer)"
                >
                  <strong>{{ offer.variantName || offer.sellerSku }}</strong>
                  <small>{{ offer.sellerSku }}</small>
                </button>
              </div>
            </div>

            <ng-container *ngIf="selectedOffer() as offer">
              <label>
                Số lượng
                <input
                  type="number"
                  min="1"
                  [max]="offer.purchaseLimit"
                  [(ngModel)]="quantity"
                  aria-label="Số lượng sản phẩm"
                />
              </label>
              <small class="hint">Tối đa {{ offer.purchaseLimit }} sản phẩm cho phiên bản này.</small>
            </ng-container>

            <div class="actions">
              <button
                class="primary"
                (click)="addToCart()"
                [disabled]="adding() || !selectedOffer() || !price()"
                *ngIf="auth.authenticated()"
              >
                {{ adding() ? 'Đang thêm…' : 'Thêm vào giỏ' }}
              </button>
              <button class="primary" (click)="auth.login()" *ngIf="!auth.authenticated()">
                Đăng nhập để mua
              </button>
              <button class="ghost" (click)="followSeller()" *ngIf="auth.authenticated()">
                Theo dõi cửa hàng
              </button>
            </div>
            <p class="success" *ngIf="success()">{{ success() }}</p>
            <a class="community-link" [routerLink]="['/product', productId, 'community']">
              Xem toàn bộ hỏi đáp & đánh giá →
            </a>
          </section>
        </div>

        <section class="detail-columns">
          <div class="panel">
            <div class="section-inline"><h2>Hỏi đáp gần đây</h2></div>
            <div class="comment" *ngFor="let comment of comments()">
              <strong>Khách hàng {{ comment.authorId }}</strong>
              <span class="status-chip">{{ comment.status }}</span>
              <p>{{ comment.content || '[Nội dung đã xóa]' }}</p>
              <small>{{ comment.replyCount }} phản hồi · {{ comment.createdAt | date: 'short' }}</small>
            </div>
            <p class="hint" *ngIf="comments().length === 0">Chưa có câu hỏi nào.</p>
            <div class="composer" *ngIf="auth.authenticated()">
              <textarea [(ngModel)]="question" placeholder="Hỏi cửa hàng về sản phẩm…"></textarea>
              <button (click)="ask()">Gửi câu hỏi</button>
            </div>
          </div>

          <div class="panel">
            <h2>Đánh giá khách hàng</h2>
            <div class="review" *ngFor="let review of reviews()">
              <strong>{{ review.rating }}★ {{ review.title }}</strong>
              <p>{{ review.content }}</p>
              <small>{{ review.createdAt | date: 'short' }}</small>
            </div>
            <p class="hint" *ngIf="reviews().length === 0">Sản phẩm chưa có đánh giá.</p>
          </div>
        </section>
      </ng-container>
    </section>
  `,
  styles: [
    `
      .product-gallery-main {
        overflow: hidden;
      }
      .product-gallery-main img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        display: block;
        background: white;
      }
      .media-thumb {
        min-width: 112px;
        width: 112px;
        padding: 8px;
        border-radius: 12px;
        background: white;
        color: #26324a;
        border: 1px solid #e3e7ef;
        display: block;
      }
      .media-thumb.selected {
        border-color: #496ce5;
        box-shadow: 0 0 0 2px #496ce51e;
      }
      .media-thumb img {
        width: 94px;
        height: 78px;
        display: block;
        object-fit: cover;
        border-radius: 8px;
      }
      .media-thumb small {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-top: 7px;
      }
    `,
  ],
})
export class ProductDetailComponent implements OnInit {
  readonly product = signal<ProductView | undefined>(undefined);
  readonly offers = signal<ProductOfferView[]>([]);
  readonly selectedOffer = signal<ProductOfferView | undefined>(undefined);
  readonly mediaVariants = signal<MediaVariant[]>([]);
  readonly selectedMedia = signal<MediaVariant | undefined>(undefined);
  readonly comments = signal<CommentThread[]>([]);
  readonly reviews = signal<ReviewView[]>([]);
  readonly summary = signal<ReviewSummary | undefined>(undefined);
  readonly price = signal<PriceView | undefined>(undefined);
  readonly loading = signal(true);
  readonly adding = signal(false);
  readonly error = signal<UiErrorView | undefined>(undefined);
  readonly success = signal('');

  question = '';
  productId = 0;
  quantity = 1;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly marketplace: MarketplaceApiService,
    private readonly cartStore: StorefrontCartStore,
    readonly auth: AuthService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.productId = Number(this.route.snapshot.paramMap.get('id'));
    try {
      const [product, offers, media, comments, reviews, summary] = await Promise.all([
        this.marketplace.product(this.productId),
        this.marketplace.productOffers(this.productId),
        this.marketplace.productMedia(this.productId),
        this.marketplace.comments(this.productId),
        this.marketplace.reviews(this.productId),
        this.marketplace.reviewSummary(this.productId),
      ]);
      this.product.set(product);
      this.offers.set(offers);
      this.mediaVariants.set(media);
      this.selectedMedia.set(
        media.find((variant) => variant.placement === 'PRODUCT_DETAIL') ??
          media.find((variant) => variant.placement === 'HOME_CARD') ??
          media[0],
      );
      this.comments.set(comments.data);
      this.reviews.set(reviews.data);
      this.summary.set(summary);
      this.error.set(undefined);
      if (offers.length) await this.selectOffer(offers[0]);
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }

  async selectOffer(offer: ProductOfferView): Promise<void> {
    this.selectedOffer.set(offer);
    this.quantity = 1;
    this.price.set(undefined);
    this.success.set('');
    try {
      this.price.set(await this.marketplace.price(offer.skuId, offer.sellerId));
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async addToCart(): Promise<void> {
    const offer = this.selectedOffer();
    const currentPrice = this.price();
    if (!offer || !currentPrice || this.adding()) return;

    this.adding.set(true);
    this.success.set('');
    try {
      const cart = await this.cartStore.ensureLoaded();
      const safeQuantity = Math.max(1, Math.min(Number(this.quantity) || 1, offer.purchaseLimit));
      const updated = await this.marketplace.putCartItem({
        sellerId: offer.sellerId,
        skuId: offer.skuId,
        quantity: safeQuantity,
        priceSnapshot: currentPrice.amount,
        selected: true,
        expectedVersion: cart.version,
      });
      this.cartStore.accept(updated);
      this.quantity = safeQuantity;
      this.error.set(undefined);
      this.success.set('Đã thêm sản phẩm vào giỏ hàng.');
    } catch (error) {
      this.error.set(uiError(error));
      await this.cartStore.refresh().catch(() => undefined);
    } finally {
      this.adding.set(false);
    }
  }

  async ask(): Promise<void> {
    const product = this.product();
    if (!product || !this.question.trim()) return;
    try {
      await this.marketplace.createComment(this.productId, product.sellerId, this.question);
      this.question = '';
      this.comments.set((await this.marketplace.comments(this.productId)).data);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async followSeller(): Promise<void> {
    const product = this.product();
    if (!product) return;
    try {
      await this.marketplace.followSeller(product.sellerId);
      this.success.set('Bạn đang theo dõi cập nhật từ cửa hàng này.');
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }
}
