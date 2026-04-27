<template>
  <div class="main-content">
    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="page-hero-bg"></div>
      <div class="page-hero-content">
        <div class="page-hero-left">
          <div class="page-hero-icon">📊</div>
          <div>
            <div class="page-hero-title">我的结果</div>
            <div class="page-hero-subtitle">每一次努力，都是成长的印记</div>
          </div>
        </div>
        <div class="page-hero-right">
          <div class="page-hero-stats">
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.stats.total }}</div>
              <div class="page-hero-stat-lbl">总审核数</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.stats.avg }}</div>
              <div class="page-hero-stat-lbl">平均分</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.stats.passRate }}%</div>
              <div class="page-hero-stat-lbl">通过率</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="score-stats-grid">
      <div class="stats-card">
        <div class="stats-icon blue">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="stats-body">
          <div class="stats-value">{{ data.stats.total }}</div>
          <div class="stats-label">总审核数</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon green">
          <el-icon :size="22"><Trophy /></el-icon>
        </div>
        <div class="stats-body">
          <div class="stats-value">{{ data.stats.highest }}</div>
          <div class="stats-label">最高分</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon orange">
          <el-icon :size="22"><TrendCharts /></el-icon>
        </div>
        <div class="stats-body">
          <div class="stats-value">{{ data.stats.avg }}</div>
          <div class="stats-label">平均分</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon purple">
          <el-icon :size="22"><CircleCheck /></el-icon>
        </div>
        <div class="stats-body">
          <div class="stats-value">{{ data.stats.passRate }}%</div>
          <div class="stats-label">通过率</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="card toolbar-card">
      <div class="toolbar-inner">
        <div class="toolbar-title">
          <span class="t-dot"></span>
          结果记录
          <span class="toolbar-count">共 {{ data.stats.total }} 条</span>
        </div>
        <div class="toolbar-actions">
          <el-select v-model="data.sortKey" size="small" style="width: 130px;" @change="handleSort">
            <el-option label="按时间排序" value="submitTime" />
            <el-option label="按分数排序" value="totalScore" />
          </el-select>
          <el-button size="small" circle @click="load" :loading="data.loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 结果列表 -->
    <div class="card list-card">
      <div v-if="data.loading" class="loading-state">
        <el-icon class="spin" :size="28"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="sortedData.length === 0" class="empty-state">
        <div class="empty-icon">📊</div>
        <div class="empty-text">暂无审核结果</div>
        <div class="empty-hint">参加审核后，结果会自动同步到这里</div>
      </div>
      <div v-else class="score-card-list">
        <div v-for="record in paginatedData" :key="record.id" class="score-card">
          <!-- 分数圆圈 -->
          <div class="score-circle" :class="getScoreClass(record.totalScore)">
            <div class="sc-val">{{ record.totalScore || 0 }}</div>
            <div class="sc-lbl">总分</div>
          </div>

          <!-- 信息区 -->
          <div class="score-info">
            <div class="score-name">
              {{ record.examName }}
              <el-tag :type="record.isPass ? 'success' : 'danger'" size="small" effect="light" class="pass-tag">
                {{ record.isPass ? '通过' : '未通过' }}
              </el-tag>
            </div>
            <div class="score-bar-wrap">
              <div class="score-bar">
                <div
                  class="score-bar-fill"
                  :class="getScoreClass(record.totalScore)"
                  :style="{ width: Math.min(record.totalScore || 0, 100) + '%' }"
                ></div>
              </div>
              <span class="score-bar-text">{{ record.totalScore || 0 }} 分</span>
            </div>
            <div class="score-detail">
              <span><el-icon><Edit /></el-icon> 客观 {{ record.autoScore || 0 }}</span>
              <span><el-icon><Document /></el-icon> 主观 {{ record.manualScore || 0 }}</span>
              <span><el-icon><Clock /></el-icon> 用时 {{ Math.floor((record.duration || 0) / 60) }} 分钟</span>
              <span><el-icon><Calendar /></el-icon> {{ formatTime(record.submitTime) }}</span>
            </div>
          </div>

          <!-- 操作 -->
          <div class="score-action">
            <el-button type="primary" size="small" round @click="viewDetail(record)">
              <el-icon><View /></el-icon> 查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="sortedData.length > data.pageSize" class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="sortedData.length"
          :page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="data.detailDialogVisible"
      title="审核详情"
      width="70%"
      :close-on-click-modal="false"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="data.currentRecord" class="detail-body">
        <!-- 分数概览 -->
        <div class="detail-overview">
          <div class="do-card primary">
            <div class="do-val">{{ data.currentRecord.totalScore || 0 }}</div>
            <div class="do-lbl">总分</div>
          </div>
          <div class="do-card success">
            <div class="do-val">{{ data.currentRecord.autoScore || 0 }}</div>
            <div class="do-lbl">客观题得分</div>
          </div>
          <div class="do-card warning">
            <div class="do-val">{{ data.currentRecord.manualScore || 0 }}</div>
            <div class="do-lbl">主观题得分</div>
          </div>
          <div class="do-card info">
            <div class="do-val">{{ Math.floor((data.currentRecord.duration || 0) / 60) }}</div>
            <div class="do-lbl">用时(分钟)</div>
          </div>
        </div>

        <!-- 结果对比 -->
        <div v-if="data.compareData" class="detail-compare">
          <div class="dc-title">结果对比</div>
          <div class="dc-grid">
            <div class="dc-item">
              <div class="dc-val">{{ data.compareData.avgScore }}</div>
              <div class="dc-lbl">分支平均</div>
            </div>
            <div class="dc-item">
              <div class="dc-val">{{ data.compareData.maxScore }}</div>
              <div class="dc-lbl">最高分</div>
            </div>
            <div class="dc-item highlight">
              <div class="dc-val">{{ data.compareData.percentile }}%</div>
              <div class="dc-lbl">击败了</div>
            </div>
          </div>
        </div>

        <!-- 答题详情 -->
        <div class="detail-answers">
          <div class="da-title">答题详情</div>
          <div
            v-for="(answer, index) in data.currentAnswers"
            :key="answer.id"
            class="answer-detail-item"
            :class="answer.isCorrect ? 'correct' : 'wrong'"
          >
            <div class="answer-header">
              <div class="answer-meta">
                <span class="q-badge">{{ index + 1 }}</span>
                <el-tag v-if="answer.question?.type === 'single'" type="success" size="small">单选题</el-tag>
                <el-tag v-else-if="answer.question?.type === 'multiple'" type="primary" size="small">多选题</el-tag>
                <el-tag v-else-if="answer.question?.type === 'judge'" type="warning" size="small">判断题</el-tag>
                <el-tag v-else type="info" size="small">简答题</el-tag>
                <span class="q-score">({{ answer.question?.score }}分)</span>
                <el-icon v-if="answer.isCorrect" class="status-icon correct" :size="16"><CircleCheck /></el-icon>
                <el-icon v-else-if="answer.question?.type !== 'essay'" class="status-icon wrong" :size="16"><CircleClose /></el-icon>
              </div>
              <div class="answer-score">得分：{{ answer.score || 0 }}</div>
            </div>

            <div class="question-content">{{ answer.question?.content }}</div>

            <!-- 图片题展示 -->
            <div v-if="answer.question?.images?.length" class="question-images">
              <img v-for="(img, i) in answer.question.images" :key="i"
                   :src="img.url" @click="previewImage(img.url)" />
            </div>

            <!-- 选项展示 -->
            <div v-if="answer.question?.options" class="options-list">
              <div v-for="(text, key) in answer.question.options" :key="key"
                   class="option-readonly"
                   :class="{
                     'correct-option': key.toUpperCase() === (answer.question.answer || '').toUpperCase(),
                     'wrong-option': key.toUpperCase() === (answer.studentAnswer || '').toUpperCase() && !answer.isCorrect
                   }">
                <span class="opt-key">{{ key }}</span>
                <span class="opt-text">{{ text }}</span>
              </div>
            </div>

            <div class="answer-row">
              <span class="ar-label">你的答案：</span>
              <span :class="answer.isCorrect ? 'correct-text' : 'wrong-text'" class="ar-value">{{ answer.studentAnswer || '未作答' }}</span>
            </div>
            <div v-if="answer.question?.type !== 'essay'" class="answer-row">
              <span class="ar-label">正确答案：</span>
              <span class="ar-value correct-text">{{ answer.question?.answer }}</span>
            </div>
            <div v-if="answer.question?.analysis" class="analysis-box">
              <el-icon :size="14"><InfoFilled /></el-icon>
              <span><strong>解析：</strong>{{ answer.question?.analysis }}</span>
            </div>
            <div v-if="answer.comment" class="comment-box">
              <el-icon :size="14"><ChatDotRound /></el-icon>
              <span><strong>阅卷评语：</strong>{{ answer.comment }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer v-if="data.previewVisible" :url-list="[data.previewUrl]" @close="data.previewVisible = false" />
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import request from "@/utils/request.js";
import {
  Document, Trophy, TrendCharts, CircleCheck, CircleClose,
  InfoFilled, ChatDotRound, Edit, Clock, Calendar, View,
  Refresh, Loading
} from "@element-plus/icons-vue";

const data = reactive({
  tableData: [],
  detailDialogVisible: false,
  currentRecord: null,
  currentAnswers: [],
  stats: { total: 0, highest: 0, avg: 0, passRate: 0 },
  loading: false,
  sortKey: 'submitTime',
  sortDesc: true,
  pageNum: 1,
  pageSize: 10,
  compareData: null,
  previewVisible: false,
  previewUrl: ''
})

// 排序后的数据
const sortedData = computed(() => {
  const arr = [...data.tableData]
  arr.sort((a, b) => {
    let av = a[data.sortKey]
    let bv = b[data.sortKey]
    if (data.sortKey === 'submitTime') {
      av = av ? new Date(av).getTime() : 0
      bv = bv ? new Date(bv).getTime() : 0
    } else {
      av = av || 0
      bv = bv || 0
    }
    return data.sortDesc ? bv - av : av - bv
  })
  return arr
})

// 分页数据
const paginatedData = computed(() => {
  const start = (data.pageNum - 1) * data.pageSize
  return sortedData.value.slice(start, start + data.pageSize)
})

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 60) return 'average'
  return 'poor'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

