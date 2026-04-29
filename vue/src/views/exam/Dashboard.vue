<template>
  <div class="gd" v-loading="data.loading">

    <!-- 欢迎横幅 -->
    <div class="gd-hero">
      <div class="gd-hero-bg"></div>
      <div class="gd-hero-content">
        <div class="gd-hero-left">
          <div class="gd-hero-eyebrow">{{ greeting }}，{{ data.user?.name }}</div>
          <h2 class="gd-hero-title">{{ heroTitle }}</h2>
          <p class="gd-hero-sub">{{ heroSub }}</p>
        </div>
        <div class="gd-hero-right">
          <div class="gd-hero-stat" v-for="s in heroStats" :key="s.label">
            <div class="gds-num">{{ s.val }}</div>
            <div class="gds-label">{{ s.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 管理员视图 -->
    <template v-if="isAdmin">

      <!-- 统计卡片 -->
      <div class="gd-stats">
        <div class="gd-stat-card" v-for="s in adminStats" :key="s.label" @click="router.push(s.path)">
          <div class="gsc-inner" :style="{ background: s.gradient }">
            <div class="gsc-icon"><el-icon><component :is="s.icon" /></el-icon></div>
            <div class="gsc-body">
              <div class="gsc-num">{{ s.val }}</div>
              <div class="gsc-label">{{ s.label }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体内容 -->
      <div class="gd-body">

        <!-- 阅卷进度 -->
        <div class="gd-card">
          <div class="gdc-header">
            <div class="gdc-title">
              <div class="gdc-icon" style="background:rgba(0,180,42,0.1); color:#00b42a"><el-icon><EditPen /></el-icon></div>
              阅卷进度
            </div>
            <el-button text type="primary" size="small" @click="router.push('/exam/gradingCenter')">查看全部 →</el-button>
          </div>
          <div class="gdc-body">
            <div v-for="exam in data.examList.slice(0,8)" :key="exam.id" class="gd-progress-item">
              <div class="gdp-info">
                <span class="gdp-name">{{ exam.name }}</span>
                <el-tag size="small" type="warning" effect="plain">{{ exam.ungradedCount || 0 }} 份待阅</el-tag>
              </div>
              <div class="gdp-bar">
                <div class="gdp-fill" :style="{ width: calcProgress(exam) + '%' }"></div>
              </div>
              <span class="gdp-pct">{{ calcProgress(exam) }}%</span>
            </div>
            <div v-if="!data.examList.length" class="gd-empty">暂无审核数据</div>
          </div>
        </div>

        <!-- 最新公告 -->
        <div class="gd-card">
          <div class="gdc-header">
            <div class="gdc-title">
              <div class="gdc-icon" style="background:rgba(99,102,241,0.1); color:#6366f1"><el-icon><Bell /></el-icon></div>
              最新公告
            </div>
            <el-button text type="primary" size="small" @click="router.push('/exam/announcements')">查看全部 →</el-button>
          </div>
          <div class="gdc-body">
            <div v-for="a in data.announcements.slice(0,6)" :key="a.id" class="gd-ann-item">
              <span class="gdann-tag" :class="a.type">{{ typeLabel(a.type) }}</span>
              <span class="gdann-title">{{ a.title }}</span>
              <span class="gdann-time">{{ a.createdAt?.substring(5,16) }}</span>
            </div>
            <div v-if="!data.announcements.length" class="gd-empty">暂无公告</div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="gd-card gd-quick">
          <div class="gdc-header">
            <div class="gdc-title">
              <div class="gdc-icon" style="background:rgba(245,158,11,0.1); color:#f59e0b"><el-icon><Grid /></el-icon></div>
              快捷操作
            </div>
          </div>
          <div class="gdc-body">
            <div class="gd-quick-grid">
              <div class="gdq-item" v-for="q in quickActions" :key="q.label" @click="router.push(q.path)">
                <div class="gdq-icon" :style="{ background: q.gradient }">
                  <el-icon :color="q.color"><component :is="q.icon" /></el-icon>
                </div>
                <span>{{ q.label }}</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </template>

    <!-- 阅卷人视图 -->
    <template v-else-if="data.user.role === 'HELPER'">
      <div class="gd-stats">
        <div class="gd-stat-card">
          <div class="gsc-inner" style="background: linear-gradient(135deg, #16a34a, #4ade80)">
            <div class="gsc-icon"><el-icon><Check /></el-icon></div>
            <div class="gsc-body">
              <div class="gsc-num">{{ data.stats.gradedCount }}</div>
              <div class="gsc-label">今日已批阅</div>
            </div>
          </div>
        </div>
        <div class="gd-stat-card">
          <div class="gsc-inner" style="background: linear-gradient(135deg, #dc2626, #f87171)">
            <div class="gsc-icon"><el-icon><Clock /></el-icon></div>
            <div class="gsc-body">
              <div class="gsc-num">{{ data.stats.pendingGrade }}</div>
              <div class="gsc-label">待批阅试卷</div>
            </div>
          </div>
        </div>
      </div>

      <div class="gd-card">
        <div class="gdc-header">
          <div class="gdc-title">
            <div class="gdc-icon" style="background:rgba(0,180,42,0.1); color:#00b42a"><el-icon><EditPen /></el-icon></div>
            我的阅卷任务
          </div>
        </div>
        <div class="gdc-body">
          <div v-for="exam in data.examList" :key="exam.id" class="gd-task-item">
            <div class="gdt-info">
              <span class="gdt-name">{{ exam.name }}</span>
              <el-tag type="warning" size="small" effect="plain">{{ exam.ungradedCount || 0 }} 份待阅</el-tag>
            </div>
            <el-button type="primary" size="small" round @click="router.push({ path: '/exam/gradingCenter', query: { examId: exam.id } })">开始阅卷</el-button>
          </div>
          <div v-if="!data.examList.length" class="gd-empty">暂无待阅任务</div>
        </div>
      </div>
    </template>

    <!-- 玩家视图 -->
    <template v-else>
      <div class="gd-stats">
        <div class="gd-stat-card">
          <div class="gsc-inner" style="background: linear-gradient(135deg, #0284c7, #38bdf8)">
            <div class="gsc-icon"><el-icon><Notebook /></el-icon></div>
            <div class="gsc-body">
              <div class="gsc-num">{{ data.stats.examCount }}</div>
              <div class="gsc-label">可参加审核</div>
            </div>
          </div>
        </div>
        <div class="gd-stat-card">
          <div class="gsc-inner" style="background: linear-gradient(135deg, #7c3aed, #a78bfa)">
            <div class="gsc-icon"><el-icon><DataLine /></el-icon></div>
            <div class="gsc-body">
              <div class="gsc-num">{{ data.stats.myScoreCount }}</div>
              <div class="gsc-label">已出结果</div>
            </div>
          </div>
        </div>
      </div>

      <div class="gd-card">
        <div class="gdc-header">
          <div class="gdc-title">
            <div class="gdc-icon" style="background:rgba(99,102,241,0.1); color:#6366f1"><el-icon><Bell /></el-icon></div>
            最新公告
          </div>
        </div>
        <div class="gdc-body">
          <div v-for="a in data.announcements.slice(0,6)" :key="a.id" class="gd-ann-item">
            <span class="gdann-tag" :class="a.type">{{ typeLabel(a.type) }}</span>
            <span class="gdann-title">{{ a.title }}</span>
            <span class="gdann-time">{{ a.createdAt?.substring(5,16) }}</span>
          </div>
          <div v-if="!data.announcements.length" class="gd-empty">暂无公告</div>
        </div>
      </div>
    </template>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import router from '@/router/index.js'
import request from '@/utils/request.js'
import { EditPen, Clock, Check, DataLine, Bell, Notebook, Grid, Document, DataAnalysis, Stamp, Finished } from '@element-plus/icons-vue'

const data = reactive({
  loading: false,
  user: JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}'),
  stats: { examCount: 0, pendingApproval: 0, gradedCount: 0, publishedCount: 0, pendingGrade: 0, myScoreCount: 0 },
  examList: [],
  announcements: [],
})

const isAdmin = computed(() => ['OWNER', 'ADMIN'].includes(data.user.role))

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const heroTitle = computed(() => {
  if (isAdmin.value) return '阅卷工作台'
  if (data.user.role === 'HELPER') return '阅卷任务'
  return '审核中心'
})
const heroSub = computed(() => {
  if (isAdmin.value) return '实时掌握审核进度与阅卷状态'
  if (data.user.role === 'HELPER') return '高效批阅每一份审核答卷'
  return '查看可参加的审核与结果公示'
})

const heroStats = computed(() => {
  if (isAdmin.value) return [
    { val: data.examList.length, label: '审核总数' },
    { val: data.stats.pendingApproval, label: '待审批' },
    { val: data.stats.gradedCount, label: '已批阅' },
  ]
  if (data.user.role === 'HELPER') return [
    { val: data.stats.gradedCount, label: '今日已批' },
    { val: data.stats.pendingGrade, label: '待批阅' },
  ]
  return [
    { val: data.stats.examCount, label: '可参加' },
    { val: data.stats.myScoreCount, label: '已出结果' },
  ]
})

const adminStats = computed(() => [
  { label: '审核总数', val: data.examList.length, icon: 'Notebook', gradient: 'linear-gradient(135deg, #0284c7, #38bdf8)', path: '/exam/examAdmin' },
  { label: '待审批', val: data.stats.pendingApproval, icon: 'Stamp', gradient: 'linear-gradient(135deg, #d97706, #fbbf24)', path: '/exam/approvalCenter' },
  { label: '已批阅', val: data.stats.gradedCount, icon: 'Check', gradient: 'linear-gradient(135deg, #16a34a, #4ade80)', path: '/exam/gradingCenter' },
  { label: '已公示', val: data.stats.publishedCount, icon: 'DataAnalysis', gradient: 'linear-gradient(135deg, #7c3aed, #a78bfa)', path: '/exam/resultsCenter' },
])

const quickActions = [
  { label: '阅卷中心', path: '/exam/gradingCenter', icon: 'EditPen', gradient: 'rgba(0,180,42,0.1)', color: '#00b42a' },
  { label: '审批中心', path: '/exam/approvalCenter', icon: 'Stamp', gradient: 'rgba(217,119,6,0.1)', color: '#d97706' },
  { label: '结果公示', path: '/exam/resultsCenter', icon: 'DataAnalysis', gradient: 'rgba(124,58,237,0.1)', color: '#7c3aed' },
  { label: '公告中心', path: '/exam/announcements', icon: 'Bell', gradient: 'rgba(14,116,144,0.1)', color: '#0891b2' },
]

const typeLabel = (type) => ({ general: '公告', exam: '审核', result: '结果', notice: '通知' }[type] || '公告')

const calcProgress = (exam) => {
  if (!exam._gradingProgress) return 0
  const p = exam._gradingProgress
  return p.total ? Math.round(((p.graded || 0) / p.total) * 100) : 0
}

const loadData = () => {
  data.loading = true
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') {
      data.examList = res.data || []
      data.stats.examCount = data.examList.length
      data.examList.forEach(exam => {
        if ((exam.ungradedCount > 0 || exam.status === 'ongoing' || exam.status === 'finished')) {
          request.get('/examApproval/getGradingProgress/' + exam.id).then(progressRes => {
            if (progressRes.code === '200') exam._gradingProgress = progressRes.data
          }).catch(() => {})
        }
      })
    }
  }).catch(() => {}).finally(() => { data.loading = false })

  request.get('/examAnnouncement/selectPublished').then(res => {
    if (res.code === '200') data.announcements = res.data || []
  }).catch(() => {})

  if (isAdmin.value) {
    request.get('/examApproval/getPending', { params: { pageNum: 1, pageSize: 1 } }).then(res => {
      if (res.code === '200') data.stats.pendingApproval = res.data?.total || 0
    }).catch(() => {})
    request.get('/examRecord/selectAll', { params: {} }).then(res => {
      if (res.code === '200') {
        const records = res.data || []
        data.stats.gradedCount = records.filter(r => r.manualScore != null).length
        data.stats.publishedCount = records.filter(r => r.isPublished).length
      }
    }).catch(() => {})
  }
}

