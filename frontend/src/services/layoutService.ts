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

export interface LayoutStall {
  id: number | string;
  stallTypeId?: number;
  stallType?: string;
  bookingStatus?: string;
  path?: string | null;
  points?: { x: number; y: number }[];
}

export interface LayoutHall {
  exhibitionHallId?: number;
  hallId?: number;
  hallName?: string;
  stalls?: LayoutStall[];
}

export interface LayoutExhibition {
  id: number;
  exhibitionName: string;
  exhibitionState?: string;
  halls?: LayoutHall[];
}

export interface LayoutResponse {
  exhibitions?: LayoutExhibition[];
}

class LayoutService {
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

  async getLayoutsByOrganizer(organizerId: number): Promise<LayoutResponse> {
    const response = await authFetch(`${API_BASE_URL}/api/layout/organizers/${organizerId}`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }

  async getLayoutByHall(hallId: number): Promise<LayoutHall | null> {
    const response = await authFetch(`${API_BASE_URL}/api/layout/halls/${hallId}`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    const data = (await response.json()) as { halls?: LayoutHall[] } | LayoutHall;
    if (data && "halls" in data && Array.isArray((data as { halls?: LayoutHall[] }).halls)) {
      return (data as { halls?: LayoutHall[] }).halls?.[0] ?? null;
    }
    return data as LayoutHall;
  }

  async getAllHallLayouts(): Promise<{ halls?: LayoutHall[] }> {
    const response = await authFetch(`${API_BASE_URL}/api/layout/halls`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }

  async getExhibitionLayouts(exhibitionId: number): Promise<{
    halls?: Array<{
      hallId: number;
      exhibitionHallId?: number;
      hallName?: string;
      stalls?: Array<{
        stallId: number | string;
        exhibitionStallId?: number;
        status?: string;
      }>;
    }>;
  }> {
    const response = await authFetch(`${API_BASE_URL}/api/layout/exhibitions/${exhibitionId}`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw await parseErrorResponse(response);
    }

    return response.json();
  }
}

export const layoutService = new LayoutService();
