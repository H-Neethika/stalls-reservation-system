import { ReactNode, useMemo, useState } from "react";
import type { CSSProperties } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/use-auth";
import {
  Building2,
  CalendarDays,
  ChevronsLeft,
  ChevronsRight,
  LayoutDashboard,
  FileSpreadsheet,
  Settings,
} from "lucide-react";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";

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
    label: "Halls",
    icon: Building2,
    path: "/organizer/halls",
  },
  {
    label: "Reservations",
    icon: FileSpreadsheet,
    path: "/organizer/exhibition-reservations",
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
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

  const sidebarWidth = useMemo(() => (isSidebarCollapsed ? 80 : 256), [isSidebarCollapsed]);
  const headerHeight = 72;

  const cssVars = useMemo(
    () =>
      ({
        "--sidebar-width": `${sidebarWidth}px`,
        "--header-height": `${headerHeight}px`,
      }) as CSSProperties,
    [sidebarWidth, headerHeight],
  );

  const handleNavigation = (path: string) => navigate(path);

  const activePath =
    sidebarItems.find((item) => location.pathname.startsWith(item.path))
      ?.path || "/organizer/dashboard";

  return (
    <div
      className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5"
      style={cssVars}
    >
      <aside
        className="fixed inset-y-0 left-0 z-40 flex flex-col border-r bg-card/80 backdrop-blur transition-[width]"
        style={{ width: "var(--sidebar-width)" }}
      >
        <div className={cn("flex items-center border-b", isSidebarCollapsed ? "px-3 py-4" : "px-5 py-5")}>
          {!isSidebarCollapsed && (
            <div>
              <h1 className="text-2xl font-bold">Organizer</h1>
              <p className="text-sm text-muted-foreground">Stall Reservation System</p>
            </div>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="ml-auto h-9 w-9"
            onClick={() => setIsSidebarCollapsed((prev) => !prev)}
            aria-label={isSidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}
          >
            {isSidebarCollapsed ? <ChevronsRight className="h-4 w-4" /> : <ChevronsLeft className="h-4 w-4" />}
          </Button>
        </div>
        <TooltipProvider>
          <nav className="flex-1 space-y-1 px-2 py-6">
            {sidebarItems.map((item) => {
              const Icon = item.icon;
              const isActive =
                location.pathname === item.path ||
                (item.path !== "/organizer/dashboard" && location.pathname.startsWith(item.path));

              const button = (
                <button
                  key={item.label}
                  onClick={() => handleNavigation(item.path)}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm font-medium transition",
                    isActive ? "bg-primary/10 text-primary" : "text-muted-foreground hover:bg-muted",
                    isSidebarCollapsed ? "justify-center" : "justify-start",
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  {!isSidebarCollapsed && <span className="truncate">{item.label}</span>}
                </button>
              );

              if (isSidebarCollapsed) {
                return (
                  <Tooltip key={item.label}>
                    <TooltipTrigger asChild>{button}</TooltipTrigger>
                    <TooltipContent side="right">{item.label}</TooltipContent>
                  </Tooltip>
                );
              }

              return button;
            })}
          </nav>
        </TooltipProvider>
        <div className={cn("px-3 pb-6", isSidebarCollapsed ? "text-center" : "text-left")}>
          <Button variant="outline" className="w-full" onClick={signOut}>
            Sign Out
          </Button>
        </div>
      </aside>

      <header
        className="fixed top-0 right-0 z-30 border-b bg-card/80 backdrop-blur transition-[left,width]"
        style={{
          left: "var(--sidebar-width)",
          width: "clamp(0px, calc(100% - var(--sidebar-width)), 100%)",
          height: "var(--header-height)",
        }}
      >
        <div className="flex h-full items-center justify-between px-4">
          <div>
            <p className="text-sm text-muted-foreground">{user?.organizationName || "Organizer"}</p>
            <h1 className="text-2xl font-bold">
              {user?.name && title === "Dashboard" ? `${user.name}'s ${title}` : title}
            </h1>
          </div>
          <div className="flex items-center gap-3">
            {user && (
              <div className="hidden sm:flex flex-col text-right">
                <span className="text-sm font-semibold">{user.name}</span>
                <span className="text-xs text-muted-foreground">{user.email}</span>
              </div>
            )}
            <div className="sm:hidden">
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
      </header>

      <main
        className="min-h-screen bg-transparent transition-[margin-left,width]"
        style={{
          marginLeft: "var(--sidebar-width)",
          width: "clamp(0px, calc(100% - var(--sidebar-width)), 100%)",
        }}
      >
        <div
          className="px-4 pb-10 pt-6"
          style={{
            paddingTop: `calc(var(--header-height) + 1.5rem)`,
          }}
        >
          <div className="mx-auto max-w-6xl">{children}</div>
        </div>
      </main>
    </div>
  );
};

export const organizerSidebarItems = sidebarItems;
