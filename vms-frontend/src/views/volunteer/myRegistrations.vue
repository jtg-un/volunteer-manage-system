<template>
  <div class="my-registrations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的报名</span>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-statistic title="累计服务时长" :value="stats.totalHours || 0" suffix="小时" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="累计积分" :value="stats.totalPoints || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="参与活动次数" :value="stats.activityCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="本月服务时长" :value="stats.monthHours || 0" suffix="小时" />
        </el-col>
      </el-row>
    </el-card>

    <el-card class="list-card">
      <el-table :data="registrations" v-loading="loading">
        <el-table-column label="活动名称" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="showActivityDetail(row.activityId)">
              {{ row.activityTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="posName" label="岗位" width="120" />
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.activityStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="activityStatusName" label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusType(row.activityStatus)" size="small">
              {{ row.activityStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regStatusName" label="报名状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRegStatusType(row.regStatus)" size="small">
              {{ row.regStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="120">
          <template #default="{ row }">
            <template v-if="row.regStatus === 1">
              <el-tag v-if="row.checkedOut" type="success" size="small">已签退</el-tag>
              <el-tag v-else-if="row.checkedIn" type="warning" size="small">已签到</el-tag>
              <el-tag v-else type="info" size="small">未签到</el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100">
          <template #default="{ row }">
            <template v-if="row.hoursIssued">
              <span class="hours-text">{{ row.hours }}小时</span>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.regStatus === 1 && row.activityStatus === 1">
              <el-button
                v-if="!row.checkedIn"
                type="primary"
                size="small"
                @click="handleCheckIn(row)"
              >
                签到
              </el-button>
              <el-button
                v-else-if="!row.checkedOut"
                type="success"
                size="small"
                @click="handleCheckOut(row)"
              >
                签退
              </el-button>
            </template>
            <el-button
              v-if="row.regStatus === 0 || (row.regStatus === 1 && !row.checkedIn)"
              type="danger"
              size="small"
              text
              @click="handleCancel(row)"
            >
              取消报名
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 活动详情弹窗 -->
    <ActivityDetailDialog
      v-model="activityDetailVisible"
      :data="activityDetailData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyRegistrations, getVolunteerStats, cancelRegistration, checkIn, checkOut } from '@/api/volunteer'
import { getActivityDetail } from '@/api/activity'
import ActivityDetailDialog from '@/components/activity/ActivityDetailDialog.vue'

const loading = ref(false)
const registrations = ref([])
const stats = ref({})

// 活动详情弹窗
const activityDetailVisible = ref(false)
const activityDetailData = ref({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
  loadStats()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyRegistrations({
      page: pagination.page,
      size: pagination.size
    })
    registrations.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    stats.value = await getVolunteerStats()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function handleCheckIn(row) {
  try {
    await checkIn(row.regId)
    ElMessage.success('签到成功')
    loadData()
  } catch (error) {
    console.error('签到失败:', error)
  }
}

async function handleCheckOut(row) {
  try {
    await checkOut(row.regId)
    ElMessage.success('签退成功')
    loadData()
  } catch (error) {
    console.error('签退失败:', error)
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelRegistration(row.regId)
    ElMessage.success('取消报名成功')
    loadData()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error)
    }
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').slice(0, 16)
}

function getActivityStatusType(status) {
  const types = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'warning',
    4: 'danger'
  }
  return types[status] || 'info'
}

function getRegStatusType(status) {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

// 显示活动详情
async function showActivityDetail(activityId) {
  try {
    const res = await getActivityDetail(activityId)
    activityDetailData.value = res
    activityDetailVisible.value = true
  } catch (error) {
    console.error('加载活动详情失败:', error)
  }
}
</script>

<style scoped>
.my-registrations {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.list-card {
  margin-top: 20px;
}

.hours-text {
  color: #67c23a;
  font-weight: bold;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>