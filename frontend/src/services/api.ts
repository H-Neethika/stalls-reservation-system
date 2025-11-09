// API service for backend integration
import { User, AuthResponse, LoginRequest, RegisterRequest } from "@/types";

const API_BASE_URL = "http://localhost:5000";

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
      const response = await fetch(url, config);

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
    const response = await this.request<AuthResponse>("/api/users/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });

    // Store tokens in localStorage
    localStorage.setItem("accessToken", response.accessToken);
    localStorage.setItem("refreshToken", response.refreshToken);
    localStorage.setItem("user", JSON.stringify(response.user));

    return response;
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
  initiateOAuth2Login(provider: "google" | "github"): void {
    // Redirect to backend OAuth2 login endpoint
    window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`;
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
