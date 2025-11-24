import { API_BASE_URL, authFetch } from "@/services/api";

type ApiError = Error & { status?: number; responseBody?: unknown };

const parseErrorResponse = async (response: Response): Promise<ApiError> => {
  let body: unknown = null;
  try {
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      body = await response.json();
    } else {
      body = await response.text();
    }
  } catch {
    body = null;
  }
  const message =
    (typeof body === "object" &&
      body !== null &&
      "message" in (body as Record<string, unknown>) &&
      typeof (body as { message?: string }).message === "string" &&
      (body as { message: string }).message) ||
    (typeof body === "string" ? body : "Request failed");

  const err = new Error(message) as ApiError;
  err.status = response.status;
  err.responseBody = body;
  return err;
};

class ReservationService {
  private getAuthHeaders() {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      throw new Error("Missing access token. Please sign in again.");
    }
    return {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    };
  }

  async createReservation(payload: { exhibitionId: number; stallIds: number[] }) {
    const response = await authFetch(`${API_BASE_URL}/api/reservation`, {
      method: "POST",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }
    return response.json();
  }

  async createPayment(payload: { reservationId: number; currency: string }) {
    const response = await authFetch(`${API_BASE_URL}/api/reservation/payment`, {
      method: "PUT",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }
    return response.json();
  }

  async updateStallStatus(payload: { stallIds: number[]; bookingStatus: string }) {
    const response = await authFetch(`${API_BASE_URL}/api/booking-status/update`, {
      method: "POST",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }
    return response.json();
  }
}

export const reservationService = new ReservationService();
