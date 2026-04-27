<template>
  <div v-loading="data.loading">
    <!-- 粒子反馈 Canvas -->
    <canvas ref="particleCanvas" class="particle-canvas" v-if="data.showParticles"></canvas>

    <!-- 顶部进度条 -->
    <div class="top-progress-bar">
      <div class="progress-info-left">
        <h2 class="exam-title">{{ data.exam?.name }}</h2>
        <p class="exam-sub">试卷：{{ data.paper?.name }} | 总分：{{ data.paper?.totalScore }}分 | 及格分：{{ data.paper?.passScore }}分</p>
      </div>
      <div class="progress-center">
        <!-- 环形进度 -->
        <div class="progress-ring-wrap">
          <svg class="progress-ring-svg" viewBox="0 0 44 44">
            <circle class="ring-bg" cx="22" cy="22" r="18" />
            <circle class="ring-fill" cx="22" cy="22" r="18"
              :stroke-dasharray="113.1"
              :stroke-dashoffset="113.1 - (113.1 * data.progressPercent / 100)"
            />
          </svg>
          <span class="ring-text">{{ data.progressPercent }}%</span>
        </div>
        <div class="progress-label">答题进度</div>
      </div>
      <div class="progress-info-right">
        <!-- 快捷键提示 -->
        <div class="shortcut-hints">
          <span class="shortcut-item"><kbd>J</kbd> 下一题</span>
          <span class="shortcut-item"><kbd>K</kbd> 上一题</span>
        </div>
        <div class="timer-wrap" :class="{ urgent: data.remainingTime < 300 }" @click="data.timerClicks++">
          <el-icon :size="20"><Timer /></el-icon>
          <span class="timer-text">{{ formatTime(data.remainingTime) }}</span>
        </div>
        <p class="timer-label">剩余时间</p>
      </div>
    </div>

    <div style="display: flex; gap: 15px">
      <!-- 答题区域 -->
      <div class="card" style="flex: 1">
        <!-- 题目导航 -->
        <div class="question-nav">
          <el-button size="small" @click="prevQuestion" :disabled="data.currentIndex <= 0">
            <el-icon><ArrowLeft /></el-icon> 上一题
          </el-button>
          <span class="nav-indicator">第 <strong>{{ data.currentIndex + 1 }}</strong> / {{ data.questions.length }} 题</span>
          <el-button size="small" @click="nextQuestion" :disabled="data.currentIndex >= data.questions.length - 1">
            下一题 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div v-for="(question, index) in data.questions" :key="question.id"
          :ref="el => { if (el) questionRefs[index] = el }"
          style="margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee"
          v-show="index === data.currentIndex"
        >
          <div style="margin-bottom: 10px">
            <span style="background: #409eff; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 8px">
              {{ index + 1 }}
            </span>
            <el-tag v-if="question.type === 'single'" type="success" size="small" style="margin-right: 8px">单选题</el-tag>
            <el-tag v-else-if="question.type === 'multiple'" type="primary" size="small" style="margin-right: 8px">多选题</el-tag>
            <el-tag v-else-if="question.type === 'judge'" type="warning" size="small" style="margin-right: 8px">判断题</el-tag>
            <el-tag v-else-if="question.type === 'fillin'" type="info" size="small" style="margin-right: 8px">填空题</el-tag>
            <el-tag v-else type="info" size="small" style="margin-right: 8px">简答题</el-tag>
            <span style="color: #666">({{ question.score }}分)</span>
          </div>
          <div style="font-size: 16px; margin-bottom: 15px; line-height: 1.6">{{ question.content }}</div>

          <!-- 单选题 -->
          <el-radio-group v-if="question.type === 'single'" v-model="data.answers[question.id]" @change="handleAnswer(question.id, 'single')">
            <div v-for="(option, key) in question.options" :key="key" class="option-item" :class="{ selected: data.answers[question.id] === key }">
              <el-radio :label="key" class="option-radio">{{ key }}. {{ option }}</el-radio>
            </div>
          </el-radio-group>

          <!-- 多选题 -->
          <el-checkbox-group v-else-if="question.type === 'multiple'" v-model="data.multipleAnswers[question.id]" @change="handleAnswer(question.id, 'multiple')">
            <div v-for="(option, key) in question.options" :key="key" class="option-item" :class="{ selected: data.multipleAnswers[question.id]?.includes(key) }">
              <el-checkbox :label="key" class="option-checkbox">{{ key }}. {{ option }}</el-checkbox>
            </div>
          </el-checkbox-group>

          <!-- 判断题 -->
          <el-radio-group v-else-if="question.type === 'judge'" v-model="data.answers[question.id]" @change="handleAnswer(question.id, 'single')">
            <div class="option-item" :class="{ selected: data.answers[question.id] === 'true' }">
              <el-radio label="true" class="option-radio">正确</el-radio>
            </div>
            <div class="option-item" :class="{ selected: data.answers[question.id] === 'false' }">
              <el-radio label="false" class="option-radio">错误</el-radio>
            </div>
          </el-radio-group>

          <!-- 填空题 -->
          <template v-else-if="question.type === 'fillin'">
            <div v-for="(_, bIdx) in getFillinBlanks(question)" :key="bIdx" style="display: flex; align-items: center; margin-bottom: 10px">
              <span style="color: #409eff; font-weight: bold; margin-right: 8px">空{{ bIdx + 1 }}:</span>
              <el-input
                v-model="data.fillinAnswers[question.id][bIdx]"
                placeholder="请输入答案"
                style="max-width: 300px"
                @blur="handleAnswer(question.id, 'fillin')"
              />
            </div>
          </template>

          <!-- 简答题 -->
          <el-input
            v-else
            v-model="data.answers[question.id]"
            type="textarea"
            :rows="4"
            placeholder="请输入答案"
            @blur="handleAnswer(question.id, 'single')"
          />
        </div>

        <!-- 底部导航 -->
        <div class="question-nav bottom-nav">
          <el-button size="small" @click="prevQuestion" :disabled="data.currentIndex <= 0">
            <el-icon><ArrowLeft /></el-icon> 上一题
          </el-button>
          <el-button type="primary" @click="submitExam" style="margin-left: auto">
            <el-icon><Check /></el-icon> 提交试卷
          </el-button>
          <el-button size="small" @click="nextQuestion" :disabled="data.currentIndex >= data.questions.length - 1">
            下一题 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 答题卡 -->
      <div class="card answer-sheet-card">
        <h3 style="margin-bottom: 15px">答题卡</h3>
        <div class="answer-sheet-grid">
          <div
            v-for="(question, index) in data.questions"
            :key="question.id"
            :class="['answer-card-item', getAnswerClass(question.id), { current: index === data.currentIndex }]"
            @click="jumpToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <div style="margin-top: 16px; padding-top: 14px; border-top: 1px solid #eee">
          <div style="display: flex; align-items: center; margin-bottom: 8px">
            <div class="answer-card-item" style="background: #e4e7ed; width: 20px; height: 20px"></div>
            <span style="margin-left: 8px; font-size: 12px">未答</span>
          </div>
          <div style="display: flex; align-items: center; margin-bottom: 8px">
            <div class="answer-card-item" style="background: #67c23a; width: 20px; height: 20px"></div>
            <span style="margin-left: 8px; font-size: 12px">已答</span>
          </div>
          <div style="display: flex; align-items: center">
            <div class="answer-card-item" style="background: #409eff; width: 20px; height: 20px; box-shadow: 0 0 0 2px rgba(64,158,255,0.3)"></div>
            <span style="margin-left: 8px; font-size: 12px">当前</span>
          </div>
        </div>

        <!-- 统计 -->
        <div class="answer-stats">
          <div class="stat-row">
            <span class="stat-label">已答</span>
            <span class="stat-value answered">{{ data.answeredCount }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">未答</span>
            <span class="stat-value unanswered">{{ data.questions.length - data.answeredCount }}</span>
          </div>
        </div>

        <el-button type="primary" @click="handleSubmitExam" style="width: 100%; margin-top: 12px">
          <el-icon><Check /></el-icon> 提交试卷
        </el-button>
      </div>
    </div>

    <!-- 交卷确认弹窗 -->
    <el-dialog v-model="data.submitDialogVisible" title="📋 提交确认" width="420px" :close-on-click-modal="false" destroy-on-close append-to-body>
      <div v-if="data.questions.length - data.answeredCount > 0" class="submit-warning">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            还有 <strong>{{ data.questions.length - data.answeredCount }}</strong> 道题未作答，确定要提交吗？
          </template>
        </el-alert>
      </div>
      <div v-else class="submit-ready">
        <el-alert type="success" :closable="false" show-icon>
          <template #title>所有题目已作答，可以提交！</template>
        </el-alert>
      </div>
      <div class="submit-summary">
        <div class="summary-item">
          <span class="si-label">用时</span>
          <span class="si-value">{{ formatDuration(data.elapsedSeconds) }}</span>
        </div>
        <div class="summary-item">
          <span class="si-label">剩余时间</span>
          <span class="si-value" :class="{ urgent: data.remainingTime < 300 }">{{ formatTime(data.remainingTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, computed, nextTick } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Timer, ArrowLeft, ArrowRight, Check } from "@element-plus/icons-vue";
import router from "@/router/index.js";

const particleCanvas = ref(null)
const questionRefs = ref([])

const data = reactive({
  loading: true,
  examId: null,
  recordId: null,
  exam: null,
  paper: null,
  questions: [],
  answers: {},
  multipleAnswers: {},
  fillinAnswers: {},
  remainingTime: 0,
  elapsedSeconds: 0,
  timer: null,
  switchCount: 0,
  currentIndex: 0,
  showParticles: false,
  submitDialogVisible: false,
  timerClicks: 0,
})

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

// 计算属性
const data_computed = computed(() => data)

// 答题进度百分比
const progressPercent = computed(() => {
  if (data.questions.length === 0) return 0
  const answered = data.questions.filter(q => {
    const ans = data.answers[q.id]
    if (q.type === 'multiple') return data.multipleAnswers[q.id]?.length > 0
    if (q.type === 'fillin') return data.fillinAnswers[q.id]?.some(a => a && a.trim())
    return ans && ans.toString().trim()
  }).length
  return Math.round((answered / data.questions.length) * 100)
})

// 已答数量
const answeredCount = computed(() => {
  return data.questions.filter(q => {
    const ans = data.answers[q.id]
    if (q.type === 'multiple') return data.multipleAnswers[q.id]?.length > 0
    if (q.type === 'fillin') return data.fillinAnswers[q.id]?.some(a => a && a.trim())
    return ans && ans.toString().trim()
  }).length
})

// 格式化时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const formatDuration = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) return `${h}小时${m}分`
  if (m > 0) return `${m}分${s}秒`
  return `${s}秒`
}

// 加载考试数据
const loadExam = () => {
  data.examId = parseInt(router.currentRoute.value.query.examId)
  request.get('/exam/selectById/' + data.examId).then(res => {
    if (res.code === '200') {
      data.exam = res.data
      request.get('/examPaper/selectById/' + data.exam.paperId).then(res2 => {
        if (res2.code === '200') {
          data.paper = res2.data
          request.get('/question/selectByPaperId/' + data.exam.paperId).then(res3 => {
            if (res3.code === '200') {
              data.questions = (res3.data || []).sort(() => Math.random() - 0.5)
              data.questions.forEach(q => {
                if (q.type === 'fillin') {
                  const blanks = getFillinBlanks(q)
                  initFillinAnswers(q.id, blanks.length)
                }
              })
            }
          })
        }
      })
    }
  })
}

// 开始考试
const startExam = () => {
  request.post('/examRecord/start?examId=' + data.examId + '&studentId=' + user.id).then(res => {
    if (res.code === '200') {
      data.recordId = res.data.id
      data.remainingTime = data.exam.duration * 60
      data.loading = false
      startTimer()
    }
  })
}

// 通用答案处理
const handleAnswer = (questionId, type) => {
  // 触发粒子反馈
  triggerParticleFeedback()
  saveAnswer(questionId)
}

// 触发粒子反馈动画
const triggerParticleFeedback = () => {
  data.showParticles = true
  nextTick(() => {
    const canvas = particleCanvas.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    const particles = []
    const colors = ['#67c23a', '#85ce61', '#a0d468', '#8cc63f']
    for (let i = 0; i < 30; i++) {
      const angle = Math.random() * Math.PI * 2
      const speed = 2 + Math.random() * 3
      particles.push({
        x: canvas.width / 2, y: canvas.height / 2,
        vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed - 2,
        life: 1, color: colors[Math.floor(Math.random() * colors.length)],
        size: 3 + Math.random() * 4
      })
    }
    let animId = null
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      let alive = false
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i]
        p.x += p.vx; p.y += p.vy; p.vy += 0.1; p.life -= 0.025
        if (p.life <= 0) { particles.splice(i, 1); continue }
        alive = true
        ctx.globalAlpha = p.life
        ctx.fillStyle = p.color
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2)
        ctx.fill()
      }
      ctx.globalAlpha = 1
      if (alive) animId = requestAnimationFrame(animate)
      else data.showParticles = false
    }
    animate()
  })
}

