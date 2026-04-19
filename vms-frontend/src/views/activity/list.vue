<template>
  <GuestLayout>
    <div class="activity-list-page">
      <!-- 筛选工具栏 -->
      <div class="filter-section">
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
      </div>

      <!-- 活动卡片列表 -->
      <div class="activities-section" v-loading="loading">
        <div class="activities-grid">
          <div
            v-for="activity in activityList"
            :key="activity.activityId"
            class="activity-card"
            @click="goToDetail(activity.activityId)"
          >
            <div class="activity-cover">
              <el-image
                :src="activity.coverImageUrl || ''"
                fit="cover"
              >
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon size="48"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="cover-badge">
                <el-tag :type="getStatusType(activity.status)" size="small">
                  {{ activity.statusName }}
                </el-tag>
              </div>
            </div>
            <div class="activity-info">
              <h3 class="activity-title">{{ activity.title }}</h3>
              <div class="activity-meta">
                <span class="meta-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  {{ activity.orgName }}
                </span>
              </div>
              <div class="activity-time">
                <el-icon><Clock /></el-icon>
                {{ formatDateTime(activity.startTime) }} ~ {{ formatDateTime(activity.endTime) }}
              </div>
              <div class="activity-footer">
                <span class="activity-region">
                  <el-icon><Location /></el-icon>
                  {{ activity.regionName || '线上' }}
                </span>
                <span class="activity-count">
                  {{ activity.totalCurrentCount || 0 }}/{{ activity.totalPlanCount || 0 }}人
                </span>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="activityList.length === 0 && !loading" description="暂无活动" :image-size="100" />

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          class="pagination"
          @size-change="loadActivityList"
          @current-change="loadActivityList"
        />
      </div>
    </div>
  </GuestLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import GuestLayout from '@/layouts/GuestLayout.vue'
import { getActivityList } from '@/api/activity'
import { getDict } from '@/api/system'
import { useRegion } from '@/composables/useRegion'
import { Picture, OfficeBuilding, Clock, Location } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const activityList = ref([])
const total = ref(0)
const categoryList = ref([])

const {
  provinceList,
  cityList,
  selectedProvince,
  selectedCity,
  regionCode,
  loadProvinces,
  handleProvinceChange,
  handleCityChange,
  resetRegion
} = useRegion()

const queryParams = reactive({
  page: 1,
  size: 12,
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
    activityList.value = res.records || []
    total.value = res.total || 0
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

function goToDetail(activityId) {
  router.push(`/activity/detail/${activityId}`)
}

function formatDateTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return types[status] || 'info'
}
</script>

<style scoped>
.activity-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.activities-section {
  min-height: 400px;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.activity-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.2);
}

.activity-cover {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.activity-cover .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}

.activity-card:hover .activity-cover .el-image {
  transform: scale(1.08);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  color: #c0c4cc;
}

.cover-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.activity-info {
  padding: 16px;
}

.activity-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.activity-region {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
}

.activity-count {
  color: #67c23a;
  font-weight: 500;
}

.pagination {
  margin-top: 24px;
  justify-content: center;
}

@media (max-width: 1200px) {
  .activities-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .activities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .activities-grid {
    grid-template-columns: 1fr;
  }
  .activity-cover {
    height: 180px;
  }
}
</style>