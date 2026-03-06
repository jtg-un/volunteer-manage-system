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
          <el-cascader
            v-model="form.regionCodes"
            :options="regionOptions"
            :props="regionProps"
            placeholder="请选择项目属地"
            style="width: 100%"
            @change="handleRegionChange"
          />
        </el-form-item>

        <el-form-item label="服务对象" prop="targetAudience">
          <el-input v-model="form.targetAudience" placeholder="请输入服务对象，如：老年人、儿童等" />
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
import { Plus, Delete } from '@element-plus/icons-vue'
import { publishActivity } from '@/api/activity'
import { getDict, getRegionList } from '@/api/system'

const formRef = ref(null)
const submitting = ref(false)
const categoryList = ref([])
const regionOptions = ref([])

const form = reactive({
  title: '',
  categoryId: '',
  regionCodes: [],
  regionCode: '',
  targetAudience: '',
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

onMounted(async () => {
  await loadCategories()
  await loadProvinces()
})

async function loadCategories() {
  try {
    const res = await getDict('service_category')
    categoryList.value = res
  } catch {
    ElMessage.error('加载服务类别失败')
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

function handleRegionChange(value) {
  form.regionCode = value[value.length - 1] || ''
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

    if (form.positions.some(p => !p.posName || !p.planCount)) {
      ElMessage.warning('请完善所有岗位信息')
      return
    }

    submitting.value = true

    const data = {
      title: form.title,
      categoryId: form.categoryId,
      regionCode: form.regionCode,
      targetAudience: form.targetAudience,
      startTime: form.startTime,
      endTime: form.endTime,
      description: form.description,
      positions: form.positions
    }

    await publishActivity(data)
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

function handleReset() {
  formRef.value?.resetFields()
  form.positions = [{ posName: '', planCount: 1 }]
  form.regionCodes = []
  form.regionCode = ''
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
</style>