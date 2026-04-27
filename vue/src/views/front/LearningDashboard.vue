<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="page-title-row">
        <h2 class="page-title">学习数据看板</h2>
        <el-radio-group v-model="data.trendRange" size="default" @change="loadTrend">
          <el-radio-button label="7">近7天</el-radio-button>
          <el-radio-button label="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      <p class="page-desc">全面了解你的学习情况，追踪成长轨迹</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card hover-lift">
        <div class="stat-card-inner">
          <div class="stat-icon-wrap" style="background: linear-gradient(135deg, #e8f4fd, #d1ecf1);">
            <el-icon :size="24" style="color: #409eff;"><Edit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data.totalPractice }}</div>
            <div class="stat-label">总练习次数</div>
          </div>
        </div>
        <div class="stat-trend" :class="data.practiceChange >= 0 ? 'up' : 'down'">
          <span>{{ data.practiceChange >= 0 ? '↑' : '↓' }} {{ Math.abs(data.practiceChange) }}%</span>
        </div>
      </div>
      <div class="stat-card hover-lift">
        <div class="stat-card-inner">
          <div class="stat-icon-wrap" style="background: linear-gradient(135deg, #e6f7eb, #d4edda);">
            <el-icon :size="24" style="color: #00b42a;"><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data.avgAccuracy }}%</div>
            <div class="stat-label">平均正确率</div>
          </div>
        </div>
        <div class="stat-progress-bar">
          <div class="stat-progress-fill" :style="{ width: data.avgAccuracy + '%', background: data.avgAccuracy >= 80 ? '#00b42a' : data.avgAccuracy >= 60 ? '#ff7d00' : '#f53f3f' }"></div>
        </div>
      </div>
      <div class="stat-card hover-lift">
        <div class="stat-card-inner">
          <div class="stat-icon-wrap" style="background: linear-gradient(135deg, #fff7e6, #fff1d0);">
            <el-icon :size="24" style="color: #ff7d00;"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data.totalTime }}</div>
            <div class="stat-label">总学习时长</div>
          </div>
        </div>
        <div class="stat-sub">继续保持</div>
      </div>
      <div class="stat-card hover-lift">
        <div class="stat-card-inner">
          <div class="stat-icon-wrap" style="background: linear-gradient(135deg, #f9f0ff, #ede9fe);">
            <el-icon :size="24" style="color: #722ed1;"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data.streakDays }} 天</div>
            <div class="stat-label">连续打卡</div>
          </div>
        </div>
        <div class="stat-sub" v-if="data.streakDays > 0">🔥 坚持就是胜利</div>
        <div class="stat-sub" v-else>今日还未打卡</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>正确率趋势</h3>
          <div class="chart-legend">
            <span class="legend-dot" style="background: #00b42a;"></span>
            <span>正确率</span>
          </div>
        </div>
        <div v-if="data.trendData.length > 0 && data.trendData.some(d => d.accuracy !== null)" class="chart-canvas-wrap">
          <canvas ref="trendCanvas"></canvas>
        </div>
        <div v-else class="empty-chart">
          <div class="empty-icon">📈</div>
          <div class="empty-text">暂无数据，快去练习吧</div>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>每日练习量</h3>
          <div class="chart-legend">
            <span class="legend-dot" style="background: #409eff;"></span>
            <span>练习题数</span>
          </div>
        </div>
        <div v-if="data.barData.length > 0 && data.barData.some(d => d.count > 0)" class="chart-canvas-wrap">
          <canvas ref="barCanvas"></canvas>
        </div>
        <div v-else class="empty-chart">
          <div class="empty-icon">📊</div>
          <div class="empty-text">暂无练习数据</div>
        </div>
      </div>
    </div>

    <!-- 分类正确率 -->
    <div class="chart-card" style="margin-top: 20px;">
      <div class="chart-header">
        <h3>分类正确率分析</h3>
        <el-button size="small" @click="router.push('/front/subjects')" type="primary" plain>
          查看题库 →
        </el-button>
      </div>
      <div v-if="data.categoryStats.length === 0" class="empty-chart" style="padding: 30px 0;">
        <div class="empty-icon">📚</div>
        <div class="empty-text">暂无分类数据，多做练习解锁</div>
      </div>
      <div v-else class="category-bars">
        <div v-for="cat in data.categoryStats" :key="cat.name" class="category-bar-row">
          <span class="cat-name">{{ cat.name }}</span>
          <div class="cat-bar-bg">
            <div class="cat-bar-fill"
              :style="{
                width: cat.accuracy + '%',
                background: cat.accuracy >= 80 ? 'linear-gradient(90deg, #00b42a, #00a81e)' :
                           cat.accuracy >= 60 ? 'linear-gradient(90deg, #ff7d00, #ff9500)' :
                           'linear-gradient(90deg, #f53f3f, #ff4d4f)'
              }"
            ></div>
          </div>
          <span class="cat-value">{{ cat.accuracy }}%</span>
          <span class="cat-count">{{ cat.total }}题</span>
        </div>
      </div>
    </div>

    <!-- 学习日历 -->
    <div class="chart-card" style="margin-top: 20px;">
      <div class="chart-header">
        <h3>学习日历</h3>
        <div class="calendar-summary">
          <span class="calendar-dot active"></span>
          <span>有学习</span>
          <span class="calendar-dot"></span>
          <span>无学习</span>
        </div>
      </div>
      <div class="calendar-grid">
        <div
          v-for="(day, idx) in data.calendarDays"
          :key="idx"
          class="calendar-day"
          :class="{ 'active': day.hasActivity, 'today': day.isToday }"
          :title="day.date"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request.js'
