<template>
  <div class="main-content">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text-wrap">
        <div class="welcome-title">
          {{ getGreeting() }}，{{ data.user.name || '同学' }} 👋
        </div>
        <div class="welcome-desc">{{ todayStr }}</div>
      </div>
      <div class="welcome-right" v-if="data.user.id">
        <div class="welcome-stats">
          <div class="ws-item">
            <span class="ws-val">{{ data.stats.completed }}</span>
            <span class="ws-lbl">已完成</span>
          </div>
          <div class="ws-divider"></div>
          <div class="ws-item">
            <span class="ws-val">{{ data.stats.avgScore }}</span>
            <span class="ws-lbl">平均分</span>
          </div>
          <div class="ws-divider"></div>
          <div class="ws-item">
            <span class="ws-val">{{ data.stats.passRate }}%</span>
            <span class="ws-lbl">通过率</span>
          </div>
        </div>
        <div class="progress-wrap" @click="router.push('/front/dashboard')">
          <svg class="spr-svg" viewBox="0 0 60 60">
            <circle class="spr-bg" cx="30" cy="30" r="24" />
            <circle class="spr-fill" cx="30" cy="30" r="24"
              :stroke-dasharray="150.8"
              :stroke-dashoffset="150.8 - (150.8 * data.learningProgress / 100)"
            />
          </svg>
          <div class="spr-text">
            <span class="spr-value">{{ data.learningProgress }}%</span>
            <span class="spr-label">学习</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 每日任务 -->
    <div class="mission-trend-row" v-if="data.user.id">
      <div class="daily-mission-card">
        <div class="dm-header">
          <span class="dm-title">今日任务</span>
          <span class="dm-progress">{{ data.missions.filter(m => m.done).length }}/{{ data.missions.length }}</span>
        </div>
        <div class="mission-list">
          <div
            v-for="mission in data.missions"
            :key="mission.id"
            class="mission-item"
            :class="{ done: mission.done }"
            @click="doMission(mission)"
          >
            <div class="mi-check">
              <span v-if="mission.done" class="mi-checked">✓</span>
              <span v-else class="mi-circle"></span>
            </div>
            <div class="mi-info">
              <span class="mi-name">{{ mission.name }}</span>
              <span class="mi-desc">{{ mission.desc }}</span>
            </div>
            <span class="mi-badge" :style="{ background: mission.color }">{{ mission.reward }}</span>
          </div>
        </div>
      </div>

      <!-- 学习趋势 -->
      <div class="trend-card">
        <div class="trend-header">
          <span class="trend-title">近7天练习趋势</span>
          <span class="trend-more" @click="router.push('/front/myScores')">详情 →</span>
        </div>
        <div class="trend-bars">
          <div v-for="(day, idx) in data.practiceTrend" :key="idx" class="trend-bar-item">
            <div class="tb-bar-wrap">
              <div class="tb-bar" :style="{ height: (day.count / data.maxTrendCount * 80) + 'px' }">
                <span class="tb-tooltip" v-if="day.count > 0">{{ day.count }}题</span>
              </div>
            </div>
            <span class="tb-label">{{ day.label }}</span>
          </div>
        </div>
        <div class="trend-summary" v-if="data.practiceTrend.length">
          <span>本周共练习 <strong>{{ data.totalWeeklyPractice }}</strong> 题</span>
          <span :class="data.weeklyChange >= 0 ? 'trend-up' : 'trend-down'">
            {{ data.weeklyChange >= 0 ? '↑' : '↓' }}
            {{ Math.abs(data.weeklyChange) }}% vs上周
          </span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stats-card blue hover-lift">
        <div class="stats-icon blue"><el-icon :size="22"><EditPen /></el-icon></div>
        <div class="stats-value">{{ data.stats.available }}</div>
        <div class="stats-label">可参加考试</div>
      </div>
      <div class="stats-card green hover-lift">
        <div class="stats-icon green"><el-icon :size="22"><CircleCheck /></el-icon></div>
        <div class="stats-value">{{ data.stats.completed }}</div>
        <div class="stats-label">已完成考试</div>
      </div>
      <div class="stats-card orange hover-lift">
        <div class="stats-icon orange"><el-icon :size="22"><TrendCharts /></el-icon></div>
        <div class="stats-value">{{ data.stats.avgScore }}</div>
        <div class="stats-label">平均分</div>
      </div>
      <div class="stats-card purple hover-lift">
        <div class="stats-icon purple"><el-icon :size="22"><Trophy /></el-icon></div>
        <div class="stats-value">{{ data.stats.passRate }}%</div>
        <div class="stats-label">通过率</div>
      </div>
    </div>

    <!-- 学习打卡 -->
    <div class="checkin-card" v-if="data.user.id">
      <div class="checkin-info">
        <div class="checkin-flame">🔥</div>
        <div class="checkin-text">
          <div class="checkin-title">{{ data.checkin.checked ? '今日已打卡' : '今日还未打卡' }}</div>
          <div class="checkin-streak">已连续 {{ data.checkin.streakDays }} 天 · 坚持就是胜利</div>
        </div>
      </div>
      <el-button v-if="!data.checkin.checked" type="primary" round @click="doCheckin" :loading="data.checkin.loading">
        立即打卡
      </el-button>
      <div v-else class="checkin-done">
        <span class="checkin-done-icon">✓</span>
        <span>已打卡</span>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="quick-actions">
      <div class="qa-item" @click="router.push('/front/practiceMode')">
        <div class="qa-icon" style="background: linear-gradient(135deg, #00b42a, #00a81e);">🎯</div>
        <div class="qa-text">
          <div class="qa-title">刷题练习</div>
          <div class="qa-desc">随机组卷，即时批阅</div>
        </div>
        <el-icon class="qa-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="qa-item" @click="router.push('/front/wrongQuestions')">
        <div class="qa-icon" style="background: linear-gradient(135deg, #f53f3f, #ff4d4f);">📝</div>
        <div class="qa-text">
          <div class="qa-title">错题集</div>
          <div class="qa-desc">{{ data.stats.wrongCount || 0 }} 道错题待复习</div>
        </div>
        <el-icon class="qa-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="qa-item" @click="router.push('/front/examList')">
        <div class="qa-icon" style="background: linear-gradient(135deg, #409eff, #53a8ff);">📋</div>
        <div class="qa-text">
          <div class="qa-title">在线考试</div>
          <div class="qa-desc">{{ data.stats.available }} 场可参加</div>
        </div>
        <el-icon class="qa-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="qa-item" @click="router.push('/front/leaderboard')">
        <div class="qa-icon" style="background: linear-gradient(135deg, #ff7d00, #ff9500);">🏆</div>
        <div class="qa-text">
          <div class="qa-title">排行榜</div>
          <div class="qa-desc">查看学霸排名</div>
        </div>
        <el-icon class="qa-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 学科入口 -->
    <div class="card" style="margin-bottom: 24px">
      <div class="section-header">
        <div class="section-title">题库中心</div>
        <span class="section-more" @click="router.push('/front/subjects')">查看全部 →</span>
      </div>
      <div class="subject-grid">
        <div
          v-for="subject in data.subjects"
          :key="subject.id"
          class="subject-card"
          @click="goSubject(subject)"
        >
          <div class="subject-icon" :class="!subject.icon || subject.icon.startsWith('http') || subject.icon.includes('/files/') ? '' : (subject.icon || 'default')">
            <img v-if="subject.icon && (subject.icon.startsWith('http') || subject.icon.includes('/files/'))" :src="subject.icon" class="subject-img-icon" />
            <img v-else-if="getSubjectImg(subject.icon)" :src="getSubjectImg(subject.icon)" class="subject-img-icon" />
            <span v-else>{{ getSubjectEmoji(subject.icon) }}</span>
          </div>
          <div class="subject-name">{{ subject.name }}</div>
          <div class="subject-desc">{{ subject.description || '暂无描述' }}</div>
          <div class="subject-meta">
            <span>{{ subject.children?.length || 0 }} 个板块</span>
          </div>
          <div class="subject-tags" v-if="subject.children?.length">
            <span
              v-for="child in subject.children.slice(0, 4)"
              :key="child.id"
              class="subject-tag"
            >{{ child.name }}</span>
          </div>
          <div class="subject-enter">进入题库 <el-icon><ArrowRight /></el-icon></div>
        </div>
        <div v-if="!data.subjects.length" class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-icon">📚</div>
          <div class="empty-text">暂无题库数据</div>
        </div>
      </div>
    </div>

    <!-- 即将开始考试 -->
    <div class="card" style="margin-bottom: 24px" v-if="data.upcomingExams.length">
      <div class="section-header">
        <div class="section-title">即将开始的考试</div>
        <span class="section-more" @click="router.push('/front/examList')">全部考试 →</span>
      </div>
      <div class="upcoming-exams">
        <div
          v-for="exam in data.upcomingExams"
          :key="exam.id"
          class="upcoming-exam-card"
          @click="tryStartExam(exam)"
        >
          <div class="exam-status-dot" :class="getExamStatus(exam)"></div>
          <div class="uec-info">
            <div class="uec-name">{{ exam.name }}</div>
            <div class="uec-meta">{{ exam.paperName }} · {{ exam.duration }}分钟</div>
          </div>
          <el-tag
            v-if="getExamStatus(exam) === 'ongoing'"
            type="success"
            size="small"
          >进行中</el-tag>
          <el-tag
            v-else-if="getExamStatus(exam) === 'notStarted'"
            type="warning"
            size="small"
          >未开始</el-tag>
        </div>
      </div>
    </div>

    <!-- 最近成绩 + 公告 -->
    <div class="two-col-grid">
      <div class="card">
        <div class="section-header">
          <div class="section-title">最近成绩</div>
          <span class="section-more" @click="router.push('/front/myScores')">全部成绩 →</span>
        </div>
        <div v-if="data.recentScores.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <div class="empty-text">暂无考试成绩</div>
        </div>
        <div v-else class="score-card-list">
          <div v-for="score in data.recentScores" :key="score.id" class="score-card">
            <div class="score-circle" :class="getScoreClass(score.totalScore)">{{ score.totalScore || 0 }}</div>
            <div class="score-info">
              <div class="score-name">{{ score.examName }}</div>
              <el-tag :type="score.isPass ? 'success' : 'danger'" size="small" style="margin-top: 4px;">
                {{ score.isPass ? '通过' : '未通过' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="section-header">
          <div class="section-title">系统公告</div>
        </div>
        <div v-if="data.noticeData.length === 0" class="empty-state">
          <div class="empty-icon">📢</div>
          <div class="empty-text">暂无公告</div>
        </div>
        <div v-else class="notice-timeline">
          <div v-for="notice in data.noticeData.slice(0, 5)" :key="notice.id" class="notice-item">
            <div class="notice-dot"></div>
            <div class="notice-content">
              <div class="notice-text">{{ notice.content }}</div>
              <div class="notice-time">{{ notice.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 彩蛋 + 考试通过公示 -->
    <div class="two-col-grid" style="margin-top: 24px;" v-if="data.user.id">
      <div class="card">
        <div class="section-header">
          <div class="section-title">彩蛋探索者</div>
          <el-tag size="small" type="info">{{ data.easterLeaderboard.length }} 次发现</el-tag>
        </div>
        <div v-if="data.easterLeaderboard.length === 0" class="empty-state">
          <div class="empty-icon">🔮</div>
          <div class="empty-text">还没有人发现彩蛋，快去各处探索吧！</div>
        </div>
        <div v-else class="leaderboard-list">
          <div v-for="(record, index) in data.easterLeaderboard.slice(0, 10)" :key="record.id" class="leaderboard-item">
            <div class="leaderboard-rank">
              <span v-if="index < 3" class="rank-medal">{{ ['🥇','🥈','🥉'][index] }}</span>
              <span v-else class="rank-number">{{ index + 1 }}</span>
            </div>
            <div class="leaderboard-info">
              <div class="leaderboard-name">{{ record.userName || '匿名探索者' }}</div>
              <div class="leaderboard-time">{{ formatDateTime(record.discoverTime) }}</div>
            </div>
            <el-tag size="small" :type="getEggTagType(record.eggName)">{{ getEggLabel(record.eggName) }}</el-tag>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="section-header">
          <div class="section-title">考试通过公示</div>
        </div>
        <div v-if="data.publicResults.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-text">暂无考试通过记录</div>
        </div>
        <div v-else class="public-result-list">
          <div v-for="record in data.publicResults.slice(0, 10)" :key="record.id" class="public-result-item">
            <div class="result-indicator" :class="record.isPass ? 'pass' : 'fail'">
              <span>{{ record.isPass ? '✓' : '✗' }}</span>
            </div>
            <div class="result-info">
              <div class="result-name">{{ record.studentName }}</div>
              <div class="result-exam">{{ record.examName }}</div>
            </div>
            <div class="result-status">
              <el-tag :type="record.isPass ? 'success' : 'danger'" size="small">
                {{ record.isPass ? '通过' : '未通过' }}
              </el-tag>
              <div class="result-time">{{ formatDateTime(record.submitTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习小贴士悬浮球 -->
    <div class="study-tip-float" @click="data.tipExpanded = !data.tipExpanded">
      <span class="tip-icon">{{ data.currentTip?.icon || '💡' }}</span>
      <div class="tip-bubble" v-if="data.tipExpanded">
        <div class="tip-content">{{ data.currentTip?.text }}</div>
        <div class="tip-footer">
          <span class="tip-next" @click.stop="nextTip">下一条 →</span>
          <span class="tip-close" @click.stop="data.tipExpanded = false">×</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request.js";
import router from "@/router/index.js";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  stats: { available: 0, completed: 0, avgScore: 0, passRate: 0, wrongCount: 0 },
  subjects: [],
  upcomingExams: [],
  recentScores: [],
  noticeData: [],
  easterLeaderboard: [],
  publicResults: [],
  checkin: { checked: false, streakDays: 0, loading: false },
  learningProgress: 0,
  missions: [
    { id: 1, name: '完成一次练习', desc: '进入练习模式完成至少1题', reward: '+5分', done: false, action: () => router.push('/front/practiceMode'), color: '#00b42a' },
    { id: 2, name: '阅读一篇教程', desc: '学习新知识，巩固基础', reward: '+3分', done: false, action: () => router.push('/front/tutorials'), color: '#409eff' },
    { id: 3, name: '复习错题', desc: '回顾错题，加深理解', reward: '+2分', done: false, action: () => router.push('/front/wrongQuestions'), color: '#ff7d00' },
  ],
  practiceTrend: [],
  maxTrendCount: 1,
  totalWeeklyPractice: 0,
  weeklyChange: 0,
  tipExpanded: false,
  currentTip: null,
  tips: [
    { icon: '🧠', text: '学习时多思考为什么，而不是死记硬背。' },
    { icon: '📖', text: '每天坚持学习一点点，积少成多！' },
    { icon: '💡', text: '做错的题才是最好的老师，一定要弄懂它。' },
    { icon: '⏰', text: '番茄工作法：学习25分钟，休息5分钟，效率翻倍！' },
    { icon: '🎯', text: '设定明确的学习目标，比漫无目的地刷题更有效。' },
    { icon: '🔄', text: '定期复习旧知识，巩固记忆曲线。' },
    { icon: '📝', text: '做笔记时用自己的话复述，比抄写更有效。' },
    { icon: '🏃', text: '适度运动可以提高学习效率和专注力！' },
  ],
})

const todayStr = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
})

const getGreeting = () => {
  const greetings = [
    '欢迎回来', '元气满满', '继续加油', '学习愉快', '突破自我',
  ]
  return greetings[Math.floor(Math.random() * greetings.length)]
}

const computeLearningProgress = () => {
  const missionDone = data.missions.filter(m => m.done).length
  const checkinDone = data.checkin.checked ? 1 : 0
  const examDone = data.stats.completed > 0 ? 1 : 0
  const practiceDone = data.totalWeeklyPractice > 0 ? 1 : 0
  const total = data.missions.length + 3
  const done = missionDone + checkinDone + examDone + practiceDone
  data.learningProgress = Math.round((done / total) * 100)
}

const doMission = (mission) => {
  if (mission.done) { ElMessage.info('该任务已完成！'); return }
  mission.action()
}

const nextTip = () => {
  const idx = data.tips.indexOf(data.currentTip)
  data.currentTip = data.tips[(idx + 1) % data.tips.length]
}

const loadPracticeTrend = () => {
  request.get('/practice/trend', {
    params: { userId: data.user.id, userRole: data.user.role }
  }).then(res => {
    if (res.code === '200' && res.data) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const now = new Date()
      data.practiceTrend = []
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now)
        d.setDate(d.getDate() - i)
        const dateStr = `${d.getMonth() + 1}/${d.getDate()}`
        const count = res.data[dateStr] || 0
        data.practiceTrend.push({ label: days[d.getDay()], count })
      }
      data.maxTrendCount = Math.max(...data.practiceTrend.map(t => t.count), 1)
      data.totalWeeklyPractice = data.practiceTrend.reduce((s, t) => s + t.count, 0)
    }
  }).catch(() => {
    data.practiceTrend = []
    data.maxTrendCount = 1
    data.totalWeeklyPractice = 0
    data.weeklyChange = 0
  })
}

