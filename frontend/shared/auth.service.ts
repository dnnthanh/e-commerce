import { Injectable, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { readEnvelope } from './api-contract';
import { runtimeConfig } from './runtime-config';
/** Keycloak PKCE authentication facade shared by storefront/admin apps. */
@Injectable({ providedIn: 'root' })
export class AuthService {
    /** Keycloak JS adapter. */ private readonly keycloak = new Keycloak({ url: runtimeConfig.keycloakUrl, realm: 'marketplace', clientId: runtimeConfig.keycloakClientId });
    /** Single initialization promise preventing duplicate adapter init. */ private initialization?: Promise<void>;
    /** Whether Keycloak has authenticated the browser session. */ readonly authenticated = signal(false);
    /** Effective role/permission/resource-scope snapshot loaded from backend. */ readonly authorization = signal<{
        roles: string[];
        permissions: string[];
        sellerIds: number[];
    }>({ roles: [], permissions: [], sellerIds: [] });
    /** Initializes Keycloak exactly once. */
    ensureInitialized(): Promise<void> {
        if (!this.initialization)
            this.initialization = this.initializeInternal();
        return this.initialization;
    }
    private async initializeInternal(): Promise<void> {
        const loggedIn = await this.keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256', silentCheckSsoRedirectUri: `${location.origin}/assets/silent-check-sso.html` });
        this.authenticated.set(loggedIn);
        if (loggedIn)
            await this.refreshAuthorization();
    }
    /** Starts browser login. */ async login(): Promise<void> { await this.ensureInitialized(); await this.keycloak.login({ redirectUri: location.href }); }
    /** Logs out and returns to app root. */ async logout(): Promise<void> { await this.ensureInitialized(); await this.keycloak.logout({ redirectUri: location.origin }); }
    /** Returns a fresh-enough bearer token. */ async token(): Promise<string> { await this.ensureInitialized(); await this.keycloak.updateToken(30); return this.keycloak.token ?? ''; }
    /** Checks one effective permission for UI rendering only. */ has(permission: string): boolean { return this.authorization().permissions.includes(permission); }
    /** Refreshes effective authorization from the backend rather than expanding the JWT. */
    async refreshAuthorization(): Promise<void> {
        const response = await fetch(`${runtimeConfig.apiBaseUrl}/private/me/authorization`, { headers: { Authorization: `Bearer ${this.keycloak.token ?? ''}` } });
        if (response.ok) {
            const envelope = await readEnvelope<{
                roles: string[];
                permissions: string[];
                sellerIds: number[];
            }>(response);
            if (envelope.data)
                this.authorization.set(envelope.data);
        }
    }
}
