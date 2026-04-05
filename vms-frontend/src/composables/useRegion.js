import { ref } from 'vue'
import { getRegionList } from '@/api/system'

/**
 * 地区分步选择 composable
 * 省→市→区 三级联动
 */
export function useRegion() {
  // 省市区选项列表
  const provinceList = ref([])
  const cityList = ref([])
  const districtList = ref([])

  // 选中的值
  const selectedProvince = ref('')
  const selectedCity = ref('')
  const selectedDistrict = ref('')

  // 最终用于查询的地区编码
  const regionCode = ref('')

  /**
   * 加载省级地区
   */
  async function loadProvinces() {
    try {
      const res = await getRegionList('')
      provinceList.value = res
    } catch {
      provinceList.value = []
    }
  }

  /**
   * 省份变更，加载市级
   */
  async function handleProvinceChange(provinceCode) {
    // 清空下级
    selectedCity.value = ''
    selectedDistrict.value = ''
    cityList.value = []
    districtList.value = []

    if (!provinceCode) {
      regionCode.value = ''
      return
    }

    regionCode.value = provinceCode

    try {
      const res = await getRegionList(provinceCode)
      cityList.value = res
    } catch {
      cityList.value = []
    }
  }

  /**
   * 市级变更，加载区级
   */
  async function handleCityChange(cityCode) {
    // 清空下级
    selectedDistrict.value = ''
    districtList.value = []

    if (!cityCode) {
      regionCode.value = selectedProvince.value
      return
    }

    regionCode.value = cityCode

    try {
      const res = await getRegionList(cityCode)
      districtList.value = res
    } catch {
      districtList.value = []
    }
  }

  /**
   * 区级变更
   */
  function handleDistrictChange(districtCode) {
    if (!districtCode) {
      regionCode.value = selectedCity.value || selectedProvince.value
      return
    }
    regionCode.value = districtCode
  }

  /**
   * 重置选择
   */
  function resetRegion() {
    selectedProvince.value = ''
    selectedCity.value = ''
    selectedDistrict.value = ''
    cityList.value = []
    districtList.value = []
    regionCode.value = ''
  }

  return {
    // 选项列表
    provinceList,
    cityList,
    districtList,
    // 选中的值
    selectedProvince,
    selectedCity,
    selectedDistrict,
    regionCode,
    // 方法
    loadProvinces,
    handleProvinceChange,
    handleCityChange,
    handleDistrictChange,
    resetRegion
  }
}