<template>
  <div class="region-manage-page">
    <el-card>
      <template #header>
        <span>行政区划管理</span>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="层级">
          <el-select v-model="level" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="省级" :value="1" />
            <el-option label="市级" :value="2" />
            <el-option label="区县级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="搜索编码/名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增区划</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="regionCode" label="区划编码" width="120" />
        <el-table-column prop="regionName" label="区划名称" min-width="150" />
        <el-table-column prop="parentCode" label="父级编码" width="120">
          <template #default="{ row }">
            {{ row.parentCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="层级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有子级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.hasChildren" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.hasChildren">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑区划' : '新增区划'" width="550px">
      <!-- 编码规则说明 -->
      <el-alert
        v-if="!isEdit"
        title="区划编码规则说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div style="font-size: 13px; line-height: 1.8;">
            <p><strong>省级：</strong>2位数字，如 11=北京市、31=上海市、32=江苏省</p>
            <p><strong>市级：</strong>4位数字，前2位为省编码，如 3201=南京市</p>
            <p><strong>区县级：</strong>6位数字，前4位为市编码，如 320102=玄武区</p>
            <p style="color: #E6A23C;"><strong>直辖市特殊：</strong>北京(11)/天津(12)/上海(31)/重庆(50) 为省级，无市级，下辖的区为区县级(6位)</p>
          </div>
        </template>
        <template #title>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>区划编码规则说明</span>
            <el-button type="primary" size="small" @click="showLookupDialog = true">
              查询编码
            </el-button>
          </div>
        </template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="父级区划">
          <el-cascader
            v-model="form.parentCodes"
            :options="parentOptions"
            :props="cascaderProps"
            placeholder="选择父级区划（可留空）"
            clearable
            style="width: 100%"
            @change="handleParentChange"
          />
          <div class="form-tip">选择父级后，层级将自动推断</div>
        </el-form-item>
        <el-form-item label="区划编码" prop="regionCode">
          <el-input v-model="form.regionCode" placeholder="如: 110101" :disabled="isEdit">
            <template #append>
              <el-button v-if="!isEdit && form.parentCode" @click="generateCode">自动生成</el-button>
            </template>
          </el-input>
          <div class="form-tip" v-if="!isEdit">
            当前层级：<strong>{{ currentLevelText }}</strong>，编码应为 {{ codeLengthText }}
          </div>
        </el-form-item>
        <el-form-item label="区划名称" prop="regionName">
          <el-input v-model="form.regionName" placeholder="如: 东城区" />
        </el-form-item>
        <el-form-item label="层级" prop="level">
          <el-radio-group v-model="form.level" :disabled="!!form.parentCode">
            <el-radio :value="1">省级</el-radio>
            <el-radio :value="2">市级</el-radio>
            <el-radio :value="3">区县级</el-radio>
          </el-radio-group>
          <div class="form-tip" v-if="form.parentCode">层级根据父级自动设置</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 区划编码查询弹窗 -->
    <el-dialog
      v-model="showLookupDialog"
      title="区划编码查询"
      width="600px"
      append-to-body
    >
      <el-form :inline="true" style="margin-bottom: 15px;">
        <el-form-item>
          <el-input
            v-model="lookupKeyword"
            placeholder="输入区划名称搜索"
            clearable
            style="width: 200px"
            @input="handleLookupSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="lookupLevel" placeholder="全部层级" clearable style="width: 100px" @change="handleLookupSearch">
            <el-option label="省级" :value="1" />
            <el-option label="市级" :value="2" />
            <el-option label="区县级" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="lookupPagedData" stripe max-height="400" v-loading="lookupLoading">
        <el-table-column prop="name" label="区划名称" min-width="180">
          <template #default="{ row }">
            <span :style="{ paddingLeft: (row.level - 1) * 15 + 'px' }">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column label="层级" width="80">
          <template #default="{ row }">
            <el-tag :type="getLookupLevelType(row.level)" size="small">{{ getLookupLevelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="copyLookupCode(row.code)">
              复制
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="lookupPage"
        :page-size="lookupPageSize"
        :total="lookupFiltered.length"
        layout="total, prev, pager, next"
        style="margin-top: 15px; justify-content: flex-end"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRegionPage, getRegionList, addRegion, updateRegion, deleteRegion } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const level = ref(null)
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const parentOptions = ref([])

// 区划查询弹窗
const showLookupDialog = ref(false)
const lookupData = ref([])
const lookupKeyword = ref('')
const lookupLevel = ref(null)
const lookupPage = ref(1)
const lookupPageSize = 20
const lookupLoading = ref(false)

const form = reactive({
  regionCode: '',
  regionName: '',
  parentCode: '',
  parentCodes: [],
  level: null
})

const rules = {
  regionCode: [{ required: true, message: '请输入区划编码', trigger: 'blur' }],
  regionName: [{ required: true, message: '请输入区划名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择层级', trigger: 'change' }]
}

const cascaderProps = {
  value: 'regionCode',
  label: 'regionName',
  children: 'children',
  checkStrictly: true,
  lazy: true,
  lazyLoad: async (node, resolve) => {
    const { level: nodeLevel, value } = node
    const parentCode = nodeLevel === 0 ? '' : value
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

// 当前层级文字
const currentLevelText = computed(() => {
  const lvl = form.level
  const map = { 1: '省级', 2: '市级', 3: '区县级' }
  return map[lvl] || '未设置'
})

// 编码长度提示
const codeLengthText = computed(() => {
  const lvl = form.level
  const map = { 1: '2位数字', 2: '4位数字', 3: '6位数字' }
  return map[lvl] || ''
})

// 查询弹窗筛选后的数据
const lookupFiltered = computed(() => {
  let result = lookupData.value

  if (lookupKeyword.value) {
    const keyword = lookupKeyword.value.toLowerCase()
    result = result.filter(item => item.name.toLowerCase().includes(keyword))
  }

  if (lookupLevel.value) {
    result = result.filter(item => item.level === lookupLevel.value)
  }

  return result
})

// 查询弹窗分页数据
const lookupPagedData = computed(() => {
  const start = (lookupPage.value - 1) * lookupPageSize
  const end = start + lookupPageSize
  return lookupFiltered.value.slice(start, end)
})

onMounted(() => {
  fetchData()
  loadParentOptions()
  loadLookupData()
})

// 加载区划查询数据
async function loadLookupData() {
  lookupLoading.value = true
  try {
    const response = await fetch('/区划.txt')
    const text = await response.text()
    const lines = text.split('\n')

    const data = []
    for (const line of lines) {
      // 匹配格式：名称 代码（如：北京市 110000）
      const match = line.trim().match(/^(.+?)\s+(\d{6})$/)
      if (match) {
        const name = match[1].trim()
        const code = match[2]

        // 根据编码判断层级
        let level = 1
        if (code.endsWith('0000')) {
          level = 1 // 省级
        } else if (code.endsWith('00')) {
          level = 2 // 市级
        } else {
          level = 3 // 区县级
        }

        data.push({ name, code, level })
      }
    }

    lookupData.value = data
  } catch (error) {
    console.error('加载区划数据失败:', error)
  } finally {
    lookupLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value
    }
    if (level.value) params.level = level.value
    if (keyword.value) params.keyword = keyword.value
    const data = await getRegionPage(params)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadParentOptions() {
  try {
    const res = await getRegionList('')
    parentOptions.value = res.map(item => ({
      regionCode: item.regionCode,
      regionName: item.regionName,
      leaf: !item.hasChildren
    }))
  } catch {
    parentOptions.value = []
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

function handleReset() {
  level.value = null
  keyword.value = ''
  pageNum.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  form.regionCode = ''
  form.regionName = ''
  form.parentCode = ''
  form.parentCodes = []
  form.level = null
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.regionCode = row.regionCode
  form.regionName = row.regionName
  form.parentCode = row.parentCode || ''
  form.parentCodes = []
  form.level = row.level
  dialogVisible.value = true
}

// 父级变更，自动推断层级
function handleParentChange(values) {
  if (values && values.length > 0) {
    form.parentCode = values[values.length - 1]
    // 根据父级编码长度推断层级
    const parentLen = form.parentCode.length
    // 直辖市编码：11北京、12天津、31上海、50重庆
    const municipalityCodes = ['11', '12', '31', '50']
    const isMunicipality = municipalityCodes.includes(form.parentCode)

    if (parentLen === 2) {
      if (isMunicipality) {
        // 直辖市没有市级，直接到区县级
        form.level = 3
      } else {
        // 普通省份，子级是市级
        form.level = 2
      }
    } else if (parentLen === 4) {
      form.level = 3 // 父级是市，当前是区
    }
  } else {
    form.parentCode = ''
    form.level = 1 // 无父级，默认省级
  }
}

// 自动生成编码
function generateCode() {
  if (!form.parentCode) {
    ElMessage.warning('请先选择父级区划')
    return
  }

  // 直辖市编码
  const municipalityCodes = ['11', '12', '31', '50']
  const parentLen = form.parentCode.length
  const isMunicipality = municipalityCodes.includes(form.parentCode)

  if (parentLen === 2) {
    if (isMunicipality) {
      // 直辖市下新增区：父级2位 + 中间2位(01市辖区) + 2位序号 = 6位
      form.regionCode = form.parentCode + '0101'
    } else {
      // 普通省下新增市：父级2位 + 2位序号 = 4位
      form.regionCode = form.parentCode + '01'
    }
  } else if (parentLen === 4) {
    // 市下新增区：父级4位 + 2位序号 = 6位
    form.regionCode = form.parentCode + '01'
  }
  ElMessage.info('已生成初始编码，请根据实际情况调整')
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    const data = {
      regionCode: form.regionCode,
      regionName: form.regionName,
      parentCode: form.parentCode || null,
      level: form.level
    }
    if (isEdit.value) {
      await updateRegion(data)
      ElMessage.success('更新成功')
    } else {
      await addRegion(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
    loadParentOptions()
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该区划？', '提示', {
      type: 'warning'
    })
    await deleteRegion(row.regionCode)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

function levelText(lvl) {
  const map = { 1: '省级', 2: '市级', 3: '区县级' }
  return map[lvl] || '未知'
}

function levelTagType(lvl) {
  const map = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[lvl] || 'info'
}

function handleLookupSearch() {
  lookupPage.value = 1
}

function copyLookupCode(code) {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('编码已复制: ' + code)
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function getLookupLevelText(level) {
  const map = { 1: '省级', 2: '市级', 3: '区县级' }
  return map[level] || '未知'
}

function getLookupLevelType(level) {
  const map = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[level] || 'info'
}
</script>

<style scoped>
.region-manage-page {
  padding: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-alert__content) {
  padding: 0;
}

:deep(.el-alert__description) {
  margin-top: 0;
}
</style>