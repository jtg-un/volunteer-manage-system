<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>用户管理</span>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="角色">
          <el-select v-model="filterRole" placeholder="全部" clearable @change="fetchData">
            <el-option label="志愿者" :value="0" />
            <el-option label="组织负责人" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orgName" label="所属组织" min-width="150">
          <template #default="{ row }">
            {{ row.orgName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleStatusChange(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleStatusChange(row, 1)"
            >
              启用
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const filterRole = ref(null)
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      role: filterRole.value,
      page: pagination.value.page,
      size: pagination.value.size
    })
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

const handleStatusChange = (row, targetStatus) => {
  const statusText = targetStatus === 0 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${statusText}该用户吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await updateUserStatus({
        userId: row.userId,
        status: targetStatus
      })
      ElMessage.success('状态更新成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const getRoleName = (role) => {
  const names = {
    0: '志愿者',
    1: '组织负责人',
    2: '管理员'
  }
  return names[role] || '未知'
}

const getRoleType = (role) => {
  const types = {
    0: 'primary',
    1: 'warning',
    2: 'danger'
  }
  return types[role] || 'info'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}
</style>