<template>
  <!-- 悬浮按钮 -->
  <div class="ai-float-btn" @click="handleClick" :title="isLoggedIn ? 'AI智能推荐' : '请登录后使用'">
    <el-icon size="28"><ChatDotRound /></el-icon>
  </div>

  <!-- AI对话窗口 -->
  <el-dialog
    v-model="dialogVisible"
    title="AI智能推荐助手"
    width="450px"
    append-to-body
    :close-on-click-modal="false"
    class="ai-dialog"
  >
    <!-- 对话历史 -->
    <div class="chat-container">
      <div class="chat-history">
        <div v-for="msg in messages" :key="msg.id" :class="['message', msg.type]">
          <div class="message-content">
            <span v-html="formatMessage(msg.content)"></span>
          </div>
        </div>
        <div v-if="loading" class="message ai">
          <div class="message-content loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在思考中...
          </div>
        </div>
      </div>

      <!-- 推荐活动卡片 -->
      <div class="recommend-cards" v-if="recommendations.length > 0">
        <div class="recommend-title">推荐活动</div>
        <div class="cards-grid">
          <div
            v-for="act in recommendations"
            :key="act.activityId"
            class="activity-card"
            @click="goToDetail(act.activityId)"
          >
            <div class="card-cover">
              <el-image :src="act.coverImageUrl || ''" fit="cover">
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon size="24"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="card-info">
              <h4>{{ act.title }}</h4>
              <p class="card-org">{{ act.orgName }}</p>
              <p class="card-meta">
                {{ act.startTime }} ~ {{ act.endTime }}
                <span class="card-count">{{ act.currentCount }}/{{ act.planCount }}人</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="userInput"
        placeholder="输入问题获取推荐，如：推荐社区服务的活动"
        :disabled="loading"
        @keyup.enter="sendRequest"
      />
      <el-button type="primary" :loading="loading" @click="sendRequest">
        发送
      </el-button>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="small" @click="clearHistory">清空对话</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAiRecommend } from '@/api/ai'
import { ChatDotRound, Loading, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isVolunteer = computed(() => userStore.userInfo?.role === 0)

const dialogVisible = ref(false)
const userInput = ref('')
const loading = ref(false)
const messages = ref([])
const recommendations = ref([])
let messageId = 0

onMounted(() => {
  // 初始欢迎消息
  messages.value.push({
    id: messageId++,
    type: 'ai',
    content: '您好！我是AI推荐助手，可以根据您的偏好和参与历史，为您推荐合适的志愿活动。请问您有什么需求？'
  })
})

function handleClick() {
  // 允许打开对话框，后续发送时检查登录
  dialogVisible.value = true
}

async function sendRequest() {
  if (!userInput.value.trim()) {
    return
  }

  // 发送时检查登录状态
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后使用AI助手')
    router.push('/login')
    return
  }
  if (!isVolunteer.value) {
    ElMessage.warning('AI助手仅对志愿者开放')
    return
  }

  // 添加用户消息
  messages.value.push({
    id: messageId++,
    type: 'user',
    content: userInput.value
  })

  const question = userInput.value
  userInput.value = ''
  loading.value = true
  recommendations.value = []

  try {
    const data = await getAiRecommend(question)

    // 添加AI回复
    messages.value.push({
      id: messageId++,
      type: 'ai',
      content: data.answer || '抱歉，暂时无法获取推荐'
    })

    // 设置推荐活动
    recommendations.value = data.activities || []
  } catch (e) {
    messages.value.push({
      id: messageId++,
      type: 'ai',
      content: '抱歉，服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
  }
}

function formatMessage(content) {
  // 直接返回内容，不再处理活动ID格式
  return content
}

function clearHistory() {
  messages.value = []
  recommendations.value = []
  messageId = 0
  messages.value.push({
    id: messageId++,
    type: 'ai',
    content: '对话已清空。请问有什么可以帮您的？'
  })
}

function goToDetail(activityId) {
  dialogVisible.value = false
  router.push(`/activity/detail/${activityId}`)
}
</script>

<style scoped>
/* 悬浮按钮 */
.ai-float-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.5);
  z-index: 999;
  transition: all 0.3s;
}

.ai-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

/* 对话容器 */
.chat-container {
  max-height: 400px;
  overflow-y: auto;
}

.chat-history {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.message {
  margin-bottom: 12px;
}

.message.user {
  text-align: right;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px 12px 4px 12px;
  padding: 10px 14px;
  display: inline-block;
  max-width: 80%;
}

.message.ai .message-content {
  background: #fff;
  color: #303133;
  border-radius: 12px 12px 12px 4px;
  padding: 10px 14px;
  display: inline-block;
  max-width: 80%;
  border: 1px solid #e4e7ed;
}

.message-content.loading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-content .highlight {
  color: #667eea;
  font-weight: 600;
}

/* 推荐卡片 */
.recommend-cards {
  margin-top: 16px;
}

.recommend-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.activity-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.card-cover {
  height: 80px;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.card-info {
  padding: 10px;
}

.card-info h4 {
  font-size: 14px;
  color: #303133;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-org {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #606266;
  margin: 0;
}

.card-count {
  color: #67c23a;
  margin-left: 8px;
}

/* 输入框 */
.chat-input {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.chat-input .el-input {
  flex: 1;
}

/* 底部 */
.dialog-footer {
  text-align: center;
}

/* 响应式 */
@media (max-width: 500px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>