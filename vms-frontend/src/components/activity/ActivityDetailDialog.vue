<template>
  <el-dialog
    v-model="visible"
    title="活动详情"
    width="700px"
    destroy-on-close
    @close="handleClose"
    @open="loadImages"
  >
    <!-- 活动图片展示 -->
    <ImageGallery :images="activityImages" v-if="activityImages.length" />
    <div class="image-divider" v-if="activityImages.length"></div>

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
      <el-table-column label="操作" width="100" v-if="showRegister">
        <template #default="{ row }">
          <el-button
            v-if="row.remainCount > 0"
            type="primary"
            size="small"
            @click="handleRegister(row)"
          >
            报名
          </el-button>
          <span v-else class="text-muted">已满</span>
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
import { ref, computed, watch } from 'vue'
import { getStatusName, getStatusType, formatDateTime } from '@/utils/activity'
import ImageGallery from '@/components/common/ImageGallery.vue'
import { getActivityImages } from '@/api/image'

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
  },
  showRegister: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'register'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activityImages = ref([])

// 加载活动图片
async function loadImages() {
  // 确保activityId存在且是有效数字
  if (props.data.activityId && typeof props.data.activityId === 'number') {
    try {
      const res = await getActivityImages(props.data.activityId)
      activityImages.value = res || []
    } catch (e) {
      console.error('加载图片失败:', e)
      activityImages.value = []
    }
  }
}

function handleClose() {
  emit('update:modelValue', false)
}

function handleRegister(position) {
  emit('register', {
    activityId: props.data.activityId,
    posId: position.posId,
    posName: position.posName
  })
}
</script>

<style scoped>
.image-divider {
  margin-bottom: 16px;
}

.text-danger {
  color: #f56c6c;
}

.text-muted {
  color: #909399;
}
</style>