<template>
  <div class="take-exam">

    <!-- 参加审核列表 -->
    <template v-if="!data.inExam">

      <!-- 页面标题 -->
      <div class="page-hero">
        <div class="hero-left">
          <div class="hero-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div>
            <h1>参加审核</h1>
            <p>选择审核、进入答题、完成提交</p>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid" v-if="data.availableExams.length">
        <div class="stat-card stat-available"><div class="stat-inner"><div class="stat-num">{{ data.availableExams.length }}</div><div class="stat-label">可参加</div></div></div>
        <div class="stat-card stat-ongoing"><div class="stat-inner"><div class="stat-num">{{ ongoingCount }}</div><div class="stat-label">进行中</div></div></div>
      </div>

      <!-- 审核列表 -->
      <div class="exam-list-card" v-loading="data.loading">
        <transition-group name="exam-anim">
          <div v-for="exam in data.availableExams" :key="exam.id" class="exam-card-item">
            <div class="exam-card-body">
              <div class="exam-card-header">
                <div class="exam-card-icon">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <div class="exam-info">
                  <span class="exam-name">{{ exam.name }}</span>
                  <div class="exam-tags">
                    <span class="exam-type-badge">{{ exam.examType === 'scheduled' ? '统一审核' : '常驻审核' }}</span>
                    <el-tag v-if="exam.enableRecording" type="warning" size="small" effect="plain">录屏监控</el-tag>
                  </div>
                </div>
              </div>
              <div class="exam-desc" v-if="exam.description">{{ exam.description }}</div>
              <div class="exam-card-meta">
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ exam.duration }}分钟</span>
                </div>
                <div class="meta-item" v-if="exam.startTime">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ exam.startTime?.replace('T', ' ') }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Document /></el-icon>
                  <span>{{ exam.paperName || '未关联试卷' }}</span>
                </div>
                <div class="meta-item" v-if="exam.branch">
                  <el-icon><Folder /></el-icon>
                  <span>{{ exam.branch }}</span>
                </div>
              </div>
            </div>
            <div class="exam-card-action">
              <el-button type="primary" round @click="startExam(exam)" style="--el-button-bg-color: #4338ca; --el-button-border-color: #4338ca;">
                <el-icon><Edit /></el-icon> 进入审核
              </el-button>
            </div>
          </div>
        </transition-group>

        <div v-if="!data.loading && !data.availableExams.length" class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="#a5b4fc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="#a5b4fc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>暂无可参加的审核</p>
        </div>
      </div>
    </template>

    <!-- 审核答题界面 -->
    <template v-else>
      <!-- 答题页头部 -->
      <div class="exam-taking-hero">
        <div class="exam-taking-info">
          <h2>{{ data.currentExam.name }}</h2>
          <div class="exam-taking-meta">
            <span class="time-badge" :class="data.timeLeft < 300 ? 'time-urgent' : ''">
              <el-icon><Clock /></el-icon>
              {{ formatTime(data.timeLeft) }}
            </span>
            <span class="progress-badge">
              <el-icon><Edit /></el-icon>
              {{ answeredCount }}/{{ data.questions.length }}
            </span>
          </div>
        </div>
        <div class="exam-taking-actions">
          <el-button type="success" round @click="submitExam" style="--el-button-bg-color: #16a34a; --el-button-border-color: #16a34a;">
            <el-icon><Check /></el-icon> 提交审核
          </el-button>
        </div>
      </div>

      <!-- 题目导航 -->
      <div class="q-nav-card">
        <div class="q-nav-header">
          <span class="q-nav-title">题目导航</span>
          <div class="q-nav-legend">
            <span class="legend-item"><span class="legend-dot dot-current"></span>当前</span>
            <span class="legend-item"><span class="legend-dot dot-answered"></span>已答</span>
            <span class="legend-item"><span class="legend-dot dot-unanswered"></span>未答</span>
          </div>
        </div>
        <div class="q-nav-grid">
          <div v-for="(q, idx) in data.questions" :key="idx"
               class="q-nav-dot" :class="{
                 current: data.currentQ === idx,
                 answered: data.answers[q.id] !== undefined && data.answers[q.id] !== '' && data.answers[q.id] !== null,
               }"
               @click="goToQuestion(idx)">
            {{ idx + 1 }}
          </div>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-card">
        <transition :name="data.slideDirection" mode="out-in">
          <div class="question-content" :key="data.currentQ" v-if="data.questions[data.currentQ]">
            <div class="q-header">
              <span class="q-num">第 {{ data.currentQ + 1 }} 题</span>
              <span class="q-type-badge" :class="data.questions[data.currentQ].type">
                {{ typeLabel(data.questions[data.currentQ].type) }}
              </span>
              <span class="q-score-badge">{{ data.questions[data.currentQ].score }}分</span>
            </div>
            <div class="q-title">{{ data.questions[data.currentQ].content }}</div>

            <!-- 选择题选项 -->
            <div v-if="data.questions[data.currentQ].type === 'single'" class="q-options">
              <el-radio-group v-model="data.answers[data.questions[data.currentQ].id]" class="option-group">
                <div v-for="opt in parseOptions(data.questions[data.currentQ])" :key="opt.key" class="option-item" :class="{ selected: data.answers[data.questions[data.currentQ].id] === opt.key }">
                  <el-radio :value="opt.key">
                    <span class="opt-key">{{ opt.key }}</span>
                    <span class="opt-val">{{ opt.value }}</span>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
            <div v-else-if="data.questions[data.currentQ].type === 'multiple'" class="q-options">
              <el-checkbox-group v-model="data.answers[data.questions[data.currentQ].id]" class="option-group">
                <div v-for="opt in parseOptions(data.questions[data.currentQ])" :key="opt.key" class="option-item" :class="{ selected: Array.isArray(data.answers[data.questions[data.currentQ].id]) && data.answers[data.questions[data.currentQ].id].includes(opt.key) }">
                  <el-checkbox :value="opt.key">
                    <span class="opt-key">{{ opt.key }}</span>
                    <span class="opt-val">{{ opt.value }}</span>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
            <div v-else-if="data.questions[data.currentQ].type === 'judge'" class="q-options">
              <el-radio-group v-model="data.answers[data.questions[data.currentQ].id]" class="option-group">
                <div v-for="opt in [{ key: '正确', val: '正确' }, { key: '错误', val: '错误' }]" :key="opt.key" class="option-item" :class="{ selected: data.answers[data.questions[data.currentQ].id] === opt.key }">
                  <el-radio :value="opt.key">{{ opt.val }}</el-radio>
                </div>
              </el-radio-group>
            </div>
            <div v-else-if="data.questions[data.currentQ].type === 'fill'" class="q-options">
              <el-input v-model="data.answers[data.questions[data.currentQ].id]" placeholder="请输入答案" size="large" />
            </div>
            <div v-else class="q-options">
              <el-input v-model="data.answers[data.questions[data.currentQ].id]" type="textarea" :rows="5" placeholder="请输入答案" />
            </div>

            <!-- 上下题 -->
            <div class="q-footer">
              <el-button :disabled="data.currentQ === 0" @click="prevQuestion" round size="default">
                <el-icon><ArrowLeft /></el-icon> 上一题
              </el-button>
              <span class="q-counter">{{ data.currentQ + 1 }} / {{ data.questions.length }}</span>
              <el-button v-if="data.currentQ < data.questions.length - 1" @click="nextQuestion" round type="primary" size="default">
                下一题 <el-icon><ArrowRight /></el-icon>
              </el-button>
              <el-button v-else @click="submitExam" round type="success" size="default">
                <el-icon><Check /></el-icon> 提交审核
              </el-button>
            </div>
          </div>
        </transition>
      </div>
    </template>

    <!-- 录屏提示 -->
    <el-dialog v-model="data.recordingDialog" title="录屏提示" width="440px" :close-on-click-modal="false" :show-close="false">
      <div class="record-tip-box">
        <el-icon size="32" color="#f59e0b"><VideoCamera /></el-icon>
        <p>本次审核需要开启录屏监控，请选择要分享的屏幕。</p>
        <p style="color: #92400e; font-size: 13px; background: #fef3c7; border-radius: 8px; padding: 8px;">请选择整个屏幕进行分享，审核期间请勿切换窗口。</p>
      </div>
      <template #footer>
        <el-button @click="data.recordingDialog = false" size="default">取消</el-button>
        <el-button type="primary" @click="startRecordingAndExam" size="default">
          <el-icon><VideoCamera /></el-icon> 开始录屏并进入审核
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { Clock, Calendar, Document, Folder, Edit, Check, ArrowLeft, ArrowRight, VideoCamera } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  availableExams: [],
  inExam: false,
  currentExam: {},
  currentRecord: {},
  questions: [],
  answers: {},
  currentQ: 0,
  timeLeft: 0,
  timer: null,
  recordingDialog: false,
  mediaRecorder: null,
  screenStream: null,
  switchCount: 0,
  recordingId: null,
  slideDirection: 'slide-left',
  autoSaveTimer: null
})

