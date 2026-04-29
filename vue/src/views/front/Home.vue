<template>
  <div class="student-home app-page-shell">
    <section class="student-hero">
      <div>
        <p>{{ todayStr }}</p>
        <h1>{{ greeting }}，{{ data.user.name || "玩家" }}</h1>
        <span>完成题库复习、模拟练习和正式进服审核。</span>
      </div>
      <div class="hero-progress" @click="router.push('/front/myScores')">
        <svg viewBox="0 0 64 64">
          <circle cx="32" cy="32" r="25" class="ring-bg" />
          <circle cx="32" cy="32" r="25" class="ring-fill" :stroke-dasharray="157" :stroke-dashoffset="157 - (157 * data.learningProgress / 100)" />
        </svg>
        <strong>{{ data.learningProgress }}%</strong>
        <small>准备度</small>
      </div>
    </section>

    <section class="metric-grid">
      <button v-for="item in metrics" :key="item.label" type="button" class="metric-card" @click="router.push(item.path)">
        <span class="metric-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.label }}</small>
      </button>
    </section>

    <section class="quick-grid">
      <button v-for="action in quickActions" :key="action.path" type="button" @click="router.push(action.path)">
        <span :class="action.tone">
          <el-icon><component :is="action.icon" /></el-icon>
        </span>
        <strong>{{ action.title }}</strong>
        <small>{{ action.desc }}</small>
        <el-icon class="quick-arrow"><ArrowRight /></el-icon>
      </button>
    </section>

    <section class="student-grid">
      <div class="panel-card wide-panel">
        <div class="panel-header">
          <div>
            <span>审核题库</span>
            <h2>按方向浏览复习材料</h2>
          </div>
          <el-button link type="primary" @click="router.push('/front/subjects')">查看全部</el-button>
        </div>
        <div v-if="data.subjects.length" class="subject-grid">
          <button v-for="subject in data.subjects.slice(0, 6)" :key="subject.id" type="button" class="subject-card" @click="goSubject(subject)">
            <span class="subject-icon">
              <img v-if="getSubjectImg(subject.icon)" :src="getSubjectImg(subject.icon)" alt="" />
              <el-icon v-else><Reading /></el-icon>
            </span>
            <strong>{{ subject.name }}</strong>
            <small>{{ subject.description || "暂无描述" }}</small>
            <em>{{ subject.children?.length || 0 }} 个板块</em>
          </button>
        </div>
        <el-empty v-else description="暂无题库数据" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>即将开始</span>
            <h2>可参加的进服审核</h2>
          </div>
          <el-button link type="primary" @click="router.push('/front/examList')">全部审核</el-button>
        </div>
        <div v-if="data.upcomingExams.length" class="exam-list">
          <button v-for="exam in data.upcomingExams" :key="exam.id" type="button" class="exam-row" @click="tryStartExam(exam)">
            <span class="status-dot" :class="getExamStatus(exam)"></span>
            <span>
              <strong>{{ exam.name }}</strong>
              <small>{{ exam.paperName || "未绑定试卷" }} · {{ exam.duration || 0 }} 分钟</small>
            </span>
            <el-tag :type="getExamStatus(exam) === 'ongoing' ? 'success' : 'warning'" size="small">
              {{ getExamStatus(exam) === "ongoing" ? "进行中" : "未开始" }}
            </el-tag>
          </button>
        </div>
        <el-empty v-else description="暂无可参加审核" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>最近结果</span>
            <h2>你的审核成绩</h2>
          </div>
          <el-button link type="primary" @click="router.push('/front/myScores')">全部结果</el-button>
        </div>
        <div v-if="data.recentScores.length" class="score-list">
          <article v-for="score in data.recentScores" :key="score.id" class="score-row">
            <span class="score-circle" :class="getScoreClass(score.totalScore)">{{ score.totalScore || 0 }}</span>
            <span>
              <strong>{{ score.examName }}</strong>
              <small>{{ score.isPass ? "已通过" : "未通过" }}</small>
            </span>
            <el-tag :type="score.isPass ? 'success' : 'danger'" size="small">{{ score.isPass ? "通过" : "未通过" }}</el-tag>
          </article>
        </div>
        <el-empty v-else description="暂无审核结果" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>系统公告</span>
            <h2>最近通知</h2>
          </div>
        </div>
        <div v-if="data.noticeData.length" class="notice-list">
          <article v-for="notice in data.noticeData.slice(0, 5)" :key="notice.id || notice.time" class="notice-item">
            <strong>{{ notice.title || "公告" }}</strong>
            <p>{{ notice.content }}</p>
            <small>{{ formatDateTime(notice.time) }}</small>
          </article>
        </div>
        <el-empty v-else description="暂无公告" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>通过公示</span>
            <h2>近期审核通过记录</h2>
          </div>
        </div>
        <div v-if="data.publicResults.length" class="public-list">
          <article v-for="record in data.publicResults.slice(0, 6)" :key="record.id" class="public-row">
            <span class="public-badge" :class="{ pass: record.isPass }">{{ record.isPass ? "过" : "未" }}</span>
            <span>
              <strong>{{ record.studentName }}</strong>
              <small>{{ record.examName }}</small>
            </span>
            <small>{{ formatDateTime(record.submitTime) }}</small>
          </article>
        </div>
        <el-empty v-else description="暂无公示记录" :image-size="90" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import {
  ArrowRight,
  CircleCheck,
  Collection,
  DataAnalysis,
  Edit,
  EditPen,
  Reading,
  Trophy,
  WarnTriangleFilled,
} from "@element-plus/icons-vue"
import request from "@/utils/request.js"
import router from "@/router/index.js"
import creeperImg from "@/assets/imgs/creeper.png"
import redstoneImg from "@/assets/imgs/redstone.png"

