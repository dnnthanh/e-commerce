import { computed, Injectable, signal } from '@angular/core';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CartItem, CartView, ProductOfferView } from '../../../../../shared/marketplace-types';

export interface StorefrontCartLine {
  item: CartItem;
  offer?: ProductOfferView;
}

/**
 * Customer-facing cart state. Cart version remains authoritative from be-cart; public Catalog
 * lookups only enrich identifiers for presentation.
 */
@Injectable({ providedIn: 'root' })
export class StorefrontCartStore {
  readonly cart = signal<CartView | undefined>(undefined);
  readonly offers = signal<Record<number, ProductOfferView>>({});
  readonly loading = signal(false);
  readonly count = computed(
    () =>
      this.cart()?.items
        .filter((item) => !item.savedForLater)
        .reduce((total, item) => total + item.quantity, 0) ?? 0,
  );

  constructor(private readonly marketplace: MarketplaceApiService) {}

  async refresh(): Promise<CartView> {
    this.loading.set(true);
    try {
      const cart = await this.marketplace.cart();
      this.cart.set(cart);
      await this.enrich(cart);
      return cart;
    } finally {
      this.loading.set(false);
    }
  }

  async ensureLoaded(): Promise<CartView> {
    return this.cart() ?? this.refresh();
  }

  accept(cart: CartView): void {
    this.cart.set(cart);
    void this.enrich(cart).catch(() => undefined);
  }

  line(item: CartItem): StorefrontCartLine {
    return { item, offer: this.offers()[item.skuId] };
  }

  private async enrich(cart: CartView): Promise<void> {
    const known = this.offers();
    const missingSkuIds = [
      ...new Set(cart.items.map((item) => item.skuId).filter((skuId) => !known[skuId])),
    ];
    if (!missingSkuIds.length) return;

    const resolved = await Promise.all(
      missingSkuIds.map(async (skuId) => {
        try {
          const offer = await this.marketplace.offer(skuId);
          return [skuId, offer] as const;
        } catch {
          return undefined;
        }
      }),
    );

    const next = { ...this.offers() };
    for (const entry of resolved) {
      if (entry) next[entry[0]] = entry[1];
    }
    this.offers.set(next);
  }
}
