<template>
  <div class="main-content">
    <div class="breadcrumb-nav">
      <a @click="router.push('/front/home')">首页</a>
      <span>/</span>
      <span class="current">审核模拟</span>
    </div>

    <!-- 选择练习参数 -->
    <template v-if="!data.practiceStarted">
      <div class="practice-setup-card">
        <div class="practice-scene" :style="getSceneStyle('practice')"></div>
        <div class="setup-header">
          <h2>审核模拟</h2>
          <p>按入服审核方向随机生成模拟题，提前熟悉建筑、后期、红石、见习审核要求</p>
        </div>

        <div class="setup-form">
          <div class="form-group">
            <label>审核方向</label>
            <el-tree-select
              v-model="data.categoryId"
              :data="categoryTree"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="全部分类"
              clearable
              check-strictly
              style="width: 100%;"
            />
          </div>

          <div class="form-group">
            <label>难度级别</label>
            <div class="difficulty-tags">
              <span class="diff-tag" :class="{ active: data.difficulty === '' }" @click="data.difficulty = ''">全部</span>
              <span class="diff-tag easy" :class="{ active: data.difficulty === 'easy' }" @click="data.difficulty = 'easy'">简单</span>
              <span class="diff-tag medium" :class="{ active: data.difficulty === 'medium' }" @click="data.difficulty = 'medium'">中等</span>
              <span class="diff-tag hard" :class="{ active: data.difficulty === 'hard' }" @click="data.difficulty = 'hard'">困难</span>
            </div>
          </div>

          <div class="form-group">
            <label>题目数量</label>
            <div class="count-selector">
              <span v-for="n in [5, 10, 15, 20, 30]" :key="n"
                    class="count-tag" :class="{ active: data.questionCount === n }"
                    @click="data.questionCount = n">{{ n }}题</span>
            </div>
          </div>

          <div class="form-group">
            <label>模拟名称（可选）</label>
            <el-input v-model="data.practiceName" placeholder="例如：红石审核模拟 / 建筑审核自测" clearable />
          </div>

          <el-button type="primary" size="large" round @click="startPractice"
                     :loading="data.generating" :disabled="data.generating"
                     style="width: 100%; margin-top: 8px;">
            <el-icon><EditPen /></el-icon> 开始模拟
          </el-button>
        </div>
      </div>

      <!-- 练习统计卡片 -->
      <div class="stats-row">
        <div class="stat-item" style="--accent: var(--primary-color);">
          <div class="stat-value">{{ data.totalPractices }}</div>
          <div class="stat-label">累计模拟</div>
        </div>
        <div class="stat-item" style="--accent: var(--success-color);">
          <div class="stat-value">{{ data.avgScore }}</div>
          <div class="stat-label">平均得分</div>
        </div>
        <div class="stat-item" style="--accent: var(--warning-color);">
          <div class="stat-value">{{ data.wrongCount }}</div>
          <div class="stat-label">错题数</div>
        </div>
        <div class="stat-item" style="--accent: var(--primary-color);">
          <div class="stat-value">{{ data.totalQuestions }}</div>
          <div class="stat-label">题目总数</div>
        </div>
      </div>
    </template>

    <!-- 答题界面 -->
    <template v-else-if="data.practiceStarted && !data.practiceFinished">

      <!-- 复习确认对话框 -->
      <el-dialog
        v-model="data.reviewVisible"
        title="📋 提交前检查"
        width="60%"
        :close-on-click-modal="false"
      >
        <div>
          <div style="margin-bottom: 16px; font-size: 15px; color: var(--text-secondary);">
            请确认你的作答，以下
            <strong style="color: var(--primary-color);">{{ data.unansweredCount }}</strong>
            道题尚未作答：
          </div>
          <div v-if="data.unansweredCount > 0" class="unanswered-list">
            <div v-for="q in data.unansweredQuestions" :key="q.id" class="unanswered-item"
                 @click="jumpToQuestion(q.idx)">
              <span class="un-q-badge">{{ q.idx + 1 }}</span>
              <span class="un-q-content">{{ q.content.substring(0, 40) }}{{ q.content.length > 40 ? '...' : '' }}</span>
              <el-icon color="#f56c6c" :size="14"><Warning /></el-icon>
            </div>
          </div>
          <div v-if="data.unansweredCount === 0" style="text-align: center; padding: 24px; color: var(--success-color);">
            <el-icon :size="40"><CircleCheck /></el-icon>
            <div style="margin-top: 8px; font-size: 16px;">太棒了！所有题目都已作答 ✓</div>
          </div>
        </div>
        <template #footer>
          <el-button @click="data.reviewVisible = false">继续答题</el-button>
          <el-button type="primary" @click="submitPractice">
            确认提交 <el-icon><Check /></el-icon>
          </el-button>
        </template>
      </el-dialog>

      <div class="practice-header">
        <div class="practice-info">
          <h3>{{ data.practiceName }}</h3>
          <span class="practice-meta">
            共 {{ data.questions.length }} 题 · 每题 10 分 · 总分 {{ data.questions.length * 10 }}
          </span>
        </div>
        <div class="practice-timer">
          <el-icon><Clock /></el-icon>
          <span>{{ timerDisplay }}</span>
        </div>
      </div>

      <!-- 进度指示 -->
      <div class="progress-bar-wrap">
        <div class="progress-label">
          <span>答题进度</span>
          <span>{{ data.answeredCount }} / {{ data.questions.length }}</span>
        </div>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>

      <!-- 答题卡 -->
      <div class="answer-sheet-row">
        <div v-for="(q, idx) in data.questions" :key="q.id"
             class="sheet-dot"
             :class="{
               answered: data.answers[q.id],
               current: idx === data.currentIdx,
               marked: data.markedQuestions.has(q.id)
             }"
             @click="data.currentIdx = idx"
             :title="'第' + (idx + 1) + '题' + (data.answers[q.id] ? ' (已答)' : ' (未答)')">
          {{ idx + 1 }}
        </div>
        <span style="margin-left: 8px; font-size: 12px; color: var(--text-tertiary); align-self: center;">
          绿色=已答 · 蓝色=当前
        </span>
      </div>

      <!-- 题目展示区 -->
      <div class="question-display" v-if="currentQuestion">
        <div class="question-header">
          <span class="q-number">第 {{ data.currentIdx + 1 }} 题</span>
          <el-tag :type="getTypeTag(currentQuestion.type)" size="small">{{ getTypeLabel(currentQuestion.type) }}</el-tag>
          <el-tag v-if="currentQuestion.difficulty === 'easy'" type="success" size="small" effect="light">简单</el-tag>
          <el-tag v-else-if="currentQuestion.difficulty === 'medium'" type="warning" size="small" effect="light">中等</el-tag>
          <el-tag v-else-if="currentQuestion.difficulty === 'hard'" type="danger" size="small" effect="light">困难</el-tag>
          <span class="q-score">(10分)</span>
          <span
            class="mark-btn"
            :class="{ marked: data.markedQuestions.has(currentQuestion.id) }"
            @click="toggleMark(currentQuestion.id)"
            title="标记此题"
          >
            <el-icon><Flag /></el-icon>
          </span>
        </div>

        <div class="question-content">{{ currentQuestion.content }}</div>

        <div v-if="currentQuestionVisuals.length" class="visual-reference-strip">
          <div
            v-for="visual in currentQuestionVisuals"
            :key="visual.key"
            class="visual-reference"
            :style="visualBackgroundStyle(visual)"
          >
            <div>
              <strong>{{ visual.title }}</strong>
              <span>{{ visual.desc }}</span>
              <a v-if="visual.source" :href="visual.source" target="_blank" rel="noopener noreferrer">{{ visual.sourceName }}</a>
            </div>
          </div>
        </div>

        <!-- 图片展示（使用 Element Plus el-image 预览） -->
        <div v-if="currentQuestion.images?.length" class="question-images">
          <el-image
            v-for="(img, i) in currentQuestion.images" :key="i"
            :src="img.url"
            :preview-src-list="currentQuestion.images.map(im => im.url)"
            fit="cover"
            class="q-img"
            :initial-index="i"
          />
        </div>

        <!-- 选项 -->
        <div v-if="['single', 'multiple'].includes(currentQuestion.type)" class="options-area">
          <div v-for="(text, key) in currentQuestion.options" :key="key"
               class="option-item"
               :class="{ selected: isOptionSelected(key) }"
               @click="selectOption(key)">
            <div class="option-letter">{{ key }}</div>
            <div class="option-text">{{ text }}</div>
          </div>
        </div>

        <!-- 判断题 -->
        <div v-else-if="currentQuestion.type === 'judge'" class="options-area">
          <div class="option-item" :class="{ selected: data.answers[currentQuestion.id] === '对' }"
               @click="data.answers[currentQuestion.id] = '对'">
            <div class="option-letter">✓</div>
            <div class="option-text">正确</div>
          </div>
          <div class="option-item" :class="{ selected: data.answers[currentQuestion.id] === '错' }"
               @click="data.answers[currentQuestion.id] = '错'">
            <div class="option-letter">✗</div>
            <div class="option-text">错误</div>
          </div>
        </div>

        <!-- 填空题 -->
        <div v-else-if="currentQuestion.type === 'fillin'" class="fillin-area">
          <div v-for="(_, i) in fillCount" :key="i" class="fillin-row">
            <span class="fillin-label">空{{ i + 1 }}:</span>
            <el-input v-model="data.fillinValues[i]" placeholder="请输入答案"
                      @change="updateFillinAnswer" style="flex: 1;" />
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="question-nav">
          <el-button @click="prevQuestion" :disabled="data.currentIdx === 0">
            <el-icon><ArrowLeft /></el-icon> 上一题
          </el-button>
          <div class="nav-center">
            <span class="nav-progress">{{ data.currentIdx + 1 }} / {{ data.questions.length }}</span>
            <el-button size="small" text type="primary" @click="showReview">
              <el-icon><List /></el-icon> 答题卡
            </el-button>
          </div>
          <el-button v-if="data.currentIdx < data.questions.length - 1" type="primary" @click="nextQuestion">
            下一题 <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button v-else type="success" @click="showReview">
            检查并提交 <el-icon><Check /></el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <!-- 练习结果 -->
    <template v-else>
      <div class="result-banner" :class="data.record?.isPass ? 'pass' : 'fail'">
        <div class="result-icon-wrap">
          <el-icon :size="56" class="result-icon">
            <CircleCheck v-if="data.record?.isPass" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <h2>{{ data.record?.isPass ? '练习完成！' : '继续加油！' }}</h2>
        <p>{{ data.record?.totalScore || 0 }} / {{ data.questions.length * 10 }} 分</p>
        <!-- 结果分段 -->
        <div class="score-segment">
          <div class="seg-item">
            <span class="seg-val correct-color">{{ correctCount }}</span>
            <span class="seg-lbl">答对</span>
          </div>
          <div class="seg-divider"></div>
          <div class="seg-item">
            <span class="seg-val wrong-color">{{ data.questions.length - correctCount }}</span>
            <span class="seg-lbl">答错</span>
          </div>
          <div class="seg-divider"></div>
          <div class="seg-item">
            <span class="seg-val time-color">{{ timerDisplay }}</span>
            <span class="seg-lbl">用时</span>
          </div>
        </div>
      </div>

      <div class="result-cards">
        <div class="score-mini-card" style="--accent: var(--primary-color);">
          <div class="smc-value">{{ data.record?.totalScore || 0 }}</div>
          <div class="smc-label">得分</div>
        </div>
        <div class="score-mini-card" style="--accent: var(--success-color);">
          <div class="smc-value" style="color: var(--success-color);">{{ correctCount }}</div>
          <div class="smc-label">答对</div>
        </div>
        <div class="score-mini-card" style="--accent: var(--danger-color);">
          <div class="smc-value" style="color: var(--danger-color);">{{ data.questions.length - correctCount }}</div>
          <div class="smc-label">答错</div>
        </div>
        <div class="score-mini-card" style="--accent: var(--warning-color);">
          <div class="smc-value" style="color: var(--warning-color);">{{ timerDisplay }}</div>
          <div class="smc-label">用时</div>
        </div>
      </div>

      <!-- 能力雷达 -->
      <div v-if="data.abilityData" class="card ability-card">
        <h4 style="margin: 0 0 16px; font-size: 15px;">📊 能力分析</h4>
        <div class="ability-bars">
          <div v-for="item in data.abilityData" :key="item.type" class="ability-row">
            <span class="ability-label">{{ item.label }}</span>
            <div class="ability-bar-track">
              <div class="ability-bar-fill" :style="{ width: item.pct + '%', background: item.color }"></div>
            </div>
            <span class="ability-pct">{{ item.pct }}%</span>
          </div>
        </div>
      </div>

      <!-- 答题详情 -->
      <div class="card answer-section">
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
          <h3 style="margin: 0; font-size: 16px;">答题详情</h3>
          <el-button size="small" text type="primary" @click="toggleAllDetails">
            {{ data.showAll ? '收起' : '展开全部' }}
          </el-button>
        </div>

        <div v-for="(q, idx) in (data.showAll ? data.resultDetails : data.resultDetails.slice(0, 3))"
             :key="q.questionId"
             class="answer-detail-card"
             :class="{ correct: q.isCorrect, wrong: !q.isCorrect }">
          <div class="detail-header">
            <span class="q-badge">{{ idx + 1 }}</span>
            <el-tag :type="getTypeTag(q.type)" size="small">{{ getTypeLabel(q.type) }}</el-tag>
            <el-tag v-if="q.isCorrect" type="success" size="small" style="margin-left: 8px;">正确</el-tag>
            <el-tag v-else type="danger" size="small" style="margin-left: 8px;">错误</el-tag>
            <span style="margin-left: 12px; font-weight: bold; color: var(--text-secondary);">得分：{{ q.score }}</span>
          </div>
          <div class="detail-content">{{ q.content }}</div>

          <!-- 图片 -->
          <div v-if="q.images?.length" style="display: flex; gap: 8px; flex-wrap: wrap; margin: 8px 0;">
            <el-image
              v-for="(img, i) in q.images" :key="i"
              :src="img.url"
              :preview-src-list="q.images.map(im => im.url)"
              fit="cover"
              style="max-width: 120px; max-height: 100px; border-radius: 6px; cursor: pointer;"
              :initial-index="i"
            />
          </div>

          <!-- 选项 -->
          <div v-if="q.options" style="margin: 8px 0; display: flex; flex-direction: column; gap: 6px;">
            <div v-for="(text, key) in q.options" :key="key"
                 class="option-readonly"
                 :class="{
                   'correct-option': key.toUpperCase() === (q.correctAnswer || '').toUpperCase(),
                   'wrong-option': key.toUpperCase() === (q.studentAnswer || '').toUpperCase() && !q.isCorrect
                 }">
              <span class="opt-key">{{ key }}.</span> {{ text }}
            </div>
          </div>

          <div class="answer-row">
            <span class="answer-label">你的答案：</span>
            <span :class="q.isCorrect ? 'correct-text' : 'wrong-text'">{{ q.studentAnswer || '未作答' }}</span>
          </div>
          <div class="answer-row" v-if="!q.isCorrect && q.correctAnswer">
            <span class="answer-label">正确答案：</span>
            <span class="correct-text">{{ q.correctAnswer }}</span>
          </div>
          <div v-if="q.analysis" class="analysis-box">
            <strong>解析：</strong>{{ q.analysis }}
          </div>
          <div v-if="!q.isCorrect" class="wrong-action">
            <el-button size="small" type="warning" plain @click="addToWrong(q)" :disabled="q.addedToWrong">
              <el-icon><Star /></el-icon> {{ q.addedToWrong ? '已加入错题复盘' : '加入错题复盘' }}
            </el-button>
            <el-button size="small" type="primary" plain @click="addToFavorites(q)">
              <el-icon><Star /></el-icon> 标记题目
            </el-button>
          </div>
        </div>

        <div v-if="data.resultDetails.length > 3" style="text-align: center; margin-top: 12px;">
          <el-button v-if="!data.showAll" type="primary" text @click="data.showAll = true">
            展开全部 {{ data.resultDetails.length }} 道题 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <el-button v-else type="primary" text @click="data.showAll = false">
            收起 <el-icon><ArrowUp /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="result-footer">
        <el-button type="primary" size="large" round @click="resetPractice">
          <el-icon><RefreshRight /></el-icon> 再来一次
        </el-button>
        <el-button size="large" round @click="router.push('/front/home')">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button size="large" round type="success" @click="challengeFriend">
          <el-icon><Trophy /></el-icon> 挑战好友
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, onUnmounted, watch, ref } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { CircleCheck, CircleClose, Warning } from "@element-plus/icons-vue";
import { getQuestionVisuals, getSceneStyle, visualBackgroundStyle } from "@/data/reviewVisuals.js";

