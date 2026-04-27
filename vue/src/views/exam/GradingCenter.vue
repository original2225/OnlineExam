<template>
  <div class="gc" v-loading="data.loading">

    <!-- 顶部工具栏 -->
    <div class="gc-toolbar">
      <div class="gct-left">
        <el-select v-model="data.selectedExamId" placeholder="选择审核" clearable @change="loadRecords" style="width: 260px">
          <template #prefix><el-icon><Document /></el-icon></template>
          <el-option v-for="e in data.examList" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-select v-model="data.filterStatus" placeholder="答题状态" clearable @change="loadRecords" style="width: 140px">
          <el-option label="已完成" value="completed" />
          <el-option label="超时" value="timeout" />
        </el-select>
      </div>
      <div class="gct-right" v-if="data.progress && data.selectedExamId">
        <div class="gct-prog-item">
          <span class="gct-pi-label">总答卷</span>
          <span class="gct-pi-val">{{ data.progress.total || 0 }}</span>
        </div>
        <div class="gct-prog-sep"></div>
        <div class="gct-prog-item">
          <span class="gct-pi-label">已批阅</span>
          <span class="gct-pi-val ok">{{ data.progress.graded || 0 }}</span>
        </div>
        <div class="gct-prog-sep"></div>
        <div class="gct-prog-item">
          <span class="gct-pi-label">待批阅</span>
          <span class="gct-pi-val warn">{{ (data.progress.total - data.progress.graded) || 0 }}</span>
        </div>
        <div class="gct-prog-pct">{{ gradingPercent }}%</div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="gc-progress-card" v-if="data.progress && data.selectedExamId">
      <div class="gcp-track">
        <div class="gcp-fill" :style="{ width: gradingPercent + '%' }"></div>
      </div>
    </div>

    <!-- 空提示 -->
    <div class="gc-empty" v-if="!data.selectedExamId">
      <div class="gc-empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
          <path d="M9 12l2 2 4-4" stroke="var(--primary-color)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="var(--border-light)" stroke-width="1.5"/>
        </svg>
      </div>
      <p>请从上方选择一个审核</p>
      <span>选择后可查看并批阅答卷</span>
    </div>

    <!-- 答卷列表 -->
    <div class="gc-list" v-if="data.selectedExamId && !data.loading">

      <transition-group name="record-anim" tag="div" class="gc-list-inner">
        <div v-for="row in data.records" :key="row.id" class="gc-record-card">
          <div class="grc-avatar" :style="{ background: avatarGradient(row) }">
            {{ (row.studentName || '?')[0] }}
          </div>
          <div class="grc-body">
            <div class="grc-header">
              <span class="grc-name">{{ row.studentName }}</span>
              <span class="grc-no">{{ row.studentNo }}</span>
              <el-tag :type="examStatusType(row.examStatus)" size="small" round>{{ examStatusLabel(row.examStatus) }}</el-tag>
            </div>
            <div class="grc-meta">
              <span class="grcm-chip">
                <el-icon><Clock /></el-icon>
                {{ formatTime(row.submitTime) }}
              </span>
              <span class="grcm-chip" v-if="row.duration">
                <el-icon><Timer /></el-icon>
                {{ Math.round(row.duration / 60) }}分钟
              </span>
            </div>
            <div class="grc-scores">
              <div class="grcs-chip">
                <span class="grcsl">客观</span>
                <span class="grcsv auto">{{ row.autoScore ?? '—' }}</span>
              </div>
              <span class="grcs-op">+</span>
              <div class="grcs-chip" :class="{ ungraded: row.manualScore == null }">
                <span class="grcsl">主观</span>
                <span class="grcsv">{{ row.manualScore != null ? row.manualScore : '待阅' }}</span>
              </div>
              <span class="grcs-op">=</span>
              <div class="grcs-chip total">
                <span class="grcsl">总分</span>
                <span class="grcsv">{{ row.totalScore ?? 0 }}</span>
              </div>
            </div>
          </div>
          <div class="grc-action">
            <el-button
              type="primary"
              round
              size="small"
              @click="openGrade(row)"
              :disabled="row.status !== 'completed'"
            >
              <el-icon><EditPen /></el-icon>
              {{ row.manualScore != null ? '查看' : '批阅' }}
            </el-button>
          </div>
        </div>
      </transition-group>

      <div v-if="data.records.length === 0" class="gc-empty">
        <div class="gc-empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
            <path d="M9 12l2 2 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="#cbd5e1" stroke-width="1.5"/>
          </svg>
        </div>
        <p>暂无答卷记录</p>
      </div>
    </div>

    <!-- 批阅弹窗 -->
    <el-dialog v-model="data.gradeDialog" title="批阅试卷" width="880px" class="grade-dialog" destroy-on-close top="3vh">
      <div class="grade-dialog-body">
        <div class="grade-left">
          <!-- 玩家信息条 -->
          <div class="grade-student-bar">
            <div class="gsb-avatar" :style="{ background: avatarGradient(data.currentRecord) }">
              {{ (data.currentRecord.studentName || '?')[0] }}
            </div>
            <div class="gsb-info">
              <div class="gsb-name">{{ data.currentRecord.studentName }}</div>
              <div class="gsb-no">编号：{{ data.currentRecord.studentNo }}</div>
            </div>
            <div class="gsb-auto">
              <span class="gsb-label">自动评分</span>
              <span class="gsb-val">{{ data.currentRecord.autoScore || 0 }}</span>
            </div>
          </div>

          <!-- 题目列表 -->
          <div v-for="(q, idx) in data.questions" :key="q.id" class="grade-q-card">
            <div class="gqc-header">
              <div class="gqc-index">{{ idx + 1 }}</div>
              <el-tag size="small">{{ typeLabel(q.type) }}</el-tag>
              <span class="gqc-score">{{ q.score }}分</span>
            </div>
            <div class="gqc-content">{{ q.content }}</div>

            <!-- 选择题选项 -->
            <div v-if="q.type === 'single' || q.type === 'multiple'" class="gqc-options">
              <div v-for="opt in parseOptions(q)" :key="opt.key"
                class="gqc-opt"
                :class="{ correct: isCorrectOpt(q, opt.key), wrong: isWrongOpt(q, opt.key) }">
                <span class="gqco-key">{{ opt.key }}.</span>
                <span>{{ opt.value }}</span>
              </div>
            </div>

            <!-- 答案对照 -->
            <div class="gqc-answer-row">
              <div class="gqar-row">
                <span class="gqar-label">玩家答案</span>
                <span class="gqar-val student">{{ getAnswer(q) || '未作答' }}</span>
              </div>
              <div class="gqar-row" v-if="q.answer">
                <span class="gqar-label">正确答案</span>
                <span class="gqar-val correct">{{ q.answer }}</span>
              </div>
            </div>

            <!-- 主观题评分 -->
            <div class="gqc-grade-row" v-if="q.type === 'essay' || q.type === 'fillin'">
              <el-input-number v-model="data.gradeScores[q.id]" :min="0" :max="q.score" :precision="1" size="default" style="width: 120px" />
              <span class="gqgr-sep">分</span>
              <el-input v-model="data.gradeCommentMap[q.id]" placeholder="评语（可选）" size="default" style="flex:1" />
            </div>
          </div>
        </div>

        <div class="grade-right">
          <div class="grade-summary-card">
            <div class="gsc-title">评分汇总</div>
            <div class="gsc-row">
              <span>自动评分</span>
              <span class="gsc-val">{{ data.currentRecord.autoScore || 0 }}</span>
            </div>
            <div class="gsc-row">
              <span>人工评分</span>
              <span class="gsc-val">{{ manualTotal }}</span>
            </div>
            <div class="gsc-divider"></div>
            <div class="gsc-row total">
              <span>总分</span>
              <span class="gsc-val total">{{ (parseFloat(data.currentRecord.autoScore || 0) + manualTotal).toFixed(1) }}</span>
            </div>
          </div>

          <div class="grade-action-panel">
            <div class="gap-comment">
              <el-input-number v-model="data.performanceScore" :min="0" :max="100" :step="5" placeholder="表现评分" style="width: 100%; margin-bottom: 8px" />
              <el-radio-group v-model="data.advisoryVote" style="margin-bottom: 8px">
                <el-radio value="PASS">建议通过</el-radio>
                <el-radio value="FAIL">建议不通过</el-radio>
                <el-radio value="ABSTAIN">弃权</el-radio>
              </el-radio-group>
              <el-checkbox-group v-if="data.advisoryVote === 'FAIL'" v-model="data.rejectionReasons" style="margin-bottom: 8px">
                <el-checkbox label="规则理解不足" />
                <el-checkbox label="沟通态度不合适" />
                <el-checkbox label="名声或历史记录不佳" />
                <el-checkbox label="不适合当前服务器氛围" />
              </el-checkbox-group>
              <el-input v-if="data.advisoryVote === 'FAIL'" v-model="data.customReason" placeholder="其他原因（可选）" style="margin-bottom: 8px" />
              <el-input v-model="data.gradeComment" type="textarea" :rows="3" placeholder="总评语（可选）" />
            </div>
            <div class="gap-btns">
              <el-button type="primary" size="large" @click="submitGrade" class="gap-submit">
                <el-icon><Check /></el-icon> 提交批阅与表决
              </el-button>
            </div>
            <div class="gap-hint">
              <el-icon><Key /></el-icon> Ctrl+Enter 快捷提交
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { Search, Document, Clock, Timer, EditPen, Check, CircleCheck, Key } from '@element-plus/icons-vue'

