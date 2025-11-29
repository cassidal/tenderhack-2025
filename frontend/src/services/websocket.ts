import { Client } from '@stomp/stompjs';

const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080';
const WS_ENDPOINT = '/ws-grouping';

export interface TaskStatusEvent {
  taskId: string;
  status: 'RUNNING' | 'COMPLETED' | 'ERROR';
  progress?: number | null;
  message?: string | null;
  timestamp?: string;
}


export class WebSocketService {
  private client: Client | null = null;
  private taskId: string | null = null;
  private onMessageCallback: ((message: TaskStatusEvent) => void) | null = null;
  private onCompleteCallback: (() => void) | null = null;
  private onErrorCallback: ((error: any) => void) | null = null;

  connect(taskId: string, onComplete: () => void, onError?: (error: any) => void): void {
    this.taskId = taskId;
    this.onCompleteCallback = onComplete;
    this.onErrorCallback = onError;

    // Преобразуем HTTP URL в WebSocket URL если нужно
    let wsUrl = WS_BASE_URL;
    if (!wsUrl.startsWith('ws://') && !wsUrl.startsWith('wss://')) {
      wsUrl = wsUrl.replace('http://', 'ws://').replace('https://', 'wss://');
    }
    
    this.client = new Client({
      brokerURL: `${wsUrl}${WS_ENDPOINT}`,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected successfully');
        console.log('Subscribing to topic for taskId:', this.taskId);
        this.subscribe();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        if (this.onErrorCallback) {
          this.onErrorCallback(frame);
        }
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
      },
    });

    this.client.activate();
  }

  private subscribe(): void {
    if (!this.client || !this.taskId) {
      console.error('Cannot subscribe: client or taskId is null', { client: !!this.client, taskId: this.taskId });
      return;
    }

    // Топик соответствует формату из NotificationService: /topic/tasks/{taskId}/status
    const topic = `/topic/tasks/${this.taskId}/status`;
    console.log('Subscribing to topic:', topic);
    
    this.client.subscribe(topic, (message) => {
      try {
        const data: TaskStatusEvent = JSON.parse(message.body);
        console.log('WebSocket message received:', data);
        console.log('Message status:', data.status);
        console.log('Message taskId:', data.taskId);

        if (this.onMessageCallback) {
          this.onMessageCallback(data);
        }

        // Проверяем, завершена ли задача
        if (data.status === 'COMPLETED' || data.status === 'ERROR') {
          console.log('Task completed or errored, calling onComplete callback');
          this.onCompleteCallback?.();
          // Не отключаем сразу, чтобы можно было получать финальное сообщение
          setTimeout(() => {
            console.log('Disconnecting WebSocket after completion');
            this.disconnect();
          }, 1000);
        }
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
        console.error('Message body:', message.body);
      }
    });
  }

  setOnMessage(callback: (message: TaskStatusEvent) => void): void {
    this.onMessageCallback = callback;
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.taskId = null;
    this.onMessageCallback = null;
    this.onCompleteCallback = null;
    this.onErrorCallback = null;
  }

  isConnected(): boolean {
    return this.client?.active || false;
  }
}

export const websocketService = new WebSocketService();

