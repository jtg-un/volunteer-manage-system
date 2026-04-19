<template>
  <div class="welcome-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">志愿服务管理系统</h1>
        <p class="hero-subtitle">汇聚爱心力量，共建美好社会</p>
        <p class="hero-desc">
          连接志愿者与公益组织，让每一份爱心都能找到归宿
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/login')">
            立即登录
          </el-button>
          <el-button size="large" round @click="$router.push('/register')">
            注册账号
          </el-button>
          <el-button size="large" round plain @click="$router.push('/activity/list')">
            浏览活动
          </el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-item">
          <div class="stat-num">{{ animatedStats.volunteerCount }}</div>
          <div class="stat-label">注册志愿者</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ animatedStats.orgCount }}</div>
          <div class="stat-label">志愿组织</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ animatedStats.activityCount }}</div>
          <div class="stat-label">发布活动</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ animatedStats.totalHours }}</div>
          <div class="stat-label">服务时长(小时)</div>
        </div>
      </div>
    </div>

    <!-- 公告栏 -->
    <div class="notice-bar-section">
      <div class="notice-bar-container">
        <div class="notice-bar-header">
          <el-icon class="notice-icon"><Bell /></el-icon>
          <span class="notice-bar-title">系统公告</span>
          <el-button type="primary" link size="small" @click="$router.push('/notice/list')">
            更多公告
          </el-button>
        </div>
        <div class="notice-list" v-loading="noticeLoading">
          <template v-if="notices.length > 0">
            <div
              v-for="notice in notices"
              :key="notice.noticeId"
              class="notice-row"
              @click="goToNotice(notice.noticeId)"
            >
              <el-tag :type="notice.type === 1 ? 'warning' : 'info'" size="small">
                {{ notice.typeName }}
              </el-tag>
              <span class="notice-title-text">{{ notice.title }}</span>
              <span class="notice-time">{{ formatNoticeDate(notice.createTime) }}</span>
            </div>
          </template>
          <div v-else class="notice-empty">暂无公告</div>
        </div>
      </div>
    </div>

    <!-- 功能介绍 -->
    <div class="section features-section">
      <div class="section-title">
        <h2>平台功能</h2>
        <p>为您提供全方位的志愿服务支持</p>
      </div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><Search /></el-icon>
          </div>
          <h3>活动发现</h3>
          <p>浏览各类志愿活动，找到适合您的公益项目</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><Timer /></el-icon>
          </div>
          <h3>时长记录</h3>
          <p>自动记录志愿服务时长，积累爱心积分</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><OfficeBuilding /></el-icon>
          </div>
          <h3>组织入驻</h3>
          <p>公益组织可入驻平台，发布和管理志愿活动</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><DocumentChecked /></el-icon>
          </div>
          <h3>活动管理</h3>
          <p>完整的活动生命周期管理，从发布到结项</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><Tickets /></el-icon>
          </div>
          <h3>报名签到</h3>
          <p>便捷的报名流程，实时签到签退记录</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon size="48"><Star /></el-icon>
          </div>
          <h3>评价反馈</h3>
          <p>活动结束后互评，促进志愿服务质量提升</p>
        </div>
      </div>
    </div>

    <!-- 活动风采 -->
    <div class="section gallery-section">
      <div class="section-title">
        <h2>活动风采</h2>
        <p>精彩志愿活动瞬间</p>
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
        </div>
        <div v-if="galleryImages.length === 0 && !galleryLoading" class="gallery-empty">
          <el-empty description="暂无活动风采" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 最新活动 -->
    <div class="section activities-section">
      <div class="section-title">
        <h2>热门活动</h2>
        <p>正在火热报名中</p>
        <el-button type="primary" round @click="$router.push('/activity/list')">
          查看全部活动
        </el-button>
      </div>
      <div class="activities-grid" v-loading="activitiesLoading">
        <div
          v-for="activity in latestActivities"
          :key="activity.activityId"
          class="activity-card"
          @click="goToDetail(activity.activityId)"
        >
          <div class="activity-cover">
            <el-image
              :src="activity.coverImageUrl || ''"
              fit="cover"
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon size="40"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="cover-badge">
              <el-tag :type="getStatusType(activity.status)" size="small">
                {{ getStatusName(activity.status) }}
              </el-tag>
            </div>
          </div>
          <div class="activity-info">
            <h3 class="activity-title">{{ activity.title }}</h3>
            <div class="activity-org">
              <el-icon><OfficeBuilding /></el-icon>
              {{ activity.orgName }}
            </div>
            <div class="activity-time">
              <el-icon><Clock /></el-icon>
              {{ formatDate(activity.startTime) }} ~ {{ formatDate(activity.endTime) }}
            </div>
            <div class="activity-footer">
              <span class="activity-region">
                <el-icon><Location /></el-icon>
                {{ activity.regionName || '线上' }}
              </span>
              <span class="activity-count">
                <el-icon><User /></el-icon>
                {{ activity.totalCurrentCount || 0 }}人报名
              </span>
            </div>
          </div>
        </div>
        <div v-if="latestActivities.length === 0 && !activitiesLoading" class="activities-empty">
          <el-empty description="暂无进行中的活动" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 参与流程 -->
    <div class="section process-section">
      <div class="section-title">
        <h2>参与流程</h2>
        <p>简单几步，开启您的志愿服务之旅</p>
      </div>
      <div class="process-steps">
        <div class="process-step">
          <div class="step-num">1</div>
          <div class="step-content">
            <h4>注册账号</h4>
            <p>选择志愿者或组织身份，完成注册</p>
          </div>
        </div>
        <div class="process-line"></div>
        <div class="process-step">
          <div class="step-num">2</div>
          <div class="step-content">
            <h4>浏览活动</h4>
            <p>查找感兴趣的志愿活动</p>
          </div>
        </div>
        <div class="process-line"></div>
        <div class="process-step">
          <div class="step-num">3</div>
          <div class="step-content">
            <h4>报名参与</h4>
            <p>选择岗位，提交报名申请</p>
          </div>
        </div>
        <div class="process-line"></div>
        <div class="process-step">
          <div class="step-num">4</div>
          <div class="step-content">
            <h4>签到服务</h4>
            <p>活动当天签到签退，记录时长</p>
          </div>
        </div>
        <div class="process-line"></div>
        <div class="process-step">
          <div class="step-num">5</div>
          <div class="step-content">
            <h4>获得认证</h4>
            <p>累计志愿时长，获得积分与评价</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部行动 -->
    <div class="cta-section">
      <div class="cta-content">
        <h2>准备好开始您的志愿服务之旅了吗？</h2>
        <p>加入我们，让爱心传递，让社会更美好</p>
        <div class="cta-buttons">
          <el-button type="primary" size="large" round @click="$router.push('/register')">
            立即注册
          </el-button>
          <el-button size="large" round plain @click="$router.push('/login')">
            已有账号，登录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <p>志愿服务管理系统 © 2026</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeStats, getHomeGallery } from '@/api/home'
