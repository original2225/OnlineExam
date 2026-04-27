<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">🏆</div>
      <div class="page-hero-title">排行榜</div>
      <div class="page-hero-subtitle">看看你的学习排名，和同学一起进步</div>
      <div class="page-hero-actions">
        <el-radio-group v-model="data.activeTab" @change="loadTab" size="large" class="lb-mode-switch">
          <el-radio-button label="practice">练习榜</el-radio-button>
          <el-radio-button label="checkin">打卡榜</el-radio-button>
          <el-radio-button label="exam">考试榜</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="podium" v-if="getTop3.length">
      <div class="podium-item second" @click="challengePlayer(getTop3[1])">
        <div class="podium-avatar">{{ getTop3[1]?.name?.charAt(0) || '?' }}</div>
        <div class="podium-medal">🥈</div>
        <div class="podium-name">{{ getTop3[1]?.name || '虚位以待' }}</div>
        <div class="podium-score">{{ getTop3[1]?.streakDays || getTop3[1]?.avgScore || getTop3[1]?.practiceCount || 0 }}</div>
        <div class="podium-block">
          <span class="challenge-btn">挑战</span>
        </div>
      </div>
      <div class="podium-item first" @click="challengePlayer(getTop3[0])">
        <div class="podium-avatar crown">
          <span class="crown-icon">👑</span>
          {{ getTop3[0]?.name?.charAt(0) || '?' }}
        </div>
        <div class="podium-medal">🥇</div>
        <div class="podium-name">{{ getTop3[0]?.name || '虚位以待' }}</div>
        <div class="podium-score">{{ getTop3[0]?.streakDays || getTop3[0]?.avgScore || getTop3[0]?.practiceCount || 0 }}</div>
        <div class="podium-block">
          <span class="challenge-btn">挑战</span>
        </div>
      </div>
      <div class="podium-item third" @click="challengePlayer(getTop3[2])">
        <div class="podium-avatar">{{ getTop3[2]?.name?.charAt(0) || '?' }}</div>
        <div class="podium-medal">🥉</div>
        <div class="podium-name">{{ getTop3[2]?.name || '虚位以待' }}</div>
        <div class="podium-score">{{ getTop3[2]?.streakDays || getTop3[2]?.avgScore || getTop3[2]?.practiceCount || 0 }}</div>
        <div class="podium-block">
          <span class="challenge-btn">挑战</span>
        </div>
      </div>
    </div>

    <div class="lb-card">
      <div class="lb-list-header">
        <span>排名</span>
        <span>用户</span>
        <span class="text-right">{{ tabMetric }}</span>
      </div>
      <div v-if="currentList.length === 0" class="lb-empty">
        <div class="lb-empty-icon">🏅</div>
        <div class="lb-empty-text">暂无数据，快来抢占榜首吧！</div>
      </div>
      <div v-else>
        <div
          v-for="(item, index) in restList"
          :key="index"
          class="lb-list-item"
          :class="{ 'is-me': item.id === data.currentUserId }"
          @click="challengePlayer(item)"
        >
          <div class="li-rank">
            <span class="rank-num">{{ index + 4 }}</span>
          </div>
          <div class="li-user">
            <div class="li-avatar">{{ item.name?.charAt(0) || '?' }}</div>
            <div class="li-info">
              <span class="li-name">{{ item.name || item.studentName || '匿名用户' }}</span>
              <span class="li-badge" v-if="item.id === data.currentUserId">我</span>
            </div>
          </div>
          <div class="li-score">
            <span class="li-score-val">{{ item.streakDays || item.avgScore || item.practiceCount || 0 }}</span>
            <span class="li-score-unit">{{ tabUnit }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="lb-hint" v-if="data.easterEggFound" @click="data.showEggDetail = !data.showEggDetail">
      🎉 你发现了领奖台彩蛋！<span class="hint-arrow">{{ data.showEggDetail ? '↑' : '↓' }}</span>
    </div>
    <div class="egg-detail" v-if="data.showEggDetail">
      <div class="egg-icon">🏆</div>
      <div class="egg-text">点击任意领奖台上的选手卡片即可发起挑战！</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import router from '@/router/index.js'

const data = reactive({
  activeTab: 'practice',
  practiceList: [],
  checkinList: [],
  examList: [],
  currentUserId: null,
  easterEggFound: false,
  showEggDetail: false,
})

const currentList = computed(() => {
  if (data.activeTab === 'practice') return data.practiceList
  if (data.activeTab === 'checkin') return data.checkinList
  return data.examList
})

const getTop3 = computed(() => currentList.value.slice(0, 3))
const restList = computed(() => currentList.value.slice(3))

const tabMetric = computed(() => {
  if (data.activeTab === 'practice') return '练习次数'
  if (data.activeTab === 'checkin') return '连续天数'
  return '平均分'
})

const tabUnit = computed(() => {
  if (data.activeTab === 'practice') return '次'
  if (data.activeTab === 'checkin') return '天'
  return '分'
})

const loadTab = (tab) => {
  if (tab === 'practice' && data.practiceList.length === 0) loadPractice()
  if (tab === 'checkin' && data.checkinList.length === 0) loadCheckin()
  if (tab === 'exam' && data.examList.length === 0) loadExam()
}

const loadPractice = () => {
  request.get('/practice/leaderboard').then(res => {
    data.practiceList = (res.data || []).slice(0, 20)
  }).catch(() => {})
}

const loadCheckin = () => {
  request.get('/studyCheckin/leaderboard').then(res => {
    data.checkinList = (res.data || []).slice(0, 20)
  }).catch(() => {})
}

const loadExam = () => {
  request.get('/examRecord/selectAll').then(res => {
    const records = (res.data || []).filter(r => r.status === 'completed' && r.totalScore !== null)
    const map = {}
    records.forEach(r => {
      if (!map[r.studentId]) map[r.studentId] = { studentName: r.studentName, scores: [], passCount: 0, id: r.studentId }
      map[r.studentId].scores.push(Number(r.totalScore) || 0)
      if (r.examStatus === 'PASSED') map[r.studentId].passCount++
    })
    data.examList = Object.values(map).map(s => ({
      ...s, avgScore: s.scores.length > 0 ? Math.round(s.scores.reduce((a, b) => a + b, 0) / s.scores.length) : 0
    })).sort((a, b) => b.passCount - a.passCount || b.avgScore - a.avgScore).slice(0, 20)
  }).catch(() => {})
}

const challengePlayer = (player) => {
  if (!player) return
  if (player.id === data.currentUserId) {
    ElMessage.info('不能挑战自己哦！')
    return
  }
  if (!data.easterEggFound) {
    data.easterEggFound = true
    request.post('/easterEgg/discover', {
      userId: data.currentUserId,
      userName: localStorage.getItem('xm-user') ? JSON.parse(localStorage.getItem('xm-user')).name : '',
      userRole: localStorage.getItem('xm-user') ? JSON.parse(localStorage.getItem('xm-user')).role : '',
      eggName: 'leaderboard_challenge'
    }).catch(() => {})
  }
  router.push({ path: '/front/practiceMode', query: { challenge: player.id, playerName: player.name || player.studentName } })
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  data.currentUserId = user.id
  loadTab('practice')
  loadCheckin()
  loadExam()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.lb-mode-switch :deep(.el-radio-button__inner) {
  border-radius: 20px !important;
  margin: 0 4px;
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3) !important;
  color: rgba(255, 255, 255, 0.8);
}
.lb-mode-switch :deep(.el-radio-button__inner:hover) {
  color: #fff;
}
.lb-mode-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  color: #fff;
  box-shadow: none;
}
.lb-mode-switch :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 20px !important;
}

