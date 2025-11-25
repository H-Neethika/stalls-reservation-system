import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2, ArrowLeft } from "lucide-react";
import { useAuth } from "@/hooks/use-auth";
import { useToast } from "@/hooks/use-toast";
import { reservationService } from "@/services/reservationService";
import { exhibitionService } from "@/services/exhibitionService";
import { Exhibition } from "@/types";

interface ReservedStall {
  id: number;
  hallName?: string;
  stallName?: string;
  stallType?: string;
  price?: number;
  bookingStatus?: string;
}

interface ReservationSummary {
  id: number;
  exhibitionId?: number;
  createdAt?: string;
  status?: string;
  totalAmount?: number;
  stalls?: ReservedStall[];
}

const statusVariant = (status?: string) => {
  if (!status) return "secondary";
  const normalized = status.toUpperCase();
  if (normalized === "CONFIRMED") return "default";
  if (normalized === "FAILED") return "destructive";
  return "secondary";
};

const MyBookings = () => {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  const [bookings, setBookings] = useState<ReservationSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [exhibitions, setExhibitions] = useState<Record<number, Exhibition | null>>({});

  const fetchExhibitionDetails = async (reservations: ReservationSummary[]) => {
    const uniqueIds = Array.from(
      new Set(
        reservations
          .map((r) => r.exhibitionId)
          .filter((id): id is number => typeof id === "number"),
      ),
    ).filter((id) => exhibitions[id] === undefined);

    if (uniqueIds.length === 0) return;

    try {
      const results = await Promise.all(
        uniqueIds.map(async (id) => {
          try {
            const data = await exhibitionService.getExhibition(String(id));
            return { id, data };
          } catch (error) {
            console.error("Failed to fetch exhibition", id, error);
            return { id, data: null };
          }
        }),
      );

      setExhibitions((prev) => {
        const next = { ...prev };
        results.forEach(({ id, data }) => {
          next[id] = data;
        });
        return next;
      });
    } catch (error) {
      console.error("Unexpected exhibition fetch error", error);
    }
  };

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const reservations = await reservationService.getMyReservations();
      const list = Array.isArray(reservations) ? reservations : [];
      setBookings(list);
      await fetchExhibitionDetails(list);
    } catch (error: any) {
      toast({
        title: "Could not load bookings",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }
    fetchBookings();
  }, [user]);

  const reservedBookings = useMemo(
    () =>
      bookings.filter(
        (booking) =>
          booking.stalls?.some(
            (stall) => (stall.bookingStatus || "").toUpperCase() === "RESERVED",
          ),
      ).filter((booking) => booking.status === "CONFIRMED"),
    [bookings],

  );
  // console.log("Reserved Bookings:", reservedBookings);

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
        {!user ? (
          <Card className="text-center py-12">
            <CardContent>
              <p className="text-muted-foreground mb-4">
                Please sign in to view your bookings.
              </p>
              <Button onClick={() => navigate("/login")}>Go to Login</Button>
            </CardContent>
          </Card>
        ) : reservedBookings.length === 0 ? (
          <Card className="text-center py-12">
            <CardContent>
              <p className="text-muted-foreground mb-4">
                You haven't made any bookings yet.
              </p>
              <Button onClick={() => navigate("/halls")}>Browse Halls</Button>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {reservedBookings.map((booking) => {
              const exhibition = booking.exhibitionId
                ? exhibitions[booking.exhibitionId]
                : undefined;
              return (
                <Card key={booking.id}>
                  <CardHeader className="space-y-2">
                    <div className="flex items-center justify-between">
                      <CardTitle>
                        {exhibition?.exhibitionName
                          ? exhibition.exhibitionName
                          : `Reservation #${booking.id}`}
                      </CardTitle>
                      <Badge variant={statusVariant(booking.status)}>
                        {booking.status || "UNKNOWN"}
                      </Badge>
                    </div>
                    <div className="text-xs text-muted-foreground">
                      {booking.createdAt
                        ? `Booked on ${new Date(booking.createdAt).toLocaleString()}`
                        : "Booking date unavailable"}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      Exhibition ID: {booking.exhibitionId ?? "N/A"}
                    </div>
                    {exhibition && (
                      <div className="text-xs text-muted-foreground">
                        {exhibition.startDateTime && exhibition.endDateTime
                          ? `${new Date(exhibition.startDateTime).toLocaleDateString()} - ${new Date(exhibition.endDateTime).toLocaleDateString()}`
                          : "Dates not available"}
                      </div>
                    )}
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Total amount</span>
                      <span className="font-semibold">
                        LKR {Number(booking.totalAmount || 0).toLocaleString()}
                      </span>
                    </div>

                    <div className="space-y-3">
                      <div className="text-sm font-semibold">Stalls</div>
                      {booking.stalls && booking.stalls.length > 0 ? (
                        booking.stalls.map((stall) => (
                          <div
                            key={`${booking.id}-${stall.id}`}
                            className="rounded border px-3 py-3 text-sm flex items-start justify-between gap-3"
                          >
                            <div>
                              <div className="font-semibold">
                                {stall.stallName || `Stall ${stall.id}`}
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {stall.hallName || "Hall not available"}
                                {stall.stallType ? ` • ${stall.stallType}` : ""}
                              </div>
                            </div>
                            <div className="text-right">
                              <div className="font-semibold">
                                LKR {Number(stall.price || 0).toLocaleString()}
                              </div>
                              {stall.bookingStatus && (
                                <Badge variant="outline" className="mt-1">
                                  {stall.bookingStatus}
                                </Badge>
                              )}
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-muted-foreground">
                          No stall details available for this reservation.
                        </p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;
