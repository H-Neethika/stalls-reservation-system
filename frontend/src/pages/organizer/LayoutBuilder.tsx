import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { mockApi } from "@/lib/mockData";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import HallLayoutCanvas from "@/components/HallLayoutCanvas";
import { toast } from "@/hooks/use-toast";

interface StallShape {
    id: string;
    name: string;
    x: number;
    y: number;
    width: number;
    height: number;
    color: string;
    size?: string;
    price?: number;
}

const LayoutBuilder = () => {
    const { hallId } = useParams<{ hallId: string }>();
    const navigate = useNavigate();

    const [layout, setLayout] = useState<StallShape[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (hallId) loadLayout();
    }, [hallId]);

    const loadLayout = async () => {
        setLoading(true);
        const existing = await mockApi.getLayoutByHallId(hallId!);
        if (existing?.layout_json) {
            setLayout(existing.layout_json);
        } else {
            // start with an empty layout
            setLayout([]);
        }
        setLoading(false);
    };

    const addStall = () => {
        const newStall: StallShape = {
            id: `stall-${Date.now()}`,
            name: `S${layout.length + 1}`,
            x: 100 + layout.length * 10,
            y: 100 + layout.length * 10,
            width: 60,
            height: 40,
            color: "#6EE7B7",
        };
        const updated = [...layout, newStall];
        setLayout(updated);
    };

    const saveLayout = async () => {
        if (!hallId) return;
        await mockApi.saveLayout(hallId, layout);
        toast({
            title: "Layout saved",
            description: "Your hall layout has been successfully saved.",
        });
    };

    if (loading) return <p className="p-4">Loading...</p>;

    return (
        <div className="container mx-auto p-6 space-y-4">
            <Card>
                <CardHeader>
                    <CardTitle>Hall Layout Builder</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="flex gap-4 mb-4">
                        <Button onClick={addStall}>Add Stall</Button>
                        <Button variant="outline" onClick={saveLayout}>
                            Save Layout
                        </Button>
                        <Button variant="ghost" onClick={() => navigate("/halls")}>
                            Back to Halls
                        </Button>
                    </div>

                    <HallLayoutCanvas
                        layout={layout}
                        mode="edit"
                        onLayoutChange={setLayout}
                    />
                </CardContent>
            </Card>
        </div>
    );
};

export default LayoutBuilder;
