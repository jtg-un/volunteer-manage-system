<template>
  <div class="home-page">
    <el-card>
      <template #header>
        <span>欢迎使用志愿者管理系统</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.realName }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item v-if="userStore.isOrg" label="组织">{{ userStore.userInfo?.orgName }}</el-descriptions-item>
        <el-descriptions-item v-if="userStore.isOrg" label="审核状态">
          <el-tag :type="auditStatusType">{{ auditStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="userStore.isOrg && userStore.userInfo?.auditStatus === 2" label="拒绝原因" :span="2">
          <span style="color: #f56c6c">{{ userStore.userInfo?.rejectReason || '无' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 组织待审核提示 -->
      <el-alert
        v-if="userStore.isOrg && userStore.userInfo?.auditStatus === 0"
        title="您的组织入驻申请正在审核中，审核通过后即可使用系统功能。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-top: 20px"
      />

      <!-- 组织被拒绝提示 -->
      <el-alert
        v-if="userStore.isOrg && userStore.userInfo?.auditStatus === 2"
        title="您的组织入驻申请未通过审核，请查看拒绝原因。"
        type="error"
        show-icon
        :closable="false"
        style="margin-top: 20px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const roleText = computed(() => {
  const role = userStore.userInfo?.role
  if (role === 0) return '志愿者'
  if (role === 1) return '组织负责人'
  if (role === 2) return '系统管理员'
  return '未知角色'
})

const auditStatusText = computed(() => {
  const status = userStore.userInfo?.auditStatus
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '未知'
})

const auditStatusType = computed(() => {
  const status = userStore.userInfo?.auditStatus
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
})
</script>

<style scoped>
.home-page {
  padding: 20px;
}
</style>