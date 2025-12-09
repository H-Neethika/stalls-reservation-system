import { useEffect, useMemo, useRef, useState } from "react";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import {
  Loader2,
  FileDown,
  Users,
  Layers,
  CircleDollarSign,
} from "lucide-react";
import html2canvas from "html2canvas";
import { jsPDF } from "jspdf";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";
import { exhibitionService } from "@/services/exhibitionService";
import { reservationService } from "@/services/reservationService";
import { apiService } from "@/services/api";
import type { Exhibition, User } from "@/types";

interface ReservedStall {
  id?: number;
  price?: number;
  stallType?: string;
  hallName?: string;
  stallName?: string;
  bookingStatus?: string;
  genres?: string[];
}

interface ReservationRecord {
  id?: number;
  exhibitionId?: number;
  userId?: number;
  totalAmount?: number;
  createdAt?: string;
  status?: string;
  stalls?: ReservedStall[];
  vendor?: Partial<User> | null;
}

const statusVariant = (status?: string) => {
  if (!status) return "secondary";
  const normalized = status.toUpperCase();
  if (normalized === "CONFIRMED") return "default";
  if (normalized === "FAILED") return "destructive";
  if (normalized === "PENDING_PAYMENT" || normalized === "PENDING")
    return "secondary";
  return "secondary";
};

const isStallReserved = (status?: string) => {
  const normalized = (status || "").toUpperCase();
  return (
    normalized === "RESERVED" ||
    normalized === "BOOKED" ||
    normalized === "CONFIRMED"
  );
};

