import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { MediaVariant, UploadSession } from '../../../../../shared/marketplace-types';
import { errorMessage } from '../../../../../shared/ui-state';
@Component({ standalone: true, selector: 'admin-media', imports: [CommonModule, FormsModule], template: `<section><div class="page-title"><div><small>MEDIA PIPELINE</small><h1>Upload & variants</h1></div></div><div class="two-col"><div class="panel"><h2>Create presigned upload</h2><label>Product<input type="number" [(ngModel)]="productId"></label><label>Seller<input type="number" [(ngModel)]="sellerId"></label><label>Filename<input [(ngModel)]="filename"></label><label>Content type<input [(ngModel)]="contentType"></label><label>Checksum<input [(ngModel)]="checksum"></label><button (click)="createUpload()">Create session</button><pre class="json" *ngIf="session()">{{session()|json}}</pre><button *ngIf="session() as s" class="ghost" (click)="complete(s)">Mark upload complete</button></div><div class="panel"><h2>Generated product variants</h2><button class="ghost" (click)="loadVariants()">Reload variants</button><div class="mini-row" *ngFor="let v of variants()"><div><b>{{v.placement}}</b><small>{{v.width}}×{{v.height}} {{v.format}}</small></div><code>{{v.objectKey}}</code></div><p class="hint">Completion only acknowledges object upload. Readiness is decided by the backend media pipeline/worker.</p></div></div><div class="error" *ngIf="error()">{{error()}}</div></section>` })
export class MediaComponent {
    productId = 1;
    sellerId = 10001;
    filename = 'product.webp';
    contentType = 'image/webp';
    checksum = 'sha256-demo';
    readonly session = signal<UploadSession | undefined>(undefined);
    readonly variants = signal<MediaVariant[]>([]);
    readonly error = signal('');
    constructor(private readonly marketplace: MarketplaceApiService) { }
    async createUpload() { try {
        this.session.set(await this.marketplace.createUpload({ productId: this.productId, sellerId: this.sellerId, filename: this.filename, contentType: this.contentType, checksum: this.checksum }));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async complete(s: UploadSession) { try {
        await this.marketplace.completeUpload(s.assetId);
        await this.loadVariants();
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
    async loadVariants() { try {
        this.variants.set(await this.marketplace.productMedia(this.productId));
    }
    catch (e) {
        this.error.set(errorMessage(e));
    } }
}
