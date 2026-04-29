<template>
  <div class="admin-home app-page-shell">
    <section class="admin-hero">
      <div>
        <p>{{ greeting }}，{{ data.user?.name || "管理员" }}</p>
        <h1>管理控制台</h1>
        <span>{{ today }} · {{ roleLabel }}</span>
      </div>
      <div class="hero-clock">
        <strong>{{ clockTime }}</strong>
        <small>{{ clockDate }}</small>
      </div>
    </section>

    <section class="metric-grid">
      <button v-for="item in metrics" :key="item.label" type="button" class="metric-card" @click="$router.push(item.path)">
        <span class="metric-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.label }}</small>
      </button>
    </section>

    <section class="admin-grid">
      <div class="panel-card todo-panel">
        <div class="panel-header">
          <div>
            <span>待办事项</span>
            <h2>需要处理的审核工作</h2>
          </div>
          <el-tag v-if="pendingCount" type="danger">{{ pendingCount }}</el-tag>
        </div>
        <div v-if="visibleTodos.length" class="todo-list">
          <button v-for="todo in visibleTodos" :key="todo.id" type="button" class="todo-item" @click="$router.push(todo.path)">
            <span class="todo-icon" :class="{ urgent: todo.urgent }">
              <el-icon><component :is="todo.icon" /></el-icon>
            </span>
            <span>
              <strong>{{ todo.text }}</strong>
              <small>{{ todo.meta }}</small>
            </span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <el-empty v-else description="当前没有待办" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>快捷入口</span>
            <h2>常用管理操作</h2>
          </div>
        </div>
        <div class="shortcut-grid">
          <button v-for="item in shortcuts" :key="item.path" type="button" @click="$router.push(item.path)">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </div>
      </div>

      <div class="panel-card wide-panel">
        <div class="panel-header">
          <div>
            <span>近期审核</span>
            <h2>最新创建或发布的审核</h2>
          </div>
          <el-button link type="primary" @click="$router.push('/manager/exam')">查看全部</el-button>
        </div>
        <div v-if="data.recentExams.length" class="exam-list">
          <button v-for="exam in data.recentExams" :key="exam.id" type="button" class="exam-row" @click="$router.push('/manager/exam')">
            <span class="status-dot" :class="getExamDotClass(exam)"></span>
            <span class="exam-main">
              <strong>{{ exam.name }}</strong>
              <small>{{ exam.paperName || "未绑定试卷" }} · {{ formatShortTime(exam.startTime) || "常驻/未设置时间" }}</small>
            </span>
            <el-tag :type="getExamTagType(exam)" size="small">{{ getExamStatusText(exam) }}</el-tag>
          </button>
        </div>
        <el-empty v-else description="暂无审核安排" :image-size="90" />
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <div>
            <span>系统状态</span>
            <h2>当前运行概览</h2>
          </div>
        </div>
        <div class="status-list">
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>后端服务</span>
            <strong>正常</strong>
          </div>
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>数据库</span>
            <strong>正常</strong>
          </div>
          <div class="status-item">
            <span class="status-dot" :class="pendingApprovalCount > 0 ? 'warn' : 'online'"></span>
            <span>注册审批</span>
            <strong>{{ pendingApprovalCount }} 条</strong>
          </div>
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>页面运行</span>
            <strong>{{ uptimeStr }}</strong>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from "vue"
import request from "@/utils/request.js"
import {
  ArrowRight,
  Avatar,
  Calendar,
  DataLine,
  Document,
  EditPen,
  Finished,
  List,
  Tools,
  User,
} from "@element-plus/icons-vue"

const data = reactive({
  user: JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}"),
  recentExams: [],
  stats: { users: 0, admins: 0, exams: 0, questions: 0 },
})

const pendingApprovalCount = ref(0)
const startTime = Date.now()
const totalUsers = computed(() => data.stats.users + data.stats.admins)

