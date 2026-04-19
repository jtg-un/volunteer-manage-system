<template>
  <div class="my-activity">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>我的活动</span>
          <el-button type="primary" @click="$router.push('/org/publish-activity')">
            发布新活动
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="关键词">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索活动标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待审核" :value="3" />
            <el-option label="待启动" :value="0" />
            <el-option label="运行中" :value="1" />
            <el-option label="已结项" :value="2" />
            <el-option label="已拒绝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类别">
          <el-select v-model="searchCategory" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option
              v-for="item in categoryList"
              :key="item.dictKey"
              :label="item.dictValue"
              :value="item.dictKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column label="活动标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="服务类别" width="100" />
        <el-table-column prop="regionName" label="所属地区" width="120" />
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="120">
          <template #default="{ row }">
            {{ row.totalCurrentCount }} / {{ row.totalPlanCount }}
          </template>
        </el-table-column>
        <el-table-column prop="pendingRegCount" label="待审核报名" width="100">
          <template #default="{ row }">
            <el-badge :value="row.pendingRegCount" :hidden="row.pendingRegCount === 0">
              <span>{{ row.pendingRegCount }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="info"
              size="small"
              @click="handleImageManage(row)"
            >
              图片
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleStatusChange(row, 1)"
            >
              启动
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="info"
              size="small"
              @click="handleStatusChange(row, 2)"
            >
              结项
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑活动" width="700px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        v-loading="editLoading"
      >
        <el-form-item label="项目标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入项目标题" />
        </el-form-item>

        <el-form-item label="服务类别" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择服务类别" style="width: 100%">
            <el-option
              v-for="item in categoryList"
              :key="item.dictKey"
              :label="item.dictValue"
              :value="item.dictKey"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目属地" prop="regionCode">
          <el-cascader
            v-model="editForm.regionCodes"
            :options="regionOptions"
            :props="regionProps"
            placeholder="请选择项目属地"
            style="width: 100%"
            @change="handleEditRegionChange"
          />
        </el-form-item>

        <el-form-item label="服务对象" prop="targetAudience">
          <el-select
            v-model="editForm.targetAudience"
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

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="editForm.startTime"
            type="datetime"
            placeholder="开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="editForm.endTime"
            type="datetime"
            placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>

        <!-- 岗位设置 -->
        <el-divider content-position="left">岗位设置</el-divider>

        <div v-for="(position, index) in editForm.positions" :key="index" class="position-item">
          <el-row :gutter="20">
            <el-col :span="10">
              <el-form-item
                :label="`岗位${index + 1}`"
                :prop="`positions.${index}.posName`"
                :rules="[{ required: true, message: '请输入岗位名称', trigger: 'blur' }]"
              >
                <el-input v-model="position.posName" placeholder="岗位名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item
                label="人数"
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
                :disabled="editForm.positions.length <= 1"
                @click="removeEditPosition(index)"
              />
            </el-col>
          </el-row>
        </div>

        <el-form-item>
          <el-button type="primary" :icon="Plus" @click="addEditPosition">
            添加岗位
          </el-button>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleUpdateSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片管理弹窗 -->
    <el-dialog v-model="imageVisible" title="活动图片管理" width="600px" destroy-on-close>
      <div class="image-dialog-content">
        <div class="image-tip">
          <el-alert
            title="上传活动图片，设置封面后将在活动列表展示"
            type="info"
            :closable="false"
          />
        </div>
        <ImageUpload
          ref="imageUploadRef"
          biz-type="activity"
          :biz-id="currentImageActivityId"
          :max-count="10"
          :initial-images="currentActivityImages"
        />
      </div>
      <template #footer>
        <el-button @click="imageVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getMyActivities, updateActivityStatus, updateActivity, cancelActivity } from '@/api/activity'
import { getDict, getRegionList } from '@/api/system'
import { getActivityImages } from '@/api/image'
import OrgActivityDetailDialog from '@/components/org/OrgActivityDetailDialog.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 搜索筛选
const searchKeyword = ref('')
const searchStatus = ref(null)
const searchCategory = ref('')
const categoryList = ref([])

const detailVisible = ref(false)
const currentActivityId = ref(null)

// 图片管理
const imageVisible = ref(false)
const currentImageActivityId = ref(null)
const currentActivityImages = ref([])
const imageUploadRef = ref(null)

// 编辑相关
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const targetAudienceList = ref([])
const regionOptions = ref([])

const editForm = reactive({
  activityId: null,
  title: '',
  categoryId: '',
  regionCodes: [],
  regionCode: '',
  targetAudience: [],
  startTime: '',
  endTime: '',
  description: '',
  positions: []
})

const editRules = {
  title: [{ required: true, message: '请输入项目标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择服务类别', trigger: 'change' }],
  regionCode: [{ required: true, message: '请选择项目属地', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const regionProps = {
  value: 'regionCode',
  label: 'regionName',
  children: 'children',
  lazy: true,
  lazyLoad: async (node, resolve) => {
    const { level, value } = node
    const parentCode = level === 0 ? '' : value
    try {
      const res = await getRegionList(parentCode)
      const nodes = res.map(item => ({
        regionCode: item.regionCode,
        regionName: item.regionName,
        leaf: item.level >= 3 || !item.hasChildren
      }))
      resolve(nodes)
    } catch {
      resolve([])
    }
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchStatus.value !== null) params.status = searchStatus.value
    if (searchCategory.value) params.categoryId = searchCategory.value

    const res = await getMyActivities(params)
    tableData.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error(error)
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

const handleReset = () => {
  searchKeyword.value = ''
  searchStatus.value = null
  searchCategory.value = ''
  pagination.value.page = 1
  fetchData()
}

const handleDetail = (row) => {
  currentActivityId.value = row.activityId
  detailVisible.value = true
}

const handleStatusChange = (row, targetStatus) => {
  const statusText = targetStatus === 1 ? '启动' : '结项'
  ElMessageBox.confirm(`确定要${statusText}该活动吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await updateActivityStatus({
        activityId: row.activityId,
        status: targetStatus
      })
      ElMessage.success('状态更新成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm('取消活动将删除所有报名记录，确定要取消吗？', '警告', {
    type: 'warning',
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想'
  }).then(async () => {
    try {
      await cancelActivity(row.activityId)
      ElMessage.success('活动已取消')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const handleImageManage = async (row) => {
  currentImageActivityId.value = row.activityId
  try {
    const res = await getActivityImages(row.activityId)
    currentActivityImages.value = res || []
  } catch {
    currentActivityImages.value = []
  }
  imageVisible.value = true
}

const handleEdit = async (row) => {
  // 加载字典和地区
  await loadCategories()
  await loadTargetAudience()
  await loadProvinces()

  // 填充表单
  editForm.activityId = row.activityId
  editForm.title = row.title
  editForm.categoryId = row.categoryId
  editForm.regionCode = row.regionCode
  // 将逗号分隔的字符串转为数组
  editForm.targetAudience = row.targetAudience ? row.targetAudience.split(',') : []
  editForm.startTime = row.startTime
  editForm.endTime = row.endTime
  editForm.description = row.description

  // 获取活动详情以获取岗位信息
  try {
    const res = await getMyActivities({ page: 1, size: 1 })
    // 这里需要调用详情接口获取岗位，暂时用列表数据
    editForm.positions = row.positions || [{ posId: null, posName: '', planCount: 1 }]
  } catch (error) {
    console.error(error)
    editForm.positions = [{ posId: null, posName: '', planCount: 1 }]
  }

  editVisible.value = true
}

const handleEditRegionChange = (value) => {
  editForm.regionCode = value[value.length - 1] || ''
}

const addEditPosition = () => {
  editForm.positions.push({ posId: null, posName: '', planCount: 1 })
}

const removeEditPosition = (index) => {
  if (editForm.positions.length > 1) {
    editForm.positions.splice(index, 1)
  }
}

const handleUpdateSubmit = async () => {
  try {
    await editFormRef.value.validate()

    if (editForm.positions.some(p => !p.posName || !p.planCount)) {
      ElMessage.warning('请完善所有岗位信息')
      return
    }

    editLoading.value = true

    const data = {
      activityId: editForm.activityId,
      title: editForm.title,
      categoryId: editForm.categoryId,
      regionCode: editForm.regionCode,
      targetAudience: Array.isArray(editForm.targetAudience) ? editForm.targetAudience.join(',') : editForm.targetAudience,
      startTime: editForm.startTime,
      endTime: editForm.endTime,
      description: editForm.description,
      positions: editForm.positions
    }

    await updateActivity(data)
    ElMessage.success('活动更新成功')
    editVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error(error)
    }
  } finally {
    editLoading.value = false
  }
}

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
    // 静默处理
  }
}

async function loadProvinces() {
  try {
    const res = await getRegionList('')
    regionOptions.value = res.map(item => ({
      regionCode: item.regionCode,
      regionName: item.regionName,
      children: item.hasChildren ? [] : undefined,
      leaf: !item.hasChildren
    }))
  } catch {
    ElMessage.error('加载行政区划失败')
  }
}

const getStatusType = (status) => {
  const types = {
    0: 'info',      // 待启动
    1: 'success',   // 运行中
    2: 'warning',   // 已结项
    3: 'warning',   // 待审核
    4: 'danger'     // 已拒绝
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}

const formatDateTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  await loadCategories()
  fetchData()
})
</script>

<style scoped>
.my-activity {
  padding: 20px;
}

.position-item {
  padding: 10px;
  margin-bottom: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.image-dialog-content {
  padding: 10px 0;
}

.image-tip {
  margin-bottom: 16px;
}
</style>