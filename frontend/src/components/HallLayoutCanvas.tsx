import React from "react";
import { Stage, Layer, Rect, Text } from "react-konva";

interface StallShape {
    id: string;
    name: string;
    x: number;
    y: number;
    width: number;
    height: number;
    color: string;
    price?: number;
    size?: string;
}

interface HallLayoutCanvasProps {
    layout: StallShape[];
    mode: "edit" | "view";
    onLayoutChange?: (updated: StallShape[]) => void;
    onStallClick?: (stall: StallShape) => void;
}

const HallLayoutCanvas: React.FC<HallLayoutCanvasProps> = ({
    layout,
    mode,
    onLayoutChange,
    onStallClick,
}) => {
    const [stalls, setStalls] = React.useState(layout);

    const handleDragMove = (id: string, e: any) => {
        const updated = stalls.map((stall) =>
            stall.id === id
                ? { ...stall, x: e.target.x(), y: e.target.y() }
                : stall
        );
        setStalls(updated);
        onLayoutChange?.(updated);
    };

    return (
        <Stage width={800} height={600} style={{ border: "1px solid #ccc", background: "#fafafa" }}>
            <Layer>
                {stalls.map((stall) => (
                    <React.Fragment key={stall.id}>
                        <Rect
                            x={stall.x}
                            y={stall.y}
                            width={stall.width}
                            height={stall.height}
                            fill={stall.color}
                            cornerRadius={4}
                            shadowBlur={2}
                            draggable={mode === "edit"}
                            onDragMove={(e) => handleDragMove(stall.id, e)}
                            onClick={() => mode === "view" && onStallClick?.(stall)}
                        />
                        <Text
                            text={stall.name}
                            x={stall.x}
                            y={stall.y + stall.height / 2 - 8}
                            width={stall.width}
                            align="center"
                            fontSize={12}
                            fill="#000"
                        />
                    </React.Fragment>
                ))}
            </Layer>
        </Stage>
    );
};

export default HallLayoutCanvas;
