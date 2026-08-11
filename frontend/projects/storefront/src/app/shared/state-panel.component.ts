import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export type StatePanelKind =
  | 'loading'
  | 'empty'
  | 'unauthorized'
  | 'forbidden'
  | 'not-found'
  | 'error';

/** Shared async/empty/auth state presentation for customer-facing routes. */
@Component({
  standalone: true,
  selector: 'market-state-panel',
  imports: [CommonModule],
  template: `
    <div class="state-panel" [class.state-error]="kind !== 'loading' && kind !== 'empty'">
      <span class="state-icon">{{ icon() }}</span>
      <div>
        <strong>{{ title() }}</strong>
        <p>{{ message }}</p>
        <small *ngIf="traceId">Mã theo dõi: {{ traceId }}</small>
      </div>
    </div>
  `,
})
export class StatePanelComponent {
  @Input({ required: true }) kind!: StatePanelKind;
  @Input({ required: true }) message = '';
  @Input() traceId?: string;

  icon(): string {
    switch (this.kind) {
      case 'loading':
        return '…';
      case 'empty':
        return '○';
      case 'unauthorized':
        return '↪';
      case 'forbidden':
        return '⛔';
      case 'not-found':
        return '⌕';
      default:
        return '!';
    }
  }

  title(): string {
    switch (this.kind) {
      case 'loading':
        return 'Đang tải';
      case 'empty':
        return 'Chưa có dữ liệu';
      case 'unauthorized':
        return 'Cần đăng nhập';
      case 'forbidden':
        return 'Không có quyền truy cập';
      case 'not-found':
        return 'Không tìm thấy';
      default:
        return 'Có lỗi xảy ra';
    }
  }
}
