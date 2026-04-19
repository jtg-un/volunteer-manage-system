<template>
  <div class="welcome-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">志愿服务管理系统</h1>
        <p class="hero-subtitle">汇聚爱心力量，共建美好社会</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/activity/list')">
            <el-icon><Search /></el-icon>
            浏览活动
          </el-button>
          <el-button size="large" @click="$router.push('/login')" v-if="!userStore.isLoggedIn">
            <el-icon><User /></el-icon>
            立即参与
          </el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div class="hero-stat-item">
          <div class="hero-stat-num">{{ animatedStats.volunteerCount }}</div>
          <div class="hero-stat-label">注册志愿者</div>
        </div>
        <div class="hero-stat-item">
          <div class="hero-stat-num">{{ animatedStats.orgCount }}</div>
          <div class="hero-stat-label">志愿组织</div>
        </div>
        <div class="hero-stat-item">
          <div class="hero-stat-num">{{ animatedStats.activityCount }}</div>
          <div class="hero-stat-label">志愿活动</div>
        </div>
        <div class="hero-stat-item">
          <div class="hero-stat-num">{{ animatedStats.totalHours }}</div>
          <div class="hero-stat-label">服务时长(时)</div>
        </div>
      </div>
    </div>

    <!-- 快捷导航 -->
    <div class="navigation-section">
      <div class="section-header">
        <h2>快捷导航</h2>
        <p>快速找到您需要的功能</p>
      </div>
      <div class="nav-grid">
        <div class="nav-card" @click="$router.push('/activity/list')">
          <div class="nav-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="32"><Search /></el-icon>
          </div>
          <div class="nav-content">
            <h3>活动列表</h3>
            <p>浏览和报名志愿活动</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/volunteer/my-registrations')" v-if="userStore.isVolunteer">
          <div class="nav-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="32"><Tickets /></el-icon>
          </div>
          <div class="nav-content">
            <h3>我的报名</h3>
            <p>查看报名状态和签到</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/volunteer/records')" v-if="userStore.isVolunteer">
          <div class="nav-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="32"><Timer /></el-icon>
          </div>
          <div class="nav-content">
            <h3>时长记录</h3>
            <p>查看志愿服务时长</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/org/publish-activity')" v-if="userStore.isOrg">
          <div class="nav-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon size="32"><Plus /></el-icon>
          </div>
          <div class="nav-content">
            <h3>发布活动</h3>
            <p>创建新的志愿活动</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/org/my-activity')" v-if="userStore.isOrg">
          <div class="nav-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon size="32"><Document /></el-icon>
          </div>
          <div class="nav-content">
            <h3>我的活动</h3>
            <p>管理发布的活动</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/org/profile')" v-if="userStore.isOrg">
          <div class="nav-icon" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
            <el-icon size="32"><OfficeBuilding /></el-icon>
          </div>
          <div class="nav-content">
            <h3>组织信息</h3>
            <p>维护组织基本信息</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/admin/org-audit')" v-if="userStore.isAdmin">
          <div class="nav-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="32"><Checked /></el-icon>
          </div>
          <div class="nav-content">
            <h3>组织审核</h3>
            <p>审核入驻组织申请</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/admin/activity-audit')" v-if="userStore.isAdmin">
          <div class="nav-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="32"><DocumentChecked /></el-icon>
          </div>
          <div class="nav-content">
            <h3>活动审核</h3>
            <p>审核发布的活动</p>
          </div>
        </div>

        <div class="nav-card" @click="$router.push('/user/profile')">
          <div class="nav-icon" style="background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="nav-content">
            <h3>个人中心</h3>
            <p>管理个人信息</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="section stats-section">
      <div class="section-header">
        <h2>平台数据</h2>
        <p>志愿服务实时统计</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon volunteer-icon">
            <el-icon size="40"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ homeStats.volunteerCount || 0 }}</span>
            <span class="stat-label">注册志愿者</span>
          </div>
          <div class="stat-trend">
            <el-icon color="#67c23a"><CaretTop /></el-icon>
            <span>持续增长</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon org-icon">
            <el-icon size="40"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ homeStats.orgCount || 0 }}</span>
            <span class="stat-label">志愿组织</span>
          </div>
          <div class="stat-trend">
            <el-icon color="#67c23a"><CaretTop /></el-icon>
            <span>持续增长</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon activity-icon">
            <el-icon size="40"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ homeStats.activityCount || 0 }}</span>
            <span class="stat-label">发布活动</span>
          </div>
          <div class="stat-badge">{{ homeStats.runningActivityCount || 0 }} 个进行中</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon hours-icon">
            <el-icon size="40"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ homeStats.totalHours || 0 }}</span>
            <span class="stat-label">累计时长(小时)</span>
          </div>
          <div class="stat-badge">{{ homeStats.totalParticipations || 0 }} 人次参与</div>
        </div>
      </div>
    </div>

    <!-- 活动风采 -->
    <div class="section gallery-section">
      <div class="section-header">
        <h2>活动风采</h2>
        <p>精彩瞬间回顾</p>
        <el-button type="primary" link @click="$router.push('/activity/list')">查看更多活动</el-button>
      </div>
      <div class="gallery-grid" v-loading="galleryLoading">
        <div
          v-for="(img, index) in galleryImages"
          :key="img.fileId"
          class="gallery-item"
          :class="{ 'large': index === 0 }"
        >
          <el-image
            :src="getImageUrl(img.fileUrl)"
            fit="cover"
            :preview-src-list="galleryImages.map(i => getImageUrl(i.fileUrl))"
            :initial-index="index"
          />
          <div class="gallery-overlay">
            <el-icon size="24"><View /></el-icon>
          </div>
        </div>
        <div v-if="galleryImages.length === 0 && !galleryLoading" class="gallery-empty">
          <el-empty description="暂无活动风采" />
        </div>
      </div>
    </div>

    <!-- 最新活动 -->
    <div class="section activities-section">
      <div class="section-header">
        <h2>最新活动</h2>
        <p>火热报名中</p>
        <el-button type="primary" link @click="$router.push('/activity/list')">查看全部</el-button>
      </div>
      <div class="activities-list" v-loading="activitiesLoading">
        <div
          v-for="activity in latestActivities"
          :key="activity.activityId"
          class="activity-card"
          @click="showActivityDetail(activity)"
        >
          <div class="activity-cover">
            <el-image
              :src="getActivityCover(activity)"
              fit="cover"
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon size="40"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="activity-info">
            <h3 class="activity-title">{{ activity.title }}</h3>
            <div class="activity-meta">
              <span><el-icon><OfficeBuilding /></el-icon>{{ activity.orgName }}</span>
            </div>
            <div class="activity-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(activity.startTime) }} ~ {{ formatDateTime(activity.endTime) }}
            </div>
            <div class="activity-footer">
              <el-tag :type="getStatusType(activity.status)" size="small">
                {{ getStatusName(activity.status) }}
              </el-tag>
              <span class="activity-count">{{ activity.totalCurrentCount || 0 }}人已报名</span>
            </div>
          </div>
        </div>
        <el-empty v-if="latestActivities.length === 0 && !activitiesLoading" description="暂无活动" />
      </div>
    </div>

    <!-- 活动详情弹窗 -->
    <ActivityDetailDialog
      v-model="activityDetailVisible"
      :data="activityDetailData"
      :show-register="userStore.isVolunteer"
      @register="handleRegister"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getHomeStats, getHomeGallery } from '@/api/home'
