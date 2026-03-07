<template>
  <div class="user-profile">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          <el-form :model="infoForm" label-width="80px" v-loading="infoLoading">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="80" :src="avatarPreview || infoForm.avatarUrl">
                  <el-icon :size="40"><UserFilled /></el-icon>
                </el-avatar>
                <div class="upload-actions">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                  >
                    <el-button type="primary" size="small">上传头像</el-button>
                  </el-upload>
                  <div class="upload-tip">支持 JPG、PNG、GIF、WEBP 格式，最大 5MB</div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="infoForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码卡片 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="passwordForm" label-width="80px" :rules="passwordRules" ref="passwordFormRef">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { updateUserInfo, updatePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const infoLoading = ref(false)
const infoForm = ref({
  realName: '',
  phone: '',
  avatarUrl: ''
})

const avatarPreview = ref('')

// 上传配置
const uploadUrl = '/api/file/avatar'

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  // 初始化表单数据
  if (userStore.userInfo) {
    infoForm.value.realName = userStore.userInfo.realName || ''
    infoForm.value.phone = userStore.userInfo.phone || ''
    infoForm.value.avatarUrl = userStore.userInfo.avatarUrl || ''
  }
})

// 上传前验证
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG、PNG、GIF、WEBP 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    infoForm.value.avatarUrl = response.data.url
    avatarPreview.value = response.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('头像上传失败，请重试')
}

const handleUpdateInfo = async () => {
  infoLoading.value = true
  try {
    const res = await updateUserInfo(infoForm.value)
    // 更新本地存储的用户信息
    userStore.userInfo = res
    ElMessage.success('信息更新成功')
  } catch (error) {
    console.error(error)
  } finally {
    infoLoading.value = false
  }
}

const handleUpdatePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}
</style>