<template>
  <GuestLayout>
    <div class="notice-list-page">
      <div class="page-header">
        <h1>系统公告</h1>
        <p>了解平台最新动态和通知</p>
      </div>

      <div class="notice-list" v-loading="loading">
        <div
          v-for="notice in notices"
          :key="notice.noticeId"
          class="notice-item"
          @click="goToDetail(notice.noticeId)"
        >
          <div class="notice-left">
            <el-tag :type="notice.type === 1 ? 'warning' : 'info'" size="small">
              {{ notice.typeName }}
            </el-tag>
          </div>
          <div class="notice-content">
            <h3 class="notice-title">{{ notice.title }}</h3>
            <p class="notice-summary">{{ notice.content?.substring(0, 100) }}...</p>
          </div>
          <div class="notice-right">
            <span class="notice-date">{{ formatDate(notice.createTime) }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <el-empty v-if="notices.length === 0 && !loading" description="暂无公告" :image-size="100" />

        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="pagination"
          @size-change="loadNotices"
          @current-change="loadNotices"
        />
      </div>
    </div>
  </GuestLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GuestLayout from '@/layouts/GuestLayout.vue'
import { getNoticePage } from '@/api/system'
import { ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const notices = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadNotices()
})

async function loadNotices() {
  loading.value = true
  try {
    const data = await getNoticePage({ page: pageNum.value, size: pageSize.value })
    notices.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    notices.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goToDetail(noticeId) {
  router.push(`/notice/detail/${noticeId}`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.notice-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.notice-list {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  min-height: 400px;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background: #f5f7fa;
}

.notice-left {
  width: 60px;
}

.notice-content {
  flex: 1;
  padding: 0 16px;
}

.notice-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.notice-summary {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}

.notice-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.notice-date {
  font-size: 13px;
}

.pagination {
  margin-top: 24px;
  justify-content: center;
}
</style>