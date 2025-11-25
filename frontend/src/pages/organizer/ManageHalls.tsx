import { useEffect, useRef, useState } from "react";
import { Building2, Loader2, Map, ArrowLeft } from "lucide-react";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { hallService } from "@/services/hallService";
import { useToast } from "@/hooks/use-toast";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/hooks/use-auth";
import { Button } from "@/components/ui/button";
import StallMap from "./StallMap";
import { layoutService } from "@/services/layoutService";
import html2canvas from "html2canvas";
import { jsPDF } from "jspdf";

interface HallResponse {
  id?: string;
  hallName?: string;
  totalStalls?: number;
  stallTypes?: { stallTypeId: number; stallType: string; count: number }[];
  rows?: number;
  columns?: number;
  description?: string;
}

const ManageHalls = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [halls, setHalls] = useState<HallResponse[]>([]);
  const [activeHall, setActiveHall] = useState<HallResponse | null>(null);
  const [showMap, setShowMap] = useState(false);
  const [hallLayouts, setHallLayouts] = useState<Record<string, any[]>>({});
  const [layoutLoading, setLayoutLoading] = useState(false);
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const loadHalls = async () => {
      try {
        const data = await hallService.getHalls();
        const normalized = (Array.isArray(data) ? data : []).map((hall) => ({
          ...hall,
          hallName: hall.hallName || hall.description || `Hall ${hall.id ?? ""}`.trim(),
          stallTypes: hall.stallTypes || [],
          totalStalls:
            hall.totalStalls ??
            (hall.rows && hall.columns ? hall.rows * hall.columns : undefined),
        }));
        setHalls(normalized);
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : "Failed to load halls.";
        toast({
          title: "Error loading halls",
          description: message,
          variant: "destructive",
        });
        setHalls([]);
      } finally {
        setLoading(false);
      }
    };

    loadHalls();
  }, [toast]);

  const loadHallLayout = async (hallId: string | number) => {
    try {
      setLayoutLoading(true);
      const data = await layoutService.getLayoutByHall(Number(hallId));
      if (data?.stalls) {
        setHallLayouts((prev) => ({ ...prev, [String(hallId)]: data.stalls }));
      } else {
        setHallLayouts((prev) => ({ ...prev, [String(hallId)]: [] }));
      }
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Failed to load hall layout.";
      toast({
        title: "Layout load failed",
        description: message,
        variant: "destructive",
      });
      setHallLayouts((prev) => ({ ...prev, [String(hallId)]: [] }));
    } finally {
      setLayoutLoading(false);
    }
  };

  const openHallMap = (hall: HallResponse) => {
    setActiveHall(hall);
    setShowMap(true);
    if (hall.id && !hallLayouts[String(hall.id)]) {
      loadHallLayout(hall.id);
    }
  };

  const closeHallMap = () => {
    setActiveHall(null);
    setShowMap(false);
  };

  const downloadMap = async (mode: "png" | "pdf") => {
    if (!mapRef.current) return;
    try {
      const canvas = await html2canvas(mapRef.current, { scale: 2 });
      if (mode === "png") {
        const link = document.createElement("a");
        link.download = `${activeHall?.hallName || "hall-map"}.png`;
        link.href = canvas.toDataURL("image/png");
        link.click();
      } else {
        const imgData = canvas.toDataURL("image/png");
        const pdf = new jsPDF({
          orientation: canvas.width > canvas.height ? "landscape" : "portrait",
          unit: "px",
          format: [canvas.width, canvas.height],
        });
        pdf.addImage(imgData, "PNG", 0, 0, canvas.width, canvas.height);
        pdf.save(`${activeHall?.hallName || "hall-map"}.pdf`);
      }
      toast({
        title: "Download started",
        description: `Your hall map is being saved as ${mode.toUpperCase()}.`,
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Could not download map.";
      toast({
        title: "Download failed",
        description: message,
        variant: "destructive",
      });
    }
  };

  if (loading) {
    return (
      <OrganizerLayout title="Halls">
        <div className="flex h-[50vh] items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      </OrganizerLayout>
    );
  }

  const headerBlock = (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        {showMap && (
          <Button variant="ghost" size="icon" onClick={closeHallMap} aria-label="Back to halls">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        )}
        <div>
          <h2 className="text-3xl font-bold">{showMap ? activeHall?.hallName || "Hall map" : "Halls"}</h2>
          <p className="text-muted-foreground">
            {showMap
              ? "Interactive hall map. Click a stall to toggle its state (demo)."
              : user?.organizationName
                ? `${user.organizationName}'s halls`
                : "Your halls at a glance."}
          </p>
        </div>
      </div>
    </div>
  );

  const hallCards = (
    <>
      {halls.length === 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2].map((num) => (
            <Card
              key={`dummy-hall-${num}`}
              className="cursor-pointer transition hover:-translate-y-1 hover:shadow-lg"
              onClick={() => openHallMap({ hallName: `Hall ${String(num).padStart(2, "0")}` })}
            >
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <p className="text-xs uppercase text-muted-foreground">Hall</p>
                  <CardTitle>{`Hall ${String(num).padStart(2, "0")}`}</CardTitle>
                </div>
                <Building2 className="h-6 w-6 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-sm text-muted-foreground space-y-1">
                  <p>Name: Sample Hall</p>
                  <p>ID: N/A</p>
                  <p>Layout: 10 rows x 10 columns</p>
                  <p className="line-clamp-2">
                    This is a placeholder hall card. Your halls will appear here once created.
                  </p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {halls.map((hall, index) => (
            <Card
              key={hall.id ?? `hall-${index}`}
              className="cursor-pointer transition hover:-translate-y-1 hover:shadow-lg"
              onClick={() => openHallMap(hall)}
            >
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <p className="text-xs uppercase text-muted-foreground">Hall</p>
                  <CardTitle>{`Hall ${String(index + 1).padStart(2, "0")}`}</CardTitle>
                </div>
                <Building2 className="h-6 w-6 text-primary" />
              </CardHeader>
              <CardContent>
                <div className="text-sm text-muted-foreground space-y-2">
                  {hall.hallName && <p className="font-semibold text-foreground">{hall.hallName}</p>}
                  <p>ID: {hall.id ?? "N/A"}</p>
                  {typeof hall.totalStalls === "number" && (
                    <p>
                      Total Stalls: <span className="font-medium text-foreground">{hall.totalStalls}</span>
                    </p>
                  )}
                  {hall.stallTypes && hall.stallTypes.length > 0 ? (
                    <div className="space-y-1">
                      <p className="text-xs uppercase text-muted-foreground">By Stall Type</p>
                      <div className="flex flex-wrap gap-2">
                        {hall.stallTypes.map((type) => (
                          <span
                            key={`${hall.id}-${type.stallTypeId}`}
                            className="rounded-full bg-primary/10 px-3 py-1 text-xs text-primary"
                          >
                            {type.stallType}: {type.count}
                          </span>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <p>Stall type breakdown: N/A</p>
                  )}
                  {hall.description && <p className="line-clamp-2">{hall.description}</p>}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </>
  );

  console.log(hallLayouts, "hall layouts");

  return (
    <OrganizerLayout title="Halls">
      <div className="space-y-6">
        {headerBlock}
        {showMap ? (
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <div className="flex items-center gap-2 mb-4 text-sm text-muted-foreground">
              <Map className="h-4 w-4" />
              Viewing map for {activeHall?.hallName || "Hall"}
            </div>
            <div className="flex gap-2 mb-4">
              <Button size="sm" variant="outline" onClick={() => downloadMap("png")} disabled={layoutLoading}>
                Download PNG
              </Button>
              <Button size="sm" variant="outline" onClick={() => downloadMap("pdf")} disabled={layoutLoading}>
                Download PDF
              </Button>
            </div>
            {layoutLoading && (
              <div className="flex h-32 items-center justify-center text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Loading layout...
              </div>
            )}
            {!layoutLoading && (
              <>
                {activeHall?.id && hallLayouts[String(activeHall.id)] ? (
                  <div ref={mapRef} className="space-y-2">
                    {activeHall?.hallName && (
                      <div className="text-sm font-semibold text-foreground">{activeHall.hallName}</div>
                    )}
                    <StallMap stalls={hallLayouts[String(activeHall.id)]} readOnly />
                  </div>
                ) : (
                  <div className="text-sm text-muted-foreground">
                    No layout data available for this hall.
                  </div>
                )}
              </>
            )}
          </div>
        ) : (
          hallCards
        )}
      </div>
    </OrganizerLayout>
  );
};

export default ManageHalls;
