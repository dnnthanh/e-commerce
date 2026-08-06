import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
/** Requires an authenticated Keycloak session before entering a private route. */
export const authGuard: CanActivateFn = async () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    await auth.ensureInitialized();
    if (auth.authenticated())
        return true;
    await auth.login();
    return router.parseUrl('/');
};
/** Creates a route guard for one effective permission. */
export function permissionGuard(permission: string): CanActivateFn {
    return async () => { const auth = inject(AuthService); const router = inject(Router); await auth.ensureInitialized(); return auth.has(permission) ? true : router.parseUrl('/'); };
}
