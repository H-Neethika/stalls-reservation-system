import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall } from "@/lib/mockData";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Loader2, ArrowLeft, Grid3x3, Plus } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";

interface HallWithCount extends Hall {
  stall_count: number;
}

const ManageHalls = () => {
  const navigate = useNavigate();
  const { signOut } = useAuth();
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
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
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
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/organizer/dashboard")}>
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <h1 className="text-2xl font-bold">Manage Halls</h1>
          </div>
          <div className="flex gap-2">
            <Button onClick={() => navigate("/organizer/halls/design")}>
              <Plus className="w-4 h-4 mr-2" />
              New Hall
            </Button>
            <Button variant="ghost" onClick={signOut}>
              Sign Out
            </Button>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
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
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
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
                      <span className="font-medium">{hall.rows} × {hall.columns}</span>
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
    </div>
  );
};

export default ManageHalls;
