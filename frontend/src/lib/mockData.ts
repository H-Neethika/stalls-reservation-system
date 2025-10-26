// Mock data for frontend-only application

export interface User {
  id: string;
  email: string;
  name: string;
  organization_name?: string;
  role: "vendor" | "organizer";
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

// Mock users
export const mockUsers: User[] = [
  {
    id: "vendor-1",
    email: "vendor@bookfair.com",
    name: "John Doe",
    organization_name: "ABC Publishers",
    role: "vendor",
  },
  {
    id: "organizer-1",
    email: "organizer@bookfair.com",
    name: "Admin User",
    organization_name: "Bookfair Organization",
    role: "organizer",
  },
];

// Mock halls
export const mockHalls: Hall[] = [
  {
    id: "hall-a",
    name: "Hall A",
    description: "Main exhibition hall with premium stalls",
    rows: 8,
    columns: 10,
    created_by: "organizer-1",
    created_at: new Date().toISOString(),
  },
  {
    id: "hall-b",
    name: "Hall B",
    description: "Secondary hall for specialized genres",
    rows: 6,
    columns: 8,
    created_by: "organizer-1",
    created_at: new Date().toISOString(),
  },
  {
    id: "hall-c",
    name: "Hall C",
    description: "Compact hall for independent publishers",
    rows: 5,
    columns: 6,
    created_by: "organizer-1",
    created_at: new Date().toISOString(),
  },
];

// Generate mock stalls
const generateStalls = (): Stall[] => {
  const stalls: Stall[] = [];
  let stallIndex = 0;

  mockHalls.forEach((hall) => {
    for (let row = 0; row < hall.rows; row++) {
      for (let col = 0; col < hall.columns; col++) {
        // Create some gaps for walkways
        if ((row + 1) % 3 === 0 && col % 2 === 0) continue;

        const letter = String.fromCharCode(65 + Math.floor(stallIndex / 26) % 26);
        const number = (stallIndex % 26) + 1;
        const sizes: ("SMALL" | "MEDIUM" | "LARGE")[] = ["SMALL", "MEDIUM", "LARGE"];
        const size = sizes[Math.floor(Math.random() * sizes.length)];
        const basePrice = size === "SMALL" ? 5000 : size === "MEDIUM" ? 8000 : 12000;

        stalls.push({
          id: `stall-${hall.id}-${row}-${col}`,
          hall_id: hall.id,
          name: `${letter}${number}`,
          size,
          row_position: row,
          col_position: col,
          price: basePrice,
          is_reserved: Math.random() > 0.7, // 30% reserved randomly
        });

        stallIndex++;
      }
    }
  });

  return stalls;
};

export const mockStalls: Stall[] = generateStalls();

// Mock reservations
export const mockReservations: Reservation[] = [
  {
    id: "res-1",
    user_id: "vendor-1",
    stall_id: mockStalls.filter((s) => s.is_reserved)[0]?.id || "stall-hall-a-0-0",
    status: "CONFIRMED",
    qr_code: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    created_at: new Date(Date.now() - 86400000 * 2).toISOString(),
  },
  {
    id: "res-2",
    user_id: "vendor-1",
    stall_id: mockStalls.filter((s) => s.is_reserved)[1]?.id || "stall-hall-a-0-1",
    status: "CONFIRMED",
    qr_code: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    created_at: new Date(Date.now() - 86400000).toISOString(),
  },
];

// Local storage keys
const STORAGE_KEYS = {
  CURRENT_USER: "bookfair_current_user",
  USERS: "bookfair_users",
  HALLS: "bookfair_halls",
  STALLS: "bookfair_stalls",
  RESERVATIONS: "bookfair_reservations",
};

// Initialize local storage with mock data
export const initializeMockData = () => {
  if (!localStorage.getItem(STORAGE_KEYS.USERS)) {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(mockUsers));
  }
  if (!localStorage.getItem(STORAGE_KEYS.HALLS)) {
    localStorage.setItem(STORAGE_KEYS.HALLS, JSON.stringify(mockHalls));
  }
  if (!localStorage.getItem(STORAGE_KEYS.STALLS)) {
    localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(mockStalls));
  }
  if (!localStorage.getItem(STORAGE_KEYS.RESERVATIONS)) {
    localStorage.setItem(STORAGE_KEYS.RESERVATIONS, JSON.stringify(mockReservations));
  }
};

