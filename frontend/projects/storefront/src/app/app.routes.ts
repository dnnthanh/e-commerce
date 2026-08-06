import { Routes } from '@angular/router';
import { authGuard } from '../../../../shared/auth.guard';
import { AccountComponent } from './features/account.component';
import { CartComponent } from './features/cart.component';
import { CategoryComponent } from './features/category.component';
import { CheckoutComponent } from './features/checkout.component';
import { CommunityComponent } from './features/community.component';
import { HomeComponent } from './features/home.component';
import { NotificationsComponent } from './features/notifications.component';
import { OrderDetailComponent } from './features/order-detail.component';
import { OrdersComponent } from './features/orders.component';
import { ProductDetailComponent } from './features/product-detail.component';
import { ReturnsComponent } from './features/returns.component';
import { SearchComponent } from './features/search.component';
import { SellerShopComponent } from './features/seller-shop.component';

/** Customer storefront routes. Public discovery stays anonymous; purchase/account areas require auth. */
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'search', component: SearchComponent },
  { path: 'category/:id', component: CategoryComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'product/:id/community', component: CommunityComponent },
  { path: 'shops/:shopId', component: SellerShopComponent },
  { path: 'cart', component: CartComponent, canActivate: [authGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [authGuard] },
  { path: 'orders/:orderNo', component: OrderDetailComponent, canActivate: [authGuard] },
  { path: 'returns', component: ReturnsComponent, canActivate: [authGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
  { path: 'account', component: AccountComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