const roleLabel = computed(() => (data.user.role === "USER" ? "玩家" : "管理员"))
const today = new Date().toLocaleDateString("zh-CN", { year: "numeric", month: "long", day: "numeric", weekday: "long" })
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return "凌晨好"
  if (h < 12) return "上午好"
  if (h < 14) return "中午好"
  if (h < 18) return "下午好"
  return "晚上好"
})

const clockTime = ref("")
const clockDate = ref("")
let clockTimer = null

const updateClock = () => {
  const now = new Date()
  clockTime.value = now.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false })
  clockDate.value = now.toLocaleDateString("zh-CN", { month: "long", day: "numeric", weekday: "short" })
}

const uptimeStr = computed(() => {
  const diff = Math.floor((Date.now() - startTime) / 1000)
  if (diff < 60) return `${diff}秒`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟`
  return `${Math.floor(diff / 3600)}小时${Math.floor((diff % 3600) / 60)}分`
})

const metrics = computed(() => [
  { label: "总成员", value: totalUsers.value, path: "/manager/student", icon: "Avatar", tone: "blue" },
  { label: "管理员", value: data.stats.admins, path: "/manager/admin", icon: "Tools", tone: "purple" },
  { label: "玩家", value: data.stats.users, path: "/manager/student", icon: "User", tone: "green" },
  { label: "审核", value: data.stats.exams, path: "/manager/exam", icon: "Calendar", tone: "cyan" },
  { label: "题目", value: data.stats.questions, path: "/manager/question", icon: "EditPen", tone: "red" },
])

const shortcuts = [
  { label: "审核管理", path: "/manager/exam", icon: "Document" },
  { label: "结果管理", path: "/manager/score", icon: "DataLine" },
  { label: "批阅中心", path: "/exam/gradingCenter", icon: "Finished" },
  { label: "题目管理", path: "/manager/question", icon: "EditPen" },
  { label: "玩家管理", path: "/manager/student", icon: "User" },
]

const todoItems = reactive([
  { id: 1, text: "审批注册申请", meta: "", icon: "User", path: "/manager/registrationApproval", urgent: false },
  { id: 2, text: "批阅主观题", meta: "", icon: "Finished", path: "/exam/gradingCenter", urgent: false },
  { id: 3, text: "审核题目贡献", meta: "", icon: "List", path: "/manager/questionReview", urgent: false },
])

const visibleTodos = computed(() => todoItems.filter(item => item.meta))
const pendingCount = computed(() => visibleTodos.value.length)
const silent = { silentError: true }

const getExamDotClass = (exam) => {
  if (exam.examType === "permanent") return "online"
  const now = new Date()
  const start = exam.startTime ? new Date(exam.startTime) : null
  const end = exam.endTime ? new Date(exam.endTime) : null
  if (start && now < start) return "warn"
  if (end && now > end) return "ended"
  return "online"
}

const getExamTagType = (exam) => {
  if (exam.examType === "permanent") return "success"
  if (exam.status === "draft") return "info"
  if (exam.status === "finished") return "warning"
  return "primary"
}

const getExamStatusText = (exam) => {
  if (exam.examType === "permanent") return "常驻"
  const map = { draft: "草稿", published: "已发布", ongoing: "进行中", finished: "已结束", cancelled: "已取消" }
  return map[exam.status] || exam.status || "未设置"
}

const formatShortTime = (dt) => {
  if (!dt) return ""
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`
}

const loadStats = () => {
  request.get("/student/selectPage", { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === "200") data.stats.users = res.data?.total || 0 }).catch(() => {})
  request.get("/admin/selectPage", { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === "200") data.stats.admins = res.data?.total || 0 }).catch(() => {})
  request.get("/exam/selectPage", { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === "200") data.stats.exams = res.data?.total || 0 }).catch(() => {})
  request.get("/question/selectPage", { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === "200") data.stats.questions = res.data?.total || 0 }).catch(() => {})
}

