import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento, /api é repassado ao back-end: front e API ficam na mesma origem,
// e o cookie de sessão funciona sem CORS
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY ?? 'http://localhost:8080',
      },
    },
  },
});
