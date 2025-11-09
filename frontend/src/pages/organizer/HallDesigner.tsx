import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArrowLeft, Loader2, Plus, Save, X } from "lucide-react";
import { useAuth } from "@/hooks/use-auth";
import { useToast } from "@/hooks/use-toast";

type StallSize = "SMALL" | "MEDIUM" | "LARGE";

interface PlacedStall {
  id: string;
  name: string;
  size: StallSize;
  row: number;
  col: number;
  price: number;
}

interface DragState {
  stallId: string;
  offsetX: number;
  offsetY: number;
  previewRow: number;
  previewCol: number;
}

const STALL_SIZE_PRICES: Record<StallSize, number> = {
  SMALL: 5000,
  MEDIUM: 8000,
  LARGE: 12000,
};

const STALL_SIZE_LABELS: Record<StallSize, string> = {
  SMALL: "Small",
  MEDIUM: "Medium",
  LARGE: "Large",
};

const STALL_SIZE_ABBREVIATIONS: Record<StallSize, string> = {
  SMALL: "S",
  MEDIUM: "M",
  LARGE: "L",
};

const STALL_SIZE_UNITS: Record<StallSize, number> = {
  SMALL: 1,
  MEDIUM: 2,
  LARGE: 4,
};

const CELL_SIZE = 48;