const data = reactive({
  categoryId: null,
  difficulty: '',
  questionCount: 10,
  practiceName: '',
  generating: false,
  categoryTree: [],

  totalPractices: 0,
  avgScore: 0,
  wrongCount: 0,
  totalQuestions: 0,

  practiceStarted: false,
  practiceFinished: false,
  recordId: null,
  examId: null,
  paperId: null,
  questions: [],
  answers: {},
  fillinValues: [],
  currentIdx: 0,
  markedQuestions: new Set(),

  startTime: null,
  elapsedSeconds: 0,
  timerInterval: null,

  record: null,
  resultDetails: [],
  showAll: false,

  reviewVisible: false,
  abilityData: null
})

const user = computed(() => JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}'))

const currentQuestion = computed(() => data.questions[data.currentIdx] || null)
const currentQuestionVisuals = computed(() => getQuestionVisuals(currentQuestion.value || {}, 2))

const fillCount = computed(() => {
  const q = currentQuestion.value
  if (!q || q.type !== 'fillin') return 0
  return (q.answer || '').split('|||').length
})

const correctCount = computed(() => data.resultDetails.filter(d => d.isCorrect).length)

const timerDisplay = computed(() => {
  const s = data.elapsedSeconds
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})

const answeredCount = computed(() => Object.keys(data.answers).filter(k => {
  const v = data.answers[k]
  return v !== undefined && v !== null && v !== ''
}).length)