const previewImage = (url) => {
  data.previewUrl = url
  data.previewVisible = true
}

const handleSort = () => {
  data.pageNum = 1
}

const handlePageChange = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const viewDetail = (row) => {
  data.currentRecord = row
  data.compareData = null
  request.get('/examRecord/detail/' + row.id).then(res => {
    if (res.code === '200') {
      data.currentAnswers = res.data.answers || []
      data.detailDialogVisible = true
    }
  })
  // 加载对比数据
  if (row.examId) {
    request.get('/examRecord/compareScore', { params: { examId: row.examId, studentId: user.id } }).then(res => {
      if (res.code === '200' && res.data) data.compareData = res.data
    })
  }
}

const load = () => {
  data.loading = true
  request.get('/score/getByStudentId/' + user.id).then(res => {
    if (res.code === '200') {
      data.tableData = res.data || []
      data.pageNum = 1
      // 计算统计
      const scores = data.tableData
      data.stats.total = scores.length
      if (scores.length) {
        const allScores = scores.map(s => s.totalScore || 0)
        data.stats.highest = Math.max(...allScores)
        data.stats.avg = Math.round(allScores.reduce((a, b) => a + b, 0) / scores.length)
        const passed = scores.filter(s => s.isPass).length
        data.stats.passRate = Math.round((passed / scores.length) * 100)
      } else {
        data.stats.highest = 0
        data.stats.avg = 0
        data.stats.passRate = 0
      }
    }
  }).finally(() => {
    data.loading = false
  })
}

