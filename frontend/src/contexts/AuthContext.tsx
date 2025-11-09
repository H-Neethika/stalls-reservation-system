import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import { User } from "@/types";
import { apiService } from "@/services/api";
import { useToast } from "@/hooks/use-toast";

interface AuthContextType {
  user: User | null;
  userRole: string | null;
  loading: boolean;
  signUp: (
    email: string,
    password: string,
    name: string,
    organizationName: string,
    role: string
  ) => Promise<{ error: string | null }>;
  signIn: (
    email: string,
    password: string
  ) => Promise<{ error: string | null }>;
  signInWithOAuth: (provider: "google" | "github") => void;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export { AuthContext };

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [userRole, setUserRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    // Check for existing session
    const currentUser = apiService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
      setUserRole(currentUser.role);
    }
    setLoading(false);
  }, []);

  const signUp = async (
    email: string,
    password: string,
    name: string,
    organizationName: string,
    role: string
  ) => {
    try {
      const newUser = await apiService.register({
        email,
        password,
        name,
        organizationName,
        role: role.toUpperCase() as "VENDOR" | "ORGANIZER",
      });

      if (newUser) {
        // After registration, automatically sign them in
        const authResponse = await apiService.login({ email, password });
        setUser(authResponse.user);
        setUserRole(authResponse.user.role);
        toast({
          title: "Success",
          description: "Account created successfully!",
        });
      }

      return { error: null };
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : "Registration failed";
      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      });
      return { error: errorMessage };
    }
  };

  const signIn = async (email: string, password: string) => {
    try {
      const authResponse = await apiService.login({ email, password });

      if (authResponse.user) {
        setUser(authResponse.user);
        setUserRole(authResponse.user.role);
        toast({
          title: "Success",
          description: "Signed in successfully!",
        });
      }

      return { error: null };
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : "Login failed";
      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      });
      return { error: errorMessage };
    }
  };

  const signInWithOAuth = (provider: "google" | "github") => {
    apiService.initiateOAuth2Login(provider);
  };

  const signOut = async () => {
    await apiService.logout();
    setUser(null);
    setUserRole(null);
    toast({
      title: "Signed out",
      description: "You have been signed out successfully.",
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        userRole,
        loading,
        signUp,
        signIn,
        signInWithOAuth,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
