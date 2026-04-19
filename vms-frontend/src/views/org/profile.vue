<template>
  <div class="org-profile-page">
    <el-card v-if="orgInfo">
      <template #header>
        <div class="card-header">
          <span>组织信息</span>
          <el-tag :type="statusTagType(orgInfo.auditStatus)" size="large">{{ statusText(orgInfo.auditStatus) }}</el-tag>
        </div>
      </template>

      <!-- 待审核状态 -->
      <div v-if="orgInfo.auditStatus === 0" class="status-notice">
        <el-alert title="您的组织入驻申请正在审核中，请耐心等待管理员审核。" type="warning" show-icon :closable="false" />
      </div>

      <!-- 已拒绝状态 -->
      <div v-if="orgInfo.auditStatus === 2" class="status-notice">
        <el-alert :title="`您的组织入驻申请已被拒绝。原因：${orgInfo.rejectReason || '无'}`" type="error" show-icon :closable="false" />
      </div>

      <!-- 已通过状态 - 可编辑 -->
      <el-form v-if="orgInfo.auditStatus === 1" ref="formRef" :model="form" :rules="rules" label-width="100px" class="org-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="队伍名称" prop="orgName">
              <el-input v-model="form.orgName" placeholder="请输入队伍名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位类型" prop="unitType">
              <el-select v-model="form.unitType" placeholder="请选择单位类型" style="width: 100%">
                <el-option v-for="item in unitTypeList" :key="item.dictKey" :label="item.dictValue" :value="item.dictKey" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成立日期">
          <el-date-picker v-model="form.foundDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入组织简介" />
        </el-form-item>

        <!-- 组织风采图片 -->
        <el-divider content-position="left">组织风采</el-divider>
        <el-form-item label="活动照片">
          <ImageUpload
            ref="galleryRef"
            biz-type="org"
            :biz-id="orgInfo.orgId"
            :max-count="8"
            :initial-images="galleryImages"
            @change="handleGalleryChange"
          />
          <div class="gallery-tip">展示组织过往活动的精彩瞬间，最多8张图片</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>

      <!-- 待审核/已拒绝状态 - 只读展示 -->
      <el-descriptions v-if="orgInfo.auditStatus !== 1" :column="2" border>
        <el-descriptions-item label="队伍名称">{{ orgInfo.orgName }}</el-descriptions-item>
        <el-descriptions-item label="单位类型">{{ orgInfo.unitType || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ orgInfo.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orgInfo.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="成立日期">{{ orgInfo.foundDate || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ orgInfo.address || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ orgInfo.intro || '未填写' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyOrg, updateOrg, getDict } from '@/api/org'
import { getMyOrgGallery } from '@/api/image'
import ImageUpload from '@/components/common/ImageUpload.vue'

const loading = ref(false)
const orgInfo = ref(null)
const formRef = ref(null)
const galleryRef = ref(null)
const unitTypeList = ref([])
const galleryImages = ref([])

const form = reactive({
  orgName: '',
  unitType: '',
  contactPerson: '',
  contactPhone: '',
  foundDate: '',
  address: '',
  intro: ''
})

const rules = {
  orgName: [{ required: true, message: '请输入队伍名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await fetchOrgInfo()
  await fetchUnitType()
  await fetchGallery()
})

async function fetchGallery() {
  if (orgInfo.value?.auditStatus === 1) {
    try {
      const data = await getMyOrgGallery()
      galleryImages.value = data || []
    } catch {
      galleryImages.value = []
    }
  }
}

function handleGalleryChange(images) {
  galleryImages.value = images
}

async function fetchOrgInfo() {
  const data = await getMyOrg()
  orgInfo.value = data
  if (data.auditStatus === 1) {
    Object.assign(form, {
      orgName: data.orgName || '',
      unitType: data.unitType || '',
      contactPerson: data.contactPerson || '',
      contactPhone: data.contactPhone || '',
      foundDate: data.foundDate || '',
      address: data.address || '',
      intro: data.intro || ''
    })
  }
}

async function fetchUnitType() {
  const data = await getDict('unit_type')
  unitTypeList.value = data
}

function statusText(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await updateOrg(form)
    ElMessage.success('保存成功')
    await fetchOrgInfo()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.org-profile-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-notice {
  margin-bottom: 20px;
}

.org-form {
  max-width: 800px;
}

.gallery-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>