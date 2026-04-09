<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-left">
        <el-avatar :size="64" :src="userStore.userInfo?.avatarUrl" icon="UserFilled" />
        <div class="welcome-text">
          <h2>{{ greeting }}，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h2>
          <p>{{ todayStr }} | {{ roleText }}</p>
        </div>
      </div>
      <div class="welcome-right">
        <span class="weather-text">{{ weatherText }}</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <div class="section-title">快捷入口</div>
      <div class="action-buttons">
        <!-- 志愿者快捷入口 -->
        <template v-if="userStore.isVolunteer">
          <div class="action-item" @click="$router.push('/activity/list')">
            <el-icon size="28"><Search /></el-icon>
            <span>浏览活动</span>
          </div>
          <div class="action-item" @click="$router.push('/volunteer/my-registrations')">
            <el-icon size="28"><Tickets /></el-icon>
            <span>我的报名</span>
          </div>
          <div class="action-item" @click="$router.push('/volunteer/records')">
            <el-icon size="28"><Timer /></el-icon>
            <span>时长记录</span>
          </div>
          <div class="action-item" @click="$router.push('/user/profile')">
            <el-icon size="28"><User /></el-icon>
            <span>个人中心</span>
          </div>
        </template>

        <!-- 组织快捷入口 -->
        <template v-else-if="userStore.isOrg">
          <div class="action-item" @click="$router.push('/org/publish-activity')">
            <el-icon size="28"><Plus /></el-icon>
            <span>发布活动</span>
          </div>
          <div class="action-item" @click="$router.push('/org/my-activity')">
            <el-icon size="28"><Document /></el-icon>
            <span>我的活动</span>
          </div>
          <div class="action-item" @click="$router.push('/org/registrations')">
            <el-icon size="28"><List /></el-icon>
            <span>报名管理</span>
          </div>
          <div class="action-item" @click="$router.push('/org/profile')">
            <el-icon size="28"><OfficeBuilding /></el-icon>
            <span>组织信息</span>
          </div>
        </template>

        <!-- 管理员快捷入口 -->
        <template v-else-if="userStore.isAdmin">
          <div class="action-item" @click="$router.push('/admin/org-audit')">
            <el-icon size="28"><Checked /></el-icon>
            <span>组织审核</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/activity-audit')">
            <el-icon size="28"><DocumentChecked /></el-icon>
            <span>活动审核</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/user-manage')">
            <el-icon size="28"><UserFilled /></el-icon>
            <span>用户管理</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/notice-manage')">
            <el-icon size="28"><Bell /></el-icon>
            <span>公告管理</span>
          </div>
        </template>
      </div>
    </div>

    <!-- 志愿者首页内容 -->
    <template v-if="userStore.isVolunteer">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="24"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ volunteerStats.totalHours || 0 }}</span>
            <span class="stat-label">累计时长(小时)</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="24"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ volunteerStats.totalPoints || 0 }}</span>
            <span class="stat-label">累计积分</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="24"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ volunteerStats.activityCount || 0 }}</span>
            <span class="stat-label">参与活动</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon size="24"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ volunteerStats.monthHours || 0 }}</span>
            <span class="stat-label">本月时长</span>
          </div>
        </div>
      </div>

      <!-- 推荐活动 + 我的报名 -->
      <div class="content-row">
        <div class="content-card">
          <div class="card-header">
            <span>推荐活动</span>
            <el-button type="primary" link @click="$router.push('/activity/list')">更多</el-button>
          </div>
          <div class="activity-list" v-loading="activityLoading">
            <div
              v-for="item in recommendActivities"
              :key="item.activityId"
              class="activity-item"
              @click="showActivityDetail(item.activityId)"
            >
              <div class="activity-title">{{ item.activityName }}</div>
              <div class="activity-meta">
                <span><el-icon><Location /></el-icon> {{ item.regionName || '线上' }}</span>
                <span><el-icon><Calendar /></el-icon> {{ formatDate(item.startTime) }}</span>
              </div>
            </div>
            <el-empty v-if="recommendActivities.length === 0 && !activityLoading" description="暂无推荐活动" />
          </div>
        </div>

        <div class="content-card">
          <div class="card-header">
            <span>我的报名</span>
            <el-button type="primary" link @click="$router.push('/volunteer/my-registrations')">更多</el-button>
          </div>
          <div class="registration-list" v-loading="registrationLoading">
            <div v-for="item in myRegistrations" :key="item.regId" class="registration-item">
              <div class="reg-info">
                <span class="reg-name">{{ item.activityName }}</span>
                <span class="reg-pos">{{ item.posName }}</span>
              </div>
              <el-tag :type="getStatusType(item.regStatus)" size="small">{{ item.statusName }}</el-tag>
            </div>
            <el-empty v-if="myRegistrations.length === 0 && !registrationLoading" description="暂无报名记录" />
          </div>
        </div>
      </div>
    </template>

    <!-- 组织首页内容 -->
    <template v-else-if="userStore.isOrg">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ orgStats.totalActivities || 0 }}</span>
            <span class="stat-label">发布活动</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="24"><VideoPlay /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ orgStats.runningActivities || 0 }}</span>
            <span class="stat-label">运行中</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="24"><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ orgStats.totalRegistrations || 0 }}</span>
            <span class="stat-label">报名人数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon size="24"><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ orgStats.pendingRegistrations || 0 }}</span>
            <span class="stat-label">待审核报名</span>
          </div>
        </div>
      </div>

      <!-- 我的活动 + 待审核报名 -->
      <div class="content-row">
        <div class="content-card">
          <div class="card-header">
            <span>我的活动</span>
            <el-button type="primary" link @click="$router.push('/org/my-activity')">更多</el-button>
          </div>
          <div class="activity-list" v-loading="orgActivityLoading">
            <div v-for="item in myActivities" :key="item.activityId" class="activity-item">
              <div class="activity-title">{{ item.activityName }}</div>
              <div class="activity-meta">
                <el-tag :type="getActivityStatusType(item.status)" size="small">{{ item.statusName }}</el-tag>
                <span>报名: {{ item.registrationCount || 0 }}人</span>
              </div>
            </div>
            <el-empty v-if="myActivities.length === 0 && !orgActivityLoading" description="暂无活动" />
          </div>
        </div>

        <div class="content-card">
          <div class="card-header">
            <span>待处理事项</span>
          </div>
          <div class="todo-list">
            <div class="todo-item" v-if="orgStats.pendingRegistrations > 0">
              <el-icon color="#e6a23c"><Warning /></el-icon>
              <span>有 {{ orgStats.pendingRegistrations }} 条报名待审核</span>
              <el-button type="primary" size="small" @click="$router.push('/org/registrations')">去处理</el-button>
            </div>
            <el-empty v-if="orgStats.pendingRegistrations === 0" description="暂无待处理事项" />
          </div>
        </div>
      </div>
    </template>

    <!-- 管理员首页内容 -->
    <template v-else-if="userStore.isAdmin">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="24"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ adminStats.userCount || 0 }}</span>
            <span class="stat-label">注册用户</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="24"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ adminStats.orgCount || 0 }}</span>
            <span class="stat-label">入驻组织</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ adminStats.activityCount || 0 }}</span>
            <span class="stat-label">发布活动</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon size="24"><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ adminStats.volunteerCount || 0 }}</span>
            <span class="stat-label">志愿者</span>
          </div>
        </div>
      </div>

      <!-- 待办事项 + 系统公告 -->
      <div class="content-row">
        <div class="content-card">
          <div class="card-header">
            <span>待办事项</span>
          </div>
          <div class="todo-list">
            <div class="todo-item" v-if="adminStats.pendingOrgs > 0">
              <el-icon color="#e6a23c"><Warning /></el-icon>
              <span>有 {{ adminStats.pendingOrgs }} 个组织待审核</span>
              <el-button type="primary" size="small" @click="$router.push('/admin/org-audit')">去处理</el-button>
            </div>
            <div class="todo-item" v-if="adminStats.pendingActivities > 0">
              <el-icon color="#e6a23c"><Warning /></el-icon>
              <span>有 {{ adminStats.pendingActivities }} 个活动待审核</span>
              <el-button type="primary" size="small" @click="$router.push('/admin/activity-audit')">去处理</el-button>
            </div>
            <el-empty v-if="adminStats.pendingOrgs === 0 && adminStats.pendingActivities === 0" description="暂无待办事项" />
          </div>
        </div>

        <div class="content-card">
          <div class="card-header">
            <span>系统公告</span>
            <el-button type="primary" link @click="$router.push('/admin/notice-manage')">管理</el-button>
          </div>
          <div class="notice-list" v-loading="noticeLoading">
            <div v-for="item in notices" :key="item.noticeId" class="notice-item">
              <span class="notice-title">{{ item.title }}</span>
              <span class="notice-date">{{ formatDate(item.createTime) }}</span>
            </div>
            <el-empty v-if="notices.length === 0 && !noticeLoading" description="暂无公告" />
          </div>
        </div>
      </div>
    </template>

    <!-- 活动详情弹窗 -->
    <ActivityDetailDialog v-model="activityDetailVisible" :activity-id="currentActivityId" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getVolunteerStats, getMyRegistrations } from '@/api/volunteer'
