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

export interface HallLayout {
  hall_id: string;
  layout_json: any;
  updated_at: string;
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
    description: "Signature octagon plan with concentric stall rings",
    rows: 12,
    columns: 12,
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

// Create the default Hall A stall plan (80 stalls arranged in 4 rings)
const createHallAPlan = (): Stall[] => {
  const coordinates: Array<{ row: number; col: number; ring: number }> = [];

  const addHorizontalSegment = (row: number, startCol: number, endCol: number, ring: number) => {
    for (let col = startCol; col <= endCol; col++) {
      coordinates.push({ row, col, ring });
    }
  };

  const addVerticalSegment = (col: number, startRow: number, endRow: number, ring: number) => {
    for (let row = startRow; row <= endRow; row++) {
      coordinates.push({ row, col, ring });
    }
  };

  // Outer ring
  addHorizontalSegment(0, 2, 9, 0);
  addHorizontalSegment(11, 2, 9, 0);
  addVerticalSegment(0, 2, 9, 0);
  addVerticalSegment(11, 2, 9, 0);

  // Second ring
  addHorizontalSegment(1, 3, 8, 1);
  addHorizontalSegment(10, 3, 8, 1);
  addVerticalSegment(1, 3, 8, 1);
  addVerticalSegment(10, 3, 8, 1);

  // Third ring
  addHorizontalSegment(2, 4, 7, 2);
  addHorizontalSegment(9, 4, 7, 2);
  addVerticalSegment(2, 4, 7, 2);
  addVerticalSegment(9, 4, 7, 2);

  // Inner ring
  addHorizontalSegment(3, 5, 6, 3);
  addHorizontalSegment(8, 5, 6, 3);
  addVerticalSegment(3, 5, 6, 3);
  addVerticalSegment(8, 5, 6, 3);

  const ringConfigs: Array<{ size: Stall["size"]; price: number }> = [
    { size: "SMALL", price: 5000 },
    { size: "MEDIUM", price: 8000 },
    { size: "LARGE", price: 12000 },
    { size: "LARGE", price: 15000 },
  ];

  return coordinates.map((coord, index) => {
    const { size, price } = ringConfigs[coord.ring];
    const stallNumber = String(index + 1).padStart(2, "0");

    return {
      id: `stall-hall-a-${coord.row}-${coord.col}`,
      hall_id: "hall-a",
      name: `A${stallNumber}`,
      size,
      row_position: coord.row,
      col_position: coord.col,
      price,
      is_reserved: index < 2,
    };
  });
};

// Generate mock stalls
const generateStalls = (): Stall[] => {
  const stalls: Stall[] = [];

  mockHalls.forEach((hall) => {
    if (hall.id === "hall-a") {
      stalls.push(...createHallAPlan());
      return;
    }

    const sizes: Stall["size"][] = ["SMALL", "MEDIUM", "LARGE"];
    let hallStallNumber = 0;

    for (let row = 0; row < hall.rows; row++) {
      for (let col = 0; col < hall.columns; col++) {
        if ((row + 1) % 3 === 0 && col % 2 === 0) continue;

        const size = sizes[Math.floor(Math.random() * sizes.length)];
        const basePrice = size === "SMALL" ? 5000 : size === "MEDIUM" ? 8000 : 12000;
        hallStallNumber += 1;
        const hallPrefix = hall.name.split(" ").pop()?.charAt(0).toUpperCase() || "S";

        stalls.push({
          id: `stall-${hall.id}-${row}-${col}`,
          hall_id: hall.id,
          name: `${hallPrefix}${String(hallStallNumber).padStart(2, "0")}`,
          size,
          row_position: row,
          col_position: col,
          price: basePrice,
          is_reserved: Math.random() > 0.7,
        });
      }
    }
  });

  return stalls;
};

export const mockStalls: Stall[] = generateStalls();

const findHallAStallId = (name: string) =>
  mockStalls.find((s) => s.hall_id === "hall-a" && s.name === name)?.id;

// Mock reservations
export const mockReservations: Reservation[] = [
  {
    id: "res-1",
    user_id: "vendor-1",
    stall_id: findHallAStallId("A01") || mockStalls[0]?.id || "stall-hall-a-0-2",
    status: "CONFIRMED",
    qr_code:
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    created_at: new Date(Date.now() - 86400000 * 2).toISOString(),
  },
  {
    id: "res-2",
    user_id: "vendor-1",
    stall_id: findHallAStallId("A02") || mockStalls[1]?.id || "stall-hall-a-0-3",
    status: "CONFIRMED",
    qr_code:
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    created_at: new Date(Date.now() - 86400000).toISOString(),
  },
];

const DATA_VERSION = "2025-10-hall-a-plan-v1";

const STORAGE_KEYS = {
  CURRENT_USER: "bookfair_current_user",
  USERS: "bookfair_users",
  HALLS: "bookfair_halls",
  STALLS: "bookfair_stalls",
  RESERVATIONS: "bookfair_reservations",
  LAYOUTS: "bookfair_layouts",
  DATA_VERSION: "bookfair_data_version",
};

export const initializeMockData = () => {
  const currentVersion = localStorage.getItem(STORAGE_KEYS.DATA_VERSION);

  if (currentVersion !== DATA_VERSION) {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(mockUsers));
    localStorage.setItem(STORAGE_KEYS.HALLS, JSON.stringify(mockHalls));
    localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(mockStalls));
    localStorage.setItem(STORAGE_KEYS.RESERVATIONS, JSON.stringify(mockReservations));
    localStorage.setItem(STORAGE_KEYS.LAYOUTS, JSON.stringify({}));
    localStorage.setItem(STORAGE_KEYS.DATA_VERSION, DATA_VERSION);
    return;
  }

  if (!localStorage.getItem(STORAGE_KEYS.LAYOUTS)) {
    localStorage.setItem(STORAGE_KEYS.LAYOUTS, JSON.stringify({}));
  }
};

