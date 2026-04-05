import request from '@/utils/request'

/**
 * 组织端报名管理 API
 */

// 获取报名列表（activityId可选，不传则查询所有活动）
export function getRegistrations(params) {
  return request.get('/org/registration/list', { params })
}

// 审核报名
export function auditRegistration(data) {
  return request.post('/org/registration/audit', data)
}

// 确认发放时长
export function confirmHours(data) {
  return request.post('/org/registration/hours', data)
}