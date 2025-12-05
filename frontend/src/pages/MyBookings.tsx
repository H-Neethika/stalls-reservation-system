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
import { genreService } from "@/services/genreService";

import {
  Dialog,
  DialogHeader,
  DialogContent,
  DialogFooter,
  DialogTitle,
} from "@/components/ui/dialog";

import { Input } from "@/components/ui/input";

interface ReservedStall {
  id: number;
  displayName?: string;
  hallName?: string;
  stallName?: string;
  stallType?: string;
  price?: number;
  bookingStatus?: string;
  reservationId?: number;
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
  const [exhibitions, setExhibitions] = useState<
    Record<number, Exhibition | null>
  >({});

  const [genreModalOpen, setGenreModalOpen] = useState(false);
  const [selectedStall, setSelectedStall] = useState<ReservedStall | null>(
    null
  );
  const [genreName, setGenreName] = useState("");
  const [selectedGenre, setSelectedGenre] = useState<any>(null);

  const openGenreModal = (
    stall: ReservedStall,
    reservationId: number,
    existingGenre: any = null
  ) => {
    setSelectedStall({ ...stall, reservationId });

    if (existingGenre) {
      // EDIT MODE
      setSelectedGenre(existingGenre);
      setGenreName(existingGenre.name);
    } else {
      // ADD MODE
      setSelectedGenre(null);
      setGenreName("");
    }

    setGenreModalOpen(true);
  };

  const handleSaveGenre = async () => {
    if (!genreName.trim() || !selectedStall) return;

    try {
      if (selectedGenre) {
        // EDIT
        await genreService.updateGenre(selectedGenre.id, {
          name: genreName,
        });
      } else {
        // ADD
        await genreService.createGenre({
          name: genreName,
          reservationId: selectedStall.reservationId!,
          stallId: selectedStall.id,
        });
      }

      // refresh list
      const updated = await genreService.getGenresByStall(selectedStall.id);
      setStallGenres((prev) => ({
        ...prev,
        [selectedStall.id]: Array.isArray(updated) ? updated : [updated],
      }));

      toast({
        title: selectedGenre ? "Genre updated" : "Genre added",
        description: `${selectedStall.stallName}`,
      });

      setGenreModalOpen(false);
    } catch (error) {
      toast({
        title: "Failed",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    }
  };

  // 1️⃣ first: state
  const [stallGenres, setStallGenres] = useState<Record<number, string[]>>({});

  // 2️⃣ second: define the function
  const fetchGenresForStalls = async (bookings: ReservationSummary[]) => {
    const map: Record<number, string[]> = {};
    for (const booking of bookings) {
      for (const stall of booking.stalls || []) {
        const data = await genreService.getGenresByStall(stall.id);
        map[stall.id] = !data ? [] : Array.isArray(data) ? data : [data];
      }
    }
    setStallGenres(map);
  };

  const fetchExhibitionDetails = async (reservations: ReservationSummary[]) => {
    const uniqueIds = Array.from(
      new Set(
        reservations
          .map((r) => r.exhibitionId)
          .filter((id): id is number => typeof id === "number")
      )
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
        })
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
      await fetchGenresForStalls(list);
    } catch (error) {
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
      bookings
        .filter((booking) =>
          booking.stalls?.some(
            (stall) => (stall.bookingStatus || "").toUpperCase() === "RESERVED"
          )
        )
        .filter((booking) => booking.status === "CONFIRMED"),
    [bookings]
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
      <Dialog open={genreModalOpen} onOpenChange={setGenreModalOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Genre Name</DialogTitle>
          </DialogHeader>

          <div className="space-y-3 py-2">
            <Input
              placeholder="Enter genre"
              value={genreName}
              onChange={(e) => setGenreName(e.target.value)}
            />
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setGenreModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveGenre}>Save</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
        <nav className="border-b bg-card/50 backdrop-blur">
          <div className="container mx-auto px-4 py-4 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                size="icon"
                onClick={() => navigate("/halls")}
              >
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
                          ? `Booked on ${new Date(
                              booking.createdAt
                            ).toLocaleString()}`
                          : "Booking date unavailable"}
                      </div>
                      <div className="text-xs text-muted-foreground">
                        Exhibition ID: {booking.exhibitionId ?? "N/A"}
                      </div>
                      {exhibition && (
                        <div className="text-xs text-muted-foreground">
                          {exhibition.startDateTime && exhibition.endDateTime
                            ? `${new Date(
                                exhibition.startDateTime
                              ).toLocaleDateString()} - ${new Date(
                                exhibition.endDateTime
                              ).toLocaleDateString()}`
                            : "Dates not available"}
                        </div>
                      )}
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">
                          Total amount
                        </span>
                        <span className="font-semibold">
                          LKR{" "}
                          {Number(booking.totalAmount || 0).toLocaleString()}
                        </span>
                      </div>

                      <div className="space-y-3">
                        <div className="text-sm font-semibold">Stalls</div>
                        {booking.stalls && booking.stalls.length > 0 ? (
                          booking.stalls.map((stall) => (
                            <div
                              key={`${booking.id}-${stall.id}`}
                              className="rounded border px-3 py-3 text-sm space-y-3"
                            >
                              {/* Row 1: Info + Price */}
                              <div className="flex items-start justify-between gap-3">
                                <div>
                                  <div className="font-semibold">
                                    {`Stall ${stall.displayName}` || `Stall ${stall.id}`}
                                  </div>
                                  <div className="text-xs text-muted-foreground">
                                    {stall.hallName || "Hall not available"}
                                    {stall.stallType
                                      ? ` • ${stall.stallType}`
                                      : ""}
                                  </div>
                                </div>

                                <div className="text-right">
                                  <div className="font-semibold">
                                    LKR{" "}
                                    {Number(stall.price || 0).toLocaleString()}
                                  </div>

                                  {stall.bookingStatus && (
                                    <Badge variant="outline" className="mt-1">
                                      {stall.bookingStatus}
                                    </Badge>
                                  )}
                                </div>
                              </div>

                              {/* Row 2: Genres + Add button */}
                              <div className="flex items-center justify-between">
                                {/* LEFT SIDE - List of genres */}
                                <div className="text-xs text-muted-foreground flex gap-2 flex-wrap">
                                  {stallGenres[stall.id] &&
                                  stallGenres[stall.id].length > 0 ? (
                                    stallGenres[stall.id].map((g) => (
                                      <span
                                        key={g.id}
                                        className="px-2 py-1 bg-secondary rounded text-xs"
                                      >
                                        <b>Genre</b>: {g.name}
                                      </span>
                                    ))
                                  ) : (
                                    <span className="text-muted-foreground text-xs">
                                      No genres added
                                    </span>
                                  )}
                                </div>

                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() =>
                                    openGenreModal(
                                      stall,
                                      booking.id,
                                      stallGenres[stall.id]?.[0] || null // pass first genre if exists
                                    )
                                  }
                                >
                                  {stallGenres[stall.id] &&
                                  stallGenres[stall.id].length > 0
                                    ? "Edit Genre"
                                    : "Add Genre"}
                                </Button>
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
    </>
  );
};

export default MyBookings;