import { getActivityList } from '@/api/activity'
import ActivityDetailDialog from '@/components/activity/ActivityDetailDialog.vue'
import {
  Search, Tickets, Timer, User, Plus, Document, OfficeBuilding,
  Checked, DocumentChecked, UserFilled, Calendar, Clock,
  View, Picture, CaretTop
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 统计数据
const homeStats = ref({})
const animatedStats = ref({
  volunteerCount: 0,
  orgCount: 0,
  activityCount: 0,
  totalHours: 0
})

// 活动风采
const galleryImages = ref([])
const galleryLoading = ref(false)

// 最新活动
const latestActivities = ref([])
const activitiesLoading = ref(false)

// 活动详情弹窗
const activityDetailVisible = ref(false)
const activityDetailData = ref({})

onMounted(async () => {
  loadHomeStats()
  loadGallery()
  loadLatestActivities()
})

async function loadHomeStats() {
  try {
    const data = await getHomeStats()
    homeStats.value = data || {}
    animateNumbers()
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

function animateNumbers() {
  const duration = 1500
  const targets = {
    volunteerCount: homeStats.value.volunteerCount || 0,
    orgCount: homeStats.value.orgCount || 0,
    activityCount: homeStats.value.activityCount || 0,
    totalHours: homeStats.value.totalHours || 0
  }

  const startTime = Date.now()
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)

    animatedStats.value = {
      volunteerCount: Math.floor(targets.volunteerCount * progress),
      orgCount: Math.floor(targets.orgCount * progress),
      activityCount: Math.floor(targets.activityCount * progress),
      totalHours: Math.floor(targets.totalHours * progress)
    }

    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  animate()
}

async function loadGallery() {
  galleryLoading.value = true
  try {
    const data = await getHomeGallery()
    galleryImages.value = data || []
  } catch (e) {
    console.error('加载活动风采失败:', e)
  } finally {
    galleryLoading.value = false
  }
}

async function loadLatestActivities() {
  activitiesLoading.value = true
  try {
    const data = await getActivityList({ page: 1, size: 6, status: 1 })
    latestActivities.value = data.records || []
  } catch (e) {
    console.error('加载最新活动失败:', e)
  } finally {
    activitiesLoading.value = false
  }
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `/api${url}`
}

function getActivityCover(activity) {
  // 查找该活动的封面图片
  const cover = galleryImages.value.find(img => img.bizId === activity.activityId)
  return cover ? getImageUrl(cover.fileUrl) : ''
}

function showActivityDetail(activity) {
  activityDetailData.value = activity
  activityDetailVisible.value = true
}

function handleRegister(data) {
  console.log('报名:', data)
}

function formatDateTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return types[status] || 'info'
}

function getStatusName(status) {
  const names = { 0: '待启动', 1: '运行中', 2: '已结项', 3: '待审核', 4: '已拒绝' }
  return names[status] || '未知'
}
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* Hero Banner */
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.hero-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-content {
  max-width: 600px;
  margin-bottom: 40px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.9;
  margin: 0 0 32px 0;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.hero-stats {
  display: flex;
  gap: 40px;
  margin-top: 20px;
}

.hero-stat-item {
  text-align: center;
}

.hero-stat-num {
  font-size: 36px;
  font-weight: 700;
}

.hero-stat-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

/* Navigation */
.navigation-section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin: -20px 20px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  max-width: 1400px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.section-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.nav-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  background: #fff;
}

.nav-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.nav-content h3 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 4px 0;
}