// 保存单选/判断/简答答案
const saveAnswer = (questionId) => {
  if (data.recordId && data.answers[questionId]) {
    request.post('/examRecord/saveAnswer', null, {
      params: {
        recordId: data.recordId, questionId: questionId,
        studentAnswer: data.answers[questionId]
      }
    })
  }
}

// 保存多选答案
const saveMultipleAnswer = (questionId) => {
  if (data.recordId && data.multipleAnswers[questionId]) {
    const answer = data.multipleAnswers[questionId].sort().join('')
    data.answers[questionId] = answer
    request.post('/examRecord/saveAnswer', null, {
      params: {
        recordId: data.recordId, questionId: questionId, studentAnswer: answer
      }
    })
  }
}

// 获取填空题的空位数
const getFillinBlanks = (question) => {
  const matches = question.content.match(/_{3,}/g)
  return matches || ['___']
}

// 初始化填空题答案
const initFillinAnswers = (questionId, blankCount) => {
  if (!data.fillinAnswers[questionId]) {
    data.fillinAnswers[questionId] = new Array(blankCount).fill('')
  }
}

// 保存填空答案
const saveFillinAnswer = (questionId) => {
  if (data.recordId && data.fillinAnswers[questionId]) {
    const answer = data.fillinAnswers[questionId].join('|||')
    data.answers[questionId] = answer
    request.post('/examRecord/saveAnswer', null, {
      params: {
        recordId: data.recordId, questionId: questionId, studentAnswer: answer
      }
    })
  }
}

