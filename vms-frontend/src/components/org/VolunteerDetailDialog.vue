<template>
  <el-dialog
    v-model="visible"
    title="报名者详情"
    width="550px"
    destroy-on-close
  >
    <div class="volunteer-detail" v-if="registration">
      <!-- 基本信息 -->
      <div class="user-header">
        <el-avatar :size="64" :src="registration.avatarUrl">
          <el-icon :size="32"><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-info">
          <h3>{{ registration.realName }}</h3>
          <p>{{ registration.phone }}</p>
        </div>
      </div>

      <el-divider />

      <!-- 报名信息 -->
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="报名岗位">
          {{ registration.posName }}
        </el-descriptions-item>
        <el-descriptions-item label="报名状态">
          <el-tag :type="getRegStatusType(registration.regStatus)" size="small">
            {{ registration.statusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报名时间">
          {{ formatDateTime(registration.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="签到状态">
          <template v-if="registration.regStatus === 1">
            <el-tag v-if="registration.checkedOut" type="success" size="small">已签退</el-tag>
            <el-tag v-else-if="registration.checkedIn" type="warning" size="small">已签到</el-tag>
            <span v-else>未签到</span>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="服务时长">
          <template v-if="registration.hoursIssued">
            <span style="color: #67c23a">{{ registration.hours }} 小时</span>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 评价信息（如果有） -->
      <el-divider content-position="left" v-if="registration.hoursIssued">
        服务评价
      </el-divider>

      <div v-if="evaluation && registration.hoursIssued" class="evaluation-section">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="培训评分">
            <el-rate
              :model-value="evaluation.scoreTraining"
              disabled
              show-score
              :score-template="evaluation.scoreTraining + '分'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="协作评分">
            <el-rate
              :model-value="evaluation.scoreCooperation"
              disabled
              show-score
              :score-template="evaluation.scoreCooperation + '分'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="执行力评分">
            <el-rate
              :model-value="evaluation.scoreExecution"
              disabled
              show-score
              :score-template="evaluation.scoreExecution + '分'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="综合评分" :span="3">
            <el-tag type="success" size="large">{{ avgScore }} 分</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="3">
            {{ evaluation.comment || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else-if="registration.hoursIssued" class="evaluation-empty">
        <el-text type="info">暂未评价</el-text>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { getEvaluationByRegId, checkEvaluated } from '@/api/evaluation'

const props = defineProps({
  modelValue: Boolean,
  registration: Object
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const evaluation = ref(null)

// 计算综合评分
const avgScore = computed(() => {
  if (!evaluation.value) return '-'
  return ((evaluation.value.scoreTraining +
           evaluation.value.scoreCooperation +
           evaluation.value.scoreExecution) / 3).toFixed(2)
})

// 监听弹窗打开，加载评价信息
watch(visible, async (val) => {
  if (val && props.registration?.regId) {
    evaluation.value = null
    // 如果已发放时长，检查是否有评价
    if (props.registration.hoursIssued) {
      try {
        const hasEvaluated = await checkEvaluated(props.registration.regId)
        if (hasEvaluated) {
          evaluation.value = await getEvaluationByRegId(props.registration.regId)
        }
      } catch (error) {
        console.error('加载评价失败:', error)
      }
    }
  }
})

function formatDateTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 16)
}

function getRegStatusType(status) {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}
</script>

<style scoped>
.volunteer-detail {
  padding: 10px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info h3 {
  margin: 0;
  font-size: 18px;
}

.user-info p {
  margin: 4px 0 0;
  color: #909399;
}

.evaluation-section {
  margin-top: 10px;
}

.evaluation-empty {
  padding: 10px;
  text-align: center;
}
</style>