const progressPercent = computed(() => {
  if (!data.questions.length) return 0
  return Math.round((answeredCount.value / data.questions.length) * 100)
})

const unansweredCount = computed(() => data.questions.length - answeredCount.value)

const unansweredQuestions = computed(() =>
  data.questions
    .filter((q, idx) => !data.answers[q.id])
    .map((q, idx) => {
      const originalIdx = data.questions.indexOf(q)
      return { ...q, idx: originalIdx }
    })
)

const getTypeLabel = (type) => {
  const map = { single: '单选题', multiple: '多选题', judge: '判断题', essay: '简答题', fillin: '填空题', fill: '填空题' }
  return map[type] || '未知'
}
const getTypeTag = (type) => {
  const map = { single: 'success', multiple: 'primary', judge: 'warning', fillin: 'info', essay: 'info' }
  return map[type] || 'info'
}

const loadCategories = () => {
  request.get('/questionCategory/selectTree').then(res => {
    if (res.code === '200') data.categoryTree = res.data || []
  })
}

const loadStats = () => {
  request.get('/examRecord/getByStudentId/' + user.value.id).then(res => {
    if (res.code === '200') {
      const records = res.data || []
      data.totalPractices = records.filter(r => r.examName?.startsWith('[练习]')).length
      const practiceRecords = records.filter(r => r.examName?.startsWith('[练习]') && r.totalScore)
      if (practiceRecords.length) {
        const sum = practiceRecords.reduce((acc, r) => acc + (r.totalScore || 0), 0)
        data.avgScore = Math.round(sum / practiceRecords.length)
      }
    }
  })
  request.get('/question/selectPage', { params: { pageNum: 1, pageSize: 1 } }).then(res => {
    if (res.code === '200') data.totalQuestions = res.data?.total || 0
  })
  request.get('/wrongQuestion/selectByUser', { params: { userId: user.value.id, userRole: user.value.role } }).then(res => {
    if (res.code === '200') data.wrongCount = (res.data || []).length
  })
}

