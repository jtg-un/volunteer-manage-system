import request from '@/utils/request'

// 获取AI推荐活动
export function getAiRecommend(question) {
  return request.post('/ai/recommend', { question })
}