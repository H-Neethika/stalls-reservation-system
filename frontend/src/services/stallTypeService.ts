import { API_BASE_URL, authFetch } from "@/services/api";

export interface StallTypeResponse {
  id: number;
  name?: string;
  typeName?: string;
  type?: string;
  description?: string;
}

type ApiError = Error & { status?: number; responseBody?: unknown };

const parseErrorResponse = async (response: Response): Promise<ApiError> => {
  let body: unknown = null;
  const contentType = response.headers.get("content-type");
  try {
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

class StallTypeService {
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

  async getStallTypes(): Promise<StallTypeResponse[]> {
    const response = await authFetch(`${API_BASE_URL}/api/stall-types`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }
}

export const stallTypeService = new StallTypeService();
