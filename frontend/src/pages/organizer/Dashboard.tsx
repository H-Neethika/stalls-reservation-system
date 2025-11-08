import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Building2, LayoutDashboard, ListChecks } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

const OrganizerDashboard = () => {
  const navigate = useNavigate();
  const { signOut } = useAuth();

  const menuItems = [
    {
      title: "Manage Halls",
      description: "View and create exhibition halls",
      icon: Building2,
      path: "/organizer/halls",
    },
    {
      title: "Design Halls",
      description: "Create custom hall layouts",
      icon: LayoutDashboard,
      path: "/organizer/halls/design",
    },
    {
      title: "All Reservations",
      description: "View all vendor bookings",
      icon: ListChecks,
      path: "/organizer/reservations",
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold">Organizer Dashboard</h1>
          <Button variant="ghost" onClick={signOut}>
            Sign Out
          </Button>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-12">
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold mb-4">Welcome, Organizer</h2>
          <p className="text-muted-foreground text-lg">
            Manage exhibition halls and monitor reservations
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <Card 
                key={item.path}
                className="hover-scale cursor-pointer"
                onClick={() => navigate(item.path)}
              >
                <CardHeader>
                  <Icon className="w-12 h-12 mb-4 text-primary" />
                  <CardTitle>{item.title}</CardTitle>
                  <CardDescription>{item.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button variant="outline" className="w-full">
                    Open
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default OrganizerDashboard;
