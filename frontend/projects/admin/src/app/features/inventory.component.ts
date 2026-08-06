import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { BalanceView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-inventory', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>INVENTORY / RESERVATION</small><h1>Balances</h1></div><button (click)="load(0)">Refresh</button></div><form class="filters" (ngSubmit)="load(0)"><input type="number" [(ngModel)]="skuId" name="sku" placeholder="SKU optional"><input type="number" [(ngModel)]="warehouseId" name="warehouse" placeholder="Warehouse optional"><button>Filter</button></form><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><table><thead><tr><th>SKU</th><th>Warehouse</th><th>On hand</th><th>Reserved</th><th>Available</th><th>Version</th></tr></thead><tbody><tr *ngFor="let b of balances()"><td>{{b.skuId}}</td><td>{{b.warehouseId}}</td><td>{{b.onHand}}</td><td>{{b.reserved}}</td><td><b>{{b.available}}</b></td><td>{{b.version}}</td></tr></tbody></table><div class="row"><button class="ghost" [disabled]="page()===0" (click)="load(page()-1)">Prev</button><span>page {{page()+1}} / {{totalPages()}}</span><button class="ghost" [disabled]="!hasNext()" (click)="load(page()+1)">Next</button></div></div><p class="hint">Reservations are checkout/internal flows. Admin observes balances and version contention rather than calling internal reservation APIs from the browser.</p></section>` })
export class InventoryComponent implements OnInit {
    skuId?: number;
    warehouseId?: number;
    readonly balances = signal<BalanceView[]>([]);
    readonly error = signal('');
    readonly page = signal(0);
    readonly totalPages = signal(1);
    readonly hasNext = signal(false);
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(0); }
    async load(page: number) { try {
        const r = await this.marketplace.balances(page, 50, this.skuId, this.warehouseId);
        this.balances.set(r.data);
        this.page.set(r.metadata.page);
        this.totalPages.set(r.metadata.totalPages);
        this.hasNext.set(r.metadata.hasNext);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
