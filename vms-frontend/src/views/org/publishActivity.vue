<template>
  <div class="publish-activity">
    <el-card>
      <template #header>
        <span>发布活动</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="项目标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入项目标题" />
        </el-form-item>

        <el-form-item label="服务类别" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择服务类别" style="width: 100%">
            <el-option
              v-for="item in categoryList"
              :key="item.dictKey"
              :label="item.dictValue"
              :value="item.dictKey"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目属地" prop="regionCode">
          <el-select
            v-model="selectedProvince"
            placeholder="选择省"
            clearable
            style="width: 140px"
            @change="handleProvinceChange"
          >
            <el-option
              v-for="item in provinceList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
          <el-select
            v-model="selectedCity"
            placeholder="选择市"
            clearable
            style="width: 140px; margin-left: 8px"
            :disabled="!selectedProvince"
            @change="handleCityChange"
          >
            <el-option
              v-for="item in cityList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
          <el-select
            v-model="selectedDistrict"
            placeholder="选择区"
            clearable
            style="width: 140px; margin-left: 8px"
            :disabled="!selectedCity"
            @change="handleDistrictChange"
          >
            <el-option
              v-for="item in districtList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="服务对象" prop="targetAudience">
          <el-select
            v-model="form.targetAudience"
            placeholder="请选择服务对象"
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
            allow-create
            filterable
          >
            <el-option
              v-for="item in targetAudienceList"
              :key="item.dictKey"
              :label="item.dictValue"
              :value="item.dictKey"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="活动时间" required>
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center">至</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>

        <!-- 活动图片 -->
        <el-divider content-position="left">活动图片</el-divider>

        <el-form-item label="活动封面">
          <div class="image-preview-area">
            <!-- 已选择的图片预览 -->
            <div class="preview-list" v-if="previewImages.length">
              <div
                v-for="(img, index) in previewImages"
                :key="index"
                class="preview-item"
              >
                <el-image :src="img.url" fit="cover" />
                <div class="preview-actions">
                  <el-icon
                    @click="setCoverIndex(index)"
                    :class="{ 'is-cover': coverIndex === index }"
                    class="action-icon"
                  >
                    <Star />
                  </el-icon>
                  <el-icon @click="removePreviewImage(index)" class="action-icon delete">
                    <Delete />
                  </el-icon>
                </div>
                <el-tag v-if="coverIndex === index" type="success" size="small" class="cover-tag">
                  封面
                </el-tag>
              </div>
            </div>

            <!-- 上传按钮 -->
            <el-upload
              v-if="previewImages.length < maxImages"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept="image/jpeg,image/png,image/gif,image/webp"
              class="upload-btn"
              drag
            >
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击或拖拽上传</div>
              <div class="upload-tip">最多 {{ maxImages }} 张，单张不超过 5MB</div>
            </el-upload>
          </div>
          <div class="image-count" v-if="previewImages.length">
            已选择 {{ previewImages.length }}/{{ maxImages }} 张
          </div>
        </el-form-item>

        <!-- 岗位设置 -->
        <el-divider content-position="left">岗位设置</el-divider>

        <div v-for="(position, index) in form.positions" :key="index" class="position-item">
          <el-row :gutter="20">
            <el-col :span="10">
              <el-form-item
                :label="`岗位${index + 1}名称`"
                :prop="`positions.${index}.posName`"
                :rules="[{ required: true, message: '请输入岗位名称', trigger: 'blur' }]"
              >
                <el-input v-model="position.posName" placeholder="岗位名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item
                label="计划人数"
                :prop="`positions.${index}.planCount`"
                :rules="[{ required: true, message: '请输入计划人数', trigger: 'blur' }]"
              >
                <el-input-number v-model="position.planCount" :min="1" :max="999" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-button
                type="danger"
                :icon="Delete"
                circle
                :disabled="form.positions.length <= 1"
                @click="removePosition(index)"
              />
            </el-col>
          </el-row>
        </div>

        <el-form-item>
          <el-button type="primary" :icon="Plus" @click="addPosition">
            添加岗位
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布活动
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Star } from '@element-plus/icons-vue'
import { publishActivity } from '@/api/activity'
import { uploadActivityImage } from '@/api/image'
import { getDict } from '@/api/system'
import { useRegion } from '@/composables/useRegion'

const formRef = ref(null)
const submitting = ref(false)
const categoryList = ref([])
const targetAudienceList = ref([])

