import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useAuth } from "@/hooks/use-auth";

const OrganizerSettings = () => {
  const { user } = useAuth();

  return (
    <OrganizerLayout title="Settings">
      <div className="max-w-3xl mx-auto space-y-8">
        <div className="text-center">
          <h2 className="text-3xl font-bold mb-2">Account Settings</h2>
          <p className="text-muted-foreground">
            Manage your profile information and preferences
          </p>
        </div>

        {user ? (
          <Card>
            <CardHeader>
              <CardTitle>Your Profile</CardTitle>
              <CardDescription>
                Details pulled from the authentication store
              </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-6 sm:grid-cols-2">
              <div>
                <p className="text-muted-foreground text-sm mb-1">Name</p>
                <p className="font-medium">{user.name}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm mb-1">Email</p>
                <p className="font-medium break-words">{user.email}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm mb-1">
                  Organization
                </p>
                <p className="font-medium">
                  {user.organizationName || "Not specified"}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm mb-1">Role</p>
                <p className="font-medium capitalize">
                  {user.role.toLowerCase()}
                </p>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardContent className="py-6 text-center text-muted-foreground">
              Unable to load profile details. Please refresh the page.
            </CardContent>
          </Card>
        )}
      </div>
    </OrganizerLayout>
  );
};

export default OrganizerSettings;

