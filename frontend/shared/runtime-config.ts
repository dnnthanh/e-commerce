/** Runtime values injected by the container rather than hard-coded into the Angular build. */
export interface RuntimeConfig {
    /** Public API gateway base URL. */ apiBaseUrl: string;
    /** Public Keycloak base URL. */ keycloakUrl: string;
    /** Keycloak public client used by the current Angular app. */ keycloakClientId: string;
}
declare global {
    interface Window {
        __MARKETPLACE_CONFIG__?: RuntimeConfig;
    }
}
export const runtimeConfig: RuntimeConfig = window.__MARKETPLACE_CONFIG__ ?? {
    apiBaseUrl: 'http://localhost:8080', keycloakUrl: 'http://localhost:8180', keycloakClientId: location.port === '4201' ? 'admin-console' : 'storefront'
};
