import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { ProductView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-catalog', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>CATALOG LIFECYCLE</small><h1>Products</h1></div><button (click)="load()">Refresh</button></div><div class="two-col"><div class="panel"><h2>Create product</h2><label>Seller ID<input type="number" [(ngModel)]="sellerId"></label><label>Category ID<input type="number" [(ngModel)]="categoryId"></label><label>Name<input [(ngModel)]="name"></label><label>Description<textarea [(ngModel)]="description"></textarea></label><button (click)="create()">Create draft</button></div><div class="panel"><h2>Catalog query</h2><form class="filters" (ngSubmit)="load()"><input [(ngModel)]="query" name="query" placeholder="Search name"><button>Search</button></form><div class="error" *ngIf="error()">{{error()}}</div><div class="mini-row" *ngFor="let p of products()"><div><b>{{p.name}}</b><small>#{{p.id}} seller {{p.sellerId}} · category {{p.categoryId}}</small><p>{{p.status}} · {{p.updatedAt|date:'short'}}</p></div><button *ngIf="p.status!=='PUBLISHED'" (click)="publish(p)">Publish</button></div></div></div></section>` })
export class CatalogComponent implements OnInit {
    readonly products = signal<ProductView[]>([]);
    readonly error = signal('');
    sellerId = 10001;
    categoryId = 1;
    name = '';
    description = '';
    query = '';
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(); }
    async load() { try {
        this.products.set((await this.marketplace.products(0, 50, this.query)).data);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async create() { try {
        await this.marketplace.createProduct({ sellerId: this.sellerId, categoryId: this.categoryId, name: this.name, description: this.description });
        this.name = '';
        this.description = '';
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async publish(p: ProductView) { try {
        await this.marketplace.publishProduct(p.id);
        await this.load();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
