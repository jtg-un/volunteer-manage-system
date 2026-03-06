<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">志愿者管理系统</h2>
      <el-radio-group v-model="loginType" class="type-selector">
        <el-radio-button :value="0">志愿者登录</el-radio-button>
        <el-radio-button :value="1">组织登录</el-radio-button>
        <el-radio-button :value="2">管理员登录</el-radio-button>
      </el-radio-group>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-link">
        <template v-if="loginType === 0">
          <span>还没有账号？</span>
          <router-link :to="{ path: '/register', query: { type: 0 } }">立即注册</router-link>
        </template>
        <template v-else-if="loginType === 1">
          <span>还没有账号？</span>
          <router-link :to="{ path: '/register', query: { type: 1 } }">立即注册</router-link>
          <span class="tips">（注册后需审核通过方可登录）</span>
        </template>
        <template v-else>
          <span>管理员账号由系统分配</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const loginType = ref(0)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const roleNames = {
  0: '志愿者',
  1: '组织负责人',
  2: '系统管理员'
}

onMounted(() => {
  // 从注册页跳转时，根据 type 参数设置登录类型
  const type = route.query.type
  if (type === '0' || type === '1') {
    loginType.value = parseInt(type)
  }
})

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const data = await userStore.login(form.username, form.password)
    // 检查登录角色是否匹配选择的角色
    if (data.role !== loginType.value) {
      ElMessage.warning(`该账号是${roleNames[data.role]}，请选择正确的登录类型`)
      return
    }
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.type-selector :deep(.el-radio-button__inner) {
  padding: 8px 15px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}

.register-link .tips {
  color: #999;
  font-size: 12px;
}
</style>