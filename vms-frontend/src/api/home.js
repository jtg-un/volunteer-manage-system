import request from '@/utils/request'

/**
 * 获取首页统计数据
 */
export function getHomeStats() {
  return request.get('/home/stats')
}

/**
 * 获取活动风采图片
 */
export function getHomeGallery() {
  return request.get('/home/gallery')
}