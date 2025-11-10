import { CreateExhibitionRequest, Exhibition } from "@/types";
import { API_BASE_URL } from "@/services/api";

class ExhibitionService {
  async createExhibition(
    payload: CreateExhibitionRequest
  ): Promise<Exhibition> {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      throw new Error("Missing access token. Please sign in again.");
    }

    const response = await fetch(`${API_BASE_URL}/api/exhibition`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
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
        // swallow parsing errors; we'll fall back to generic message
      }

      const errorMessage =
        (typeof errorBody === "object" && errorBody !== null
          ? (errorBody as { message?: string; error?: string }).message ||
            (errorBody as { message?: string; error?: string }).error
          : null) ||
        (typeof errorBody === "string" ? errorBody : null) ||
        "Failed to create exhibition";

      const errorCode =
        (typeof errorBody === "object" && errorBody !== null
          ? (errorBody as { code?: number; errorCode?: number }).code ??
            (errorBody as { code?: number; errorCode?: number }).errorCode
          : undefined) ?? response.status;

      const enrichedError = new Error(errorMessage) as Error & {
        status?: number;
        code?: number;
        responseBody?: unknown;
      };
      enrichedError.status = response.status;
      enrichedError.code = errorCode;
      enrichedError.responseBody = errorBody;
      throw enrichedError;
    }

    return response.json();
  }
}

export const exhibitionService = new ExhibitionService();
