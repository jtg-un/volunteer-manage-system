import request from '@/utils/request'

// 发布活动
export function publishActivity(data) {
  return request.post('/activity/publish', data)
}

// 获取活动列表
export function getActivityList(params) {
  return request.get('/activity/list', { params })
}

// 获取活动详情
export function getActivityDetail(id) {
  return request.get(`/activity/detail/${id}`)
}

// ==================== 管理员接口 ====================

// 管理员获取待审核活动列表
export function getPendingActivities(params) {
  return request.get('/activity/admin/pending', { params })
}

// 管理员审核活动
export function auditActivity(data) {
  return request.post('/activity/admin/audit', data)
}

// ==================== 组织接口 ====================

// 组织获取自己的活动列表
export function getMyActivities(params) {
  return request.get('/activity/my/list', { params })
}

// 组织更新活动状态
export function updateActivityStatus(data) {
  return request.put('/activity/my/status', data)
}