const data = reactive({
  user: JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}"),
  stats: { available: 0, completed: 0, avgScore: 0, passRate: 0, wrongCount: 0 },
  subjects: [],
  upcomingExams: [],
  recentScores: [],
  noticeData: [],
  publicResults: [],
  learningProgress: 0,
})

const todayStr = new Date().toLocaleDateString("zh-CN", { year: "numeric", month: "long", day: "numeric", weekday: "long" })
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return "上午好"
  if (hour < 18) return "下午好"
  return "晚上好"
})

const metrics = computed(() => [
  { label: "可参加审核", value: data.stats.available, path: "/front/examList", icon: "EditPen", tone: "blue" },
  { label: "已完成审核", value: data.stats.completed, path: "/front/myScores", icon: "CircleCheck", tone: "green" },
  { label: "平均分", value: data.stats.avgScore, path: "/front/myScores", icon: "DataAnalysis", tone: "orange" },
  { label: "通过率", value: `${data.stats.passRate}%`, path: "/front/myScores", icon: "Trophy", tone: "purple" },
  { label: "错题数", value: data.stats.wrongCount, path: "/front/wrongQuestions", icon: "WarnTriangleFilled", tone: "red" },
])

const quickActions = computed(() => [
  { title: "审核题库", desc: "按方向复习规则", path: "/front/subjects", icon: "Collection", tone: "blue" },
  { title: "正式审核", desc: `${data.stats.available} 场可参加`, path: "/front/examList", icon: "EditPen", tone: "green" },
  { title: "审核模拟", desc: "进入练习模式", path: "/front/practiceMode", icon: "Edit", tone: "orange" },
  { title: "错题复盘", desc: `${data.stats.wrongCount} 道待回看`, path: "/front/wrongQuestions", icon: "WarnTriangleFilled", tone: "red" },
])

const computeLearningProgress = () => {
  const done = [
    data.stats.completed > 0,
    data.stats.available > 0,
    data.stats.avgScore >= 60,
    data.stats.wrongCount === 0 && data.stats.completed > 0,
  ].filter(Boolean).length
  data.learningProgress = Math.round((done / 4) * 100)
}

