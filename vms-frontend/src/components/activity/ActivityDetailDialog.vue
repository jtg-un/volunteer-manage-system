<template>
  <el-dialog
    v-model="visible"
    title="活动详情"
    width="700px"
    destroy-on-close
    @close="handleClose"
  >
    <el-descriptions :column="2" border>
      <el-descriptions-item label="项目编号">
        {{ data.projectCode }}
      </el-descriptions-item>
      <el-descriptions-item label="项目标题" :span="2">
        {{ data.title }}
      </el-descriptions-item>
      <el-descriptions-item label="服务类别">
        {{ data.categoryName }}
      </el-descriptions-item>
      <el-descriptions-item label="所属地区">
        {{ data.regionName }}
      </el-descriptions-item>
      <el-descriptions-item label="发起组织">
        {{ data.orgName }}
      </el-descriptions-item>
      <el-descriptions-item label="服务对象">
        {{ data.targetAudience || '未设置' }}
      </el-descriptions-item>
      <el-descriptions-item label="活动时间" :span="2">
        {{ formatDateTime(data.startTime) }} ~ {{ formatDateTime(data.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="活动状态">
        <el-tag :type="getStatusType(data.status)">
          {{ getStatusName(data.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="提交时间" v-if="showCreateTime">
        {{ formatDateTime(data.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="项目描述" :span="2">
        {{ data.description || '暂无描述' }}
      </el-descriptions-item>
      <el-descriptions-item label="拒绝原因" :span="2" v-if="data.status === 4">
        <el-text type="danger">{{ data.rejectReason || '无' }}</el-text>
      </el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">岗位信息</el-divider>

    <el-table :data="data.positions || []" border style="width: 100%">
      <el-table-column prop="posName" label="岗位名称" />
      <el-table-column prop="planCount" label="计划人数" width="100" />
      <el-table-column prop="currentCount" label="已报名" width="100" />
      <el-table-column label="剩余名额" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.remainCount <= 0 }">
            {{ row.remainCount }}
          </span>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <slot name="actions" :data="data" />
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { getStatusName, getStatusType, formatDateTime } from '@/utils/activity'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => ({})
  },
  showCreateTime: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

function handleClose() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.text-danger {
  color: #f56c6c;
}
</style>