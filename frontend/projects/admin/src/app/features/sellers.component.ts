import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ShopView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-sellers', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>SELLER CONTEXT</small><h1>Shop management</h1></div></div><div class="two-col"><div class="panel"><form class="filters" (ngSubmit)="load()"><input type="number" [(ngModel)]="sellerId" name="seller" placeholder="Seller ID"><button>Load shops</button></form><div class="mini-row" *ngFor="let s of shops()"><div><b>{{s.name}}</b><small>#{{s.id}} · {{s.slug}} · {{s.status}}</small><p>{{s.description}}</p></div><button (click)="edit(s)">Edit</button></div></div><div class="panel" *ngIf="editing() as shop"><h2>Edit public material data</h2><label>Name<input [(ngModel)]="editName"></label><label>Description<textarea [(ngModel)]="editDescription"></textarea></label><button (click)="save(shop)">Save</button><p class="hint">Material changes are handled by seller service and may fan out through outbox notifications/search indexing.</p></div></div><div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class SellersComponent implements OnInit {
    sellerId = 10001;
    readonly shops = signal<ShopView[]>([]);
    readonly editing = signal<ShopView | undefined>(undefined);
    readonly error = signal('');
    editName = '';
    editDescription = '';
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(); }
    async load() { try {
        this.shops.set(await this.marketplace.sellerShops(this.sellerId));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    edit(shop: ShopView) { this.editing.set(shop); this.editName = shop.name; this.editDescription = shop.description ?? ''; }
    async save(shop: ShopView) { try {
        await this.marketplace.updateShop(shop.id, shop.sellerId, this.editName, this.editDescription);
        this.editing.set(undefined);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
