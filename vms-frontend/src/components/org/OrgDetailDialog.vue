<template>
  <el-dialog
    v-model="visible"
    title="组织信息"
    width="600px"
    destroy-on-close
  >
    <div v-loading="loading" v-if="org">
      <!-- 基本信息 -->
      <div class="org-header">
        <div class="org-title">
          <h2>{{ org.orgName }}</h2>
          <el-tag v-if="org.auditStatus !== undefined" :type="getAuditStatusType(org.auditStatus)" size="small">
            {{ getAuditStatusName(org.auditStatus) }}
          </el-tag>
        </div>
        <p class="org-code">联络编号：{{ org.orgCode || '-' }}</p>
      </div>

      <el-divider />

      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="单位类型">
          {{ getUnitTypeName(org.unitType) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="成立日期">
          {{ org.foundDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ org.contactPerson || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ org.contactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ org.address || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          {{ org.intro || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getPublicOrgDetail } from '@/api/org'

const props = defineProps({
  modelValue: Boolean,
  orgId: Number
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const org = ref(null)

// 单位类型映射
const unitTypeMap = {
  'government': '政府机关',
  'enterprise': '企业',
  'social_org': '社会组织',
  'community': '社区',
  'school': '学校',
  'hospital': '医院',
  'other': '其他'
}

// 监听弹窗打开，加载组织详情
watch(visible, async (val) => {
  if (val && props.orgId) {
    loading.value = true
    try {
      org.value = await getPublicOrgDetail(props.orgId)
    } catch (error) {
      console.error('加载组织详情失败:', error)
    } finally {
      loading.value = false
    }
  }
})

function getUnitTypeName(type) {
  return unitTypeMap[type] || type
}

function getAuditStatusName(status) {
  const names = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return names[status] || '未知'
}

function getAuditStatusType(status) {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}
</script>

<style scoped>
.org-header {
  padding: 10px 0;
}

.org-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.org-title h2 {
  margin: 0;
  font-size: 20px;
}

.org-code {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}
</style>