const initTip = () => {
  data.currentTip = data.tips[Math.floor(Math.random() * data.tips.length)]
}

const getSubjectEmoji = (icon) => {
  const map = { math: '📐', writing: '📝', computer: '💻', programming: '⌨️' }
  return map[icon] || '📚'
}

import creeperImg from '@/assets/imgs/creeper.png'
import redstoneImg from '@/assets/imgs/redstone.png'

const getSubjectImg = (icon) => {
  const map = { minecraft: creeperImg, redstone: redstoneImg }
  return map[icon] || ''
}

const getExamStatus = (exam) => {
  if (exam.examType === 'permanent') return 'ongoing'
  const now = new Date()
  const start = exam.startTime ? new Date(exam.startTime) : null
  const end = exam.endTime ? new Date(exam.endTime) : null
  if (start && now < start) return 'notStarted'
  if (end && now > end) return 'ended'
  return 'ongoing'
}

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 60) return 'average'
  return 'poor'
}

const goSubject = (subject) => {
  router.push({ path: '/front/subjects', query: { subjectId: subject.id } })
}

const tryStartExam = (exam) => {
  if (getExamStatus(exam) === 'ongoing') {
    router.push({ path: '/front/examTaking', query: { examId: exam.id } })
  }
}

const loadSubjects = () => {
  request.get('/questionCategory/selectTree').then(res => {
    if (res.code === '200') {
      data.subjects = res.data || []
    }
  })
}