.nav-content p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* Section */
.section {
  max-width: 1400px;
  margin: 40px auto 0;
  padding: 0 20px;
}

.stats-section {
  margin-top: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}

.volunteer-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.org-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.activity-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.hours-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  font-size: 12px;
  color: #67c23a;
}

.stat-badge {
  margin-top: 12px;
  padding: 4px 12px;
  background: #f0f9ff;
  color: #409eff;
  border-radius: 12px;
  font-size: 12px;
}

/* Gallery */
.gallery-section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(2, 180px);
  gap: 12px;
}

.gallery-item {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.gallery-item.large {
  grid-column: span 2;
  grid-row: span 2;
}

.gallery-item .el-image {
  width: 100%;
  height: 100%;
}

.gallery-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.gallery-item:hover .gallery-overlay {
  opacity: 1;
}

.gallery-empty {
  grid-column: span 4;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

/* Activities */
.activities-section {
  padding-bottom: 40px;
}

.activities-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.activity-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.activity-cover {
  height: 160px;
  background: #f5f7fa;
}

.activity-cover .el-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
}

.activity-info {
  padding: 16px;
}

.activity-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-count {
  font-size: 13px;
  color: #67c23a;
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .gallery-grid {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(2, 150px);
  }
  .gallery-item.large {
    grid-column: span 1;
    grid-row: span 1;
  }
}

@media (max-width: 992px) {
  .activities-list {
    grid-template-columns: repeat(2, 1fr);
  }
  .hero-title {
    font-size: 36px;
  }
  .hero-stats {
    flex-wrap: wrap;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .activities-list {
    grid-template-columns: 1fr;
  }
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .hero-actions {
    flex-direction: column;
  }
}
</style>