import { getActivityList } from '@/api/activity'
import { getLatestNotices } from '@/api/system'
import {
  Search, Timer, OfficeBuilding, DocumentChecked, Tickets, Star,
  Clock, Location, User, Picture, Bell
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 统计数据
const animatedStats = ref({
  volunteerCount: 0,
  orgCount: 0,
  activityCount: 0,
  totalHours: 0
})
const homeStats = ref({})

// 活动风采
const galleryImages = ref([])
const galleryLoading = ref(false)

// 最新活动
const latestActivities = ref([])
const activitiesLoading = ref(false)

// 公告
const notices = ref([])
const noticeLoading = ref(false)

onMounted(async () => {
  loadHomeStats()
  loadGallery()
  loadLatestActivities()
  loadNotices()
})

async function loadHomeStats() {
  try {
    const data = await getHomeStats()
    homeStats.value = data || {}
    animateNumbers()
  } catch (e) {
    // 使用默认数据
    homeStats.value = { volunteerCount: 0, orgCount: 0, activityCount: 0, totalHours: 0 }
  }
}

function animateNumbers() {
  const duration = 2000
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
    galleryImages.value = []
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
    latestActivities.value = []
  } finally {
    activitiesLoading.value = false
  }
}

async function loadNotices() {
  noticeLoading.value = true
  try {
    const data = await getLatestNotices(5)
    notices.value = data || []
  } catch (e) {
    notices.value = []
  } finally {
    noticeLoading.value = false
  }
}

function goToNotice(noticeId) {
  router.push(`/notice/detail/${noticeId}`)
}

function formatNoticeDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 图片存储在8083服务，需要完整路径
  return url
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return types[status] || 'info'
}

function getStatusName(status) {
  const names = { 0: '待启动', 1: '进行中', 2: '已结束', 3: '待审核', 4: '已拒绝' }
  return names[status] || '未知'
}

