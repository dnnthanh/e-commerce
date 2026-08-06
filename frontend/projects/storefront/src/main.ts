import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AppComponent, routes } from './app/app.component';
import { AuthService } from '../../../shared/auth.service';
bootstrapApplication(AppComponent, { providers: [provideRouter(routes)] }).then(ref => ref.injector.get(AuthService).ensureInitialized());
