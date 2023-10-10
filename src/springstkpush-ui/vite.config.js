import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [vue()],
	server: {
		port: 5173,
		proxy: {
			"/api/v1/payment/initiate": {
				target: "http://localhost:8080",
				ws: true,
				changeOrigin: true,
			},
		},
	},
});
