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
      const errorText = await response.text();
      throw new Error(errorText || "Failed to create exhibition");
    }

    return response.json();
  }
}

export const exhibitionService = new ExhibitionService();
