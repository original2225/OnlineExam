<template>
  <div class="dashboard">

    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-bg"></div>
      <div class="welcome-content">
        <div class="welcome-left">
          <div class="greeting-time">{{ greeting }}</div>
          <h2 class="welcome-name">{{ data.user?.name }}</h2>
          <p class="welcome-meta">{{ roleLabel }} · {{ today }}</p>
        </div>
        <div class="welcome-right">
          <div class="weather-card">
            <span class="weather-icon">{{ weather.icon }}</span>
            <div class="weather-info">
              <div class="weather-temp">{{ weather.temp }}°C</div>
              <div class="weather-text">{{ weather.text }}</div>
              <div class="weather-city" v-if="weather.city">
                <el-icon><Location /></el-icon> {{ weather.city }}
              </div>
            </div>
            <el-select v-model="weatherCity" size="small" @change="loadWeather" placeholder="选择城市" filterable style="width:110px">
              <el-option label="自动定位" value="自动定位" />
              <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-grid">
      <div class="stat-card" @click="$router.push('/manager/student')">
        <div class="stat-inner stat-purple">
          <div class="stat-icon"><el-icon><Avatar /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ totalUsers }}</div>
            <div class="stat-label">总人数</div>
          </div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/manager/admin')">
        <div class="stat-inner stat-blue">
          <div class="stat-icon"><el-icon><Tools /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ data.stats.admins }}</div>
            <div class="stat-label">管理员</div>
          </div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/manager/examiner')">
        <div class="stat-inner stat-orange">
          <div class="stat-icon"><el-icon><Reading /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ data.stats.examiners }}</div>
            <div class="stat-label">阅卷人</div>
          </div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/manager/student')">
        <div class="stat-inner stat-green">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ data.stats.users }}</div>
            <div class="stat-label">玩家</div>
          </div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/manager/exam')">
        <div class="stat-inner stat-teal">
          <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ data.stats.exams }}</div>
            <div class="stat-label">审核数量</div>
          </div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/manager/question')">
        <div class="stat-inner stat-red">
          <div class="stat-icon"><el-icon><EditPen /></el-icon></div>
          <div class="stat-body">
            <div class="stat-num">{{ data.stats.questions }}</div>
            <div class="stat-label">题目总数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content-row">

      <!-- 左侧主区 -->
      <div class="main-area">

        <!-- 公告区 -->
        <div class="panel-card notice-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon color="#6366f1"><Bell /></el-icon>
              系统公告
            </div>
            <el-tag v-if="data.noticeData.length" type="primary" size="small">{{ data.noticeData.length }} 条</el-tag>
          </div>
          <div class="notice-list" v-if="data.noticeData.length">
            <div v-for="(item, i) in data.noticeData.slice(0,5)" :key="i" class="notice-item" :class="{ 'notice-new': i === 0 }">
              <div class="notice-dot" v-if="i === 0"></div>
              <div class="notice-content">
                <div class="notice-title">{{ item.title }}</div>
                <div class="notice-body">{{ item.content }}</div>
                <div class="notice-time">
                  <el-icon><Clock /></el-icon> {{ formatTime(item.time) }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint">
            <el-icon><Bell /></el-icon> 暂无公告
          </div>
        </div>

        <!-- 近期审核 -->
        <div class="panel-card exam-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon color="#f59e0b"><Calendar /></el-icon>
              近期审核
            </div>
            <el-button link type="primary" size="small" @click="$router.push('/manager/exam')">查看全部 →</el-button>
          </div>
          <div class="exam-timeline" v-if="data.recentExams.length">
            <div v-for="exam in data.recentExams.slice(0,6)" :key="exam.id" class="exam-item" @click="$router.push('/manager/exam')">
              <div class="exam-status-dot" :class="'dot-' + getExamDotClass(exam)"></div>
              <div class="exam-info">
                <div class="exam-name">{{ exam.name }}</div>
                <div class="exam-meta">
                  <el-tag :type="getExamTagType(exam)" size="small" effect="light">{{ getExamStatusText(exam) }}</el-tag>
                  <span class="exam-time" v-if="exam.startTime">{{ formatShortTime(exam.startTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint">
            <el-icon><Calendar /></el-icon> 暂无审核安排
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="panel-card shortcut-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon color="#10b981"><Grid /></el-icon>
              快捷入口
            </div>
          </div>
          <div class="shortcut-grid">
            <div class="shortcut-item" v-for="s in shortcuts" :key="s.path" @click="$router.push(s.path)">
              <div class="shortcut-icon" :style="{ background: s.bg }">
                <el-icon :size="22" :color="s.color"><component :is="s.icon" /></el-icon>
              </div>
              <span>{{ s.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar">

        <!-- 实时时钟 -->
        <div class="clock-card">
          <div class="clock-time">{{ clockTime }}</div>
          <div class="clock-date">{{ clockDate }}</div>
          <div class="clock-progress">
            <div class="cp-label">今日已过</div>
            <div class="cp-track">
              <div class="cp-fill" :style="{ width: dayProgress + '%' }"></div>
            </div>
            <div class="cp-pct">{{ dayProgress }}%</div>
          </div>
        </div>

        <!-- 每���一言 -->
        <div class="quote-card" @click="refreshQuote">
          <div class="quote-bg"></div>
          <div class="quote-content">
            <div class="quote-icon">💡</div>
            <div class="quote-text">「 {{ dailyQuote }} 」</div>
            <div class="quote-hint">点击换一言</div>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="panel-card todo-panel" v-if="isAdmin">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon color="#ef4444"><List /></el-icon>
              待办事项
            </div>
            <span class="todo-badge" v-if="pendingCount > 0">{{ pendingCount }}</span>
          </div>
          <div class="todo-list" v-if="todoItems.filter(t => !t.done).length">
            <div
              v-for="todo in todoItems.filter(t => !t.done).slice(0, 4)"
              :key="todo.id"
              class="todo-item"
              :class="{ urgent: todo.urgent }"
              @click="$router.push(todo.path)"
            >
              <div class="todo-check">
                <el-icon v-if="todo.done"><Check /></el-icon>
              </div>
              <div class="todo-body">
                <span class="todo-text">{{ todo.text }}</span>
                <span class="todo-meta" v-if="todo.meta">{{ todo.meta }}</span>
              </div>
              <el-tag :type="todo.urgent ? 'danger' : 'info'" size="small" effect="light">{{ todo.tag }}</el-tag>
            </div>
          </div>
          <div v-else class="empty-hint">
            <span>🎉 所有待办已完成</span>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="panel-card status-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon color="#10b981"><Monitor /></el-icon>
              系统状态
            </div>
          </div>
          <div class="status-list">
            <div class="status-item">
              <div class="status-dot online"></div>
              <span class="status-label">后端服务</span>
              <span class="status-val ok">正常</span>
            </div>
            <div class="status-item">
              <div class="status-dot online"></div>
              <span class="status-label">数据库</span>
              <span class="status-val ok">正常</span>
            </div>
            <div class="status-item">
              <div class="status-dot" :class="pendingApprovalCount > 0 ? 'warn' : 'online'"></div>
              <span class="status-label">待审批</span>
              <span class="status-val" :class="pendingApprovalCount > 0 ? 'warn' : 'ok'">{{ pendingApprovalCount }} 条</span>
            </div>
            <div class="status-item">
              <div class="status-dot online"></div>
              <span class="status-label">运行时间</span>
              <span class="status-val ok">{{ uptimeStr }}</span>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, h } from "vue";
import request from "@/utils/request.js";
import { Bell, Clock, Calendar, EditPen, List, Grid, Monitor, Avatar, User, Tools, Reading, Check, Location, Document, Finished, DataLine } from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: [],
  recentExams: [],
  stats: { users: 0, admins: 0, examiners: 0, exams: 0, questions: 0, notices: 0 }
})

const pendingApprovalCount = ref(0)
const startTime = Date.now()

const totalUsers = computed(() => data.stats.users + data.stats.admins + data.stats.examiners)

const shortcuts = [
  { label: '审核管理', path: '/manager/exam', icon: 'Document', bg: 'linear-gradient(135deg, #dbeafe, #bfdbfe)', color: '#3b82f6' },
  { label: '结果管理', path: '/manager/score', icon: 'DataLine', bg: 'linear-gradient(135deg, #dcfce7, #bbf7d0)', color: '#16a34a' },
  { label: '阅卷中心', path: '/exam/gradingCenter', icon: 'Finished', bg: 'linear-gradient(135deg, #fef3c7, #fde68a)', color: '#d97706' },
  { label: '题目管理', path: '/manager/question', icon: 'EditPen', bg: 'linear-gradient(135deg, #fce7f3, #fbcfe8)', color: '#db2777' },
  { label: '玩家管理', path: '/manager/student', icon: 'User', bg: 'linear-gradient(135deg, #ede9fe, #ddd6fe)', color: '#7c3aed' },
  { label: '公告管理', path: '/manager/notice', icon: 'Bell', bg: 'linear-gradient(135deg, #fee2e2, #fecaca)', color: '#dc2626' },
]

const todoItems = reactive([
  { id: 1, text: '审批注册申请', meta: '', tag: '审批', path: '/manager/student', done: false, urgent: false },
  { id: 2, text: '批阅主观题', meta: '', tag: '批阅', path: '/exam/gradingCenter', done: false, urgent: false },
  { id: 3, text: '审核题目贡献', meta: '', tag: '审核', path: '/manager/question', done: false, urgent: false },
])

const pendingCount = computed(() => todoItems.filter(t => !t.done && t.meta).length)

const isAdmin = computed(() => {
  const role = data.user.role
  return role === 'OWNER' || role === 'ADMIN'
})

const roleLabel = computed(() => {
  const map = { OWNER: '所有者', ADMIN: '管理员', HELPER: '阅卷人', USER: '玩家' }
  return map[data.user.role] || '玩家'
})

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 时钟
const clockTime = ref('')
const clockDate = ref('')
const dayProgress = ref(0)
let clockTimer = null

const updateClock = () => {
  const now = new Date()
  clockTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
  clockDate.value = now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' })
  const totalMinutes = now.getHours() * 60 + now.getMinutes()
  dayProgress.value = Math.round((totalMinutes / 1440) * 100)
}

const uptimeStr = computed(() => {
  const diff = Math.floor((Date.now() - startTime) / 1000)
  if (diff < 60) return `${diff}秒`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟`
  return `${Math.floor(diff / 3600)}小时${Math.floor((diff % 3600) / 60)}分`
})

// 审核
const getExamDotClass = (exam) => {
  if (exam.examType === 'permanent') return 'green'
  const now = new Date()
  const start = exam.startTime ? new Date(exam.startTime) : null
  const end = exam.endTime ? new Date(exam.endTime) : null
  if (start && now < start) return 'yellow'
  if (end && now > end) return 'gray'
  return 'green'
}
const getExamTagType = (exam) => {
  if (exam.examType === 'permanent') return 'success'
  if (exam.status === 'draft') return 'info'
  if (exam.status === 'finished') return 'warning'
  return 'primary'
}
const getExamStatusText = (exam) => {
  if (exam.examType === 'permanent') return '常驻'
  const map = { draft: '草稿', published: '已发布', ongoing: '进行中', finished: '已结束', cancelled: '已取消' }
  return map[exam.status] || exam.status
}
const formatShortTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
const formatTime = (dt) => {
  if (!dt) return '—'
  return String(dt).replace('T', ' ').substring(0, 19)
}

// 一言
const quotes = [
  '稳一点，比快一点更适合大型工程。', '红石机器上线前，先想清楚停机方案。',
  '公共仓储最怕临时随手放，记录比记忆可靠。', '建筑先定比例和风格，再堆细节。',
  'MSPT 异常先停机，再排查。', '不会问不是问题，不沟通才是问题。',
  '公共设施改动前，先说明影响范围。', '截图、坐标、材料表，都是工程交接的一部分。',
  '见习阶段先守规则，再谈效率。', '一次好审核，看答案，也看协作意识。',
  '机器能跑不等于适合上服，维护成本同样重要。', '服务器长期稳定，靠每个人少一点侥幸。',
  '先读文档再动手，能少踩很多坑。',
]
const dailyQuote = ref(quotes[Math.floor(Math.random() * quotes.length)])
const refreshQuote = () => { dailyQuote.value = quotes[Math.floor(Math.random() * quotes.length)] }

// 数据
const silent = { silentError: true }

const loadNotice = () => {
  request.get('/notice/selectAll', silent).then(res => {
    if (res.code === '200') { data.noticeData = res.data || []; data.stats.notices = data.noticeData.length }
  }).catch(() => {})
}
const loadStats = () => {
  request.get('/student/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === '200') data.stats.users = res.data?.total || 0 }).catch(() => {})
  request.get('/admin/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === '200') data.stats.admins = res.data?.total || 0 }).catch(() => {})
  request.get('/examiner/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === '200') data.stats.examiners = res.data?.total || 0 }).catch(() => {})
  request.get('/exam/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === '200') data.stats.exams = res.data?.total || 0 }).catch(() => {})
  request.get('/question/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1 } }).then(res => { if (res.code === '200') data.stats.questions = res.data?.total || 0 }).catch(() => {})
}
const loadRecentExams = () => {
  request.get('/exam/selectAll', silent).then(res => {
    if (res.code === '200') {
      data.recentExams = (res.data || []).sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || '')).slice(0, 6)
    }
  }).catch(() => {})
}
const loadTodoItems = () => {
  request.get('/registration/selectPage', { ...silent, params: { pageNum: 1, pageSize: 1, status: 'PENDING' } }).then(res => {
    if (res.code === '200') { const c = res.data?.total || 0; todoItems[0].meta = c > 0 ? `${c} 条待审批` : ''; todoItems[0].urgent = c > 5 }
  }).catch(() => {})
  request.get('/grading/countPending', silent).then(res => {
    if (res.code === '200') { const c = res.data || 0; todoItems[1].meta = c > 0 ? `${c} 份待批阅` : ''; todoItems[1].urgent = c > 10 }
  }).catch(() => {})
  request.get('/questionContribution/pendingCount', silent).then(res => {
    if (res.code === '200') { const c = res.data || 0; todoItems[2].meta = c > 0 ? `${c} 题待审核` : ''; todoItems[2].urgent = c > 20 }
  }).catch(() => {})
}

// 天气
const weather = reactive({ temp: '--', text: '加载中', icon: '🌤', windDir: '', windSpeed: '', city: '' })
const cityOptions = ['北京','上海','广州','深圳','杭州','成都','武汉','南京','重庆','西安','苏州','长沙','天津','郑州','青岛','沈阳','宁波','昆明','合肥','福州','厦门','哈尔滨','大连','济南','贵阳','南宁','兰州','太原','石家庄']
const weatherCity = ref(localStorage.getItem('xm-weather-city') || '自动定位')
const getWeatherIcon = (code) => {
  code = parseInt(code) || 0
  if (code === 113) return '☀️'
  if (code === 116) return '⛅'
  if (code <= 200) return '🌧'
  if (code <= 390) return '🌧'
  return '🌤'
}
const detectCity = async () => {
  try {
    const res = await fetch('http://ip-api.com/json/?lang=zh-CN')
    const json = await res.json()
    if (json?.city) return json.city.replace(/市$/, '')
  } catch {}
  return '北京'
}
const loadWeather = async () => {
  let city = weatherCity.value && weatherCity.value !== '自动定位' ? weatherCity.value : await detectCity()
  fetch(`https://wttr.in/${encodeURIComponent(city)}?format=j1&lang=zh`, { headers: { 'Accept': 'application/json' } })
    .then(r => r.json()).then(json => {
      const cur = json.current_condition?.[0]
      if (cur) {
        weather.temp = cur.temp_C; weather.text = cur.lang_zh?.[0]?.value || cur.weatherDesc?.[0]?.value || ''
        weather.icon = getWeatherIcon(cur.weatherCode); weather.city = city
      }
    }).catch(() => { weather.text = '获取失败'; weather.icon = '🌡'; weather.city = city })
  localStorage.setItem('xm-weather-city', weatherCity.value)
}

onMounted(() => {
  updateClock(); clockTimer = setInterval(updateClock, 1000)
  loadNotice(); loadStats(); loadRecentExams(); loadTodoItems()
  setTimeout(loadWeather, 300)
})
onUnmounted(() => { if (clockTimer) clearInterval(clockTimer) })
</script>

<style scoped>
.dashboard { max-width: 1400px; margin: 0 auto; }

/* ===== 欢迎横幅 ===== */
.welcome-banner {
  background: var(--gradient-header);
  border-radius: 18px; padding: 32px 36px; margin-bottom: 20px;
  position: relative; overflow: hidden;
}
.banner-bg {
  position: absolute; top: -50%; right: -5%; width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(var(--primary-rgb), 0.2) 0%, transparent 70%);
  border-radius: 50%; pointer-events: none;
}
.welcome-content { display: flex; justify-content: space-between; align-items: center; position: relative; }
.welcome-left {}
.greeting-time { font-size: 13px; color: rgba(255,255,255,0.5); margin-bottom: 4px; font-weight: 500; }
.welcome-name { margin: 0 0 6px; font-size: 28px; font-weight: 800; color: #fff; }
.welcome-meta { margin: 0; font-size: 13px; color: rgba(255,255,255,0.6); }
.weather-card { display: flex; align-items: center; gap: 12px; padding: 14px 18px; background: rgba(255,255,255,0.08); border-radius: 14px; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); }
.weather-icon { font-size: 36px; line-height: 1; }
.weather-temp { font-size: 22px; font-weight: 700; color: #fff; line-height: 1; }
.weather-text { font-size: 12px; color: rgba(255,255,255,0.6); margin-top: 2px; }
.weather-city { font-size: 11px; color: rgba(255,255,255,0.5); display: flex; align-items: center; gap: 3px; margin-top: 2px; }

/* ===== 统计网格 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 14px;
  border: 1px solid var(--border-lighter);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, border-color 0.3s;
  background: var(--bg-card);
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  border-color: var(--primary-color);
}
.stat-inner {
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--gradient-primary);
  color: #fff;
}
.stat-icon { font-size: 30px; opacity: 0.9; }
.stat-body { display: flex; flex-direction: column; gap: 4px; }
.stat-num {
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
  color: #fff;
}
.stat-label {
  font-size: 13px;
  opacity: 0.85;
  color: #fff;
  font-weight: 500;
}

/* ===== 内容区 ===== */
.content-row { display: flex; gap: 20px; }
.main-area { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.sidebar { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }

/* ===== 面板卡片 ===== */
.panel-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--border-lighter);
}
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.panel-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: var(--text-primary); }

/* ===== 公告 ===== */
.notice-list { display: flex; flex-direction: column; gap: 0; }
.notice-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px dashed var(--border-lighter); }
.notice-item:last-child { border-bottom: none; }
.notice-item.notice-new .notice-dot { background: var(--danger-color, #ef4444); box-shadow: 0 0 8px rgba(239,68,68,0.5); }
.notice-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--text-secondary); margin-top: 6px; flex-shrink: 0; }
.notice-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.notice-body { font-size: 13px; color: var(--text-regular); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.notice-time { font-size: 11px; color: var(--text-secondary); margin-top: 6px; display: flex; align-items: center; gap: 4px; }

/* ===== 审核 ===== */
.exam-timeline { display: flex; flex-direction: column; gap: 2px; }
.exam-item { display: flex; align-items: center; gap: 12px; padding: 10px 8px; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
.exam-item:hover { background: var(--bg-page); }
.exam-status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-green { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.dot-yellow { background: #f59e0b; }
.dot-gray { background: var(--text-secondary); }
.exam-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.exam-meta { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.exam-time { font-size: 11px; color: var(--text-secondary); }

/* ===== 快捷入口 ===== */
.shortcut-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.shortcut-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px; border-radius: 12px; cursor: pointer; transition: all 0.2s; }
.shortcut-item:hover { transform: translateY(-2px); }
.shortcut-item:hover span { color: var(--primary-color); }
.shortcut-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.shortcut-item span { font-size: 12px; color: var(--text-secondary); font-weight: 500; }

/* ===== 时钟 ===== */
.clock-card { background: var(--gradient-header); border-radius: 16px; padding: 24px 20px; text-align: center; }
.clock-time { font-size: 40px; font-weight: 800; color: #fff; font-variant-numeric: tabular-nums; letter-spacing: 3px; }
.clock-date { font-size: 14px; color: rgba(255,255,255,0.5); margin-top: 4px; margin-bottom: 16px; }
.clock-progress { display: flex; align-items: center; gap: 10px; }
.cp-label { font-size: 11px; color: rgba(255,255,255,0.4); }
.cp-track { flex: 1; height: 5px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden; }
.cp-fill { height: 100%; border-radius: 3px; background: var(--gradient-primary); transition: width 1s ease; }
.cp-pct { font-size: 12px; color: rgba(255,255,255,0.6); font-weight: 600; }

/* ===== 一言 ===== */
.quote-card { background: var(--gradient-primary); border-radius: 16px; padding: 20px; position: relative; overflow: hidden; cursor: pointer; transition: transform 0.2s; }
.quote-card:hover { transform: translateY(-2px); }
.quote-bg { position: absolute; bottom: -20px; right: -10px; font-size: 80px; opacity: 0.08; pointer-events: none; }
.quote-content { position: relative; }
.quote-icon { font-size: 20px; margin-bottom: 8px; }
.quote-text { font-size: 13px; color: rgba(255,255,255,0.9); line-height: 1.8; font-style: italic; }
.quote-hint { font-size: 11px; color: rgba(255,255,255,0.4); margin-top: 10px; text-align: right; }

/* ===== 待办 ===== */
.todo-badge { background: var(--danger-color, #ef4444); color: #fff; font-size: 11px; font-weight: 700; padding: 1px 7px; border-radius: 10px; }
.todo-list { display: flex; flex-direction: column; gap: 2px; }
.todo-item { display: flex; align-items: center; gap: 10px; padding: 10px 8px; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
.todo-item:hover { background: var(--bg-page); }
.todo-item.urgent .todo-check { background: #fee2e2; color: var(--danger-color, #ef4444); }
.todo-check { width: 22px; height: 22px; border-radius: 50%; background: var(--bg-page); flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.todo-body { flex: 1; min-width: 0; }
.todo-text { font-size: 13px; font-weight: 500; color: var(--text-primary); display: block; }
.todo-meta { font-size: 11px; color: var(--text-secondary); display: block; margin-top: 1px; }

/* ===== 系统状态 ===== */
.status-list { display: flex; flex-direction: column; gap: 10px; }
.status-item { display: flex; align-items: center; gap: 8px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-dot.online { background: var(--success-color, #10b981); box-shadow: 0 0 6px rgba(16,185,129,0.4); }
.status-dot.warn { background: var(--warning-color, #f59e0b); box-shadow: 0 0 6px rgba(245,158,11,0.4); }
.status-label { font-size: 13px; color: var(--text-secondary); flex: 1; }
.status-val { font-size: 12px; font-weight: 600; }
.status-val.ok { color: var(--success-color, #10b981); }
.status-val.warn { color: var(--warning-color, #f59e0b); }

/* 空状态 */
.empty-hint { color: var(--text-secondary); font-size: 13px; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 30px 0; }

/* ===== 响应式 ===== */
@media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .content-row { flex-direction: column; }
  .sidebar { width: 100%; }
}
</style>