const loadExams = () => {
  request.get('/exam/getAvailableExams/' + data.user.id).then(res => {
    if (res.code === '200') {
      const exams = res.data || []
      data.stats.available = exams.filter(e => getExamStatus(e) !== 'ended').length
      data.upcomingExams = exams
        .filter(e => getExamStatus(e) !== 'ended')
        .sort((a, b) => {
          const statusOrder = { ongoing: 0, notStarted: 1 }
          return (statusOrder[getExamStatus(a)] || 2) - (statusOrder[getExamStatus(b)] || 2)
        })
        .slice(0, 4)
    }
  })
}

const loadScores = () => {
  request.get('/score/getByStudentId/' + data.user.id).then(res => {
    if (res.code === '200') {
      const scores = res.data || []
      data.stats.completed = scores.length
      data.recentScores = scores.slice(0, 4)
      if (scores.length) {
        const total = scores.reduce((s, r) => s + (r.totalScore || 0), 0)
        data.stats.avgScore = Math.round(total / scores.length)
        const passed = scores.filter(r => r.isPass).length
        data.stats.passRate = Math.round((passed / scores.length) * 100)
      }
    }
  })
}

const loadWrongCount = () => {
  request.get('/wrongQuestion/selectByUser', {
    params: { userId: data.user.id, userRole: data.user.role }
  }).then(res => {
    if (res.code === '200') data.stats.wrongCount = (res.data || []).length
  })
}

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    data.noticeData = res.data || []
  })
}