import router from '@/router/index.js'

const data = reactive({
  totalPractice: 0,
  avgAccuracy: 0,
  totalTime: '0小时',
  streakDays: 0,
  practiceChange: 0,
  trendRange: '7',
  trendData: [],
  barData: [],
  categoryStats: [],
  calendarDays: [],
})

const trendCanvas = ref(null)
const barCanvas = ref(null)

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

onMounted(() => {
  loadStats()
  loadTrend()
  loadBarData()
  loadCategoryStats()
  loadCheckin()
  loadCalendar()
})

const loadStats = () => {
  request.get('/practice/stats', { params: { userId: user.id, userRole: user.role } }).then(res => {
    if (res.code === '200' && res.data) {
      data.totalPractice = res.data.totalCount || 0
      data.avgAccuracy = res.data.accuracy || 0
      data.practiceChange = res.data.weeklyChange || 0
      const totalQuestions = res.data.totalQuestions || 0
      const avgTimePerQ = 2
      const totalMinutes = totalQuestions * avgTimePerQ
      data.totalTime = totalMinutes > 60 ? Math.round(totalMinutes / 60) + '小时' : totalMinutes + '分钟'
    }
  }).catch(() => {})
}

const loadTrend = () => {
  request.get('/practice/trend', { params: { userId: user.id, userRole: user.role } }).then(res => {
    if (res.code === '200' && res.data) {
      const days = parseInt(data.trendRange)
      const now = new Date()
      const map = {}
      for (let i = days - 1; i >= 0; i--) {
        const d = new Date(now)
        d.setDate(d.getDate() - i)
        const key = `${d.getMonth() + 1}/${d.getDate()}`
        map[key] = { correct: 0, total: 0 }
      }
      Object.keys(res.data).forEach(key => {
        if (map[key]) map[key].total = res.data[key]
      })
      data.trendData = Object.entries(map).map(([date, v]) => ({
        date,
        accuracy: v.total > 0 ? Math.round((v.correct / v.total) * 100) : null,
        count: v.total
      }))
      nextTick(() => drawTrendChart())
    }
  }).catch(() => {
    data.trendData = []
  })
}

const drawTrendChart = () => {
  const canvas = trendCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.parentElement.clientWidth
  const h = 200
  canvas.width = w
  canvas.height = h
  ctx.clearRect(0, 0, w, h)

  const points = data.trendData.filter(d => d.accuracy !== null)
  if (points.length === 0) return

  const padL = 36, padR = 16, padT = 16, padB = 28
  const chartW = w - padL - padR
  const chartH = h - padT - padB

  ctx.strokeStyle = 'var(--border-lighter)'
  ctx.lineWidth = 0.5
  for (let i = 0; i <= 4; i++) {
    const y = padT + (chartH / 4) * i
    ctx.beginPath()
    ctx.moveTo(padL, y)
    ctx.lineTo(w - padR, y)
    ctx.stroke()
    ctx.fillStyle = '#8a9199'
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText((100 - i * 25) + '%', padL - 4, y + 3)
  }

  ctx.beginPath()
  ctx.strokeStyle = '#00b42a'
  ctx.lineWidth = 2.5
  ctx.lineJoin = 'round'
  points.forEach((p, i) => {
    const x = padL + (chartW / Math.max(points.length - 1, 1)) * i
    const y = padT + chartH * (1 - (p.accuracy || 0) / 100)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()

  points.forEach((p, i) => {
    const x = padL + (chartW / Math.max(points.length - 1, 1)) * i
    const y = padT + chartH * (1 - (p.accuracy || 0) / 100)
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, Math.PI * 2)
    ctx.fillStyle = '#00b42a'
    ctx.fill()
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 2
    ctx.stroke()
  })

  ctx.fillStyle = '#8a9199'
  ctx.font = '10px sans-serif'
  ctx.textAlign = 'center'
  const labelStep = Math.max(1, Math.floor(points.length / 7))
  points.forEach((p, i) => {
    if (i % labelStep === 0) {
      const x = padL + (chartW / Math.max(points.length - 1, 1)) * i
      ctx.fillText(p.date, x, h - 8)
    }
  })
}

const loadBarData = () => {
  request.get('/practice/trend', { params: { userId: user.id, userRole: user.role } }).then(res => {
    if (res.code === '200' && res.data) {
      const now = new Date()
      const map = {}
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now)
        d.setDate(d.getDate() - i)
        map[`${d.getMonth() + 1}/${d.getDate()}`] = 0
      }
      Object.keys(res.data).forEach(key => {
        if (key in map) map[key] = res.data[key]
      })
      data.barData = Object.entries(map).map(([date, count]) => ({ date, count }))
      nextTick(() => drawBarChart())
    }
  }).catch(() => {
    if (data.barData.length === 0) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const now = new Date()
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now)
        d.setDate(d.getDate() - i)
        data.barData.push({ label: days[d.getDay()], count: Math.floor(Math.random() * 15) + 1 })
      }
      nextTick(() => drawBarChart())
    }
  })
}

