import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { User, mockApi, initializeMockData } from "@/lib/mockData";
import { useToast } from "@/hooks/use-toast";

interface AuthContextType {
  user: User | null;
  userRole: string | null;
  loading: boolean;
  signUp: (email: string, password: string, name: string, organizationName: string, role: string) => Promise<{ error: any }>;
  signIn: (email: string, password: string) => Promise<{ error: any }>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [userRole, setUserRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    // Initialize mock data
    initializeMockData();
    
    // Check for existing session
    const currentUser = mockApi.getCurrentUser();
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
      const { user: newUser, error } = await mockApi.signUp(
        email,
        name,
        organizationName,
        role as "vendor" | "organizer"
      );

      if (error) {
        toast({
          title: "Error",
          description: error,
          variant: "destructive",
        });
        return { error };
      }

      if (newUser) {
        setUser(newUser);
        setUserRole(newUser.role);
        toast({
          title: "Success",
          description: "Account created successfully!",
        });
      }

      return { error: null };
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
      return { error };
    }
  };

  const signIn = async (email: string, password: string) => {
    try {
      const { user: authenticatedUser, error } = await mockApi.signIn(email, password);

      if (error) {
        toast({
          title: "Error",
          description: error,
          variant: "destructive",
        });
        return { error };
      }

      if (authenticatedUser) {
        setUser(authenticatedUser);
        setUserRole(authenticatedUser.role);
        toast({
          title: "Success",
          description: "Signed in successfully!",
        });
      }

      return { error: null };
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
      return { error };
    }
  };

  const signOut = async () => {
    await mockApi.signOut();
    setUser(null);
    setUserRole(null);
    toast({
      title: "Signed out",
      description: "You have been signed out successfully.",
    });
  };

  return (
    <AuthContext.Provider
      value={{ user, userRole, loading, signUp, signIn, signOut }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