const HallDesigner = () => {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();

  const gridRef = useRef<HTMLDivElement | null>(null);
  const stallCounterRef = useRef(0);

  const [hallName, setHallName] = useState("");
  const [description, setDescription] = useState("");
  const [rows, setRows] = useState(10);
  const [columns, setColumns] = useState(10);
  const [defaultSize, setDefaultSize] = useState<StallSize>("SMALL");
  const [stalls, setStalls] = useState<PlacedStall[]>([]);
  const [dragState, setDragState] = useState<DragState | null>(null);
  const [saving, setSaving] = useState(false);

  const getPriceForSize = (size: StallSize) => STALL_SIZE_PRICES[size];

  const generateStallName = (index: number): string => {
    const letter = String.fromCharCode(65 + (Math.floor(index / 26) % 26));
    const number = (index % 26) + 1;
    return `${letter}${number}`;
  };

  const getNextStallName = () => {
    const name = generateStallName(stallCounterRef.current);
    stallCounterRef.current += 1;
    return name;
  };

  const isSpaceAvailable = useCallback(
    (row: number, col: number, size: StallSize, ignoreId?: string) => {
      const units = STALL_SIZE_UNITS[size];
      if (row < 0 || col < 0 || row + units > rows || col + units > columns) {
        return false;
      }

      return !stalls.some((stall) => {
        if (stall.id === ignoreId) {
          return false;
        }

        const stallUnits = STALL_SIZE_UNITS[stall.size];
        const rowOverlap =
          row < stall.row + stallUnits && row + units > stall.row;
        const colOverlap =
          col < stall.col + stallUnits && col + units > stall.col;
        return rowOverlap && colOverlap;
      });
    },
    [columns, rows, stalls]
  );

  const addStallAt = (size: StallSize, row: number, col: number) => {
    const name = getNextStallName();
    setStalls((prev) => [
      ...prev,
      {
        id: `stall-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        name,
        size,
        row,
        col,
        price: getPriceForSize(size),
      },
    ]);
  };

  const findAvailablePosition = useCallback(
    (size: StallSize) => {
      const units = STALL_SIZE_UNITS[size];
      if (units > rows || units > columns) {
        return null;
      }

      for (let row = 0; row <= rows - units; row += 1) {
        for (let col = 0; col <= columns - units; col += 1) {
          if (isSpaceAvailable(row, col, size)) {
            return { row, col };
          }
        }
      }

      return null;
    },
    [columns, rows, isSpaceAvailable]
  );

  const handleAddStallClick = () => {
    const position = findAvailablePosition(defaultSize);
    if (!position) {
      toast({
        title: "Layout full",
        description: `No free space for a ${STALL_SIZE_LABELS[
          defaultSize
        ].toLowerCase()} stall. Try resizing existing stalls or expanding the grid.`,
        variant: "destructive",
      });
      return;
    }

    addStallAt(defaultSize, position.row, position.col);
  };

  const handleGridClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (!gridRef.current) return;

    const rect = gridRef.current.getBoundingClientRect();
    const col = Math.floor((event.clientX - rect.left) / CELL_SIZE);
    const row = Math.floor((event.clientY - rect.top) / CELL_SIZE);
    const units = STALL_SIZE_UNITS[defaultSize];

    if (units > rows || units > columns) {
      toast({
        title: "Stall too large",
        description:
          "Increase the hall dimensions before placing this stall size.",
        variant: "destructive",
      });
      return;
    }

    const clampedRow = Math.max(0, Math.min(rows - units, row));
    const clampedCol = Math.max(0, Math.min(columns - units, col));

    if (!isSpaceAvailable(clampedRow, clampedCol, defaultSize)) {
      toast({
        title: "Space occupied",
        description:
          "That area is already covered by another stall. Choose another spot or move existing stalls.",
        variant: "destructive",
      });
      return;
    }

    addStallAt(defaultSize, clampedRow, clampedCol);
  };

  const removeStall = (id: string) => {
    setStalls((prev) => prev.filter((stall) => stall.id !== id));
  };

  const updateStallSize = (id: string, size: StallSize) => {
    setStalls((prev) => {
      const current = prev.find((stall) => stall.id === id);
      if (!current || current.size === size) {
        return prev;
      }

      if (!isSpaceAvailable(current.row, current.col, size, id)) {
        toast({
          title: "Not enough space",
          description:
            "Move the stall to a larger free area before increasing the size.",
          variant: "destructive",
        });
        return prev;
      }

      return prev.map((stall) =>
        stall.id === id
          ? { ...stall, size, price: getPriceForSize(size) }
          : stall
      );
    });
  };

  const startDrag = (
    event: React.PointerEvent<HTMLDivElement>,
    stall: PlacedStall
  ) => {
    const removeButton = (event.target as HTMLElement).closest(
      "[data-stall-remove]"
    );
    if (removeButton) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();

    const rect = event.currentTarget.getBoundingClientRect();
    setDragState({
      stallId: stall.id,
      offsetX: event.clientX - rect.left,
      offsetY: event.clientY - rect.top,
      previewRow: stall.row,
      previewCol: stall.col,
    });

    event.currentTarget.setPointerCapture(event.pointerId);
  };

  useEffect(() => {
    if (!dragState) return;

    const activeStall = stalls.find((stall) => stall.id === dragState.stallId);
    if (!activeStall) {
      setDragState(null);
      return;
    }

    const handlePointerMove = (event: PointerEvent) => {
      if (!gridRef.current) return;

      const containerRect = gridRef.current.getBoundingClientRect();
      let col = Math.round(
        (event.clientX - containerRect.left - dragState.offsetX) / CELL_SIZE
      );
      let row = Math.round(
        (event.clientY - containerRect.top - dragState.offsetY) / CELL_SIZE
      );

      const units = STALL_SIZE_UNITS[activeStall.size];
      col = Math.max(0, Math.min(columns - units, col));
      row = Math.max(0, Math.min(rows - units, row));

      if (!isSpaceAvailable(row, col, activeStall.size, activeStall.id)) {
        return;
      }

      setDragState((prev) => {
        if (!prev) return prev;
        if (prev.previewRow === row && prev.previewCol === col) {
          return prev;
        }
        return { ...prev, previewRow: row, previewCol: col };
      });
    };

    const handlePointerUp = () => {
      setStalls((prev) =>
        prev.map((stall) =>
          stall.id === dragState.stallId
            ? { ...stall, row: dragState.previewRow, col: dragState.previewCol }
            : stall
        )
      );
      setDragState(null);
    };

    window.addEventListener("pointermove", handlePointerMove);
    window.addEventListener("pointerup", handlePointerUp, { once: true });

    return () => {
      window.removeEventListener("pointermove", handlePointerMove);
      window.removeEventListener("pointerup", handlePointerUp);
    };
  }, [columns, dragState, isSpaceAvailable, rows, stalls]);

  const handleSave = async () => {
    if (!hallName.trim()) {
      toast({
        title: "Error",
        description: "Please enter a hall name.",
        variant: "destructive",
      });
      return;
    }

    if (stalls.length === 0) {
      toast({
        title: "Error",
        description: "Add at least one stall to the layout before saving.",
        variant: "destructive",
      });
      return;
    }

    setSaving(true);
    try {
      const newHall = await mockApi.createHall({
        name: hallName,
        description: description || `${hallName} - Custom designed hall`,
        rows,
        columns,
        created_by: user?.id || "",
      });

      const stallPayload = stalls.map((stall) => ({
        hall_id: newHall.id,
        name: stall.name,
        size: stall.size,
        row_position: stall.row,
        col_position: stall.col,
        price: stall.price,
        is_reserved: false,
      }));

      await mockApi.createStalls(stallPayload);

      toast({
        title: "Success",
        description: "Hall layout saved successfully.",
      });

      navigate("/organizer/halls");
    } catch (error: any) {
      toast({
        title: "Error",
        description:
          error?.message || "Something went wrong while saving the hall.",
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  const gridWidth = Math.max(columns * CELL_SIZE, 240);
  const gridHeight = Math.max(rows * CELL_SIZE, 240);

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => navigate("/organizer/halls")}
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <h1 className="text-2xl font-bold">Design Hall</h1>
          </div>
          <div className="flex gap-2">
            <Button onClick={handleSave} disabled={saving}>
              {saving ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="w-4 h-4 mr-2" />
                  Save Hall
                </>
              )}
            </Button>
            <Button variant="ghost" onClick={signOut}>
              Sign Out
            </Button>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-4 gap-6">
          {/* Configuration Panel */}
          <div className="lg:col-span-1 space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Hall Details</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="name">Hall Name</Label>
                  <Input
                    id="name"
                    value={hallName}
                    onChange={(e) => setHallName(e.target.value)}
                    placeholder="e.g., Hall A"
                  />
                </div>
                <div>
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Optional description"
                  />
                </div>
                <div>
                  <Label htmlFor="rows">Rows (small units)</Label>
                  <Input
                    id="rows"
                    type="number"
                    min="1"
                    max="60"
                    value={rows}
                    onChange={(e) =>
                      setRows(Math.max(1, Number(e.target.value) || 1))
                    }
                  />
                </div>
                <div>
                  <Label htmlFor="columns">Columns (small units)</Label>
                  <Input
                    id="columns"
                    type="number"
                    min="1"
                    max="60"
                    value={columns}
                    onChange={(e) =>
                      setColumns(Math.max(1, Number(e.target.value) || 1))
                    }
                  />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Stall Settings</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Default stall size</Label>
                  <Select
                    value={defaultSize}
                    onValueChange={(value) =>
                      setDefaultSize(value as StallSize)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="SMALL">
                        Small - LKR {STALL_SIZE_PRICES.SMALL.toLocaleString()}
                      </SelectItem>
                      <SelectItem value="MEDIUM">
                        Medium - LKR {STALL_SIZE_PRICES.MEDIUM.toLocaleString()}
                      </SelectItem>
                      <SelectItem value="LARGE">
                        Large - LKR {STALL_SIZE_PRICES.LARGE.toLocaleString()}
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-muted-foreground">
                    Medium stalls occupy 2x2 small units. Large stalls occupy
                    4x4 small units.
                  </p>
                </div>
                <Button
                  variant="secondary"
                  className="w-full"
                  onClick={handleAddStallClick}
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Add {STALL_SIZE_LABELS[defaultSize]} Stall
                </Button>
                <p className="text-xs text-muted-foreground">
                  Click on the layout to place the selected stall size. Drag
                  stalls to reposition them.
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Current Stalls ({stalls.length})</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {stalls.length === 0 ? (
                  <p className="text-sm text-muted-foreground">
                    No stalls yet. Use the button above or click the layout to
                    start placing stalls.
                  </p>
                ) : (
                  stalls.map((stall) => (
                    <div
                      key={stall.id}
                      className="flex items-center justify-between rounded-md border border-muted bg-background px-3 py-2 text-sm"
                    >
                      <div>
                        <p className="font-medium">{stall.name}</p>
                        <p className="text-xs text-muted-foreground">
                          {STALL_SIZE_LABELS[stall.size]} - Row {stall.row + 1},
                          Col {stall.col + 1} - LKR{" "}
                          {stall.price.toLocaleString()}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Select
                          value={stall.size}
                          onValueChange={(value) =>
                            updateStallSize(stall.id, value as StallSize)
                          }
                        >
                          <SelectTrigger className="h-8 w-24 text-xs">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="SMALL">Small</SelectItem>
                            <SelectItem value="MEDIUM">Medium</SelectItem>
                            <SelectItem value="LARGE">Large</SelectItem>
                          </SelectContent>
                        </Select>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => removeStall(stall.id)}
                          title="Remove stall"
                        >
                          <X className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>
          </div>

          {/* Canvas Area */}
          <div className="lg:col-span-3">
            <Card>
              <CardHeader>
                <CardTitle>Hall Layout</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="bg-gradient-to-b from-primary/20 to-primary/5 p-4 rounded-t-lg text-center mb-4">
                  <p className="text-sm font-medium">STAGE / ENTRANCE</p>
                </div>
                <div className="overflow-auto max-h-[600px] rounded-lg border border-dashed border-muted-foreground/40 bg-white">
                  <div
                    ref={gridRef}
                    onClick={handleGridClick}
                    className="relative"
                    style={{
                      width: gridWidth,
                      height: gridHeight,
                      backgroundImage:
                        "linear-gradient(to right, rgba(148, 163, 184, 0.3) 1px, transparent 1px), linear-gradient(to bottom, rgba(148, 163, 184, 0.3) 1px, transparent 1px)",
                      backgroundSize: `${CELL_SIZE}px ${CELL_SIZE}px`,
                    }}
                  >
                    {stalls.map((stall) => {
                      const units = STALL_SIZE_UNITS[stall.size];
                      const isDragging = dragState?.stallId === stall.id;
                      const displayRow = isDragging
                        ? dragState.previewRow
                        : stall.row;
                      const displayCol = isDragging
                        ? dragState.previewCol
                        : stall.col;

                      return (
                        <div
                          key={stall.id}
                          className={`absolute rounded-md border-2 bg-primary/10 text-primary transition-shadow ${
                            isDragging
                              ? "border-primary shadow-xl ring-2 ring-primary/50"
                              : "border-primary/60 hover:shadow-lg"
                          }`}
                          style={{
                            width: units * CELL_SIZE,
                            height: units * CELL_SIZE,
                            left: displayCol * CELL_SIZE,
                            top: displayRow * CELL_SIZE,
                          }}
                        >
                          <button
                            data-stall-remove
                            type="button"
                            onClick={(event) => {
                              event.stopPropagation();
                              event.preventDefault();
                              removeStall(stall.id);
                            }}
                            className="absolute right-1 top-1 rounded-full bg-white/80 p-1 text-primary shadow hover:bg-white"
                            title={`Remove ${stall.name}`}
                          >
                            <X className="h-3 w-3" />
                          </button>
                          <div
                            onPointerDown={(event) => startDrag(event, stall)}
                            className="flex h-full w-full cursor-grab select-none flex-col items-center justify-center gap-1 px-1 text-center text-xs font-semibold tracking-wide active:cursor-grabbing"
                          >
                            <span>{stall.name}</span>
                            <span className="text-[11px] font-medium">
                              {STALL_SIZE_ABBREVIATIONS[stall.size]}
                            </span>
                            <span className="text-[10px] font-normal text-muted-foreground">
                              LKR {stall.price.toLocaleString()}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HallDesigner;