.podium {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}
.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bg-card);
  border-radius: 16px;
  padding: 20px 16px 0;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
  box-shadow: var(--shadow-sm);
}
.podium-item:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}
.podium-item.first { order: 2; min-height: 200px; }
.podium-item.second { order: 1; min-height: 170px; }
.podium-item.third { order: 3; min-height: 150px; }

.podium-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  position: relative;
}
.podium-avatar.crown {
  background: linear-gradient(135deg, #ffd700, #ffaa00);
}
.crown-icon {
  position: absolute;
  top: -14px;
  font-size: 20px;
}
.podium-medal { font-size: 28px; margin-bottom: 4px; }
.podium-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  text-align: center;
}
.podium-score {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 12px;
}
.podium-block {
  width: 100%;
  background: var(--gradient-primary);
  border-radius: 0 0 14px 14px;
  padding: 8px;
  text-align: center;
}
.podium-item.first .podium-block {
  background: linear-gradient(135deg, #ffd700, #ffaa00);
}
.podium-item.second .podium-block {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
}
.podium-item.third .podium-block {
  background: linear-gradient(135deg, #cd7f32, #b06820);
}
.challenge-btn {
  font-size: 13px;
  color: #fff;
  font-weight: 600;
}

.lb-card {
  background: var(--bg-card);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.lb-list-header {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  padding: 14px 20px;
  background: var(--bg-page);
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
  border-bottom: 1px solid var(--border-lighter);
}
.text-right { text-align: right; }
.lb-empty { text-align: center; padding: 48px 20px; }
.lb-empty-icon { font-size: 48px; margin-bottom: 12px; }
.lb-empty-text { font-size: 14px; color: var(--text-secondary); }

.lb-list-item {
  display: grid;
  grid-template-columns: 60px 1fr 100px;
  padding: 14px 20px;
  align-items: center;
  border-bottom: 1px solid var(--border-lighter);
  cursor: pointer;
  transition: background 0.15s;
}
.lb-list-item:last-child { border-bottom: none; }
.lb-list-item:hover { background: var(--bg-page); }
.lb-list-item.is-me { background: var(--primary-light); }
.lb-list-item.is-me:hover { background: var(--primary-light); }

.li-rank { text-align: center; }
.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--bg-page);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 700;
}
.li-user { display: flex; align-items: center; gap: 10px; }
.li-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.li-info { display: flex; align-items: center; gap: 6px; }
.li-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.li-badge {
  background: var(--primary-color);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}
.li-score { text-align: right; }
.li-score-val { font-size: 16px; font-weight: 700; color: var(--primary-color); }
.li-score-unit { font-size: 11px; color: var(--text-secondary); margin-left: 2px; }

.lb-hint {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #e6a23c;
  cursor: pointer;
  padding: 10px;
  background: #fff9e6;
  border-radius: 8px;
  transition: all 0.2s;
}
.lb-hint:hover { background: #fff3cc; }
.hint-arrow { display: inline-block; transition: transform 0.2s; }
.egg-detail {
  margin-top: 8px;
  background: #fff3cc;
  border-radius: 8px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.egg-icon { font-size: 28px; }
.egg-text { font-size: 13px; color: #8a6d3b; line-height: 1.5; }

@media (max-width: 600px) {
  .podium-item { min-width: 90px; padding: 16px 10px 0; }
  .podium-avatar { width: 44px; height: 44px; font-size: 18px; }
  .podium-score { font-size: 16px; }
}
</style>
