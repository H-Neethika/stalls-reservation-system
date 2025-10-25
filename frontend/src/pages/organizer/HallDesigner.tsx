import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, Loader2, Save, Plus, Pencil, Eraser } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";
import { Stage, Layer, Rect, Text, Transformer, Line } from "react-konva";

interface StallShape {
  id: string;
  name: string;
  x: number;
  y: number;
  width: number;
  height: number;
  color: string;
  price?: number;
  size?: "SMALL" | "MEDIUM" | "LARGE";
}

const HallDesigner = () => {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  const { hallId } = useParams<{ hallId: string }>();

  const [hallName, setHallName] = useState("");
  const [description, setDescription] = useState("");
  const [layout, setLayout] = useState<StallShape[]>([]);
  const [boundaryPoints, setBoundaryPoints] = useState<number[]>([]);
  const [drawMode, setDrawMode] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  const stageRef = useRef<any>(null);
  const transformerRef = useRef<any>(null);

  // Load hall + layout if editing
  useEffect(() => {
    const loadData = async () => {
      if (!hallId) {
        setLoading(false);
        return;
      }
      const halls = await mockApi.getHalls();
      const hall = halls.find((h) => h.id === hallId);
      if (hall) {
        setHallName(hall.name);
        setDescription(hall.description);
      }

      const layoutData = await mockApi.getLayoutByHallId(hallId);
      if (layoutData?.layout_json) {
        setLayout(layoutData.layout_json.stalls || []);
        setBoundaryPoints(layoutData.layout_json.boundary || []);
      }
      setLoading(false);
    };
    loadData();
  }, [hallId]);

  // Sync transformer when stall selected
  useEffect(() => {
    if (selectedId && transformerRef.current && stageRef.current) {
      const selectedNode = stageRef.current.findOne(`#${selectedId}`);
      if (selectedNode) {
        transformerRef.current.nodes([selectedNode]);
        transformerRef.current.getLayer().batchDraw();
      }
    }
  }, [selectedId, layout]);

  // Add new stall
  const handleAddStall = () => {
    const newStall: StallShape = {
      id: `stall-${Date.now()}`,
      name: `S${layout.length + 1}`,
      x: 120 + layout.length * 20,
      y: 120 + layout.length * 10,
      width: 60,
      height: 40,
      color: "#6EE7B7",
      size: "SMALL",
      price: 5000,
    };
    setLayout((prev) => [...prev, newStall]);
  };

  // Handle dragging stalls
  const handleDragMove = (id: string, e: any) => {
    const { x, y } = e.target.position();
    setLayout((prev) =>
      prev.map((stall) => (stall.id === id ? { ...stall, x, y } : stall))
    );
  };

  // Handle resizing stalls
  const handleTransform = (id: string, node: any) => {
    const scaleX = node.scaleX();
    const scaleY = node.scaleY();
    const newWidth = Math.max(30, node.width() * scaleX);
    const newHeight = Math.max(30, node.height() * scaleY);
    node.scaleX(1);
    node.scaleY(1);
    setLayout((prev) =>
      prev.map((stall) =>
        stall.id === id ? { ...stall, width: newWidth, height: newHeight } : stall
      )
    );
  };

  // Handle stage click for drawing boundary
  const handleStageClick = (e: any) => {
    if (!drawMode) return;
    const stage = e.target.getStage();
    const pointer = stage.getPointerPosition();
    if (pointer) {
      setBoundaryPoints((prev) => [...prev, pointer.x, pointer.y]);
    }
  };

  const clearBoundary = () => setBoundaryPoints([]);

  // Save layout + boundary
  const handleSave = async () => {
    if (!hallName.trim()) {
      toast({ title: "Error", description: "Please enter a hall name", variant: "destructive" });
      return;
    }

    setSaving(true);
    try {
      let currentHallId = hallId;

      if (!currentHallId) {
        const newHall = await mockApi.createHall({
          name: hallName,
          description: description || `${hallName} - Custom designed hall`,
          rows: 0,
          columns: 0,
          created_by: user?.id || "",
        });
        currentHallId = newHall.id;
      }

      await mockApi.saveLayout(currentHallId!, {
        stalls: layout,
        boundary: boundaryPoints,
      });

      toast({ title: "Saved", description: "Layout saved successfully!" });
      navigate("/organizer/halls");
    } catch (err: any) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    } finally {
      setSaving(false);
    }
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
      {/* Header */}
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/organizer/halls")}>
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <h1 className="text-2xl font-bold">Hall Designer</h1>
          </div>
          <div className="flex gap-2">
            <Button onClick={handleSave} disabled={saving}>
              {saving ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" /> Saving...
                </>
              ) : (
                <>
                  <Save className="w-4 h-4 mr-2" /> Save Layout
                </>
              )}
            </Button>
            <Button variant="ghost" onClick={signOut}>Sign Out</Button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8 grid lg:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Hall Details</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              <div>
                <Label>Hall Name</Label>
                <Input value={hallName} onChange={(e) => setHallName(e.target.value)} />
              </div>
              <div>
                <Label>Description</Label>
                <Textarea value={description} onChange={(e) => setDescription(e.target.value)} />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Layout Tools</CardTitle></CardHeader>
            <CardContent className="flex flex-col gap-2">
              <Button onClick={handleAddStall}>
                <Plus className="mr-2 h-4 w-4" /> Add Stall
              </Button>
              <Button
                variant={drawMode ? "secondary" : "outline"}
                onClick={() => setDrawMode((p) => !p)}
              >
                {drawMode ? (
                  <>
                    <Pencil className="mr-2 h-4 w-4" /> Drawing Mode: ON
                  </>
                ) : (
                  <>
                    <Pencil className="mr-2 h-4 w-4" /> Draw Hall Boundary
                  </>
                )}
              </Button>
              <Button variant="destructive" onClick={clearBoundary}>
                <Eraser className="mr-2 h-4 w-4" /> Clear Boundary
              </Button>
              <p className="text-xs text-muted-foreground mt-2">
                - Draw the outer hall shape by clicking points on canvas.<br />
                - Add stalls and drag/resize them inside the shape.
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Canvas */}
        <div className="lg:col-span-3">
          <Card>
            <CardHeader><CardTitle>Canvas</CardTitle></CardHeader>
            <CardContent>
              <Stage
                ref={stageRef}
                width={900}
                height={600}
                className="border border-gray-300 bg-white rounded"
                onMouseDown={(e) => {
                  if (drawMode) handleStageClick(e);
                  else if (e.target === e.target.getStage()) setSelectedId(null);
                }}
              >
                <Layer>
                  {/* Boundary Polygon */}
                  {boundaryPoints.length > 2 && (
                    <Line
                      points={boundaryPoints}
                      stroke="#6B7280"
                      strokeWidth={2}
                      closed
                      fill="rgba(147,197,253,0.15)"
                    />
                  )}

                  {/* Stalls */}
                  {layout.map((stall) => (
                    <Rect
                      key={stall.id}
                      id={stall.id}
                      x={stall.x}
                      y={stall.y}
                      width={stall.width}
                      height={stall.height}
                      fill={stall.color}
                      stroke={stall.id === selectedId ? "#3B82F6" : "#000"}
                      strokeWidth={stall.id === selectedId ? 2 : 1}
                      draggable
                      onDragMove={(e) => handleDragMove(stall.id, e)}
                      onClick={() => setSelectedId(stall.id)}
                      onTransformEnd={(e) => handleTransform(stall.id, e.target)}
                    />
                  ))}

                  {/* Resize Transformer */}
                  <Transformer ref={transformerRef} />

                  {/* Stall Labels */}
                  {layout.map((stall) => (
                    <Text
                      key={`${stall.id}-label`}
                      text={stall.name}
                      x={stall.x}
                      y={stall.y + stall.height / 2 - 8}
                      width={stall.width}
                      align="center"
                      fontSize={12}
                      fill="#000"
                    />
                  ))}
                </Layer>
              </Stage>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default HallDesigner;
