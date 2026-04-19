<template>
  <div class="guest-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-left">
        <router-link to="/welcome" class="logo">
          <el-icon size="24"><UserFilled /></el-icon>
          <span>志愿服务管理系统</span>
        </router-link>
      </div>
      <div class="navbar-center">
        <router-link to="/activity/list" class="nav-link">活动列表</router-link>
        <router-link to="/welcome" class="nav-link">平台介绍</router-link>
      </div>
      <div class="navbar-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatarUrl" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/')">进入系统</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" round @click="$router.push('/login')">登录</el-button>
          <el-button round @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <slot />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>志愿服务管理系统 © 2026 | 汇聚爱心力量，共建美好社会</p>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

function handleLogout() {
  userStore.logout()
  router.push('/welcome')
}
</script>

<style scoped>
.guest-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.navbar-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-size: 18px;
  font-weight: 600;
  text-decoration: none;
}

.logo:hover {
  color: #764ba2;
}

.navbar-center {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: #606266;
  font-size: 15px;
  text-decoration: none;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #667eea;
}

.navbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #303133;
  font-size: 14px;
}

.main-content {
  flex: 1;
}

.footer {
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.7);
  padding: 24px;
  text-align: center;
  font-size: 14px;
}
</style>