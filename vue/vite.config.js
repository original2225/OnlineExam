import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'

export default defineConfig({
  plugins: [
    vue(),

    // ❌ 关闭 useSource（默认就是 false）
    ElementPlus(),

    // ❌ 去掉 importStyle: 'sass'
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),

    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],

  optimizeDeps: {
    include: [
      "vue",
      "vue-router",
      "axios",
    ],
  },

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  css: {
    preprocessorOptions: {
      scss: {
        // 先不要做主题覆盖
      }
    }
  },
})