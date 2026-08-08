import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { NotificationPreference } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { StatePanelComponent } from '../shared/state-panel.component';

@Component({
  standalone: true,
  selector: 'market-account',
  imports: [CommonModule, FormsModule, RouterLink, StatePanelComponent],
  template: `
    <section class="page narrow account-page">
      <div class="page-title"><div><small>TÀI KHOẢN</small><h1>Không gian của bạn</h1></div></div>

      <market-state-panel
        *ngIf="error() as e"
        [kind]="e.kind"
        [message]="e.message"
        [traceId]="e.traceId"
      />

      <section class="panel account-overview">
        <div class="account-avatar">{{ initials() }}</div>
        <div>
          <small>ĐÃ ĐĂNG NHẬP</small>
          <h2>Tài khoản NOVA MARKET</h2>
          <p class="hint">Quyền hiển thị dưới đây chỉ điều khiển UX. Backend vẫn xác thực mọi request.</p>
        </div>
      </section>

      <section class="account-links">
        <a class="panel" routerLink="/orders"><strong>Đơn hàng</strong><span>Xem lịch sử và tracking →</span></a>
        <a class="panel" routerLink="/notifications"><strong>Thông báo</strong><span>Xem cập nhật mới nhất →</span></a>
        <a class="panel" routerLink="/returns"><strong>Trả hàng</strong><span>Quản lý yêu cầu trả hàng →</span></a>
      </section>

      <section class="panel authorization-summary" *ngIf="auth.authorization() as authorization">
        <h2>Quyền truy cập</h2>
        <div class="tag-list" *ngIf="authorization.roles.length">
          <span class="tag" *ngFor="let role of authorization.roles">{{ role }}</span>
        </div>
        <details *ngIf="authorization.permissions.length">
          <summary>{{ authorization.permissions.length }} quyền hiệu lực</summary>
          <div class="tag-list subdued">
            <span class="tag" *ngFor="let permission of authorization.permissions">{{ permission }}</span>
          </div>
        </details>
        <p class="hint" *ngIf="authorization.sellerIds.length">
          Phạm vi seller: {{ authorization.sellerIds.join(', ') }}
        </p>
      </section>

      <section class="panel" *ngIf="preference() as preference">
        <small>THÔNG BÁO</small>
        <h2>Tùy chọn nhận cập nhật</h2>
        <div class="preference-grid">
          <label><input type="checkbox" [(ngModel)]="preference.realtime" /> Realtime</label>
          <label><input type="checkbox" [(ngModel)]="preference.email" /> Email</label>
          <label><input type="checkbox" [(ngModel)]="preference.push" /> Push</label>
          <label><input type="checkbox" [(ngModel)]="preference.sellerUpdates" /> Cập nhật từ cửa hàng</label>
        </div>
        <button (click)="save(preference)">Lưu tùy chọn</button>
        <span class="success inline-success" *ngIf="saved()">Đã lưu.</span>
      </section>
    </section>
  `,
})
export class AccountComponent implements OnInit {
  readonly preference = signal<NotificationPreference | undefined>(undefined);
  readonly error = signal<UiErrorView | undefined>(undefined);
  readonly saved = signal(false);

  constructor(
    readonly auth: AuthService,
    private readonly marketplace: MarketplaceApiService,
  ) {}

  async ngOnInit(): Promise<void> {
    try {
      this.preference.set(await this.marketplace.notificationPreferences());
      this.error.set(undefined);
    } catch (error) {
      this.error.set(uiError(error));
    }
  }

  initials(): string {
    const roles = this.auth.authorization().roles;
    return (roles[0] ?? 'NM').slice(0, 2).toUpperCase();
  }

  async save(preference: NotificationPreference): Promise<void> {
    try {
      this.preference.set(await this.marketplace.saveNotificationPreferences(preference));
      this.saved.set(true);
      this.error.set(undefined);
    } catch (error) {
      this.saved.set(false);
      this.error.set(uiError(error));
    }
  }
}
