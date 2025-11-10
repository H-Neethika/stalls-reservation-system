import { CreateExhibitionRequest, Exhibition } from "@/types";
import { API_BASE_URL } from "@/services/api";

type ApiError = Error & {
  status?: number;
  code?: number;
  responseBody?: unknown;
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
    (typeof errorBody === "object" && errorBody !== null
      ? (errorBody as { message?: string; error?: string }).message ||
        (errorBody as { message?: string; error?: string }).error
      : null) ||
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

    return response.json();
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

    return response.json();
  }
}

export const exhibitionService = new ExhibitionService();
