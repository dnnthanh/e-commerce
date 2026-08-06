import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../../shared/auth.service';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, { providers: [provideRouter(routes)] }).then((ref) =>
  ref.injector.get(AuthService).ensureInitialized(),
);