// Mock API functions
export const mockApi = {
  // Auth
  signIn: async (email: string, password: string): Promise<{ user: User | null; error: string | null }> => {
    const users = JSON.parse(localStorage.getItem(STORAGE_KEYS.USERS) || "[]") as User[];
    const user = users.find((u) => u.email === email);
    
    if (!user) {
      return { user: null, error: "Invalid email or password" };
    }
    
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(user));
    return { user, error: null };
  },

  signUp: async (email: string, name: string, organizationName: string, role: "vendor" | "organizer"): Promise<{ user: User | null; error: string | null }> => {
    const users = JSON.parse(localStorage.getItem(STORAGE_KEYS.USERS) || "[]") as User[];
    
    if (users.find((u) => u.email === email)) {
      return { user: null, error: "Email already exists" };
    }

    const newUser: User = {
      id: `user-${Date.now()}`,
      email,
      name,
      organization_name: organizationName,
      role,
    };

    users.push(newUser);
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(newUser));
    
    return { user: newUser, error: null };
  },

  signOut: async () => {
    localStorage.removeItem(STORAGE_KEYS.CURRENT_USER);
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem(STORAGE_KEYS.CURRENT_USER);
    return userStr ? JSON.parse(userStr) : null;
  },

  // Halls
  getHalls: async (): Promise<Hall[]> => {
    return JSON.parse(localStorage.getItem(STORAGE_KEYS.HALLS) || "[]");
  },

  createHall: async (hall: Omit<Hall, "id" | "created_at">): Promise<Hall> => {
    const halls = JSON.parse(localStorage.getItem(STORAGE_KEYS.HALLS) || "[]") as Hall[];
    const newHall: Hall = {
      ...hall,
      id: `hall-${Date.now()}`,
      created_at: new Date().toISOString(),
    };
    halls.push(newHall);
    localStorage.setItem(STORAGE_KEYS.HALLS, JSON.stringify(halls));
    return newHall;
  },

  // Stalls
  getStalls: async (hallId?: string): Promise<Stall[]> => {
    const stalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    return hallId ? stalls.filter((s) => s.hall_id === hallId) : stalls;
  },

  createStalls: async (stalls: Omit<Stall, "id">[]): Promise<Stall[]> => {
    const existingStalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    const newStalls: Stall[] = stalls.map((s, index) => ({
      ...s,
      id: `stall-${Date.now()}-${index}`,
    }));
    existingStalls.push(...newStalls);
    localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(existingStalls));
    return newStalls;
  },

  updateStall: async (id: string, updates: Partial<Stall>): Promise<void> => {
    const stalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    const index = stalls.findIndex((s) => s.id === id);
    if (index !== -1) {
      stalls[index] = { ...stalls[index], ...updates };
      localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(stalls));
    }
  },

  // Reservations
  getReservations: async (userId?: string): Promise<Reservation[]> => {
    const reservations = JSON.parse(localStorage.getItem(STORAGE_KEYS.RESERVATIONS) || "[]") as Reservation[];
    return userId ? reservations.filter((r) => r.user_id === userId) : reservations;
  },

  createReservations: async (reservations: Omit<Reservation, "id" | "created_at">[]): Promise<Reservation[]> => {
    const existingReservations = JSON.parse(localStorage.getItem(STORAGE_KEYS.RESERVATIONS) || "[]") as Reservation[];
    const stalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    
    const newReservations: Reservation[] = reservations.map((r, index) => ({
      ...r,
      id: `res-${Date.now()}-${index}`,
      created_at: new Date().toISOString(),
    }));

    // Mark stalls as reserved
    reservations.forEach((res) => {
      const stallIndex = stalls.findIndex((s) => s.id === res.stall_id);
      if (stallIndex !== -1) {
        stalls[stallIndex].is_reserved = true;
      }
    });

    existingReservations.push(...newReservations);
    localStorage.setItem(STORAGE_KEYS.RESERVATIONS, JSON.stringify(existingReservations));
    localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(stalls));
    
    return newReservations;
  },
};
