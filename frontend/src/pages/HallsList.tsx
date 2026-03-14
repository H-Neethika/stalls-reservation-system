import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall as MockHall } from "@/lib/mockData";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Building2, Calendar, Loader2, Map, Grid3x3, ArrowLeft } from "lucide-react";
import { useAuth } from "@/hooks/use-auth";
import { useToast } from "@/hooks/use-toast";
import { exhibitionService } from "@/services/exhibitionService";

interface Hall extends MockHall {
  available_stalls: number;
  total_stalls: number;
}

type PublishedExhibition = {
  id: string;
  title: string;
  description: string;
  dateRange: string;
  location: string;
  hallIds: string[];
  status: "PUBLISHED" | "DRAFT";
  halls?: any[];
};

const HallsList = () => {
  const navigate = useNavigate();
  const { signOut } = useAuth();
  const { toast } = useToast();
  const [halls, setHalls] = useState<Hall[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedExhibition, setSelectedExhibition] = useState<PublishedExhibition | null>(null);
  const [publishedExhibitions, setPublishedExhibitions] = useState<PublishedExhibition[]>([]);
  const [loadingExhibitions, setLoadingExhibitions] = useState(true);

  useEffect(() => {
    fetchHalls();
  }, []);

  const fetchHalls = async () => {
    try {
      const hallsData = await mockApi.getHalls();
      const allStalls = await mockApi.getStalls();

      const hallsWithCounts: Hall[] = hallsData.map((hall) => {
        const hallStalls = allStalls.filter((s) => s.hall_id === hall.id);
        const availableStalls = hallStalls.filter((s) => !s.is_reserved);

        return {
          ...hall,
          total_stalls: hallStalls.length,
          available_stalls: availableStalls.length,
        };
      });

      setHalls(hallsWithCounts);
    } catch (error: any) {
      toast({
        title: "Failed to Load Hall",
        description: "Unable to load hall details. Please refresh the page.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const fetchExhibitions = async () => {
      try {
        const data = await exhibitionService.getPublishedExhibitions();
        console.log("data : ",data);
      const mapped =
  data?.map((expo) => ({
    id: String(expo.id),
    title: expo.exhibitionName || "Exhibition",
    description: expo.description || "Explore the latest offerings.",

    // Exhibition start & end
    exhibitionStart: expo.startDateTime ? new Date(expo.startDateTime) : null,
    exhibitionEnd: expo.endDateTime ? new Date(expo.endDateTime) : null,

    // Booking open & close
    bookingOpen: expo.bookingOpenDateTime ? new Date(expo.bookingOpenDateTime) : null,
    bookingClose: expo.bookingCloseDateTime ? new Date(expo.bookingCloseDateTime) : null,

    // Hall IDs
    hallIds: Array.isArray(expo.halls)
      ? expo.halls.map((h) => String(h.id))
      : [],

    halls: expo.halls || [],
    status: expo.exhibitionState || "PUBLISHED",
  })) || [];


        if (mapped.length === 0) {
          throw new Error("No published exhibitions returned");
        }
        setPublishedExhibitions(mapped);
      } catch (error: any) {
  setPublishedExhibitions([]); // ← no fallback
  // toast({
  //   title: "No exhibitions available",
  //   description:
  //     typeof error?.message === "string"
  //       ? error.message
  //       : "Unable to load exhibitions at the moment.",
  // });
}
 finally {
        setLoadingExhibitions(false);
      }
    };

    fetchExhibitions();
  }, [toast, halls]);

  const hallsById = useMemo(
    () => halls.reduce<Record<string, Hall>>((acc, hall) => ({ ...acc, [hall.id]: hall }), {}),
    [halls],
  );

  const hallsForSelectedExhibition = useMemo(() => {
    if (!selectedExhibition) return [];
    return selectedExhibition.hallIds
      .map((id) => hallsById[id])
      .filter(Boolean)
      .map((hall) => hall as Hall);
  }, [selectedExhibition, hallsById]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  const renderExhibitions = () => {
    if (loadingExhibitions) {
      return (
        <Card className="text-center py-12">
          <CardContent className="text-muted-foreground">Loading exhibitions...</CardContent>
        </Card>
      );
    }

if (publishedExhibitions.length === 0) {
  return (
    <Card className="text-center py-12">
      <CardContent>
        <Map className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />


        <h3 className="text-xl font-semibold mb-2">No Exhibitions Available</h3>

        <p className="text-muted-foreground">
          There are currently no exhibitions published for booking.
        </p>

        <p className="text-sm text-muted-foreground mt-2">
          Please check back later or contact the organizer for updates.
        </p>
      </CardContent>
    </Card>
  );
}



    return (
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        {publishedExhibitions
  .filter((expo) => expo.status === "PUBLISHED")
  .map((expo) => {
    console.log("Expo:", expo);  // <-- LOG HERE

    return (
     <Card
  key={expo.id}
  className="hover-scale cursor-pointer"
  onClick={() =>
    navigate(`/exhibitions/${expo.id}/reserve`, {
      state: { exhibition: expo },
    })
  }
>
  <CardHeader>
    <CardTitle className="flex items-center gap-2">
      <Building2 className="w-5 h-5" />
      {expo.title}
    </CardTitle>
    <CardDescription>{expo.description}</CardDescription>
  </CardHeader>

  <CardContent className="space-y-4 text-sm text-muted-foreground">

    {/* Exhibition Dates */}
    <div className="flex items-start gap-2">
      <Calendar className="w-4 h-4 mt-1" />
      <div>
        <span className="font-medium text-foreground">Exhibition:</span>
        <p>
          {expo.exhibitionStart?.toLocaleDateString()} →{" "}
          {expo.exhibitionEnd?.toLocaleDateString()}
        </p>
      </div>
    </div>

    {/* Booking Opens */}
    <div className="flex items-start gap-2">
      <Calendar className="w-4 h-4 mt-1" />
      <div>
        <span className="font-medium text-foreground">Booking Opens:</span>
        <p>
          {expo.bookingOpen
            ? `${expo.bookingOpen.toLocaleDateString()} at ${expo.bookingOpen.toLocaleTimeString()}`
            : "TBA"}
        </p>
      </div>
    </div>

    {/* Booking Closes */}
    <div className="flex items-start gap-2">
      <Calendar className="w-4 h-4 mt-1" />
      <div>
        <span className="font-medium text-foreground">Booking Closes:</span>
        <p>
          {expo.bookingClose
            ? `${expo.bookingClose.toLocaleDateString()} at ${expo.bookingClose.toLocaleTimeString()}`
            : "TBA"}
        </p>
      </div>
    </div>

    {/* Hall count */}
    <Badge variant="secondary" className="mt-1">
      {expo.hallIds.length} hall{expo.hallIds.length === 1 ? "" : "s"}
    </Badge>

    <Button className="w-full mt-2">Select Exhibition</Button>
  </CardContent>
</Card>

    );
  })}

      </div>
    );
  };

  const renderHallsForExhibition = () => {
    if (!selectedExhibition) return null;

    if (hallsForSelectedExhibition.length === 0) {
      return (
        <Card className="text-center py-12">
          <CardContent>
            <Grid3x3 className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground">No halls available for this exhibition.</p>
          </CardContent>
        </Card>
      );
    }

    return (
      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {hallsForSelectedExhibition.map((hall) => (
              <Card
                key={hall.id}
                className="hover-scale cursor-pointer"
                onClick={() =>
                  navigate(`/exhibitions/${selectedExhibition?.id}/reserve`, {
                    state: { exhibition: selectedExhibition },
                  })
                }
              >
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building2 className="w-5 h-5" />
                {hall.name}
              </CardTitle>
              <CardDescription>{hall.description}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Layout:</span>
                  <span className="font-medium">
                    {hall.rows} x {hall.columns}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Total Stalls:</span>
                  <span className="font-medium">{hall.total_stalls}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Available:</span>
                  <span className="font-medium text-green-600">{hall.available_stalls}</span>
                </div>
              </div>
              <Button className="w-full mt-4">Open Hall Map</Button>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            {selectedExhibition && (
              <Button variant="ghost" size="icon" onClick={() => setSelectedExhibition(null)}>
                <ArrowLeft className="w-5 h-5" />
              </Button>
            )}
            <h1 className="text-2xl font-bold">
              {selectedExhibition ? selectedExhibition.title : "Colombo International Book Fair"}
            </h1>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => navigate("/my-bookings")}>
              My Bookings
            </Button>
            <Button variant="ghost" onClick={signOut}>
              Sign Out
            </Button>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-12">
        <div className="text-center mb-12">
          <Building2 className="w-16 h-16 mx-auto mb-4 text-primary" />
          <h2 className="text-4xl font-bold mb-4">
            {selectedExhibition ? "Choose a Hall" : "Available Exhibitions"}
          </h2>
          <p className="text-muted-foreground text-lg">
            {selectedExhibition
              ? "Select a hall to view the map, pick up to 3 stalls, and proceed to payment."
              : "Select an exhibition to view and reserve available stalls."}
          </p>
        </div>

        {selectedExhibition ? renderHallsForExhibition() : renderExhibitions()}
      </div>
    </div>
  );
};

export default HallsList;
