<template>
  <div v-loading="data.loading">
    <canvas ref="celebrateCanvas" class="celebrate-canvas" v-if="data.showCelebration"></canvas>

    <div class="mood-bar" v-if="data.record?.isPass">
      <span class="mood-label">如果你很开心：</span>
      <span class="mood-btn" @click="reactMood('laugh')" :class="{ active: data.userMood === 'laugh' }">😄 笑死</span>
      <span class="mood-btn" @click="reactMood('clap')" :class="{ active: data.userMood === 'clap' }">👏 点赞</span>
      <span class="mood-btn" @click="reactMood('think')" :class="{ active: data.userMood === 'think' }">🤔 思考</span>
      <span class="mood-counts">{{ data.moodCounts.laugh || 0 }}人😄 {{ data.moodCounts.clap || 0 }}人👏</span>
    </div>

    <div class="result-banner" :class="data.record?.isPass ? 'pass' : 'fail'">
      <div class="result-icon-wrap">
        <div class="result-ring" :class="data.record?.isPass ? 'pass-ring' : 'fail-ring'"></div>
        <div class="result-emoji" v-if="data.record?.isPass">
          <span class="emoji-ani">{{ getScoreEmoji() }}</span>
        </div>
        <el-icon :size="56" class="result-icon">
          <CircleCheck v-if="data.record?.isPass" />
          <CircleClose v-else />
        </el-icon>
      </div>
      <h2 class="result-title">{{ data.record?.isPass ? '恭喜你，审核通过！' : '很遗憾，审核未通过' }}</h2>
      <p class="result-sub" v-if="data.record?.isPass && data.record?.totalScore >= 95">太强了！接近满分！</p>
      <p class="result-sub" v-else-if="data.record?.isPass && data.record?.totalScore >= 90">太厉害了，优秀！</p>
      <p class="result-sub" v-else-if="data.record?.isPass && data.record?.totalScore >= 80">表现不错，继续保持！</p>
      <p class="result-sub" v-else-if="data.record?.isPass">及格了，再接再厉！</p>
      <p class="result-sub" v-else>不要灰心，下次一定能行！</p>
    </div>

    <div class="score-compare-card" v-if="data.compareData">
      <div class="compare-header">
        <span class="compare-title">📊 结果对比</span>
        <el-tag size="small" type="info">击败了 {{ data.compareData.percentile }}% 的玩家</el-tag>
      </div>
      <div class="compare-bars">
        <div class="compare-item">
          <span class="ci-label">你的分数</span>
          <div class="ci-bar-wrap">
            <div class="ci-bar-fill mine" :style="{ width: Math.min(data.record?.totalScore, 100) + '%' }"></div>
          </div>
          <span class="ci-value">{{ data.record?.totalScore }}分</span>
        </div>
        <div class="compare-item">
          <span class="ci-label">平均分</span>
          <div class="ci-bar-wrap">
            <div class="ci-bar-fill avg" :style="{ width: Math.min(data.compareData.avgScore, 100) + '%' }"></div>
          </div>
          <span class="ci-value">{{ data.compareData.avgScore }}分</span>
        </div>
        <div class="compare-item">
          <span class="ci-label">最高分</span>
          <div class="ci-bar-wrap">
            <div class="ci-bar-fill max" :style="{ width: Math.min(data.compareData.maxScore, 100) + '%' }"></div>
          </div>
          <span class="ci-value">{{ data.compareData.maxScore }}分</span>
        </div>
      </div>
    </div>

    <div class="score-cards-grid">
      <div class="score-mini-card card-accent-primary">
        <div class="smc-value">{{ data.record?.totalScore || 0 }}</div>
        <div class="smc-label">总分</div>
        <div class="smc-trend" v-if="data.compareData">
          <span :class="data.record?.totalScore >= data.compareData.avgScore ? 'trend-up' : 'trend-down'">
            {{ data.record?.totalScore >= data.compareData.avgScore ? '↑' : '↓' }}
            {{ Math.abs((data.record?.totalScore || 0) - data.compareData.avgScore).toFixed(1) }}分
          </span>
        </div>
      </div>
      <div class="score-mini-card card-accent-success">
        <div class="smc-value smc-value-success">{{ data.record?.autoScore || 0 }}</div>
        <div class="smc-label">客观题</div>
        <div class="smc-ratio">{{ data.correctObjCount }}/{{ data.totalObjCount }}</div>
      </div>
      <div class="score-mini-card card-accent-warning">
        <div class="smc-value smc-value-warning">{{ data.record?.manualScore || 0 }}</div>
        <div class="smc-label">主观题</div>
      </div>
      <div class="score-mini-card card-accent-tertiary">
        <div class="smc-value smc-value-tertiary">{{ formatDuration(data.record?.duration) }}</div>
        <div class="smc-label">用时</div>
      </div>
    </div>

    <div class="action-bar">
      <el-button type="primary" size="large" round @click="goBack">
        <el-icon><Back /></el-icon> 返回审核列表
      </el-button>
      <el-button size="large" round @click="router.push('/front/myScores')">
        <el-icon><DataLine /></el-icon> 查看结果单
      </el-button>
    </div>

    <div class="card answer-section">
      <div class="answer-section-header">
        <h3 class="section-heading">答题详情</h3>
        <div class="answer-summary-tags">
          <el-tag type="success" size="small">✅ 正确 {{ data.correctCount }}</el-tag>
          <el-tag type="danger" size="small">❌ 错误 {{ data.wrongCount }}</el-tag>
          <el-tag type="warning" size="small" v-if="data.essayCount">📝 待阅 {{ data.essayCount }}</el-tag>
        </div>
      </div>

      <div v-for="(answer, index) in data.answers" :key="answer.id"
        class="answer-detail-card"
        :class="{ correct: answer.isCorrect, wrong: !answer.isCorrect && answer.question?.type !== 'essay' }"
      >
        <div class="answer-detail-header">
          <span class="q-num-badge">{{ index + 1 }}</span>
          <el-tag v-if="answer.question?.type === 'single'" type="success" size="small">单选</el-tag>
          <el-tag v-else-if="answer.question?.type === 'multiple'" type="primary" size="small">多选</el-tag>
          <el-tag v-else-if="answer.question?.type === 'judge'" type="warning" size="small">判断</el-tag>
          <el-tag v-else-if="answer.question?.type === 'fillin'" type="info" size="small">填空</el-tag>
          <el-tag v-else type="info" size="small">简答</el-tag>
          <span class="q-score">{{ answer.question?.score }}分</span>
          <el-tag v-if="answer.isCorrect" type="success" size="small" class="result-tag">
            <el-icon><CircleCheck /></el-icon> +{{ answer.score }}
          </el-tag>
          <el-tag v-else-if="answer.question?.type !== 'essay'" type="danger" size="small" class="result-tag">
            <el-icon><CircleClose /></el-icon> +{{ answer.score || 0 }}
          </el-tag>
          <el-tag v-else type="warning" size="small" class="result-tag">待阅</el-tag>
        </div>

        <div class="question-content">{{ answer.question?.content }}</div>

        <div class="answer-block your-answer">
          <span class="ab-label">你的答案</span>
          <span class="ab-value" :class="answer.isCorrect ? 'correct-text' : (answer.question?.type !== 'essay' ? 'wrong-text' : '')">
            {{ answer.question?.type === 'fillin' ? formatFillinAnswer(answer.studentAnswer) : (answer.studentAnswer || '❓ 未作答') }}
          </span>
        </div>

        <div class="answer-block correct-answer" v-if="answer.question?.type !== 'essay'">
          <span class="ab-label">正确答案</span>
          <span class="ab-value correct-text">
            {{ answer.question?.type === 'fillin' ? formatFillinAnswer(answer.question?.answer) : answer.question?.answer }}
          </span>
        </div>

        <div class="analysis-box" v-if="answer.question?.analysis">
          <span class="analysis-icon">💡</span>
          <span class="analysis-text">{{ answer.question?.analysis }}</span>
        </div>

        <div v-if="answer.isCorrect" class="annotation-area">
          <div class="anno-divider"></div>
          <div class="anno-header-row">
            <span class="anno-title">💡 满分批注</span>
            <el-button size="small" type="primary" text @click="toggleAnnoInput(answer)">
              <el-icon><EditPen /></el-icon> 写批注
            </el-button>
          </div>
          <div v-if="data.annoInputVisible[answer.questionId]" class="anno-input-box">
            <el-input v-model="data.annoContent[answer.questionId]" type="textarea" :rows="2"
              placeholder="分享你对这道题的理解和解题思路..." maxlength="500" show-word-limit />
            <div class="anno-input-actions">
              <el-button size="small" @click="data.annoInputVisible[answer.questionId] = false">取消</el-button>
              <el-button size="small" type="primary" @click="submitAnnotation(answer)" :loading="data.annoSubmitting">发布</el-button>
            </div>
          </div>
          <div v-if="data.annotations[answer.questionId]?.length" class="anno-list">
            <div v-for="anno in data.annotations[answer.questionId]" :key="anno.id" class="anno-item">
              <div class="anno-meta">
                <span class="anno-user">{{ anno.userName }}</span>
                <span class="anno-time">{{ formatAnnoTime(anno.createdAt) }}</span>
              </div>
              <div class="anno-content">{{ anno.content }}</div>
              <div class="anno-actions">
                <span class="anno-like-btn" @click="likeAnnotation(answer.questionId, anno)">👍 {{ anno.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
          <div v-else-if="!data.annoInputVisible[answer.questionId]" class="anno-empty">暂无批注，做第一个分享的人吧</div>
        </div>

      </div>
    </div>

    <div class="result-footer">
      <el-button type="primary" size="large" round @click="goBack">返回审核列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { CircleCheck, CircleClose } from "@element-plus/icons-vue";
import router from "@/router/index.js";

const celebrateCanvas = ref(null)
let celebrationRAF = null

const data = reactive({
  loading: true,
  recordId: null,
  record: null,
  answers: [],
  showCelebration: false,
  compareData: null,
  userMood: null,
  moodCounts: { laugh: 0, clap: 0 },
  correctCount: 0,
  wrongCount: 0,
  essayCount: 0,
  correctObjCount: 0,
  totalObjCount: 0,
  annotations: {},
  annoInputVisible: {},
  annoContent: {},
  annoSubmitting: false,
  wrongAdded: {},
})

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const getScoreEmoji = () => {
  const s = data.record?.totalScore
  if (s >= 95) return '🏆'
  if (s >= 90) return '🌟'
  if (s >= 80) return '💪'
  return '🎉'
}

const computeStats = () => {
  let correct = 0, wrong = 0, essay = 0, objCorrect = 0, objTotal = 0
  data.answers.forEach(a => {
    if (a.question?.type === 'essay') { essay++; return }
    if (a.isCorrect) { correct++; objCorrect++ }
    else wrong++
    objTotal++
  })
  data.correctCount = correct
  data.wrongCount = wrong
  data.essayCount = essay
  data.correctObjCount = objCorrect
  data.totalObjCount = objTotal
}

const loadCompareData = (examId) => {
  request.get('/examRecord/compareScore', { params: { examId, studentId: user.id } }).then(res => {
    if (res.code === '200' && res.data) {
      data.compareData = res.data
    }
  }).catch(() => {})
}

const loadResult = () => {
  data.recordId = parseInt(router.currentRoute.value.query.recordId)
  request.get('/examRecord/detail/' + data.recordId).then(res => {
    if (res.code === '200') {
      data.record = res.data
      data.answers = res.data.answers || []
      computeStats()
      data.loading = false

      if (data.record?.isPass) {
        data.showCelebration = true
        nextTick(() => startCelebration())
        loadCompareData(data.record.examId)
      }

      data.answers.forEach(a => {
        if (a.isCorrect && a.questionId) loadAnnotations(a.questionId)
      })
    }
  })
}

const loadAnnotations = (questionId) => {
  request.get('/questionAnnotation/selectByQuestion', { params: { questionId } }).then(res => {
    if (res.code === '200') data.annotations[questionId] = res.data || []
  })
}

const toggleAnnoInput = (answer) => {
  data.annoInputVisible[answer.questionId] = !data.annoInputVisible[answer.questionId]
}

const submitAnnotation = (answer) => {
  const content = data.annoContent[answer.questionId]
  if (!content || !content.trim()) { ElMessage.warning('请输入批注内容'); return }
  data.annoSubmitting = true
  request.post('/questionAnnotation/add', {
    questionId: answer.questionId,
    userId: user.id, userName: user.name, userRole: user.role,
    content: content.trim()
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('批注发布成功')
      data.annoContent[answer.questionId] = ''
      data.annoInputVisible[answer.questionId] = false
      loadAnnotations(answer.questionId)
    } else ElMessage.error(res.msg || '发布失败')
  }).finally(() => { data.annoSubmitting = false })
}

const likeAnnotation = (questionId, anno) => {
  request.put('/questionAnnotation/like/' + anno.id).then(res => {
    if (res.code === '200') anno.likeCount = (anno.likeCount || 0) + 1
  })
}

const addToWrong = (answer) => {
  request.post('/wrongQuestion/add', {
    userId: user.id, userRole: user.role, questionId: answer.questionId,
    source: 'exam', sourceId: data.recordId, wrongAnswer: answer.studentAnswer
  }).then(res => {
    if (res.code === '200') {
      data.wrongAdded[answer.questionId] = true
      ElMessage.success('已加入错题复盘')
    }
  })
}

const reactMood = (mood) => {
  if (data.userMood) { ElMessage.info('你已经表达过心情啦'); return }
  data.userMood = mood
  data.moodCounts[mood] = (data.moodCounts[mood] || 0) + 1
  ElMessage.success({ message: mood === 'laugh' ? '😄 太棒了！' : mood === 'clap' ? '👏 继续加油！' : '🤔 思考使人进步！', duration: 2000 })
}


const startCelebration = () => {
  const canvas = celebrateCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const particles = []
  const colors = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff9ff3', '#54a0ff', '#5f27cd', '#48dbfb', '#ff9f43', '#ee5a24']

  for (let wave = 0; wave < 5; wave++) {
    setTimeout(() => {
      const cx = canvas.width * (0.1 + Math.random() * 0.8)
      const cy = canvas.height * (0.08 + Math.random() * 0.35)
      for (let i = 0; i < 60; i++) {
        const angle = (Math.PI * 2 / 60) * i + Math.random() * 0.4
        const speed = 2 + Math.random() * 5
        particles.push({
          x: cx, y: cy,
          vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed,
          life: 1, decay: 0.005 + Math.random() * 0.012,
          color: colors[Math.floor(Math.random() * colors.length)],
          size: 2 + Math.random() * 3, sparkle: Math.random() > 0.5
        })
      }
    }, wave * 700)
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    let alive = false
    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i]
      p.x += p.vx; p.y += p.vy; p.vy += 0.05; p.vx *= 0.99
      p.life -= p.decay
      if (p.life <= 0) { particles.splice(i, 1); continue }
      alive = true
      ctx.globalAlpha = p.life
      ctx.fillStyle = p.color
      if (p.sparkle && p.life > 0.5) {
        ctx.shadowColor = p.color; ctx.shadowBlur = 10
      } else ctx.shadowBlur = 0
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2)
      ctx.fill()
    }
    ctx.globalAlpha = 1; ctx.shadowBlur = 0
    if (alive) {
      celebrationRAF = requestAnimationFrame(animate)
    } else {
      celebrationRAF = null
      data.showCelebration = false
    }
  }
  celebrationRAF = requestAnimationFrame(animate)
}

