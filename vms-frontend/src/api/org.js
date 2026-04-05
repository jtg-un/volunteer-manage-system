import request from '@/utils/request'

// 获取组织列表（管理员）
export function getOrgList(params) {
  return request.get('/org/admin/list', { params })
}

// 获取组织详情（管理员）
export function getOrgDetail(orgId) {
  return request.get(`/org/admin/detail/${orgId}`)
}

// 审核组织（管理员）
export function auditOrg(data) {
  return request.post('/org/admin/audit', data)
}

// 获取当前组织信息
export function getMyOrg() {
  return request.get('/org/my')
}

// 更新组织信息
export function updateOrg(data) {
  return request.put('/org/my', data)
}

// 获取组织详情（公开接口，所有用户可访问）
export function getPublicOrgDetail(orgId) {
  return request.get(`/org/public/${orgId}`)
}

// 获取字典
export function getDict(dictType) {
  return request.get(`/dict/${dictType}`)
}