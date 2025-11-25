import { Client, IMessage, Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";

type StallStatusMessage = {
  exhibitionId: number;
  hallId: number;
  stallId: number;
  exhibitionStallId?: number;
  status?: string;
  reservationId?: number;
  expiresAt?: number;
};

export type StallStatusHandler = (msg: StallStatusMessage) => void;

let client: Client | null = null;

export function connectRealtime(exhibitionId: string | number, onMessage: StallStatusHandler) {
  if (!exhibitionId) return { disconnect: () => {} };

  const baseUrl =
    import.meta.env.BACKEND_BASE_URL?.toString()  ||
    `http://localhost:${import.meta.env.CLOUD_GATEWAY_PORT || 9000}`;
  const endpoint = `${baseUrl.replace(/\/$/, "")}/ws-stalls`;

  const socket = new SockJS(endpoint);
  client = Stomp.over(socket);
  client.reconnectDelay = 5000;
  client.debug = () => {};

  const subscriptionPath = `/topic/exhibition/${exhibitionId}/stalls`;

  client.onConnect = () => {
    client?.subscribe(subscriptionPath, (frame: IMessage) => {
      try {
        const payload = JSON.parse(frame.body) as StallStatusMessage;
        onMessage(payload);
      } catch (err) {
        console.error("Failed to parse realtime message", err);
      }
    });
  };

  client.activate();

  return {
    disconnect: () => {
      try {
        client?.deactivate();
      } catch {
        // ignore
      }
    },
  };
}
