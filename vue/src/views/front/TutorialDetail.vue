<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">📖</div>
      <div class="page-hero-title">教程详情</div>
      <div class="page-hero-subtitle">{{ data.tutorial?.title || '加载中...' }}</div>
      <div class="page-hero-stats">
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ data.tutorial?.viewCount || 0 }}</div>
          <div class="page-hero-stat-lbl">浏览次数</div>
        </div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ data.tutorial?.contentType === 'video' ? '视频教程' : '图文教程' }}</div>
          <div class="page-hero-stat-lbl">教程类型</div>
        </div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ data.tutorial?.categoryName || '未分类' }}</div>
          <div class="page-hero-stat-lbl">所属分类</div>
        </div>
      </div>
    </div>

    <div class="breadcrumb-nav">
      <a @click="goBack"><el-icon><Back /></el-icon> 例题讲解</a>
      <span>/</span>
      <span class="current">{{ data.tutorial?.title }}</span>
    </div>

    <div v-loading="data.loading" class="detail-grid">
      <div class="detail-main">
        <div class="card detail-header-card">
          <div class="detail-header-row">
            <div class="type-icon" :class="data.tutorial?.contentType === 'video' ? 'video' : 'text'">
              {{ data.tutorial?.contentType === 'video' ? '🎬' : '📝' }}
            </div>
            <div class="detail-header-info">
              <h1 class="detail-title">{{ data.tutorial?.title }}</h1>
              <p class="detail-desc">{{ data.tutorial?.description }}</p>
              <div class="detail-tags">
                <el-tag v-if="data.tutorial?.contentType === 'video'" type="success" size="small">视频教程</el-tag>
                <el-tag v-else type="primary" size="small">图文教程</el-tag>
                <span v-if="data.tutorial?.categoryName" class="detail-tag">{{ data.tutorial?.categoryName }}</span>
                <span class="detail-view-count"><el-icon><View /></el-icon> {{ data.tutorial?.viewCount || 0 }} 次浏览</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="data.tutorial?.contentType === 'video' && data.tutorial?.videoUrl" class="card video-card">
          <video :src="data.tutorial.videoUrl" controls class="video-player" preload="metadata">
            您的浏览器不支持视频播放
          </video>
        </div>

        <div v-if="data.tutorial?.contentType === 'image_text' && data.tutorial?.content" class="card content-card">
          <div class="rich-content" v-html="data.tutorial.content"></div>
        </div>
      </div>

      <div class="detail-sidebar">
        <div class="card sidebar-card">
          <h3 class="sidebar-title">创建者</h3>
          <div class="creator-row">
            <div class="creator-avatar">
              {{ data.tutorial?.creatorName?.charAt(0) || '?' }}
            </div>
            <div class="creator-info">
              <div class="creator-name">{{ data.tutorial?.creatorName }}</div>
              <el-tag size="small" class="creator-role-tag">
                {{ getRoleName(data.tutorial?.creatorRole) }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="card sidebar-card">
          <h3 class="sidebar-title">
            <el-icon><Document /></el-icon>
            发布信息
          </h3>
          <div class="publish-info">
            <div class="publish-row">发布时间：{{ data.tutorial?.createdAt }}</div>
            <div class="publish-row">更新时间：{{ data.tutorial?.updatedAt }}</div>
          </div>
        </div>

        <div class="card sidebar-card">
          <h3 class="sidebar-title">相关教程</h3>
          <div v-if="data.related.length === 0" class="related-empty">暂无相关教程</div>
          <div v-for="item in data.related" :key="item.id" class="related-item" @click="goToTutorial(item.id)">
            <div class="related-title">{{ item.title }}</div>
            <div class="related-meta">{{ item.creatorName }} · {{ item.viewCount || 0 }} 次浏览</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { useRoute } from "vue-router";
import { View, Back, Document } from "@element-plus/icons-vue";

const route = useRoute()

const data = reactive({
  loading: true,
  tutorial: null,
  related: []
})

const getRoleName = (role) => {
  return role === 'USER' ? '玩家' : role ? '管理员' : '未知'
}

const loadTutorial = () => {
  const id = route.query.id
  if (!id) return

  request.get('/tutorial/selectById/' + id).then(res => {
    data.tutorial = res.data
    data.loading = false
  })

  request.get('/tutorial/selectAll', { params: { pageSize: 5, status: 'active' } }).then(res => {
    data.related = (res.data || []).filter(t => t.id !== parseInt(id)).slice(0, 5)
  })
}

const goBack = () => {
  router.push('/front/tutorials')
}

const goToTutorial = (id) => {
  router.push('/front/tutorialDetail?id=' + id)
  data.loading = true
  loadTutorial()
}

onMounted(() => {
  loadTutorial()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.detail-main {
  min-width: 0;
}

.detail-header-card {
  margin-bottom: 20px;
}

.detail-header-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.type-icon.video {
  background: var(--primary-light);
}

.type-icon.text {
  background: rgba(64, 158, 255, 0.1);
}

.detail-header-info {
  flex: 1;
  min-width: 0;
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--text-primary);
}

.detail-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.detail-tags {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-tag {
  background: var(--bg-hover);
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.detail-view-count {
  color: var(--text-tertiary);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-card {
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  border-radius: var(--radius-md);
  max-height: 500px;
}

.content-card {
  margin-bottom: 20px;
}

.rich-content {
  line-height: 1.8;
  font-size: 15px;
  color: var(--text-primary);
}

.rich-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 12px 0;
}

.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  margin-bottom: 0;
}

.sidebar-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.creator-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.creator-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), rgba(var(--primary-rgb), 0.7));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.creator-info {
  min-width: 0;
}

.creator-name {
  font-weight: 600;
  color: var(--text-primary);
}

.creator-role-tag {
  margin-top: 4px;
}

.publish-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.publish-row {
  padding: 4px 0;
}

.related-empty {
  color: var(--text-tertiary);
  font-size: 13px;
  text-align: center;
  padding: 20px;
}

.related-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);
  cursor: pointer;
}

.related-item:last-child {
  border-bottom: none;
}

.related-item:hover .related-title {
  color: var(--primary-color);
}

.related-title {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.related-meta {
  font-size: 12px;
  color: var(--text-tertiary);
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-header-row {
    flex-direction: column;
  }
}
</style>