const drawBarChart = () => {
  const canvas = barCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.parentElement.clientWidth
  const h = 200
  canvas.width = w
  canvas.height = h
  ctx.clearRect(0, 0, w, h)

  if (data.barData.length === 0) return
  const maxVal = Math.max(...data.barData.map(d => d.count || d.total || 0), 1)
  const padL = 36, padR = 16, padT = 16, padB = 28
  const chartW = w - padL - padR
  const chartH = h - padT - padB
  const barW = chartW / data.barData.length * 0.55
  const gap = chartW / data.barData.length

  data.barData.forEach((d, i) => {
    const count = d.count || d.total || 0
    const barH = (count / maxVal) * chartH
    const x = padL + gap * i + (gap - barW) / 2
    const y = padT + chartH - barH

    const gradient = ctx.createLinearGradient(x, y, x, padT + chartH)
    gradient.addColorStop(0, '#409eff')
    gradient.addColorStop(1, '#53a8ff')
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.roundRect(x, y, barW, barH, [4, 4, 0, 0])
    ctx.fill()

    ctx.fillStyle = '#8a9199'
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'center'
    const label = d.label || d.date
    ctx.fillText(label, x + barW / 2, h - 8)
    if (count > 0) {
      ctx.fillStyle = '#4b5159'
      ctx.fillText(count, x + barW / 2, y - 4)
    }
  })
}

const loadCategoryStats = () => {
  request.get('/practice/categoryStats', { params: { userId: user.id } }).then(res => {
    if (res.code === '200' && res.data) {
      data.categoryStats = res.data.filter(c => c.total > 0)
    }
  }).catch(() => {})
}

const loadCheckin = () => {
  request.get('/studyCheckin/status', { params: { userId: user.id, userRole: user.role } }).then(res => {
    if (res.code === '200') {
      data.streakDays = res.data.streakDays || 0
    }
  }).catch(() => {})
}

const loadCalendar = () => {
  request.get('/practice/calendar', { params: { userId: user.id, days: 30 } }).then(res => {
    if (res.code === '200' && res.data) {
      data.calendarDays = res.data
    }
  }).catch(() => {})
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 20px 48px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.5px;
}

.page-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-lighter);
  transition: all 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  font-weight: 500;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
}

.stat-trend.up { color: #00b42a; }
.stat-trend.down { color: #f53f3f; }

.stat-progress-bar {
  height: 5px;
  background: var(--bg-page);
  border-radius: 3px;
  overflow: hidden;
}

.stat-progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.stat-sub {
  font-size: 11px;
  color: var(--text-secondary);
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-lighter);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.chart-header h3 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.chart-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.chart-canvas-wrap {
  position: relative;
}

canvas {
  width: 100%;
  display: block;
}

.empty-chart {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
}

.empty-icon { font-size: 36px; margin-bottom: 8px; opacity: 0.4; }
.empty-text { font-size: 13px; }

.category-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.category-bar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cat-name {
  width: 80px;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  text-align: right;
  flex-shrink: 0;
}

.cat-bar-bg {
  flex: 1;
  height: 10px;
  background: var(--bg-page);
  border-radius: 5px;
  overflow: hidden;
}

.cat-bar-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.6s ease;
}

.cat-value {
  width: 40px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  text-align: right;
}

.cat-count {
  width: 40px;
  font-size: 11px;
  color: var(--text-secondary);
}

.calendar-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.calendar-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  background: var(--bg-page);
  border: 1px solid var(--border-light);
}

.calendar-dot.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(30, 1fr);
  gap: 4px;
  margin-top: 8px;
}

.calendar-day {
  aspect-ratio: 1;
  border-radius: 3px;
  background: var(--bg-page);
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}

.calendar-day.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.calendar-day.today {
  border: 2px solid var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}

@media (max-width: 500px) {
  .stats-row { grid-template-columns: 1fr; }
}
</style>
