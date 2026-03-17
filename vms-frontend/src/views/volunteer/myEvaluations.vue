<template>
  <div class="my-evaluations">
    <el-card>
      <template #header>
        <span>我的评价记录</span>
      </template>

      <el-empty v-if="!loading && evaluations.length === 0" description="暂无评价记录" />

      <el-table v-else :data="evaluations" v-loading="loading">
        <el-table-column prop="activityTitle" label="活动名称" min-width="200" />
        <el-table-column prop="orgName" label="评价组织" width="150" />
        <el-table-column label="综合评分" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="large">{{ row.avgScore }} 分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="培训评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate :model-value="row.scoreTraining" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
          </template>
        </el-table-column>
        <el-table-column label="协作评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate :model-value="row.scoreCooperation" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
          </template>
        </el-table-column>
        <el-table-column label="执行力评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate :model-value="row.scoreExecution" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="200">
          <template #default="{ row }">
            <span>{{ row.comment || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyEvaluations } from '@/api/evaluation'

const loading = ref(false)
const evaluations = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyEvaluations({
      page: pagination.page,
      size: pagination.size
    })
    evaluations.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载评价列表失败:', error)
  } finally {
    loading.value = false
  }
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.my-evaluations {
  padding: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>