const loadRecentExams = () => {
  request.get("/exam/selectAll", silent).then(res => {
    if (res.code === "200") {
      data.recentExams = (res.data || [])
        .sort((a, b) => String(b.createdAt || b.id || "").localeCompare(String(a.createdAt || a.id || "")))
        .slice(0, 6)
    }
  }).catch(() => {})
}

const loadTodoItems = () => {
  request.get("/registration/selectPage", { ...silent, params: { pageNum: 1, pageSize: 1, status: "PENDING" } }).then(res => {
    if (res.code === "200") {
      const count = res.data?.total || 0
      pendingApprovalCount.value = count
      todoItems[0].meta = count > 0 ? `${count} 条待审批` : ""
      todoItems[0].urgent = count > 5
    }
  }).catch(() => {})
  request.get("/grading/countPending", silent).then(res => {
    if (res.code === "200") {
      const count = res.data || 0
      todoItems[1].meta = count > 0 ? `${count} 份待批阅` : ""
      todoItems[1].urgent = count > 10
    }
  }).catch(() => {})
  request.get("/questionContribution/pendingCount", silent).then(res => {
    if (res.code === "200") {
      const count = res.data || 0
      todoItems[2].meta = count > 0 ? `${count} 题待审核` : ""
      todoItems[2].urgent = count > 20
    }
  }).catch(() => {})
}

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  loadStats()
  loadRecentExams()
  loadTodoItems()
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style scoped>
.admin-home {
  display: grid;
  gap: 20px;
}

.admin-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
  border-radius: 8px;
  background:
    linear-gradient(110deg, rgba(10, 20, 32, 0.92), rgba(10, 20, 32, 0.68)),
    url("@/assets/imgs/bj1.png") center / cover;
  color: #fff;
}

.admin-hero p,
.admin-hero span,
.hero-clock small {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
}

.admin-hero h1 {
  margin: 8px 0;
  color: #fff;
  font-size: 30px;
}

.hero-clock {
  min-width: 180px;
  text-align: right;
}

.hero-clock strong {
  display: block;
  color: #fff;
  font-size: 34px;
  font-variant-numeric: tabular-nums;
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

.metric-icon.blue { color: #2563eb; background: #dbeafe; }
.metric-icon.purple { color: #7c3aed; background: #ede9fe; }
.metric-icon.orange { color: #d97706; background: #fef3c7; }
.metric-icon.green { color: #16a34a; background: #dcfce7; }
.metric-icon.cyan { color: #0891b2; background: #cffafe; }
.metric-icon.red { color: #dc2626; background: #fee2e2; }

.metric-card strong {
  display: block;
  color: var(--text-primary);
  font-size: 28px;
}

.metric-card small {
  color: var(--text-secondary);
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(300px, 0.75fr);
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

.todo-list,
.exam-list,
.status-list {
  display: grid;
  gap: 10px;
}

.todo-item,
.exam-row,
.shortcut-grid button {
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-page);
  color: var(--text-primary);
  font: inherit;
  cursor: pointer;
}

.todo-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  text-align: left;
}

.todo-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--primary-light);
  color: var(--primary-color);
}

.todo-icon.urgent {
  background: #fee2e2;
  color: #dc2626;
}

.todo-item strong,
.exam-main strong {
  display: block;
  color: var(--text-primary);
  font-size: 14px;
}

.todo-item small,
.exam-main small {
  color: var(--text-secondary);
  font-size: 12px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.shortcut-grid button {
  min-height: 84px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.shortcut-grid button:hover,
.todo-item:hover,
.exam-row:hover {
  border-color: rgba(var(--primary-rgb), 0.35);
}

.exam-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  text-align: left;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--text-secondary);
}

.status-dot.online { background: #16a34a; box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.12); }
.status-dot.warn { background: #d97706; box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.12); }
.status-dot.ended { background: #94a3b8; }

.status-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);
  color: var(--text-secondary);
  font-size: 13px;
}

.status-item:last-child {
  border-bottom: 0;
}

.status-item strong {
  color: var(--text-primary);
}

@media (max-width: 1080px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .admin-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-clock {
    text-align: left;
  }

  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
