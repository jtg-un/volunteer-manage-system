<template>
  <div class="volunteer-records">
    <el-card>
      <template #header>
        <span>时长记录</span>
      </template>

      <el-table :data="records" v-loading="loading">
        <el-table-column label="活动名称" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="showActivityDetail(row.activityId)">
              {{ row.activityTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.activityStartTime) }} ~ {{ formatDateTime(row.activityEndTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="服务时长" width="100">
          <template #default="{ row }">
            <span class="hours-text">{{ row.hours }}小时</span>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="获得积分" width="100">
          <template #default="{ row }">
            <span class="points-text">{{ row.points }}分</span>
          </template>
        </el-table-column>
        <el-table-column label="发放时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.auditTime) }}
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
import { getMyRecords } from '@/api/volunteer'
import { getActivityDetail } from '@/api/activity'
import ActivityDetailDialog from '@/components/activity/ActivityDetailDialog.vue'

const loading = ref(false)
const records = ref([])

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
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyRecords({
      page: pagination.page,
      size: pagination.size
    })
    records.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载时长记录失败:', error)
  } finally {
    loading.value = false
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').slice(0, 16)
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
.volunteer-records {
  padding: 20px;
}

.hours-text {
  color: #67c23a;
  font-weight: bold;
}

.points-text {
  color: #e6a23c;
  font-weight: bold;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>