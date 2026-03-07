import request from '@/utils/request'

// ==================== 用户个人接口 ====================

// 更新个人信息
export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

// 修改密码
export function updatePassword(data) {
  return request.put('/user/password', data)
}

// ==================== 管理员接口 ====================

// 获取用户列表
export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

// 更新用户状态
export function updateUserStatus(data) {
  return request.put('/admin/user/status', data)
}