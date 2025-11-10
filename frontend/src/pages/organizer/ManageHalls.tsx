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
import { Loader2, Grid3x3, Plus } from "lucide-react";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { useToast } from "@/hooks/use-toast";

interface HallWithCount extends Hall {
  stall_count: number;
}

const ManageHalls = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [halls, setHalls] = useState<HallWithCount[]>([]);
  const [loading, setLoading] = useState(true);

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

  const content = (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-3xl font-bold">Manage Halls</h2>
          <p className="text-muted-foreground">
            Review existing hall layouts or create new ones.
          </p>
        </div>
        <Button onClick={() => navigate("/organizer/halls/design")}>
          <Plus className="w-4 h-4 mr-2" />
          New Hall
        </Button>
      </div>

      {halls.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <Grid3x3 className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground mb-4">No halls created yet.</p>
            <Button onClick={() => navigate("/organizer/halls/design")}>
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
                      {hall.rows} × {hall.columns}
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