const ongoingCount = computed(() => data.availableExams.filter(e => e.status === 'ongoing').length)
const answeredCount = computed(() => Object.keys(data.answers).filter(k => {
  const v = data.answers[k]
  return v !== undefined && v !== '' && v !== null && !(Array.isArray(v) && v.length === 0)
}).length)

const prevQuestion = () => {
  if (data.currentQ > 0) {
    data.slideDirection = 'slide-right'
    data.currentQ--
  }
}

const nextQuestion = () => {
  if (data.currentQ < data.questions.length - 1) {
    data.slideDirection = 'slide-left'
    data.currentQ++
  }
}

const goToQuestion = (idx) => {
  if (idx === data.currentQ) return
  data.slideDirection = idx > data.currentQ ? 'slide-left' : 'slide-right'
  data.currentQ = idx
}

const typeLabel = (t) => ({ single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '简答' }[t] || t)

const parseOptions = (q) => {
  const opts = []
  if (q.optionA) opts.push({ key: 'A', value: q.optionA })
  if (q.optionB) opts.push({ key: 'B', value: q.optionB })
  if (q.optionC) opts.push({ key: 'C', value: q.optionC })
  if (q.optionD) opts.push({ key: 'D', value: q.optionD })
  return opts
}

const formatTime = (s) => {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

const loadAvailable = () => {
  request.get('/exam/getAvailableExams/' + data.user.id).then(res => {
    if (res.code === '200') data.availableExams = res.data || []
  }).catch(() => {})
}

const startExam = (exam) => {
  if (exam.enableRecording) {
    data.currentExam = exam
    data.recordingDialog = true
  } else {
    doStartExam(exam)
  }
}

const startRecordingAndExam = async () => {
  try {
    data.screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: false })
    data.mediaRecorder = new MediaRecorder(data.screenStream, { mimeType: 'video/webm;codecs=vp9' })
    const chunks = []
    data.mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) chunks.push(e.data) }
    data.mediaRecorder.onstop = () => {
      const blob = new Blob(chunks, { type: 'video/webm' })
      handleRecordingComplete(blob)
      data.screenStream.getTracks().forEach(t => t.stop())
    }
    data.mediaRecorder.start(1000)
    document.addEventListener('visibilitychange', onVisibilityChange)
    request.post('/examRecording/start', {
      examId: data.currentExam.id,
      studentId: data.user.id,
      status: 'recording'
    }).then(res => {
      if (res.code === '200') data.recordingId = res.data?.id
    }).catch(() => {})
    data.recordingDialog = false
    doStartExam(data.currentExam)
  } catch (e) {
    ElMessage.error('无法获取屏幕分享权限')
  }
}

