import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { PromotionResult } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-promotion', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>PROMOTION ENGINE</small><h1>Evaluation workbench</h1></div></div><div class="two-col"><div class="panel"><h2>Cart context</h2><label>Subtotal<input type="number" [(ngModel)]="subtotal"></label><label>Codes<input [(ngModel)]="codes" placeholder="WELCOME10,FREESHIP"></label><label>Channel<select [(ngModel)]="channel"><option>WEB</option><option>MOBILE</option></select></label><label>Customer segment<input [(ngModel)]="segment"></label><button (click)="evaluate()">Evaluate</button><p class="hint">Reservation/confirm/release APIs are internal checkout contracts and are intentionally not called from browser code.</p></div><div class="panel" *ngIf="result() as r"><h2>Decision</h2><div class="cards"><article><span>Subtotal</span><strong>{{r.subtotal|number}}</strong></article><article><span>Discount</span><strong>{{r.totalDiscount|number}}</strong></article><article><span>Payable</span><strong>{{r.payable|number}}</strong></article></div><pre class="json">{{r.applied|json}}</pre></div></div><div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class PromotionComponent {
    subtotal = 1000000;
    codes = '';
    channel = 'WEB';
    segment = 'DEFAULT';
    readonly result = signal<PromotionResult | undefined>(undefined);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async evaluate() { try {
        this.result.set(await this.marketplace.evaluatePromotion(this.subtotal, this.codes.split(',').map(x => x.trim()).filter(Boolean), [], this.channel, this.segment));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
