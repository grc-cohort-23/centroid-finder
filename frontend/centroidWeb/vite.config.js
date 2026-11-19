import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:3000',
      '/outputs': 'http://localhost:3000',
      '/videos': 'http://localhost:3000',
      '/jobs': 'http://localhost:3000',
    },
  },
})
