<template>
  <div class="image-upload-gallery">
    <!-- 已上传图片列表 -->
    <div class="image-list" v-if="images.length">
      <draggable
        :list="images"
        item-key="fileId"
        @end="handleDragEnd"
        class="drag-container"
        :animation="200"
      >
        <template #item="{ element }">
          <div class="image-item">
            <el-image
              :src="element.fileUrl"
              fit="cover"
              :preview-src-list="images.map(i => i.fileUrl)"
              :initial-index="images.findIndex(i => i.fileId === element.fileId)"
            />
            <div class="image-actions">
              <el-tooltip content="设为封面" placement="top">
                <el-icon
                  @click="handleSetCover(element)"
                  :class="{ 'is-cover': element.isCover === 1 }"
                  class="action-icon"
                >
                  <Star />
                </el-icon>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-icon @click="handleDelete(element.fileId)" class="action-icon delete">
                  <Delete />
                </el-icon>
              </el-tooltip>
            </div>
            <el-tag v-if="element.isCover === 1" type="success" size="small" class="cover-tag">
              封面
            </el-tag>
          </div>
        </template>
      </draggable>
    </div>

    <!-- 上传区域 -->
    <el-upload
      v-if="images.length < maxCount"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :show-file-list="false"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :before-upload="beforeUpload"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="upload-area"
      drag
    >
      <el-icon class="upload-icon"><Plus /></el-icon>
      <div class="upload-text">点击或拖拽上传</div>
      <div class="upload-tip">最多 {{ maxCount }} 张，单张不超过 5MB</div>
    </el-upload>

    <!-- 数量提示 -->
    <div class="count-info" v-if="images.length > 0">
      已上传 {{ images.length }}/{{ maxCount }} 张
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Star } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import {
  uploadActivityImage,
  deleteActivityImage,
  setActivityCover,
  sortActivityImages,
  uploadOrgGallery,
  deleteOrgGallery,
  sortOrgGallery
} from '@/api/image'

const props = defineProps({
  // 业务类型: activity 或 org
  bizType: {
    type: String,
    default: 'activity'
  },
  // 业务ID（活动ID或组织ID）
  bizId: {
    type: Number,
    required: true
  },
  // 最大数量
  maxCount: {
    type: Number,
    default: 10
  },
  // 初始图片列表
  initialImages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update', 'change'])

const images = ref([...props.initialImages])
const loading = ref(false)

// 上传URL和参数
const uploadUrl = computed(() => {
  return props.bizType === 'activity'
    ? '/api/activity-img/upload'
    : '/api/org-img/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const uploadData = computed(() => {
  if (props.bizType === 'activity') {
    return { activityId: props.bizId, isCover: images.value.length === 0 ? 1 : 0 }
  }
  return { orgId: props.bizId }
})

// 监听初始图片变化
watch(() => props.initialImages, (newVal) => {
  images.value = [...newVal]
}, { deep: true })

// 上传前验证
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  if (images.value.length >= props.maxCount) {
    ElMessage.error(`最多只能上传 ${props.maxCount} 张图片`)
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    images.value.push(response.data)
    ElMessage.success('上传成功')
    emitChange()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

// 删除图片
const handleDelete = async (fileId) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const deleteFn = props.bizType === 'activity' ? deleteActivityImage : deleteOrgGallery
    await deleteFn(fileId)

    images.value = images.value.filter(img => img.fileId !== fileId)
    ElMessage.success('删除成功')
    emitChange()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

// 设置封面
const handleSetCover = async (image) => {
  if (image.isCover === 1) return

  if (props.bizType === 'activity') {
    await setActivityCover(image.fileId)
  }

  // 更新本地状态
  images.value.forEach(img => {
    img.isCover = img.fileId === image.fileId ? 1 : 0
  })
  ElMessage.success('封面设置成功')
  emitChange()
}

// 拖拽结束
const handleDragEnd = async () => {
  const sortItems = images.value.map((img, index) => ({
    fileId: img.fileId,
    sortOrder: index,
    isCover: img.isCover
  }))

  const sortFn = props.bizType === 'activity' ? sortActivityImages : sortOrgGallery
  await sortFn({
    bizType: props.bizType === 'activity' ? 'activity_image' : 'org_gallery',
    bizId: props.bizId,
    items: sortItems
  })

  ElMessage.success('排序已保存')
  emitChange()
}

// 触发change事件
const emitChange = () => {
  emit('change', images.value)
  emit('update', images.value)
}

// 暴露方法供外部调用
defineExpose({
  images,
  getImages: () => images.value,
  getCoverImage: () => images.value.find(img => img.isCover === 1)
})
</script>

<style scoped>
.image-upload-gallery {
  width: 100%;
}

.image-list {
  margin-bottom: 16px;
}

.drag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #dcdfe6;
  cursor: move;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  gap: 8px;
}

.action-icon {
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}

.action-icon.is-cover {
  color: #ffd700;
}

.action-icon.delete:hover {
  color: #f56c6c;
}

.cover-tag {
  position: absolute;
  bottom: 4px;
  left: 4px;
}

.upload-area {
  width: 100%;
}

.upload-area .el-upload-dragger {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 32px;
  color: #909399;
  margin-bottom: 8px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.count-info {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>