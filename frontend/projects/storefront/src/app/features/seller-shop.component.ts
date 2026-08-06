import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ShopView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-seller-shop', imports: [CommonModule, RouterLink], template: `<section class="page narrow"><div class="panel" *ngIf="shop() as s"><small>SELLER SHOP</small><h1>{{s.name}}</h1><span class="status-chip">{{s.status}}</span><p>{{s.description}}</p><p>Seller #{{s.sellerId}} · slug {{s.slug}}</p><a routerLink="/search">Tìm sản phẩm của marketplace →</a></div><div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class SellerShopComponent implements OnInit {
    readonly shop = signal<ShopView | undefined>(undefined);
    readonly error = signal('');
    constructor(private readonly route: ActivatedRoute, private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { try {
        this.shop.set(await this.marketplace.shop(Number(this.route.snapshot.paramMap.get('shopId'))));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
