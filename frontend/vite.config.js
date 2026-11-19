import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Fast Refresh de React se habilita con @vitejs/plugin-react.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      // Permite usar VITE_API_URL=/api en desarrollo sin problemas de CORS.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  preview: {
    port: 4173
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    chunkSizeWarningLimit: 900
  }
});
