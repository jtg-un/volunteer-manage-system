import request from '@/utils/request'

// 获取字典
export function getDict(dictType) {
  return request.get(`/dict/${dictType}`)
}

// 获取行政区划列表（根据父级编码）
export function getRegionList(parentCode) {
  return request.get('/system/region/list', {
    params: { parentCode }
  })
}