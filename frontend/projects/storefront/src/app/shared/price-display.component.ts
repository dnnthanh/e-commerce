import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/** Consistent storefront monetary presentation. Monetary truth stays on backend responses. */
@Component({
  standalone: true,
  selector: 'market-price-display',
  imports: [CommonModule],
  template: `<strong class="price-display">{{ amount | number: '1.0-0' }} {{ currency }}</strong>`,
})
export class PriceDisplayComponent {
  @Input({ required: true }) amount!: number;
  @Input() currency = '₫';
}
