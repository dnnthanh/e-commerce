import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../../shared/auth.service';
import { MarketplaceApiService } from '../../../../../shared/marketplace-api.service';
import { MediaVariant, UploadSession } from '../../../../../shared/marketplace-types';
import { UiErrorView, uiError } from '../../../../../shared/ui-state';
import { AdminPageHeaderComponent } from '../shared/admin-page-header.component';
import { AdminStateComponent } from '../shared/admin-state.component';

@Component({
  standalone: true,
  selector: 'admin-media',
  imports: [CommonModule, FormsModule, AdminPageHeaderComponent, AdminStateComponent],
  template: `
    <section>
      <admin-page-header
        eyebrow="MEDIA PIPELINE"
        title="Upload & variants"
        description="Create a presigned upload session, complete the existing media command, then inspect generated variants."
      />

      <div class="two-col">
        <div class="panel">
          <h2>Create presigned upload</h2>
          <label>Product ID<input type="number" min="1" [(ngModel)]="productId"></label>
          <label *ngIf="sellerScopes.length; else manualSeller">
            Seller scope
            <select [(ngModel)]="sellerId">
              <option *ngFor="let id of sellerScopes" [ngValue]="id">Seller {{ id }}</option>
            </select>
          </label>
          <ng-template #manualSeller><label>Seller ID<input type="number" min="1" [(ngModel)]="sellerId"></label></ng-template>
          <label>Filename<input [(ngModel)]="filename"></label>
          <label>Content type<input [(ngModel)]="contentType"></label>
          <label>Checksum<input [(ngModel)]="checksum"></label>
          <button type="button" [disabled]="creating() || !canCreate" (click)="createUpload()">Create session</button>
          <pre class="json" *ngIf="session() as current">{{ current | json }}</pre>
          <button
            *ngIf="session() as current"
            class="ghost"
            type="button"
            [disabled]="completing()"
            (click)="complete(current)"
          >Mark upload complete</button>
        </div>

        <div class="panel">
          <div class="panel-title">
            <h2>Generated product variants</h2>
            <button class="ghost" type="button" [disabled]="loadingVariants() || productId < 1" (click)="loadVariants()">Reload</button>
          </div>
          <admin-state *ngIf="loadingVariants()" kind="loading" title="Loading variants" message="Reading the current media projection." />
          <admin-state *ngIf="!loadingVariants() && variants().length === 0" kind="empty" title="No ready variants" message="The media worker has not exposed generated variants for this product yet." />
          <div class="mini-row" *ngFor="let variant of variants()">
            <div><b>{{ variant.placement }}</b><small>{{ variant.width }}×{{ variant.height }} {{ variant.format }}</small></div>
            <code>{{ variant.objectKey }}</code>
          </div>
          <p class="hint">Completion acknowledges object upload only. Readiness remains decided by the backend media pipeline/worker.</p>
        </div>
      </div>

      <admin-state
        *ngIf="failure() as problem"
        kind="error"
        title="Media request failed"
        [message]="problem.message"
        [traceId]="problem.traceId"
      />
    </section>
  `,
})
export class MediaComponent implements OnInit {
  private readonly marketplace = inject(MarketplaceApiService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  productId = 1;
  sellerId?: number;
  filename = 'product.webp';
  contentType = 'image/webp';
  checksum = '';
  readonly session = signal<UploadSession | undefined>(undefined);
  readonly variants = signal<MediaVariant[]>([]);
  readonly failure = signal<UiErrorView | undefined>(undefined);
  readonly creating = signal(false);
  readonly completing = signal(false);
  readonly loadingVariants = signal(false);

  get sellerScopes(): number[] {
    return this.auth.authorization().sellerIds;
  }

  get canCreate(): boolean {
    return this.productId > 0 && !!this.sellerId && !!this.filename.trim() && !!this.contentType.trim() && !!this.checksum.trim();
  }

  async ngOnInit(): Promise<void> {
    const product = Number(this.route.snapshot.queryParamMap.get('productId'));
    const seller = Number(this.route.snapshot.queryParamMap.get('sellerId'));
    if (Number.isFinite(product) && product > 0) this.productId = product;
    if (Number.isFinite(seller) && seller > 0) this.sellerId = seller;
    if (!this.sellerId && this.sellerScopes.length) this.sellerId = this.sellerScopes[0];
    await this.loadVariants();
  }

  async createUpload(): Promise<void> {
    if (!this.canCreate) return;
    this.creating.set(true);
    this.failure.set(undefined);
    try {
      this.session.set(await this.marketplace.createUpload({
        productId: this.productId,
        sellerId: this.sellerId!,
        filename: this.filename.trim(),
        contentType: this.contentType.trim(),
        checksum: this.checksum.trim(),
      }));
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.creating.set(false);
    }
  }

  async complete(current: UploadSession): Promise<void> {
    this.completing.set(true);
    this.failure.set(undefined);
    try {
      await this.marketplace.completeUpload(current.assetId);
      await this.loadVariants();
    } catch (error) {
      this.failure.set(uiError(error));
    } finally {
      this.completing.set(false);
    }
  }

  async loadVariants(): Promise<void> {
    if (this.productId < 1) return;
    this.loadingVariants.set(true);
    this.failure.set(undefined);
    try {
      this.variants.set(await this.marketplace.productMedia(this.productId));
    } catch (error) {
      this.variants.set([]);
      this.failure.set(uiError(error));
    } finally {
      this.loadingVariants.set(false);
    }
  }
}
