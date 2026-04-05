<template>
  <div class="activity-audit">
    <el-card>
      <template #header>
        <span>活动管理</span>
      </template>

      <!-- 筛选工具栏 -->
      <div style="margin-bottom: 16px; display: flex; gap: 16px;">
        <el-select v-model="filterStatus" placeholder="活动状态" clearable style="width: 120px;" @change="handleSearch">
          <el-option label="待审核" :value="3" />
          <el-option label="待启动" :value="0" />
          <el-option label="运行中" :value="1" />
          <el-option label="已结项" :value="2" />
          <el-option label="已拒绝" :value="4" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column label="活动标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="发起组织" width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="showOrgDetail(row.orgId)">
              {{ row.orgName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="服务类别" width="100" />
        <el-table-column prop="regionName" label="所属地区" width="120" />
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            {{ formatTimeRange(row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <template v-if="row.status === 3">
              <el-button type="success" size="small" @click="handleAudit(row, 1)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, 2)">
                拒绝
              </el-button>
            </template>
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
    <OrgActivityDetailDialog
      v-model="detailVisible"
      :activity-id="currentActivityId"
      @updated="fetchData"
    />

    <!-- 组织详情弹窗 -->
    <OrgDetailDialog
      v-model="orgDetailVisible"
      :org-id="selectedOrgId"
    />

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingActivities, auditActivity } from '@/api/activity'
import { getStatusName, getStatusType, formatDateTime, formatTimeRange } from '@/utils/activity'
import OrgActivityDetailDialog from '@/components/org/OrgActivityDetailDialog.vue'
import OrgDetailDialog from '@/components/org/OrgDetailDialog.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const filterStatus = ref(null)
const detailVisible = ref(false)
const currentActivityId = ref(null)

// 组织详情弹窗
const orgDetailVisible = ref(false)
const selectedOrgId = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const pendingAuditActivity = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingActivities({
      page: pagination.value.page,
      size: pagination.value.size,
      status: filterStatus.value
    })
    tableData.value = res.records || []
    pagination.value.total = res.total || 0
  } catch {
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchData()
}

const handleDetail = (row) => {
  currentActivityId.value = row.activityId
  detailVisible.value = true
}

const showOrgDetail = (orgId) => {
  selectedOrgId.value = orgId
  orgDetailVisible.value = true
}

const handleAudit = async (row, result) => {
  if (result === 1) {
    ElMessageBox.confirm('确定通过该活动审核吗？', '提示', {
      type: 'success'
    }).then(async () => {
      await doAudit(row.activityId, result, '')
    }).catch(() => {})
  } else {
    pendingAuditActivity.value = row
    rejectReason.value = ''
    rejectVisible.value = true
  }
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  await doAudit(pendingAuditActivity.value.activityId, 2, rejectReason.value)
  rejectVisible.value = false
}

const doAudit = async (activityId, auditResult, rejectReasonText) => {
  try {
    await auditActivity({
      activityId,
      auditResult,
      rejectReason: rejectReasonText
    })
    ElMessage.success('审核完成')
    fetchData()
    detailVisible.value = false
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.activity-audit {
  padding: 20px;
}
</style>