const getSubjectImg = (icon) => {
  const map = { minecraft: creeperImg, redstone: redstoneImg }
  if (icon && (icon.startsWith?.("http") || icon.includes?.("/files/"))) return icon
  return map[icon] || ""
}

const getExamStatus = (exam) => {
  if (exam.examType === "permanent") return "ongoing"
  const now = new Date()
  const start = exam.startTime ? new Date(exam.startTime) : null
  const end = exam.endTime ? new Date(exam.endTime) : null
  if (start && now < start) return "notStarted"
  if (end && now > end) return "ended"
  return "ongoing"
}

const getScoreClass = (score) => {
  if (score >= 90) return "excellent"
  if (score >= 80) return "good"
  if (score >= 60) return "average"
  return "poor"
}

const goSubject = (subject) => {
  router.push({ path: "/front/subjects", query: { subjectId: subject.id } })
}

const tryStartExam = (exam) => {
  if (getExamStatus(exam) === "ongoing") {
    router.push({ path: "/front/examTaking", query: { examId: exam.id } })
  } else {
    ElMessage.info("该审核尚未开始")
  }
}

const formatDateTime = (dt) => {
  if (!dt) return "未设置时间"
  const d = new Date(dt)
  if (Number.isNaN(d.getTime())) return String(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`
}

const loadSubjects = () => {
  request.get("/questionCategory/selectTree").then(res => {
    if (res.code === "200") data.subjects = res.data || []
  }).catch(() => {})
}

const loadExams = () => {
  if (!data.user.id) return
  request.get("/exam/getAvailableExams/" + data.user.id).then(res => {
    if (res.code === "200") {
      const exams = res.data || []
      data.stats.available = exams.filter(item => getExamStatus(item) !== "ended").length
      data.upcomingExams = exams
        .filter(item => getExamStatus(item) !== "ended")
        .sort((a, b) => {
          const order = { ongoing: 0, notStarted: 1, ended: 2 }
          return order[getExamStatus(a)] - order[getExamStatus(b)]
        })
        .slice(0, 5)
      computeLearningProgress()
    }
  }).catch(() => {})
}

const loadScores = () => {
  if (!data.user.id) return
  request.get("/score/getByStudentId/" + data.user.id).then(res => {
    if (res.code === "200") {
      const scores = res.data || []
      data.stats.completed = scores.length
      data.recentScores = scores.slice(0, 4)
      if (scores.length) {
        const total = scores.reduce((sum, item) => sum + (item.totalScore || 0), 0)
        data.stats.avgScore = Math.round(total / scores.length)
        data.stats.passRate = Math.round((scores.filter(item => item.isPass).length / scores.length) * 100)
      }
      computeLearningProgress()
    }
  }).catch(() => {})
}

const loadWrongCount = () => {
  request.get("/wrongQuestion/selectByUser").then(res => {
    if (res.code === "200") {
      data.stats.wrongCount = (res.data || []).length
      computeLearningProgress()
    }
  }).catch(() => {})
}

const loadNotice = () => {
  request.get("/notice/selectAll").then(res => {
    if (res.code === "200") data.noticeData = res.data || []
  }).catch(() => {})
}

const loadPublicResults = () => {
  request.get("/examRecord/publicResults").then(res => {
    if (res.code === "200") data.publicResults = res.data || []
  }).catch(() => {})
}

onMounted(() => {
  loadSubjects()
  loadExams()
  loadScores()
  loadWrongCount()
  loadNotice()
  loadPublicResults()
})
</script>

<style scoped>
.student-home {
  display: grid;
  gap: 20px;
}

.student-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 34px;
  border-radius: 8px;
  background:
    linear-gradient(110deg, rgba(9, 18, 30, 0.9), rgba(9, 18, 30, 0.58)),
    url("@/assets/imgs/bj2.png") center / cover;
  color: #fff;
}

.student-hero p,
.student-hero span,
.hero-progress small {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
}

.student-hero h1 {
  margin: 8px 0;
  color: #fff;
  font-size: 30px;
}

.hero-progress {
  position: relative;
  width: 88px;
  height: 88px;
  flex-shrink: 0;
  cursor: pointer;
}

.hero-progress svg {
  width: 88px;
  height: 88px;
  transform: rotate(-90deg);
}

.ring-bg,
.ring-fill {
  fill: none;
  stroke-width: 7;
}

.ring-bg {
  stroke: rgba(255, 255, 255, 0.18);
}

.ring-fill {
  stroke: #4ade80;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.6s;
}

.hero-progress strong,
.hero-progress small {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
}

.hero-progress strong {
  top: 27px;
  color: #fff;
  font-size: 18px;
}

.hero-progress small {
  top: 50px;
  font-size: 11px;
}

.metric-card {
  text-align: left;
  cursor: pointer;
}

.metric-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  border-radius: 8px;
  font-size: 22px;
}

.metric-icon.blue,
.quick-grid .blue { color: #2563eb; background: #dbeafe; }
.metric-icon.green,
.quick-grid .green { color: #16a34a; background: #dcfce7; }
.metric-icon.orange,
.quick-grid .orange { color: #d97706; background: #fef3c7; }
.metric-icon.purple { color: #7c3aed; background: #ede9fe; }
.metric-icon.red,
.quick-grid .red { color: #dc2626; background: #fee2e2; }

.metric-card strong {
  display: block;
  color: var(--text-primary);
  font-size: 28px;
}

.metric-card small {
  color: var(--text-secondary);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.quick-grid button {
  position: relative;
  min-height: 122px;
  padding: 18px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-card);
  color: var(--text-primary);
  text-align: left;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.quick-grid button:hover {
  border-color: rgba(var(--primary-rgb), 0.35);
}

.quick-grid button > span {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  border-radius: 8px;
}

.quick-grid strong,
.subject-card strong,
.exam-row strong,
.score-row strong,
.public-row strong,
.notice-item strong {
  display: block;
  color: var(--text-primary);
  font-size: 14px;
}

.quick-grid small,
.subject-card small,
.exam-row small,
.score-row small,
.public-row small,
.notice-item small {
  color: var(--text-secondary);
  font-size: 12px;
}

.quick-arrow {
  position: absolute;
  right: 16px;
  bottom: 16px;
  color: var(--text-secondary);
}

.student-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 20px;
}

.panel-card {
  padding: 22px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
}

.wide-panel {
  grid-row: span 2;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-header span {
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 900;
}

.panel-header h2 {
  margin: 4px 0 0;
  color: var(--text-primary);
  font-size: 17px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.subject-card,
.exam-row {
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-page);
  color: var(--text-primary);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.subject-card {
  min-height: 166px;
  padding: 16px;
}

.subject-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  border-radius: 8px;
  background: var(--primary-light);
  color: var(--primary-color);
  font-size: 22px;
}

.subject-icon img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.subject-card em {
  display: inline-block;
  margin-top: 12px;
  color: var(--primary-color);
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.exam-list,
.score-list,
.notice-list,
.public-list {
  display: grid;
  gap: 10px;
}

.exam-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.status-dot.ongoing { background: #16a34a; box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.12); }
.status-dot.notStarted { background: #d97706; box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.12); }
.status-dot.ended { background: #94a3b8; }

.score-row,
.public-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);
}

.score-row:last-child,
.public-row:last-child {
  border-bottom: 0;
}

.score-circle,
.public-badge {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 900;
}

.score-circle.excellent { color: #16a34a; background: #dcfce7; }
.score-circle.good { color: #2563eb; background: #dbeafe; }
.score-circle.average { color: #d97706; background: #fef3c7; }
.score-circle.poor { color: #dc2626; background: #fee2e2; }
.public-badge { color: #dc2626; background: #fee2e2; }
.public-badge.pass { color: #16a34a; background: #dcfce7; }

.notice-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);
}

.notice-item:last-child {
  border-bottom: 0;
}

.notice-item p {
  display: -webkit-box;
  margin: 6px 0;
  overflow: hidden;
  color: var(--text-regular);
  font-size: 13px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

@media (max-width: 1080px) {
  .student-grid {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .student-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .subject-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
