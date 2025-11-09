import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Loader2,
  Users,
  Building2,
  DollarSign,
  ArrowLeft,
  LogOut,
} from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";

interface Reservation {
  id: string;
  status: string;
  created_at: string;
  stall: {
    name: string;
    size: string;
    price: number;
  };
  hall: {
    name: string;
  };
  profile: {
    name: string;
    email: string;
    organization_name: string;
  };
}

const AllReservations = () => {
  const navigate = useNavigate();
  const { signOut } = useAuth();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    total: 0,
    confirmed: 0,
    totalRevenue: 0,
  });
  const { toast } = useToast();

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      const allReservations = await mockApi.getReservations();
      const allStalls = await mockApi.getStalls();
      const allHalls = await mockApi.getHalls();
      const users = JSON.parse(localStorage.getItem("bookfair_users") || "[]");

      const transformedData: Reservation[] = allReservations.map((res) => {
        const stall = allStalls.find((s) => s.id === res.stall_id);
        const hall = allHalls.find((h) => h.id === stall?.hall_id);
        const user = users.find((u: any) => u.id === res.user_id);

        return {
          id: res.id,
          status: res.status,
          created_at: res.created_at,
          stall: {
            name: stall?.name || "Unknown",
            size: stall?.size || "SMALL",
            price: stall?.price || 0,
          },
          hall: {
            name: hall?.name || "Unknown",
          },
          profile: {
            name: user?.name || "Unknown",
            email: user?.email || "N/A",
            organization_name: user?.organization_name || "N/A",
          },
        };
      });

      setReservations(transformedData);

      // Calculate stats
      const confirmed = transformedData.filter(
        (r) => r.status === "CONFIRMED"
      ).length;
      const revenue = transformedData
        .filter((r) => r.status === "CONFIRMED")
        .reduce((sum, r) => sum + Number(r.stall.price), 0);

      setStats({
        total: transformedData.length,
        confirmed,
        totalRevenue: revenue,
      });
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      {/* Header with Navigation */}
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/organizer/dashboard")}
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <h1 className="text-3xl font-bold">All Reservations</h1>
        </div>
        <Button variant="ghost" onClick={signOut}>
          <LogOut className="mr-2 h-4 w-4" />
          Logout
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">
              Total Reservations
            </CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.total}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Confirmed</CardTitle>
            <Building2 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.confirmed}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              LKR {stats.totalRevenue.toLocaleString()}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Reservations Table */}
      <Card>
        <CardHeader>
          <CardTitle>Reservation Details</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left p-4">Vendor</th>
                  <th className="text-left p-4">Organization</th>
                  <th className="text-left p-4">Hall</th>
                  <th className="text-left p-4">Stall</th>
                  <th className="text-left p-4">Size</th>
                  <th className="text-left p-4">Price</th>
                  <th className="text-left p-4">Status</th>
                  <th className="text-left p-4">Date</th>
                </tr>
              </thead>
              <tbody>
                {reservations.map((reservation) => (
                  <tr
                    key={reservation.id}
                    className="border-b hover:bg-muted/50"
                  >
                    <td className="p-4">
                      <div>
                        <div className="font-medium">
                          {reservation.profile.name}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          {reservation.profile.email}
                        </div>
                      </div>
                    </td>
                    <td className="p-4">
                      {reservation.profile.organization_name}
                    </td>
                    <td className="p-4">{reservation.hall.name}</td>
                    <td className="p-4 font-medium">
                      {reservation.stall.name}
                    </td>
                    <td className="p-4">
                      <Badge variant="outline">{reservation.stall.size}</Badge>
                    </td>
                    <td className="p-4">
                      LKR {Number(reservation.stall.price).toLocaleString()}
                    </td>
                    <td className="p-4">
                      <Badge
                        variant={
                          reservation.status === "CONFIRMED"
                            ? "default"
                            : reservation.status === "PENDING"
                            ? "secondary"
                            : "destructive"
                        }
                      >
                        {reservation.status}
                      </Badge>
                    </td>
                    <td className="p-4 text-sm text-muted-foreground">
                      {new Date(reservation.created_at).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default AllReservations;