const startPractice = () => {
  data.generating = true
  request.post('/practice/generate', {
    userId: user.value.id,
    userRole: user.value.role,
    categoryId: data.categoryId || null,
    difficulty: data.difficulty || null,
    questionCount: data.questionCount,
    name: data.practiceName || null
  }).then(res => {
    if (res.code === '200') {
      data.recordId = res.data.recordId
      data.examId = res.data.examId
      data.paperId = res.data.paperId
      data.practiceStarted = true
      data.practiceName = data.practiceName || '随机练习'
      data.markedQuestions = new Set()
      loadQuestions()
      startTimer()
    } else {
      ElMessage.error(res.msg || '生成练习失败')
    }
  }).finally(() => {
    data.generating = false
  })
}

const loadQuestions = () => {
  request.get('/question/selectByPaperId/' + data.paperId).then(res => {
    if (res.code === '200') {
      data.questions = res.data || []
    }
  })
}

const startTimer = () => {
  data.startTime = Date.now()
  data.elapsedSeconds = 0
  data.timerInterval = setInterval(() => {
    data.elapsedSeconds = Math.floor((Date.now() - data.startTime) / 1000)
  }, 1000)
}

const isOptionSelected = (key) => {
  const ans = data.answers[currentQuestion.value?.id]
  if (!ans) return false
  if (currentQuestion.value?.type === 'multiple') {
    return ans.includes(key)
  }
  return ans === key
}

