<template>
  <div class="main-content">
    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="page-hero-bg"></div>
      <div class="page-hero-content">
        <div class="page-hero-left">
          <div class="page-hero-icon">📋</div>
          <div>
            <div class="page-hero-title">在线考试</div>
            <div class="page-hero-subtitle">认真作答，诚信考试，展现真实实力</div>
          </div>
        </div>
        <div class="page-hero-right">
          <div class="page-hero-stats">
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ filteredExams.length }}</div>
              <div class="page-hero-stat-lbl">可参加</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ statusCounts.ongoing }}</div>
              <div class="page-hero-stat-lbl">进行中</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ statusCounts.notStarted }}</div>
              <div class="page-hero-stat-lbl">未开始</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索 + 筛选 -->
    <div class="card toolbar-card">
      <div class="toolbar-inner">
        <div class="toolbar-search">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="data.searchText" placeholder="搜索考试名称..." />
            <el-icon v-if="data.searchText" class="search-clear" @click="data.searchText = ''"><Close /></el-icon>
          </div>
        </div>
        <div class="toolbar-tabs">
          <div class="tab" :class="{ active: data.statusFilter === '' }" @click="data.statusFilter = ''">
            全部 <span class="tab-badge">{{ data.tableData.length }}</span>
          </div>
          <div class="tab ongoing" :class="{ active: data.statusFilter === 'ongoing' }" @click="data.statusFilter = 'ongoing'">
            进行中 <span class="tab-badge">{{ statusCounts.ongoing }}</span>
          </div>
          <div class="tab notStarted" :class="{ active: data.statusFilter === 'notStarted' }" @click="data.statusFilter = 'notStarted'">
            未开始 <span class="tab-badge">{{ statusCounts.notStarted }}</span>
          </div>
          <div class="tab ended" :class="{ active: data.statusFilter === 'ended' }" @click="data.statusFilter = 'ended'">
            已结束 <span class="tab-badge">{{ statusCounts.ended }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="data.loading" class="card" style="padding: 60px; text-align: center; color: var(--text-secondary);">
      <el-icon class="spin" :size="28"><Loading /></el-icon>
      <div style="margin-top: 12px;">加载中...</div>
    </div>

    <!-- 考试卡片列表 -->
    <div v-else-if="filteredExams.length === 0" class="card">
      <div class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">{{ data.searchText ? '未找到匹配的考试' : '暂无可参加的考试' }}</div>
      </div>
    </div>
    <div v-else class="exam-card-list">
      <div
        v-for="exam in paginatedExams"
        :key="exam.id"
        class="exam-card"
        :class="[getExamStatus(exam), exam.examType === 'permanent' ? 'permanent' : '']"
      >
        <!-- 状态角标 -->
        <div class="exam-status-badge" :class="getExamStatus(exam)">
          <el-icon v-if="getExamStatus(exam) === 'ongoing'" :size="12"><VideoPlay /></el-icon>
          <el-icon v-else-if="getExamStatus(exam) === 'notStarted'" :size="12"><Clock /></el-icon>
          <el-icon v-else :size="12"><CircleClose /></el-icon>
          {{ getExamStatusText(exam) }}
        </div>

        <div class="exam-info">
          <div class="exam-name">
            {{ exam.name }}
            <el-tag v-if="exam.examType === 'permanent'" size="small" type="success" effect="plain" class="exam-type-tag">常驻</el-tag>
            <el-tag v-else size="small" effect="plain" class="exam-type-tag">统一</el-tag>
          </div>
          <div class="exam-meta">
            <span><el-icon><Document /></el-icon> {{ exam.paperName || '未知试卷' }}</span>
            <span><el-icon><Clock /></el-icon> {{ exam.duration || 60 }} 分钟</span>
            <span v-if="exam.examType !== 'permanent' && exam.startTime">
              <el-icon><Calendar /></el-icon>
              {{ formatTime(exam.startTime) }} ~ {{ formatTime(exam.endTime) }}
            </span>
            <span v-if="exam.examType === 'permanent'">
              <el-icon><Timer /></el-icon> 长期有效
            </span>
          </div>
        </div>
        <div class="exam-action">
          <el-button
            v-if="getExamStatus(exam) === 'ongoing'"
            type="success"
            round
            @click="startExam(exam)"
          >
            <el-icon><VideoPlay /></el-icon> 开始考试
          </el-button>
          <el-button
            v-else-if="getExamStatus(exam) === 'notStarted'"
            type="warning"
            round
            disabled
          >
            <el-icon><Clock /></el-icon> 等待开始
          </el-button>
          <el-button v-else type="info" round disabled>
            <el-icon><CircleClose /></el-icon> 已结束
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredExams.length > data.pageSize" class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="filteredExams.length"
        :page-size="data.pageSize"
        v-model:current-page="data.pageNum"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { Search, Close, Loading, VideoPlay, Clock, CircleClose, Document, Calendar, Timer } from "@element-plus/icons-vue";

const data = reactive({
  tableData: [],
  searchText: '',
  statusFilter: '',
  loading: false,
  pageNum: 1,
  pageSize: 10
})

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const getExamStatus = (exam) => {
  if (exam.examType === 'permanent') return 'ongoing'
  const now = new Date()
  const startTime = exam.startTime ? new Date(exam.startTime) : null
  const endTime = exam.endTime ? new Date(exam.endTime) : null
  if (startTime && now < startTime) return 'notStarted'
  if (endTime && now > endTime) return 'ended'
  return 'ongoing'
}

const getExamStatusText = (exam) => {
  const s = getExamStatus(exam)
  const map = { ongoing: '进行中', notStarted: '未开始', ended: '已结束' }
  return map[s]
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 各状态数量
const statusCounts = computed(() => {
  const counts = { ongoing: 0, notStarted: 0, ended: 0 }
  data.tableData.forEach(exam => {
    const s = getExamStatus(exam)
    counts[s]++
  })
  return counts
})

const filteredExams = computed(() => {
  let list = data.tableData
  if (data.searchText) {
    const kw = data.searchText.toLowerCase()
    list = list.filter(e =>
      (e.name || '').toLowerCase().includes(kw) ||
      (e.paperName || '').toLowerCase().includes(kw)
    )
  }
  if (data.statusFilter) {
    list = list.filter(e => getExamStatus(e) === data.statusFilter)
  }
  return list
})

const paginatedExams = computed(() => {
  const start = (data.pageNum - 1) * data.pageSize
  return filteredExams.value.slice(start, start + data.pageSize)
})

const startExam = (exam) => {
  router.push({ path: '/front/examTaking', query: { examId: exam.id } })
}

const load = () => {
  data.loading = true
  request.get('/exam/getAvailableExams/' + user.id).then(res => {
    if (res.code === '200') {
      data.tableData = res.data || []
    }
  }).finally(() => {
    data.loading = false
  })
}

onMounted(() => {
  load()
})

// 筛选变化时重置页码
watch([() => data.searchText, () => data.statusFilter], () => {
  data.pageNum = 1
})
</script>

<script>
export default { name: 'ExamList' }
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
.toolbar-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
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
.tab { padding: 5px 12px; border-radius: 6px; font-size: 13px; cursor: pointer; color: var(--text-secondary); transition: all 0.2s; display: flex; align-items: center; gap: 4px; }
.tab:hover { color: var(--text-primary); }
.tab.active { background: var(--primary-color); color: #fff; font-weight: 500; }
.tab.ongoing.active { background: #00b42a; }
.tab.notStarted.active { background: #e6a23c; }
.tab.ended.active { background: #909399; }
.tab-badge {
  background: rgba(var(--primary-rgb), 0.1);
  border-radius: 10px;
  padding: 0 5px;
  font-size: 11px;
  min-width: 16px;
  text-align: center;
}
.tab.active .tab-badge { background: rgba(255,255,255,0.25); }

/* 考试卡片 */
.exam-card-list { display: flex; flex-direction: column; gap: 12px; }
.exam-card {
  position: relative;
  overflow: hidden;
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}
.exam-card:hover { border-color: var(--primary-color); box-shadow: var(--shadow-md); }
.exam-status-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4px 12px;
  border-radius: 0 14px 0 10px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
}
.exam-status-badge.ongoing { background: #00b42a; }
.exam-status-badge.notStarted { background: #e6a23c; }
.exam-status-badge.ended { background: #909399; }

.exam-info { flex: 1; min-width: 0; }
.exam-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.exam-type-tag { vertical-align: middle; }
.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  color: var(--text-secondary);
}
.exam-meta span { display: flex; align-items: center; gap: 4px; }
.exam-action { flex-shrink: 0; }

.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: var(--text-secondary); font-size: 14px; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .page-hero-content { flex-direction: column; align-items: flex-start; }
  .page-hero-right { align-items: flex-start; width: 100%; }
  .page-hero-stats { width: 100%; justify-content: space-around; }
  .exam-card { flex-direction: column; align-items: flex-start; }
  .exam-action { width: 100%; }
  .exam-action .el-button { width: 100%; }
}
</style>
