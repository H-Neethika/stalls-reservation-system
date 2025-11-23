import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { BookOpen, Users, Building2, Calendar, LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";

const literaryGenres = [
  { name: "Fiction", icon: "📚", description: "Novels and short stories" },
  {
    name: "Non-Fiction",
    icon: "📖",
    description: "Biography, history, and more",
  },
  { name: "Poetry", icon: "✍️", description: "Verse and lyrical works" },
  { name: "Children's", icon: "🎨", description: "Books for young readers" },
  { name: "Academic", icon: "🎓", description: "Educational and scholarly" },
  { name: "Comics", icon: "💭", description: "Graphic novels and manga" },
];

const Home = () => {
  const navigate = useNavigate();
  const { user, userRole, signOut } = useAuth();

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/5">
      {/* Top Navigation Bar - Only shown when logged in */}
      {user && (
        <div className="bg-card/80 backdrop-blur-sm border-b sticky top-0 z-50">
          <div className="container mx-auto px-4 py-4 flex justify-between items-center">
            <div className="font-semibold">
              Welcome, <span className="text-primary">{user.name}</span>
            </div>
            <Button variant="ghost" onClick={signOut}>
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      )}

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="text-center mb-12 animate-fade-in">
          <h1 className="text-5xl md:text-6xl font-bold mb-6 bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
            Colombo International Bookfair
          </h1>
          <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
            Sri Lanka's largest book fair and exhibition. Reserve your stall
            today and showcase your books to thousands of readers.
          </p>

          {!user ? (
            <div className="flex gap-4 justify-center">
              <Button
                size="lg"
                onClick={() => navigate("/auth")}
                className="text-lg px-8"
              >
                Get Started
              </Button>
              <Button
                size="lg"
                variant="outline"
                onClick={() => navigate("/auth")}
                className="text-lg px-8"
              >
                Sign In
              </Button>
            </div>
          ) : (
            <Button
              size="lg"
              onClick={() =>
                navigate(
                  userRole === "organizer" ? "/organizer/dashboard" : "/halls"
                )
              }
              className="text-lg px-8"
            >
              {userRole === "organizer" ? "Go to Dashboard" : "Browse Exhibitions"}
            </Button>
          )}
        </div>

        {/* Stats */}
        <div className="grid md:grid-cols-3 gap-6 mb-16">
          <Card className="text-center hover-scale">
            <CardHeader>
              <Users className="w-12 h-12 mx-auto mb-4 text-primary" />
              <CardTitle>1000+ Vendors</CardTitle>
              <CardDescription>Book publishers and sellers</CardDescription>
            </CardHeader>
          </Card>
          <Card className="text-center hover-scale">
            <CardHeader>
              <Building2 className="w-12 h-12 mx-auto mb-4 text-primary" />
              <CardTitle>Multiple Halls</CardTitle>
              <CardDescription>
                Custom designed exhibition spaces
              </CardDescription>
            </CardHeader>
          </Card>
          <Card className="text-center hover-scale">
            <CardHeader>
              <Calendar className="w-12 h-12 mx-auto mb-4 text-primary" />
              <CardTitle>10 Days Event</CardTitle>
              <CardDescription>Experience the book culture</CardDescription>
            </CardHeader>
          </Card>
        </div>
      </section>

      {/* Literary Genres Section */}
      <section className="bg-card/50 py-16">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <BookOpen className="w-16 h-16 mx-auto mb-4 text-primary" />
            <h2 className="text-4xl font-bold mb-4">Explore Literary Genres</h2>
            <p className="text-muted-foreground text-lg">
              Discover diverse categories of books at the exhibition
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {literaryGenres.map((genre, index) => (
              <Card
                key={genre.name}
                className="hover-scale cursor-pointer transition-all hover:shadow-lg"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <CardHeader>
                  <div className="text-4xl mb-4">{genre.icon}</div>
                  <CardTitle>{genre.name}</CardTitle>
                  <CardDescription>{genre.description}</CardDescription>
                </CardHeader>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="container mx-auto px-4 py-20">
        <Card className="bg-gradient-to-r from-primary/10 to-accent/10 border-0">
          <CardContent className="p-12 text-center">
            <h2 className="text-3xl font-bold mb-4">
              Ready to Reserve Your Stall?
            </h2>
            <p className="text-muted-foreground mb-8 text-lg max-w-2xl mx-auto">
              Join hundreds of book publishers and vendors at Sri Lanka's
              premier book fair. Easy online booking with instant confirmation
              and QR code generation.
            </p>
            <Button
              size="lg"
              onClick={() => navigate(user ? "/halls" : "/auth")}
              className="text-lg px-12"
            >
              {user ? "Browse Available Exhibitions" : "Register Now"}
            </Button>
          </CardContent>
        </Card>
      </section>
    </div>
  );
};

export default Home;