onUnmounted(() => {
  if (celebrationRAF) {
    cancelAnimationFrame(celebrationRAF)
    celebrationRAF = null
  }
  data.showCelebration = false
})

const goBack = () => { router.push('/front/examList') }

const formatAnnoTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const formatFillinAnswer = (answer) => {
  if (!answer) return '未作答'
  return answer.split('|||').map((a, i) => `空${i + 1}: ${a}`).join('；')
}

const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return s > 0 ? `${m}分${s}秒` : `${m}分钟`
}

onMounted(() => { loadResult() })
</script>

<style scoped>
.celebrate-canvas { position: fixed; inset: 0; z-index: 9999; pointer-events: none; }

.mood-bar {
  display: flex; align-items: center; gap: 10px;
  background: var(--bg-card); border-radius: var(--radius-lg); padding: 12px 20px;
  margin-bottom: 16px; box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0,0,0,0.04);
  flex-wrap: wrap;
}
.mood-label { font-size: 14px; color: var(--text-secondary); font-weight: 500; }
.mood-btn {
  padding: 6px 14px; border-radius: 20px; border: 1px solid var(--border-lighter);
  font-size: 13px; cursor: pointer; transition: all 0.2s; color: var(--text-secondary);
}
.mood-btn:hover { border-color: var(--primary-color); color: var(--primary-color); background: var(--primary-light); }
.mood-btn.active { border-color: var(--primary-color); color: var(--primary-color); background: var(--primary-light); font-weight: 600; }
.mood-counts { margin-left: auto; font-size: 12px; color: var(--text-tertiary); }

