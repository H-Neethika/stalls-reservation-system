import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { mockApi, Stall as MockStall, Hall as MockHall } from "@/lib/mockData";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2, ArrowLeft, ShoppingCart } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { generateQRCodeForReservation } from "@/utils/qrGenerator";

type Stall = MockStall;
type Hall = MockHall;

const HallBooking = () => {
  const { hallId } = useParams<{ hallId: string }>();
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { toast } = useToast();

  const [hall, setHall] = useState<Hall | null>(null);
  const [stalls, setStalls] = useState<Stall[]>([]);
  const [selectedStalls, setSelectedStalls] = useState<Stall[]>([]);
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);

  useEffect(() => {
    if (hallId) {
      fetchHallData();
    }
  }, [hallId]);

  const fetchHallData = async () => {
    try {
      const hallsData = await mockApi.getHalls();
      const hallData = hallsData.find((h) => h.id === hallId);

      if (!hallData) throw new Error("Hall not found");
      setHall(hallData);

      const stallsData = await mockApi.getStalls(hallId);
      setStalls(stallsData);
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

  const toggleStallSelection = (stall: Stall) => {
    if (stall.is_reserved) return;

    const isSelected = selectedStalls.find((s) => s.id === stall.id);
    if (isSelected) {
      setSelectedStalls(selectedStalls.filter((s) => s.id !== stall.id));
    } else {
      if (selectedStalls.length >= 3) {
        toast({
          title: "Limit reached",
          description: "You can only select up to 3 stalls per booking",
          variant: "destructive",
        });
        return;
      }
      setSelectedStalls([...selectedStalls, stall]);
    }
  };

  const handleConfirmBooking = async () => {
    if (!user) return;
    
    setBookingLoading(true);
    try {
      // Create reservations with QR codes
      const reservationsWithQR = await Promise.all(
        selectedStalls.map(async (stall) => {
          const qrCode = await generateQRCodeForReservation(
            `temp-${Date.now()}`,
            stall.name,
            hall?.name || ""
          );

          return {
            user_id: user.id,
            stall_id: stall.id,
            status: "CONFIRMED" as const,
            qr_code: qrCode,
          };
        })
      );

      await mockApi.createReservations(reservationsWithQR);

      // Update stalls as reserved
      for (const stall of selectedStalls) {
        await mockApi.updateStall(stall.id, { is_reserved: true });
      }

      toast({
        title: "Success!",
        description: "Your stalls have been booked successfully. Check My Bookings to view your QR codes.",
      });

      setShowConfirmation(false);
      navigate("/my-bookings");
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setBookingLoading(false);
    }
  };

  const totalPrice = selectedStalls.reduce((sum, stall) => sum + Number(stall.price), 0);

  const getSizeColor = (size: string) => {
    switch (size) {
      case "SMALL":
        return "bg-green-500";
      case "MEDIUM":
        return "bg-amber-500";
      case "LARGE":
        return "bg-red-500";
      default:
        return "bg-gray-500";
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!hall) return null;

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      <nav className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate("/halls")}>
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div>
              <h1 className="text-2xl font-bold">{hall.name}</h1>
              <p className="text-sm text-muted-foreground">{hall.description}</p>
            </div>
          </div>
          <Button variant="ghost" onClick={signOut}>
            Sign Out
          </Button>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-4 gap-6">
          {/* Stall Selection Grid */}
          <div className="lg:col-span-3">
            <Card>
              <CardHeader>
                <CardTitle>Select Your Stalls</CardTitle>
                <CardDescription>
                  {hall.id === "hall-a"
                    ? "Octagon stall plan inspired by the default Hall A map. Click on available stalls to select (max 3)."
                    : "Cinema-style layout - Click on available stalls to select (max 3)"}
                </CardDescription>
                
                {/* Legend */}
                <div className="flex flex-wrap gap-4 mt-4">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-green-500 rounded" />
                    <span className="text-sm">Small</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-amber-500 rounded" />
                    <span className="text-sm">Medium</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-red-500 rounded" />
                    <span className="text-sm">Large</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-gray-300 rounded" />
                    <span className="text-sm">Reserved</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-primary rounded" />
                    <span className="text-sm">Selected</span>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                {/* Stage */}
                <div className="bg-gradient-to-b from-primary/20 to-primary/5 p-4 rounded-t-lg text-center mb-4">
                  <p className="text-sm font-medium">STAGE / ENTRANCE</p>
                </div>

                {/* Grid */}
                <div className="space-y-2">
                  {Array.from({ length: hall.rows }).map((_, rowIndex) => (
                    <div key={rowIndex} className="flex gap-2 justify-center">
                      {Array.from({ length: hall.columns }).map((_, colIndex) => {
                        const stall = stalls.find(
                          (s) => s.row_position === rowIndex && s.col_position === colIndex
                        );

                        if (!stall) {
                          return <div key={colIndex} className="w-16 h-16" />;
                        }

                        const isSelected = selectedStalls.find((s) => s.id === stall.id);

                        return (
                          <button
                            key={stall.id}
                            onClick={() => toggleStallSelection(stall)}
                            disabled={stall.is_reserved}
                            className={`
                              w-16 h-16 rounded flex flex-col items-center justify-center text-xs font-medium
                              transition-all hover:scale-105 disabled:cursor-not-allowed disabled:hover:scale-100
                              ${stall.is_reserved ? "bg-gray-300 text-gray-500" : getSizeColor(stall.size) + " text-white"}
                              ${isSelected ? "ring-4 ring-primary ring-offset-2" : ""}
                            `}
                          >
                            <span>{stall.name}</span>
                            <span className="text-xs">LKR {stall.price}</span>
                          </button>
                        );
                      })}
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Booking Summary Sidebar */}
          <div className="lg:col-span-1">
            <Card className="sticky top-4">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <ShoppingCart className="w-5 h-5" />
                  Booking Summary
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <p className="text-sm font-medium">Selected Stalls ({selectedStalls.length}/3)</p>
                    {selectedStalls.length === 0 ? (
                      <p className="text-sm text-muted-foreground">No stalls selected</p>
                    ) : (
                      selectedStalls.map((stall) => (
                        <div key={stall.id} className="flex justify-between items-center text-sm border-b pb-2">
                          <span>{stall.name}</span>
                          <Badge variant="outline">{stall.size}</Badge>
                        </div>
                      ))
                    )}
                  </div>

                  <div className="border-t pt-4">
                    <div className="flex justify-between font-bold">
                      <span>Total:</span>
                      <span>LKR {totalPrice.toLocaleString()}</span>
                    </div>
                  </div>

                  <Button 
                    className="w-full" 
                    disabled={selectedStalls.length === 0}
                    onClick={() => setShowConfirmation(true)}
                  >
                    Proceed to Book
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      {/* Confirmation Dialog */}
      <Dialog open={showConfirmation} onOpenChange={setShowConfirmation}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Your Booking</DialogTitle>
            <DialogDescription>
              Please review your booking details before confirming.
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4">
            <div>
              <p className="font-medium mb-2">Selected Stalls:</p>
              {selectedStalls.map((stall) => (
                <div key={stall.id} className="flex justify-between text-sm py-1">
                  <span>{stall.name} ({stall.size})</span>
                  <span>LKR {stall.price}</span>
                </div>
              ))}
            </div>
            
            <div className="border-t pt-2">
              <div className="flex justify-between font-bold">
                <span>Total Amount:</span>
                <span>LKR {totalPrice.toLocaleString()}</span>
              </div>
            </div>

            <p className="text-sm text-muted-foreground">
              A QR code will be generated for each booking.
            </p>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setShowConfirmation(false)} disabled={bookingLoading}>
              Cancel
            </Button>
            <Button onClick={handleConfirmBooking} disabled={bookingLoading}>
              {bookingLoading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Processing...
                </>
              ) : (
                "Confirm Booking"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default HallBooking;
