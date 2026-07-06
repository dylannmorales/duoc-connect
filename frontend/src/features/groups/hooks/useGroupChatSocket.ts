import { Client, type IMessage } from '@stomp/stompjs';
import { useCallback, useEffect, useRef, useState } from 'react';
import type { MensajeGrupo } from '@/features/groups/types/group.types';

function resolveWebSocketUrl() {
  const configured = import.meta.env.VITE_WS_URL;
  if (configured && configured.startsWith('ws')) {
    return configured.replace(/\/ws\/?$/, '/ws-native');
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws-native`;
}

interface UseGroupChatSocketOptions {
  grupoId: string;
  enabled?: boolean;
  onMessage: (mensaje: MensajeGrupo) => void;
}

export function useGroupChatSocket({ grupoId, enabled = true, onMessage }: UseGroupChatSocketOptions) {
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    const token = localStorage.getItem('accessToken');
    if (!token) {
      setError('Debes iniciar sesión para usar el chat en tiempo real');
      return;
    }

    const client = new Client({
      brokerURL: resolveWebSocketUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        setError('');
        client.subscribe(`/topic/grupos/${grupoId}`, (message: IMessage) => {
          try {
            const body = JSON.parse(message.body) as MensajeGrupo;
            onMessageRef.current(body);
          } catch {
            setError('No se pudo procesar un mensaje entrante');
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => {
        setConnected(false);
        setError('Error de conexión con el chat en tiempo real');
      },
      onWebSocketClose: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [grupoId, enabled]);

  const sendMessage = useCallback(
    (contenido: string) => {
      const client = clientRef.current;
      if (!client?.connected) {
        throw new Error('Chat no conectado');
      }

      client.publish({
        destination: `/app/grupos/${grupoId}/mensajes`,
        body: JSON.stringify({ contenido }),
      });
    },
    [grupoId],
  );

  return { connected, error, sendMessage };
}
