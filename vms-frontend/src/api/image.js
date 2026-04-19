import request from '@/utils/request'

// ========== 活动图片 ==========

/**
 * 上传活动图片
 * @param {File} file 图片文件
 * @param {Number} activityId 活动ID
 * @param {Number} isCover 是否封面 0/1
 */
export function uploadActivityImage(file, activityId, isCover = 0) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('activityId', activityId)
  formData.append('isCover', isCover)
  return request.post('/activity-img/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取活动图片列表
 */
export function getActivityImages(activityId) {
  return request.get(`/activity-img/${activityId}`)
}

/**
 * 删除活动图片
 */
export function deleteActivityImage(fileId) {
  return request.delete(`/activity-img/${fileId}`)
}

/**
 * 设置活动封面
 */
export function setActivityCover(fileId) {
  return request.put(`/activity-img/cover/${fileId}`)
}

/**
 * 排序活动图片
 */
export function sortActivityImages(data) {
  return request.put('/activity-img/sort', data)
}

// ========== 组织风采 ==========

/**
 * 上传组织风采
 */
export function uploadOrgGallery(file, orgId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('orgId', orgId)
  return request.post('/org-img/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取组织风采列表（指定组织）
 */
export function getOrgGallery(orgId) {
  return request.get(`/org-img/${orgId}`)
}

/**
 * 删除组织风采
 */
export function deleteOrgGallery(fileId) {
  return request.delete(`/org-img/${fileId}`)
}

/**
 * 排序组织风采
 */
export function sortOrgGallery(data) {
  return request.put('/org-img/sort', data)
}

// ========== 当前组织风采（带认证的接口保留原有路径） ==========

/**
 * 获取当前组织的风采
 */
export function getMyOrgGallery() {
  return request.get('/org/images/my')
}