import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Collapse } from "antd";
import { Loader2, ArrowLeft, Map, ShoppingCart } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { exhibitionService } from "@/services/exhibitionService";
import { layoutService } from "@/services/layoutService";
import { reservationService } from "@/services/reservationService";
import { connectRealtime } from "@/services/realtimeService";
import StallMap from "./organizer/StallMap";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

interface Price {
  hallId: number;
  stallTypeId: number;
  price: number;
  stallType?: string;
}

interface HallWithPrices {
  id: number | string;
  hallName?: string;
  prices?: Price[];
}

interface ExhibitionPayload {
  id: string | number;
  exhibitionName?: string;
  startDateTime?: string;
  endDateTime?: string;
  bookingOpenDateTime?: string;
  bookingCloseDateTime?: string;
  stallsPerPerson?: number;
  halls?: HallWithPrices[];
}

interface SelectedStall {
  hallId: string;
  stallId: string;
  stallTypeId?: number;
  stallType?: string;
  price: number;
  exhibitionStallId?: number;
}

const MAX_SELECTION = 3;

const VendorExhibitionBooking = () => {
  const { exhibitionId } = useParams<{ exhibitionId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();

  const initialExhibition = (location.state as { exhibition?: ExhibitionPayload } | undefined)?.exhibition;

  const [exhibition, setExhibition] = useState<ExhibitionPayload | null>(initialExhibition || null);
  const [loading, setLoading] = useState(!initialExhibition);
  const [hallLayouts, setHallLayouts] = useState<Record<string, any[]>>({});
  const [loadingHallId, setLoadingHallId] = useState<string | null>(null);
  const [selectedStalls, setSelectedStalls] = useState<SelectedStall[]>([]);
  const [availabilityMap, setAvailabilityMap] = useState<
    Record<string, Record<string, { status?: string; exhibitionStallId?: number }>>
  >({});
  const [showConfirm, setShowConfirm] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [paymentUrl, setPaymentUrl] = useState<string | null>(null);
  const [realtimeDisconnect, setRealtimeDisconnect] = useState<(() => void) | null>(null);

  useEffect(() => {
    const fetchExhibition = async () => {
      if (initialExhibition || !exhibitionId) return;
      try {
        const list = await exhibitionService.getPublishedExhibitions();
        const found = list.find((expo) => String(expo.id) === String(exhibitionId));
        if (found) {
          setExhibition(found as ExhibitionPayload);
        } else {
          toast({
            title: "Exhibition not found",
            description: "Please go back and select another exhibition.",
            variant: "destructive",
          });
        }
      } catch (error: any) {
        toast({
          title: "Failed to load exhibition",
          description: error?.message || "Please try again later.",
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };
    fetchExhibition();
  }, [exhibitionId, initialExhibition, toast]);

  useEffect(() => {
    const fetchAvailability = async () => {
      if (!exhibitionId) return;

      try {
        const data = await layoutService.getExhibitionLayouts(Number(exhibitionId));
        // console.log("Availability Data:", data);

        const map: Record<string, Record<string, { status?: string; exhibitionStallId?: number }>> = {};

        (data || []).forEach((hall) => {
          if (!hall.hallId) return;

          const hallKey = String(hall.hallId);
          map[hallKey] = map[hallKey] || {};

          hall.stalls?.forEach((stall) => {
            map[hallKey][String(stall.stallId)] = {
              status: stall.status,
              exhibitionStallId: stall.exhibitionStallId,
            };
          });
        });

        setAvailabilityMap(map);

      } catch (error: any) {
        toast({
          title: "Failed to load availability",
          description: error?.message || "Stall availability could not be loaded.",
          variant: "destructive",
        });
      }
    };

    fetchAvailability();
  }, [exhibitionId, toast]);

  useEffect(() => {
    if (!exhibitionId) return;
    const sub = connectRealtime(exhibitionId, (msg) => {
      const { hallId, stallId, status } = msg || {};
      if (!hallId || !stallId || !status) return;
      setAvailabilityMap((prev) => {
        const next = { ...prev };
        const hallKey = String(hallId);
        const stallKey = String(stallId);
        next[hallKey] = next[hallKey] || {};
        next[hallKey][stallKey] = {
          ...(next[hallKey][stallKey] || {}),
          status: status.toUpperCase(),
          exhibitionStallId: msg.exhibitionStallId ?? next[hallKey][stallKey]?.exhibitionStallId,
        };
        return next;
      });
    });
    setRealtimeDisconnect(() => sub.disconnect);
    return () => {
      sub.disconnect();
      setRealtimeDisconnect(null);
    };
  }, [exhibitionId]);

  const mergedLayouts = useMemo(() => {
    const result: Record<string, any[]> = {};
    Object.entries(hallLayouts).forEach(([hallId, stalls]) => {
      const availability = availabilityMap[hallId] || {};
      result[hallId] = stalls.map((stall: any) => ({
        ...stall,
        bookingStatus: availability[String(stall.id)]?.status || stall.bookingStatus,
        exhibitionStallId: availability[String(stall.id)]?.exhibitionStallId ?? stall.exhibitionStallId,
      }));
    });
    // console.log("Hall Layouts:", hallLayouts);
    // console.log("Availability Map:", availabilityMap);
    console.log("Merged Layouts:", result);
    return result;
  }, [hallLayouts, availabilityMap]);

  const handleLoadHallLayout = async (hallId: string | number) => {
    if (hallLayouts[String(hallId)]) return;
    try {
      setLoadingHallId(String(hallId));
      const data = await layoutService.getLayoutByHall(Number(hallId));
      const baseStalls = data?.stalls || [];
      setHallLayouts((prev) => ({ ...prev, [String(hallId)]: baseStalls }));
    } catch (error: any) {
      toast({
        title: "Failed to load hall map",
        description: error?.message || "Try again later.",
        variant: "destructive",
      });
      setHallLayouts((prev) => ({ ...prev, [String(hallId)]: [] }));
    } finally {
      setLoadingHallId(null);
    }
  };

  const handleToggleStall = (hallId: string, stall: any) => {
    const stallId = String(stall.id);
    const isSelected = selectedStalls.some((s) => s.stallId === stallId);
    const currentStatus =
      availabilityMap[hallId]?.[stallId]?.status ||
      stall.bookingStatus ||
      stall.status ||
      "AVAILABLE";
    const normalizedStatus = String(currentStatus).toUpperCase();
    if (normalizedStatus !== "AVAILABLE") {
      toast({
        title: "Stall unavailable",
        description: "This stall cannot be selected.",
        variant: "destructive",
      });
      return;
    }

    if (isSelected) {
      setSelectedStalls((prev) => prev.filter((s) => s.stallId !== stallId));
      return;
    }

    if (selectedStalls.length >= MAX_SELECTION) {
      toast({
        title: "Selection limit reached",
        description: `You can select up to ${MAX_SELECTION} stalls.`,
        variant: "destructive",
      });
      return;
    }

    const typeId = Number(stall.stallTypeId) || undefined;
    const hallPrices =
      exhibition?.halls?.find((h) => String(h.id) === String(hallId))?.prices || [];
    const normalizedType = String(stall.stallType || stall.size || "").toUpperCase();
    const priceEntry =
      hallPrices.find((p) => Number(p.stallTypeId) === typeId) ||
      hallPrices.find((p) => String(p.stallType || "").toUpperCase() === normalizedType) ||
      hallPrices[0];
    const price = priceEntry ? Number(priceEntry.price) : 0;

    setSelectedStalls((prev) => [
      ...prev,
      {
        hallId: String(hallId),
        stallId,
        stallTypeId: typeId,
        stallType: stall.stallType || stall.size,
        price,
        exhibitionStallId: availabilityMap[hallId]?.[stallId]?.exhibitionStallId,
      },
    ]);
  };

  const totalPrice = selectedStalls.reduce((sum, s) => sum + Number(s.price || 0), 0);

  const handleProceed = () => {
    if (selectedStalls.length === 0) return;
    setShowConfirm(true);
  };

  const lockSelectedStalls = async (status: "PENDING" | "AVAILABLE") => {
    const stallIds = selectedStalls.map((s) => Number(s.stallId));
    if (stallIds.length === 0) return;
    try {
      await reservationService.updateStallStatus({
        stallIds,
        bookingStatus: status,
      });
    } catch (error: any) {
      console.error("Failed to update stall lock status", error);
      toast({
        title: "Stall lock failed",
        description: error?.message || `Could not set stalls to ${status}.`,
        variant: "destructive",
      });
    }
  };

  const handleConfirmBooking = async () => {
    setBookingLoading(true);
    try {
      const stallIds = selectedStalls.map((s) => Number(s.stallId));
      const reservationData = await reservationService.createReservation({
        exhibitionId: Number(exhibition?.id),
        stallIds,
      });
      const reservationId = reservationData?.reservationId ?? reservationData?.id ?? reservationData;

      const paymentData = await reservationService.createPayment({
        reservationId,
        currency: "LKR",
      });

      const link =
        paymentData?.paymentUrl || paymentData?.url || paymentData?.link || paymentData?.redirectUrl;
      if (!link) {
        throw new Error("Payment link not returned");
      }

      setPaymentUrl(link);
      setShowConfirm(false);
      window.location.href = link;
    } catch (error: any) {
      toast({
        title: "Booking failed",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    } finally {
      setBookingLoading(false);
    }
  };

  // useEffect(() => {
  //   if (showConfirm) {
  //     lockSelectedStalls("PENDING");
  //   } else if (!bookingLoading && !paymentUrl) {
  //     // only unlock if user closed the dialog without proceeding
  //     lockSelectedStalls("AVAILABLE");
  //   }
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, [showConfirm]);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!exhibition) {
    return (
      <div className="flex min-h-screen items-center justify-center text-muted-foreground">
        Exhibition not found.
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <div className="border-b bg-card/60 backdrop-blur sticky top-0 z-30">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
              <ArrowLeft className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-2xl font-bold">{exhibition.exhibitionName || "Exhibition"}</h1>
              <p className="text-sm text-muted-foreground">
                {exhibition.startDateTime && exhibition.endDateTime
                  ? `${new Date(exhibition.startDateTime).toLocaleDateString()} - ${new Date(
                    exhibition.endDateTime,
                  ).toLocaleDateString()}`
                  : "Dates TBA"}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8 grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          <Card className="border-dashed sticky top-24 z-20">
            <CardContent className="flex flex-wrap gap-4 items-center py-4 text-sm text-muted-foreground">
              <span className="font-semibold text-foreground">Status</span>
              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full bg-white border" />
                <span>Available</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full bg-[#ea9c0cff]" />
                <span>Pending</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full bg-[#e00707ff]" />
                <span>Reserved</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full bg-[#046528ff]" />
                <span>Selected</span>
              </div>
            </CardContent>
          </Card>

          <Collapse
            accordion={false}
            bordered={false}
            items={
              exhibition.halls?.map((hall) => ({
                key: String(hall.id),
                label: (
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Map className="h-4 w-4 text-primary" />
                      <span className="font-semibold">{hall.hallName || `Hall ${hall.id}`}</span>
                    </div>
                    <div className="flex flex-wrap gap-2 text-xs">
                      {hall.prices?.map((p) => (
                        <Badge key={p.id} variant="secondary">
                          {p.stallType || `Type ${p.stallTypeId}`}: LKR {Number(p.price).toLocaleString()}
                        </Badge>
                      ))}
                    </div>
                  </div>
                ),
                children: (
                  <div className="rounded-lg border bg-card p-4">
                    {loadingHallId === String(hall.id) ? (
                      <div className="flex h-40 items-center justify-center text-muted-foreground">
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Loading hall map...
                      </div>
                    ) : mergedLayouts[String(hall.id)] ? (
                      mergedLayouts[String(hall.id)].length > 0 ? (
                        <StallMap
                          stalls={mergedLayouts[String(hall.id)]}
                          readOnly={false}
                          selectedIds={selectedStalls
                            .filter((s) => s.hallId === String(hall.id))
                            .map((s) => s.stallId)}
                          onToggleSelect={(stallId, stall) => handleToggleStall(String(hall.id), stall)}
                        />
                      ) : (
                        <div className="text-sm text-muted-foreground">No stalls available.</div>
                      )
                    ) : (
                      <Button variant="outline" onClick={() => handleLoadHallLayout(String(hall.id))}>
                        Load Hall Map
                      </Button>
                    )}
                  </div>
                ),
              })) || []
            }
            onChange={(keys) => {
              const activeKey = Array.isArray(keys) ? keys[keys.length - 1] : keys;
              if (activeKey) {
                handleLoadHallLayout(String(activeKey));
              }
            }}
          />
        </div>

        <div className="lg:col-span-1">
          <Card className="sticky top-28">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingCart className="h-5 w-5" />
                Stall Selection
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-sm text-muted-foreground">
                Selected ({selectedStalls.length}/{MAX_SELECTION})
              </div>
              {selectedStalls.length === 0 ? (
                <p className="text-sm text-muted-foreground">No stalls selected.</p>
              ) : (
                <div className="space-y-2">
                  {selectedStalls.map((stall) => (
                    <div
                      key={stall.stallId}
                      className="flex items-center justify-between rounded border px-2 py-2 text-sm"
                    >
                      <div className="flex flex-col">
                        <span className="font-medium">{stall.stallId}</span>
                        <span className="text-xs text-muted-foreground">
                          Hall {stall.hallId} · {stall.stallType || "Stall"}
                        </span>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold">LKR {Number(stall.price).toLocaleString()}</div>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 px-2 text-xs"
                          onClick={() =>
                            setSelectedStalls((prev) => prev.filter((s) => s.stallId !== stall.stallId))
                          }
                        >
                          Remove
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <div className="border-t pt-3">
                <div className="flex justify-between font-semibold">
                  <span>Total</span>
                  <span>LKR {totalPrice.toLocaleString()}</span>
                </div>
              </div>

              <Button className="w-full" disabled={selectedStalls.length === 0} onClick={handleProceed}>
                Proceed to Pay
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      <Dialog open={showConfirm} onOpenChange={setShowConfirm}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Booking</DialogTitle>
            <DialogDescription>Review your selected stalls before proceeding to payment.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4 text-sm">
            <div className="space-y-2">
              {selectedStalls.map((stall) => (
                <div key={stall.stallId} className="flex justify-between rounded border px-3 py-2">
                  <div>
                    <div className="font-semibold">{stall.stallId}</div>
                    <div className="text-xs text-muted-foreground">
                      Hall {stall.hallId} · {stall.stallType || "Stall"}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-semibold">LKR {Number(stall.price).toLocaleString()}</div>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex justify-between font-semibold border-t pt-3">
              <span>Total</span>
              <span>LKR {totalPrice.toLocaleString()}</span>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowConfirm(false)} disabled={bookingLoading}>
              Cancel
            </Button>
            <Button onClick={handleConfirmBooking} disabled={bookingLoading}>
              {bookingLoading ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Processing...
                </>
              ) : (
                "Confirm Booking"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default VendorExhibitionBooking;
