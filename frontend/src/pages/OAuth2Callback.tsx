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
        const code = searchParams.get("code");
        const state = searchParams.get("state");
        const error = searchParams.get("error");

        if (error) {
          throw new Error(`OAuth2 error: ${error}`);
        }

        if (!code || !state) {
          throw new Error("Missing OAuth2 parameters");
        }

        // Handle the OAuth2 callback
        const authResponse = await apiService.handleOAuth2Callback(code, state);

        if (authResponse.user) {
          setStatus("success");
          toast({
            title: "Success",
            description: "Signed in successfully with OAuth2!",
          });

          // Redirect based on user role
          setTimeout(() => {
            const redirectPath =
              authResponse.user.role === "ORGANIZER"
                ? "/organizer/dashboard"
                : "/halls";
            navigate(redirectPath);
          }, 1000);
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
