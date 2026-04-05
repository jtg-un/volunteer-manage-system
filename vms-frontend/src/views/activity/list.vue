<template>
  <div class="activity-list">
    <!-- 筛选工具栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索活动标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="服务类别">
          <el-select
            v-model="queryParams.categoryId"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.dictKey"
              :label="item.dictValue"
              :value="item.dictKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属地区">
          <el-select
            v-model="selectedProvince"
            placeholder="选择省"
            clearable
            style="width: 120px"
            @change="handleProvinceChange"
          >
            <el-option
              v-for="item in provinceList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
          <el-select
            v-model="selectedCity"
            placeholder="选择市"
            clearable
            style="width: 120px; margin-left: 8px"
            :disabled="!selectedProvince"
            @change="handleCityChange"
          >
            <el-option
              v-for="item in cityList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
          <el-select
            v-model="selectedDistrict"
            placeholder="选择区"
            clearable
            style="width: 120px; margin-left: 8px"
            :disabled="!selectedCity"
            @change="handleDistrictChange"
          >
            <el-option
              v-for="item in districtList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="待启动" :value="0" />
            <el-option label="运行中" :value="1" />
            <el-option label="已结项" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <el-card class="list-card">
      <el-table v-loading="loading" :data="activityList" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="title" label="项目标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row.activityId)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="服务类别" width="120" />
        <el-table-column prop="regionName" label="所属地区" width="120" />
        <el-table-column label="发起组织" width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="showOrgDetail(row.orgId)">
              {{ row.orgName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="200">
          <template #default="{ row }">
            {{ formatTimeRange(row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="120">
          <template #default="{ row }">
            <span :class="getCountClass(row.totalCurrentCount, row.totalPlanCount)">
              {{ row.totalCurrentCount }}/{{ row.totalPlanCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row.activityId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadActivityList"
        @current-change="loadActivityList"
      />
    </el-card>

    <!-- 活动详情弹窗 -->
    <ActivityDetailDialog
      v-model="detailVisible"
      :data="detailData"
      :show-register="userStore.isVolunteer && detailData.status === 0"
      @register="handleRegister"
    />

    <!-- 组织详情弹窗 -->
    <OrgDetailDialog
      v-model="orgDetailVisible"
      :org-id="selectedOrgId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, getActivityDetail } from '@/api/activity'
import { getDict } from '@/api/system'
import { registerActivity } from '@/api/volunteer'
import { useRegion } from '@/composables/useRegion'
import { getStatusType, formatTimeRange, getCountClass } from '@/utils/activity'
import { useUserStore } from '@/stores/user'
import ActivityDetailDialog from '@/components/activity/ActivityDetailDialog.vue'
import OrgDetailDialog from '@/components/org/OrgDetailDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const detailVisible = ref(false)
const detailData = ref({})
const activityList = ref([])
const total = ref(0)
const categoryList = ref([])

// 组织详情弹窗
const orgDetailVisible = ref(false)
const selectedOrgId = ref(null)

const {
  provinceList,
  cityList,
  districtList,
  selectedProvince,
  selectedCity,
  selectedDistrict,
  regionCode,
  loadProvinces,
  handleProvinceChange,
  handleCityChange,
  handleDistrictChange,
  resetRegion
} = useRegion()

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: '',
  status: null
})

onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadProvinces(),
    loadActivityList()
  ])
})

async function loadCategories() {
  try {
    const res = await getDict('service_category')
    categoryList.value = res
  } catch {
    // ignore
  }
}

async function loadActivityList() {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      categoryId: queryParams.categoryId || undefined,
      regionCode: regionCode.value || undefined,
      status: queryParams.status
    }
    const res = await getActivityList(params)
    activityList.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  loadActivityList()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.categoryId = ''
  queryParams.status = null
  queryParams.page = 1
  resetRegion()
  loadActivityList()
}

async function handleViewDetail(id) {
  try {
    const res = await getActivityDetail(id)
    detailData.value = res
    detailVisible.value = true
  } catch {
    ElMessage.error('加载活动详情失败')
  }
}

async function handleRegister(data) {
  try {
    await ElMessageBox.confirm(
      `确定要报名岗位「${data.posName}」吗？`,
      '报名确认',
      {
        confirmButtonText: '确定报名',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    await registerActivity({
      activityId: data.activityId,
      posId: data.posId
    })
    ElMessage.success('报名成功，请等待审核')
    detailVisible.value = false
    loadActivityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
    }
  }
}

// 显示组织详情弹窗
function showOrgDetail(orgId) {
  selectedOrgId.value = orgId
  orgDetailVisible.value = true
}
</script>

<style scoped>
.activity-list {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.text-danger {
  color: #f56c6c;
}

.text-warning {
  color: #e6a23c;
}

.text-success {
  color: #67c23a;
}
</style>