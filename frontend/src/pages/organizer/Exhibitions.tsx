import { useEffect, useMemo, useState } from "react";
import dayjs, { Dayjs } from "dayjs";
import {
  Modal,
  Form,
  Input,
  DatePicker,
  InputNumber,
  Table,
  Space,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { OrganizerLayout } from "@/components/organizer/OrganizerLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Eye, Pencil, Trash2, AlertTriangle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/hooks/use-auth";
import { exhibitionService } from "@/services/exhibitionService";
import { hallService } from "@/services/hallService";
import { stallTypeService } from "@/services/stallTypeService";
import { CreateExhibitionRequest, Exhibition } from "@/types";
import { Checkbox, Divider } from "antd";
import type { CheckboxValueType } from "antd/es/checkbox/Group";

type ApiError = Error & {
  code?: number;
  responseBody?: unknown;
};

type ExhibitionCard = Exhibition & {
  dateRange?: string;
  bookingWindow?: string;
};

const stateVariant: Record<string, "default" | "secondary" | "outline"> = {
  DRAFT: "secondary",
  PLANNING: "secondary",
  ACTIVE: "default",
  PUBLISHED: "default",
  ARCHIVED: "outline",
};

const extractErrorSection = (body: unknown): string | null => {
  if (!body || typeof body !== "object") {
    return null;
  }

  const obj = body as Record<string, unknown>;
  if (!("error" in obj)) {
    return null;
  }

  const section = obj.error;
  if (typeof section === "string") {
    return section;
  }

  if (section && typeof section === "object") {
    const nested = section as Record<string, unknown>;
    if (typeof nested.message === "string") {
      return nested.message;
    }
    if (typeof nested.detail === "string") {
      return nested.detail;
    }
    const firstString = Object.values(nested).find(
      (value): value is string => typeof value === "string",
    );
    if (firstString) {
      return firstString;
    }
    try {
      return JSON.stringify(nested);
    } catch {
      return null;
    }
  }

  return null;
};

const OrganizerExhibitions = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form] = Form.useForm();
  const [exhibitions, setExhibitions] = useState<ExhibitionCard[]>([]);
  const [isFetching, setIsFetching] = useState(false);
  const [selectedExhibition, setSelectedExhibition] =
    useState<ExhibitionCard | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [halls, setHalls] = useState<{ id: number; label: string }[]>([]);
  const [stallTypes, setStallTypes] = useState<{ id: number; label: string }[]>([]);
  const [selectedHallIds, setSelectedHallIds] = useState<number[]>([]);
  const [hallPrices, setHallPrices] = useState<Record<number, Record<number, number>>>({});
  const startDateValue = Form.useWatch("startDateTime", form);
  const endDateValue = Form.useWatch("endDateTime", form);
  const bookingOpenValue = Form.useWatch("bookingOpenDateTime", form);

  const toCard = (expo: Exhibition): ExhibitionCard => ({
    ...expo,
    status: expo.status || "Planning",
    exhibitionState: expo.exhibitionState || expo.status || "Planning",
    bookingWindow: `${dayjs(expo.bookingOpenDateTime).format(
      "MMM DD, YYYY",
    )} - ${dayjs(expo.bookingCloseDateTime).format("MMM DD, YYYY")}`,
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
          extractErrorSection(err.responseBody) ||
          (typeof err.responseBody === "object" &&
            err.responseBody !== null &&
            "message" in err.responseBody &&
            typeof (err.responseBody as { message?: string }).message === "string"
            ? (err.responseBody as { message: string }).message
            : typeof err.responseBody === "string"
              ? err.responseBody
              : null);

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

  useEffect(() => {
    const fetchLookups = async () => {
      try {
        const [apiHalls, apiStallTypes] = await Promise.all([
          hallService.getHalls(),
          stallTypeService.getStallTypes(),
        ]);

        const mappedHalls =
          apiHalls
            .filter((hall) => hall.id)
            .map((hall) => ({
              id: Number(hall.id),
              label: hall.hallName || `Hall ${hall.id}`,
            })) || [];

        const mappedStallTypes =
          apiStallTypes
            .filter((type) => type.id !== undefined)
            .map((type) => ({
              id: Number(type.id),
              label: type.type || type.name || type.typeName || `Type ${type.id}`,
            })) || [];

        if (mappedHalls.length) {
          setHalls(mappedHalls);
          setSelectedHallIds((prev) => (prev.length ? prev : [mappedHalls[0].id]));
        } else {
          throw new Error("No halls returned");
        }

        if (mappedStallTypes.length) {
          setStallTypes(mappedStallTypes);
        } else {
          throw new Error("No stall types returned");
        }
      } catch (error) {
        const fallbackHalls = [
          { id: 1, label: "Hall 01 (sample)" },
          { id: 2, label: "Hall 02 (sample)" },
        ];
        const fallbackStallTypes = [
          { id: 1, label: "Type 1 (sample)" },
          { id: 2, label: "Type 2 (sample)" },
          { id: 3, label: "Type 3 (sample)" },
        ];
        setHalls(fallbackHalls);
        setStallTypes(fallbackStallTypes);
        setSelectedHallIds((prev) => (prev.length ? prev : [fallbackHalls[0].id]));
      }
    };
    fetchLookups();
  }, []);

  const openModal = () => {
    setEditingId(null);
    form.resetFields();
    setIsModalOpen(true);
  };
  const closeModal = () => {
    setIsModalOpen(false);
    form.resetFields();
    setEditingId(null);
  };

  const handleViewExhibition = async (id: string) => {
    try {
      const data = await exhibitionService.getExhibitionsByOrganizer(user.id);
      setSelectedExhibition(data.map(toCard).find((expo) => expo.id === id) || null);
      setIsViewModalOpen(true);
    } catch (error) {
      const err = error as ApiError;
      toast({
        title: "Failed to load exhibition",
        description:
          extractErrorSection(err.responseBody) ||
          err.message ||
          "Please try again later.",
        variant: "destructive",
      });
    }
  };

  const handleEditExhibition = async (id: string) => {
    try {
      const list = await exhibitionService.getExhibitionsByOrganizer(Number(user?.id));
      const data = list.find((expo) => String(expo.id) === String(id));
      if (!data) {
        throw new Error("Exhibition not found");
      }
      setEditingId(id);
      form.setFieldsValue({
        exhibitionName: data.exhibitionName,
        startDateTime: dayjs(data.startDateTime),
        endDateTime: dayjs(data.endDateTime),
        bookingOpenDateTime: dayjs(data.bookingOpenDateTime),
        bookingCloseDateTime: dayjs(data.bookingCloseDateTime),
        stallsPerPerson: data.stallsPerPerson,
      });
      const hallIdsFromPrices =
        data.hallPrices && data.hallPrices.length > 0
          ? Array.from(new Set(data.hallPrices.map((p) => Number(p.hallId))))
          : [];
      const hallIdsFromHalls =
        Array.isArray((data as any).halls) && (data as any).halls.length > 0
          ? (data as any).halls.map((h: any) => Number(h.hallId ?? h.id ?? h))
          : [];
      const hallIds = hallIdsFromPrices.length ? hallIdsFromPrices : hallIdsFromHalls;
      if (hallIds.length) {
        setSelectedHallIds(hallIds);
      }

      const priceMap: Record<number, Record<number, number>> = {};
      const resolveTypeId = (type: any) => {
        if (type?.stallTypeId) return Number(type.stallTypeId);
        if (type?.stallType) {
          const found = stallTypes.find(
            (t) => t.label.toUpperCase() === String(type.stallType).toUpperCase(),
          );
          if (found) return found.id;
        }
        return undefined;
      };

      if (data.hallPrices && data.hallPrices.length > 0) {
        data.hallPrices.forEach((p) => {
          const hId = Number(p.hallId);
          if (!hId) return;
          const typeId = resolveTypeId(p);
          if (!typeId) return;
          priceMap[hId] = priceMap[hId] || {};
          priceMap[hId][typeId] = Number(p.price);
        });
      }

      if (Array.isArray((data as any).halls)) {
        (data as any).halls.forEach((hall: any) => {
          const hId = Number(hall.hallId ?? hall.id);
          if (!hId) return;
          if (Array.isArray(hall.prices)) {
            hall.prices.forEach((p: any) => {
              const typeId = resolveTypeId(p);
              if (!typeId) return;
              priceMap[hId] = priceMap[hId] || {};
              priceMap[hId][typeId] = Number(p.price);
            });
          }
        });
      }

      if (Object.keys(priceMap).length) {
        setHallPrices(priceMap);
      } else if (hallIds.length) {
        setHallPrices({ [hallIds[0]]: {} });
      }
      setIsModalOpen(true);
    } catch (error) {
      const err = error as ApiError;
      toast({
        title: "Failed to load exhibition",
        description:
          extractErrorSection(err.responseBody) ||
          err.message ||
          "Please try again later.",
        variant: "destructive",
      });
    }
  };

  const handleDeleteExhibition = (id: string) => {
    Modal.confirm({
      title: "Delete exhibition",
      content: "Are you sure you want to delete this exhibition?",
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await exhibitionService.deleteExhibition(id);
          setExhibitions((prev) => prev.filter((expo) => expo.id !== id));
          toast({
            title: "Exhibition deleted",
            description: "The exhibition has been deleted successfully.",
          });
        } catch (error) {
          const err = error as ApiError;
          toast({
            title: "Failed to delete exhibition",
            description:
              extractErrorSection(err.responseBody) ||
              err.message ||
              "Please try again later.",
            variant: "destructive",
          });
        }
      },
    });
  };

  const formatDate = (value: Dayjs) => value.format("YYYY-MM-DDTHH:mm:ss");

  const handleSubmitExhibition = async (values: {
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
      hallIds: selectedHallIds,
      hallPrices: selectedHallIds.flatMap((hallId) =>
        stallTypes.map((type) => ({
          hallId,
          stallTypeId: type.id,
          price: Number(hallPrices[hallId]?.[type.id] ?? 0),
        })),
      ),
    };

    setIsSubmitting(true);
    try {
      if (editingId) {
        const updated = await exhibitionService.updateExhibition(
          editingId,
          payload,
        );
        toast({
          title: "Exhibition updated",
          description: `${updated.exhibitionName} has been updated.`,
        });
        setExhibitions((prev) =>
          prev.map((expo) => (expo.id === editingId ? toCard(updated) : expo)),
        );
      } else {
        const created = await exhibitionService.createExhibition(payload);
        toast({
          title: "Exhibition created",
          description: `${payload.exhibitionName} has been created successfully.`,
        });
        const fullList = await exhibitionService.getExhibitionsByOrganizer(user.id);

        setExhibitions(fullList.map(toCard));
      }
      closeModal();
    } catch (error: unknown) {
      const err = error as ApiError;
      const backendMessage =
        extractErrorSection(err.responseBody) ||
        (typeof err.responseBody === "object" &&
          err.responseBody !== null &&
          "message" in err.responseBody &&
          typeof (err.responseBody as { message?: string }).message === "string"
          ? (err.responseBody as { message: string }).message
          : typeof err.responseBody === "string"
            ? err.responseBody
            : null);

      const description =
        backendMessage ||
        err.message ||
        "Something went wrong while creating the exhibition.";

      toast({
        title: editingId ? "Failed to update exhibition" : "Failed to create exhibition",
        description,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const columns: ColumnsType<ExhibitionCard & { key: string }> = useMemo(
    () => [
      {
        title: "Exhibition",
        dataIndex: "exhibitionName",
        key: "exhibitionName",
        render: (value) => <span className="font-semibold">{value}</span>,
      },
      {
        title: "Event Dates",
        dataIndex: "dateRange",
        key: "dateRange",
      },
      {
        title: "Booking Window",
        dataIndex: "bookingWindow",
        key: "bookingWindow",
      },
      {
        title: "Stalls / Person",
        dataIndex: "stallsPerPerson",
        key: "stallsPerPerson",
      },
      {
        title: "State",
        dataIndex: "exhibitionState",
        key: "exhibitionState",
        render: (value, record) => (
          <Badge
            variant={
              stateVariant[
              (value || record.status || "PLANNING").toString().toUpperCase()
              ] || "outline"
            }
          >
            {value || record.status || "Planning"}
          </Badge>
        ),
      },
      {
        title: "Actions",
        key: "actions",
        render: (_, record) => (
          <Space size="small">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleViewExhibition(record.id)}
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleEditExhibition(record.id)}
              disabled={
                String(record.exhibitionState || record.status || "")
                  .toUpperCase() === "PUBLISHED"
              }
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="destructive"
              size="sm"
              onClick={() => handleDeleteExhibition(record.id)}
              disabled={
                String(record.exhibitionState || record.status || "")
                  .toUpperCase() === "PUBLISHED"
              }
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <OrganizerLayout title="Exhibitions">
      <div className="flex flex-col gap-6">
        <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-amber-800 flex items-start gap-3">
          <AlertTriangle className="h-5 w-5 mt-0.5" />
          <div className="text-sm">
            <p className="font-semibold">Publishing is final</p>
            <p>Exhibitions can’t be modified once they are published.</p>
          </div>
        </div>

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
          <Card>
            <CardHeader>
              <CardTitle>Exhibitions</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <Table
                columns={columns}
                dataSource={exhibitions.map((expo) => ({
                  ...expo,
                  key: expo.id,
                }))}
                pagination={{ pageSize: 5 }}
                rowKey="key"
              />
            </CardContent>
          </Card>
        )}
      </div>

      <Modal
        title="Create Exhibition"
        open={isModalOpen}
        onCancel={closeModal}
        footer={null}
        width={960}
        styles={{ body: { maxHeight: "none", overflowY: "visible" } }}
        style={{ top: 24 }}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={handleSubmitExhibition}
          initialValues={{ stallsPerPerson: 3 }}
        >
          <div className="grid md:grid-cols-2 gap-2">
            <Form.Item
              label="Exhibition Name"
              name="exhibitionName"
              rules={[{ required: true, message: "Enter the exhibition name" }]}
            >
              <Input placeholder="Enter exhibition title" />
            </Form.Item>
            <Form.Item
              label="Stalls Per Person"
              name="stallsPerPerson"
              rules={[{ required: true, message: "Enter stalls per person" }]}
            >
              <InputNumber min={1} className="w-full" />
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
              <DatePicker
                showTime
                className="w-full"
                disabled={!startDateValue || !endDateValue}
                disabledDate={(current) => {
                  if (!startDateValue) return true;
                  const today = dayjs().startOf("day");
                  return (
                    (current && current < today) ||
                    (current && current > dayjs(startDateValue).endOf("day"))
                  );
                }}
              />
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
              <DatePicker
                showTime
                className="w-full"
                disabled={!startDateValue || !endDateValue || !bookingOpenValue}
                disabledDate={(current) => {
                  if (!bookingOpenValue || !startDateValue) return true;
                  return (
                    (current && current < dayjs(bookingOpenValue).startOf("day")) ||
                    (current && current > dayjs(startDateValue).endOf("day"))
                  );
                }}
              />
            </Form.Item>
          </div>
          <Divider orientation="left" plain>
            Halls & Pricing
          </Divider>
          <div className="space-y-4 rounded-lg border bg-muted/30 p-4">
            <p className="text-sm text-muted-foreground">
              Select one or more halls and set stall type prices for each hall. These sample halls are shown until the backend halls endpoint is wired.
            </p>
            <div className="grid gap-3">
              <Checkbox.Group
                value={selectedHallIds}
                onChange={(values: CheckboxValueType[]) =>
                  setSelectedHallIds(values.map((v) => Number(v)))
                }
                className="flex flex-col gap-3"
              >
                {halls.map((hall) => (
                  <div
                    key={hall.id}
                    className="rounded-lg border bg-white p-3 shadow-sm"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <Checkbox value={hall.id}>{hall.label}</Checkbox>
                      </div>
                      <span className="text-xs text-muted-foreground">
                        Set prices per stall type
                      </span>
                    </div>
                    {selectedHallIds.includes(hall.id) && (
                      <div className="mt-3 grid gap-3 md:grid-cols-3 sm:grid-cols-2">
                        {stallTypes.map((type) => (
                          <Form.Item
                            key={`${hall.id}-${type.id}`}
                            label={`${type.label} Price`}
                            required
                            className="mb-0"
                          >
                            <InputNumber
                              min={0}
                              className="w-full"
                              value={hallPrices[hall.id]?.[type.id] ?? 0}
                              onChange={(value) =>
                                setHallPrices((prev) => ({
                                  ...prev,
                                  [hall.id]: {
                                    ...(prev[hall.id] || {}),
                                    [type.id]: Number(value || 0),
                                  },
                                }))
                              }
                              addonBefore="LKR"
                            />
                          </Form.Item>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </Checkbox.Group>
            </div>
          </div>
          <Form.Item className="mb-0">
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting
                ? editingId
                  ? "Saving..."
                  : "Creating..."
                : editingId
                  ? "Save Changes"
                  : "Create Exhibition"}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selectedExhibition?.exhibitionName || "Exhibition Details"}
        open={isViewModalOpen}
        onCancel={() => setIsViewModalOpen(false)}
        footer={
          <Button variant="outline" onClick={() => setIsViewModalOpen(false)}>
            Close
          </Button>
        }
      >
        {selectedExhibition ? (
          <div className="space-y-4 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Event Dates</span>
              <span className="font-medium">
                {selectedExhibition.startDateTime
                  ? dayjs(selectedExhibition.startDateTime).format("MMM DD, YYYY HH:mm")
                  : selectedExhibition.dateRange}
                {" — "}
                {selectedExhibition.endDateTime
                  ? dayjs(selectedExhibition.endDateTime).format("MMM DD, YYYY HH:mm")
                  : null}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Booking Window</span>
              <span className="font-medium">
                {selectedExhibition.bookingOpenDateTime
                  ? `${dayjs(selectedExhibition.bookingOpenDateTime).format("MMM DD, YYYY HH:mm")} — ${dayjs(selectedExhibition.bookingCloseDateTime).format("MMM DD, YYYY HH:mm")}`
                  : selectedExhibition.bookingWindow}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Stalls Per Person</span>
              <span className="font-medium">
                {selectedExhibition.stallsPerPerson}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">State</span>
              <Badge
                variant={
                  stateVariant[
                  (selectedExhibition.exhibitionState ||
                    selectedExhibition.status ||
                    "PLANNING")!
                    .toString()
                    .toUpperCase()
                  ] || "outline"
                }
              >
                {selectedExhibition.exhibitionState ||
                  selectedExhibition.status ||
                  "Planning"}
              </Badge>
            </div>
            <div className="space-y-2">
              <p className="text-muted-foreground">Halls & Prices</p>
              {Array.isArray((selectedExhibition as any).halls) &&
                (selectedExhibition as any).halls.length > 0 ? (
                (selectedExhibition as any).halls.map((hall: any) => (
                  <div key={hall.id || hall.hallId} className="rounded border p-2">
                    <div className="font-semibold text-foreground">
                      {hall.hallName || `Hall ${hall.hallId || hall.id}`}
                    </div>
                    {Array.isArray(hall.prices) && hall.prices.length > 0 ? (
                      <div className="mt-2 flex flex-wrap gap-2">
                        {hall.prices.map((price: any) => (
                          <Badge key={price.id || `${hall.id}-${price.stallTypeId}`} variant="secondary">
                            {price.stallType || `Type ${price.stallTypeId}`}: LKR{" "}
                            {Number(price.price).toLocaleString()}
                          </Badge>
                        ))}
                      </div>
                    ) : (
                      <p className="text-xs text-muted-foreground">No prices found for this hall.</p>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No halls linked.</p>
              )}
            </div>
          </div>
        ) : (
          <p className="text-muted-foreground">No data available.</p>
        )}
      </Modal>
    </OrganizerLayout>
  );
};

export default OrganizerExhibitions;
