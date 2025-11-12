import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall } from "@/lib/mockData";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
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

interface HallWithCount extends Hall {
  stall_count: number;
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
    description: "",
    rows: 8,
    columns: 8,
  });

  useEffect(() => {
    fetchHalls();
  }, []);

  const fetchHalls = async () => {
    try {
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
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Failed to load halls.";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const resetHallForm = () =>
    setHallForm({
      name: "",
      description: "",
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
      const newHall = await mockApi.createHall({
        name: hallForm.name.trim(),
        description: hallForm.description.trim(),
        rows: Number(hallForm.rows),
        columns: Number(hallForm.columns),
        created_by: user?.id || "organizer",
      });

      setHalls((prev) => [
        ...prev,
        {
          ...newHall,
          stall_count: 0,
        },
      ]);
      toast({
        title: "Hall created",
        description: `${newHall.name} is now available.`,
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
                    placeholder="E.g. Hall D"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="hall-description">Description</Label>
                  <Input
                    id="hall-description"
                    value={hallForm.description}
                    onChange={(e) =>
                      setHallForm((prev) => ({
                        ...prev,
                        description: e.target.value,
                      }))
                    }
                    placeholder="Optional description"
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
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {halls.map((hall) => (
            <Card key={hall.id} className="hover-scale">
              <CardHeader>
                <CardTitle>{hall.name}</CardTitle>
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
                    <span className="font-medium">{hall.stall_count}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
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
