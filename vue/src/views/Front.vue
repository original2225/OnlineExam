<template>
  <div class="fr">
    <!-- 顶栏 -->
    <header class="fr-header">
      <div class="frh-left" @click="handleBrandClick">
        <img src="@/assets/imgs/logo.png" alt="" class="frh-logo">
        <div class="frh-brand">
          <b>北冥审核系统</b>
          <span>进服审核</span>
        </div>
      </div>

      <nav class="frh-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="frh-tab"
          :class="{ active: isActive(item.path) }"
        >
          <div class="frh-tab-icon" :class="item.color"><el-icon><component :is="item.icon" /></el-icon></div>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="frh-right">
        <ThemeSwitcher />
        <NotificationBell />

        <template v-if="!data.user.id">
          <div class="frh-sep"></div>
          <el-button size="small" round @click="router.push('/login')" class="frh-ghost">登录</el-button>
          <el-button size="small" round type="primary" @click="router.push('/register')">注册</el-button>
        </template>

        <template v-else>
          <div class="frh-sep"></div>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="frh-user">
              <div class="frh-avatar">
                <img :src="data.user.avatar || defaultAvatar" alt="">
                <div class="frh-dot"></div>
              </div>
              <div class="frh-user-meta">
                <span class="frh-uname">{{ data.user.name }}</span>
                <span class="frh-urole" :style="{ color: roleColor, borderColor: roleColor }">{{ getRoleLabel(data.user.role) }}</span>
              </div>
              <el-icon class="frh-ua"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="person"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="scores"><el-icon><DataAnalysis /></el-icon> 审核结果</el-dropdown-item>
                <el-dropdown-item v-if="data.user.role !== 'USER'" command="manager" divided>
                  <el-icon><Setting /></el-icon> 管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </header>

    <!-- 公告条 -->
    <div class="fr-notice" v-if="data.noticeData.length">
      <span class="frn-badge">公告</span>
      <span class="frn-text">{{ data.topNotice }}</span>
    </div>

    <!-- 内容 -->
    <main class="fr-main">
      <router-view v-slot="{ Component, route: currentRoute }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="currentRoute.path" @updateUser="updateUser" />
        </transition>
      </router-view>
    </main>

    <!-- 底栏 -->
    <footer class="fr-footer">
      <div class="frf-inner">
        <div class="frf-left">
          <img src="@/assets/imgs/logo.png" alt="" class="frf-logo">
          <div class="frf-brand">
            <b>北冥审核系统服务器</b>
            <span>建筑 · 后期 · 红石 · 见习</span>
          </div>
        </div>
        <div class="frf-right">
          <a href="https://docs.beiming.games" target="_blank">技术文档</a>
          <span class="frf-sep">·</span>
          <span>&copy; 2024 北冥</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import router from "@/router/index.js"
import { reactive, onMounted, onUnmounted } from "vue"
import request from "@/utils/request.js"
import ThemeSwitcher from "@/components/ThemeSwitcher.vue"
import NotificationBell from "@/components/NotificationBell.vue"
import { ElMessage } from "element-plus"
import {
  HomeFilled, Collection, EditPen, Edit, WarnTriangleFilled, DataLine,
  Trophy, Reading, ArrowDown, User, DataAnalysis,
  Star, Setting, SwitchButton
} from "@element-plus/icons-vue"

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: [],
  topNotice: '',
  noticeIndex: 0,
  noticeTimer: null,
  brandClickCount: 0,
  brandClickTimer: null,
})

const navItems = [
  { path: '/front/home', label: '首页', icon: 'HomeFilled', color: '' },
  { path: '/front/subjects', label: '审核题库', icon: 'Collection', color: 'blue' },
  { path: '/front/examList', label: '进服审核', icon: 'EditPen', color: 'green' },
  { path: '/front/practiceMode', label: '审核模拟', icon: 'Edit', color: 'orange' },
  { path: '/front/wrongQuestions', label: '错题复盘', icon: 'WarnTriangleFilled', color: 'red' },
  ...(data.user.role && data.user.role !== 'USER' ? [{ path: '/front/contributeQuestion', label: '提交题目', icon: 'EditPen', color: 'teal' }] : []),
  { path: '/front/myScores', label: '审核结果', icon: 'DataAnalysis', color: 'purple' },
  { path: '/front/leaderboard', label: '审核榜单', icon: 'Trophy', color: 'gold' },
]

