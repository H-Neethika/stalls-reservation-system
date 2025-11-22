import { CreateExhibitionRequest, Exhibition } from "@/types";
import { API_BASE_URL } from "@/services/api";

type ApiError = Error & {
  status?: number;
  code?: number;
  responseBody?: unknown;
};

const extractErrorMessage = (payload: unknown): string | undefined => {
  if (!payload) return undefined;
  if (typeof payload === "string") return payload;

  if (typeof payload === "object") {
    const obj = payload as Record<string, unknown>;
    if (typeof obj.message === "string") return obj.message;
    if (typeof obj.error === "string") return obj.error;

    if (obj.error && typeof obj.error === "object") {
      const nested = obj.error as Record<string, unknown>;
      if (typeof nested.message === "string") return nested.message;
      if (typeof nested.detail === "string") return nested.detail;
    }
  }

  return undefined;
};

const parseErrorResponse = async (response: Response): Promise<ApiError> => {
  let errorBody: unknown = null;
  const contentType = response.headers.get("content-type");

  try {
    if (contentType && contentType.includes("application/json")) {
      errorBody = await response.json();
    } else {
      const text = await response.text();
      errorBody = text || null;
    }
  } catch {
    // ignore parsing issues
  }

  const message =
    extractErrorMessage(errorBody) ||
    (typeof errorBody === "string" ? errorBody : null) ||
    "Request failed";

  const code =
    (typeof errorBody === "object" && errorBody !== null
      ? (errorBody as { code?: number; errorCode?: number }).code ??
        (errorBody as { code?: number; errorCode?: number }).errorCode
      : undefined) ?? response.status;

  const error = new Error(message) as ApiError;
  error.status = response.status;
  error.code = code;
  error.responseBody = errorBody;
  return error;
};

class ExhibitionService {
  private async parseJsonSafe<T>(response: Response): Promise<T> {
    const text = await response.text();
    if (!text) {
      return {} as T;
    }
    try {
      return JSON.parse(text) as T;
    } catch {
      throw new Error("Invalid JSON response from server");
    }
  }
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

  async createExhibition(
    payload: CreateExhibitionRequest
  ): Promise<Exhibition> {
    const response = await fetch(`${API_BASE_URL}/api/exhibition`, {
      method: "POST",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return this.parseJsonSafe<Exhibition>(response);
  }

  async getExhibitionsByOrganizer(
    organizerId: number
  ): Promise<Exhibition[]> {
    const response = await fetch(
      `${API_BASE_URL}/api/exhibition/user/${organizerId}`,
      {
        method: "GET",
        headers: this.getAuthHeaders(),
      }
    );

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return this.parseJsonSafe<Exhibition[]>(response);
  }

  async getExhibition(id: string): Promise<Exhibition> {
    const response = await fetch(`${API_BASE_URL}/api/exhibition/${id}`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return this.parseJsonSafe<Exhibition>(response);
  }

  async updateExhibition(
    id: string,
    payload: CreateExhibitionRequest
  ): Promise<Exhibition> {
    const response = await fetch(`${API_BASE_URL}/api/exhibition/${id}`, {
      method: "PUT",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    let parsed: Exhibition | null = null;
    try {
      parsed = await this.parseJsonSafe<Exhibition>(response);
    } catch (_error) {
      // Some backends respond with plain text on 2xx, so fall back to payload data.
      parsed = null;
    }

    if (parsed && parsed.id) {
      return parsed;
    }
    return {
      id,
      organizerId: payload.organizerId,
      exhibitionName: payload.exhibitionName,
      startDateTime: payload.startDateTime,
      endDateTime: payload.endDateTime,
      bookingOpenDateTime: payload.bookingOpenDateTime,
      bookingCloseDateTime: payload.bookingCloseDateTime,
      stallsPerPerson: payload.stallsPerPerson,
    };
  }

  async deleteExhibition(id: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/exhibition/${id}`, {
      method: "DELETE",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }
  }
}

export const exhibitionService = new ExhibitionService();