// 图片相关
const previewImages = ref([])  // 预览图片列表 { url, file }
const coverIndex = ref(0)      // 封面图片索引
const maxImages = 10

const {
  provinceList,
  cityList,
  districtList,
  selectedProvince,
  selectedCity,
  selectedDistrict,
  regionCode,
  loadProvinces,
  handleProvinceChange,
  handleCityChange,
  handleDistrictChange,
  resetRegion
} = useRegion()

const form = reactive({
  title: '',
  categoryId: '',
  targetAudience: [],
  startTime: '',
  endTime: '',
  description: '',
  positions: [
    { posName: '', planCount: 1 }
  ]
})

const rules = {
  title: [{ required: true, message: '请输入项目标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择服务类别', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadTargetAudience(),
    loadProvinces()
  ])
})

async function loadCategories() {
  try {
    const res = await getDict('service_category')
    categoryList.value = res
  } catch {
    ElMessage.error('加载服务类别失败')
  }
}

async function loadTargetAudience() {
  try {
    const res = await getDict('target_audience')
    targetAudienceList.value = res
  } catch {
    // 字典数据可能还没插入，静默处理
  }
}

function addPosition() {
  form.positions.push({ posName: '', planCount: 1 })
}

function removePosition(index) {
  if (form.positions.length > 1) {
    form.positions.splice(index, 1)
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()

    if (!regionCode.value) {
      ElMessage.warning('请选择项目属地')
      return
    }

    if (form.positions.some(p => !p.posName || !p.planCount)) {
      ElMessage.warning('请完善所有岗位信息')
      return
    }

    submitting.value = true

    const data = {
      title: form.title,
      categoryId: form.categoryId,
      regionCode: regionCode.value,
      targetAudience: Array.isArray(form.targetAudience) ? form.targetAudience.join(',') : form.targetAudience,
      startTime: form.startTime,
      endTime: form.endTime,
      description: form.description,
      positions: form.positions
    }

    // 发布活动，获取返回的activityId
    const activityId = await publishActivity(data)

    // 如果有图片，上传图片
    if (previewImages.value.length > 0 && activityId) {
      await uploadImages(activityId)
    }

    ElMessage.success('活动发布成功')
    handleReset()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.response?.data?.message || '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

// 上传图片
async function uploadImages(activityId) {
  for (let i = 0; i < previewImages.value.length; i++) {
    const img = previewImages.value[i]
    const isCover = i === coverIndex.value ? 1 : 0
    try {
      await uploadActivityImage(img.file, activityId, isCover)
    } catch (e) {
      console.error('图片上传失败:', e)
    }
  }
}

// 文件选择变化
function handleFileChange(file) {
  // 验证文件类型和大小
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式')
    return
  }
  if (file.raw.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  if (previewImages.value.length >= maxImages) {
    ElMessage.error(`最多只能上传 ${maxImages} 张图片`)
    return
  }

  // 添加到预览列表
  previewImages.value.push({
    url: URL.createObjectURL(file.raw),
    file: file.raw
  })
}

// 设置封面
function setCoverIndex(index) {
  coverIndex.value = index
}

// 移除预览图片
function removePreviewImage(index) {
  // 释放URL对象
  URL.revokeObjectURL(previewImages.value[index].url)
  previewImages.value.splice(index, 1)
  // 调整封面索引
  if (coverIndex.value >= previewImages.value.length) {
    coverIndex.value = Math.max(0, previewImages.value.length - 1)
  }
}

function handleReset() {
  formRef.value?.resetFields()
  form.positions = [{ posName: '', planCount: 1 }]
  resetRegion()
  // 清空图片预览
  previewImages.value.forEach(img => URL.revokeObjectURL(img.url))
  previewImages.value = []
  coverIndex.value = 0
}
</script>

<style scoped>
.publish-activity {
  padding: 20px;
}

.position-item {
  padding: 10px;
  margin-bottom: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

/* 图片预览区域 */
.image-preview-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #dcdfe6;
}

.preview-item:hover .preview-actions {
  opacity: 1;
}

.preview-actions {
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

.upload-btn {
  width: 120px;
  height: 120px;
}

.upload-btn .el-upload-dragger {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.upload-icon {
  font-size: 24px;
  color: #909399;
}

.upload-text {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.upload-tip {
  font-size: 10px;
  color: #909399;
  margin-top: 2px;
}

.image-count {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>