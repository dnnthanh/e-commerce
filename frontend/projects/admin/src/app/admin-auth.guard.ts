import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../../../shared/auth.service';

/** Keeps Admin authentication UX separate from storefront's login-first guard. */
export const adminAuthGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.ensureInitialized();
  return auth.authenticated() ? true : router.parseUrl('/unauthorized');
};