onMounted(loadData)
</script>

<style scoped>
.gd { max-width: 1200px; margin: 0 auto; }

/* ===== 欢迎横幅 ===== */
.gd-hero {
  position: relative;
  background: var(--gradient-header);
  border-radius: 18px;
  padding: 32px 36px;
  margin-bottom: 24px;
  overflow: hidden;
}
.gd-hero-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 80% 50%, rgba(var(--primary-rgb), 0.15) 0%, transparent 60%);
  pointer-events: none;
}
.gd-hero-content {
  display: flex; justify-content: space-between; align-items: center;
  position: relative; gap: 24px;
}
.gd-hero-eyebrow { font-size: 13px; color: rgba(255,255,255,0.5); margin-bottom: 6px; font-weight: 500; }
.gd-hero-title { margin: 0 0 6px; font-size: 28px; font-weight: 800; color: #fff; }
.gd-hero-sub { margin: 0; font-size: 13px; color: rgba(255,255,255,0.6); }
.gd-hero-right { display: flex; gap: 32px; flex-shrink: 0; }
.gd-hero-stat { text-align: center; }
.gds-num { font-size: 28px; font-weight: 800; color: #fff; line-height: 1; }
.gds-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; }

/* ===== 统计卡片 ===== */
.gd-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.gd-stat-card { border-radius: 14px; overflow: hidden; cursor: pointer; transition: transform 0.3s; }
.gd-stat-card:hover { transform: translateY(-3px); }
.gsc-inner {
  padding: 20px;
  display: flex; align-items: center; gap: 14px;
  color: #fff;
}
.gsc-icon { font-size: 28px; opacity: 0.85; }
.gsc-body { display: flex; flex-direction: column; gap: 2px; }
.gsc-num { font-size: 28px; font-weight: 800; line-height: 1; color: #fff; }
.gsc-label { font-size: 12px; opacity: 0.85; color: #fff; }

/* ===== 主体 ===== */
.gd-body { display: flex; flex-direction: column; gap: 16px; }
.gd-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px;
  border: 1px solid var(--border-lighter);
}
.gdc-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.gdc-title { display: flex; align-items: center; gap: 10px; font-size: 15px; font-weight: 700; color: var(--text-primary); }
.gdc-icon {
  width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}
.gdc-body { display: flex; flex-direction: column; gap: 0; }

/* 进度 */
.gd-progress-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--border-lighter); }
.gd-progress-item:last-child { border-bottom: none; }
.gdp-info { display: flex; align-items: center; gap: 10px; width: 200px; flex-shrink: 0; }
.gdp-name { font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 140px; }
.gdp-bar { flex: 1; height: 6px; background: var(--border-lighter); border-radius: 3px; overflow: hidden; }
.gdp-fill { height: 100%; background: var(--gradient-primary); border-radius: 3px; transition: width 0.6s ease; min-width: 2px; }
.gdp-pct { font-size: 12px; font-weight: 600; color: var(--primary-color); width: 36px; text-align: right; flex-shrink: 0; }

