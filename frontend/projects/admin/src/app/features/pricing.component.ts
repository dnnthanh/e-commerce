import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { PriceView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-pricing', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>PRICING RESOLUTION</small><h1>Effective price diagnostics</h1></div></div><div class="panel"><div class="filters"><input type="number" [(ngModel)]="skuId" placeholder="SKU"><input type="number" [(ngModel)]="sellerId" placeholder="Seller"><select [(ngModel)]="channel"><option>WEB</option><option>MOBILE</option><option>POS</option></select><button (click)="resolve()">Resolve price</button></div><div class="error" *ngIf="error()">{{error()}}</div><div *ngIf="price() as p" class="cards"><article><span>Amount</span><strong>{{p.amount|number}}</strong><small>{{p.currency}}</small></article><article><span>Rule</span><strong>#{{p.ruleId||'base'}}</strong><small>{{p.resolvedAt|date:'medium'}}</small></article><article><span>Seller</span><strong>{{p.sellerId}}</strong><small>SKU {{p.skuId}}</small></article></div><p class="hint">This screen intentionally calls the same public effective-price contract as storefront. Rule administration is not exposed by the current backend HTTP contract.</p></div></section>` })
export class PricingComponent {
    skuId = 1000001;
    sellerId = 10001;
    channel = 'WEB';
    readonly price = signal<PriceView | undefined>(undefined);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async resolve() { try {
        this.price.set(await this.marketplace.price(this.skuId, this.sellerId, this.channel));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