const roleColors = { OWNER: '#f87171', ADMIN: '#fbbf24', HELPER: '#60a5fa', USER: '#4ade80' }
const roleColor = roleColors[JSON.parse(localStorage.getItem('xm-user') || '{}').role] || '#4ade80'


const getRoleLabel = (role) => ({ OWNER: '所有者', ADMIN: '管理员', HELPER: '阅卷人', USER: '玩家' }[role] || '玩家')

const isActive = (path) => {
  const current = router.currentRoute.value.path
  return current === path || current.startsWith(path + '/')
}

const discoverEgg = (eggName) => {
  if (!data.user.id) return
  request.post('/easterEgg/discover', {
    userId: data.user.id,
    userName: data.user.name,
    userRole: data.user.role,
    eggName
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success(res.data?.alreadyDiscovered ? '你已经发现过北冥入口彩蛋' : '发现彩蛋：北冥入口守门人')
    }
  }).catch(() => {})
}

const handleBrandClick = () => {
  data.brandClickCount++
  clearTimeout(data.brandClickTimer)
  data.brandClickTimer = setTimeout(() => { data.brandClickCount = 0 }, 1800)
  if (data.brandClickCount >= 7) {
    data.brandClickCount = 0
    discoverEgg('beiming_entry_gate')
  } else {
    router.push('/')
  }
}

const handleCommand = (cmd) => {
  const map = { person: '/front/person', scores: '/front/myScores', manager: '/manager/home' }
  if (cmd === 'logout') { localStorage.removeItem('xm-user'); router.push('/login') }
  else if (map[cmd]) router.push(map[cmd])
}

const updateUser = () => { data.user = JSON.parse(localStorage.getItem('xm-user') || '{}') }

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if (res.code === '200' && res.data?.length) {
      data.noticeData = res.data
      data.topNotice = res.data[0]?.content || ''
      if (res.data.length > 1) {
        data.noticeTimer = setInterval(() => {
          data.noticeIndex = (data.noticeIndex + 1) % data.noticeData.length
          data.topNotice = data.noticeData[data.noticeIndex]?.content || ''
        }, 4000)
      }
    }
  }).catch(() => {})
}

onMounted(() => loadNotice())
onUnmounted(() => {
  if (data.noticeTimer) clearInterval(data.noticeTimer)
  if (data.brandClickTimer) clearTimeout(data.brandClickTimer)
})
</script>

<style scoped>
.fr { display: flex; flex-direction: column; min-height: 100vh; background: var(--bg-page); }

/* ===== 顶栏 ===== */
.fr-header {
  height: 58px;
  background: var(--gradient-header);
  display: flex; align-items: center;
  padding: 0 20px; gap: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.2);
  position: sticky; top: 0; z-index: 100;
}

.frh-left {
  display: flex; align-items: center; gap: 10px;
  cursor: pointer; padding: 4px 10px; border-radius: 8px;
  transition: background 0.2s; flex-shrink: 0;
}
.frh-left:hover { background: rgba(255,255,255,0.07); }
.frh-logo { width: 32px; height: 32px; border-radius: 8px; border: 2px solid rgba(255,255,255,0.12); }
.frh-brand { display: flex; flex-direction: column; line-height: 1.15; }
.frh-brand b { color: #fff; font-size: 15px; letter-spacing: 0.5px; }
.frh-brand span { color: rgba(255,255,255,0.3); font-size: 9px; letter-spacing: 2px; }

/* 导航 */
.frh-nav {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  gap: 2px; overflow-x: auto;
}
.frh-nav::-webkit-scrollbar { height: 0; }

.frh-tab {
  display: flex; align-items: center; gap: 5px;
  padding: 5px 12px; border-radius: 6px;
  font-size: 13px; font-weight: 500;
  color: rgba(255,255,255,0.5);
  text-decoration: none; transition: all 0.2s;
  white-space: nowrap; position: relative;
}
.frh-tab:hover { color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.05); }

