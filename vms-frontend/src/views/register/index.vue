<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">{{ registerType === 0 ? '志愿者注册' : '组织注册' }}</h2>
      <el-radio-group v-if="!isFixedType" v-model="registerType" class="type-selector">
        <el-radio-button :value="0">志愿者注册</el-radio-button>
        <el-radio-button :value="1">组织注册</el-radio-button>
      </el-radio-group>

      <!-- 志愿者注册表单 -->
      <el-form
        v-if="registerType === 0"
        ref="volunteerFormRef"
        :model="volunteerForm"
        :rules="volunteerRules"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input v-model="volunteerForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="volunteerForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="volunteerForm.realName" placeholder="真实姓名" prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="volunteerForm.phone" placeholder="手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="volunteerForm.email" placeholder="邮箱（选填）" prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleVolunteerRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 组织注册表单 -->
      <el-form
        v-else
        ref="orgFormRef"
        :model="orgForm"
        :rules="orgRules"
        class="register-form"
        label-position="top"
      >
        <el-divider content-position="left">账号信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item prop="username">
              <el-input v-model="orgForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="password">
              <el-input
                v-model="orgForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="email">
          <el-input v-model="orgForm.email" placeholder="邮箱（选填）" prefix-icon="Message" />
        </el-form-item>

        <el-divider content-position="left">组织信息</el-divider>
        <el-form-item prop="orgName">
          <el-input v-model="orgForm.orgName" placeholder="队伍全称" prefix-icon="OfficeBuilding" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item prop="contactPerson">
              <el-input v-model="orgForm.contactPerson" placeholder="联系人" prefix-icon="UserFilled" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="contactPhone">
              <el-input v-model="orgForm.contactPhone" placeholder="联系电话" prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-input v-model="orgForm.address" placeholder="地址（选填）" prefix-icon="Location" />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="orgForm.intro"
            type="textarea"
            :rows="3"
            placeholder="组织简介（选填）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleOrgRegister">
            提交注册（需审核）
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        <span>已有账号？</span>
        <router-link :to="{ path: '/login', query: isFixedType ? { type: registerType } : {} }">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerVolunteer, registerOrg } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const registerType = ref(0)
const loading = ref(false)

const volunteerFormRef = ref(null)
const orgFormRef = ref(null)

// 是否为固定类型（从登录页跳转过来）
const isFixedType = computed(() => route.query.type !== undefined)

onMounted(() => {
  // 从登录页跳转时，根据 type 参数设置注册类型
  const type = route.query.type
  if (type === '0' || type === '1') {
    registerType.value = parseInt(type)
  }
})

// 志愿者表单
const volunteerForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: ''
})

// 组织表单
const orgForm = reactive({
  username: '',
  password: '',
  email: '',
  orgName: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  intro: ''
})

const phoneRule = { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
const usernameRule = { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名需为4-20位字母、数字或下划线', trigger: 'blur' }
const passwordRule = { pattern: /^.{6,20}$/, message: '密码需为6-20位', trigger: 'blur' }
const emailRule = { pattern: /^$|^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确', trigger: 'blur' }

const volunteerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, usernameRule],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, passwordRule],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [phoneRule],
  email: [emailRule]
}

const orgRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, usernameRule],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, passwordRule],
  orgName: [{ required: true, message: '请输入队伍全称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }, phoneRule],
  email: [emailRule]
}

async function handleVolunteerRegister() {
  try {
    await volunteerFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await registerVolunteer(volunteerForm)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

async function handleOrgRegister() {
  try {
    await orgFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await registerOrg(orgForm)
    ElMessage.success('注册成功，请等待审核')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-card {
  width: 500px;
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

.register-form {
  margin-top: 20px;
}

.register-btn {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>