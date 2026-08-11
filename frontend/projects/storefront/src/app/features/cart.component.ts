import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { CartItem, CartValidation } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { PriceDisplayComponent } from '../shared/price-display.component';
import { StatePanelComponent } from '../shared/state-panel.component';
import { StorefrontCartLine, StorefrontCartStore } from '../shared/storefront-cart.store';

interface CartSellerGroup {
  sellerId: number;
  lines: StorefrontCartLine[];
}

/** Customer cart preserving backend optimistic-version semantics while enriching SKU ids for display. */
@Component({
  standalone: true,
  selector: 'market-cart',
  imports: [CommonModule, FormsModule, RouterLink, PriceDisplayComponent, StatePanelComponent],
  template: `
    <section class="page">
      <div class="page-title">
        <div><small>GIỎ HÀNG</small><h1>Giỏ hàng của bạn</h1></div>
        <a class="primary" routerLink="/checkout" *ngIf="selected().length">
          Tiếp tục checkout · {{ selected().length }} dòng
        </a>
      </div>

      <market-state-panel *ngIf="cartStore.loading()" kind="loading" message="Đang tải giỏ hàng…" />
      <market-state-panel
        *ngIf="error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />
      <market-state-panel
        *ngIf="!cartStore.loading() && cart() && cart()!.items.length === 0"
        kind="empty"
        message="Giỏ hàng đang trống. Hãy chọn một sản phẩm để bắt đầu."
      />

      <div class="cart-layout" *ngIf="cart() as currentCart">
        <div>
          <section class="panel cart-seller" *ngFor="let group of sellerGroups()">
            <div class="seller-heading">
              <div><small>CỬA HÀNG</small><h2>Cửa hàng #{{ group.sellerId }}</h2></div>
            </div>

            <div class="cart-line" *ngFor="let line of group.lines">
              <div class="cart-product">
                <div class="mini-thumb">{{ (line.offer?.productName || 'SP').slice(0, 2).toUpperCase() }}</div>
                <div>
                  <strong>{{ line.offer?.productName || 'Sản phẩm trong giỏ' }}</strong>
                  <small>{{ line.offer?.variantName || line.offer?.sellerSku || 'Phiên bản đang được cập nhật' }}</small>
                  <label class="inline-check" *ngIf="!line.item.savedForLater">
                    <input
                      type="checkbox"
                      [ngModel]="line.item.selected !== false"
                      (ngModelChange)="toggle(line.item, $event)"
                    />
                    Chọn mua
                  </label>
                  <span class="saved-label" *ngIf="line.item.savedForLater">Đã lưu để mua sau</span>
                </div>
              </div>

              <label class="quantity-control">
                Số lượng
                <input
                  type="number"
                  min="1"
                  [max]="line.offer?.purchaseLimit || 999"
                  [disabled]="line.item.savedForLater === true"
                  [ngModel]="line.item.quantity"
                  (ngModelChange)="quantity(line.item, $event)"
                />
              </label>

              <div class="line-price">
                <market-price-display [amount]="line.item.priceSnapshot * line.item.quantity" />
                <small>{{ line.item.priceSnapshot | number: '1.0-0' }} ₫ / sản phẩm</small>
              </div>

              <div class="actions compact-actions">
                <button class="ghost" *ngIf="!line.item.savedForLater" (click)="save(line.item)">Mua sau</button>
                <button class="ghost" *ngIf="line.item.savedForLater" (click)="moveToCart(line.item)">Chuyển vào giỏ</button>
                <button class="danger-link" (click)="remove(line.item)">Xóa</button>
              </div>
            </div>
          </section>
        </div>

        <aside class="panel cart-summary" *ngIf="currentCart.items.length">
          <small>TÓM TẮT</small>
          <h2>Thanh toán</h2>
          <div class="summary-row"><span>Sản phẩm đã chọn</span><strong>{{ selected().length }}</strong></div>
          <div class="summary-row"><span>Tạm tính</span><strong>{{ subtotal() | number: '1.0-0' }} ₫</strong></div>
          <p class="hint">Giá cuối cùng và tồn kho sẽ được backend xác nhận lại ở checkout.</p>
          <button (click)="validate()">Kiểm tra giá & tồn kho</button>
          <a class="primary block-link" routerLink="/checkout" *ngIf="selected().length">Đi tới checkout</a>
          <button class="ghost" (click)="reload()">Tải lại giỏ hàng</button>
        </aside>
      </div>

      <section class="panel validation-panel" *ngIf="validation() as result">
        <h2>{{ result.valid ? 'Giỏ hàng sẵn sàng' : 'Cần cập nhật giỏ hàng' }}</h2>
        <p *ngIf="result.valid">Giá và tồn kho hiện tại đã vượt qua bước kiểm tra.</p>
        <div class="validation-row" *ngFor="let violation of result.violations || []">
          <strong>{{ violation.code }}</strong>
          <span>{{ violation.message }}</span>
        </div>
      </section>
    </section>
  `,
})
export class CartComponent implements OnInit {
  readonly cart = this.cartStore.cart;
  readonly validation = signal<CartValidation | undefined>(undefined);
  readonly error = signal<UiErrorView | undefined>(undefined);

