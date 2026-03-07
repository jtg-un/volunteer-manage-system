import { ref } from 'vue'
import { getRegionList } from '@/api/system'

/**
 * 地区级联加载 composable
 */
export function useRegion() {
  const regionOptions = ref([])

  /**
   * 地区级联配置
   */
  const regionProps = {
    value: 'regionCode',
    label: 'regionName',
    children: 'children',
    lazy: true,
    lazyLoad: async (node, resolve) => {
      const { level, value } = node
      const parentCode = level === 0 ? '' : value
      try {
        const res = await getRegionList(parentCode)
        const nodes = res.map(item => ({
          regionCode: item.regionCode,
          regionName: item.regionName,
          leaf: item.level >= 3 || !item.hasChildren
        }))
        resolve(nodes)
      } catch {
        resolve([])
      }
    }
  }

  /**
   * 加载省级地区
   */
  async function loadProvinces() {
    try {
      const res = await getRegionList('')
      regionOptions.value = res.map(item => ({
        regionCode: item.regionCode,
        regionName: item.regionName,
        children: item.hasChildren ? [] : undefined,
        leaf: !item.hasChildren
      }))
    } catch {
      regionOptions.value = []
    }
  }

  return {
    regionOptions,
    regionProps,
    loadProvinces
  }
}