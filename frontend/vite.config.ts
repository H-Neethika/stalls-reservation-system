import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Load env file from project root (one level up)
  const env = loadEnv(mode, path.resolve(__dirname, ".."), "");

  // Get port from FRONTEND_PORT environment variable, fallback to 3000
  const port = parseInt(env.FRONTEND_PORT || "3000");

  return {
    server: {
      host: "::",
      port: port,
    },
    plugins: [react(), mode === "development" && componentTagger()].filter(
      Boolean
    ),
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  };
});
