import { Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';
import { CursorMetadata, readEnvelope } from './api-contract';
import { runtimeConfig } from './runtime-config';
/** Realtime notification client with durable catch-up, reconnect backoff and duplicate suppression. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
    /** Current durable/realtime inbox. */ readonly items = signal<any[]>([]);
    /** Last durable cursor observed. */ private lastCursor?: string;
    constructor(private readonly auth: AuthService) { }
    /** Connects until the supplied lifecycle predicate asks the loop to stop. */
    async connectUntil(stopped: () => boolean): Promise<void> {
        await this.catchUp();
        let attempt = 0;
        while (!stopped()) {
            try {
                const response = await fetch(`${runtimeConfig.apiBaseUrl}/private/notifications/stream`, { headers: { Authorization: `Bearer ${await this.auth.token()}`, Accept: 'text/event-stream' } });
                if (!response.ok || !response.body)
                    throw new Error('realtime unavailable');
                attempt = 0;
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = '';
                while (!stopped()) {
                    const { done, value } = await reader.read();
                    if (done)
                        break;
                    buffer += decoder.decode(value, { stream: true });
                    const events = buffer.split('\n\n');
                    buffer = events.pop() ?? '';
                    for (const raw of events) {
                        const data = raw.split('\n').find(line => line.startsWith('data:'));
                        if (data) {
                            const item = JSON.parse(data.slice(5).trim());
                            this.merge([item]);
                        }
                    }
                }
                await reader.cancel();
            }
            catch {
                if (stopped())
                    break;
                attempt++;
                await new Promise(resolve => setTimeout(resolve, Math.min(30000, 1000 * 2 ** Math.min(attempt, 5)) + Math.random() * 700));
                await this.catchUp();
            }
        }
    }
    /** Loads notifications missed while the realtime connection was unavailable. */
    async catchUp(): Promise<void> {
        if (!this.auth.authenticated())
            return;
        const suffix = this.lastCursor ? `?after=${encodeURIComponent(this.lastCursor)}` : '';
        const response = await fetch(`${runtimeConfig.apiBaseUrl}/private/notifications${suffix}`, { headers: { Authorization: `Bearer ${await this.auth.token()}` } });
        if (response.ok) {
            const envelope = await readEnvelope<any[], CursorMetadata>(response);
            this.merge(envelope.data ?? []);
            this.lastCursor = envelope.metadata?.nextCursor ?? this.items()[0]?.createdAt ?? this.lastCursor;
        }
    }
    private merge(incoming: any[]): void { this.items.update(current => { const byId = new Map(current.map(item => [item.id, item])); for (const item of incoming)
        byId.set(item.id, item); return [...byId.values()].sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt))); }); }
}
