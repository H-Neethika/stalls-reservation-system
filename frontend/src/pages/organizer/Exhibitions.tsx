import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import dayjs, { Dayjs } from "dayjs";
import { Modal, Form, Input, DatePicker, InputNumber } from "antd";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { CalendarDays, Eye, MapPin, Pencil, Users } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";
import { exhibitionService } from "@/services/exhibitionService";
import { CreateExhibitionRequest, Exhibition } from "@/types";

type ApiError = Error & {
  code?: number;
  responseBody?: unknown;
};

type ExhibitionCard = Exhibition & {
  dateRange?: string;
};

const statusVariant: Record<string, "default" | "secondary" | "outline"> = {
  Planning: "secondary",
  Active: "default",
  Archived: "outline",
};

const OrganizerExhibitions = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form] = Form.useForm();
  const [exhibitions, setExhibitions] = useState<ExhibitionCard[]>([]);
  const [isFetching, setIsFetching] = useState(false);

  const toCard = (expo: Exhibition): ExhibitionCard => ({
    ...expo,
    status: expo.status || "Planning",
    venue: expo.venue || "Venue TBD",
    halls: expo.halls ?? 0,
    stalls: expo.stalls ?? expo.stallsPerPerson,
    description:
      expo.description ||
      "Add more context and logistics details from the edit screen.",
    dateRange: `${dayjs(expo.startDateTime).format("MMM DD")} - ${dayjs(
      expo.endDateTime,
    ).format("MMM DD, YYYY")}`,
  });

  useEffect(() => {
    if (!user?.id) {
      setExhibitions([]);
      return;
    }

    const fetchExhibitions = async () => {
      setIsFetching(true);
      try {
        const data = await exhibitionService.getExhibitionsByOrganizer(
          user.id,
        );
        setExhibitions(data.map(toCard));
      } catch (error) {
        const err = error as ApiError;
        const backendMessage =
          typeof err.responseBody === "string"
            ? err.responseBody
            : err.responseBody && typeof err.responseBody === "object"
            ? (err.responseBody as { message?: string; error?: string })
                .message ??
              (err.responseBody as { message?: string; error?: string }).error
            : null;

        toast({
          title: "Failed to load exhibitions",
          description:
            backendMessage ||
            err.message ||
            "Please try again later or refresh the page.",
          variant: "destructive",
        });
      } finally {
        setIsFetching(false);
      }
    };

    fetchExhibitions();
  }, [user?.id, toast]);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => {
    setIsModalOpen(false);
    form.resetFields();
  };

  const formatDate = (value: Dayjs) => value.format("YYYY-MM-DDTHH:mm:ss");

  const handleCreateExhibition = async (values: {
    exhibitionName: string;
    startDateTime: Dayjs;
    endDateTime: Dayjs;
    bookingOpenDateTime: Dayjs;
    bookingCloseDateTime: Dayjs;
    stallsPerPerson: number;
  }) => {
    if (!user?.id) {
      toast({
        title: "Authentication required",
        description: "Please sign in again to create exhibitions.",
        variant: "destructive",
      });
      return;
    }

    const payload: CreateExhibitionRequest = {
      organizerId: user.id,
      exhibitionName: values.exhibitionName,
      startDateTime: formatDate(values.startDateTime),
      endDateTime: formatDate(values.endDateTime),
      bookingOpenDateTime: formatDate(values.bookingOpenDateTime),
      bookingCloseDateTime: formatDate(values.bookingCloseDateTime),
      stallsPerPerson: values.stallsPerPerson,
    };

    setIsSubmitting(true);
    try {
      const created = await exhibitionService.createExhibition(payload);
      toast({
        title: "Exhibition created",
        description: `${created.exhibitionName} has been created successfully.`,
      });
      setExhibitions((prev) => [toCard(created), ...prev]);
      closeModal();
    } catch (error: unknown) {
      const err = error as ApiError;
      const backendMessage =
        typeof err.responseBody === "string"
          ? err.responseBody
          : err.responseBody && typeof err.responseBody === "object"
          ? (err.responseBody as { message?: string; error?: string }).message ??
            (err.responseBody as { message?: string; error?: string }).error
          : null;

      const description =
        backendMessage ||
        err.message ||
        "Something went wrong while creating the exhibition.";

      toast({
        title: "Failed to create exhibition",
        description,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <OrganizerLayout title="Exhibitions">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-3xl font-bold">Manage Exhibitions</h2>
            <p className="text-muted-foreground">
              Create and maintain upcoming exhibition schedules.
            </p>
          </div>
          <Button onClick={openModal}>Create Exhibition</Button>
        </div>

        {isFetching ? (
          <Card className="border-dashed">
            <CardContent className="py-8 text-center text-muted-foreground">
              Loading exhibitions...
            </CardContent>
          </Card>
        ) : exhibitions.length === 0 ? (
          <Card className="border-dashed">
            <CardContent className="py-8 text-center text-muted-foreground">
              No exhibitions yet. Use &ldquo;Create Exhibition&rdquo; to add
              your first event.
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {exhibitions.map((expo) => (
              <Card key={expo.id} className="flex flex-col">
                <CardHeader className="space-y-3">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <CardTitle>{expo.exhibitionName}</CardTitle>
                      <CardDescription>{expo.venue || "Venue TBD"}</CardDescription>
                    </div>
                    <Badge
                      variant={
                        statusVariant[expo.status || "Planning"] || "outline"
                      }
                    >
                      {expo.status || "Planning"}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {expo.description ||
                      "Add more context and logistics details from the edit screen."}
                  </p>
                </CardHeader>
                <CardContent className="flex flex-1 flex-col gap-4">
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <CalendarDays className="h-4 w-4" />
                      <span>{expo.dateRange}</span>
                    </div>
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <MapPin className="h-4 w-4" />
                      <span>{expo.halls ?? 0} halls</span>
                    </div>
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <Users className="h-4 w-4" />
                      <span>{expo.stalls ?? expo.stallsPerPerson} stalls</span>
                    </div>
                  </div>

                  <div className="mt-auto flex gap-3">
                    <Button
                      variant="outline"
                      className="flex-1 flex items-center justify-center gap-2"
                      onClick={() =>
                        navigate(`/organizer/exhibitions/${expo.id}/edit`)
                      }
                    >
                      <Pencil className="h-4 w-4" />
                      Edit
                    </Button>
                    <Button
                      className="flex-1 flex items-center justify-center gap-2"
                      onClick={() =>
                        navigate(`/organizer/exhibitions/${expo.id}`)
                      }
                    >
                      <Eye className="h-4 w-4" />
                      View
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>

      <Modal
        title="Create Exhibition"
        open={isModalOpen}
        onCancel={closeModal}
        footer={null}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={handleCreateExhibition}
          initialValues={{ stallsPerPerson: 1 }}
        >
          <Form.Item
            label="Exhibition Name"
            name="exhibitionName"
            rules={[{ required: true, message: "Enter the exhibition name" }]}
          >
            <Input placeholder="Enter exhibition title" />
          </Form.Item>
          <Form.Item
            label="Start Date & Time"
            name="startDateTime"
            rules={[{ required: true, message: "Select a start date and time" }]}
          >
            <DatePicker showTime className="w-full" />
          </Form.Item>
          <Form.Item
            label="End Date & Time"
            name="endDateTime"
            dependencies={["startDateTime"]}
            rules={[
              { required: true, message: "Select an end date and time" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const start = getFieldValue("startDateTime");
                  if (!value || !start || value.isAfter(start)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error("End date must be after the start date"),
                  );
                },
              }),
            ]}
          >
            <DatePicker showTime className="w-full" />
          </Form.Item>
          <Form.Item
            label="Booking Opens"
            name="bookingOpenDateTime"
            rules={[{ required: true, message: "Select when booking opens" }]}
          >
            <DatePicker showTime className="w-full" />
          </Form.Item>
          <Form.Item
            label="Booking Closes"
            name="bookingCloseDateTime"
            dependencies={["bookingOpenDateTime"]}
            rules={[
              { required: true, message: "Select when booking closes" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const open = getFieldValue("bookingOpenDateTime");
                  if (!value || !open || value.isAfter(open)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error("Close date must be after the open date"),
                  );
                },
              }),
            ]}
          >
            <DatePicker showTime className="w-full" />
          </Form.Item>
          <Form.Item
            label="Stalls Per Person"
            name="stallsPerPerson"
            rules={[{ required: true, message: "Enter stalls per person" }]}
          >
            <InputNumber min={1} className="w-full" />
          </Form.Item>
          <Form.Item className="mb-0">
            <Button
              type="submit"
              className="w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating..." : "Create Exhibition"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </OrganizerLayout>
  );
};

export default OrganizerExhibitions;
