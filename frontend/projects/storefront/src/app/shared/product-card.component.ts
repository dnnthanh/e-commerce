import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Reusable storefront product card for Catalog and Search read models. */
@Component({
  standalone: true,
  selector: 'market-product-card',
  imports: [CommonModule, RouterLink],
  template: `
    <a class="product-card" [routerLink]="['/product', productId]">
      <div class="thumb"><span>{{ initials() }}</span></div>
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
})
export class ProductCardComponent {
  @Input({ required: true }) productId!: number;
  @Input({ required: true }) name = '';
  @Input() sellerId?: number;
  @Input() price?: number;
  @Input() rating?: number;

  initials(): string {
    return this.name.trim().slice(0, 2).toUpperCase() || 'NM';
  }
}
