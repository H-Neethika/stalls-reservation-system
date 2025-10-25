import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2, ArrowLeft, Download } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";

interface Booking {
  id: string;
  status: string;
  created_at: string;
  qr_code?: string;
  stall: {
    name: string;
    size: string;
    price: number;
  };
  hall: {
    name: string;
  };
}

const MyBookings = () => {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user) {
      fetchBookings();
    }
  }, [user]);

  const fetchBookings = async () => {
    try {
      const reservations = await mockApi.getReservations(user?.id);
      const allStalls = await mockApi.getStalls();
      const allHalls = await mockApi.getHalls();

      const transformedBookings: Booking[] = reservations.map((res) => {
        const stall = allStalls.find((s) => s.id === res.stall_id);
        const hall = allHalls.find((h) => h.id === stall?.hall_id);

        return {
          id: res.id,
          status: res.status,
          created_at: res.created_at,
          qr_code: res.qr_code,
          stall: {
            name: stall?.name || "Unknown",
            size: stall?.size || "SMALL",
            price: stall?.price || 0,
          },
          hall: {
            name: hall?.name || "Unknown",
          },
        };
      });

      setBookings(transformedBookings);
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

  const downloadQRCode = (qrCode: string, stallName: string) => {
    const link = document.createElement("a");
    link.download = `${stallName}_QRCode.png`;
    link.href = qrCode;
    link.click();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/halls")}>
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <h1 className="text-2xl font-bold">My Bookings</h1>
          </div>
          <Button variant="ghost" onClick={signOut}>
            Sign Out
          </Button>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
        {bookings.length === 0 ? (
          <Card className="text-center py-12">
            <CardContent>
              <p className="text-muted-foreground mb-4">You haven't made any bookings yet.</p>
              <Button onClick={() => navigate("/halls")}>Browse Halls</Button>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {bookings.map((booking) => (
              <Card key={booking.id}>
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span>{booking.hall.name}</span>
                    <Badge
                      variant={booking.status === "CONFIRMED" ? "default" : "secondary"}
                    >
                      {booking.status}
                    </Badge>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Stall:</span>
                      <span className="font-medium">{booking.stall.name}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Size:</span>
                      <Badge variant="outline">{booking.stall.size}</Badge>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Price:</span>
                      <span className="font-medium">LKR {Number(booking.stall.price).toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Booked on:</span>
                      <span>{new Date(booking.created_at).toLocaleDateString()}</span>
                    </div>
                  </div>

                  {booking.qr_code && (
                    <div className="space-y-2">
                      <img 
                        src={booking.qr_code} 
                        alt="QR Code" 
                        className="w-full h-auto border rounded"
                      />
                      <Button
                        variant="outline"
                        className="w-full"
                        onClick={() => downloadQRCode(booking.qr_code!, booking.stall.name)}
                      >
                        <Download className="w-4 h-4 mr-2" />
                        Download QR Code
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;