function goToDetail(activityId) {
  router.push(`/activity/detail/${activityId}`)
}
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  background: #f8fafc;
}

/* Hero Banner */
.hero-banner {
  min-height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px 60px;
  color: #fff;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-content {
  text-align: center;
  z-index: 1;
  max-width: 800px;
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: 4px;
}

.hero-subtitle {
  font-size: 24px;
  margin: 0 0 12px 0;
  opacity: 0.95;
}

.hero-desc {
  font-size: 16px;
  margin: 0 0 40px 0;
  opacity: 0.8;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-stats {
  display: flex;
  gap: 60px;
  margin-top: 60px;
  z-index: 1;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 48px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 8px;
}

/* Notice Bar */
.notice-bar-section {
  background: #fff;
  padding: 20px;
}

.notice-bar-container {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.notice-bar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  color: #667eea;
}

.notice-icon {
  font-size: 20px;
  animation: bell-ring 2s ease-in-out infinite;
}

@keyframes bell-ring {
  0%, 100% { transform: rotate(0deg); }
  10% { transform: rotate(15deg); }
  20% { transform: rotate(-15deg); }
  30% { transform: rotate(10deg); }
  40% { transform: rotate(-10deg); }
  50% { transform: rotate(0deg); }
}

.notice-bar-title {
  font-size: 16px;
  font-weight: 600;
}

.notice-list {
  padding: 0;
}

.notice-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.3s;
}

.notice-row:last-child {
  border-bottom: none;
}

.notice-row:hover {
  background: #f5f7fa;
}

.notice-title-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-empty {
  font-size: 14px;
  color: #909399;
  padding: 24px;
  text-align: center;
}

/* Section */
.section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px;
}

.section-title {
  text-align: center;
  margin-bottom: 40px;
}

.section-title h2 {
  font-size: 32px;
  color: #1a1a2e;
  margin: 0 0 12px 0;
}

.section-title p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* Features */
.features-section {
  background: #fff;
  max-width: 100%;
  padding: 60px 20px;
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.feature-card {
  text-align: center;
  padding: 32px 24px;
  border-radius: 16px;
  background: #f8fafc;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
  background: #fff;
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.feature-card h3 {
  font-size: 18px;
  color: #1a1a2e;
  margin: 0 0 12px 0;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* Gallery */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(2, 200px);
  gap: 16px;
}

.gallery-item {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.gallery-item.large {
  grid-column: span 2;
  grid-row: span 2;
}

.gallery-item .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}

.gallery-item:hover .el-image {
  transform: scale(1.05);
}

.gallery-empty {
  grid-column: span 4;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

/* Activities */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.activity-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
}

.activity-cover {
  height: 180px;
  position: relative;
  overflow: hidden;
}

.activity-cover .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}

.activity-card:hover .activity-cover .el-image {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  color: #c0c4cc;
}

.cover-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.activity-info {
  padding: 16px;
}

.activity-title {
  font-size: 16px;
  color: #1a1a2e;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-org {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

.activity-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-count {
  color: #67c23a;
}

.activities-empty {
  grid-column: span 3;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

/* Process */
.process-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  max-width: 100%;
  padding: 80px 20px;
}

.process-steps {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
}

.process-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-num {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.step-content h4 {
  font-size: 16px;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.step-content p {
  font-size: 13px;
  color: #666;
  margin: 0;
  max-width: 140px;
}

.process-line {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  margin: 0 10px;
  margin-top: -30px;
}

/* CTA */
.cta-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px;
  text-align: center;
  color: #fff;
}

.cta-content h2 {
  font-size: 28px;
  margin: 0 0 16px 0;
}

.cta-content p {
  font-size: 16px;
  margin: 0 0 32px 0;
  opacity: 0.9;
}

.cta-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* Footer */
.footer {
  background: #1a1a2e;
  color: #fff;
  padding: 20px;
  text-align: center;
  font-size: 14px;
  opacity: 0.8;
}

/* Responsive */
@media (max-width: 992px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .activities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 160px);
  }
  .gallery-item.large {
    grid-column: span 1;
    grid-row: span 1;
  }
  .hero-title {
    font-size: 40px;
  }
  .hero-stats {
    gap: 30px;
  }
  .stat-num {
    font-size: 32px;
  }
  .process-steps {
    flex-wrap: wrap;
    gap: 24px;
  }
  .process-line {
    display: none;
  }
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  .activities-grid {
    grid-template-columns: 1fr;
  }
  .gallery-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  .hero-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
}
</style>