import { getActivityList, getMyActivities } from '@/api/activity'
import { getLatestNotices } from '@/api/system'
import ActivityDetailDialog from '@/components/activity/ActivityDetailDialog.vue'
import {
  Search, Tickets, Timer, User, Plus, Document, List, OfficeBuilding,
  Checked, DocumentChecked, UserFilled, Bell, Star, Calendar, Clock,
  Location, VideoPlay, Warning
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 欢迎语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const todayStr = computed(() => {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

const weatherText = ref('天气晴朗，适合参加志愿活动')

const roleText = computed(() => {
  const role = userStore.userInfo?.role
  if (role === 0) return '志愿者'
  if (role === 1) return '组织负责人'
  if (role === 2) return '系统管理员'
  return '未知角色'
})

// 志愿者数据
const volunteerStats = ref({})
const recommendActivities = ref([])
const myRegistrations = ref([])
const activityLoading = ref(false)
const registrationLoading = ref(false)

// 组织数据
const orgStats = ref({})
const myActivities = ref([])
const orgActivityLoading = ref(false)

// 管理员数据
const adminStats = ref({
  userCount: 0,
  orgCount: 0,
  activityCount: 0,
  volunteerCount: 0,
  pendingOrgs: 0,
  pendingActivities: 0
})

// 公告
const notices = ref([])
const noticeLoading = ref(false)

// 活动详情弹窗
const activityDetailVisible = ref(false)
const currentActivityId = ref(null)

onMounted(async () => {
  if (userStore.isVolunteer) {
    loadVolunteerData()
  } else if (userStore.isOrg) {
    loadOrgData()
  } else if (userStore.isAdmin) {
    loadAdminData()
  }
  loadNotices()
})

async function loadVolunteerData() {
  try {
    const stats = await getVolunteerStats()
    volunteerStats.value = stats || {}
  } catch {}

  activityLoading.value = true
  try {
    const data = await getActivityList({ page: 1, size: 5, status: 1 })
    recommendActivities.value = data.records || []
  } catch {} finally {
    activityLoading.value = false
  }

  registrationLoading.value = true
  try {
    const data = await getMyRegistrations({ page: 1, size: 5 })
    myRegistrations.value = data.records || []
  } catch {} finally {
    registrationLoading.value = false
  }
}

async function loadOrgData() {
  orgActivityLoading.value = true
  try {
    const data = await getMyActivities({ page: 1, size: 5 })
    myActivities.value = data.records || []

    // 计算统计数据
    const activities = data.records || []
    orgStats.value = {
      totalActivities: data.total || 0,
      runningActivities: activities.filter(a => a.status === 1).length,
      totalRegistrations: activities.reduce((sum, a) => sum + (a.registrationCount || 0), 0),
      pendingRegistrations: activities.reduce((sum, a) => sum + (a.pendingCount || 0), 0)
    }
  } catch {} finally {
    orgActivityLoading.value = false
  }
}

async function loadAdminData() {
  // TODO: 需要后端提供管理员统计接口
  // 目前使用模拟数据
  adminStats.value = {
    userCount: 128,
    orgCount: 15,
    activityCount: 42,
    volunteerCount: 98,
    pendingOrgs: 3,
    pendingActivities: 5
  }
}

async function loadNotices() {
  noticeLoading.value = true
  try {
    const data = await getLatestNotices(5)
    notices.value = data || []
  } catch {} finally {
    noticeLoading.value = false
  }
}

function showActivityDetail(id) {
  currentActivityId.value = id
  activityDetailVisible.value = true
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getActivityStatusType(status) {
  const map = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return map[status] || 'info'
}
</script>

<style scoped>
.home-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 32px;
  color: #fff;
  margin-bottom: 20px;
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
}

.welcome-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.weather-text {
  font-size: 14px;
  opacity: 0.9;
}

/* 快捷入口 */
.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
}

.action-item:hover {
  background: #e6f0ff;
  transform: translateY(-2px);
}

.action-item span {
  font-size: 14px;
  color: #606266;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 内容区域 */
.content-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.content-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

/* 活动列表 */
.activity-list {
  min-height: 200px;
}

.activity-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.activity-item:hover {
  background: #f9fafc;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.activity-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 报名列表 */
.registration-list {
  min-height: 200px;
}

.registration-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.registration-item:last-child {
  border-bottom: none;
}

.reg-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reg-name {
  font-size: 14px;
  color: #303133;
}

.reg-pos {
  font-size: 12px;
  color: #909399;
}

/* 待办事项 */
.todo-list {
  min-height: 200px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fdf6ec;
  border-radius: 8px;
  margin-bottom: 12px;
}

.todo-item span {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

/* 公告列表 */
.notice-list {
  min-height: 200px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-date {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    flex-wrap: wrap;
  }
  .stat-card {
    flex: 1 1 calc(50% - 8px);
    min-width: 200px;
  }
}

@media (max-width: 992px) {
  .content-row {
    flex-direction: column;
  }
  .action-buttons {
    flex-wrap: wrap;
  }
  .action-item {
    flex: 1 1 calc(25% - 12px);
    min-width: 120px;
  }
}
</style>