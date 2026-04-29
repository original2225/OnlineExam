<template>
  <div class="ex-layout">
    <!-- 侧边栏 -->
    <aside class="ex-sidebar" :class="{ fold: data.fold }">
      <!-- Logo -->
      <div class="exs-logo" @click="router.push('/')">
        <img src="@/assets/imgs/logo.png" alt="">
        <transition name="fade">
          <div v-if="!data.fold" class="exs-logo-text">
            <b>北冥</b>
            <span>批阅中心</span>
          </div>
        </transition>
      </div>

      <!-- 成员卡片 -->
      <div class="exs-profile" v-if="!data.fold">
        <img :src="data.user.avatar || defaultAvatar" class="exs-avatar" alt="">
        <div class="exs-profile-info">
          <strong>{{ data.user.name }}</strong>
          <span class="exs-profile-role" :style="{ color: roleColor, borderColor: roleColor }">{{ getRoleLabel(data.user.role) }}</span>
        </div>
      </div>
      <div v-else class="exs-profile-mini">
        <img :src="data.user.avatar || defaultAvatar" alt="">
      </div>

      <!-- 快捷返回 -->
      <div class="exs-back-btn" @click="router.push(isAdmin ? '/manager/home' : '/front/home')">
        <div class="exs-back-shine"></div>
        <el-icon :size="14"><Back /></el-icon>
        <span v-if="!data.fold">{{ isAdmin ? '管理后台' : '玩家前台' }}</span>
      </div>

      <!-- 导航 -->
      <nav class="exs-nav">
        <div class="exs-group" v-for="group in visibleGroups" :key="group.title">
          <div v-if="!data.fold" class="exs-group-title">{{ group.title }}</div>
          <div
            v-for="item in group.items"
            :key="item.path"
            class="exs-item"
            :class="{ active: isCurrent(item.path) }"
            @click="nav(item.path)"
          >
            <div class="exs-item-icon" :class="item.color"><el-icon><component :is="item.icon" /></el-icon></div>
            <span v-if="!data.fold">{{ item.label }}</span>
          </div>
        </div>
      </nav>

      <!-- 底部折叠 -->
      <div class="exs-footer">
        <div class="exs-fold" @click="data.fold = !data.fold">
          <el-icon><component :is="data.fold ? 'DArrowRight' : 'DArrowLeft'" /></el-icon>
          <span v-if="!data.fold">收起</span>
        </div>
      </div>
    </aside>

    <!-- 右侧 -->
    <div class="ex-right">
      <!-- 顶栏 -->
      <header class="ex-topbar">
        <div class="ext-left">
          <span class="ext-page-title">{{ currentPageName }}</span>
        </div>
        <div class="ext-right">
          <ThemeSwitcher />
          <ExamRecordChatLauncher />
          <NotificationBell />
        </div>
      </header>

      <!-- 内容 -->
      <main class="ex-content">
        <router-view v-slot="{ Component, route: currentRoute }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="currentRoute.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, watch } from 'vue'
import router from '@/router/index.js'
import { ElMessage } from 'element-plus'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import ExamRecordChatLauncher from '@/components/ExamRecordChatLauncher.vue'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}'),
  fold: false,
})

const roleColors = { OWNER: '#fbbf24', ADMIN: '#fbbf24', HELPER: '#fbbf24', USER: '#4ade80' }
const roleColor = computed(() => roleColors[data.user.user?.role || data.user.role] || '#4ade80')
const getRoleLabel = (role) => (role === 'USER' ? '玩家' : '管理员')

const isAdmin = computed(() => ['OWNER', 'ADMIN'].includes(data.user.role))

const allGroups = [
  {
    title: '概览',
    items: [
      { path: '/exam/dashboard', label: '工作台', icon: 'HomeFilled', color: '', roles: ['OWNER', 'ADMIN', 'HELPER', 'USER'] },
    ]
  },
  {
    title: '管理',
    items: [
      { path: '/exam/examAdmin', label: '审核管理', icon: 'Document', color: 'blue', roles: ['OWNER', 'ADMIN'] },
      { path: '/exam/approvalCenter', label: '审批中心', icon: 'Stamp', color: 'orange', roles: ['OWNER', 'ADMIN'] },
      { path: '/exam/scoreManage', label: '结果管理', icon: 'DataLine', color: 'purple', roles: ['OWNER', 'ADMIN'] },
    ]
  },
  {
    title: '批阅',
    items: [
      { path: '/exam/gradingCenter', label: '批阅中心', icon: 'Checked', color: 'green', roles: ['OWNER', 'ADMIN', 'HELPER'] },
    ]
  },
  {
    title: '公示',
    items: [
      { path: '/exam/resultsCenter', label: '结果公示', icon: 'DataAnalysis', color: 'cyan', roles: ['OWNER', 'ADMIN', 'HELPER', 'USER'] },
    ]
  },
]

