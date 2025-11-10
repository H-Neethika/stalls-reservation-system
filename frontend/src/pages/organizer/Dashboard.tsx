import { useNavigate } from "react-router-dom";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Building2, LayoutDashboard, ListChecks } from "lucide-react";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";

const OrganizerDashboard = () => {
  const navigate = useNavigate();

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
    <OrganizerLayout title="Dashboard">
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
    </OrganizerLayout>
  );
};

export default OrganizerDashboard;
