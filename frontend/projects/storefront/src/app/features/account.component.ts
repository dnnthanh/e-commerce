import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { NotificationPreference } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-account', imports: [CommonModule, FormsModule], template: `<section class="page narrow"><div class="page-title"><div><small>ACCOUNT / PREFERENCES</small><h1>Tài khoản</h1></div></div><div class="panel"><h2>Authorization snapshot</h2><pre class="result">{{auth.authorization()|json}}</pre></div><div class="panel" *ngIf="preference() as p"><h2>Notification preferences</h2><label><input type="checkbox" [(ngModel)]="p.realtime"> Realtime</label><label><input type="checkbox" [(ngModel)]="p.email"> Email</label><label><input type="checkbox" [(ngModel)]="p.push"> Push</label><label><input type="checkbox" [(ngModel)]="p.sellerUpdates"> Seller updates</label><button (click)="save(p)">Lưu preference</button></div><div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class AccountComponent implements OnInit {
    readonly preference = signal<NotificationPreference | undefined>(undefined);
    readonly error = signal('');
    constructor(readonly auth: AuthService, private readonly marketplace: MarketplaceApiService) { }
    async ngOnInit() { try {
        this.preference.set(await this.marketplace.notificationPreferences());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async save(p: NotificationPreference) { try {
        this.preference.set(await this.marketplace.saveNotificationPreferences(p));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