.result-banner {
  text-align: center; padding: 36px 24px; border-radius: var(--radius-lg);
  margin-bottom: 20px; position: relative; overflow: hidden;
}
.result-banner.pass { background: linear-gradient(135deg, rgba(var(--primary-rgb), 0.1), rgba(56,249,215,0.05)); border: 1px solid rgba(var(--primary-rgb), 0.2); }
.result-banner.fail { background: linear-gradient(135deg, rgba(var(--danger-color), 0.1), rgba(255,107,107,0.05)); border: 1px solid rgba(245,108,108,0.2); }
.result-banner::before {
  content: ''; position: absolute; top: -80px; right: -80px;
  width: 200px; height: 200px; border-radius: 50%;
  background: rgba(var(--primary-rgb), 0.06);
}
.result-banner::after {
  content: ''; position: absolute; bottom: -60px; left: -60px;
  width: 160px; height: 160px; border-radius: 50%;
  background: rgba(var(--primary-rgb), 0.04);
}

.result-icon-wrap { position: relative; display: inline-block; margin-bottom: 12px; }
.result-ring {
  position: absolute; inset: -12px; border-radius: 50%;
  border: 3px solid rgba(var(--primary-rgb), 0.25); animation: ringPulse 2s ease-in-out infinite;
}
.fail-ring { border-color: rgba(245,108,108,0.25) !important; }
@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0; }
}
.result-icon { color: var(--success-color); }
.fail .result-icon { color: var(--danger-color); }
.result-emoji { position: absolute; top: -20px; right: -20px; }
.emoji-ani {
  font-size: 36px; display: inline-block;
  animation: emojiBounce 1s ease-in-out infinite;
}
@keyframes emojiBounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-8px) scale(1.15); }
}

