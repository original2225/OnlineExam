<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">🏅</div>
      <div class="page-hero-title">成就系统</div>
      <div class="page-hero-subtitle">记录你的每一步成长</div>
      <div class="page-hero-stats">
        <div class="page-hero-stat"><div class="page-hero-stat-val">{{ data.unlockedCount }}</div><div class="page-hero-stat-lbl">已解锁</div></div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat"><div class="page-hero-stat-val">{{ data.achievements.length }}</div><div class="page-hero-stat-lbl">总成就</div></div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat"><div class="page-hero-stat-val">{{ data.totalPoints }}</div><div class="page-hero-stat-lbl">积分</div></div>
      </div>
    </div>

    <div class="ach-filter">
      <el-radio-group v-model="data.activeCategory" size="large">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="exam">审核</el-radio-button>
        <el-radio-button label="practice">练习</el-radio-button>
        <el-radio-button label="checkin">打卡</el-radio-button>
        <el-radio-button label="social">社交</el-radio-button>
      </el-radio-group>
    </div>

    <div class="ach-grid">
      <div
        v-for="ach in filteredAchievements"
        :key="ach.key"
        class="ach-card"
        :class="{ unlocked: ach.unlocked, locked: !ach.unlocked }"
        @click="showAchDetail(ach)"
      >
        <div class="ach-badge" :class="{ 'badge-locked': !ach.unlocked }">
          <span class="badge-icon">{{ ach.unlocked ? ach.icon : '🔒' }}</span>
          <div class="badge-glow" v-if="ach.unlocked"></div>
        </div>
        <div class="ach-name">{{ ach.name }}</div>
        <div class="ach-desc">{{ ach.description }}</div>
        <div class="ach-meta">
          <span class="ach-score">+{{ ach.score }}分</span>
          <span class="ach-date" v-if="ach.earnedAt">{{ formatDate(ach.earnedAt) }}</span>
          <span class="ach-date locked-label" v-else>未解锁</span>
        </div>
        <div class="ach-unlocked-overlay" v-if="ach.unlocked">
          <div class="unlocked-check">✓</div>
        </div>
      </div>
    </div>

    <div class="recent-unlocks" v-if="data.recentUnlocks.length">
      <div class="ru-header">
        <span class="ru-title">🎉 最近解锁</span>
      </div>
      <div class="ru-list">
        <div v-for="ach in data.recentUnlocks" :key="ach.id" class="ru-item" @click="showAchDetail(ach)">
          <span class="ru-icon">{{ ach.icon }}</span>
          <div class="ru-info">
            <span class="ru-name">{{ ach.achievementName }}</span>
            <span class="ru-time">{{ formatDate(ach.earnedAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="data.detailVisible" :title="data.detailData?.name || '成就详情'" width="420px" :append-to-body="true" class="ach-detail-dialog">
      <div class="ach-detail-content" v-if="data.detailData">
        <div class="ach-detail-badge" :class="{ 'badge-locked': !data.detailData.unlocked }">
          <span class="badge-icon">{{ data.detailData.unlocked ? data.detailData.icon : '🔒' }}</span>
        </div>
        <div class="ach-detail-name">{{ data.detailData.name }}</div>
        <div class="ach-detail-desc">{{ data.detailData.description }}</div>
        <div class="ach-detail-meta">
          <div class="ach-detail-row">
            <span class="ach-detail-label">积分</span>
            <span class="ach-detail-value ach-score">+{{ data.detailData.score }}</span>
          </div>
          <div class="ach-detail-row" v-if="data.detailData.category">
            <span class="ach-detail-label">类别</span>
            <span class="ach-detail-value">{{ categoryLabel(data.detailData.category) }}</span>
          </div>
          <div class="ach-detail-row" v-if="data.detailData.earnedAt">
            <span class="ach-detail-label">解锁时间</span>
            <span class="ach-detail-value">{{ formatDate(data.detailData.earnedAt) }}</span>
          </div>
          <div class="ach-detail-row" v-if="!data.detailData.unlocked">
            <span class="ach-detail-label">状态</span>
            <span class="ach-detail-value locked-label">未解锁</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from 'vue'
import request from '@/utils/request.js'

const data = reactive({
  achievements: [],
  recentUnlocks: [],
  activeCategory: 'all',
  unlockedCount: 0,
  totalPoints: 0,
  detailVisible: false,
  detailData: null,
})

const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')

const filteredAchievements = computed(() => {
  if (data.activeCategory === 'all') return data.achievements
  return data.achievements.filter(a => a.category === data.activeCategory)
})

const updateStats = () => {
  const unlocked = data.achievements.filter(a => a.unlocked)
  data.unlockedCount = unlocked.length
  data.totalPoints = unlocked.reduce((sum, a) => sum + (a.score || 0), 0)
}

watch(() => data.achievements, updateStats, { deep: true })

const loadAchievements = () => {
  request.get('/achievement/all', {
    params: { userId: user.id, userRole: user.role }
  }).then(res => {
    if (res.code === '200') {
      data.achievements = res.data || []
      updateStats()
    }
  })
}

const loadRecent = () => {
  request.get('/achievement/my', {
    params: { userId: user.id, userRole: user.role }
  }).then(res => {
    if (res.code === '200') {
      data.recentUnlocks = (res.data || []).slice(0, 5)
    }
  })
}

const showAchDetail = (ach) => {
  data.detailData = ach
  data.detailVisible = true
}

const categoryLabel = (cat) => {
  const map = { exam: '审核', practice: '模拟', checkin: '打卡', social: '互动' }
  return map[cat] || cat
}

const formatDate = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadAchievements()
  loadRecent()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.achievement-page { max-width: 1000px; margin: 0 auto; padding: 20px; }

.ach-filter { margin-bottom: 20px; }

.ach-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px; margin-bottom: 24px;
}
.ach-card {
  background: var(--bg-card); border-radius: 14px; padding: 20px 16px;
  text-align: center; position: relative; overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s; cursor: pointer;
}
.ach-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.ach-card.unlocked { border-color: #ffd700; }
.ach-card.locked { opacity: 0.7; }

.ach-badge {
  width: 64px; height: 64px; border-radius: 50%; margin: 0 auto 12px;
  background: linear-gradient(135deg, #ffd700, #ffaa00);
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.badge-locked { background: linear-gradient(135deg, #c0c0c0, var(--text-secondary)) !important; }
.badge-icon { font-size: 30px; }
.badge-glow {
  position: absolute; inset: -4px; border-radius: 50%;
  background: rgba(255,215,0,0.2); animation: badgePulse 2s infinite;
}
@keyframes badgePulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.ach-name { font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
.ach-desc { font-size: 12px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 10px; min-height: 36px; }
.ach-meta { display: flex; justify-content: center; gap: 8px; align-items: center; }
.ach-score { font-size: 12px; color: #ffd700; font-weight: 700; }
.ach-date { font-size: 11px; color: #67c23a; }
.locked-label { color: #c0c4cc; }

.ach-unlocked-overlay {
  position: absolute; top: 8px; right: 8px;
}
.unlocked-check {
  width: 22px; height: 22px; border-radius: 50%;
  background: #67c23a; color: var(--bg-card);
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}

.recent-unlocks { background: var(--bg-card); border-radius: 14px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.ru-header { margin-bottom: 14px; }
.ru-title { font-size: 15px; font-weight: 700; color: var(--text-primary); }
.ru-list { display: flex; gap: 12px; flex-wrap: wrap; }
.ru-item {
  display: flex; align-items: center; gap: 10px;
  background: var(--bg-page); border-radius: 10px; padding: 10px 14px;
  cursor: pointer; transition: background 0.2s;
}
.ru-item:hover { background: #f0f0f0; }
.ru-icon { font-size: 28px; }
.ru-info { display: flex; flex-direction: column; }
.ru-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.ru-time { font-size: 11px; color: var(--text-secondary); }

.ach-detail-content { text-align: center; padding: 8px 0; }
.ach-detail-badge {
  width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px;
  background: linear-gradient(135deg, #ffd700, #ffaa00);
  display: flex; align-items: center; justify-content: center;
}
.ach-detail-badge.badge-locked { background: linear-gradient(135deg, #c0c0c0, var(--text-secondary)); }
.ach-detail-badge .badge-icon { font-size: 38px; }
.ach-detail-name { font-size: 20px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
.ach-detail-desc { font-size: 14px; color: var(--text-secondary); margin-bottom: 20px; line-height: 1.6; }
.ach-detail-meta { text-align: left; }
.ach-detail-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; border-bottom: 1px solid var(--border-lighter, #eee);
}
.ach-detail-row:last-child { border-bottom: none; }
.ach-detail-label { font-size: 13px; color: var(--text-secondary); }
.ach-detail-value { font-size: 13px; font-weight: 600; color: var(--text-primary); }
</style>
