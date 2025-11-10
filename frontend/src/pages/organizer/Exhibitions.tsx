import { useState } from "react";
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

type ExhibitionCard = Exhibition & {
  venue?: string;
  halls?: number;
  stalls?: number;
  dateRange?: string;
  description?: string;
};

const mockExhibitions: ExhibitionCard[] = [
  {
    id: "exp-2025-01",
    organizerId: 1,
    exhibitionName: "Colombo International Bookfair 2025",
    startDateTime: "2025-01-12T10:00:00",
    endDateTime: "2025-01-20T18:00:00",
    bookingOpenDateTime: "2024-12-20T09:00:00",
    bookingCloseDateTime: "2025-01-05T18:00:00",
    stallsPerPerson: 3,
    venue: "Hall A - Main Arena",
    halls: 6,
    stalls: 320,
    dateRange: "Jan 12 - Jan 20, 2025",
    status: "Planning",
    description:
      "Flagship annual bookfair bringing together publishers and vendors across the region.",
  },
  {
    id: "exp-2024-02",
    organizerId: 1,
    exhibitionName: "Summer Reading Expo",
    startDateTime: "2024-08-05T10:00:00",
    endDateTime: "2024-08-11T18:00:00",
    bookingOpenDateTime: "2024-07-01T09:00:00",
    bookingCloseDateTime: "2024-07-20T18:00:00",
    stallsPerPerson: 2,
    venue: "Hall C & D",
    halls: 4,
    stalls: 180,
    dateRange: "Aug 05 - Aug 11, 2024",
    status: "Active",
    description:
      "Seasonal exhibition focusing on academic texts and university partnerships.",
  },
  {
    id: "exp-2023-03",
    organizerId: 1,
    exhibitionName: "Children's Literature Week",
    startDateTime: "2023-11-18T10:00:00",
    endDateTime: "2023-11-23T16:00:00",
    bookingOpenDateTime: "2023-10-01T09:00:00",
    bookingCloseDateTime: "2023-10-20T18:00:00",
    stallsPerPerson: 1,
    venue: "Hall B",
    halls: 2,
    stalls: 95,
    dateRange: "Nov 18 - Nov 23, 2023",
    status: "Archived",
    description:
      "Past event celebrating children’s authors with interactive workshops.",
  },
];

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
  const [exhibitions, setExhibitions] = useState<ExhibitionCard[]>(
    mockExhibitions,
  );

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
      setExhibitions((prev) => [
        {
          ...created,
          status: created.status || "Planning",
          venue: "Venue TBD",
          halls: 0,
          stalls: created.stallsPerPerson,
          dateRange: `${dayjs(created.startDateTime).format(
            "MMM DD",
          )} - ${dayjs(created.endDateTime).format("MMM DD, YYYY")}`,
          description:
            "New exhibition. Use the edit action to add more details.",
        },
        ...prev,
      ]);
      closeModal();
    } catch (error) {
      toast({
        title: "Failed to create exhibition",
        description:
          error instanceof Error ? error.message : "Please try again later.",
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
                    <span>
                      {expo.dateRange ||
                        `${dayjs(expo.startDateTime).format(
                          "MMM DD",
                        )} - ${dayjs(expo.endDateTime).format("MMM DD, YYYY")}`}
                    </span>
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