  constructor(
    private readonly marketplace: MarketplaceApiService,
    readonly cartStore: StorefrontCartStore,
  ) {}

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  selected(): CartItem[] {
    return (this.cart()?.items ?? []).filter(
      (item) => !item.savedForLater && item.selected !== false,
    );
  }

  subtotal(): number {
    return this.selected().reduce(
      (total, item) => total + item.priceSnapshot * item.quantity,
      0,
    );
  }

  sellerGroups(): CartSellerGroup[] {
    const groups = new Map<number, StorefrontCartLine[]>();
    for (const item of this.cart()?.items ?? []) {
      const lines = groups.get(item.sellerId) ?? [];
      lines.push(this.cartStore.line(item));
      groups.set(item.sellerId, lines);
    }
    return [...groups.entries()].map(([sellerId, lines]) => ({ sellerId, lines }));
  }

  async reload(): Promise<void> {
    try {
      await this.cartStore.refresh();
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async quantity(item: CartItem, quantity: number): Promise<void> {
    const limit = this.cartStore.line(item).offer?.purchaseLimit ?? Number.MAX_SAFE_INTEGER;
    const nextQuantity = Math.min(Number(quantity) || 0, limit);
    if (nextQuantity < 1) return;
    await this.put(item, { quantity: nextQuantity });
  }

  async toggle(item: CartItem, selected: boolean): Promise<void> {
    await this.put(item, { selected });
  }

  async remove(item: CartItem): Promise<void> {
    try {
      this.cartStore.accept(await this.marketplace.removeCartItem(item.sellerId, item.skuId));
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async save(item: CartItem): Promise<void> {
    try {
      this.cartStore.accept(await this.marketplace.saveForLater(item.sellerId, item.skuId));
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async moveToCart(item: CartItem): Promise<void> {
    try {
      this.cartStore.accept(await this.marketplace.moveToCart(item.sellerId, item.skuId));
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  async validate(): Promise<void> {
    try {
      this.validation.set(await this.marketplace.validateCart());
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  private async put(item: CartItem, patch: Partial<CartItem>): Promise<void> {
    const currentCart = this.cart();
    if (!currentCart) return;
    try {
      const updated = await this.marketplace.putCartItem({
        sellerId: item.sellerId,
        skuId: item.skuId,
        quantity: patch.quantity ?? item.quantity,
        priceSnapshot: item.priceSnapshot,
        selected: patch.selected ?? item.selected !== false,
        expectedVersion: currentCart.version,
      });
      this.cartStore.accept(updated);
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
      await this.cartStore.refresh().catch(() => undefined);
    }
  }
}
