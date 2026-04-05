<template>
  <el-dialog
    v-model="visible"
    title="活动详情"
    width="800px"
    destroy-on-close
  >
    <!-- 活动基本信息 -->
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="项目编号">{{ activity.projectCode }}</el-descriptions-item>
      <el-descriptions-item label="活动标题">{{ activity.title }}</el-descriptions-item>
      <el-descriptions-item label="服务类别">{{ activity.categoryName }}</el-descriptions-item>
      <el-descriptions-item label="所属地区">{{ activity.regionName }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="getStatusType(activity.status)">{{ activity.statusName }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="活动时间">
        {{ formatDateTime(activity.startTime) }} ~ {{ formatDateTime(activity.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="项目描述" :span="2">{{ activity.description || '无' }}</el-descriptions-item>
      <el-descriptions-item label="拒绝原因" :span="2" v-if="activity.rejectReason">
        <span style="color: #f56c6c">{{ activity.rejectReason }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">岗位报名情况</el-divider>

    <!-- 岗位表格（带展开行） -->
    <el-table
      :data="positionsWithRegistrations"
      border
      row-key="posId"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <!-- 展开后显示该岗位的报名人员列表 -->
          <div class="expanded-content" style="padding: 10px 20px" v-if="row.registrations && row.registrations.length > 0">
            <el-table :data="row.registrations" size="small">
              <el-table-column label="姓名" width="120">
                <template #default="{ row: reg }">
                  <el-link type="primary" @click="showVolunteerDetail(reg)">
                    {{ reg.realName }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column label="状态" width="100">
                <template #default="{ row: reg }">
                  <el-tag :type="getRegStatusType(reg.regStatus)" size="small">
                    {{ reg.statusName }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="签到状态" width="100">
                <template #default="{ row: reg }">
                  <template v-if="reg.regStatus === 1">
                    <el-tag v-if="reg.checkedOut" type="success" size="small">已签退</el-tag>
                    <el-tag v-else-if="reg.checkedIn" type="warning" size="small">已签到</el-tag>
                    <span v-else>-</span>
                  </template>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="时长" width="80">
                <template #default="{ row: reg }">
                  <span v-if="reg.hoursIssued" style="color: #67c23a">{{ reg.hours }}h</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="报名时间" min-width="140">
                <template #default="{ row: reg }">
                  {{ formatDateTime(reg.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" v-if="activity.status === 0">
                <template #default="{ row: reg }">
                  <template v-if="reg.regStatus === 0">
                    <el-button type="primary" size="small" @click="handleAudit(reg, 1)">通过</el-button>
                    <el-button type="danger" size="small" @click="handleAudit(reg, 2)">拒绝</el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="padding: 20px; text-align: center">
            <el-empty description="暂无报名人员" :image-size="60" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="posName" label="岗位名称" min-width="150" />
      <el-table-column prop="planCount" label="计划人数" width="100" />
      <el-table-column label="已报名" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-warning': row.currentCount > row.planCount }">
            {{ row.currentCount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="剩余名额" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.remainCount <= 0 }">
            {{ row.remainCount }}
          </span>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>

    <!-- 报名者详情弹窗 -->
    <VolunteerDetailDialog
      v-model="volunteerDetailVisible"
      :registration="selectedRegistration"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityDetail } from '@/api/activity'
import { getRegistrations, auditRegistration } from '@/api/orgReg'
import VolunteerDetailDialog from './VolunteerDetailDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  activityId: Number
})

const emit = defineEmits(['update:modelValue', 'updated'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const activity = ref({})
const positionsWithRegistrations = ref([])

// 报名者详情弹窗
const volunteerDetailVisible = ref(false)
const selectedRegistration = ref(null)

// 监听弹窗打开，加载活动详情
watch(visible, async (val) => {
  if (val && props.activityId) {
    await loadData()
  }
})

// 岗位与报名数据合并
function mergePositionsAndRegistrations(positions, registrations) {
  // 按 posId 分组报名
  const regMap = {}
  registrations.forEach(reg => {
    if (!regMap[reg.posId]) {
      regMap[reg.posId] = []
    }
    regMap[reg.posId].push(reg)
  })

  // 合并到岗位信息
  return positions.map(pos => ({
    ...pos,
    registrations: regMap[pos.posId] || []
  }))
}

// 点击报名者姓名，显示详情弹窗
function showVolunteerDetail(reg) {
  selectedRegistration.value = reg
  volunteerDetailVisible.value = true
}

// 审核报名
async function handleAudit(reg, status) {
  const action = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}【${reg.realName}】的报名吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await auditRegistration({
      regId: reg.regId,
      status
    })
    ElMessage.success(`${action}成功`)
    emit('updated')
    // 刷新数据
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

// 加载数据
async function loadData() {
  if (!props.activityId) return
  loading.value = true
  try {
    const detail = await getActivityDetail(props.activityId)
    activity.value = detail

    const regRes = await getRegistrations({
      activityId: props.activityId,
      page: 1,
      size: 999
    })
    const registrations = regRes.records || []

    positionsWithRegistrations.value = mergePositionsAndRegistrations(
      detail.positions || [],
      registrations
    )
  } catch (error) {
    console.error('加载活动详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化函数
function formatDateTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 16)
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 4: 'danger' }
  return types[status] || 'info'
}

function getRegStatusType(status) {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}
</script>

<style scoped>
.text-warning {
  color: #e6a23c;
}
.text-danger {
  color: #f56c6c;
}
</style>