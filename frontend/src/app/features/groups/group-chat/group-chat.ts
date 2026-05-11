import {
  Component, OnInit, OnDestroy, Input, Output, EventEmitter, inject,
  ViewChild, ElementRef, AfterViewChecked, signal, computed, OnChanges, SimpleChanges
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription } from 'rxjs';
import { ChatService, ChatMessage } from '../../../core/services/chat.service';
import { AuthStore } from '../../../core/store/auth.store';
import { Group } from '../../../core/services/group.service';

@Component({
  selector: 'app-group-chat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatProgressSpinnerModule, DatePipe],
  templateUrl: './group-chat.html',
  styleUrl: './group-chat.scss'
})
export class GroupChat implements OnInit, OnDestroy, AfterViewChecked, OnChanges {
  @Input() groupId!: number;
  @Input() groupName = 'Group Chat';
  @Input() group: Group | null = null;
  @Output() onClose = new EventEmitter<void>();

  @ViewChild('messageList') messageList!: ElementRef;
  @ViewChild('inputRef') inputRef!: ElementRef<HTMLTextAreaElement>;

  readonly store = inject(AuthStore);
  readonly chatService = inject(ChatService);

  messages = signal<ChatMessage[]>([]);
  isLoading = signal(true);
  showMembers = signal(false);
  private sub!: Subscription;
  private shouldScroll = false;

  messageControl = new FormControl('', [Validators.required, Validators.maxLength(1000)]);

  /** Build a member-name map from messages (senderName keyed by senderId) */
  readonly memberNames = computed(() => {
    const map = new Map<number, string>();
    for (const m of this.messages()) {
      if (m.senderId && m.senderName) map.set(m.senderId, m.senderName);
    }
    return map;
  });

  /** Unique members seen in chat (id + name) */
  readonly chatMembers = computed(() => {
    const seen = new Map<number, string>();
    for (const m of this.messages()) {
      if (m.senderId && m.senderName && !seen.has(m.senderId)) {
        seen.set(m.senderId, m.senderName);
      }
    }
    // Also add current user if not in messages yet
    const me = this.store.user();
    if (me && !seen.has(me.id)) seen.set(me.id, me.name);
    return Array.from(seen.entries()).map(([id, name]) => ({ id, name }));
  });

  /** Total member count — prefer group.members.length, fall back to chat participants */
  readonly memberCount = computed(() =>
    this.group?.members?.length ?? this.chatMembers().length
  );

  async ngOnChanges(changes: SimpleChanges) {
    if (changes['groupId'] && !changes['groupId'].firstChange) {
      await this.initChat();
    }
  }

  async ngOnInit() {
    await this.initChat();

    this.sub = this.chatService.messages$.subscribe(msgs => {
      this.messages.set(msgs);
      this.shouldScroll = true;
    });
  }

  private async initChat() {
    this.isLoading.set(true);
    this.chatService.disconnect(true); // Clear previous messages
    const history = await this.chatService.loadHistory(this.groupId);
    this.chatService.setMessages(history);
    this.isLoading.set(false);
    this.chatService.connect(this.groupId);
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
    this.chatService.disconnect();
  }

  send() {
    const content = this.messageControl.value?.trim();
    if (!content) return;
    if (!this.chatService.connected()) {
      // Still try — STOMP will queue if reconnecting
      console.warn('Sending while not fully connected, message may be lost');
    }
    this.chatService.sendMessage(this.groupId, content);
    this.messageControl.reset();
    // Re-focus input
    setTimeout(() => this.inputRef?.nativeElement?.focus(), 0);
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  retryConnection() {
    this.chatService.connect(this.groupId);
  }

  isOwnMessage(msg: ChatMessage): boolean {
    return msg.senderId === this.store.user()?.id;
  }

  /** Show date separator between messages on different days */
  showDateSeparator(index: number): boolean {
    if (index === 0) return true;
    const prev = this.messages()[index - 1];
    const curr = this.messages()[index];
    const prevDate = ChatService.parseSentAt(prev.sentAt);
    const currDate = ChatService.parseSentAt(curr.sentAt);
    if (!prevDate || !currDate) return false;
    return prevDate.toDateString() !== currDate.toDateString();
  }

  parseSentAt(sentAt: string | number[] | undefined): Date | null {
    return ChatService.parseSentAt(sentAt);
  }

  /** Avatar colour based on sender id */
  avatarColor(senderId: number): string {
    const colors = ['#2563eb', '#7c3aed', '#0f766e', '#d97706', '#dc2626', '#0891b2', '#65a30d'];
    return colors[senderId % colors.length];
  }

  avatarInitial(name: string): string {
    return (name || 'U').charAt(0).toUpperCase();
  }

  toggleMembers() {
    this.showMembers.update(v => !v);
  }

  private scrollToBottom() {
    try {
      const el = this.messageList?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch { /* ignore */ }
  }

  trackById(_: number, msg: ChatMessage) {
    return msg.id ?? msg.sentAt;
  }
}
