import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 用户服务 (8081): 登录、注册、组织管理
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/org': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 活动服务 (8083): 活动管理
      '/api/activity': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      // 系统服务 (8087): 字典、行政区划
      '/api/system': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/api/dict': {
        target: 'http://localhost:8087',
        changeOrigin: true
      }
    }
  }
})