/** Stable backend API error contract mirrored from be-platform-starter. */
export interface ApiError {
    code: string;
    message: string;
    traceId?: string;
    timestamp?: string;
    path?: string;
    fieldErrors?: Array<{
        field: string;
        message: string;
    }>;
    details?: Record<string, unknown>;
}
/** Offset/page metadata returned by relational pageable APIs. */
export interface PageMetadata {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
}
/** Cursor metadata returned by deep-pagination APIs such as OpenSearch. */
export interface CursorMetadata {
    total: number;
    size: number;
    nextCursor?: string;
    hasNext: boolean;
}
/** Generic backend response envelope. Null properties are omitted by backend Jackson config. */
export interface ApiEnvelope<T, M = never> {
    data?: T;
    metadata?: M;
    error?: ApiError;
}
/** Frontend representation of one pageable response after envelope unwrapping. */
export interface PageResult<T> {
    data: T[];
    metadata: PageMetadata;
}
/** Frontend representation of a cursor response after envelope unwrapping. */
export interface CursorResult<T> {
    data: T;
    metadata: CursorMetadata;
}
/** Error thrown by the shared HTTP client while retaining machine code and trace correlation. */
export class ApiClientError extends Error {
    constructor(readonly code: string, message: string, readonly traceId?: string, readonly status?: number, readonly fieldErrors: ApiError['fieldErrors'] = [], readonly details?: Record<string, unknown>) {
        super(message);
        this.name = 'ApiClientError';
    }
}
/** Reads an API envelope without applying HTTP status policy; useful for direct fetch consumers. */
export async function readEnvelope<T, M = never>(response: Response): Promise<ApiEnvelope<T, M>> {
    if (response.status === 204)
        return {};
    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.includes('application/json'))
        return {};
    return await response.json() as ApiEnvelope<T, M>;
}