// 获取答题卡样式
const getAnswerClass = (questionId) => {
  if (data.answers[questionId] || (data.multipleAnswers[questionId] && data.multipleAnswers[questionId].length > 0)) return 'answered'
  if (data.fillinAnswers[questionId] && data.fillinAnswers[questionId].some(a => a && a.trim())) return 'answered'
  return 'unanswered'
}

// 上一题/下一题
const prevQuestion = () => {
  if (data.currentIndex > 0) data.currentIndex--
}
const nextQuestion = () => {
  if (data.currentIndex < data.questions.length - 1) data.currentIndex++
}
const jumpToQuestion = (index) => {
  data.currentIndex = index
}

// 提交试卷（显示确认弹窗）
const handleSubmitExam = () => {
  data.submitDialogVisible = true
}

// 确认交卷
const submitExam = () => {
  request.post('/examRecord/submit?recordId=' + data.recordId).then(res => {
    if (res.code === '200') {
      ElMessage.success('提交成功')
      clearInterval(data.timer)
      data.submitDialogVisible = false
      router.push('/front/examResult?recordId=' + data.recordId)
    }
  })
}

// 开始倒计时
const startTimer = () => {
  data.timer = setInterval(() => {
    data.remainingTime--
    data.elapsedSeconds++
    // 最后5分钟警告
    if (data.remainingTime === 300) {
      ElMessage.warning({ message: '⚠️ 仅剩5分钟，请注意答题进度！', duration: 4000 })
    }
    if (data.remainingTime <= 0) {
      clearInterval(data.timer)
      ElMessage.warning('考试时间到，自动提交！')
      submitExam()
    }
  }, 1000)
}

