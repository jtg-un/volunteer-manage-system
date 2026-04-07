<template>
  <div class="org-audit-page">
    <el-card>
      <template #header>
        <span>组织审核管理</span>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="搜索队伍名称/联系人/电话"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="auditStatus" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位类型">
          <el-select v-model="unitType" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option
              v-for="item in unitTypeList"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属地区">
          <el-select v-model="selectedProvince" placeholder="省份" clearable style="width: 120px" @change="handleProvinceChange">
            <el-option
              v-for="item in provinceList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
          <el-select v-model="selectedCity" placeholder="城市" clearable style="width: 120px; margin-left: 10px" @change="handleSearch">
            <el-option
              v-for="item in cityList"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orgId" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="orgName" label="队伍名称" min-width="150" />
        <el-table-column prop="unitTypeName" label="单位类型" width="100">
          <template #default="{ row }">
            {{ row.unitTypeName || row.unitType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="所属地区" width="120">
          <template #default="{ row }">
            {{ row.regionName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
            <el-button v-if="row.auditStatus === 0" type="success" link @click="handleAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" type="danger" link @click="handleAudit(row, 2)">拒绝</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="组织详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrg">
        <el-descriptions-item label="账号">{{ currentOrg.username }}</el-descriptions-item>
        <el-descriptions-item label="队伍名称">{{ currentOrg.orgName }}</el-descriptions-item>
        <el-descriptions-item label="联络编号">{{ currentOrg.orgCode || '未生成' }}</el-descriptions-item>
        <el-descriptions-item label="单位类型">{{ currentOrg.unitType || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="所属地区">{{ currentOrg.regionName || currentOrg.regionCode || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="成立日期">{{ currentOrg.foundDate || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrg.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrg.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentOrg.address || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ currentOrg.intro || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="statusTagType(currentOrg.auditStatus)">{{ statusText(currentOrg.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="currentOrg.auditStatus === 2">
          <span style="color: #f56c6c">{{ currentOrg.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="currentOrg && currentOrg.auditStatus === 0">
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="danger" @click="handleAudit(currentOrg, 2)">拒绝</el-button>
        <el-button type="success" @click="handleAudit(currentOrg, 1)">通过</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrgList, getOrgDetail, auditOrg } from '@/api/org'
import { getDict } from '@/api/system'
import { getRegionList } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const auditStatus = ref(null)
const unitType = ref('')
const selectedProvince = ref('')
const selectedCity = ref('')

// 字典和地区数据
const unitTypeList = ref([])
const provinceList = ref([])
const cityList = ref([])

const detailVisible = ref(false)
const currentOrg = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const pendingOrg = ref(null)

onMounted(async () => {
  // 加载单位类型字典
  try {
    unitTypeList.value = await getDict('unit_type')
  } catch {
    unitTypeList.value = []
  }
  // 加载省级地区
  try {
    provinceList.value = await getRegionList('')
  } catch {
    provinceList.value = []
  }
  fetchData()
})

// 省份变更，加载市级
async function handleProvinceChange(provinceCode) {
  selectedCity.value = ''
  cityList.value = []
  if (provinceCode) {
    try {
      cityList.value = await getRegionList(provinceCode)
    } catch {
      cityList.value = []
    }
  }
  handleSearch()
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    if (auditStatus.value !== null) {
      params.auditStatus = auditStatus.value
    }
    if (unitType.value) {
      params.unitType = unitType.value
    }
    // 地区筛选：优先使用市级，如果没有市级则使用省级
    if (selectedCity.value) {
      params.regionCode = selectedCity.value
    } else if (selectedProvince.value) {
      params.regionCode = selectedProvince.value
    }
    const data = await getOrgList(params)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取组织列表失败:', error)
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
  keyword.value = ''
  auditStatus.value = null
  unitType.value = ''
  selectedProvince.value = ''
  selectedCity.value = ''
  cityList.value = []
  pageNum.value = 1
  fetchData()
}

function statusText(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

async function handleViewDetail(row) {
  const data = await getOrgDetail(row.orgId)
  currentOrg.value = data
  detailVisible.value = true
}

function handleAudit(row, status) {
  if (status === 2) {
    pendingOrg.value = row
    rejectReason.value = ''
    rejectVisible.value = true
  } else {
    ElMessageBox.confirm('确认通过该组织的入驻申请？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      doAudit(row.orgId, 1, null)
    })
  }
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  await doAudit(pendingOrg.value.orgId, 2, rejectReason.value)
  rejectVisible.value = false
  detailVisible.value = false
}

async function doAudit(orgId, auditStatus, rejectReason) {
  const data = { orgId, auditStatus }
  if (rejectReason) {
    data.rejectReason = rejectReason
  }
  await auditOrg(data)
  ElMessage.success('审核成功')
  fetchData()
}
</script>

<style scoped>
.org-audit-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>