.frh-tab.active {
  color: #fff; font-weight: 600;
  background: rgba(255,255,255,0.08);
}
.frh-tab.active::after {
  content: ''; position: absolute; bottom: 0;
  left: 50%; transform: translateX(-50%);
  width: 16px; height: 2px; border-radius: 1px;
  background: var(--primary-color, #00b42a);
}

.frh-tab-icon {
  width: 24px; height: 24px; border-radius: 5px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; background: rgba(255,255,255,0.06);
  color: rgba(255,255,255,0.5); transition: all 0.2s;
}
.frh-tab:hover .frh-tab-icon { background: rgba(255,255,255,0.1); color: #fff; }
.frh-tab.active .frh-tab-icon { background: rgba(0,180,42,0.2); color: #4ade80; }

.frh-tab-icon.blue { background: rgba(96,165,250,0.15); color: #60a5fa; }
.frh-tab-icon.green { background: rgba(74,222,128,0.15); color: #4ade80; }
.frh-tab-icon.cyan { background: rgba(34,211,238,0.15); color: #22d3ee; }
.frh-tab-icon.orange { background: rgba(251,146,60,0.15); color: #fb923c; }
.frh-tab-icon.purple { background: rgba(167,139,250,0.15); color: #a78bfa; }
.frh-tab-icon.gold { background: rgba(251,191,36,0.15); color: #fbbf24; }
.frh-tab-icon.teal { background: rgba(45,212,191,0.15); color: #2dd4bf; }
.frh-tab-icon.pink { background: rgba(244,114,182,0.15); color: #f472b6; }

/* 右侧 */
.frh-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.frh-right :deep(.theme-trigger) { color: rgba(255,255,255,0.6); }
.frh-right :deep(.theme-trigger:hover) { background: rgba(255,255,255,0.1); }
.frh-sep { width: 1px; height: 20px; background: rgba(255,255,255,0.1); }

.frh-ghost {
  color: rgba(255,255,255,0.75) !important;
  border-color: rgba(255,255,255,0.2) !important;
  background: transparent !important;
}
.frh-ghost:hover { color: #fff !important; border-color: rgba(255,255,255,0.5) !important; }

.frh-user {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 3px 10px 3px 3px;
  border-radius: 18px; border: 1px solid rgba(255,255,255,0.08);
  transition: all 0.2s;
}
.frh-user:hover { background: rgba(255,255,255,0.07); border-color: rgba(255,255,255,0.15); }

.frh-avatar { position: relative; }
.frh-avatar img { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; border: 2px solid rgba(255,255,255,0.2); }
.frh-dot {
  position: absolute; bottom: -1px; right: -1px;
  width: 8px; height: 8px; border-radius: 50%;
  background: #4ade80; border: 2px solid #1d2129;
}

.frh-user-meta { display: flex; flex-direction: column; }
.frh-uname { color: rgba(255,255,255,0.9); font-size: 12px; font-weight: 600; line-height: 1.2; }
.frh-urole {
  font-size: 9px; font-weight: 700; margin-top: 1px;
  border: 1px solid; border-radius: 3px; padding: 0 4px; width: fit-content;
}
.frh-ua { color: rgba(255,255,255,0.35); font-size: 11px; }

/* ===== 公告 ===== */
.fr-notice {
  display: flex; align-items: center; gap: 10px;
  padding: 5px 24px; font-size: 13px; color: var(--text-secondary);
  background: rgba(0,0,0,0.02); border-bottom: 1px solid var(--border-lighter);
}
.frn-badge {
  padding: 1px 8px; border-radius: 3px;
  background: var(--primary-color, #00b42a); color: #fff;
  font-size: 10px; font-weight: 700; flex-shrink: 0; letter-spacing: 1px;
}
.frn-text { overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }

/* ===== 内容 ===== */
.fr-main { flex: 1; }

/* ===== 底栏 ===== */
.fr-footer {
  background: linear-gradient(180deg, var(--bg-card), rgba(0,0,0,0.015));
  border-top: 1px solid var(--border-lighter);
  padding: 24px 20px;
}

.frf-inner {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center; justify-content: space-between;
}

.frf-left { display: flex; align-items: center; gap: 10px; }
.frf-logo { width: 24px; height: 24px; border-radius: 6px; }
.frf-brand { display: flex; flex-direction: column; }
.frf-brand b { font-size: 13px; color: var(--text-primary); }
.frf-brand span { font-size: 10px; color: var(--text-secondary); margin-top: 1px; }

.frf-right { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-secondary); }
.frf-right a { color: var(--primary-color); font-weight: 500; transition: opacity 0.2s; }
.frf-right a:hover { opacity: 0.7; }
.frf-sep { opacity: 0.3; }

@media (max-width: 900px) {
  .frh-nav { display: none; }
  .fr-header { padding: 0 12px; }
}
@media (max-width: 600px) {
  .frh-brand { display: none; }
  .frf-inner { flex-direction: column; gap: 8px; text-align: center; }
}
</style>
