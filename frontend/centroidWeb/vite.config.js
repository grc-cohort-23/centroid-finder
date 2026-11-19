import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const apiServer = process.env.CENTROID_API_URL || 'http://127.0.0.1:3000'
const proxy = {
  '/api': apiServer,
  '/outputs': apiServer,
  '^/videos/[^/?]+': apiServer,
  '/previews': apiServer,
  '/jobs': apiServer,
}

export default defineConfig({
  plugins: [react()],
  server: { proxy },
  preview: { proxy },
})