const visibleGroups = computed(() =>
  allGroups.map(g => ({
    ...g,
    items: g.items.filter(i => i.roles.includes(data.user.role))
  })).filter(g => g.items.length > 0)
)

const isCurrent = (p) => router.currentRoute.value.path === p
const nav = (p) => router.push(p)

const pageNameMap = {
  '/exam/dashboard': '工作台',
  '/exam/examAdmin': '审核管理',
  '/exam/approvalCenter': '审批中心',
  '/exam/scoreManage': '结果管理',
  '/exam/gradingCenter': '批阅中心',
  '/exam/resultsCenter': '结果公示',
}
const currentPageName = computed(() => pageNameMap[router.currentRoute.value.path] || '')

const transitionName = ref('page')
watch(() => router.currentRoute.value.path, (to, from) => {
  transitionName.value = from?.startsWith('/exam') && to === '/exam/dashboard' ? 'zoom' : 'page'
})

if (!data.user.id) { router.push('/login'); ElMessage.error('请登录！') }
</script>

<style scoped>
@import "@/assets/css/transitions.css";

.ex-layout { display: flex; min-height: 100vh; }

/* ===== 侧边栏 ===== */
.ex-sidebar {
  width: 230px;
  background: linear-gradient(180deg, #151820 0%, #1a1f2b 40%, #181c26 100%);
  color: rgba(255,255,255,0.8);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  overflow: hidden;
  user-select: none;
}
.ex-sidebar.fold { width: 62px; }

/* Logo */
.exs-logo {
  display: flex; align-items: center; gap: 12px;
  padding: 18px 16px 14px; cursor: pointer;
}
.exs-logo:hover { opacity: 0.85; }
.exs-logo img { width: 34px; height: 34px; border-radius: 10px; flex-shrink: 0; }
.exs-logo-text { display: flex; flex-direction: column; line-height: 1.15; white-space: nowrap; }
.exs-logo-text b { font-size: 16px; color: #fff; letter-spacing: 1px; }
.exs-logo-text span { font-size: 10px; color: rgba(255,255,255,0.3); letter-spacing: 2px; }

/* 成员卡 */
.exs-profile {
  display: flex; align-items: center; gap: 10px;
  margin: 0 14px 10px; padding: 10px 12px;
  background: rgba(255,255,255,0.04);
  border-radius: 10px; border: 1px solid rgba(255,255,255,0.05);
}
.exs-avatar { width: 34px; height: 34px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.exs-profile-info { display: flex; flex-direction: column; min-width: 0; }
.exs-profile-info strong { font-size: 13px; color: rgba(255,255,255,0.9); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.exs-profile-role {
  font-size: 10px; font-weight: 700; margin-top: 3px;
  border: 1px solid; border-radius: 4px; padding: 0 5px; width: fit-content;
}
.exs-profile-mini { padding: 6px 0; text-align: center; }
.exs-profile-mini img { width: 30px; height: 30px; border-radius: 50%; }

/* 返回按钮 */
.exs-back-btn {
  position: relative;
  margin: 0 14px 12px;
  padding: 8px 14px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 8px;
  display: flex; align-items: center; gap: 8px;
  font-size: 12px; font-weight: 500;
  color: rgba(255,255,255,0.45);
  cursor: pointer; overflow: hidden; transition: all 0.2s; white-space: nowrap;
}
.exs-back-btn:hover { color: rgba(255,255,255,0.8); background: rgba(255,255,255,0.07); }
.exs-back-shine {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.04), transparent);
  animation: shine 4s ease-in-out infinite;
}
@keyframes shine { 0%,100%{transform:translateX(-100%)} 50%{transform:translateX(100%)} }

/* 导航 */
.exs-nav { flex: 1; overflow-y: auto; padding: 0 10px; }
.exs-nav::-webkit-scrollbar { width: 3px; }
.exs-nav::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.exs-group { margin-bottom: 2px; }
.exs-group-title {
  font-size: 10px; font-weight: 700;
  color: rgba(255,255,255,0.2);
  text-transform: uppercase; letter-spacing: 2px;
  padding: 12px 10px 4px;
}

.exs-item {
  display: flex; align-items: center; gap: 10px;
  padding: 0 10px; height: 36px;
  border-radius: 8px; font-size: 13px; font-weight: 500;
  color: rgba(255,255,255,0.5); cursor: pointer; transition: all 0.15s;
  white-space: nowrap; position: relative;
}
.exs-item:hover { color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.05); }
.exs-item.active { color: #fff; background: rgba(0,180,42,0.15); font-weight: 600; }
.exs-item.active::before {
  content: ''; position: absolute; left: 0; top: 6px; bottom: 6px;
  width: 3px; border-radius: 0 3px 3px 0;
  background: var(--primary-color, #00b42a);
}

.exs-item-icon {
  width: 28px; height: 28px; border-radius: 7px;
  background: rgba(255,255,255,0.06);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: rgba(255,255,255,0.5); flex-shrink: 0; transition: all 0.15s;
}
.exs-item:hover .exs-item-icon { color: rgba(255,255,255,0.8); background: rgba(255,255,255,0.1); }
.exs-item.active .exs-item-icon { background: rgba(0,180,42,0.25); color: #4ade80; }

.exs-item-icon.blue { background: rgba(59,130,246,0.15); color: #60a5fa; }
.exs-item-icon.cyan { background: rgba(34,211,238,0.12); color: #22d3ee; }
.exs-item-icon.green { background: rgba(74,222,128,0.12); color: #4ade80; }
.exs-item-icon.orange { background: rgba(251,146,60,0.15); color: #fb923c; }
.exs-item-icon.purple { background: rgba(167,139,250,0.15); color: #a78bfa; }
.exs-item-icon.teal { background: rgba(45,212,191,0.12); color: #2dd4bf; }
.exs-item.active .exs-item-icon.blue { background: rgba(59,130,246,0.2); color: #60a5fa; }
.exs-item.active .exs-item-icon.cyan { background: rgba(34,211,238,0.18); color: #22d3ee; }
.exs-item.active .exs-item-icon.green { background: rgba(74,222,128,0.2); color: #4ade80; }
.exs-item.active .exs-item-icon.orange { background: rgba(251,146,60,0.2); color: #fb923c; }
.exs-item.active .exs-item-icon.purple { background: rgba(167,139,250,0.2); color: #a78bfa; }
.exs-item.active .exs-item-icon.teal { background: rgba(45,212,191,0.18); color: #2dd4bf; }

/* 底部 */
.exs-footer { padding: 10px 14px; border-top: 1px solid rgba(255,255,255,0.04); }
.exs-fold {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 10px; border-radius: 6px;
  font-size: 12px; color: rgba(255,255,255,0.25); cursor: pointer; transition: all 0.2s;
}
.exs-fold:hover { color: rgba(255,255,255,0.6); background: rgba(255,255,255,0.04); }

/* 折叠态 */
.ex-sidebar.fold .exs-logo { justify-content: center; padding: 14px 0; }
.ex-sidebar.fold .exs-back-btn { justify-content: center; padding: 8px; margin: 0 8px 12px; }
.ex-sidebar.fold .exs-item { justify-content: center; padding: 0; }
.ex-sidebar.fold .exs-fold { justify-content: center; }
.ex-sidebar.fold .exs-nav { padding: 0 6px; }

/* ===== 右侧 ===== */
.ex-right { flex: 1; display: flex; flex-direction: column; min-width: 0; background: var(--bg-page); }

/* 顶栏 */
.ex-topbar {
  height: 52px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-lighter);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; position: sticky; top: 0; z-index: 50;
}
.ext-left { display: flex; align-items: center; }
.ext-page-title { font-size: 14px; font-weight: 700; color: var(--text-primary); }
.ext-right { display: flex; align-items: center; gap: 4px; }

/* 内容 */
.ex-content { flex: 1; padding: 20px 24px; overflow-y: auto; }

@media (max-width: 768px) {
  .ex-sidebar { width: 62px; }
  .ex-sidebar .exs-logo-text,
  .ex-sidebar .exs-profile,
  .ex-sidebar .exs-group-title,
  .ex-sidebar .exs-item span,
  .ex-sidebar .exs-back-btn span,
  .ex-sidebar .exs-fold span { display: none; }
  .ex-sidebar .exs-logo { justify-content: center; }
  .ex-sidebar .exs-back-btn { justify-content: center; }
  .ex-sidebar .exs-item { justify-content: center; }
  .ex-sidebar .exs-fold { justify-content: center; }
  .ex-content { padding: 12px; }
}
</style>
