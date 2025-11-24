import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { componentTagger } from "lovable-tagger";

export default defineConfig(({ mode }) => {
  const envDir = path.resolve(__dirname, "..");
  const env = loadEnv(mode, envDir, "");
  const port = parseInt(env.FRONTEND_PORT || "3030");

  return {
    envDir,
    envPrefix: ["VITE_", "CLOUD_", "BACKEND_", "FRONTEND_"],
    define: {
      global: "globalThis",
    },
    server: {
      host: "::",
      port: port,
    },
    plugins: [react(), mode === "development" && componentTagger()].filter(Boolean),
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  };
});
