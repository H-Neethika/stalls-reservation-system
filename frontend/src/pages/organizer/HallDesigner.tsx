import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ArrowLeft, Save, Loader2 } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";

const HallDesigner = () => {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  
  const [hallName, setHallName] = useState("");
  const [description, setDescription] = useState("");
  const [rows, setRows] = useState(10);
  const [columns, setColumns] = useState(10);
  const [selectedCells, setSelectedCells] = useState<Set<string>>(new Set());
  const [cellSizes, setCellSizes] = useState<Map<string, string>>(new Map());
  const [cellPrices, setCellPrices] = useState<Map<string, number>>(new Map());
  const [saving, setSaving] = useState(false);

  const toggleCell = (row: number, col: number) => {
    const key = `${row}-${col}`;
    const newSelected = new Set(selectedCells);
    
    if (newSelected.has(key)) {
      newSelected.delete(key);
      const newSizes = new Map(cellSizes);
      newSizes.delete(key);
      setCellSizes(newSizes);
      const newPrices = new Map(cellPrices);
      newPrices.delete(key);
      setCellPrices(newPrices);
    } else {
      newSelected.add(key);
      const newSizes = new Map(cellSizes);
      newSizes.set(key, "SMALL");
      setCellSizes(newSizes);
      const newPrices = new Map(cellPrices);
      newPrices.set(key, 5000);
      setCellPrices(newPrices);
    }
    
    setSelectedCells(newSelected);
  };

  const getCellColor = (size: string) => {
    switch (size) {
      case "SMALL": return "bg-green-500";
      case "MEDIUM": return "bg-amber-500";
      case "LARGE": return "bg-red-500";
      default: return "bg-gray-300";
    }
  };

  const updateCellSize = (key: string, size: string) => {
    const newSizes = new Map(cellSizes);
    newSizes.set(key, size);
    setCellSizes(newSizes);
    
    // Update price based on size
    const newPrices = new Map(cellPrices);
    const basePrice = size === "SMALL" ? 5000 : size === "MEDIUM" ? 8000 : 12000;
    newPrices.set(key, basePrice);
    setCellPrices(newPrices);
  };

  const generateStallName = (index: number): string => {
    const letter = String.fromCharCode(65 + Math.floor(index / 26) % 26);
    const number = (index % 26) + 1;
    return `${letter}${number}`;
  };

  const handleSave = async () => {
    if (!hallName.trim()) {
      toast({
        title: "Error",
        description: "Please enter a hall name",
        variant: "destructive",
      });
      return;
    }

    if (selectedCells.size === 0) {
      toast({
        title: "Error",
        description: "Please select at least one stall",
        variant: "destructive",
      });
      return;
    }

    setSaving(true);
    try {
      // Create hall
      const newHall = await mockApi.createHall({
        name: hallName,
        description: description || `${hallName} - Custom designed hall`,
        rows,
        columns,
        created_by: user?.id || "",
      });

      // Create stalls
      const stalls = Array.from(selectedCells).map((key, index) => {
        const [row, col] = key.split("-").map(Number);
        return {
          hall_id: newHall.id,
          name: generateStallName(index),
          size: (cellSizes.get(key) || "SMALL") as "SMALL" | "MEDIUM" | "LARGE",
          row_position: row,
          col_position: col,
          price: cellPrices.get(key) || 5000,
          is_reserved: false,
        };
      });

      await mockApi.createStalls(stalls);

      toast({
        title: "Success",
        description: "Hall created successfully!",
      });

      navigate("/organizer/halls");
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/organizer/halls")}>
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
                  <Label htmlFor="rows">Rows</Label>
                  <Input
                    id="rows"
                    type="number"
                    min="1"
                    max="50"
                    value={rows}
                    onChange={(e) => setRows(Number(e.target.value))}
                  />
                </div>
                <div>
                  <Label htmlFor="columns">Columns</Label>
                  <Input
                    id="columns"
                    type="number"
                    min="1"
                    max="50"
                    value={columns}
                    onChange={(e) => setColumns(Number(e.target.value))}
                  />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Legend</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-green-500 rounded" />
                  <span className="text-sm">Small (LKR 5,000)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-amber-500 rounded" />
                  <span className="text-sm">Medium (LKR 8,000)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-red-500 rounded" />
                  <span className="text-sm">Large (LKR 12,000)</span>
                </div>
                <p className="text-xs text-muted-foreground mt-4">
                  Click on cells to add/remove stalls. Selected: {selectedCells.size}
                </p>
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
                {/* Stage */}
                <div className="bg-gradient-to-b from-primary/20 to-primary/5 p-4 rounded-t-lg text-center mb-4">
                  <p className="text-sm font-medium">STAGE / ENTRANCE</p>
                </div>

                {/* Grid */}
                <div className="overflow-auto max-h-[600px]">
                  <div className="space-y-1">
                    {Array.from({ length: rows }).map((_, rowIndex) => (
                      <div key={rowIndex} className="flex gap-1">
                        {Array.from({ length: columns }).map((_, colIndex) => {
                          const key = `${rowIndex}-${colIndex}`;
                          const isSelected = selectedCells.has(key);
                          const size = cellSizes.get(key) || "SMALL";

                          return (
                            <div key={key} className="relative group">
                              <button
                                onClick={() => toggleCell(rowIndex, colIndex)}
                                className={`
                                  w-12 h-12 rounded border-2 transition-all
                                  ${isSelected 
                                    ? getCellColor(size) + " border-primary" 
                                    : "bg-gray-100 border-gray-300 hover:border-primary"
                                  }
                                `}
                              />
                              {isSelected && (
                                <div className="absolute -top-10 left-1/2 transform -translate-x-1/2 hidden group-hover:block z-10">
                                  <Select value={size} onValueChange={(value) => updateCellSize(key, value)}>
                                    <SelectTrigger className="w-24 h-8 text-xs bg-white">
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="SMALL">Small</SelectItem>
                                      <SelectItem value="MEDIUM">Medium</SelectItem>
                                      <SelectItem value="LARGE">Large</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    ))}
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
