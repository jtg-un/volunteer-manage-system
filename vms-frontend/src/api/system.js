import request from '@/utils/request'

// ==================== 字典接口 ====================

// 获取字典（公开）
export function getDict(dictType) {
  return request.get(`/dict/${dictType}`)
}

// 分页获取所有字典（管理员）
export function getDictPage(params) {
  return request.get('/dict/admin/page', { params })
}

// 新增字典
export function addDict(data) {
  return request.post('/dict/admin', data)
}

// 更新字典
export function updateDict(data) {
  return request.put('/dict/admin', data)
}

// 删除字典
export function deleteDict(dictId) {
  return request.delete(`/dict/admin/${dictId}`)
}

// 获取所有字典类型
export function getDictTypes() {
  return request.get('/dict/admin/types')
}

// ==================== 行政区划接口 ====================

// 获取行政区划列表（根据父级编码）
export function getRegionList(parentCode) {
  return request.get('/system/region/list', {
    params: { parentCode }
  })
}

// 获取区划详情
export function getRegionDetail(regionCode) {
  return request.get(`/system/region/detail/${regionCode}`)
}

// 分页获取所有区划（管理员）
export function getRegionPage(params) {
  return request.get('/system/region/admin/page', { params })
}

// 新增区划
export function addRegion(data) {
  return request.post('/system/region/admin', data)
}

// 更新区划
export function updateRegion(data) {
  return request.put('/system/region/admin', data)
}

// 删除区划
export function deleteRegion(regionCode) {
  return request.delete(`/system/region/admin/${regionCode}`)
}

// ==================== 公告接口 ====================

// 获取最新公告（公开）
export function getLatestNotices(limit = 5) {
  return request.get('/system/notice/latest', { params: { limit } })
}

// 获取公告详情
export function getNoticeDetail(noticeId) {
  return request.get(`/system/notice/detail/${noticeId}`)
}

// 分页获取公告列表（管理员）
export function getNoticePage(params) {
  return request.get('/system/notice/admin/page', { params })
}

// 新增公告
export function addNotice(data) {
  return request.post('/system/notice/admin', data)
}

// 更新公告
export function updateNotice(data) {
  return request.put('/system/notice/admin', data)
}

// 删除公告
export function deleteNotice(noticeId) {
  return request.delete(`/system/notice/admin/${noticeId}`)
}