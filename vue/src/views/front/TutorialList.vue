<template>
  <div class="main-content">
    <!-- 页面头部 -->
    <div class="page-hero">
      <div class="page-hero-bg"></div>
      <div class="page-hero-content">
        <div class="page-hero-left">
          <div class="page-hero-icon">📖</div>
          <div>
            <div class="page-hero-title">例题讲解</div>
            <div class="page-hero-subtitle">精选教程，助你攻克每一道难题</div>
          </div>
        </div>
        <div class="page-hero-right">
          <div class="page-hero-stats">
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.total }}</div>
              <div class="page-hero-stat-lbl">教程总数</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.categories.length }}</div>
              <div class="page-hero-stat-lbl">分类</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.totalViews }}</div>
              <div class="page-hero-stat-lbl">总浏览</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="card toolbar-card">
      <div class="toolbar-inner">
        <div class="toolbar-search">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="data.title" placeholder="搜索教程" @keyup.enter="load" />
            <el-icon v-if="data.title" class="search-clear" @click="data.title = ''; load()"><Close /></el-icon>
          </div>
        </div>

        <div class="toolbar-tabs">
          <div class="tab" :class="{ active: data.contentType === '' }" @click="data.contentType = ''; load()">全部</div>
          <div class="tab" :class="{ active: data.contentType === 'video' }" @click="data.contentType = 'video'; load()">🎬 视频</div>
          <div class="tab" :class="{ active: data.contentType === 'image_text' }" @click="data.contentType = 'image_text'; load()">📝 图文</div>
        </div>
      </div>

      <div class="category-scroll">
        <div class="category-list">
          <div
            class="category-pill"
            :class="{ active: !data.categoryId }"
            @click="data.categoryId = null; load()"
          >
            全部
          </div>
          <div
            v-for="cat in data.categories"
            :key="cat.id"
            class="category-pill"
            :class="{ active: data.categoryId === cat.id }"
            @click="data.categoryId = cat.id; load()"
          >
            {{ cat.name }}
          </div>
        </div>
      </div>
    </div>

    <!-- 教程卡片网格 -->
    <div v-if="data.tutorials.length === 0" class="card">
      <div class="empty-state">
        <div class="empty-icon">📖</div>
        <div class="empty-text">暂无教程</div>
      </div>
    </div>
    <div v-else class="tutorial-grid">
      <div v-for="item in data.tutorials" :key="item.id" class="tutorial-card" @click="goDetail(item)">
        <div class="tutorial-cover">
          <img v-if="item.coverUrl" :src="item.coverUrl" alt="" />
          <div v-else class="tutorial-cover-placeholder">
            <span v-if="item.contentType === 'video'">🎬</span>
            <span v-else>📝</span>
          </div>
          <div class="tutorial-type-badge">
            <span v-if="item.contentType === 'video'">🎬 视频</span>
            <span v-else>📝 图文</span>
          </div>
        </div>
        <div class="tutorial-info">
          <div class="tutorial-title">{{ item.title }}</div>
          <div class="tutorial-desc">{{ item.description || '暂无简介' }}</div>
          <div class="tutorial-meta">
            <span v-if="item.categoryName" class="tutorial-tag">{{ item.categoryName }}</span>
            <span class="tutorial-stat"><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
            <span class="tutorial-author">{{ item.creatorName }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div style="display: flex; justify-content: center; margin-top: 24px">
      <el-pagination
        @current-change="load"
        :current-page="data.pageNum"
        :page-size="data.pageSize"
        layout="prev, pager, next"
        :total="data.total"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { View, Search, Close } from "@element-plus/icons-vue";
import { loadFlatCategories } from "@/utils/categoryUtils.js";

const data = reactive({
  title: '',
  contentType: '',
  categoryId: null,
  tutorials: [],
  categories: [],
  pageNum: 1,
  pageSize: 12,
  total: 0,
  totalViews: 0
})

const loadCategories = () => {
  loadFlatCategories().then(flat => {
    data.categories = flat
  })
}

const load = () => {
  request.get('/tutorial/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title || null,
      contentType: data.contentType || null,
      categoryId: data.categoryId || null,
      status: 'active'
    }
  }).then(res => {
    data.tutorials = res.data?.list || []
    data.total = res.data?.total || 0
    data.totalViews = data.tutorials.reduce((s, t) => s + (t.viewCount || 0), 0)
  })
}

