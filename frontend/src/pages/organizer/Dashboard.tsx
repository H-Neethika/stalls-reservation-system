import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";
import { hallService } from "@/services/hallService";
import { exhibitionService } from "@/services/exhibitionService";
import { reservationService } from "@/services/reservationService";
import {
  Building2,
  CalendarClock,
  CircleDollarSign,
  Layers,
  ListChecks,
  LucideIcon,
  Sparkles,
  Users,
} from "lucide-react";
import type { Exhibition } from "@/types";

type ReservedStall = {
  id?: number;
  stallName?: string;
  hallName?: string;
  stallType?: string;
  price?: number;
  bookingStatus?: string;
};

type ReservationRecord = {
  id?: number;
  exhibitionId?: number;
  userId?: number;
  totalAmount?: number;
  createdAt?: string;
  status?: string;
  stalls?: ReservedStall[];
};

const isStallReserved = (status?: string) => {
  const normalized = (status || "").toUpperCase();
  return normalized === "RESERVED" || normalized === "BOOKED" || normalized === "CONFIRMED";
};

const statusBadgeVariant = (status?: string) => {
  const normalized = (status || "").toUpperCase();
  if (normalized === "CONFIRMED") return "default";
  if (normalized === "FAILED") return "destructive";
  return "secondary";
};

const StatCard = ({
  title,
  value,
  icon: Icon,
  hint,
}: {
  title: string;
  value: string;
  icon: LucideIcon;
  hint?: string;
}) => (
  <Card>
    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
      <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
      <Icon className="h-4 w-4 text-primary" />
    </CardHeader>
    <CardContent>
      <div className="text-2xl font-bold">{value}</div>
      {hint && <p className="text-xs text-muted-foreground mt-1">{hint}</p>}
    </CardContent>
  </Card>
);

const OrganizerDashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();

  const [loading, setLoading] = useState(true);
  const [exhibitions, setExhibitions] = useState<Exhibition[]>([]);
  const [reservations, setReservations] = useState<ReservationRecord[]>([]);
  const [hallCount, setHallCount] = useState(0);

  useEffect(() => {
    const load = async () => {
      if (!user?.id) {
        setLoading(false);
        return;
      }

      setLoading(true);
      try {
        const [expoData, hallData, resData] = await Promise.all([
          exhibitionService.getExhibitionsByOrganizer(user.id),
          hallService.getHalls(),
          reservationService.getAllReservations(),
        ]);

        const expoList = Array.isArray(expoData) ? expoData : [];
        const expoIdSet = new Set(expoList.map((expo) => Number(expo.id)));
        setExhibitions(expoList);
        setHallCount(Array.isArray(hallData) ? hallData.length : 0);

        const filteredReservations = (Array.isArray(resData) ? resData : []).filter((res) =>
          expoIdSet.has(Number(res.exhibitionId)),
        ) as ReservationRecord[];

        const normalizedReservations = filteredReservations
          .filter((res) => (res.status || "").toUpperCase() === "CONFIRMED")
          .map((res) => ({
            ...res,
            stalls: res.stalls?.filter((stall) => isStallReserved(stall.bookingStatus)),
          }))
          .filter((res) => res.stalls && res.stalls.length > 0);

        setReservations(normalizedReservations);
      } catch (error) {
        const message = error instanceof Error ? error.message : "Failed to load dashboard data.";
        toast({
          title: "Dashboard unavailable",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [toast, user]);

  const stats = useMemo(() => {
    const reservedStalls = reservations.reduce((sum, res) => sum + (res.stalls?.length ?? 0), 0);
    const revenue = reservations.reduce((sum, res) => sum + (res.totalAmount ?? 0), 0);
    const vendors = new Set(
      reservations
        .map((res) => res.userId)
        .filter((id): id is number => typeof id === "number"),
    ).size;
    const published = exhibitions.filter(
      (expo) =>
        (expo.exhibitionState || expo.status || "").toUpperCase() === "PUBLISHED" ||
        (expo.status || "").toUpperCase() === "ACTIVE",
    ).length;

    return {
      exhibitions: exhibitions.length,
      published,
      halls: hallCount,
      reservations: reservations.length,
      reservedStalls,
      revenue,
      vendors,
    };
  }, [exhibitions, hallCount, reservations]);

  const recentReservations = useMemo(
    () =>
      [...reservations]
        .sort((a, b) => {
          const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return bTime - aTime;
        })
        .slice(0, 5),
    [reservations],
  );

  const upcomingExhibitions = useMemo(() => {
    const now = Date.now();
    return [...exhibitions]
      .filter((expo) => (expo.startDateTime ? new Date(expo.startDateTime).getTime() : 0) > now)
      .sort(
        (a, b) =>
          (a.startDateTime ? new Date(a.startDateTime).getTime() : 0) -
          (b.startDateTime ? new Date(b.startDateTime).getTime() : 0),
      )
      .slice(0, 4);
  }, [exhibitions]);

  return (
    <OrganizerLayout title="Dashboard">
      <div className="space-y-8">
        <div className="flex flex-col gap-3 rounded-xl border bg-card/80 p-6 shadow-sm md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm text-muted-foreground">Welcome back</p>
            <h2 className="text-3xl font-bold">
              {user?.name ? `${user.name}'s overview` : "Organizer overview"}
            </h2>
            <p className="text-sm text-muted-foreground mt-1">
              Monitor reservations, exhibitions, and hall utilization at a glance.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button variant="outline" onClick={() => navigate("/organizer/exhibitions")}>
              <Sparkles className="mr-2 h-4 w-4" /> Manage exhibitions
            </Button>
            <Button variant="outline" onClick={() => navigate("/organizer/halls")}>
              <Building2 className="mr-2 h-4 w-4" /> Manage halls
            </Button>
            <Button onClick={() => navigate("/organizer/exhibition-reservations")}>
              <ListChecks className="mr-2 h-4 w-4" /> View reservations
            </Button>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard title="Exhibitions" value={String(stats.exhibitions)} icon={CalendarClock} hint="Total created" />
          <StatCard
            title="Published / Active"
            value={String(stats.published)}
            icon={Sparkles}
            hint="Currently visible to vendors"
          />
          <StatCard title="Halls" value={String(stats.halls)} icon={Building2} hint="Total halls in your org" />
          <StatCard
            title="Reserved stalls"
            value={String(stats.reservedStalls)}
            icon={Layers}
            hint="Across confirmed reservations"
          />
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <div className="md:col-span-2 space-y-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Recent reservations</CardTitle>
                <Button variant="ghost" size="sm" onClick={() => navigate("/organizer/exhibition-reservations")}>
                  See all
                </Button>
              </CardHeader>
              <CardContent>
                {recentReservations.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No reservations yet for your exhibitions.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Reservation</TableHead>
                          <TableHead>Exhibition</TableHead>
                          <TableHead>Stalls</TableHead>
                          <TableHead className="text-right">Total (LKR)</TableHead>
                          <TableHead>Status</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {recentReservations.map((res) => {
                          const expoName =
                            exhibitions.find((expo) => String(expo.id) === String(res.exhibitionId))
                              ?.exhibitionName || `#${res.exhibitionId ?? "N/A"}`;
                          return (
                            <TableRow key={res.id}>
                              <TableCell className="font-medium">#{res.id ?? "--"}</TableCell>
                              <TableCell className="text-sm">{expoName}</TableCell>
                              <TableCell className="text-sm">{res.stalls?.length ?? 0}</TableCell>
                              <TableCell className="text-right font-semibold">
                                {Number(res.totalAmount ?? 0).toLocaleString()}
                              </TableCell>
                              <TableCell>
                                <Badge variant={statusBadgeVariant(res.status)}>
                                  {res.status ? res.status.replace(/_/g, " ") : "UNKNOWN"}
                                </Badge>
                              </TableCell>
                            </TableRow>
                          );
                        })}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </CardContent>
            </Card>

            <div className="grid gap-4 sm:grid-cols-2">
              <StatCard
                title="Confirmed revenue"
                value={`LKR ${Number(stats.revenue).toLocaleString()}`}
                icon={CircleDollarSign}
                hint="Confirmed reservations only"
              />
              <StatCard
                title="Unique vendors"
                value={String(stats.vendors)}
                icon={Users}
                hint="Vendors with at least one reservation"
              />
            </div>
          </div>

          <Card className="md:col-span-1 h-full">
            <CardHeader>
              <CardTitle>Upcoming exhibitions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {upcomingExhibitions.length === 0 ? (
                <p className="text-sm text-muted-foreground">No upcoming exhibitions scheduled.</p>
              ) : (
                upcomingExhibitions.map((expo) => (
                  <div key={expo.id} className="rounded-md border p-3">
                    <div className="flex items-center justify-between gap-2">
                      <div className="font-semibold leading-tight">
                        {expo.exhibitionName || `Exhibition ${expo.id}`}
                      </div>
                      {(expo.exhibitionState || expo.status) && (
                        <Badge variant="outline" className="text-[11px]">
                          {(expo.exhibitionState || expo.status || "").replace(/_/g, " ")}
                        </Badge>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      {expo.startDateTime
                        ? new Date(expo.startDateTime).toLocaleDateString()
                        : "Start date not set"}
                      {" \u2022 "}
                      {expo.endDateTime
                        ? new Date(expo.endDateTime).toLocaleDateString()
                        : "End date not set"}
                    </p>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </OrganizerLayout>
  );
};

export default OrganizerDashboard;
