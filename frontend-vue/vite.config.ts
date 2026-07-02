import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/vuedraggable')) {
            return 'drag-vendor'
          }
          if (id.includes('node_modules/vue-echarts')) {
            return 'vue-echarts-vendor'
          }
          if (id.includes('node_modules/zrender')) {
            return 'zrender-vendor'
          }
          if (id.includes('node_modules/echarts/lib/chart')) {
            return 'echarts-charts-vendor'
          }
          if (id.includes('node_modules/echarts/lib/component')) {
            return 'echarts-components-vendor'
          }
          if (id.includes('node_modules/echarts/lib/renderer')) {
            return 'echarts-renderers-vendor'
          }
          if (id.includes('node_modules/echarts/lib/core') || id.includes('node_modules/echarts/lib/model') || id.includes('node_modules/echarts/lib/util')) {
            return 'echarts-core-vendor'
          }
          if (id.includes('node_modules/echarts/charts')) {
            return 'echarts-charts-vendor'
          }
          if (id.includes('node_modules/echarts/components') || id.includes('node_modules/echarts/renderers')) {
            return 'echarts-components-vendor'
          }
          if (id.includes('node_modules/echarts')) {
            return 'echarts-core-vendor'
          }
        },
      },
    },
  },
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [NaiveUiResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
