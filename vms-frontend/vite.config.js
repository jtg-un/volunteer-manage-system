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
      // 用户服务 (8081): 登录、注册、用户管理、文件上传
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 组织端报名管理 (8083) - 必须放在 /api/org 前面
      '/api/org/registration': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/org': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/user': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/admin/user': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/file': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 静态资源：头像访问
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 活动服务 (8083): 活动管理
      '/api/activity': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      // 志愿者端接口 (8083)
      '/api/volunteer': {
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