const route = useRoute()
const gradients = [
  'linear-gradient(135deg, #0284c7, #38bdf8)',
  'linear-gradient(135deg, #7c3aed, #a78bfa)',
  'linear-gradient(135deg, #16a34a, #4ade80)',
  'linear-gradient(135deg, #d97706, #fbbf24)',
  'linear-gradient(135deg, #dc2626, #f87171)',
  'linear-gradient(135deg, #0891b2, #06b6d4)',
]

const data = reactive({
  examList: [],
  selectedExamId: null,
  filterStatus: 'completed',
  records: [],
  progress: null,
  loading: false,
  gradeDialog: false,
  currentRecord: {},
  questions: [],
  answers: {},
  answerIds: {},
  gradeScores: {},
  gradeComment: '',
  performanceScore: 80,
  advisoryVote: 'PASS',
  rejectionReasons: [],
  customReason: '',
  gradeCommentMap: {},
})

const typeLabel = (t) => ({ single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '简答' }[t] || t)
const examStatusLabel = (s) => ({ PENDING: '待审阅', UNDER_REVIEW: '审阅中', PASSED: '通过', FAILED: '未通过' }[s] || s)
const examStatusType = (s) => ({ PENDING: 'info', UNDER_REVIEW: 'warning', PASSED: 'success', FAILED: 'danger' }[s] || 'info')