const selectOption = (key) => {
  const qid = currentQuestion.value.id
  if (currentQuestion.value.type === 'multiple') {
    let current = data.answers[qid] || ''
    if (current.includes(key)) {
      data.answers[qid] = current.replace(key, '')
    } else {
      data.answers[qid] = current + key
    }
  } else {
    data.answers[qid] = key
  }
}

const updateFillinAnswer = () => {
  if (!currentQuestion.value) return
  data.answers[currentQuestion.value.id] = data.fillinValues.join('|||')
}

const prevQuestion = () => {
  if (data.currentIdx > 0) data.currentIdx--
  syncFillinValues()
}

const nextQuestion = () => {
  if (data.currentIdx < data.questions.length - 1) data.currentIdx++
  syncFillinValues()
}

const syncFillinValues = () => {
  const q = currentQuestion.value
  if (q?.type === 'fillin') {
    const ans = data.answers[q.id] || ''
    const parts = ans.split('|||')
    const count = (q.answer || '').split('|||').length
    data.fillinValues = new Array(count).fill('').map((_, i) => parts[i] || '')
  }
}

const toggleMark = (id) => {
  if (data.markedQuestions.has(id)) {
    data.markedQuestions.delete(id)
  } else {
    data.markedQuestions.add(id)
  }
}

const jumpToQuestion = (idx) => {
  data.reviewVisible = false
  data.currentIdx = idx
  syncFillinValues()
}

const showReview = () => {
  data.reviewVisible = true
}

const toggleAllDetails = () => {
  data.showAll = !data.showAll
}

watch(() => data.answers, () => {
  if (!data.practiceStarted || data.practiceFinished || !data.recordId) return
  const qid = currentQuestion.value?.id
  if (!qid) return
  const ans = data.answers[qid]
  if (ans !== undefined) {
    request.post('/examRecord/saveAnswer', null, {
      params: { recordId: data.recordId, questionId: qid, studentAnswer: ans }
    })
  }
}, { deep: true })

