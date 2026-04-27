<template>
  <div class="results-center">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M18 20V10M12 20V4M6 20v-6" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <h1>成绩公示</h1>
          <p>查看与管理考试成绩公示状态</p>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-select v-model="data.selectedExamId" placeholder="选择考试" clearable @change="loadRecords" style="width: 240px" class="exam-sel">
            <template #prefix><el-icon><Document /></el-icon></template>
            <el-option v-for="e in data.examList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </div>
      </div>
      <div class="toolbar-right" v-if="data.selectedExamId">
        <el-button :type="data.isPublished ? 'warning' : 'success'" size="small" @click="togglePublish">
          <el-icon><Promotion /></el-icon>
          {{ data.isPublished ? '取消公示' : '公示成绩' }}
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" v-if="data.selectedExamId && data.records.length">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.records.length }}</div><div class="stat-label">总参考</div></div></div>
      <div class="stat-card stat-pass"><div class="stat-inner"><div class="stat-num">{{ passCount }}</div><div class="stat-label">及格人数</div></div></div>
      <div class="stat-card stat-fail"><div class="stat-inner"><div class="stat-num">{{ data.records.length - passCount }}</div><div class="stat-label">不及格人数</div></div></div>
      <div class="stat-card stat-rate"><div class="stat-inner"><div class="stat-num">{{ passRate }}<span class="stat-unit">%</span></div><div class="stat-label">及格率</div></div></div>
      <div class="stat-card stat-published"><div class="stat-inner"><div class="stat-num">{{ publishedCount }}</div><div class="stat-label">已公示</div></div></div>
    </div>

    <!-- 分数段分布 -->
    <div class="chart-card" v-if="data.selectedExamId && data.records.length">
      <div class="chart-title">分数段分布</div>
      <div class="bar-chart">
        <div v-for="seg in scoreDistribution" :key="seg.label" class="bar-item">
          <div class="bar-label">{{ seg.label }}</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: seg.percent + '%', background: seg.color }"></div>
          </div>
          <div class="bar-count">{{ seg.count }}人</div>
        </div>
      </div>
    </div>

    <!-- 成绩列表 -->
    <div class="records-card" v-if="data.selectedExamId" v-loading="data.loading">
      <el-table :data="data.records" stripe
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="考生" width="140">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <div class="student-avatar">{{ (scope.row.studentName || '?')[0] }}</div>
              <div>
                <div style="font-weight: 600; font-size: 14px;">{{ scope.row.studentName }}</div>
                <div style="font-size: 12px; color: #9ca3af;">{{ scope.row.studentNo }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="自动评分" width="90" align="center">
          <template #default="scope"><span class="score-obj">{{ scope.row.autoScore ?? '-' }}</span></template>
        </el-table-column>
        <el-table-column label="人工评分" width="90" align="center">
          <template #default="scope"><span class="score-man">{{ scope.row.manualScore ?? '-' }}</span></template>
        </el-table-column>
        <el-table-column label="总分" width="80" align="center" sortable sort-by="totalScore">
          <template #default="scope">
            <span class="score-total" :class="getScoreClass(scope.row.totalScore)">{{ scope.row.totalScore || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="80" align="center">
          <template #default="scope">
            <span class="is-pass-badge" :class="scope.row.isPass ? 'pass' : 'fail'">{{ scope.row.isPass ? '及格' : '不及格' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="公示" width="80" align="center">
          <template #default="scope">
            <span class="publish-badge" :class="scope.row.isPublished ? 'yes' : 'no'">{{ scope.row.isPublished ? '已公示' : '未公示' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空提示 -->
    <div v-if="!data.selectedExamId" class="empty-hint">
      <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
        <path d="M18 20V10M12 20V4M6 20v-6" stroke="#93c5fd" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <p>请选择考试查看成绩公示情况</p>
    </div>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Search, Document, Promotion } from '@element-plus/icons-vue'

const data = reactive({
  examList: [],
  selectedExamId: null,
  records: [],
  isPublished: false,
  loading: false,
})

const passCount = computed(() => data.records.filter(r => r.isPass).length)
const publishedCount = computed(() => data.records.filter(r => r.isPublished).length)
const passRate = computed(() => {
  if (!data.records.length) return 0
  return Math.round((passCount.value / data.records.length) * 100)
})

const scoreDistribution = computed(() => {
  const list = data.records
  if (!list.length) return []
  const ranges = [
    { label: '90-100', min: 90, max: 101, color: 'linear-gradient(90deg, #16a34a, #4ade80)' },
    { label: '80-89', min: 80, max: 90, color: 'linear-gradient(90deg, #2563eb, #60a5fa)' },
    { label: '70-79', min: 70, max: 80, color: 'linear-gradient(90deg, #d97706, #fbbf24)' },
    { label: '60-69', min: 60, max: 70, color: 'linear-gradient(90deg, #ea580c, #fb923c)' },
    { label: '<60', min: -1, max: 60, color: 'linear-gradient(90deg, #dc2626, #f87171)' },
  ]
  const total = list.length
  return ranges.map(r => {
    const count = list.filter(s => (s.totalScore || 0) >= r.min && (s.totalScore || 0) < r.max).length
    return { ...r, count, percent: total ? Math.round((count / total) * 100) : 0 }
  })
})

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 70) return 'good'
  if (score >= 60) return 'average'
  return 'poor'
}

const loadExams = () => {
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') data.examList = res.data || []
  })
}

const loadRecords = () => {
  if (!data.selectedExamId) { data.records = []; return }
  data.loading = true
  request.get('/examRecord/selectAll', { params: { examId: data.selectedExamId } }).then(res => {
    if (res.code === '200') {
      data.records = res.data || []
      if (data.records.length) data.isPublished = data.records[0].isPublished
    }
  }).finally(() => { data.loading = false })
}

const togglePublish = () => {
  const action = data.isPublished ? 'unpublishResults' : 'publishResults'
  request.put('/examApproval/' + action + '/' + data.selectedExamId).then(res => {
    if (res.code === '200') {
      ElMessage.success(data.isPublished ? '已取消公示' : '公示成功')
      data.isPublished = !data.isPublished
      loadRecords()
    } else ElMessage.error(res.msg)
  })
}

onMounted(() => { loadExams() })
</script>

<style scoped>
.results-center { padding: 0; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #0891b2 0%, #22d3ee 50%, #67e8f9 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 8px 32px rgba(8, 145, 178, 0.25); }
.hero-left { display: flex; align-items: center; gap: 14px; }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #fff; }
.page-hero p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { display: flex; align-items: center; gap: 10px; }
.s-icon { color: #9ca3af; }
.exam-sel :deep(.el-select__wrapper) { border-radius: 10px; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 16px 18px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #0891b2, #22d3ee); }
.stat-pass .stat-inner { background: linear-gradient(135deg, #16a34a, #4ade80); }
.stat-fail .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-rate .stat-inner { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.stat-published .stat-inner { background: linear-gradient(135deg, #d97706, #fbbf24); }
.stat-num { font-size: 26px; font-weight: 800; }
.stat-unit { font-size: 14px; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 分数段分布 ===== */
.chart-card { background: #fff; border-radius: 14px; padding: 20px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.chart-title { font-size: 15px; font-weight: 600; color: #374151; margin-bottom: 16px; }
.bar-chart { display: flex; flex-direction: column; gap: 10px; }
.bar-item { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 50px; font-size: 13px; color: #6b7280; text-align: right; }
.bar-track { flex: 1; height: 22px; background: #f5f7fa; border-radius: 6px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 6px; transition: width 0.6s ease; min-width: 2px; }
.bar-count { width: 48px; font-size: 13px; color: #9ca3af; }

/* ===== 成绩列表 ===== */
.records-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.student-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #0891b2, #22d3ee); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; }
.score-obj { font-weight: 600; color: #2563eb; }
.score-man { font-weight: 600; color: #d97706; }
.score-total { font-weight: 700; font-size: 15px; }
.score-total.excellent { color: #16a34a; }
.score-total.good { color: #2563eb; }
.score-total.average { color: #d97706; }
.score-total.poor { color: #dc2626; }
.is-pass-badge, .publish-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.is-pass-badge.pass { background: #dcfce7; color: #16a34a; }
.is-pass-badge.fail { background: #fee2e2; color: #dc2626; }
.publish-badge.yes { background: #dcfce7; color: #16a34a; }
.publish-badge.no { background: #f1f5f9; color: #6b7280; }

/* 空提示 */
.empty-hint { text-align: center; padding: 80px; background: #fff; border-radius: 14px; border: 1px solid var(--el-border-color-lighter); }
.empty-hint p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
