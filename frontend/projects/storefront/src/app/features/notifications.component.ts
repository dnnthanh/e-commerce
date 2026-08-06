import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { NotificationService } from '../../../../../shared/notification.service';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'market-notifications', imports: [CommonModule], template: `<section class="page"><div class="page-title"><div><small>DURABLE INBOX + SSE</small><h1>Thông báo</h1></div><div class="actions"><button class="ghost" (click)="refresh()">Refresh</button><button (click)="markAll()">Đánh dấu tất cả đã đọc</button></div></div><div class="error" *ngIf="error()">{{error()}}</div><div class="notification" *ngFor="let n of notifications.items()"><span class="dot" [class.read]="n.readAt"></span><div><strong>{{n.title}}</strong><p>{{n.message}}</p><small>{{n.type}} · {{n.createdAt|date:'medium'}}</small></div><button class="ghost" *ngIf="!n.readAt" (click)="read(n.id)">Đã đọc</button></div><div class="empty" *ngIf="!notifications.items().length">Không có thông báo.</div></section>` })
export class NotificationsComponent implements OnInit, OnDestroy {
    private stopped = false;
    readonly error = signal('');
    constructor(readonly notifications: NotificationService, private readonly marketplace: MarketplaceApiService) { }
    ngOnInit() { void this.notifications.connectUntil(() => this.stopped); }
    ngOnDestroy() { this.stopped = true; }
    async refresh() { try {
        this.notifications.items.set(await this.marketplace.notifications());
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async read(id: string) { try {
        await this.marketplace.markRead(id);
        await this.refresh();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async markAll() { try {
        await this.marketplace.markAllRead();
        await this.refresh();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
