import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role)
  const isVolunteer = computed(() => userRole.value === 0)
  const isOrg = computed(() => userRole.value === 1)
  const isAdmin = computed(() => userRole.value === 2)

  // 登录
  async function login(username, password) {
    const data = await loginApi({ username, password })
    token.value = data.token
    localStorage.setItem('token', data.token)
    await fetchUserInfo()
    return data
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const data = await getUserInfo()
    userInfo.value = data
    return data
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    isVolunteer,
    isOrg,
    isAdmin,
    login,
    fetchUserInfo,
    logout
  }
})