const goDetail = (item) => {
  // 增加浏览量
  request.put('/tutorial/incrementView/' + item.id)
  router.push('/front/tutorialDetail?id=' + item.id)
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.page-hero {
  position: relative;
  background: var(--gradient-header);
  border-radius: 18px;
  padding: 32px 36px;
  margin-bottom: 24px;
  overflow: hidden;
}
.page-hero-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 80% 50%, rgba(var(--primary-rgb), 0.15) 0%, transparent 60%);
  pointer-events: none;
}
.page-hero-content {
  display: flex; justify-content: space-between; align-items: center;
  position: relative; gap: 24px;
}
.page-hero-left { display: flex; align-items: center; gap: 16px; }
.page-hero-icon { font-size: 40px; line-height: 1; }
.page-hero-title { margin: 0 0 6px; font-size: 26px; font-weight: 800; color: #fff; }
.page-hero-subtitle { margin: 0; font-size: 13px; color: rgba(255,255,255,0.6); }
.page-hero-right { display: flex; flex-direction: column; align-items: flex-end; gap: 16px; }
.page-hero-stats { display: flex; gap: 24px; }
.page-hero-stat { text-align: center; }
.page-hero-stat-val { font-size: 24px; font-weight: 800; color: #fff; line-height: 1; }
.page-hero-stat-lbl { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; }
.page-hero-stat-div { width: 1px; height: 32px; background: rgba(255,255,255,0.15); }

/* 工具栏 */
.toolbar-card { margin-bottom: 16px; }
.toolbar-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; margin-bottom: 12px; }
.toolbar-search { display: flex; align-items: center; gap: 10px; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg-page); border-radius: 10px; padding: 0 12px;
  border: 1px solid var(--border-lighter); height: 36px; width: 260px;
}
.search-box input { border: none; outline: none; flex: 1; font-size: 14px; background: transparent; color: var(--text-primary); }
.search-box input::placeholder { color: var(--text-secondary); }
.search-icon { color: var(--text-secondary); font-size: 14px; }
.search-clear { color: var(--text-secondary); font-size: 14px; cursor: pointer; }
.toolbar-tabs { display: flex; gap: 6px; background: var(--bg-page); border-radius: 8px; padding: 4px; border: 1px solid var(--border-lighter); }
.tab { padding: 5px 14px; border-radius: 6px; font-size: 13px; cursor: pointer; color: var(--text-secondary); transition: all 0.2s; }
.tab:hover { color: var(--text-primary); }
.tab.active { background: var(--primary-color); color: #fff; font-weight: 500; }

/* 分类滚动 */
.category-scroll { overflow-x: auto; -webkit-overflow-scrolling: touch; }
.category-scroll::-webkit-scrollbar { height: 4px; }
.category-scroll::-webkit-scrollbar-thumb { background: var(--border-light); border-radius: 2px; }
.category-list { display: flex; gap: 8px; padding-bottom: 4px; }
.category-pill {
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  background: var(--bg-page);
  color: var(--text-secondary);
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}
.category-pill:hover { border-color: var(--primary-color); color: var(--primary-color); }
.category-pill.active { background: var(--primary-color); color: #fff; border-color: var(--primary-color); }

/* 教程卡片 */
.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.tutorial-card {
  background: var(--bg-card);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-lighter);
}
.tutorial-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.tutorial-cover {
  position: relative;
  height: 170px;
  background: var(--bg-page);
  overflow: hidden;
}
.tutorial-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.tutorial-card:hover .tutorial-cover img { transform: scale(1.03); }
.tutorial-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: var(--gradient-primary);
}
.tutorial-type-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  backdrop-filter: blur(4px);
}

.tutorial-info { padding: 16px; }
.tutorial-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tutorial-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tutorial-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.tutorial-tag {
  background: rgba(var(--primary-rgb), 0.1);
  color: var(--primary-color);
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}
.tutorial-stat {
  color: var(--text-secondary);
  font-size: 12px;
  display: flex; align-items: center; gap: 3px;
}
.tutorial-author {
  color: var(--text-secondary);
  font-size: 12px;
  margin-left: auto;
}

.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: var(--text-secondary); font-size: 14px; }

@media (max-width: 768px) {
  .page-hero-content { flex-direction: column; align-items: flex-start; }
  .page-hero-right { align-items: flex-start; width: 100%; }
  .page-hero-stats { width: 100%; justify-content: space-around; }
  .toolbar-inner { flex-direction: column; align-items: flex-start; }
  .toolbar-tabs { width: 100%; overflow-x: auto; }
}
</style>
