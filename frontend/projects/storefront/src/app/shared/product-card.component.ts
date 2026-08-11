import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { MediaVariant } from '../../../../../shared/marketplace-types';

/** Reusable storefront product card backed by Catalog and Media public contracts. */
@Component({
  standalone: true,
  selector: 'market-product-card',
  imports: [CommonModule, RouterLink],
  template: `
    <a class="product-card" [routerLink]="['/product', productId]">
      <div class="thumb product-image-frame">
        <img
          *ngIf="imageUrl && !imageFailed; else imageFallback"
          class="product-image"
          [src]="imageUrl"
          [alt]="name"
          loading="lazy"
          decoding="async"
          (error)="imageFailed = true"
        />
        <ng-template #imageFallback><span>{{ initials() }}</span></ng-template>
      </div>
      <div class="product-meta">
        <small *ngIf="sellerId !== undefined">Cửa hàng #{{ sellerId }}</small>
        <small *ngIf="rating !== undefined">★ {{ rating | number: '1.1-1' }}</small>
      </div>
      <h3>{{ name }}</h3>
      <strong *ngIf="price !== undefined; else discoverPrice">
        {{ price | number: '1.0-0' }} ₫
      </strong>
      <ng-template #discoverPrice><span class="price-link">Xem giá và phiên bản →</span></ng-template>
    </a>
  `,
  styles: [
    `
      .product-image-frame {
        overflow: hidden;
      }
      .product-image {
        width: 100%;
        height: 100%;
        display: block;
        object-fit: cover;
      }
    `,
  ],
})
export class ProductCardComponent implements OnChanges {
  @Input({ required: true }) productId!: number;
  @Input({ required: true }) name = '';
  @Input() sellerId?: number;
  @Input() price?: number;
  @Input() rating?: number;

  imageUrl?: string;
  imageFailed = false;
  private loadedProductId?: number;
  private loadSequence = 0;

  constructor(private readonly marketplace: MarketplaceApiService) {}

  ngOnChanges(): void {
    if (!this.productId || this.loadedProductId === this.productId) return;
    this.loadedProductId = this.productId;
    void this.loadImage(this.productId);
  }

  initials(): string {
    return this.name.trim().slice(0, 2).toUpperCase() || 'NM';
  }

  private async loadImage(productId: number): Promise<void> {
    const sequence = ++this.loadSequence;
    this.imageUrl = undefined;
    this.imageFailed = false;
    try {
      const variants = await this.marketplace.productMedia(productId);
      if (sequence !== this.loadSequence) return;
      this.imageUrl = this.pickCardVariant(variants)?.url;
    } catch {
      if (sequence === this.loadSequence) this.imageFailed = true;
    }
  }

  private pickCardVariant(variants: MediaVariant[]): MediaVariant | undefined {
    const placements = ['HOME_CARD', 'SEARCH_CARD', 'THUMBNAIL', 'PRODUCT_DETAIL'];
    return placements
      .map((placement) => variants.find((variant) => variant.placement === placement))
      .find((variant): variant is MediaVariant => variant !== undefined) ?? variants[0];
  }
}
