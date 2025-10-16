import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall as MockHall } from "@/lib/mockData";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Building2, Loader2, Grid3x3 } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";

interface Hall extends MockHall {
  available_stalls: number;
  total_stalls: number;
}

const HallsList = () => {
  const navigate = useNavigate();
  const { signOut } = useAuth();
  const { toast } = useToast();
  const [halls, setHalls] = useState<Hall[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHalls();
  }, []);

  const fetchHalls = async () => {
    try {
      const hallsData = await mockApi.getHalls();
      const allStalls = await mockApi.getStalls();

      // Calculate stall counts for each hall
      const hallsWithCounts: Hall[] = hallsData.map((hall) => {
        const hallStalls = allStalls.filter((s) => s.hall_id === hall.id);
        const availableStalls = hallStalls.filter((s) => !s.is_reserved);

        return {
          ...hall,
          total_stalls: hallStalls.length,
          available_stalls: availableStalls.length,
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
          <h1 className="text-2xl font-bold">Exhibition Halls</h1>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => navigate("/my-bookings")}>
              My Bookings
            </Button>
            <Button variant="ghost" onClick={signOut}>
              Sign Out
            </Button>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-12">
        <div className="text-center mb-12">
          <Building2 className="w-16 h-16 mx-auto mb-4 text-primary" />
          <h2 className="text-4xl font-bold mb-4">Available Halls</h2>
          <p className="text-muted-foreground text-lg">
            Select a hall to view and reserve available stalls
          </p>
        </div>

        {halls.length === 0 ? (
          <Card className="text-center py-12">
            <CardContent>
              <Grid3x3 className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
              <p className="text-muted-foreground">No halls available at the moment.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {halls.map((hall) => (
              <Card key={hall.id} className="hover-scale cursor-pointer" onClick={() => navigate(`/halls/${hall.id}`)}>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Building2 className="w-5 h-5" />
                    {hall.name}
                  </CardTitle>
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
                      <span className="font-medium">{hall.total_stalls}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Available:</span>
                      <span className="font-medium text-green-600">{hall.available_stalls}</span>
                    </div>
                  </div>
                  <Button className="w-full mt-4">View Stalls</Button>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default HallsList;