const ExhibitionReservations = () => {
  const { user } = useAuth();
  const { toast } = useToast();

  const [exhibitions, setExhibitions] = useState<Exhibition[]>([]);
  const [selectedExhibitionId, setSelectedExhibitionId] = useState("");

  const [reservations, setReservations] = useState<ReservationRecord[]>([]);
  const [loadingExhibitions, setLoadingExhibitions] = useState(true);
  const [loadingReservations, setLoadingReservations] = useState(false);

  const vendorCache = useRef<Record<string, Partial<User> | null>>({});
  const exportRef = useRef<HTMLDivElement>(null);

  const [exporting, setExporting] = useState(false);

  // -----------------------------------------------------
  // 1. Load all exhibitions
  // -----------------------------------------------------
  useEffect(() => {
    if (!user?.id) {
      setLoadingExhibitions(false);
      return;
    }

    const loadExpos = async () => {
      setLoadingExhibitions(true);

      try {
        const data = await exhibitionService.getExhibitionsByOrganizer(user.id);
        const list = Array.isArray(data) ? data : [];
        setExhibitions(list);

        if (list.length > 0) {
          setSelectedExhibitionId(String(list[0].id));
        }
      } finally {
        setLoadingExhibitions(false);
      }
    };

    loadExpos();
  }, [user]);

  // -----------------------------------------------------
  // 2. Load reservations
  // -----------------------------------------------------
  useEffect(() => {
    if (!selectedExhibitionId) return;

    const loadReservations = async () => {
      setLoadingReservations(true);

      try {
        const data = await reservationService.getAllReservations();
        const list = Array.isArray(data) ? data : [];

        const filtered = list.filter(
          (res) =>
            String(res.exhibitionId) === selectedExhibitionId &&
            (res.status || "").toUpperCase() === "CONFIRMED"
        );

        const vendorIds = Array.from(
          new Set(
            filtered.map((r) => r.userId).filter((id) => typeof id === "number")
          )
        );

        const missingIds = vendorIds.filter(
          (id) => vendorCache.current[String(id)] === undefined
        );

        if (missingIds.length > 0) {
          const vendorData = await Promise.all(
            missingIds.map(async (id) => {
              try {
                const vendor = await apiService.getUserById(id);
                return { id, vendor };
              } catch {
                return { id, vendor: null };
              }
            })
          );

          vendorData.forEach(({ id, vendor }) => {
            vendorCache.current[String(id)] = vendor;
          });
        }

        const hydrated = filtered
          .map((res) => ({
            ...res,
            stalls:
              res.stalls?.filter((s) => isStallReserved(s.bookingStatus)) || [],
            vendor: vendorCache.current[String(res.userId)] || null,
          }))
          .filter((res) => res.stalls.length > 0);

        setReservations(hydrated);
      } finally {
        setLoadingReservations(false);
      }
    };

    loadReservations();
  }, [selectedExhibitionId]);

  // -----------------------------------------------------
  // Summary
  // -----------------------------------------------------
  const summary = useMemo(
    () => ({
      total: reservations.length,
      confirmed: reservations.filter(
        (r) => (r.status || "").toUpperCase() === "CONFIRMED"
      ).length,
      stalls: reservations.reduce((sum, r) => sum + (r.stalls?.length ?? 0), 0),
      revenue: reservations.reduce((sum, r) => sum + (r.totalAmount ?? 0), 0),
    }),
    [reservations]
  );

  // -----------------------------------------------------
  // 3. PDF Export (with SVG → PNG conversion)
  // -----------------------------------------------------
  const handleExport = async () => {
    if (!exportRef.current || reservations.length === 0) {
      toast({
        title: "Nothing to export",
        description: "No reservation data available.",
      });
      return;
    }

    setExporting(true);

    try {
      const svgNodes = exportRef.current.querySelectorAll("svg");

      await Promise.all(
        Array.from(svgNodes).map(async (svg) => {
          const xml = new XMLSerializer().serializeToString(svg);
          const svg64 = btoa(xml);

          const img = document.createElement("img");
          img.src = `data:image/svg+xml;base64,${svg64}`;
          img.style.width =
            svg.style.width || svg.getAttribute("width") || "100%";
          img.style.height =
            svg.style.height || svg.getAttribute("height") || "100%";

          svg.replaceWith(img);
        })
      );

      const canvas = await html2canvas(exportRef.current, { scale: 2 });
      const imgData = canvas.toDataURL("image/png");

      const pdf = new jsPDF({
        orientation: canvas.width > canvas.height ? "landscape" : "portrait",
        unit: "px",
        format: [canvas.width, canvas.height],
      });

      pdf.addImage(imgData, "PNG", 0, 0, canvas.width, canvas.height);
      pdf.save(`exhibition-${selectedExhibitionId}-reservations.pdf`);
    } catch (e) {
      toast({
        title: "Export failed",
        description: "PDF generation could not complete.",
        variant: "destructive",
      });
    } finally {
      setExporting(false);
    }
  };

  const selectedExhibition = exhibitions.find(
    (e) => String(e.id) === selectedExhibitionId
  );

  // -----------------------------------------------------
  // Render
  // -----------------------------------------------------
  return (
    <OrganizerLayout title="Exhibition Reservations">
      <div className="space-y-6">
        {/* Exhibition Selector */}
        <Card>
          <CardHeader className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <CardTitle className="text-2xl">
                Reservation details by exhibition
              </CardTitle>
              <p className="text-sm text-muted-foreground">
                Pick an exhibition to view vendor reservations and download a
                PDF.
              </p>
            </div>

            <div className="flex items-center gap-3">
              <Select
                value={selectedExhibitionId}
                onValueChange={setSelectedExhibitionId}
                disabled={loadingExhibitions}
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Select exhibition" />
                </SelectTrigger>
                <SelectContent>
                  {exhibitions.map((expo) => (
                    <SelectItem key={expo.id} value={String(expo.id)}>
                      {expo.exhibitionName || `Exhibition ${expo.id}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Button
                onClick={handleExport}
                disabled={exporting || reservations.length === 0}
                variant="outline"
              >
                {exporting ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <FileDown className="h-4 w-4" />
                )}
                <span className="ml-2">Export PDF</span>
              </Button>
            </div>
          </CardHeader>

          <CardContent>
            {selectedExhibition && (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {/* Summary */}
                <div className="rounded-lg border p-4 bg-muted/40">
                  <div className="flex items-center gap-2 text-xs uppercase text-muted-foreground">
                    <Users className="h-4 w-4" /> Total reservations
                  </div>
                  <div className="mt-2 text-2xl font-semibold">
                    {summary.total}
                  </div>
                </div>

                <div className="rounded-lg border p-4 bg-muted/40">
                  <Badge variant="default" className="h-5 text-[11px]">
                    Confirmed
                  </Badge>
                  <div className="mt-2 text-2xl font-semibold">
                    {summary.confirmed}
                  </div>
                </div>

                <div className="rounded-lg border p-4 bg-muted/40">
                  <div className="flex items-center gap-2 text-xs uppercase text-muted-foreground">
                    <Layers className="h-4 w-4" /> Stalls reserved
                  </div>
                  <div className="mt-2 text-2xl font-semibold">
                    {summary.stalls}
                  </div>
                </div>

                <div className="rounded-lg border p-4 bg-muted/40">
                  <div className="flex items-center gap-2 text-xs uppercase text-muted-foreground">
                    <CircleDollarSign className="h-4 w-4" /> Total Amount
                  </div>
                  <div className="mt-2 text-2xl font-semibold">
                    LKR {Number(summary.revenue).toLocaleString()}
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* ------------------------------------ */}
        {/* PDF CONTENT */}
        {/* ------------------------------------ */}
        <Card ref={exportRef}>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                {selectedExhibition?.exhibitionName || "Reservations"}
              </CardTitle>

              {selectedExhibition && (
                <Badge variant="outline">
                  Exhibition ID: {selectedExhibition.id}
                </Badge>
              )}
            </div>
          </CardHeader>

          <CardContent className="space-y-4">
            {/* Dates */}
            {selectedExhibition && (
              <div className="text-sm">
                <div className="font-semibold text-foreground">
                  {selectedExhibition.exhibitionName}
                </div>
                <div className="text-xs text-muted-foreground">
                  {selectedExhibition.startDateTime
                    ? `From ${new Date(
                        selectedExhibition.startDateTime
                      ).toLocaleDateString()}`
                    : ""}
                  {selectedExhibition.endDateTime
                    ? ` • To ${new Date(
                        selectedExhibition.endDateTime
                      ).toLocaleDateString()}`
                    : ""}
                </div>
              </div>
            )}

            {/* Reservation Table */}
            {loadingReservations ? (
              <div className="flex h-32 items-center justify-center text-muted-foreground">
                <Loader2 className="h-5 w-5 animate-spin" />
              </div>
            ) : reservations.length === 0 ? (
              <div className="rounded border border-dashed p-6 text-center text-sm text-muted-foreground">
                No reservations for this exhibition yet.
              </div>
            ) : (
              <div className="overflow-x-auto rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Vendor</TableHead>
                      <TableHead>Organization</TableHead>
                      <TableHead>Reservation</TableHead>
                      <TableHead>Stalls</TableHead>
                      <TableHead>Genres</TableHead>
                      <TableHead className="text-right">Total (LKR)</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {reservations.map((res) => (
                      <TableRow key={res.id}>
                        <TableCell>
                          <div className="font-semibold">
                            {res.vendor?.name}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {res.vendor?.email}
                          </div>
                        </TableCell>

                        <TableCell>
                          {res.vendor?.organizationName || "N/A"}
                        </TableCell>

                        <TableCell>
                          #{res.id}
                          <div className="text-xs text-muted-foreground">
                            Exhibition {res.exhibitionId}
                          </div>
                        </TableCell>

                        <TableCell>
                          {res.stalls?.map((stall) => (
                            <div key={stall.id}>
                              <span className="font-medium">
                                {stall.stallName || "Stall"}
                              </span>
                              <span className="text-xs text-muted-foreground ml-2">
                                ID: {stall.id}
                              </span>
                              {stall.stallType && (
                                <Badge
                                  variant="outline"
                                  className="ml-2 text-[11px]"
                                >
                                  {stall.stallType}
                                </Badge>
                              )}
                            </div>
                          ))}
                        </TableCell>

                        <TableCell>
                          {res.stalls?.map((stall) => (
                            <div key={stall.id} className="mb-1">
                              {stall.genres && stall.genres.length > 0 ? (
                                <div className="flex flex-wrap gap-1">
                                  {stall.genres.map((g) => (
                                    <Badge
                                      key={g}
                                      variant="outline"
                                      className="text-[10px] px-1 py-0.5 bg-primary/10 text-primary"
                                    >
                                      {g}
                                    </Badge>
                                  ))}
                                </div>
                              ) : (
                                <span className="text-muted-foreground text-xs">
                                  No genres
                                </span>
                              )}
                            </div>
                          ))}
                        </TableCell>

                        <TableCell className="text-right font-semibold">
                          {Number(res.totalAmount).toLocaleString()}
                        </TableCell>

                        <TableCell className="text-xs text-muted-foreground">
                          {res.createdAt
                            ? new Date(res.createdAt).toLocaleDateString()
                            : "--"}
                        </TableCell>

                        <TableCell>
                          <Badge variant={statusVariant(res.status)}>
                            {res.status?.replace(/_/g, " ")}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </OrganizerLayout>
  );
};

export default ExhibitionReservations;
