<template>
  <GuestLayout>
    <div class="activity-detail-page">
      <!-- 加载状态 -->
      <div class="loading-container" v-if="loading">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 活动详情 -->
      <div class="detail-container" v-else-if="activity">
        <!-- 活动封面图 -->
        <div class="cover-section">
          <el-image
            :src="activity.coverImageUrl || ''"
            fit="cover"
            class="cover-image"
          >
            <template #error>
              <div class="cover-placeholder">
                <el-icon size="80"><Picture /></el-icon>
                <span>暂无封面图片</span>
              </div>
            </template>
          </el-image>
          <div class="cover-overlay">
            <div class="activity-status">
              <el-tag :type="getStatusType(activity.status)" size="large">
                {{ activity.statusName }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 活动信息 -->
        <div class="info-section">
          <div class="info-header">
            <h1 class="activity-title">{{ activity.title }}</h1>
            <div class="activity-meta">
              <span class="meta-item">
                <el-icon><OfficeBuilding /></el-icon>
                {{ activity.orgName }}
              </span>
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ activity.regionName || '线上活动' }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDateTime(activity.startTime) }} ~ {{ formatDateTime(activity.endTime) }}
              </span>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="info-card">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">项目编号</span>
                <span class="info-value">{{ activity.projectCode }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">服务类别</span>
                <span class="info-value">{{ activity.categoryName }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">服务对象</span>
                <span class="info-value">{{ activity.targetAudienceName || '不限' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">发起组织</span>
                <span class="info-value">{{ activity.orgName }}</span>
              </div>
            </div>
          </div>

          <!-- 活动描述 -->
          <div class="description-section">
            <h3>活动描述</h3>
            <p class="description-text">{{ activity.description || '暂无描述' }}</p>
          </div>

          <!-- 岗位信息 -->
          <div class="positions-section">
            <h3>岗位信息</h3>
            <div class="positions-grid">
              <div v-for="pos in activity.positions" :key="pos.posId" class="position-card">
                <div class="position-header">
                  <span class="position-name">{{ pos.posName }}</span>
                  <el-tag :type="pos.remainCount > 0 ? 'success' : 'danger'" size="small">
                    {{ pos.remainCount > 0 ? `剩余${pos.remainCount}人` : '已满' }}
                  </el-tag>
                </div>
                <div class="position-stats">
                  <div class="stat-item">
                    <span class="stat-label">计划人数</span>
                    <span class="stat-value">{{ pos.planCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">已报名</span>
                    <span class="stat-value">{{ pos.currentCount }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">剩余名额</span>
                    <span class="stat-value remain">{{ pos.remainCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section">
            <template v-if="userStore.isLoggedIn && userStore.isVolunteer">
              <el-button
                type="primary"
                size="large"
                round
                :disabled="activity.status !== 0"
                @click="showRegisterDialog"
              >
                立即报名
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="large" round @click="$router.push('/login')">
                登录后报名
              </el-button>
              <el-button size="large" round @click="$router.push('/register')">
                注册账号
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div class="error-container" v-else>
        <el-result icon="error" title="活动不存在" sub-title="请检查活动ID是否正确">
          <template #extra>
            <el-button type="primary" @click="$router.push('/activity/list')">返回活动列表</el-button>
          </template>
        </el-result>
      </div>

      <!-- 报名弹窗 -->
      <el-dialog v-model="registerDialogVisible" title="报名活动" width="400px">
        <el-form label-width="80px">
          <el-form-item label="选择岗位">
            <el-select v-model="selectedPosId" placeholder="请选择岗位" style="width: 100%">
              <el-option
                v-for="pos in availablePositions"
                :key="pos.posId"
                :label="`${pos.posName} (剩余${pos.remainCount}人)`"
                :value="pos.posId"
                :disabled="pos.remainCount <= 0"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="registering" @click="handleRegister">确认报名</el-button>
        </template>
      </el-dialog>
    </div>
  </GuestLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import GuestLayout from '@/layouts/GuestLayout.vue'
import { getActivityDetail } from '@/api/activity'
import { registerActivity } from '@/api/volunteer'
import { Picture, OfficeBuilding, Location, Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const activity = ref(null)
const registerDialogVisible = ref(false)
const selectedPosId = ref(null)
const registering = ref(false)

const availablePositions = computed(() => {
  return activity.value?.positions?.filter(p => p.remainCount > 0) || []
})

onMounted(async () => {
  loadActivity()
})

async function loadActivity() {
  loading.value = true
  try {
    const id = route.params.id
    const data = await getActivityDetail(id)
    activity.value = data
  } catch (e) {
    console.error('加载活动详情失败:', e)
    activity.value = null
  } finally {
    loading.value = false
  }
}

function showRegisterDialog() {
  if (availablePositions.value.length === 0) {
    ElMessage.warning('暂无可报名的岗位')
    return
  }
  selectedPosId.value = availablePositions.value[0]?.posId
  registerDialogVisible.value = true
}

async function handleRegister() {
  if (!selectedPosId.value) {
    ElMessage.warning('请选择岗位')
    return
  }

  registering.value = true
  try {
    await registerActivity({
      activityId: activity.value.activityId,
      posId: selectedPosId.value
    })
    ElMessage.success('报名成功')
    registerDialogVisible.value = false
    // 刷新活动数据
    loadActivity()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '报名失败')
  } finally {
    registering.value = false
  }
}

function formatDateTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return types[status] || 'info'
}
</script>

<style scoped>
.activity-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.loading-container,
.error-container {
  padding: 60px 24px;
}

/* 封面区域 */
.cover-section {
  position: relative;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.cover-placeholder span {
  margin-top: 12px;
  font-size: 14px;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.5));
}

.activity-status {
  position: absolute;
  top: 16px;
  right: 16px;
}

/* 信息区域 */
.info-section {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
}

.info-header {
  margin-bottom: 24px;
}

.activity-title {
  font-size: 28px;
  color: #1a1a2e;
  margin: 0 0 16px 0;
}

.activity-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #606266;
}

/* 基本信息 */
.info-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  flex: 1;
}

.info-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.info-value {
  font-size: 15px;
  color: #303133;
}

/* 活动描述 */
.description-section {
  margin-bottom: 24px;
}

.description-section h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px 0;
}

.description-text {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 岗位信息 */
.positions-section {
  margin-bottom: 24px;
}

.positions-section h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 16px 0;
}

.positions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.position-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
}

.position-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.position-name {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.position-stats {
  display: flex;
  justify-content: space-between;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.stat-value.remain {
  color: #67c23a;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .cover-section {
    height: 250px;
  }
  .positions-grid {
    grid-template-columns: 1fr;
  }
  .info-row {
    flex-direction: column;
    gap: 16px;
  }
  .activity-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>