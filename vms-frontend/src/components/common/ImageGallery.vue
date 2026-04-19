<template>
  <div class="image-gallery">
    <!-- 图片轮播 -->
    <el-carousel
      v-if="images.length > 1"
      :interval="4000"
      type="card"
      height="300px"
      indicator-position="outside"
    >
      <el-carousel-item v-for="image in images" :key="image.fileId">
        <el-image
          :src="image.fileUrl"
          fit="cover"
          class="carousel-image"
          :preview-src-list="images.map(i => i.fileUrl)"
        />
      </el-carousel-item>
    </el-carousel>

    <!-- 单张图片 -->
    <el-image
      v-else-if="images.length === 1"
      :src="images[0].fileUrl"
      fit="cover"
      class="single-image"
      :preview-src-list="[images[0].fileUrl]"
    />

    <!-- 无图片 -->
    <div v-else class="no-image">
      <el-icon :size="48"><Picture /></el-icon>
      <span>暂无图片</span>
    </div>
  </div>
</template>

<script setup>
import { Picture } from '@element-plus/icons-vue'

defineProps({
  images: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.image-gallery {
  width: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.single-image {
  width: 100%;
  height: 300px;
  border-radius: 8px;
}

.no-image {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.no-image span {
  margin-top: 12px;
  font-size: 14px;
}
</style>