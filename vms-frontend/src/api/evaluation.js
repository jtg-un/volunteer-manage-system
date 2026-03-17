import request from '@/utils/request'

/**
 * 评价相关 API
 */

// 组织端 - 评价志愿者
export function evaluateVolunteer(data) {
  return request.post('/org/evaluation', data)
}

// 组织端 - 获取报名的评价信息
export function getEvaluationByRegId(regId) {
  return request.get(`/org/evaluation/${regId}`)
}

// 组织端 - 检查报名是否已评价
export function checkEvaluated(regId) {
  return request.get(`/org/evaluation/check/${regId}`)
}

// 志愿者端 - 获取我的评价列表
export function getMyEvaluations(params) {
  return request.get('/volunteer/my/evaluations', { params })
}