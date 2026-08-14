import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../shared/auth.service';
import { AppComponent } from './app/app.component';
import { ADMIN_ROUTES } from './app/admin.routes';

bootstrapApplication(AppComponent, {
  providers: [provideRouter(ADMIN_ROUTES)],
}).then(ref => ref.injector.get(AuthService).ensureInitialized());
