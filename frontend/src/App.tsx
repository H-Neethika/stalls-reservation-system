import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Auth from "./pages/Auth";
import OAuth2Callback from "./pages/OAuth2Callback";
import HallsList from "./pages/HallsList";
import HallBooking from "./pages/HallBooking";
import VendorExhibitionBooking from "./pages/VendorExhibitionBooking";
import MyBookings from "./pages/MyBookings";
import OrganizerDashboard from "./pages/organizer/Dashboard";
import OrganizerExhibitions from "./pages/organizer/Exhibitions";
import OrganizerSettings from "./pages/organizer/Settings";
import ManageHalls from "./pages/organizer/ManageHalls";
import ManageStalls from "./pages/organizer/ManageStalls";
import HallDesigner from "./pages/organizer/HallDesigner";
import AllReservations from "./pages/organizer/AllReservations";
import NotFound from "./pages/NotFound";
import { AuthProvider } from "./contexts/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import { useAuth } from "./hooks/use-auth";
import { Loader2 } from "lucide-react";

const RootRoute = () => {
  const { userRole, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (userRole?.toUpperCase() === "ORGANIZER") {
    return <Navigate to="/organizer/dashboard" replace />;
  }

  return <Home />;
};

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<RootRoute />} />
            <Route path="/auth" element={<Auth />} />
            <Route path="/oauth2/callback" element={<OAuth2Callback />} />

            {/* Vendor Routes */}
            <Route
              path="/halls"
              element={
                <ProtectedRoute requiredRole="vendor">
                  <HallsList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/halls/:hallId"
              element={
                <ProtectedRoute requiredRole="vendor">
                  <HallBooking />
                </ProtectedRoute>
              }
            />
            <Route
              path="/exhibitions/:exhibitionId/reserve"
              element={
                <ProtectedRoute requiredRole="vendor">
                  <VendorExhibitionBooking />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-bookings"
              element={
                <ProtectedRoute requiredRole="vendor">
                  <MyBookings />
                </ProtectedRoute>
              }
            />

            {/* Organizer Routes */}
            <Route
              path="/organizer/dashboard"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <OrganizerDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/halls"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <ManageHalls />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/stalls"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <ManageStalls />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/halls/design"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <HallDesigner />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/stalls/create"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <HallDesigner />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/reservations"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <AllReservations />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/exhibitions"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <OrganizerExhibitions />
                </ProtectedRoute>
              }
            />
            <Route
              path="/organizer/settings"
              element={
                <ProtectedRoute requiredRole="organizer">
                  <OrganizerSettings />
                </ProtectedRoute>
              }
            />

            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
