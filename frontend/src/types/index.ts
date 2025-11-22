// Shared type definitions for the application

export interface User {
  id: number;
  name: string;
  email: string;
  organizationName: string;
  role: "ORGANIZER" | "VENDOR";
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  organizationName: string;
  role: "ORGANIZER" | "VENDOR";
  password: string;
}

export interface Hall {
  id: string;
  name: string;
  description: string;
  rows: number;
  columns: number;
  created_by: string;
  created_at: string;
}

export interface Stall {
  id: string;
  hall_id: string;
  name: string;
  size: "SMALL" | "MEDIUM" | "LARGE";
  row_position: number;
  col_position: number;
  price: number;
  is_reserved: boolean;
}

export interface Reservation {
  id: string;
  user_id: string;
  stall_id: string;
  status: "CONFIRMED" | "PENDING" | "CANCELLED";
  qr_code?: string;
  created_at: string;
}

export interface CreateExhibitionRequest {
  organizerId: number;
  exhibitionName: string;
  startDateTime: string;
  endDateTime: string;
  bookingOpenDateTime: string;
  bookingCloseDateTime: string;
  stallsPerPerson: number;
}

export interface Exhibition {
  id: string;
  organizerId: number;
  exhibitionName: string;
  startDateTime: string;
  endDateTime: string;
  bookingOpenDateTime: string;
  bookingCloseDateTime: string;
  stallsPerPerson: number;
  status?: string;
  exhibitionState?: string;
  venue?: string;
  halls?: number;
  stalls?: number;
  description?: string;
}
