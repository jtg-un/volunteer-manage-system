import request from '@/utils/request'

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 获取用户信息
export function getUserInfo() {
  return request.get('/auth/userinfo')
}

// 志愿者注册
export function registerVolunteer(data) {
  return request.post('/auth/register/volunteer', data)
}

// 组织注册
export function registerOrg(data) {
  return request.post('/auth/register/org', data)
}