// ---------------- Mock API ---------------- //
export const mockApi = {
  // Auth
  signIn: async (email: string, password: string) => {
    const users = JSON.parse(localStorage.getItem(STORAGE_KEYS.USERS) || "[]") as User[];
    const user = users.find((u) => u.email === email);
    if (!user) return { user: null, error: "Invalid email or password" };
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(user));
    return { user, error: null };
  },

  signUp: async (email: string, name: string, organizationName: string, role: "vendor" | "organizer") => {
    const users = JSON.parse(localStorage.getItem(STORAGE_KEYS.USERS) || "[]") as User[];
    if (users.find((u) => u.email === email)) return { user: null, error: "Email already exists" };

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

  // ---------------- Halls ----------------
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

  // ---------------- Layouts ----------------
  getLayouts: async (): Promise<Record<string, HallLayout>> => {
    return JSON.parse(localStorage.getItem(STORAGE_KEYS.LAYOUTS) || "{}");
  },

  saveLayout: async (hallId: string, layout: any): Promise<void> => {
    const layouts = JSON.parse(localStorage.getItem(STORAGE_KEYS.LAYOUTS) || "{}");
    layouts[hallId] = {
      hall_id: hallId,
      layout_json: layout,
      updated_at: new Date().toISOString(),
    };
    localStorage.setItem(STORAGE_KEYS.LAYOUTS, JSON.stringify(layouts));
  },

  getLayoutByHallId: async (hallId: string): Promise<HallLayout | null> => {
    const layouts = JSON.parse(localStorage.getItem(STORAGE_KEYS.LAYOUTS) || "{}");
    return layouts[hallId] || null;
  },

  // ---------------- Stalls ----------------
  getStalls: async (hallId?: string): Promise<Stall[]> => {
    const stalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    return hallId ? stalls.filter((s) => s.hall_id === hallId) : stalls;
  },

  createStalls: async (stalls: Omit<Stall, "id">[]): Promise<Stall[]> => {
    const existingStalls = JSON.parse(localStorage.getItem(STORAGE_KEYS.STALLS) || "[]") as Stall[];
    const newStalls: Stall[] = stalls.map((s, i) => ({ ...s, id: `stall-${Date.now()}-${i}` }));
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

  // ---------------- Reservations ----------------
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

    reservations.forEach((res) => {
      const stallIndex = stalls.findIndex((s) => s.id === res.stall_id);
      if (stallIndex !== -1) stalls[stallIndex].is_reserved = true;
    });

    existingReservations.push(...newReservations);
    localStorage.setItem(STORAGE_KEYS.RESERVATIONS, JSON.stringify(existingReservations));
    localStorage.setItem(STORAGE_KEYS.STALLS, JSON.stringify(stalls));
    return newReservations;
  },
};
