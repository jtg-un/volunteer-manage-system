<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">志愿服务管理系统</h2>
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
  /* 1. 背景改为极简的浅灰色，这是政务网站最常用的背景色 */
  background-color: #f5f7fa;
  /* 可选：增加一个非常淡的背景纹理，增加质感 */
  background-image: radial-gradient(#dcdfe6 0.5px, transparent 0.5px);
  background-size: 20px 20px;
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  /* 2. 圆角调小一点（4px），显得更严谨 */
  border-radius: 4px;
  /* 3. 增加更有层次感的投影，让白色卡片在白色背景上也能“立起来” */
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  /* 4. 在顶部加一条“志愿红”装饰条，一秒对齐中国志愿服务网风格 */
  border-top: 5px solid #e60012;
}

.title {
  text-align: center;
  /* 5. 标题颜色可以改为深灰或者志愿红 */
  color: #e60012;
  font-weight: bold;
  letter-spacing: 2px;
  margin-bottom: 30px;
}

.type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
}

/* 6. 深度修改 Element Plus 选中状态的颜色为红色 */
.type-selector :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background-color: #e60012;
  border-color: #e60012;
  box-shadow: -1px 0 0 0 #e60012;
}

.login-form {
  margin-top: 10px;
}

.login-btn {
  width: 100%;
  /* 7. 登录按钮改为红色 */
  background-color: #e60012;
  border-color: #e60012;
  height: 45px;
  font-size: 16px;
}

.login-btn:hover {
  background-color: #ff1a2b;
  border-color: #ff1a2b;
}

.register-link {
  text-align: center;
  margin-top: 25px;
  color: #606266;
  font-size: 14px;
}

.register-link a {
  color: #e60012; /* 链接也改用红色系 */
  font-weight: 500;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>