// 自动保存答案（每30秒）
let autoSave = null

// 键盘快捷键
const handleKeyDown = (e) => {
  if (data.loading) return
  if (e.key === 'j' || e.key === 'J') nextQuestion()
  if (e.key === 'k' || e.key === 'K') prevQuestion()
  // 时钟彩蛋
  if (e.key === 'Enter' && data.currentIndex === data.questions.length - 1) {
    // 最后一题回车提示交卷
  }
}

// 时钟点击彩蛋检测
const checkTimerEasterEgg = () => {
  data.timerClicks++
  if (data.timerClicks === 10) {
    data.timerClicks = 0
    ElMessage.success({ message: '🎉 你发现了时间管理大师彩蛋！专注就是胜利！', duration: 3000 })
    // 记录彩蛋
    request.post('/easterEgg/discover', {
      userId: user.id, userName: user.name, userRole: user.role, eggName: 'timer_click_10'
    }).catch(() => {})
  }
}

onMounted(() => {
  loadExam()
  startExam()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  document.addEventListener('keydown', handleKeyDown)
  autoSave = setInterval(() => {
    if (data.recordId) {
      const answers = []
      for (const questionId in data.answers) {
        answers.push({ questionId: parseInt(questionId), studentAnswer: data.answers[questionId] || '' })
      }
      if (answers.length > 0) {
        request.post('/examRecord/saveAnswers?recordId=' + data.recordId, answers).catch(() => {})
      }
    }
  }, 30000)
})