const formatTime = (t) => t ? String(t).replace('T', ' ').substring(0, 19) : '—'

const avatarGradient = (row) => {
  const idx = (row.studentName || '').charCodeAt(0) % gradients.length
  return gradients[idx]
}

const parseOptions = (q) => {
  const opts = []
  if (q.optionA) opts.push({ key: 'A', value: q.optionA })
  if (q.optionB) opts.push({ key: 'B', value: q.optionB })
  if (q.optionC) opts.push({ key: 'C', value: q.optionC })
  if (q.optionD) opts.push({ key: 'D', value: q.optionD })
  return opts
}

const getAnswer = (q) => {
  const a = data.answers[q.id]
  if (Array.isArray(a)) return a.join(', ')
  return a || ''
}

const isCorrectOpt = (q, key) => q.answer && q.answer.includes(key)
const isWrongOpt = (q, key) => {
  const a = data.answers[q.id]
  if (!a) return false
  return (Array.isArray(a) ? a : [a]).includes(key) && !q.answer?.includes(key)
}

const gradingPercent = computed(() => {
  if (!data.progress?.total) return 0
  return Math.round((data.progress.graded / data.progress.total) * 100)
})

const manualTotal = computed(() => {
  return Object.values(data.gradeScores).reduce((sum, v) => sum + (v || 0), 0).toFixed(1)
})

const loadExams = () => {
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') {
      data.examList = (res.data || []).filter(e => ['ongoing', 'finished', 'published'].includes(e.status))
      if (route.query.examId) {
        data.selectedExamId = parseInt(route.query.examId)
        loadRecords()
      }
    }
  })
}

const loadRecords = () => {
  if (!data.selectedExamId) { data.records = []; return }
  data.loading = true
  request.get('/examRecord/selectAll', {
    params: { examId: data.selectedExamId, status: data.filterStatus || 'completed' }
  }).then(res => {
    if (res.code === '200') data.records = res.data || []
  }).finally(() => { data.loading = false })

  request.get('/examApproval/getGradingProgress/' + data.selectedExamId).then(res => {
    if (res.code === '200') data.progress = res.data
  }).catch(() => {})
}

