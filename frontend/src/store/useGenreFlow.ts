import { create } from "zustand";

interface PopupStall {
  hallName: string;
  stallId: number;
}

interface GenreFlowState {
  openGenrePopup: boolean;
  popupStalls: PopupStall[];
  setOpenGenrePopup: (value: boolean) => void;
  setPopupStalls: (stalls: PopupStall[]) => void;
}

export const useGenreFlow = create<GenreFlowState>((set) => ({
  openGenrePopup: false,
  popupStalls: [],
  setOpenGenrePopup: (value) => set({ openGenrePopup: value }),
  setPopupStalls: (stalls) => set({ popupStalls: stalls }),
}));
