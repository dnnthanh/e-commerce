import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { SettlementView } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-settlements', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>ORACLE FINANCE LEDGER</small><h1>Settlement & payout</h1></div></div><form class="filters" (ngSubmit)="load(0)"><input type="number" [(ngModel)]="sellerId" name="seller"><button>Load</button></form><div class="error" *ngIf="error()">{{error()}}</div><div class="panel"><table><thead><tr><th>Settlement</th><th>Gross</th><th>Commission</th><th>Payable</th><th>Status</th><th></th></tr></thead><tbody><tr *ngFor="let s of settlements()"><td>{{s.settlementNo}}</td><td>{{s.gross|number}}</td><td>{{s.commission|number}}</td><td><b>{{s.payable|number}}</b></td><td>{{s.status}}</td><td><button *ngIf="s.status==='OPEN'" (click)="approve(s)">Approve</button></td></tr></tbody></table><div class="row"><button class="ghost" [disabled]="page()===0" (click)="load(page()-1)">Prev</button><span>page {{page()+1}}</span><button class="ghost" [disabled]="!hasNext()" (click)="load(page()+1)">Next</button></div></div></section>` })
export class SettlementsComponent implements OnInit {
    sellerId = 10001;
    readonly settlements = signal<SettlementView[]>([]);
    readonly error = signal('');
    readonly page = signal(0);
    readonly hasNext = signal(false);
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { await this.load(0); }
    async load(page: number) { try {
        const r = await this.marketplace.settlements(this.sellerId, page, 50);
        this.settlements.set(r.data);
        this.page.set(r.metadata.page);
        this.hasNext.set(r.metadata.hasNext);
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async approve(s: SettlementView) { try {
        await this.marketplace.approveSettlement(s.settlementNo, 'Approved from finance console');
        await this.load(this.page());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