const submitPractice = () => {
  data.reviewVisible = false
  const promises = Object.entries(data.answers).map(([qid, ans]) => {
    return request.post('/examRecord/saveAnswer', null, {
      params: { recordId: data.recordId, questionId: qid, studentAnswer: ans }
    })
  })
  Promise.all(promises).then(() => {
    request.post('/practice/submit', {
      recordId: data.recordId,
      userId: user.value.id,
      userRole: user.value.role,
      saveWrong: true
    }).then(res => {
      if (res.code === '200') {
        data.practiceFinished = true
        data.record = res.data
        if (data.timerInterval) clearInterval(data.timerInterval)
        loadResultDetails()
        computeAbilityData()
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    })
  })
}

const computeAbilityData = () => {
  const typeMap = { single: '单选题', multiple: '多选题', judge: '判断题' }
  const typeColor = { single: '#67a23c', multiple: '#409eff', judge: '#e6a23c' }
  const counts = {}
  const correct = {}
  data.resultDetails.forEach(r => {
    if (typeMap[r.type]) {
      counts[r.type] = (counts[r.type] || 0) + 1
      if (r.isCorrect) correct[r.type] = (correct[r.type] || 0) + 1
    }
  })
  data.abilityData = Object.entries(counts).map(([type, total]) => ({
    type,
    label: typeMap[type] || type,
    color: typeColor[type] || '#909399',
    pct: total > 0 ? Math.round((correct[type] || 0) / total * 100) : 0
  })).filter(d => d.pct > 0)
}

const loadResultDetails = () => {
  request.get('/examRecord/detail/' + data.recordId).then(res => {
    if (res.code === '200') {
      const answers = res.data.answers || []
      data.resultDetails = answers.map(a => ({
        questionId: a.questionId,
        content: a.question?.content,
        type: a.question?.type,
        images: a.question?.images,
        options: a.question?.options,
        studentAnswer: a.question?.type === 'fillin' ? formatFillin(a.studentAnswer) : (a.studentAnswer || '未作答'),
        correctAnswer: a.question?.answer,
        isCorrect: a.isCorrect,
        score: a.score || 0,
        analysis: a.question?.analysis,
        addedToWrong: false,
      }))
    }
  })
}

const formatFillin = (ans) => {
  if (!ans) return '未作答'
  return ans.split('|||').map((a, i) => `空${i + 1}: ${a}`).join('；')
}

const addToWrong = (q) => {
  request.post('/wrongQuestion/add', {
    userId: user.value.id,
    userRole: user.value.role,
    questionId: q.questionId,
    source: 'practice',
    sourceId: data.recordId,
    wrongAnswer: q.studentAnswer
  }).then(res => {
    if (res.code === '200') {
      q.addedToWrong = true
      ElMessage.success('已加入错题复盘')
    }
  })
}

const addToFavorites = (q) => {
  request.post('/questionFavorite/add', {
    questionId: q.questionId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已标记题目')
    } else {
      ElMessage.info(res.msg || '已标记')
    }
  })
}

const challengeFriend = () => {
  router.push({ path: '/front/leaderboard' })
}

const resetPractice = () => {
  data.practiceStarted = false
  data.practiceFinished = false
  data.questions = []
  data.answers = {}
  data.fillinValues = []
  data.currentIdx = 0
  data.resultDetails = []
  data.record = null
  data.elapsedSeconds = 0
  data.markedQuestions = new Set()
  data.showAll = false
  data.reviewVisible = false
  data.abilityData = null
  data.practiceName = ''
  data.categoryId = null
  data.difficulty = ''
  data.questionCount = 10
  loadStats()
}

onMounted(() => {
  loadCategories()
  loadStats()
  const route = router.currentRoute.value
  if (route.query.categoryId) {
    data.categoryId = parseInt(route.query.categoryId)
  }
})

onUnmounted(() => {
  if (data.timerInterval) clearInterval(data.timerInterval)
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: var(--text-tertiary);
}
.breadcrumb-nav a { color: var(--primary-color); cursor: pointer; }
.breadcrumb-nav .current { color: var(--text-primary); }

.practice-setup-card {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 0 36px 36px;
  box-shadow: var(--shadow-md);
  max-width: 560px;
  margin: 0 auto 24px;
  overflow: hidden;
}
.practice-scene {
  height: 190px;
  margin: 0 -36px 28px;
  background-size: 400% 200%;
  background-position: center;
}
.setup-header { text-align: center; margin-bottom: 28px; }
.setup-header h2 { font-size: 24px; margin: 0 0 8px; color: var(--text-primary); }
.setup-header p { color: var(--text-tertiary); margin: 0; font-size: 14px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; color: var(--text-primary); margin-bottom: 8px; }

.difficulty-tags, .count-selector { display: flex; gap: 8px; flex-wrap: wrap; }
.diff-tag, .count-tag {
  padding: 6px 16px; border-radius: 20px; font-size: 13px;
  cursor: pointer; border: 1px solid var(--border-lighter);
  transition: all 0.2s; background: var(--bg-page); color: var(--text-secondary);
}
.diff-tag:hover, .count-tag:hover { border-color: var(--primary-color); color: var(--primary-color); }
.diff-tag.active, .count-tag.active { background: var(--primary-color); color: #fff; border-color: var(--primary-color); }
.diff-tag.easy.active { background: var(--success-color); border-color: var(--success-color); }
.diff-tag.medium.active { background: var(--warning-color); border-color: var(--warning-color); }
.diff-tag.hard.active { background: var(--danger-color); border-color: var(--danger-color); }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 560px;
  margin: 0 auto 24px;
}
.stat-item {
  background: var(--bg-card); border-radius: 12px; padding: 20px 16px;
  text-align: center; box-shadow: var(--shadow-sm);
}
.stat-value { font-size: 28px; font-weight: 700; color: var(--accent); }
.stat-label { font-size: 12px; color: var(--text-tertiary); margin-top: 4px; }

.practice-header {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--bg-card); border-radius: 12px; padding: 16px 24px;
  margin-bottom: 12px; box-shadow: var(--shadow-sm);
}
.practice-info h3 { margin: 0; font-size: 18px; }
.practice-meta { font-size: 13px; color: var(--text-tertiary); }
.practice-timer {
  display: flex; align-items: center; gap: 6px;
  font-size: 20px; font-weight: 600;
  font-variant-numeric: tabular-nums; color: var(--primary-color);
}