onMounted(() => {
  load()
})
</script>

<script>
export default { name: 'MyScores' }
</script>

<style scoped>
/* 页面头图 */
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

/* 统计卡片 */
.score-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stats-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}
.stats-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}
.stats-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stats-icon.blue { background: rgba(96,165,250,0.12); color: #60a5fa; }
.stats-icon.green { background: rgba(74,222,128,0.12); color: #4ade80; }
.stats-icon.orange { background: rgba(251,146,60,0.12); color: #fb923c; }
.stats-icon.purple { background: rgba(167,139,250,0.12); color: #a78bfa; }
.stats-body { flex: 1; }
.stats-value { font-size: 22px; font-weight: 700; color: var(--text-primary); line-height: 1; }
.stats-label { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }

/* 工具栏 */
.toolbar-card { margin-bottom: 16px; }
.toolbar-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.toolbar-title { display: flex; align-items: center; gap: 10px; font-size: 15px; font-weight: 600; color: var(--text-primary); }
.t-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary-color); }
.toolbar-count { font-size: 12px; color: var(--text-secondary); font-weight: 400; }
.toolbar-actions { display: flex; align-items: center; gap: 10px; }

/* 列表卡片 */
.list-card { padding: 20px 24px; }

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px;
  color: var(--text-secondary);
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: var(--text-primary); font-size: 15px; font-weight: 500; margin-bottom: 6px; }
.empty-hint { color: var(--text-secondary); font-size: 13px; }

/* 结果卡片 */
.score-card-list { display: flex; flex-direction: column; gap: 12px; }
.score-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 18px;
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}
.score-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}
.score-circle {
  width: 64px; height: 64px; border-radius: 50%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  flex-shrink: 0; font-weight: 700;
  border: 3px solid;
  background: var(--bg-card);
}
.score-circle.excellent { border-color: #4ade80; color: #4ade80; }
.score-circle.good { border-color: #60a5fa; color: #60a5fa; }
.score-circle.average { border-color: #fbbf24; color: #fbbf24; }
.score-circle.poor { border-color: #f87171; color: #f87171; }
.sc-val { font-size: 20px; line-height: 1; }
.sc-lbl { font-size: 10px; font-weight: 500; margin-top: 2px; opacity: 0.8; }

.score-info { flex: 1; min-width: 0; }
.score-name {
  font-size: 15px; font-weight: 600; color: var(--text-primary);
  margin-bottom: 8px; display: flex; align-items: center; gap: 8px;
}
.pass-tag { vertical-align: middle; }
.score-bar-wrap { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.score-bar { flex: 1; height: 6px; background: var(--bg-page); border-radius: 3px; overflow: hidden; }
.score-bar-fill { height: 100%; border-radius: 3px; transition: width 0.4s ease; }
.score-bar-fill.excellent { background: #4ade80; }
.score-bar-fill.good { background: #60a5fa; }
.score-bar-fill.average { background: #fbbf24; }
.score-bar-fill.poor { background: #f87171; }
.score-bar-text { font-size: 12px; color: var(--text-secondary); min-width: 40px; text-align: right; }

.score-detail {
  display: flex; flex-wrap: wrap; gap: 14px;
  font-size: 12px; color: var(--text-secondary);
}
.score-detail span { display: flex; align-items: center; gap: 3px; }
.score-action { flex-shrink: 0; }

.pagination-wrap { display: flex; justify-content: center; padding-top: 20px; }

/* 详情弹窗 */
.detail-body { padding: 4px; }
.detail-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.do-card {
  background: var(--bg-page);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 1px solid var(--border-lighter);
}
.do-card.primary .do-val { color: var(--primary-color); }
.do-card.success .do-val { color: var(--success-color, #4ade80); }
.do-card.warning .do-val { color: var(--warning-color, #fbbf24); }
.do-card.info .do-val { color: var(--info-color, #60a5fa); }
.do-val { font-size: 28px; font-weight: 700; line-height: 1; }
.do-lbl { font-size: 12px; color: var(--text-secondary); margin-top: 6px; }

.detail-compare {
  background: var(--bg-page);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  border: 1px solid var(--border-lighter);
}
.dc-title { font-size: 14px; font-weight: 600; margin-bottom: 12px; color: var(--text-primary); }
.dc-grid { display: flex; gap: 24px; align-items: center; }
.dc-item { text-align: center; min-width: 80px; }
.dc-item.highlight .dc-val { color: var(--success-color, #4ade80); }
.dc-val { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.dc-lbl { color: var(--text-secondary); font-size: 12px; margin-top: 2px; }

.detail-answers { }
.da-title { font-size: 14px; font-weight: 600; margin-bottom: 12px; color: var(--text-primary); }

.answer-detail-item {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 14px;
  border: 1px solid var(--border-lighter);
  background: var(--bg-card);
  transition: all 0.2s;
}
.answer-detail-item.correct { border-left: 4px solid #4ade80; }
.answer-detail-item.wrong { border-left: 4px solid #f87171; }

.answer-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 10px; flex-wrap: wrap; gap: 8px;
}
.answer-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.q-score { color: var(--text-secondary); font-size: 13px; }
.status-icon.correct { color: #4ade80; }
.status-icon.wrong { color: #f87171; }
.answer-score { font-weight: 600; color: var(--text-primary); }

.question-content { font-size: 15px; line-height: 1.6; color: var(--text-primary); margin-bottom: 10px; }
.question-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.question-images img { max-width: 150px; max-height: 120px; border-radius: 8px; cursor: pointer; object-fit: cover; }

.options-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.option-readonly {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  background: var(--bg-page);
  display: flex; align-items: center; gap: 8px;
}
.option-readonly.correct-option { background: rgba(74,222,128,0.12); color: #22c55e; font-weight: 500; }
.option-readonly.wrong-option { background: rgba(248,113,113,0.12); color: #ef4444; font-weight: 500; }
.opt-key { font-weight: 600; min-width: 18px; }
.opt-text { flex: 1; }

.answer-row { margin-bottom: 6px; font-size: 14px; color: var(--text-secondary); }
.ar-label { opacity: 0.8; }
.ar-value { font-weight: 500; }
.correct-text { color: #22c55e; }
.wrong-text { color: #ef4444; }

.analysis-box {
  display: flex; align-items: flex-start;
  color: var(--text-secondary);
  font-size: 13px;
  background: var(--bg-page);
  padding: 10px 14px;
  border-radius: 8px;
  margin-top: 8px;
  gap: 8px;
}
.comment-box {
  display: flex; align-items: flex-start;
  color: #d97706;
  font-size: 13px;
  background: rgba(217, 119, 6, 0.08);
  padding: 10px 14px;
  border-radius: 8px;
  margin-top: 8px;
  gap: 8px;
}

.q-badge {
  background: var(--primary-color);
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .score-stats-grid { grid-template-columns: repeat(2, 1fr); }
  .detail-overview { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .page-hero-content { flex-direction: column; align-items: flex-start; }
  .page-hero-right { align-items: flex-start; width: 100%; }
  .page-hero-stats { width: 100%; justify-content: space-around; }
  .score-card { flex-direction: column; align-items: flex-start; }
  .score-action { width: 100%; }
  .score-action .el-button { width: 100%; }
  .dc-grid { justify-content: space-around; }
}
@media (max-width: 480px) {
  .score-stats-grid { grid-template-columns: 1fr; }
  .detail-overview { grid-template-columns: 1fr; }
}
</style>
