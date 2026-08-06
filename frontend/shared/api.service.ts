import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { ApiClientError, ApiEnvelope, CursorMetadata, CursorResult, PageMetadata, PageResult, readEnvelope, } from './api-contract';
import { runtimeConfig } from './runtime-config';
/** HTTP helper that owns backend envelope/error/trace handling for Angular applications. */
@Injectable({ providedIn: 'root' })
export class ApiService {
    private lastTraceId?: string;
    constructor(private readonly auth: AuthService) { }
    /** Last backend trace id observed by this browser, useful for support/error screens. */
    traceId(): string | undefined {
        return this.lastTraceId;
    }
    /** Executes a GET and returns only the response data. */
    async get<T>(path: string, authenticated = false): Promise<T> {
        return await this.requestData<T>('GET', path, undefined, authenticated);
    }
    /** Executes a relational pageable GET while retaining backend pagination metadata. */
    async getPage<T>(path: string, authenticated = false): Promise<PageResult<T>> {
        const envelope = await this.requestEnvelope<T[], PageMetadata>('GET', path, undefined, authenticated);
        if (!envelope.metadata)
            throw new ApiClientError('MISSING_PAGE_METADATA', 'Backend response is missing page metadata.', this.lastTraceId);
        return { data: envelope.data ?? [], metadata: envelope.metadata };
    }
    /** Executes a cursor-based GET while retaining next-cursor/total metadata. */
    async getCursor<T>(path: string, authenticated = false): Promise<CursorResult<T>> {
        const envelope = await this.requestEnvelope<T, CursorMetadata>('GET', path, undefined, authenticated);
        if (!envelope.metadata)
            throw new ApiClientError('MISSING_CURSOR_METADATA', 'Backend response is missing cursor metadata.', this.lastTraceId);
        if (envelope.data === undefined)
            throw new ApiClientError('MISSING_RESPONSE_DATA', 'Backend response is missing data.', this.lastTraceId);
        return { data: envelope.data, metadata: envelope.metadata };
    }
    /** Executes an authenticated command and returns only the response data. */
    async send<T>(method: string, path: string, body?: unknown): Promise<T> {
        return await this.requestData<T>(method, path, body, true);
    }
    private async requestData<T>(method: string, path: string, body: unknown, authenticated: boolean): Promise<T> {
        const envelope = await this.requestEnvelope<T, never>(method, path, body, authenticated);
        return envelope.data as T;
    }
    private async requestEnvelope<T, M>(method: string, path: string, body: unknown, authenticated: boolean): Promise<ApiEnvelope<T, M>> {
        const headers: Record<string, string> = { Accept: 'application/json' };
        if (body !== undefined)
            headers['Content-Type'] = 'application/json';
        if (authenticated)
            headers.Authorization = `Bearer ${await this.auth.token()}`;
        const response = await fetch(runtimeConfig.apiBaseUrl + path, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body),
        });
        const envelope = await readEnvelope<T, M>(response);
        this.lastTraceId = response.headers.get('trace-id') ?? envelope.error?.traceId ?? this.lastTraceId;
        if (!response.ok || envelope.error) {
            const error = envelope.error;
            throw new ApiClientError(error?.code ?? `HTTP_${response.status}`, error?.message ?? response.statusText ?? 'Request failed', error?.traceId ?? this.lastTraceId, response.status, error?.fieldErrors, error?.details);
        }
        return envelope;
    }
}
