import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
/** Requires an authenticated Keycloak session before entering a private storefront route. */
export const authGuard: CanActivateFn = async () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    await auth.ensureInitialized();
    if (auth.authenticated())
        return true;
    await auth.login();
    return router.parseUrl('/');
};
/** Creates a route guard for one effective permission with a caller-selected denied destination. */
export function permissionGuard(permission: string, deniedPath = '/'): CanActivateFn {
    return async () => {
        const auth = inject(AuthService);
        const router = inject(Router);
        await auth.ensureInitialized();
        return auth.has(permission) ? true : router.parseUrl(deniedPath);
    };
}