/* 公告 */
.gd-ann-item { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px dashed var(--border-lighter); }
.gd-ann-item:last-child { border-bottom: none; }
.gdann-tag { padding: 2px 8px; border-radius: 4px; font-size: 11px; color: #fff; flex-shrink: 0; }
.gdann-tag.general { background: var(--text-secondary); }
.gdann-tag.exam { background: var(--info-color, #409eff); }
.gdann-tag.result { background: var(--success-color, #00b42a); }
.gdann-tag.notice { background: var(--warning-color, #ff7d00); }
.gdann-title { flex: 1; font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.gdann-time { font-size: 11px; color: var(--text-secondary); flex-shrink: 0; }

/* 快捷 */
.gd-quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.gdq-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px; border-radius: 12px; cursor: pointer; transition: all 0.2s; }
.gdq-item:hover { background: var(--bg-page); }
.gdq-item:hover span { color: var(--primary-color); }
.gdq-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.gdq-item span { font-size: 12px; color: var(--text-secondary); font-weight: 500; }

/* 任务 */
.gd-task-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid var(--border-lighter); }
.gd-task-item:last-child { border-bottom: none; }
.gdt-info { display: flex; align-items: center; gap: 10px; }
.gdt-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }

/* 空 */
.gd-empty { text-align: center; padding: 32px; color: var(--text-secondary); font-size: 13px; }

@media (max-width: 900px) {
  .gd-stats { grid-template-columns: repeat(2, 1fr); }
  .gd-hero-content { flex-direction: column; align-items: flex-start; }
  .gd-hero-right { width: 100%; justify-content: space-around; }
}
</style>
