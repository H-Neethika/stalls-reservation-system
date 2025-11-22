import { API_BASE_URL } from "@/services/api";

type ApiError = Error & {
  status?: number;
  code?: number;
  responseBody?: unknown;
};

interface CreateHallRequest {
  hallName: string;
  rows: number;
  columns: number;
}

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
    // ignore parsing failure
  }

  const message =
    (typeof errorBody === "object" &&
    errorBody !== null &&
    "message" in (errorBody as Record<string, unknown>) &&
    typeof (errorBody as { message?: string }).message === "string"
      ? (errorBody as { message: string }).message
      : null) ||
    (typeof errorBody === "string" ? errorBody : null) ||
    "Request failed";

  const code =
    (typeof errorBody === "object" &&
    errorBody !== null &&
    "code" in (errorBody as Record<string, unknown>)
      ? Number((errorBody as { code?: number }).code)
      : undefined) ?? response.status;

  const error = new Error(message) as ApiError;
  error.status = response.status;
  error.code = code;
  error.responseBody = errorBody;
  return error;
};

interface HallResponse {
  id?: string;
  hallName?: string;
  rows?: number;
  columns?: number;
  createdAt?: string;
  created_at?: string;
  totalStalls?: number;
  description?: string;
}

class HallService {
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

  async createHall(payload: CreateHallRequest) {
    const response = await fetch(`${API_BASE_URL}/api/hall`, {
      method: "POST",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }

  async getHalls(): Promise<HallResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/hall`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }
}

export const hallService = new HallService();
