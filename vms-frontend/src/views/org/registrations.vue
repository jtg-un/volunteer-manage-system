<template>
  <div class="org-registrations">
    <el-card>
      <template #header>
        <span>报名管理</span>
      </template>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-select v-model="selectedActivity" placeholder="全部活动" clearable style="width: 300px" @change="handleFilterChange">
          <el-option
            v-for="activity in activities"
            :key="activity.activityId"
            :label="activity.title"
            :value="activity.activityId"
          />
        </el-select>
        <el-radio-group v-model="statusFilter" @change="handleFilterChange" style="margin-left: 20px">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="1">已通过</el-radio-button>
          <el-radio-button :value="2">已拒绝</el-radio-button>
          <el-radio-button :value="3">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="registrations" v-loading="loading">
        <el-table-column label="活动" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="showActivityDetail(row.activityId)">
              {{ row.activityTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="姓名" width="100">
          <template #default="{ row }">
            <el-link type="primary" @click="showVolunteerDetail(row)">
              {{ row.realName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="posName" label="岗位" width="120" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.regStatus)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="100">
          <template #default="{ row }">
            <template v-if="row.regStatus === 1">
              <el-tag v-if="row.checkedOut" type="success" size="small">已签退</el-tag>
              <el-tag v-else-if="row.checkedIn" type="warning" size="small">已签到</el-tag>
              <span v-else>-</span>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="80">
          <template #default="{ row }">
            <template v-if="row.hoursIssued">
              <span style="color: #67c23a">{{ row.hours }}h</span>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <template v-if="row.regStatus === 0">
              <el-button type="primary" size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, 2)">拒绝</el-button>
            </template>
            <template v-if="row.regStatus === 1 && row.checkedOut && !row.hoursIssued">
              <el-button type="success" size="small" @click="showHoursDialog(row)">发放时长</el-button>
            </template>
            <template v-if="row.regStatus === 1 && row.hoursIssued">
              <el-button v-if="row.evaluated" type="info" size="small" @click="showEvaluationDialog(row, true)">查看评价</el-button>
              <el-button v-else type="warning" size="small" @click="showEvaluationDialog(row, false)">评价</el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 发放时长弹窗 -->
    <el-dialog v-model="hoursDialogVisible" title="发放时长" width="400px">
      <el-form :model="hoursForm" label-width="80px">
        <el-form-item label="志愿者">
          <span>{{ currentRow?.realName }}</span>
        </el-form-item>
        <el-form-item label="服务时长">
          <el-input-number v-model="hoursForm.hours" :min="0.5" :max="24" :step="0.5" :precision="1" />
          <span style="margin-left: 10px">小时</span>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="hoursForm.points" :min="0" :disabled="autoCalcPoints" />
          <el-checkbox v-model="autoCalcPoints" style="margin-left: 10px">自动计算(10分/小时)</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hoursDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmHours">确定</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="evalDialogVisible" :title="isViewMode ? '查看评价' : '评价志愿者'" width="500px">
      <el-form :model="evalForm" label-width="100px" :disabled="isViewMode">
        <el-form-item label="志愿者">
          <span>{{ currentRow?.realName }}</span>
        </el-form-item>
        <el-form-item label="培训评分">
          <el-rate v-model="evalForm.scoreTraining" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-score :score-template="evalForm.scoreTraining + '分'" />
        </el-form-item>
        <el-form-item label="协作评分">
          <el-rate v-model="evalForm.scoreCooperation" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-score :score-template="evalForm.scoreCooperation + '分'" />
        </el-form-item>
        <el-form-item label="执行力评分">
          <el-rate v-model="evalForm.scoreExecution" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-score :score-template="evalForm.scoreExecution + '分'" />
        </el-form-item>
        <el-form-item label="综合评分">
          <el-tag type="success" size="large">{{ avgScore }} 分</el-tag>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="evalForm.comment" type="textarea" :rows="4" placeholder="请输入评价内容（选填）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalDialogVisible = false">{{ isViewMode ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isViewMode" type="primary" @click="handleEvaluate">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 活动详情弹窗 -->
    <OrgActivityDetailDialog
      v-model="activityDetailVisible"
      :activity-id="selectedActivityId"
    />

    <!-- 报名者详情弹窗 -->
    <VolunteerDetailDialog
      v-model="volunteerDetailVisible"
      :registration="selectedRegistration"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRegistrations, auditRegistration, confirmHours } from '@/api/orgReg'
import { getMyActivities } from '@/api/activity'
import { evaluateVolunteer, getEvaluationByRegId, checkEvaluated } from '@/api/evaluation'
import OrgActivityDetailDialog from '@/components/org/OrgActivityDetailDialog.vue'
import VolunteerDetailDialog from '@/components/org/VolunteerDetailDialog.vue'

const loading = ref(false)
const activities = ref([])
const selectedActivity = ref(null)
const registrations = ref([])
const statusFilter = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 发放时长弹窗
const hoursDialogVisible = ref(false)
const currentRow = ref(null)
const autoCalcPoints = ref(true)
const hoursForm = reactive({
  regId: null,
  hours: 1,
  points: 10
})

// 评价弹窗
const evalDialogVisible = ref(false)
const isViewMode = ref(false)
const evalForm = reactive({
  regId: null,
  scoreTraining: 5,
  scoreCooperation: 5,
  scoreExecution: 5,
  comment: ''
})

// 活动详情弹窗
const activityDetailVisible = ref(false)
const selectedActivityId = ref(null)

// 报名者详情弹窗
const volunteerDetailVisible = ref(false)
const selectedRegistration = ref(null)

// 计算综合评分
const avgScore = computed(() => {
  return ((evalForm.scoreTraining + evalForm.scoreCooperation + evalForm.scoreExecution) / 3).toFixed(2)
})

// 自动计算积分
watch(() => hoursForm.hours, (val) => {
  if (autoCalcPoints.value) {
    hoursForm.points = Math.round(val * 10)
  }
})

onMounted(() => {
  loadActivities()
  loadData()
})

async function loadActivities() {
  try {
    const res = await getMyActivities({ page: 1, size: 100 })
    activities.value = res.records || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

function handleFilterChange() {
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      status: statusFilter.value,
      page: pagination.page,
      size: pagination.size
    }
    // 如果选择了活动，添加活动筛选
    if (selectedActivity.value) {
      params.activityId = selectedActivity.value
    }

    const res = await getRegistrations(params)
    registrations.value = res.records || []
    pagination.total = res.total || 0

    // 检查已发放时长的报名是否已评价
    for (const reg of registrations.value) {
      if (reg.hoursIssued) {
        try {
          reg.evaluated = await checkEvaluated(reg.regId)
        } catch (error) {
          reg.evaluated = false
        }
      }
    }
  } catch (error) {
    console.error('加载报名列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleAudit(row, status) {
  const action = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该报名吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await auditRegistration({
      regId: row.regId,
      status
    })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

function showHoursDialog(row) {
  currentRow.value = row
  hoursForm.regId = row.regId
  hoursForm.hours = 1
  hoursForm.points = 10
  autoCalcPoints.value = true
  hoursDialogVisible.value = true
}

async function handleConfirmHours() {
  try {
    await confirmHours({
      regId: hoursForm.regId,
      hours: hoursForm.hours,
      points: hoursForm.points
    })
    ElMessage.success('时长发放成功')
    hoursDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('发放时长失败:', error)
  }
}

// 显示评价弹窗
async function showEvaluationDialog(row, viewMode) {
  currentRow.value = row
  isViewMode.value = viewMode

  if (viewMode) {
    // 查看模式，加载评价数据
    try {
      const res = await getEvaluationByRegId(row.regId)
      if (res) {
        evalForm.regId = row.regId
        evalForm.scoreTraining = res.scoreTraining
        evalForm.scoreCooperation = res.scoreCooperation
        evalForm.scoreExecution = res.scoreExecution
        evalForm.comment = res.comment || ''
      }
    } catch (error) {
      console.error('加载评价失败:', error)
    }
  } else {
    // 新建评价，重置表单
    evalForm.regId = row.regId
    evalForm.scoreTraining = 5
    evalForm.scoreCooperation = 5
    evalForm.scoreExecution = 5
    evalForm.comment = ''
  }

  evalDialogVisible.value = true
}

// 提交评价
async function handleEvaluate() {
  try {
    await evaluateVolunteer({
      regId: evalForm.regId,
      scoreTraining: evalForm.scoreTraining,
      scoreCooperation: evalForm.scoreCooperation,
      scoreExecution: evalForm.scoreExecution,
      comment: evalForm.comment
    })
    ElMessage.success('评价成功')
    evalDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('评价失败:', error)
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').slice(0, 16)
}

function getStatusType(status) {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

// 显示活动详情弹窗
function showActivityDetail(activityId) {
  selectedActivityId.value = activityId
  activityDetailVisible.value = true
}

// 显示报名者详情弹窗
function showVolunteerDetail(reg) {
  selectedRegistration.value = reg
  volunteerDetailVisible.value = true
}
</script>

<style scoped>
.org-registrations {
  padding: 20px;
}

.filter-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>