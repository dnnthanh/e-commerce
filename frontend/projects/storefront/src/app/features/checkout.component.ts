import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import {
  CartItem,
  CartValidation,
  CheckoutResponse,
} from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { PriceDisplayComponent } from '../shared/price-display.component';
import { StatePanelComponent } from '../shared/state-panel.component';
import { StorefrontCartStore } from '../shared/storefront-cart.store';

/** Cart-driven persistent-saga checkout. Idempotency is stable per checkout intent and hidden from UX. */
@Component({
  standalone: true,
  selector: 'market-checkout',
  imports: [CommonModule, FormsModule, RouterLink, PriceDisplayComponent, StatePanelComponent],
  template: `
    <section class="page checkout-page">
      <div class="page-title">
        <div><small>CHECKOUT</small><h1>Xác nhận đơn hàng</h1></div>
        <a routerLink="/cart">← Quay lại giỏ hàng</a>
      </div>

      <market-state-panel *ngIf="cartStore.loading()" kind="loading" message="Đang chuẩn bị checkout…" />
      <market-state-panel
        *ngIf="error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />
      <market-state-panel
        *ngIf="!cartStore.loading() && cart() && selected().length === 0"
        kind="empty"
        message="Bạn chưa chọn sản phẩm nào để checkout."
      />

      <section class="panel validation-panel" *ngIf="validation() as currentValidation">
        <h2>{{ currentValidation.valid ? 'Giỏ hàng đã được xác nhận' : 'Giỏ hàng cần cập nhật' }}</h2>
        <p *ngIf="currentValidation.valid" class="hint">
          Backend đã xác nhận giá/tồn kho hiện tại. Hệ thống sẽ kiểm tra lại ngay trước khi tạo checkout.
        </p>
        <ng-container *ngIf="!currentValidation.valid">
          <p class="hint">Hãy quay lại giỏ hàng và xử lý các thay đổi trước khi đặt hàng.</p>
          <div class="validation-row" *ngFor="let violation of currentValidation.violations || []">
            <strong>{{ violation.code }}</strong>
            <span>{{ violation.message }}</span>
          </div>
          <a class="ghost link-button" routerLink="/cart">Quay lại giỏ hàng</a>
        </ng-container>
      </section>

      <div class="checkout-layout" *ngIf="selected().length && validation()?.valid && !result()">
        <div class="checkout-main">
          <section class="panel">
            <small>GIAO HÀNG LOCAL DEMO</small>
            <h2>Thông tin nhận hàng</h2>
            <p class="hint">
              Backend hiện nhận warehouse để mô phỏng allocation. Địa chỉ giao hàng hoàn chỉnh sẽ dùng
              contract riêng khi bounded context shipping mở rộng.
            </p>
            <label>
              Kho phục vụ đơn hàng
              <input type="number" min="1" [(ngModel)]="warehouseId" />
            </label>
          </section>

          <section class="panel">
            <small>THANH TOÁN</small>
            <h2>Khuyến mại & phương thức</h2>
            <label>
              Mã khuyến mại
              <input [(ngModel)]="promotionText" placeholder="WELCOME10, FREESHIP" />
            </label>
            <label>
              Phương thức thanh toán
              <select [(ngModel)]="provider">
                <option value="MOMO">MoMo sandbox</option>
                <option value="VNPAY">VNPAY sandbox</option>
                <option value="VIETQR">VietQR</option>
              </select>
            </label>
          </section>
        </div>

        <aside class="panel checkout-summary">
          <small>ĐƠN HÀNG</small>
          <h2>{{ selected().length }} dòng sản phẩm</h2>
          <div class="checkout-line" *ngFor="let item of selected()">
            <div>
              <strong>{{ cartStore.line(item).offer?.productName || 'Sản phẩm' }}</strong>
              <small>{{ cartStore.line(item).offer?.variantName || cartStore.line(item).offer?.sellerSku || 'Phiên bản' }}</small>
              <small>Cửa hàng #{{ item.sellerId }} · x{{ item.quantity }}</small>
            </div>
            <span>{{ item.priceSnapshot * item.quantity | number: '1.0-0' }} ₫</span>
          </div>
          <div class="summary-row total-row">
            <span>Tạm tính từ giỏ hàng</span>
            <market-price-display [amount]="subtotal()" />
          </div>
          <p class="hint">Checkout sẽ re-price, đánh giá promotion và reserve inventory trên backend.</p>
          <button
            class="primary checkout-submit"
            (click)="submit()"
            [disabled]="loading() || !selected().length || validation()?.valid !== true"
          >
            {{ loading() ? 'Đang xử lý…' : 'Đặt hàng' }}
          </button>
        </aside>
      </div>

      <section class="panel checkout-result" *ngIf="result() as checkoutResult">
        <span class="success-mark">✓</span>
        <small>CHECKOUT {{ checkoutResult.status }}</small>
        <h1>{{ checkoutResult.orderNo ? 'Đơn hàng đã được tạo' : 'Checkout đang được xử lý' }}</h1>
        <p *ngIf="checkoutResult.orderNo">Mã đơn: <strong>{{ checkoutResult.orderNo }}</strong></p>
        <p>Thanh toán: <strong>{{ checkoutResult.paymentStatus }}</strong></p>
        <div class="actions">
          <a class="primary" *ngIf="checkoutResult.orderNo" [routerLink]="['/orders', checkoutResult.orderNo]">
            Xem đơn hàng
          </a>
          <a class="ghost link-button" *ngIf="checkoutResult.redirectUrl" [href]="checkoutResult.redirectUrl">
            Mở trang thanh toán
          </a>
          <a routerLink="/">Tiếp tục mua sắm</a>
        </div>
      </section>
    </section>
  `,
})
export class CheckoutComponent implements OnInit {
  readonly cart = this.cartStore.cart;
  readonly validation = signal<CartValidation | undefined>(undefined);
  readonly result = signal<CheckoutResponse | undefined>(undefined);
  readonly loading = signal(false);
  readonly error = signal<UiErrorView | undefined>(undefined);

  private readonly checkoutKey = crypto.randomUUID();
  warehouseId = 1;
  provider = 'MOMO';
  promotionText = '';

  constructor(
    private readonly marketplace: MarketplaceApiService,
    readonly cartStore: StorefrontCartStore,
  ) {}

  async ngOnInit(): Promise<void> {
    try {
      await this.cartStore.refresh();
      this.validation.set(await this.marketplace.validateCart());
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
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

  async submit(): Promise<void> {
    if (this.loading() || !this.selected().length) return;
    this.loading.set(true);
    this.error.set(undefined);
    try {
      const validation = await this.marketplace.validateCart();
      this.validation.set(validation);
      if (!validation.valid) return;

      const items = this.selected().map((item) => ({
        sellerId: item.sellerId,
        skuId: item.skuId,
        warehouseId: Math.max(1, Number(this.warehouseId) || 1),
        quantity: item.quantity,
      }));
      const promotionCodes = this.promotionText
        .split(',')
        .map((code) => code.trim())
        .filter(Boolean);
      this.result.set(
        await this.marketplace.checkout(this.checkoutKey, items, promotionCodes, this.provider),
      );
    } catch (error) {
      this.error.set(uiError(error));
    } finally {
      this.loading.set(false);
    }
  }
}
