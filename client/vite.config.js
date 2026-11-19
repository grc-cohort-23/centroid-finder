import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development, Vite serves the React app and forwards API calls to the
// Express server, so the browser sees one origin (no CORS, and canvas can
// read video pixels without being "tainted").
const SERVER = 'http://localhost:3000';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': SERVER,
      '/thumbnail': SERVER,
      '/process': SERVER,
      '/videos': SERVER,
      '/results': SERVER,
    },
  },
  test: {
    environment: 'node',
  },
});