onUnmounted(() => {
  if (data.timer) clearInterval(data.timer)
  if (autoSave) clearInterval(autoSave)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.removeEventListener('keydown', handleKeyDown)
})

// 切屏检测
const handleVisibilityChange = () => {
  if (document.visibilityState === 'hidden' && data.recordId) {
    data.switchCount++
    request.post('/examRecord/reportSwitch?recordId=' + data.recordId).then(res => {
      if (res.code === '200') {
        const reached = res.data?.maxReached
        ElMessage.warning({ message: `检测到切屏（第${data.switchCount}次），已记录${reached ? '，即将自动提交' : ''}`, duration: 3000 })
        if (reached) setTimeout(() => submitExam(), 1000)
      }
    })
  }
}

window.onbeforeunload = () => {
  if (data.recordId && data.remainingTime > 0) return '考试正在进行中，确定要离开吗？'
}
</script>

<style scoped>
/* 粒子画布 */
.particle-canvas {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
}

/* 顶部进度条 */
.top-progress-bar {
  background: #fff;
  border-radius: 12px;
  padding: 20px 28px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid rgba(0,0,0,0.04);
}
.exam-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0 0 4px; }
.exam-sub { font-size: 13px; color: #888; margin: 0; }

/* 环形进度 */
.progress-center { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.progress-ring-wrap { position: relative; width: 56px; height: 56px; }
.progress-ring-svg { width: 56px; height: 56px; transform: rotate(-90deg); }
.ring-bg { fill: none; stroke: #f0f0f0; stroke-width: 4; }
.ring-fill {
  fill: none;
  stroke: #409eff;
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.5s ease;
}
.ring-text {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #409eff;
}
.progress-label { font-size: 12px; color: #999; }

/* 快捷键提示 */
.shortcut-hints { display: flex; gap: 10px; margin-bottom: 8px; }
.shortcut-item { font-size: 12px; color: #999; }
kbd {
  background: #f0f0f0; border: 1px solid #ddd; border-radius: 3px;
  padding: 1px 5px; font-size: 11px; font-family: monospace; color: #555;
}

/* 计时器 */
.progress-info-right { text-align: right; }
.timer-wrap {
  display: inline-flex; align-items: center; gap: 6px;
  background: linear-gradient(135deg, #fef0f0, #fef3f3);
  border: 1px solid #fde2e2; border-radius: 8px;
  padding: 8px 16px; cursor: pointer; transition: all 0.2s;
}
.timer-wrap:hover { transform: scale(1.05); box-shadow: 0 2px 8px rgba(245,108,108,0.2); }
.timer-wrap.urgent {
  background: linear-gradient(135deg, #f56c6c, #e64242);
  border-color: #f56c6c; color: #fff; animation: urgentPulse 1s infinite;
}
@keyframes urgentPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245,108,108,0.4); }
  50% { box-shadow: 0 0 0 8px rgba(245,108,108,0); }
}
.timer-text { font-size: 20px; font-weight: 700; font-variant-numeric: tabular-nums; }
.timer-label { font-size: 12px; color: #999; margin-top: 4px; }

/* 题目导航 */
.question-nav {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0; margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.nav-indicator { font-size: 14px; color: #666; }
.nav-indicator strong { color: #409eff; }
.bottom-nav { border-top: 1px solid #f0f0f0; border-bottom: none; margin-top: 20px; margin-bottom: 0; padding-top: 20px; padding-bottom: 0; }

/* 选项样式 */
.option-item {
  padding: 10px 14px; border-radius: 8px;
  border: 1px solid #e4e7ed; margin-bottom: 8px;
  transition: all 0.2s; cursor: pointer;
}
.option-item:hover { border-color: #409eff; background: #f0f7ff; }
.option-item.selected { border-color: #409eff; background: #ecf5ff; }
.option-radio, .option-checkbox { width: 100%; }

/* 答题卡 */
.answer-sheet-card { width: 220px; flex-shrink: 0; }
.answer-sheet-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 6px; }
.answer-card-item {
  width: 100%; aspect-ratio: 1;
  border-radius: 6px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 13px; font-weight: 600;
  transition: all 0.2s; border: 2px solid transparent;
}
.answer-card-item.answered { background: #67c23a; color: white; }
.answer-card-item.unanswered { background: #e4e7ed; color: #666; }
.answer-card-item.current { border-color: #409eff; box-shadow: 0 0 0 3px rgba(64,158,255,0.25); }
.answer-card-item:hover { transform: scale(1.1); box-shadow: 0 2px 8px rgba(0,0,0,0.15); }

/* 答题统计 */
.answer-stats { display: flex; gap: 12px; margin-top: 14px; }
.stat-row { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 2px; }
.stat-label { font-size: 11px; color: #999; }
.stat-value { font-size: 18px; font-weight: 700; }
.stat-value.answered { color: #67c23a; }
.stat-value.unanswered { color: #999; }

/* 提交弹窗 */
.submit-warning, .submit-ready { margin-bottom: 16px; }
.submit-summary {
  display: flex; gap: 20px; margin-top: 16px;
  background: #f8f9fb; border-radius: 10px; padding: 16px;
}
.summary-item { display: flex; flex-direction: column; gap: 4px; flex: 1; align-items: center; }
.si-label { font-size: 12px; color: #999; }
.si-value { font-size: 18px; font-weight: 700; color: #333; }
.si-value.urgent { color: #f56c6c; animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }
</style>