.result-title { font-size: 26px; font-weight: 700; margin: 0 0 6px; color: var(--text-primary); }
.pass .result-title { color: var(--success-color); }
.fail .result-title { color: var(--danger-color); }
.result-sub { font-size: 15px; color: var(--text-tertiary); margin: 0; }

.score-compare-card {
  background: var(--bg-card); border-radius: var(--radius-lg); padding: 20px;
  margin-bottom: 16px; box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0,0,0,0.04);
}
.compare-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.compare-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.compare-bars { display: flex; flex-direction: column; gap: 14px; }
.compare-item { display: flex; align-items: center; gap: 12px; }
.ci-label { width: 60px; font-size: 13px; color: var(--text-secondary); text-align: right; flex-shrink: 0; }
.ci-bar-wrap { flex: 1; height: 12px; background: var(--bg-page); border-radius: 6px; overflow: hidden; }
.ci-bar-fill { height: 100%; border-radius: 6px; transition: width 0.8s ease; }
.ci-bar-fill.mine { background: linear-gradient(90deg, var(--primary-color), #53a8ff); }
.ci-bar-fill.avg { background: linear-gradient(90deg, var(--warning-color), #ebb563); }
.ci-bar-fill.max { background: linear-gradient(90deg, var(--success-color), #85ce61); }
.ci-value { width: 48px; font-size: 13px; font-weight: 600; color: var(--text-primary); text-align: left; flex-shrink: 0; }

.score-cards-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.score-mini-card {
  background: var(--bg-card); border-radius: var(--radius-lg); padding: 20px; text-align: center;
  box-shadow: var(--shadow-sm); border-left: 3px solid var(--accent);
}
.card-accent-primary { --accent: var(--primary-color); }
.card-accent-success { --accent: var(--success-color); }
.card-accent-warning { --accent: var(--warning-color); }
.card-accent-tertiary { --accent: var(--text-tertiary); }
.smc-value { font-size: 30px; font-weight: 700; color: var(--text-primary); line-height: 1; }
.smc-value-success { color: var(--success-color); }
.smc-value-warning { color: var(--warning-color); }
.smc-value-tertiary { color: var(--text-tertiary); }
.smc-label { font-size: 13px; color: var(--text-tertiary); margin-top: 6px; }
.smc-trend { margin-top: 6px; }
.trend-up { font-size: 12px; color: var(--success-color); font-weight: 600; }
.trend-down { font-size: 12px; color: var(--danger-color); font-weight: 600; }
.smc-ratio { font-size: 12px; color: var(--text-tertiary); margin-top: 4px; }

.action-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }

.answer-section { background: var(--bg-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 24px; }
.answer-section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.section-heading { font-size: 16px; font-weight: 700; color: var(--text-primary); margin: 0; }
.answer-summary-tags { display: flex; gap: 8px; }

.answer-detail-card {
  padding: 18px; margin-bottom: 14px; border-radius: var(--radius-lg);
  border: 1px solid var(--border-lighter); transition: all 0.2s;
}
.answer-detail-card:hover { box-shadow: var(--shadow-md); }
.answer-detail-card.correct { border-left: 4px solid var(--success-color); background: rgba(var(--primary-rgb), 0.02); }
.answer-detail-card.wrong { border-left: 4px solid var(--danger-color); background: rgba(245,108,108,0.02); }

.answer-detail-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.q-num-badge {
  background: var(--primary-color); color: white; padding: 2px 9px; border-radius: var(--radius-sm);
  font-size: 12px; font-weight: 700;
}
.q-score { font-size: 12px; color: var(--text-tertiary); }
.result-tag { display: inline-flex; align-items: center; gap: 3px; }

.question-content { font-size: 15px; color: var(--text-primary); line-height: 1.7; margin-bottom: 12px; }

.answer-block { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 8px; }
.ab-label { font-size: 12px; color: var(--text-tertiary); min-width: 60px; padding-top: 2px; }
.ab-value { font-size: 14px; color: var(--text-primary); line-height: 1.6; flex: 1; }
.correct-text { color: var(--success-color) !important; font-weight: 500; }
.wrong-text { color: var(--danger-color) !important; }

.analysis-box {
  display: flex; gap: 8px; background: var(--bg-page); padding: 10px 14px;
  border-radius: var(--radius-md); margin: 8px 0; font-size: 14px; color: var(--text-secondary); line-height: 1.6;
}
.analysis-icon { flex-shrink: 0; font-size: 16px; }
.analysis-text { flex: 1; }

.annotation-area { margin-top: 4px; }
.anno-divider { height: 1px; background: var(--border-lighter); margin: 12px 0; }
.anno-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.anno-title { font-size: 14px; font-weight: 600; color: var(--text-secondary); }
.anno-input-box { background: var(--bg-page); padding: 12px; border-radius: var(--radius-md); margin-bottom: 8px; }
.anno-input-actions { text-align: right; margin-top: 6px; }
.anno-list { display: flex; flex-direction: column; gap: 6px; }
.anno-item { padding: 10px; background: var(--bg-page); border-radius: var(--radius-md); }
.anno-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.anno-user { font-size: 13px; font-weight: 500; color: var(--primary-color); }
.anno-time { font-size: 11px; color: var(--text-tertiary); }
.anno-content { font-size: 14px; color: var(--text-primary); line-height: 1.6; }
.anno-actions { text-align: right; margin-top: 4px; }
.anno-like-btn { cursor: pointer; font-size: 13px; color: var(--text-tertiary); transition: color 0.2s; }
.anno-like-btn:hover { color: var(--primary-color); }
.anno-empty { color: var(--text-tertiary); font-size: 13px; text-align: center; padding: 8px; }

.wrong-actions { }
.wrong-divider { margin: 10px 0; }
.wrong-btns { display: flex; gap: 8px; }

.result-footer { text-align: center; margin-top: 24px; }

@media (max-width: 768px) {
  .score-cards-grid { grid-template-columns: repeat(2, 1fr); }
  .action-bar { flex-direction: column; }
}
</style>
