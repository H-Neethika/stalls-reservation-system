import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { apiService } from "@/services/api";
import { useToast } from "@/hooks/use-toast";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Loader2 } from "lucide-react";

const OAuth2Callback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading"
  );

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Check for errors first
        const error = searchParams.get("error");
        const errorMessage = searchParams.get("message");

        if (error) {
          setStatus("error");

          // Handle specific error types with user-friendly messages
          let displayMessage = errorMessage || "OAuth2 authentication failed";

          if (error === "account_already_exists") {
            displayMessage = decodeURIComponent(
              errorMessage ||
                "An account with this email already exists. Please sign in instead."
            );
          } else if (error === "account_not_found") {
            displayMessage = decodeURIComponent(
              errorMessage ||
                "No account found with this email. Please sign up first."
            );
          } else if (error === "email_not_found") {
            displayMessage = "Unable to retrieve email from OAuth provider.";
          }

          toast({
            title: "Authentication Failed",
            description: displayMessage,
            variant: "destructive",
          });

          // Redirect to auth page after showing error
          setTimeout(() => {
            navigate("/auth");
          }, 3000);
          return;
        }

        // Check for direct token parameters (OAuth2 success redirect)
        const accessToken = searchParams.get("accessToken");
        const refreshToken = searchParams.get("refreshToken");
        const userId = searchParams.get("userId");

        if (accessToken && refreshToken && userId) {
          // Direct token-based authentication (already authenticated by backend)
          // Store tokens
          localStorage.setItem("accessToken", accessToken);
          localStorage.setItem("refreshToken", refreshToken);

          // Decode JWT to get user info
          const tokenPayload = JSON.parse(atob(accessToken.split(".")[1]));
          const user = {
            id: parseInt(userId),
            email: tokenPayload.sub,
            name: tokenPayload.name,
            role: tokenPayload.role,
            organizationName: tokenPayload.organization || "",
          };

          localStorage.setItem("user", JSON.stringify(user));

          setStatus("success");
          toast({
            title: "Success",
            description: "Signed in successfully with OAuth2!",
          });

          // Redirect based on user role
          setTimeout(() => {
            const redirectPath =
              user.role === "ORGANIZER" ? "/organizer/dashboard" : "/halls";
            navigate(redirectPath);
            // Force page reload to update auth context
            window.location.href = redirectPath;
          }, 1000);
        } else {
          throw new Error("Missing authentication tokens");
        }
      } catch (error) {
        setStatus("error");
        const errorMessage =
          error instanceof Error
            ? error.message
            : "OAuth2 authentication failed";
        toast({
          title: "Error",
          description: errorMessage,
          variant: "destructive",
        });

        // Redirect to login page after error
        setTimeout(() => {
          navigate("/auth");
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, navigate, toast]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 via-background to-accent/5 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">
            {status === "loading" && "Completing Sign In..."}
            {status === "success" && "Success!"}
            {status === "error" && "Authentication Failed"}
          </CardTitle>
          <CardDescription>
            {status === "loading" &&
              "Please wait while we complete your authentication"}
            {status === "success" && "Redirecting you to the application..."}
            {status === "error" && "Redirecting you back to login..."}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex justify-center">
          {status === "loading" && (
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
          )}
          {status === "success" && (
            <div className="text-center">
              <svg
                className="h-12 w-12 text-green-500 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          )}
          {status === "error" && (
            <div className="text-center">
              <svg
                className="h-12 w-12 text-red-500 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default OAuth2Callback;
