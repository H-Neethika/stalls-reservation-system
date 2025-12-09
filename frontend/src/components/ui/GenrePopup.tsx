import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Check, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { genreService } from "@/services/genreService";

interface GenrePopupProps {
  open: boolean;
  onClose: () => void;
  stalls: { hallName: string; stallId: number, displayName: string }[];
  reservationId: number;
  exhibitionId: number;
}

export const GenrePopup = ({
  open,
  onClose,
  stalls,
  reservationId,
  exhibitionId, 
}: GenrePopupProps) => {
  const [stallInputs, setStallInputs] = useState<Record<number, string>>({});
  const [stallGenres, setStallGenres] = useState<Record<number, string[]>>({});

  useEffect(() => {
    const map: Record<number, string[]> = {};
    stalls.forEach((s) => {
      map[s.stallId] = [];
    });
    setStallGenres(map);
  }, [stalls]);

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Add Genres for Your Stalls</DialogTitle>
        </DialogHeader>

        {/* Loop stalls */}
        {stalls.map((stall) => (
          <div key={stall.stallId} className="mb-6">
            <p className="text-lg font-bold">Hall Name: {stall.hallName}</p>
            <p className="text-sm">Stall: {stall.displayName}</p>

            {/* Render tags */}
            <div className="flex gap-2 mt-2 flex-wrap">
              {stallGenres[stall.stallId]?.map((g) => (
                <span
                  key={g}
                  className="px-2 py-1 bg-primary/10 rounded-md flex items-center gap-1"
                >
                  {g}
                  <button
                    className="text-red-500"
                    onClick={() => {
                      setStallGenres((prev) => ({
                        ...prev,
                        [stall.stallId]: prev[stall.stallId].filter(
                          (x) => x !== g
                        ),
                      }));
                    }}
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>

            <div className="relative mt-2 w-full">
              {/* INPUT */}
              <Input
                placeholder="Type Genre"
                value={stallInputs[stall.stallId] || ""}
                onChange={(e) =>
                  setStallInputs((prev) => ({
                    ...prev,
                    [stall.stallId]: e.target.value,
                  }))
                }
                className="pr-20" // 👈 extra padding so text doesn't overlap buttons
              />

              {/* ADD BUTTON */}
              <button
                type="button"
                onClick={() => {
                  const value = stallInputs[stall.stallId]?.trim();
                  if (!value) return;

                  setStallGenres((prev) => ({
                    ...prev,
                    [stall.stallId]: [...prev[stall.stallId], value],
                  }));

                  setStallInputs((prev) => ({ ...prev, [stall.stallId]: "" }));
                }}
                className="group absolute right-10 top-1/2 -translate-y-1/2 w-6 h-6 
               bg-green-200 rounded flex items-center justify-center 
               hover:bg-green-400 border border-green-500"
              >
                <Check
                  size={16}
                  className="text-green-500 group-hover:text-white transition-colors"
                />
              </button>

              {/* CLEAR BUTTON */}
              <button
                type="button"
                onClick={() =>
                  setStallInputs((prev) => ({ ...prev, [stall.stallId]: "" }))
                }
                className="group absolute right-2 top-1/2 -translate-y-1/2 w-6 h-6 
               bg-red-200 rounded flex items-center justify-center 
               hover:bg-red-400 border border-red-500"
              >
                <X
                  size={16}
                  className="text-red-500 group-hover:text-white transition-colors"
                />
              </button>
            </div>
          </div>
        ))}

        {/* ✅ Add Save + Skip Buttons Here */}
        <div className="flex justify-end gap-3 mt-4">
          <Button variant="outline" onClick={onClose}>
            Skip
          </Button>

          <Button
            onClick={async () => {
              try {
                const payload = Object.entries(stallGenres)
                  .filter(([_, names]) => names.length > 0)
                  .map(([stallId, names]) => ({
                    names,
                    stallId: Number(stallId),
                    reservationId,
                    exhibitionId, 
                  }));

                console.log("FINAL PAYLOAD BEFORE API:", payload);

                if (payload.length === 0) {
                  onClose();
                  return;
                }

                await genreService.createBulkGenres(payload);

                console.log("Bulk API Success!");
                onClose();
              } catch (error) {
                console.error("Bulk Genre API Error:", error);
              }
            }}
          >
            Save
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};
