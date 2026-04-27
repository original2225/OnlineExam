<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">⭐</div>
      <div class="page-hero-title">我的收藏</div>
      <div class="page-hero-subtitle">收藏好题，随时回顾</div>
      <div class="page-hero-stats">
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ data.favorites.length }}</div>
          <div class="page-hero-stat-lbl">收藏题目</div>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <el-input v-model="data.searchText" placeholder="搜索题目内容..." clearable style="width: 240px;" size="default">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="data.categoryId" placeholder="题目分类" clearable style="width: 160px;" size="default" @change="loadFavorites">
          <el-option v-for="c in data.categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button type="primary" @click="startPractice" :disabled="filteredFavorites.length === 0">
          <el-icon><Edit /></el-icon> 收藏刷题
        </el-button>
      </div>
    </div>

    <div v-if="filteredFavorites.length === 0" class="empty-state">
      <div class="empty-icon-anim">
        <span class="empty-star">⭐</span>
        <span class="empty-sparkle sparkle-1">✨</span>
        <span class="empty-sparkle sparkle-2">✨</span>
      </div>
      <div class="empty-text">{{ data.favorites.length === 0 ? '还没有收藏题目' : '没有符合条件的收藏' }}</div>
      <div class="empty-hint" v-if="data.favorites.length === 0">在练习或考试结果中点击星标收藏吧</div>
    </div>

    <div v-else class="question-list">
      <div v-for="(item, idx) in paginatedFavorites" :key="item.id" class="question-card">
        <div class="question-header">
          <span class="question-index">{{ questionIndex(idx) }}</span>
          <el-tag :type="item.question.type === '选择题' ? '' : item.question.type === '判断题' ? 'success' : 'warning'" size="small">{{ item.question.type }}</el-tag>
          <el-tag type="info" size="small">{{ item.question.categoryName || '未分类' }}</el-tag>
          <el-tag :type="item.question.difficulty === 'easy' ? 'success' : item.question.difficulty === 'hard' ? 'danger' : 'warning'" size="small">
            {{ item.question.difficulty === 'easy' ? '简单' : item.question.difficulty === 'hard' ? '困难' : '中等' }}
          </el-tag>
        </div>
        <div class="question-content" v-html="item.question.content"></div>
        <div v-if="item.question.options" class="question-options">
          <div v-for="(opt, key) in parseOptions(item.question.options)" :key="key" class="option-row">
            <span class="option-key">{{ key }}.</span> {{ opt }}
          </div>
        </div>
        <div class="question-footer">
          <span class="answer-text">正确答案: <strong>{{ item.question.answer }}</strong></span>
          <el-button type="danger" text size="small" @click="removeFavorite(item.id)">
            <el-icon><Delete /></el-icon> 取消收藏
          </el-button>
        </div>
        <div v-if="item.question.analysis" class="analysis-box">
          <strong>解析:</strong> {{ item.question.analysis }}
        </div>
      </div>
    </div>

    <div v-if="filteredFavorites.length > data.pageSize" class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="filteredFavorites.length"
        :page-size="data.pageSize"
        v-model:current-page="data.pageNum"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const data = reactive({
  favorites: [],
  categoryId: '',
  categories: [],
  searchText: '',
  pageNum: 1,
  pageSize: 10,
})

onMounted(() => {
  loadCategories()
  loadFavorites()
})

const loadCategories = () => {
  request.get('/questionCategory/selectAll').then(res => {
    data.categories = res.data || []
  })
}

const loadFavorites = () => {
  request.get('/questionFavorite/selectByUser', {
    params: { userId: user.id, userRole: user.role, categoryId: data.categoryId || undefined }
  }).then(res => {
    data.favorites = res.data || []
  })
}

const removeFavorite = (id) => {
  request.delete('/questionFavorite/delete/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('已取消收藏')
      loadFavorites()
    }
  })
}

const parseOptions = (options) => {
  if (!options) return {}
  try { return typeof options === 'string' ? JSON.parse(options) : options } catch { return {} }
}

const startPractice = () => {
  router.push({ path: '/front/practiceMode', query: { favoriteMode: 'true' } })
}

const filteredFavorites = computed(() => {
  let list = data.favorites
  if (data.searchText) {
    const s = data.searchText.toLowerCase()
    list = list.filter(item => item.question?.content?.toLowerCase().includes(s))
  }
  return list
})

const paginatedFavorites = computed(() => {
  const start = (data.pageNum - 1) * data.pageSize
  return filteredFavorites.value.slice(start, start + data.pageSize)
})

const questionIndex = (idx) => {
  return (data.pageNum - 1) * data.pageSize + idx + 1
}

watch([() => data.searchText, () => data.categoryId], () => {
  data.pageNum = 1
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.filter-left { display: flex; gap: 8px; flex-wrap: wrap; }

.empty-state {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon-anim {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}
.empty-star {
  font-size: 64px;
  display: inline-block;
  animation: starBounce 2s ease-in-out infinite;
}
.empty-sparkle {
  position: absolute;
  font-size: 20px;
  animation: sparkleFade 2s ease-in-out infinite;
}
.sparkle-1 { top: -8px; right: -16px; animation-delay: 0.3s; }
.sparkle-2 { bottom: -4px; left: -14px; animation-delay: 0.8s; }
@keyframes starBounce {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-10px) rotate(-5deg); }
  50% { transform: translateY(0) rotate(0deg); }
  75% { transform: translateY(-6px) rotate(5deg); }
}
@keyframes sparkleFade {
  0%, 100% { opacity: 0; transform: scale(0.5); }
  50% { opacity: 1; transform: scale(1.2); }
}
.empty-text {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}
.empty-hint {
  color: var(--text-secondary);
  font-size: 14px;
}

.question-list { display: flex; flex-direction: column; gap: 16px; }
.question-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}
.question-card:hover {
  box-shadow: var(--shadow-md);
  border-color: rgba(var(--primary-rgb), 0.2);
}
.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.question-index {
  background: var(--primary-color);
  color: #fff;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}
.question-content {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.question-options { margin-bottom: 12px; }
.option-row {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  margin-bottom: 4px;
}
.option-key { font-weight: 600; margin-right: 4px; }
.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-lighter);
}
.answer-text { font-size: 13px; color: var(--text-secondary); }
.answer-text strong { color: #67c23a; }
.analysis-box {
  margin-top: 12px;
  padding: 12px;
  background: var(--bg-page);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

@media (max-width: 768px) {
  .filter-bar { flex-direction: column; align-items: stretch; }
  .filter-left { flex-direction: column; }
  .filter-left .el-input,
  .filter-left .el-select { width: 100% !important; }
}
</style>
