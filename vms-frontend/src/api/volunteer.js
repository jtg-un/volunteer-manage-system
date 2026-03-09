import request from '@/utils/request'

/**
 * 志愿者端 API
 */

// 报名活动
export function registerActivity(data) {
  return request.post('/volunteer/register', data)
}

// 取消报名
export function cancelRegistration(regId) {
  return request.delete(`/volunteer/register/${regId}`)
}

// 获取我的报名列表
export function getMyRegistrations(params) {
  return request.get('/volunteer/my/registrations', { params })
}

// 获取志愿者统计数据
export function getVolunteerStats() {
  return request.get('/volunteer/my/stats')
}

// 签到
export function checkIn(regId) {
  return request.post(`/volunteer/checkin/${regId}`)
}

// 签退
export function checkOut(regId) {
  return request.post(`/volunteer/checkout/${regId}`)
}

// 获取签到状态
export function getCheckinStatus(regId) {
  return request.get(`/volunteer/checkin/status/${regId}`)
}

// 获取我的时长记录
export function getMyRecords(params) {
  return request.get('/volunteer/my/records', { params })
}