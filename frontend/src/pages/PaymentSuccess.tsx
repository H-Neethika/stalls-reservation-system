import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle, Home } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useGenreFlow } from "@/store/useGenreFlow";

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const { setOpenGenrePopup } = useGenreFlow();

  const goHomeWithPopup = () => {
    setOpenGenrePopup(true); // 🔥 Tell MyBookings to open popup immediately
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5 flex items-center justify-center px-4">
      <Card className="max-w-md w-full text-center shadow-lg">
        <CardHeader>
          <div className="flex justify-center">
            <CheckCircle className="h-12 w-12 text-green-600" />
          </div>
          <CardTitle className="text-2xl mt-2">Payment Successful</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-muted-foreground">
            Your booking payment was processed successfully.
          </p>
          <div className="flex flex-col gap-3">
            <Button onClick={() => navigate("/my-bookings")}>
              View My Bookings
            </Button>
            <Button variant="outline" onClick={goHomeWithPopup}>
              <Home className="h-4 w-4 mr-2" />
              Return Home
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentSuccess;


