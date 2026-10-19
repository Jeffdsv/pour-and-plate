import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // during development, send /api requests on to the java app
    proxy: {
      '/api': 'http://localhost:8081',
    },
  },
})
