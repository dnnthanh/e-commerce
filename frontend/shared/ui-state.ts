import { ApiClientError } from './api-contract';

/** Reusable async screen state used by storefront and admin pages. */
export interface UiState<T> {
  data?: T;
  loading: boolean;
  error?: string;
}

export type UiErrorKind = 'unauthorized' | 'forbidden' | 'not-found' | 'error';

export interface UiErrorView {
  kind: UiErrorKind;
  message: string;
  traceId?: string;
}

/** Extracts the localized backend message and trace id when available. */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    return error.traceId ? `${error.message} (trace: ${error.traceId})` : error.message;
  }
  if (typeof error === 'object' && error && 'message' in error) {
    return String(
      (error as { message?: unknown }).message ?? 'Có lỗi xảy ra',
    );
  }
  return 'Không thể tải dữ liệu. Vui lòng thử lại.';
}

/** Classifies transport errors so pages can render auth/not-found states without losing trace context. */
export function uiError(error: unknown): UiErrorView {
  if (error instanceof ApiClientError) {
    const kind: UiErrorKind =
      error.status === 401
        ? 'unauthorized'
        : error.status === 403
          ? 'forbidden'
          : error.status === 404
            ? 'not-found'
            : 'error';
    return { kind, message: error.message, traceId: error.traceId };
  }
  return { kind: 'error', message: errorMessage(error) };
}
