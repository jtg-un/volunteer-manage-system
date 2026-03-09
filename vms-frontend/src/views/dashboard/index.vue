<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">VMS</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/activity/list">
          <el-icon><List /></el-icon>
          <span>活动列表</span>
        </el-menu-item>

        <!-- 志愿者菜单 -->
        <el-sub-menu v-if="userStore.isVolunteer" index="volunteer">
          <template #title>
            <el-icon><User /></el-icon>
            <span>志愿者中心</span>
          </template>
          <el-menu-item index="/volunteer/my-registrations">我的报名</el-menu-item>
          <el-menu-item index="/volunteer/records">时长记录</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

        <!-- 组织负责人菜单 -->
        <el-sub-menu v-if="userStore.isOrg" index="org">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>组织管理</span>
          </template>
          <el-menu-item index="/org/profile">组织信息</el-menu-item>
          <el-menu-item index="/org/publish-activity">发布活动</el-menu-item>
          <el-menu-item index="/org/my-activity">我的活动</el-menu-item>
          <el-menu-item index="/org/registrations">报名管理</el-menu-item>
        </el-sub-menu>

        <!-- 管理员菜单 -->
        <el-sub-menu v-if="userStore.isAdmin" index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/org-audit">组织审核</el-menu-item>
          <el-menu-item index="/admin/activity-audit">活动审核</el-menu-item>
          <el-menu-item index="/admin/user-manage">用户管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <span class="title">志愿者管理系统</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatarUrl" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  {{ roleText }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, ArrowDown, OfficeBuilding, Checked, List, Setting, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const roleText = computed(() => {
  const role = userStore.userInfo?.role
  if (role === 0) return '志愿者'
  if (role === 1) return '组织负责人'
  if (role === 2) return '系统管理员'
  return '未知角色'
})

onMounted(async () => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
      router.push('/login')
    }
  }
})

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
  background-color: #263445;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-content {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #333;
}

.el-main {
  background-color: #f0f2f5;
}
</style>