.progress-bar-wrap {
  background: var(--bg-card); border-radius: 12px; padding: 12px 20px;
  margin-bottom: 12px; box-shadow: var(--shadow-sm);
}
.progress-label {
  display: flex; justify-content: space-between;
  font-size: 12px; color: var(--text-tertiary); margin-bottom: 8px;
}
.progress-track {
  height: 6px; background: var(--bg-page); border-radius: 3px; overflow: hidden;
}
.progress-fill {
  height: 100%; background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  border-radius: 3px; transition: width 0.3s ease;
}

.answer-sheet-row {
  display: flex; gap: 6px; flex-wrap: wrap;
  padding: 12px 16px; background: var(--bg-card);
  border-radius: 12px; margin-bottom: 12px;
  box-shadow: var(--shadow-sm);
}
.sheet-dot {
  width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; cursor: pointer;
  border: 1px solid var(--border-lighter); color: var(--text-tertiary); transition: all 0.15s;
}
.sheet-dot:hover { border-color: var(--primary-color); color: var(--primary-color); }
.sheet-dot.answered { background: var(--primary-light); border-color: var(--primary-light); color: var(--primary-color); }
.sheet-dot.current { background: var(--primary-color); color: #fff; border-color: var(--primary-color); }
.sheet-dot.marked { border-color: var(--warning-color); border-style: dashed; }

.question-display {
  background: var(--bg-card); border-radius: 12px; padding: 24px;
  box-shadow: var(--shadow-sm);
}
.question-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px; flex-wrap: wrap;
}
.q-number { font-weight: 600; color: var(--text-primary); }
.q-score { color: var(--text-tertiary); font-size: 13px; }
.question-content { font-size: 16px; line-height: 1.7; margin-bottom: 20px; color: var(--text-primary); }
.visual-reference-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}
.visual-reference {
  min-height: 136px;
  overflow: hidden;
  border-radius: 10px;
  background-position: center;
  background-size: cover;
  border: 1px solid var(--border-lighter);
}
.visual-reference > div {
  min-height: 136px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 4px;
  padding: 14px;
  background: linear-gradient(180deg, rgba(9,17,28,0.08), rgba(9,17,28,0.78));
  color: #fff;
}
.visual-reference strong { font-size: 14px; }
.visual-reference span,
.visual-reference a {
  color: rgba(255,255,255,0.78);
  font-size: 12px;
  line-height: 1.5;
}
.visual-reference a { width: fit-content; text-decoration: underline; }
.question-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.q-img { max-width: 200px; max-height: 150px; border-radius: 8px; cursor: pointer; border: 1px solid var(--border-lighter); }

