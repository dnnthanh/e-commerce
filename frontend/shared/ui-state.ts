import { ApiClientError } from './api-contract';
/** Reusable async screen state used by storefront and admin pages. */
export interface UiState<T> {
    data?: T;
    loading: boolean;
    error?: string;
}
/** Extracts the localized backend message and trace id when available. */
export function errorMessage(error: unknown): string {
    if (error instanceof ApiClientError) {
        return error.traceId ? `${error.message} (trace: ${error.traceId})` : error.message;
    }
    if (typeof error === 'object' && error && 'message' in error) {
        return String((error as {
            message?: unknown;
        }).message ?? 'Có lỗi xảy ra');
    }
    return 'Không thể tải dữ liệu. Vui lòng thử lại.';
}