const loadEasterLeaderboard = () => {
  request.get('/easterEgg/leaderboard').then(res => {
    if (res.code === '200') {
      data.easterLeaderboard = res.data || []
    }
  })
}

const loadPublicResults = () => {
  request.get('/examRecord/publicResults').then(res => {
    if (res.code === '200') {
      data.publicResults = res.data || []
    }
  })
}

const eggLabels = {
  logo_click_10: 'Logo十连击',
  login_konami: 'Konami密码',
  register_triple_click: '三击摩斯码',
  workflow_dance: '工作流舞者',
  dashboard_combo: '顺序大师',
  logo_click: 'Logo点击',
}
const getEggLabel = (name) => eggLabels[name] || name || '未知彩蛋'
const getEggTagType = (name) => {
  const map = { logo_click_10: '', login_konami: 'danger', register_triple_click: 'warning', workflow_dance: 'success', dashboard_combo: 'info' }
  return map[name] || ''
}

const formatDateTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const loadCheckinStatus = () => {
  if (!data.user.id) return
  request.get('/studyCheckin/status', {
    params: { userId: data.user.id, userRole: data.user.role }
  }).then(res => {
    if (res.code === '200') {
      data.checkin.checked = res.data.todayChecked || false
      data.checkin.streakDays = res.data.streakDays || 0
    }
  })
}