const openGrade = (record) => {
  data.currentRecord = { ...record }
  data.gradeScores = {}
  data.performanceScore = 80
  data.advisoryVote = 'PASS'
  data.rejectionReasons = []
  data.customReason = ''
  data.gradeCommentMap = {}
  data.questions = []
  data.answerIds = {}

  request.get('/examRecord/detail/' + record.id).then(res => {
    if (res.code === '200' && res.data?.answers) {
      res.data.answers.forEach(a => { data.answers[a.questionId] = a.studentAnswer; data.answerIds[a.questionId] = a.id })
    }
  })

  request.get('/question/selectByPaperId/' + record.paperId).then(res => {
    if (res.code === '200') {
      data.questions = res.data || []
      data.questions.forEach(q => { data.gradeScores[q.id] = 0 })
    }
  })

  data.gradeDialog = true
}

const submitGrade = () => {
  const answers = data.questions
    .filter(q => q.type === 'essay' || q.type === 'fillin' || q.type === 'fill')
    .map(q => ({
      id: data.answerIds[q.id],
      score: data.gradeScores[q.id] || 0,
      comment: data.gradeCommentMap[q.id]
    }))

  request.post('/grading/batchGrade?recordId=' + data.currentRecord.id, {
    answers,
    performanceScore: data.performanceScore,
    advisoryVote: data.advisoryVote,
    rejectionReasons: data.rejectionReasons.join(','),
    customReason: data.customReason,
    comment: data.gradeComment
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('批阅与表决已提交')
      data.gradeDialog = false
      loadRecords()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const onKeyDown = (e) => {
  if (data.gradeDialog && e.ctrlKey && e.key === 'Enter') submitGrade()
}

onMounted(() => { loadExams(); window.addEventListener('keydown', onKeyDown) })
onUnmounted(() => { window.removeEventListener('keydown', onKeyDown) })
</script>

<style scoped>
.gc { max-width: 1100px; margin: 0 auto; }

/* ===== 工具栏 ===== */
.gc-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  background: var(--bg-card); border-radius: 14px; padding: 16px 20px;
  margin-bottom: 14px; border: 1px solid var(--border-lighter);
  gap: 16px; flex-wrap: wrap;
}
.gct-left { display: flex; align-items: center; gap: 10px; }
.gct-right { display: flex; align-items: center; gap: 12px; }
.gct-prog-item { display: flex; flex-direction: column; align-items: center; }
.gct-pi-label { font-size: 10px; color: var(--text-secondary); margin-bottom: 2px; }
.gct-pi-val { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.gct-pi-val.ok { color: var(--success-color, #00b42a); }
.gct-pi-val.warn { color: var(--danger-color, #ef4444); }
.gct-prog-sep { width: 1px; height: 28px; background: var(--border-lighter); }
.gct-prog-pct { font-size: 20px; font-weight: 800; color: var(--primary-color); margin-left: 8px; }

/* ===== 进度条 ===== */
.gc-progress-card { margin-bottom: 16px; }
.gcp-track { height: 6px; background: var(--border-lighter); border-radius: 3px; overflow: hidden; }
.gcp-fill { height: 100%; background: var(--gradient-primary); border-radius: 3px; transition: width 0.6s ease; min-width: 2px; }

/* ===== 空状态 ===== */
.gc-empty { text-align: center; padding: 60px 24px; background: var(--bg-card); border-radius: 14px; border: 1px solid var(--border-lighter); }
.gc-empty-icon { margin-bottom: 16px; display: flex; justify-content: center; }
.gc-empty p { font-size: 15px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; }
.gc-empty span { font-size: 13px; color: var(--text-secondary); }

/* ===== 答卷卡片 ===== */
.gc-list-inner { display: flex; flex-direction: column; gap: 10px; }
.gc-record-card {
  display: flex; align-items: center; gap: 16px;
  background: var(--bg-card); border-radius: 14px; padding: 16px 20px;
  border: 1px solid var(--border-lighter); transition: all 0.2s;
}
.gc-record-card:hover { border-color: var(--primary-color); box-shadow: 0 4px 16px rgba(0,0,0,0.06); }

.grc-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.grc-body { flex: 1; min-width: 0; }
.grc-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; flex-wrap: wrap; }
.grc-name { font-size: 15px; font-weight: 700; color: var(--text-primary); }
.grc-no { font-size: 12px; color: var(--text-secondary); }
.grc-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.grcm-chip { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--text-secondary); }
.grcm-chip .el-icon { font-size: 12px; }
.grc-scores { display: flex; align-items: center; gap: 6px; }
.grcs-chip { display: flex; align-items: center; gap: 5px; padding: 3px 10px; border-radius: 8px; font-size: 13px; }
.grcs-chip { background: #dbeafe; color: #2563eb; }
.grcs-chip.ungraded { background: #fee2e2; color: #ef4444; }
.grcs-chip.total { background: rgba(var(--primary-rgb), 0.1); color: var(--primary-color); }
.grcsl { font-size: 11px; opacity: 0.8; }
.grcsv { font-weight: 700; }
.grcsv.auto { color: inherit; }
.grcs-op { color: var(--border-light); font-size: 12px; font-weight: 300; }
.grc-action { flex-shrink: 0; }

/* ===== 批阅弹窗 ===== */
.grade-dialog-body { display: flex; gap: 20px; max-height: 72vh; }
.grade-left { flex: 1; overflow-y: auto; padding-right: 8px; }

/* 玩家信息条 */
.grade-student-bar {
  display: flex; align-items: center; gap: 14px;
  background: var(--bg-page); border-radius: 12px; padding: 14px 16px; margin-bottom: 16px;
  border: 1px solid var(--border-lighter);
}
.gsb-avatar { width: 40px; height: 40px; border-radius: 50%; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.gsb-info { flex: 1; }
.gsb-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.gsb-no { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.gsb-auto { text-align: center; }
.gsb-label { display: block; font-size: 11px; color: var(--text-secondary); }
.gsb-val { display: block; font-size: 20px; font-weight: 700; color: var(--info-color, #409eff); }

/* 题目卡片 */
.grade-q-card {
  background: var(--bg-page); border-radius: 12px; padding: 16px;
  margin-bottom: 12px; border: 1px solid var(--border-lighter);
}
.gqc-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.gqc-index {
  width: 22px; height: 22px; border-radius: 50%;
  background: var(--gradient-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; flex-shrink: 0;
}
.gqc-score { margin-left: auto; font-size: 13px; color: var(--text-secondary); font-weight: 600; }
.gqc-content { font-size: 14px; color: var(--text-primary); line-height: 1.6; margin-bottom: 10px; }
.gqc-options { margin-bottom: 10px; }
.gqc-opt { padding: 6px 12px; border-radius: 6px; margin-bottom: 4px; font-size: 13px; background: var(--bg-card); border: 1px solid var(--border-lighter); }
.gqc-opt.correct { background: rgba(22,163,74,0.1); color: var(--success-color, #16a34a); border-color: rgba(22,163,74,0.2); }
.gqc-opt.wrong { background: rgba(220,38,38,0.08); color: var(--danger-color, #dc2626); border-color: rgba(220,38,38,0.2); }
.gqco-key { font-weight: 700; margin-right: 6px; }
.gqc-answer-row { display: flex; flex-direction: column; gap: 4px; }
.gqar-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.gqar-label { color: var(--text-secondary); white-space: nowrap; width: 72px; }
.gqar-val { color: var(--text-primary); font-weight: 500; }
.gqar-val.student { color: var(--info-color, #2563eb); }
.gqar-val.correct { color: var(--success-color, #16a34a); font-weight: 700; }
.gqc-grade-row { display: flex; align-items: center; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px dashed var(--border-lighter); }
.gqgr-sep { color: var(--text-secondary); }

/* 右侧汇总 */
.grade-right { width: 280px; flex-shrink: 0; }
.grade-summary-card { background: var(--bg-page); border-radius: 12px; padding: 16px; margin-bottom: 16px; border: 1px solid var(--border-lighter); }
.gsc-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }
.gsc-row { display: flex; justify-content: space-between; align-items: center; padding: 5px 0; font-size: 14px; color: var(--text-secondary); }
.gsc-val { font-weight: 700; font-size: 15px; color: var(--text-primary); }
.gsc-row.total .gsc-val { font-size: 22px; color: var(--primary-color); }
.gsc-divider { height: 1px; background: var(--border-lighter); margin: 8px 0; }

.grade-action-panel { display: flex; flex-direction: column; gap: 10px; }
.gap-btns { display: flex; gap: 8px; }
.gap-submit, .gap-pass { flex: 1; border-radius: 10px; font-weight: 600; }
.gap-hint { text-align: center; font-size: 12px; color: var(--text-secondary); display: flex; align-items: center; justify-content: center; gap: 4px; }

/* 动画 */
.record-anim-enter-active, .record-anim-leave-active { transition: all 0.3s; }
.record-anim-enter-from { opacity: 0; transform: translateY(10px); }
.record-anim-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .grade-dialog-body { flex-direction: column; }
  .grade-right { width: 100%; }
  .gct-right { display: none; }
}
</style>
