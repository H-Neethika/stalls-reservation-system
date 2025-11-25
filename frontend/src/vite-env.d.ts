/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly CLOUD_GATEWAY_PORT?: string;
  readonly BACKEND_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
