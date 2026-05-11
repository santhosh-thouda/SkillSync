import { Injectable, inject, signal, NgZone } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';
import { AuthStore } from '../store/auth.store';
import { environment } from '../../../environments/environment';

export interface ChatMessage {
  id?: number;
  groupId: number;
  senderId: number;
  senderName: string;
  content: string;
  sentAt?: string | number[];
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private api      = inject(ApiService);
  private authStore = inject(AuthStore);
  private zone     = inject(NgZone);

  private client: Client | null = null;
  private subscription: StompSubscription | null = null;
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);

  /** Reactive signal — true once STOMP CONNECTED frame is received */
  readonly connected = signal(false);

  messages$: Observable<ChatMessage[]> = this.messagesSubject.asObservable();

  // ── Connection ────────────────────────────────────────────────────────────

  connect(groupId: number): void {
    this.disconnect(false);

    const token  = this.authStore.token();
    
    // Use Native WebSocket for maximum stability and no external library overhead
    // The Gateway handles /ws/ websocket upgrades automatically.
    const url = `${environment.apiUrl.replace(/^http/, 'ws').replace(/\/$/, '')}/ws`;

    this.client = new Client({
      brokerURL: url,
      connectHeaders: token ? { 'Authorization': `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        this.zone.run(() => this.connected.set(true));

        // Subscribe to group-specific topic
        this.subscription = this.client!.subscribe(
          `/topic/group/${groupId}`,
          (msg: IMessage) => {
            const incoming: ChatMessage = JSON.parse(msg.body);
            console.debug('[Chat] Incoming broadcast:', incoming);
            
            this.zone.run(() => {
              const current = this.messagesSubject.getValue();
              
              // Prevent duplicates (optimistic update vs broadcast)
              const isDuplicate = current.some(m => 
                m.content === incoming.content && 
                m.senderId === incoming.senderId &&
                !m.id
              );

              if (isDuplicate) {
                const updated = current.map(m => 
                  (m.content === incoming.content && m.senderId === incoming.senderId && !m.id) 
                  ? incoming : m
                );
                this.messagesSubject.next(updated);
              } else {
                this.messagesSubject.next([...current, incoming]);
              }
            });
          }
        );
      },

      onDisconnect: () => {
        this.zone.run(() => this.connected.set(false));
      },

      onStompError: (frame) => {
        console.error('[STOMP] error', frame);
        this.zone.run(() => this.connected.set(false));
      },

      debug: (str) => {
        if (!environment.production) console.debug('[STOMP]', str);
      }
    });

    this.client.activate();
  }

  disconnect(clearMessages = true): void {
    this.subscription?.unsubscribe();
    this.subscription = null;
    this.client?.deactivate();
    this.client = null;
    this.connected.set(false);
    if (clearMessages) {
      this.messagesSubject.next([]);
    }
  }

  // ── Sending ───────────────────────────────────────────────────────────────

  sendMessage(groupId: number, content: string): void {
    if (!this.client?.connected) {
      console.warn('[Chat] Cannot send — WebSocket not connected');
      return;
    }
    const user = this.authStore.user();
    const payload: ChatMessage = {
      groupId,
      senderId:   user?.id   ?? 0,
      senderName: user?.name ?? 'Unknown',
      content,
      sentAt: new Date().toISOString()
    };

    // Optimistic Update
    const current = this.messagesSubject.getValue();
    this.messagesSubject.next([...current, payload]);

    this.client.publish({
      destination: '/app/sendMessage',
      body: JSON.stringify(payload)
    });
  }

  // ── History ───────────────────────────────────────────────────────────────

  async loadHistory(groupId: number): Promise<ChatMessage[]> {
    try {
      const res = await firstValueFrom(
        this.api.get<ChatMessage[]>(`/groups/${groupId}/messages?limit=100`)
      );
      return res ?? [];
    } catch (err) {
      console.error('[Chat] Failed to load history', err);
      return [];
    }
  }

  setMessages(messages: ChatMessage[]): void {
    this.messagesSubject.next(messages);
  }

  static parseSentAt(sentAt: string | number[] | undefined): Date | null {
    if (!sentAt) return null;
    if (Array.isArray(sentAt)) {
      const [y, mo, d, h = 0, mi = 0, s = 0] = sentAt as number[];
      return new Date(y, mo - 1, d, h, mi, s);
    }
    const dt = new Date(sentAt as string);
    return isNaN(dt.getTime()) ? null : dt;
  }
}
