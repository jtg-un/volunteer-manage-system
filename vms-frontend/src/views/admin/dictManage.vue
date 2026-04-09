<template>
  <div class="dict-manage-page">
    <el-card>
      <template #header>
        <span>字典管理</span>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="字典类型">
          <el-select v-model="dictType" placeholder="全部" clearable style="width: 180px" @change="handleSearch">
            <el-option v-for="type in dictTypes" :key="type" :label="getDictTypeName(type)" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="搜索英文名/中文名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增字典</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="dictId" label="ID" width="80" />
        <el-table-column label="字典类型" width="120">
          <template #default="{ row }">
            {{ getDictTypeName(row.dictType) }}
          </template>
        </el-table-column>
        <el-table-column prop="dictKey" label="英文名" width="150" />
        <el-table-column prop="dictValue" label="中文名称" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑字典' : '新增字典'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典类型" prop="dictType">
          <el-select v-model="form.dictType" placeholder="选择字典类型" style="width: 100%">
            <el-option v-for="type in dictTypes" :key="type" :label="getDictTypeName(type)" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="英文名" prop="dictKey">
          <el-input v-model="form.dictKey" placeholder="如: education（英文标识）" />
        </el-form-item>
        <el-form-item label="中文名称" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="如: 教育服务（显示名称）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDictPage, getDictTypes, addDict, updateDict, deleteDict } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dictType = ref('')
const keyword = ref('')
const dictTypes = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  dictId: null,
  dictType: '',
  dictKey: '',
  dictValue: ''
})

const rules = {
  dictType: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  dictKey: [{ required: true, message: '请输入英文名', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入中文名称', trigger: 'blur' }]
}

// 字典类型名称映射
const dictTypeNameMap = {
  'service_category': '服务类型',
  'unit_type': '组织类型',
  'target_audience': '服务对象'
}

function getDictTypeName(type) {
  return dictTypeNameMap[type] || type
}

onMounted(async () => {
  await fetchDictTypes()
  fetchData()
})

async function fetchDictTypes() {
  try {
    dictTypes.value = await getDictTypes()
  } catch {
    dictTypes.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value
    }
    if (dictType.value) params.dictType = dictType.value
    if (keyword.value) params.keyword = keyword.value
    const data = await getDictPage(params)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

function handleReset() {
  dictType.value = ''
  keyword.value = ''
  pageNum.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  form.dictId = null
  form.dictType = ''
  form.dictKey = ''
  form.dictValue = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.dictId = row.dictId
  form.dictType = row.dictType
  form.dictKey = row.dictKey
  form.dictValue = row.dictValue
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    if (isEdit.value) {
      await updateDict(form)
      ElMessage.success('更新成功')
    } else {
      await addDict(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchDictTypes()
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该字典项？', '提示', {
      type: 'warning'
    })
    await deleteDict(row.dictId)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}
</script>

<style scoped>
.dict-manage-page {
  padding: 20px;
}
</style>