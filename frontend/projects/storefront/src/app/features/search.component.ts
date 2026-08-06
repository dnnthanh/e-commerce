import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CursorMetadata } from '../../../../../shared/api-contract';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { SearchResponse } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-search', imports: [CommonModule, FormsModule, RouterLink], template: `<section class="page"><div class="page-title"><div><small>OPENSEARCH / SEARCH_AFTER</small><h1>Tìm sản phẩm</h1></div></div><form class="search-bar" (ngSubmit)="runNewSearch()"><input [(ngModel)]="query" name="query" placeholder="Điện thoại, laptop, phụ kiện…"><select [(ngModel)]="size" name="size"><option [ngValue]="12">12</option><option [ngValue]="24">24</option><option [ngValue]="48">48</option></select><button>Tìm kiếm</button></form><div class="loading" *ngIf="loading()">Đang tìm…</div><div class="error" *ngIf="error()">{{error()}}</div><div class="search-layout" *ngIf="result() as r"><aside><h3>Facets</h3><p>Tổng <b>{{metadata()?.total|number}}</b></p><div *ngFor="let f of entries(r.facets||{})"><strong>{{f[0]}}</strong><p *ngFor="let v of entries(f[1])">{{v[0]}} <span>{{v[1]}}</span></p></div></aside><section class="product-grid"><a class="product-card" *ngFor="let p of r.items" [routerLink]="['/product',p.productId]"><div class="thumb"><span>{{p.name.slice(0,2)}}</span></div><small>Seller {{p.sellerId??'—'}} · ★ {{p.rating??'—'}}</small><h3>{{p.name}}</h3><strong>{{p.price??0|number}} ₫</strong></a></section></div><div class="pager" *ngIf="result()"><button [disabled]="cursorHistory.length===0" (click)="previous()">← Trước</button><span>cursor page · {{metadata()?.total|number}} kết quả</span><button [disabled]="!metadata()?.hasNext" (click)="next()">Sau →</button></div></section>` })
export class SearchComponent {
    query = '';
    size = 24;
    readonly result = signal<SearchResponse | undefined>(undefined);
    readonly metadata = signal<CursorMetadata | undefined>(undefined);
    readonly loading = signal(false);
    readonly error = signal('');
    cursorHistory: (string | undefined)[] = [];
    private currentCursor?: string;
    constructor(private readonly marketplace: MarketplaceApiService) { void this.runNewSearch(); }
    async runNewSearch() { this.cursorHistory = []; this.currentCursor = undefined; await this.search(); }
    async next() { const next = this.metadata()?.nextCursor; if (!next)
        return; this.cursorHistory.push(this.currentCursor); this.currentCursor = next; await this.search(); }
    async previous() { if (!this.cursorHistory.length)
        return; this.currentCursor = this.cursorHistory.pop(); await this.search(); }
    private async search() { this.loading.set(true); try {
        const page = await this.marketplace.search(this.query, this.currentCursor, this.size);
        this.result.set(page.data);
        this.metadata.set(page.metadata);
        this.error.set('');
    }
    catch (e) {
        this.error.set(errorMessage(e));
    }
    finally {
        this.loading.set(false);
    } }
    entries(v: Record<string, any>): [
        string,
        any
    ][] { return Object.entries(v ?? {}); }
}