.options-area { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.option-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border-radius: 10px;
  border: 1.5px solid var(--border-lighter); cursor: pointer; transition: all 0.2s;
}
.option-item:hover { border-color: rgba(var(--primary-rgb), 0.3); background: rgba(var(--primary-rgb), 0.04); }
.option-item.selected { border-color: var(--primary-color); background: var(--primary-light); }
.option-letter {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; flex-shrink: 0;
  background: var(--bg-page); color: var(--text-secondary);
}
.option-item.selected .option-letter { background: var(--primary-color); color: #fff; }
.option-text { font-size: 15px; color: var(--text-primary); }

.fillin-area { margin-bottom: 20px; }
.fillin-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.fillin-label { font-size: 14px; color: var(--text-secondary); white-space: nowrap; }

.question-nav {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 20px; border-top: 1px solid var(--border-lighter);
}
.nav-progress { color: var(--text-tertiary); font-size: 14px; }
.nav-center { display: flex; align-items: center; gap: 12px; }

.mark-btn {
  margin-left: auto; cursor: pointer; padding: 4px 8px;
  border-radius: 6px; color: var(--text-tertiary); transition: all 0.2s;
  display: flex; align-items: center;
}
.mark-btn:hover { background: rgba(230, 162, 60, 0.08); color: var(--warning-color); }
.mark-btn.marked { color: var(--warning-color); }

/* Result */
.result-banner {
  text-align: center; padding: 36px 24px;
  border-radius: 12px; margin-bottom: 20px;
}
.result-banner.pass { background: linear-gradient(135deg, rgba(103,194,58,0.1), rgba(56,249,215,0.05)); }
.result-banner.fail { background: linear-gradient(135deg, rgba(245,108,108,0.1), rgba(255,107,107,0.05)); }
.result-icon { color: var(--success-color); }
.fail .result-icon { color: var(--danger-color); }
.result-banner h2 { font-size: 24px; margin: 12px 0 4px; }
.result-banner.pass h2 { color: var(--success-color); }
.result-banner.fail h2 { color: var(--danger-color); }
.result-banner p { color: var(--text-tertiary); font-size: 18px; margin: 0 0 16px; }

.score-segment {
  display: inline-flex; align-items: center; gap: 20px;
  background: rgba(255,255,255,0.6); border-radius: 20px; padding: 10px 24px;
  margin-top: 8px;
}
.seg-item { text-align: center; }
.seg-val { font-size: 22px; font-weight: 700; }
.seg-lbl { font-size: 12px; color: var(--text-tertiary); display: block; }
.correct-color { color: var(--success-color); }
.wrong-color { color: var(--danger-color); }
.time-color { color: var(--primary-color); }
.seg-divider { width: 1px; height: 32px; background: var(--border-lighter); }

.result-cards {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 12px; margin-bottom: 20px;
}
.score-mini-card {
  background: var(--bg-card); border-radius: 12px; padding: 20px;
  text-align: center; box-shadow: var(--shadow-sm);
  border-left: 3px solid var(--accent);
}
.smc-value { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.smc-label { font-size: 13px; color: var(--text-tertiary); margin-top: 6px; }

/* Ability */
.ability-card { margin-bottom: 20px; }
.ability-bars { display: flex; flex-direction: column; gap: 12px; }
.ability-row { display: flex; align-items: center; gap: 12px; }
.ability-label { font-size: 13px; color: var(--text-secondary); width: 70px; flex-shrink: 0; }
.ability-bar-track { flex: 1; height: 8px; background: var(--bg-page); border-radius: 4px; overflow: hidden; }
.ability-bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.ability-pct { font-size: 13px; font-weight: 600; color: var(--text-secondary); width: 36px; text-align: right; flex-shrink: 0; }

/* Answer detail */
.answer-section { background: var(--bg-card); border-radius: 12px; padding: 24px; box-shadow: var(--shadow-sm); }
.answer-detail-card {
  padding: 16px; margin-bottom: 12px; border-radius: 10px;
  border: 1px solid var(--border-lighter); transition: all 0.2s;
}
.answer-detail-card.correct { border-left: 3px solid var(--success-color); background: rgba(103,194,58,0.02); }
.answer-detail-card.wrong { border-left: 3px solid var(--danger-color); background: rgba(245,108,108,0.02); }
.detail-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.detail-content { font-size: 15px; line-height: 1.6; margin-bottom: 10px; }
.q-badge { background: var(--primary-color); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.answer-row { margin-bottom: 6px; font-size: 14px; }
.answer-label { color: var(--text-tertiary); }
.correct-text { color: var(--success-color); font-weight: 500; }
.wrong-text { color: var(--danger-color); font-weight: 500; }
.analysis-box {
  color: var(--text-secondary); font-size: 14px; background: var(--bg-page);
  padding: 10px; border-radius: 6px; margin-top: 8px;
}
.option-readonly {
  padding: 8px 12px; border-radius: 6px;
  font-size: 14px; color: var(--text-secondary); background: var(--bg-page);
}
.option-readonly.correct-option { background: rgba(103,194,58,0.12); color: var(--success-color); font-weight: 500; }
.option-readonly.wrong-option { background: rgba(245,108,108,0.12); color: var(--danger-color); font-weight: 500; }
.opt-key { font-weight: 600; margin-right: 4px; }
.wrong-action { margin-top: 8px; text-align: right; display: flex; gap: 8px; justify-content: flex-end; }
.result-footer { text-align: center; margin-top: 24px; display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

/* Unanswered review */
.unanswered-list { display: flex; flex-direction: column; gap: 8px; max-height: 300px; overflow-y: auto; }
.unanswered-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 14px;
  border-radius: 8px; background: var(--bg-page); cursor: pointer;
  transition: all 0.2s;
}
.unanswered-item:hover { background: var(--primary-light); }
.un-q-badge { background: var(--danger-color); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; flex-shrink: 0; }
.un-q-content { flex: 1; font-size: 14px; color: var(--text-secondary); }

@media (max-width: 768px) {
  .stats-row, .result-cards { grid-template-columns: repeat(2, 1fr); }
  .practice-setup-card { padding: 24px; }
  .score-segment { flex-wrap: wrap; }
}
</style>
