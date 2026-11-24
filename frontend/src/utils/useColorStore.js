import { create } from 'zustand';

// 1. Create the store
export const useColorStore = create((set) => ({
    centroidColor: '#00FF00',
    changeCentroidColor: (newColor) => set({ centroidColor: newColor })
}));
