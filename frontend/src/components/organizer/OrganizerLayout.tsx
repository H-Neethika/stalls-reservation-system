import { ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/use-auth";
import {
  Building2,
  CalendarDays,
  LayoutDashboard,
  PanelsTopLeft,
  Settings,
} from "lucide-react";

const sidebarItems = [
  {
    label: "Dashboard",
    icon: LayoutDashboard,
    path: "/organizer/dashboard",
  },
  {
    label: "Exhibitions",
    icon: CalendarDays,
    path: "/organizer/exhibitions",
  },
  {
    label: "Manage Halls",
    icon: Building2,
    path: "/organizer/halls",
  },
  {
    label: "Manage Stalls",
    icon: PanelsTopLeft,
    path: "/organizer/stalls",
  },
  {
    label: "Settings",
    icon: Settings,
    path: "/organizer/settings",
  },
];

interface OrganizerLayoutProps {
  title: string;
  children: ReactNode;
}

export const OrganizerLayout = ({ title, children }: OrganizerLayoutProps) => {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleNavigation = (path: string) => navigate(path);

  const activePath =
    sidebarItems.find((item) => location.pathname.startsWith(item.path))
      ?.path || "/organizer/dashboard";

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <div className="flex min-h-screen">
        <aside className="hidden lg:flex w-64 flex-col border-r bg-card/60 backdrop-blur pb-6">
          <div className="px-6 py-6 border-b">
            <h1 className="text-2xl font-bold">Organizer</h1>
            <p className="text-sm text-muted-foreground">
              Stall Reservation System
            </p>
          </div>
          <nav className="flex-1 px-4 py-6 space-y-1">
            {sidebarItems.map((item) => {
              const Icon = item.icon;
              const isActive =
                location.pathname === item.path ||
                (item.path !== "/organizer/dashboard" &&
                  location.pathname.startsWith(item.path));

              return (
                <button
                  key={item.label}
                  onClick={() => handleNavigation(item.path)}
                  className={`w-full flex items-center gap-3 rounded-lg px-4 py-2 text-left text-sm font-medium transition ${
                    isActive
                      ? "bg-primary/10 text-primary"
                      : "text-muted-foreground hover:bg-muted"
                  }`}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </button>
              );
            })}
          </nav>
          <div className="px-4">
            <Button variant="outline" className="w-full" onClick={signOut}>
              Sign Out
            </Button>
          </div>
        </aside>

        <main className="flex-1">
          <nav className="border-b bg-card/50 backdrop-blur">
            <div className="container mx-auto px-4 py-4 flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">
                  {user?.organizationName || "Organizer"}
                </p>
                <h1 className="text-2xl font-bold">
                  {user?.name && title === "Dashboard"
                    ? `${user.name}'s ${title}`
                    : title}
                </h1>
              </div>
              <div className="flex items-center gap-3">
                {user && (
                  <div className="hidden lg:flex flex-col text-right">
                    <span className="text-sm font-semibold">{user.name}</span>
                    <span className="text-xs text-muted-foreground">
                      {user.email}
                    </span>
                  </div>
                )}
                <div className="lg:hidden">
                  <select
                    className="border rounded-md px-3 py-2 text-sm bg-background"
                    value={activePath}
                    onChange={(e) => handleNavigation(e.target.value)}
                  >
                    {sidebarItems.map((item) => (
                      <option key={item.label} value={item.path}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </div>
                <Button variant="ghost" onClick={signOut}>
                  Sign Out
                </Button>
              </div>
            </div>
          </nav>

          <div className="container mx-auto px-4 py-12">{children}</div>
        </main>
      </div>
    </div>
  );
};

export const organizerSidebarItems = sidebarItems;

