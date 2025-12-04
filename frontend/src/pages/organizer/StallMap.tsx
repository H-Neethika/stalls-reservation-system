import { useEffect, useState } from "react";
import hallData from "./mockHallMap.json";

type StallSize = "SMALL" | "MEDIUM" | "LARGE";
type StallStatus = "available" | "held" | "processing" | "booked" | "reserved" | "pending";

type Point = { x: number; y: number };

type StallInput = {
  id: string | number;
  displayName?: string; 
  size?: StallSize;
  stallType?: string;
  bookingStatus?: string;
  points?: Point[];
  path?: string | null;

};

type Stall = {
  id: string;
  displayName: string; 
  size: StallSize;
  points: Point[];
  path?: string | null;
  status?: StallStatus;
  raw?: StallInput;
};

const statusColors: Record<StallStatus, string> = {
  available: "#ffffff",
  held: "#046528ff", // user-selected
  processing: "#eab308",
  booked: "#ef4444",
  reserved: "#ADADAD",
  pending: "#ea9c0cff",
};

const toSize = (value?: string): StallSize => {
  const normalized = (value || "").toUpperCase();
  if (normalized === "MEDIUM") return "MEDIUM";
  if (normalized === "LARGE") return "LARGE";
  return "SMALL";
};

const toStatus = (value?: string): StallStatus => {
  const normalized = (value || "").toUpperCase();
  if (normalized === "HELD") return "held";
  if (normalized === "PROCESSING") return "processing";
  if (normalized === "BOOKED") return "booked";
  if (normalized === "RESERVED") return "reserved";
  if (normalized === "PENDING") return "pending";
  return "available";
};

interface StallMapProps {
  stalls?: StallInput[];
  readOnly?: boolean;
  selectedIds?: string[];
  onToggleSelect?: (stallId: string, stall: Stall) => void;
}

export default function StallMap({
  stalls: externalStalls,
  readOnly = false,
  selectedIds = [],
  onToggleSelect,
}: StallMapProps) {
  const [status, setStatus] = useState<Record<string, StallStatus>>({});
  const [stalls, setStalls] = useState<Stall[]>([]);

  useEffect(() => {
    const source = externalStalls && externalStalls.length > 0 ? externalStalls : (hallData as StallInput[]);
    const mapped: Stall[] = source
      .filter((s) => s.points && s.points.length > 0)
      .map((stall) => ({
        id: String(stall.id),
        displayName: stall.displayName ?? String(stall.id),
        size: toSize(stall.size || stall.stallType),
        points: stall.points as Point[],
        path: stall.path ?? null,
        status: toStatus(stall.bookingStatus),
      }));

    const initialStatus: Record<string, StallStatus> = {};
    mapped.forEach((stall) => {
      initialStatus[stall.id] = stall.status || "available";
    });

    setStalls(mapped);
    console.log("stalls : ",stalls);
    setStatus(initialStatus);
  }, [externalStalls]);

  const handleStallClick = (stall: Stall) => {
    const current = status[stall.id];
    if (readOnly) return;
    if (current === "booked" || current === "reserved" || current === "processing" || current === "pending") return;
    if (onToggleSelect) {
      onToggleSelect(stall.id, stall);
    }
  };

  const getCentroid = (points: Point[]) => {
    const cx = points.reduce((acc, p) => acc + p.x, 0) / points.length;
    const cy = points.reduce((acc, p) => acc + p.y, 0) / points.length;
    return { x: cx, y: cy };
  };

  return (
    <svg viewBox="0 0 1100 600" width="100%" className="w-full">
      {stalls.map((stall) => {
        const centroid = getCentroid(stall.points);
        const baseStatus = status[stall.id] || "available";
        const isSelected = selectedIds.includes(stall.id);
        const visualStatus =
          baseStatus === "available" && isSelected ? ("held" as StallStatus) : (baseStatus as StallStatus);
        const fillColor = statusColors[visualStatus] || statusColors.available;

        return (
          <g
            key={stall.id}
            onClick={() => handleStallClick(stall)}
            className={readOnly ? "cursor-default" : "cursor-pointer transition-all"}
            style={{ filter: "drop-shadow(0px 1px 3px rgba(0,0,0,0.1))" }}
          >
            {stall.path ? (
              <path d={stall.path} fill={fillColor} opacity="0.8" />
            ) : (
              <polygon
                points={stall.points.map((p) => `${p.x},${p.y}`).join(" ")}
                fill={fillColor}
                opacity="0.8"
              />
            )}
            <polygon
              points={stall.points.map((p) => `${p.x},${p.y}`).join(" ")}
              fill="none"
              stroke="#0f172a"
              strokeWidth="2"
            />
            <text
              x={centroid.x}
              y={centroid.y}
              textAnchor="middle"
              dominantBaseline="middle"
              fontSize="10"
              fill="#0f172a"
              fontWeight="400"
            >
             {stall.displayName}
            </text>
          </g>
        );
      })}
    </svg>
  );
}