const doCheckin = () => {
  data.checkin.loading = true
  request.post('/studyCheckin/checkin', {
    userId: data.user.id,
    userRole: data.user.role
  }).then(res => {
    if (res.code === '200') {
      data.checkin.checked = true
      data.checkin.streakDays = res.data.streakDays || data.checkin.streakDays + 1
      ElMessage.success({ message: `打卡成功！已连续 ${data.checkin.streakDays} 天 🎉`, duration: 3000 })
    } else {
      ElMessage.error(res.msg || '打卡失败')
    }
  }).finally(() => { data.checkin.loading = false })
}

onMounted(() => {
  loadSubjects()
  loadExams()
  loadScores()
  loadNotice()
  loadEasterLeaderboard()
  loadPublicResults()
  loadWrongCount()
  loadCheckinStatus()
  loadPracticeTrend()
  initTip()
  setTimeout(() => {
    if (data.stats.completed > 0) data.missions[0].done = true
    computeLearningProgress()
  }, 1500)
})
</script>

<style scoped>
@import "@/assets/css/front.css";

/* 欢迎横幅扩展 */
.welcome-banner .welcome-right {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.welcome-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255,255,255,0.08);
  padding: 10px 18px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.1);
}

.ws-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ws-val {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
}

.ws-lbl {
  font-size: 10px;
  color: rgba(255,255,255,0.6);
  margin-top: 1px;
}

.ws-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.15);
}

.progress-wrap {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s;
}
.progress-wrap:hover { transform: scale(1.08); }

.checkin-done {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.checkin-done-icon {
  font-size: 14px;
  font-weight: 700;
}

/* 欢迎横幅内的进度环 */
.spr-svg { width: 68px; height: 68px; transform: rotate(-90deg); }
.spr-bg { fill: none; stroke: rgba(255,255,255,0.12); stroke-width: 5; }
.spr-fill {
  fill: none; stroke: var(--primary-color); stroke-width: 5;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.8s ease;
}
.spr-text {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.spr-value { font-size: 13px; font-weight: 700; color: #fff; line-height: 1; }
.spr-label { font-size: 9px; color: rgba(255,255,255,0.6); margin-top: 1px; }

/* 两列布局 */
.two-col-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 900px) {
  .two-col-grid { grid-template-columns: 1fr; }
  .welcome-banner .welcome-right { flex-direction: column; gap: 12px; }
}
</style>
