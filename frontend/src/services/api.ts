// API service for backend integration
import { User, AuthResponse, LoginRequest, RegisterRequest } from "@/types";

export const API_BASE_URL =
  import.meta.env.BACKEND_BASE_URL ||
  import.meta.env.VITE_BACKEND_BASE_URL ||
  `http://localhost:${import.meta.env.CLOUD_GATEWAY_PORT || "6001"}`;

const getStoredAccessToken = () => localStorage.getItem("accessToken");
const getStoredRefreshToken = () => localStorage.getItem("refreshToken");

const refreshAccessToken = async (): Promise<AuthResponse> => {
  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) {
    throw new Error("No refresh token available. Please sign in again.");
  }

  const response = await fetch(`${API_BASE_URL}/api/users/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    throw new Error("Session expired. Please sign in again.");
  }

  const data = (await response.json()) as AuthResponse;
  if (data.accessToken) {
    localStorage.setItem("accessToken", data.accessToken);
  }
  if (data.refreshToken) {
    localStorage.setItem("refreshToken", data.refreshToken);
  }
  if (data.user) {
    localStorage.setItem("user", JSON.stringify(data.user));
  }
  return data;
};

export const authFetch = async (
  input: string,
  options: RequestInit = {},
  triedRefresh = false,
): Promise<Response> => {
  const headers = new Headers(options.headers as HeadersInit);
  const accessToken = getStoredAccessToken();
  if (accessToken && !headers.get("Authorization")) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(input, { ...options, headers });

  if (response.status === 401 && !triedRefresh) {
    try {
      await refreshAccessToken();
      const retryHeaders = new Headers(options.headers as HeadersInit);
      const newToken = getStoredAccessToken();
      if (newToken) {
        retryHeaders.set("Authorization", `Bearer ${newToken}`);
      }
      if (!retryHeaders.get("Content-Type") && headers.get("Content-Type")) {
        retryHeaders.set("Content-Type", headers.get("Content-Type") as string);
      }
      return fetch(input, { ...options, headers: retryHeaders });
    } catch (error) {
      throw error;
    }
  }

  return response;
};

class ApiService {
  private getAuthToken(): string | null {
    return localStorage.getItem("accessToken");
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = this.getAuthToken();

    const config: RequestInit = {
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await authFetch(url, config);

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || `HTTP error! status: ${response.status}`);
      }

      // Handle empty responses
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      } else {
        return {} as T;
      }
    } catch (error) {
      console.error(`API request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  // Authentication
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const url = `${API_BASE_URL}/api/users/login`;
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      let message = "Login failed";
      try {
        const text = await response.text();
        if (text) {
          try {
            const parsed = JSON.parse(text);
            if (typeof parsed === "object" && parsed && typeof parsed.message === "string") {
              message = parsed.message;
            } else if (typeof text === "string") {
              message = text;
            }
          } catch {
            message = text;
          }
        }
      } catch {
        // ignore
      }
      throw new Error(message);
    }

    const data = (await response.json()) as AuthResponse;

    // Store tokens in localStorage
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);
    localStorage.setItem("user", JSON.stringify(data.user));

    return data;
  }

  async register(userData: RegisterRequest): Promise<User> {
    const response = await this.request<User>("/api/users/register", {
      method: "POST",
      body: JSON.stringify(userData),
    });

    return response;
  }

  async logout(): Promise<void> {
    // Clear local storage
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  }

  // OAuth2 Authentication
  initiateOAuth2Login(
    provider: "google" | "github",
    mode: "signin" | "signup" = "signin"
  ): void {
    // Redirect to backend OAuth2 login endpoint with mode parameter
    window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}?mode=${mode}`;
  }

  async handleOAuth2Callback(
    code: string,
    state: string
  ): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>(
      `/oauth2/callback?code=${code}&state=${state}`,
      {
        method: "GET",
      }
    );

    // Store tokens in localStorage
    if (response.accessToken) {
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);
      localStorage.setItem("user", JSON.stringify(response.user));
    }

    return response;
  }

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  }

  // User management
  async getUserById(id: number): Promise<User> {
    return await this.request<User>(`/api/users/${id}`);
  }

  async getUsersByRole(role: string): Promise<User[]> {
    return await this.request<User[]>(`/api/users/role/${role}`);
  }

  // Token refresh (for future implementation)
  async refreshToken(): Promise<AuthResponse> {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      throw new Error("No refresh token available");
    }

    const response = await this.request<AuthResponse>("/api/users/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });

    // Update tokens
    localStorage.setItem("accessToken", response.accessToken);
    localStorage.setItem("refreshToken", response.refreshToken);
    localStorage.setItem("user", JSON.stringify(response.user));

    return response;
  }
}

export const apiService = new ApiService();
