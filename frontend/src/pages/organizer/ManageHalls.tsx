import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall } from "@/lib/mockData";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Loader2, Grid3x3, Plus } from "lucide-react";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";
import { hallService } from "@/services/hallService";
import { Table, Space } from "antd";
import type { ColumnsType } from "antd/es/table";

interface HallWithCount extends Hall {
  stall_count: number;
}

interface CreatedHallResponse {
  id?: string;
  hallName?: string;
  rows?: number;
  columns?: number;
  description?: string;
  created_at?: string;
}

const ManageHalls = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user } = useAuth();
  const [halls, setHalls] = useState<HallWithCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateHallOpen, setIsCreateHallOpen] = useState(false);
  const [creatingHall, setCreatingHall] = useState(false);
  const [hallForm, setHallForm] = useState({
    name: "",
    rows: 8,
    columns: 8,
  });

  useEffect(() => {
    fetchHalls();
  }, []);

  const fetchHalls = async () => {
    try {
      const apiHalls = await hallService.getHalls();
      const normalized: HallWithCount[] = apiHalls.map((hall) => ({
        id: hall.id?.toString() ?? `hall-${Date.now()}-${Math.random()}`,
        name: hall.hallName ?? "Untitled Hall",
        description: hall.description ?? "",
        rows: hall.rows ?? 0,
        columns: hall.columns ?? 0,
        created_by: user?.id || "organizer",
        created_at: hall.createdAt || hall.created_at || new Date().toISOString(),
        stall_count:
          typeof hall.totalStalls === "number"
            ? hall.totalStalls
            : hall.rows && hall.columns
            ? hall.rows * hall.columns
            : 0,
      }));

      setHalls(normalized);
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Failed to load halls.";
      toast({
        title: "Error loading halls",
        description: `${message} (showing local sample data)`,
        variant: "destructive",
      });

      // Fallback to mock data so UI still works
      const hallsData = await mockApi.getHalls();
      const allStalls = await mockApi.getStalls();
      const hallsWithCounts: HallWithCount[] = hallsData.map((hall) => {
        const stallCount = allStalls.filter((s) => s.hall_id === hall.id).length;
        return {
          ...hall,
          stall_count: stallCount,
        };
      });

      setHalls(hallsWithCounts);
    } finally {
      setLoading(false);
    }
  };

  const resetHallForm = () =>
    setHallForm({
      name: "",
      rows: 8,
      columns: 8,
    });

  const handleCreateHall = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!hallForm.name.trim()) {
      toast({
        title: "Validation error",
        description: "Hall name is required.",
        variant: "destructive",
      });
      return;
    }
    if (hallForm.rows < 1 || hallForm.columns < 1) {
      toast({
        title: "Validation error",
        description: "Rows and columns must be at least 1.",
        variant: "destructive",
      });
      return;
    }

    setCreatingHall(true);
    try {
      const createdHall: CreatedHallResponse = await hallService.createHall({
        hallName: hallForm.name.trim(),
        rows: Number(hallForm.rows),
        columns: Number(hallForm.columns),
      });

      setHalls((prev) => [
        ...prev,
        {
          id: createdHall?.id || `hall-${Date.now()}`,
          name: createdHall?.hallName || hallForm.name.trim(),
          description: createdHall?.description || "",
          rows: createdHall?.rows ?? hallForm.rows,
          columns: createdHall?.columns ?? hallForm.columns,
          created_by: user?.id || "organizer",
          created_at: createdHall?.created_at || new Date().toISOString(),
          stall_count: 0,
        },
      ]);

      toast({
        title: "Hall created",
        description: `${hallForm.name.trim()} is now available.`,
      });
      resetHallForm();
      setIsCreateHallOpen(false);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Failed to create hall.";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setCreatingHall(false);
    }
  };

  const columns: ColumnsType<HallWithCount & { key: string }> = useMemo(
    () => [
      {
        title: "Hall",
        dataIndex: "name",
        key: "hall",
        render: (value) => <span className="font-semibold">{value}</span>,
      },
      {
        title: "Layout (Rows x Columns)",
        dataIndex: "layout",
        key: "layout",
        render: (_, record) => `${record.rows} × ${record.columns}`,
      },
      {
        title: "Total Stalls",
        dataIndex: "stall_count",
        key: "stalls",
      },
      {
        title: "Created",
        dataIndex: "created_at",
        key: "created_at",
        render: (value) =>
          value ? new Date(value).toLocaleDateString() : "—",
      },
      {
        title: "Actions",
        key: "actions",
        render: (_, record) => (
          <Space size="small">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate(`/organizer/stalls?hallId=${record.id}`)}
            >
              View
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                toast({
                  title: "Edit hall",
                  description: "Hall editing is coming soon.",
                })
              }
            >
              Edit
            </Button>
            <Button
              variant="destructive"
              size="sm"
              onClick={() =>
                toast({
                  title: "Delete hall",
                  description: "Hall deletion is coming soon.",
                })
              }
            >
              Delete
            </Button>
          </Space>
        ),
      },
    ],
    [navigate, toast],
  );

  const tableData = useMemo(
    () => halls.map((hall) => ({ ...hall, key: hall.id })),
    [halls],
  );

  const content = (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-3xl font-bold">Manage Halls</h2>
          <p className="text-muted-foreground">
            Review existing hall layouts or create new ones.
          </p>
        </div>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <Dialog open={isCreateHallOpen} onOpenChange={setIsCreateHallOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                Create Hall
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create a new hall</DialogTitle>
                <DialogDescription>
                  Define the hall layout that will contain your stalls.
                </DialogDescription>
              </DialogHeader>
              <form className="space-y-4" onSubmit={handleCreateHall}>
                <div className="space-y-2">
                  <Label htmlFor="hall-name">Hall Name</Label>
                  <Input
                    id="hall-name"
                    value={hallForm.name}
                    onChange={(e) =>
                      setHallForm((prev) => ({ ...prev, name: e.target.value }))
                    }
                    placeholder="E.g. Hall W"
                    required
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="hall-rows">Rows</Label>
                    <Input
                      id="hall-rows"
                      type="number"
                      min={1}
                      value={hallForm.rows}
                      onChange={(e) =>
                        setHallForm((prev) => ({
                          ...prev,
                          rows: Number(e.target.value),
                        }))
                      }
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="hall-columns">Columns</Label>
                    <Input
                      id="hall-columns"
                      type="number"
                      min={1}
                      value={hallForm.columns}
                      onChange={(e) =>
                        setHallForm((prev) => ({
                          ...prev,
                          columns: Number(e.target.value),
                        }))
                      }
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => setIsCreateHallOpen(false)}
                    disabled={creatingHall}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={creatingHall}>
                    {creatingHall ? "Creating..." : "Create Hall"}
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
          <Button
            variant="secondary"
            onClick={() => navigate("/organizer/stalls/create")}
          >
            Create Stall
          </Button>
        </div>
      </div>

      {halls.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <Grid3x3 className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground mb-4">No halls created yet.</p>
            <Button onClick={() => setIsCreateHallOpen(true)}>
              Create Your First Hall
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent className="p-0">
            <Table
              columns={columns}
              dataSource={tableData}
              pagination={{ pageSize: 6 }}
              rowKey="key"
            />
          </CardContent>
        </Card>
      )}
    </div>
  );

  if (loading) {
    return (
      <OrganizerLayout title="Manage Halls">
        <div className="flex h-[50vh] items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      </OrganizerLayout>
    );
  }

  return <OrganizerLayout title="Manage Halls">{content}</OrganizerLayout>;
};

export default ManageHalls;