const onVisibilityChange = () => {
  if (document.hidden) {
    data.switchCount++
    ElMessage.warning(`检测到切屏！已切屏 ${data.switchCount} 次`)
  }
}

const autoSaveAnswers = () => {
  if (!data.currentRecord.id || !Object.keys(data.answers).length) return
  const answerList = Object.entries(data.answers).map(([questionId, studentAnswer]) => ({
    questionId: parseInt(questionId),
    studentAnswer: Array.isArray(studentAnswer) ? studentAnswer.join(',') : String(studentAnswer)
  }))
  request.post('/examRecord/saveAnswers', answerList, { params: { recordId: data.currentRecord.id } }).catch(() => {})
}

const doStartExam = (exam) => {
  request.post('/examRecord/start', null, { params: { examId: exam.id, studentId: data.user.id } }).then(res => {
    if (res.code === '200') {
      data.currentExam = exam
      data.currentRecord = res.data
      data.inExam = true
      data.timeLeft = exam.duration * 60
      data.answers = {}
      data.currentQ = 0
      request.get('/examPaper/selectById/' + exam.paperId).then(paperRes => {
        if (paperRes.code === '200' && paperRes.data) {
          request.get('/question/selectByPaperId/' + exam.paperId).then(qRes => {
            if (qRes.code === '200') data.questions = qRes.data || []
          }).catch(() => {})
        }
      }).catch(() => {})
      data.timer = setInterval(() => {
        data.timeLeft--
        if (data.timeLeft <= 0) {
          clearInterval(data.timer)
          submitExam()
        }
      }, 1000)
      data.autoSaveTimer = setInterval(() => { autoSaveAnswers() }, 30000)
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(() => {})
}

const submitExam = () => {
  ElMessageBox.confirm(`确定要提交审核吗？已答 ${answeredCount.value} / ${data.questions.length} 题`, '确认提交').then(() => {
    doSubmit()
  }).catch(() => {})
}

const doSubmit = () => {
  clearInterval(data.timer)
  if (data.autoSaveTimer) clearInterval(data.autoSaveTimer)
  const answerList = Object.entries(data.answers).map(([questionId, studentAnswer]) => ({
    questionId: parseInt(questionId),
    studentAnswer: Array.isArray(studentAnswer) ? studentAnswer.join(',') : String(studentAnswer)
  }))
  const savePromise = answerList.length > 0
    ? request.post('/examRecord/saveAnswers', answerList, { params: { recordId: data.currentRecord.id } })
    : Promise.resolve()
  savePromise.then(() => {
    request.post('/examRecord/submit', null, { params: { recordId: data.currentRecord.id } }).then(res => {
      if (res.code === '200') {
        ElMessage.success('提交成功！')
        if (data.mediaRecorder && data.mediaRecorder.state !== 'inactive') data.mediaRecorder.stop()
        document.removeEventListener('visibilitychange', onVisibilityChange)
        data.inExam = false
        loadAvailable()
      } else {
        ElMessage.error(res.msg)
      }
    }).catch(() => {})
  }).catch(() => {})
}

const handleRecordingComplete = (blob) => {
  const duration = data.currentExam.duration * 60 - data.timeLeft
  if (data.recordingId) {
    request.put('/examRecording/update', {
      id: data.recordingId,
      recordId: data.currentRecord.id,
      duration: duration,
      switchCount: data.switchCount,
      status: 'uploading'
    }).catch(() => {})
  }
  const formData = new FormData()
  formData.append('file', blob, `recording_${data.currentExam.id}_${data.user.id}.webm`)
  formData.append('recordingId', data.recordingId)
  formData.append('examId', data.currentExam.id)
  formData.append('studentId', data.user.id)
  request.post('/examRecording/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  }).then(res => {
    if (res.code === '200' && res.data) ElMessage.success('录屏已上传至云盘')
    else { ElMessage.warning('录屏上传失败，已保存本地'); downloadLocal(blob) }
  }).catch(() => { downloadLocal(blob) })
}

const downloadLocal = (blob) => {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `review_recording_${data.currentExam.id}_${data.user.id}.webm`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadAvailable)
onUnmounted(() => {
  if (data.timer) clearInterval(data.timer)
  if (data.autoSaveTimer) clearInterval(data.autoSaveTimer)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<style scoped>
@import "@/assets/css/transitions.css";

.take-exam { padding: 0; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #3730a3 0%, #6366f1 50%, #818cf8 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 8px 32px rgba(67, 56, 202, 0.25); }
.hero-left { display: flex; align-items: center; gap: 14px; }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #fff; }
.page-hero p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 16px; max-width: 400px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 16px 18px; color: #fff; }
.stat-available .stat-inner { background: linear-gradient(135deg, #3730a3, #6366f1); }
.stat-ongoing .stat-inner { background: linear-gradient(135deg, #16a34a, #4ade80); }
.stat-num { font-size: 26px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 审核列表卡片 ===== */
.exam-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.exam-card-item { display: flex; align-items: center; gap: 20px; padding: 22px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.exam-card-item:last-child { border-bottom: none; }
.exam-card-item:hover { background: #eef2ff; }
.exam-card-body { flex: 1; min-width: 0; }
.exam-card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.exam-card-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #3730a3, #6366f1); color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.exam-info { flex: 1; }
.exam-name { font-size: 16px; font-weight: 700; color: #1f2937; display: block; margin-bottom: 4px; }
.exam-tags { display: flex; gap: 6px; align-items: center; }
.exam-type-badge { padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #eef2ff; color: #4338ca; }
.exam-desc { font-size: 13px; color: #9ca3af; margin-bottom: 10px; }
.exam-card-meta { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 5px; font-size: 13px; color: #6b7280; }
.exam-card-action { flex-shrink: 0; }

/* ===== 答题页 ===== */
.exam-taking-hero { background: linear-gradient(135deg, #1e1b4b 0%, #3730a3 50%, #6366f1 100%); border-radius: 16px; padding: 22px 28px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(67, 56, 202, 0.3); }
.exam-taking-info h2 { margin: 0 0 8px; font-size: 20px; font-weight: 700; color: #fff; }
.exam-taking-meta { display: flex; gap: 10px; }
.time-badge, .progress-badge { display: inline-flex; align-items: center; gap: 5px; padding: 5px 14px; border-radius: 20px; font-size: 14px; font-weight: 600; background: rgba(255,255,255,0.15); color: #fff; border: 1px solid rgba(255,255,255,0.2); }
.time-badge.time-urgent { background: rgba(220,38,38,0.4); border-color: rgba(220,38,38,0.5); color: #fca5a5; animation: timePulse 1s infinite; }
@keyframes timePulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }

/* ===== 题目导航 ===== */
.q-nav-card { background: #fff; border-radius: 14px; padding: 18px 22px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.q-nav-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.q-nav-title { font-size: 14px; font-weight: 600; color: #374151; }
.q-nav-legend { display: flex; gap: 12px; }
.legend-item { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #6b7280; }
.legend-dot { width: 12px; height: 12px; border-radius: 50%; }
.dot-current { background: #4338ca; }
.dot-answered { background: #16a34a; }
.dot-unanswered { background: #e5e7eb; }
.q-nav-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.q-nav-dot { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 13px; font-weight: 600; border: 2px solid #e5e7eb; background: #f9fafb; color: #9ca3af; transition: all 0.2s; }
.q-nav-dot:hover { border-color: #6366f1; color: #4338ca; }
.q-nav-dot.current { background: #4338ca; border-color: #4338ca; color: #fff; }
.q-nav-dot.answered { background: #dcfce7; border-color: #16a34a; color: #15803d; }

/* ===== 题目内容卡片 ===== */
.question-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.question-content { padding: 28px; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.q-num { font-size: 14px; font-weight: 700; color: #374151; }
.q-type-badge { padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.q-type-badge.single { background: #dbeafe; color: #1e40af; }
.q-type-badge.multiple { background: #ede9fe; color: #5b21b6; }
.q-type-badge.judge { background: #fef3c7; color: #92400e; }
.q-type-badge.fill { background: #dcfce7; color: #14532d; }
.q-type-badge.essay { background: #fee2e2; color: #991b1b; }
.q-score-badge { padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #f1f5f9; color: #6b7280; }
.q-title { font-size: 16px; color: #1f2937; line-height: 1.7; margin-bottom: 22px; font-weight: 500; }
.q-options { margin-bottom: 24px; }
.option-group { display: flex; flex-direction: column; gap: 10px; }
.option-item { padding: 12px 16px; border-radius: 10px; border: 2px solid #e5e7eb; transition: all 0.2s; cursor: pointer; }
.option-item:hover { border-color: #818cf8; background: #eef2ff; }
.option-item.selected { border-color: #4338ca; background: #eef2ff; }
.option-item :deep(.el-radio__label), .option-item :deep(.el-checkbox__label) { display: flex; align-items: flex-start; gap: 8px; }
.opt-key { font-weight: 700; color: #4338ca; flex-shrink: 0; }
.opt-val { color: #374151; line-height: 1.5; }
.q-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.q-counter { font-size: 14px; color: #9ca3af; }

/* 交卷按钮样�� */
.exam-taking-actions .el-button { font-weight: 600; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

/* 录屏提示 */
.record-tip-box { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 12px 0; text-align: center; }
.record-tip-box p { margin: 0; font-size: 14px; color: #6b7280; }

/* 动画 */
.exam-anim-enter-active, .exam-anim-leave-active { transition: all 0.3s; }
.exam-anim-enter-from { opacity: 0; transform: translateY(8px); }
.exam-anim-leave-to { opacity: 0; }
</style>
