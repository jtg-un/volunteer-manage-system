<template>
  <GuestLayout>
    <div class="notice-detail-page" v-loading="loading">
      <div class="notice-content" v-if="notice">
        <div class="notice-header">
          <el-tag :type="notice.type === 1 ? 'warning' : 'info'" size="large">
            {{ notice.typeName }}
          </el-tag>
          <h1 class="notice-title">{{ notice.title }}</h1>
          <div class="notice-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              发布人：{{ notice.createBy || '管理员' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              发布时间：{{ formatDate(notice.createTime) }}
            </span>
          </div>
        </div>

        <div class="notice-body">
          <p class="notice-text">{{ notice.content }}</p>
        </div>

        <div class="notice-footer">
          <el-button @click="$router.push('/notice/list')">返回公告列表</el-button>
        </div>
      </div>

      <el-empty v-else-if="!loading" description="公告不存在" :image-size="100">
        <el-button type="primary" @click="$router.push('/notice/list')">返回公告列表</el-button>
      </el-empty>
    </div>
  </GuestLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GuestLayout from '@/layouts/GuestLayout.vue'
import { getNoticeDetail } from '@/api/system'
import { User, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const notice = ref(null)

onMounted(() => {
  loadNotice()
})

async function loadNotice() {
  loading.value = true
  try {
    const id = route.params.id
    const data = await getNoticeDetail(id)
    notice.value = data
  } catch (e) {
    notice.value = null
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.notice-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: 500px;
}

.notice-content {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
}

.notice-header {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.notice-title {
  font-size: 24px;
  color: #1a1a2e;
  margin: 16px 0 12px 0;
  font-weight: 600;
}

.notice-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notice-body {
  margin-bottom: 32px;
}

.notice-text {
  font-size: 16px;
  color: #606266;
  line-height: 2;
  white-space: pre-wrap;
  margin: 0;
}

.notice-footer {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>