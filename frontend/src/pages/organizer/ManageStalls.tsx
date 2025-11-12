import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockApi, Hall, Stall } from "@/lib/mockData";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { Loader2, Plus } from "lucide-react";

type StallWithHall = Stall & { hallName: string };

const ManageStalls = () => {
  const { toast } = useToast();
  const navigate = useNavigate();
  const [stalls, setStalls] = useState<StallWithHall[]>([]);
  const [halls, setHalls] = useState<Hall[]>([]);
  const [selectedHall, setSelectedHall] = useState<string>("all");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [hallData, stallData] = await Promise.all([
          mockApi.getHalls(),
          mockApi.getStalls(),
        ]);
        setHalls(hallData);
        setStalls(
          stallData.map((stall) => ({
            ...stall,
            hallName:
              hallData.find((hall) => hall.id === stall.hall_id)?.name ||
              "Unknown Hall",
          })),
        );
      } catch (error: unknown) {
        const message =
          error instanceof Error ? error.message : "Failed to load stalls.";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [toast]);

  const filteredStalls = useMemo(() => {
    if (selectedHall === "all") return stalls;
    return stalls.filter((stall) => stall.hall_id === selectedHall);
  }, [selectedHall, stalls]);

  const statusBadge = (isReserved: boolean) =>
    isReserved ? (
      <Badge variant="secondary">Reserved</Badge>
    ) : (
      <Badge variant="default">Available</Badge>
    );

  if (loading) {
    return (
      <OrganizerLayout title="Manage Stalls">
        <div className="flex h-[50vh] items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      </OrganizerLayout>
    );
  }

  return (
    <OrganizerLayout title="Manage Stalls">
      <div className="space-y-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-3xl font-bold">Stalls</h2>
            <p className="text-muted-foreground">
              View availability and create new stalls for each hall.
            </p>
          </div>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <Select value={selectedHall} onValueChange={setSelectedHall}>
              <SelectTrigger className="w-52">
                <SelectValue placeholder="Filter by hall" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All halls</SelectItem>
                {halls.map((hall) => (
                  <SelectItem key={hall.id} value={hall.id}>
                    {hall.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button onClick={() => navigate("/organizer/stalls/create")}>
              <Plus className="w-4 h-4 mr-2" />
              Create Stall
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Stall inventory</CardTitle>
          </CardHeader>
          <CardContent className="overflow-x-auto">
            {filteredStalls.length === 0 ? (
              <p className="py-6 text-center text-muted-foreground">
                No stalls found for the selected hall.
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Stall</TableHead>
                    <TableHead>Hall</TableHead>
                    <TableHead>Size</TableHead>
                    <TableHead>Price (LKR)</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="hidden md:table-cell">
                      Position (Row / Col)
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredStalls.map((stall) => (
                    <TableRow key={stall.id}>
                      <TableCell className="font-medium">
                        {stall.name}
                      </TableCell>
                      <TableCell>{stall.hallName}</TableCell>
                      <TableCell>{stall.size}</TableCell>
                      <TableCell>
                        {stall.price.toLocaleString(undefined, {
                          minimumFractionDigits: 0,
                        })}
                      </TableCell>
                      <TableCell>{statusBadge(stall.is_reserved)}</TableCell>
                      <TableCell className="hidden md:table-cell">
                        Row {stall.row_position + 1} / Col {stall.col_position + 1}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </OrganizerLayout>
  );
};

export default ManageStalls;
