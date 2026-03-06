import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装请求方法
const request = {
  get(url, config) {
    return service.get(url, config).then((res) => res.data.data)
  },
  post(url, data, config) {
    return service.post(url, data, config).then((res) => res.data.data)
  },
  put(url, data, config) {
    return service.put(url, data, config).then((res) => res.data.data)
  },
  delete(url, config) {
    return service.delete(url, config).then((res) => res.data.data)
  }
}

export default request
export { service }