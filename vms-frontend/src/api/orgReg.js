import request from '@/utils/request'

/**
 * 组织端报名管理 API
 */

// 获取活动的报名列表
export function getRegistrations(activityId, params) {
  return request.get(`/org/registration/list/${activityId}`, { params })
}

// 审核报名
export function auditRegistration(data) {
  return request.post('/org/registration/audit', data)
}

// 确认发放时长
export function confirmHours(data) {
  return request.post('/org/registration/hours', data)
}