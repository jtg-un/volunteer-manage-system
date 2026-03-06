<template>
  <div class="my-activity">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>我的活动</span>
          <el-button type="primary" @click="$router.push('/org/publish-activity')">
            发布新活动
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="categoryName" label="服务类别" width="100" />
        <el-table-column prop="regionName" label="所属地区" width="120" />
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="120">
          <template #default="{ row }">
            {{ row.totalCurrentCount }} / {{ row.totalPlanCount }}
          </template>
        </el-table-column>
        <el-table-column prop="pendingRegCount" label="待审核报名" width="100">
          <template #default="{ row }">
            <el-badge :value="row.pendingRegCount" :hidden="row.pendingRegCount === 0">
              <span>{{ row.pendingRegCount }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleStatusChange(row, 1)"
            >
              启动
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleStatusChange(row, 2)"
            >
              结项
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
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="700px">
      <el-descriptions :column="2" border v-if="currentActivity">
        <el-descriptions-item label="项目编号">{{ currentActivity.projectCode }}</el-descriptions-item>
        <el-descriptions-item label="活动标题">{{ currentActivity.title }}</el-descriptions-item>
        <el-descriptions-item label="服务类别">{{ currentActivity.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="所属地区">{{ currentActivity.regionName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentActivity.status)">{{ currentActivity.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报名情况">
          {{ currentActivity.totalCurrentCount }} / {{ currentActivity.totalPlanCount }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentActivity.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentActivity.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">
          {{ currentActivity.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="currentActivity.status === 4">
          <el-text type="danger">{{ currentActivity.rejectReason || '无' }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyActivities, updateActivityStatus } from '@/api/activity'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const detailVisible = ref(false)
const currentActivity = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyActivities({
      page: pagination.value.page,
      size: pagination.value.size
    })
    tableData.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error(error)
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  currentActivity.value = row
  detailVisible.value = true
}

const handleStatusChange = (row, targetStatus) => {
  const statusText = targetStatus === 1 ? '启动' : '结项'
  ElMessageBox.confirm(`确定要${statusText}该活动吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await updateActivityStatus({
        activityId: row.activityId,
        status: targetStatus
      })
      ElMessage.success('状态更新成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const getStatusType = (status) => {
  const types = {
    0: 'info',      // 待启动
    1: 'success',   // 运行中
    2: 'warning',   // 已结项
    3: 'warning',   // 待审核
    4: 'danger'     // 已拒绝
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}

const formatDateTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-activity {
  padding: 20px;
}
</style>