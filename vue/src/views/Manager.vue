<template>
  <div class="admin-shell" :class="{ collapsed: data.collapsed }">
    <aside class="admin-sidebar">
      <button class="brand" type="button" @click="router.push('/manager/home')">
        <img src="@/assets/imgs/logo.png" alt="北冥审核系统">
        <span class="brand-copy">
          <strong>北冥审核</strong>
          <small>管理控制台</small>
        </span>
      </button>

      <div class="operator">
        <img :src="data.user.avatar || defaultAvatar" alt="">
        <div class="operator-copy">
          <strong>{{ data.user.name || data.user.username || '管理员' }}</strong>
          <span :style="{ color: roleColor }">{{ getRoleLabel(data.user.role) }}</span>
        </div>
      </div>

      <button class="review-entry" type="button" @click="router.push('/exam/dashboard')">
        <el-icon><Checked /></el-icon>
        <span>批阅工作台</span>
      </button>

      <nav class="admin-nav" aria-label="管理后台导航">
        <section v-for="group in visibleNavGroups" :key="group.title" class="nav-group">
          <p class="nav-title">{{ group.title }}</p>
          <button
            v-for="item in group.items"
            :key="item.path"
            class="nav-item"
            :class="{ active: isCurrent(item.path) }"
            type="button"
            @click="router.push(item.path)"
          >
            <span class="nav-icon" :class="item.tone">
              <el-icon><component :is="item.icon" /></el-icon>
            </span>
            <span class="nav-label">{{ item.label }}</span>
          </button>
        </section>
      </nav>

      <div class="sidebar-tools">
        <button type="button" class="fold-btn" @click="data.collapsed = !data.collapsed">
          <el-icon><component :is="data.collapsed ? 'Expand' : 'Fold'" /></el-icon>
          <span>{{ data.collapsed ? '展开' : '收起' }}</span>
        </button>
      </div>
    </aside>

    <section class="admin-workspace">
      <header class="admin-topbar">
        <div class="topbar-left">
          <button class="mobile-menu" type="button" @click="data.collapsed = !data.collapsed">
            <el-icon><Menu /></el-icon>
          </button>
          <div>
            <p class="eyebrow">{{ greeting }}</p>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>

        <div class="topbar-right">
          <ThemeSwitcher />
          <ExamRecordChatLauncher />
          <NotificationBell />
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-menu" type="button">
              <img :src="data.user.avatar || defaultAvatar" alt="">
              <span>{{ data.user.name || data.user.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front"><el-icon><Monitor /></el-icon> 学生前台</el-dropdown-item>
                <el-dropdown-item command="person"><el-icon><User /></el-icon> 个人资料</el-dropdown-item>
                <el-dropdown-item command="password"><el-icon><Lock /></el-icon> 修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <router-view v-slot="{ Component, route: currentRoute }">
          <Suspense>
            <transition name="fade" mode="out-in">
              <component :is="Component" :key="currentRoute.path" @updateUser="updateUser" />
            </transition>
            <template #fallback>
              <div class="loading-panel">页面加载中...</div>
            </template>
          </Suspense>
        </router-view>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import router from "@/router/index.js"
import ThemeSwitcher from "@/components/ThemeSwitcher.vue"
import NotificationBell from "@/components/NotificationBell.vue"
import ExamRecordChatLauncher from "@/components/ExamRecordChatLauncher.vue"

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}'),
  collapsed: false,
})

const roleColors = { OWNER: '#b45309', ADMIN: '#b45309', HELPER: '#b45309', USER: '#0f9f6e' }
const roleColor = computed(() => roleColors[data.user.role] || '#0f9f6e')
const isAdmin = computed(() => ['OWNER', 'ADMIN'].includes(data.user.role))

const getRoleLabel = (role) => (role === 'USER' ? '玩家' : '管理员')

const navGroups = computed(() => [
  {
    title: '总览',
    items: [
      { path: '/manager/home', label: '控制台', icon: 'DataBoard', tone: 'green' },
    ],
  },
  {
    title: '成员',
    adminOnly: true,
    items: [
      { path: '/manager/admin', label: '管理员', icon: 'UserFilled', tone: 'red' },
      { path: '/manager/student', label: '玩家', icon: 'User', tone: 'green' },
      { path: '/manager/registrationApproval', label: '注册审批', icon: 'Stamp', tone: 'orange' },
      { path: '/manager/invitationCode', label: '邀请码', icon: 'Ticket', tone: 'purple' },
    ],
  },
  {
    title: '题库',
    items: [
      ...(isAdmin.value ? [{ path: '/manager/questionCategory', label: '题目分类', icon: 'FolderOpened', tone: 'teal' }] : []),
      { path: '/manager/question', label: '题目列表', icon: 'Collection', tone: 'blue' },
      ...(isAdmin.value ? [{ path: '/manager/questionReview', label: '题目审核', icon: 'DocumentChecked', tone: 'orange' }] : []),
    ],
  },
  {
    title: '审核',
    items: [
      { path: '/manager/examPaper', label: '试卷管理', icon: 'Document', tone: 'purple' },
      { path: '/manager/exam', label: '审核管理', icon: 'EditPen', tone: 'green' },
      { path: '/manager/grading', label: '批阅管理', icon: 'Finished', tone: 'cyan' },
      { path: '/manager/score', label: '结果管理', icon: 'DataAnalysis', tone: 'orange' },
    ],
  },
])

const visibleNavGroups = computed(() => navGroups.value.filter(group => !group.adminOnly || isAdmin.value))
const currentTitle = computed(() => router.currentRoute.value.meta?.name || '管理后台')

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜间值守'
  if (h < 12) return '上午好'
  if (h < 14) return '午间巡检'
  if (h < 18) return '下午好'
  return '晚间复盘'
})

const isCurrent = (path) => router.currentRoute.value.path === path

const handleCommand = (cmd) => {
  const map = { front: '/front/home', person: '/manager/person', password: '/manager/password' }
  if (cmd === 'logout') {
    localStorage.removeItem('beiming-onlineexam-user')
    router.push('/login')
  } else if (map[cmd]) {
    router.push(map[cmd])
  }
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
}

const preloadManagerPages = () => {
  const pages = [
    () => import('@/views/manager/Admin.vue'),
    () => import('@/views/manager/Student.vue'),
    () => import('@/views/manager/Question.vue'),
    () => import('@/views/manager/Exam.vue'),
    () => import('@/views/manager/Score.vue'),
  ]
  pages.forEach(load => load())
}

onMounted(() => setTimeout(preloadManagerPages, 500))

if (!data.user.id) {
  ElMessage.error('请登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-shell {
  display: grid;
  grid-template-columns: 264px minmax(0, 1fr);
  min-height: 100vh;
  background: var(--bg-page);
}

.admin-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 14px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255,255,255,0.06), transparent),
    var(--gradient-header);
  color: #fff;
}

.brand,
.review-entry,
.nav-item,
.fold-btn,
.mobile-menu,
.user-menu {
  border: 0;
  cursor: pointer;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 48px;
  padding: 6px 8px;
  color: #fff;
  background: transparent;
  text-align: left;
}

.brand img {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  object-fit: cover;
}

.brand-copy {
  display: grid;
  line-height: 1.2;
}

.brand-copy strong {
  font-size: 16px;
}

.brand-copy small {
  color: rgba(255,255,255,0.52);
  font-size: 11px;
}

.operator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(255,255,255,0.09);
  border-radius: 10px;
  background: rgba(255,255,255,0.06);
}

.operator img,
.user-menu img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.operator-copy {
  display: grid;
  min-width: 0;
}

.operator-copy strong {
  overflow: hidden;
  color: rgba(255,255,255,0.95);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.operator-copy span {
  font-size: 11px;
  font-weight: 700;
}

.review-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 13px;
  border-radius: 8px;
  color: #fff;
  background: rgba(var(--primary-rgb), 0.42);
  font-weight: 700;
}

.admin-nav {
  flex: 1;
  overflow-y: auto;
  padding-right: 2px;
}

.nav-group {
  margin-top: 14px;
}

.nav-title {
  margin: 0 0 7px;
  padding-left: 10px;
  color: rgba(255,255,255,0.36);
  font-size: 11px;
  font-weight: 800;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 38px;
  margin-bottom: 4px;
  padding: 0 10px;
  border-radius: 8px;
  color: rgba(255,255,255,0.64);
  background: transparent;
  text-align: left;
  transition: background 0.16s ease, color 0.16s ease;
}

.nav-item:hover,
.nav-item.active {
  color: #fff;
  background: rgba(255,255,255,0.09);
}

.nav-item.active {
  box-shadow: inset 3px 0 0 var(--primary-color);
}

.nav-icon {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: rgba(255,255,255,0.07);
}

.nav-icon.green { color: #34d399; }
.nav-icon.blue { color: #60a5fa; }
.nav-icon.red { color: #f87171; }
.nav-icon.cyan { color: #22d3ee; }
.nav-icon.orange { color: #f59e0b; }
.nav-icon.purple { color: #a78bfa; }
.nav-icon.teal { color: #2dd4bf; }

.sidebar-tools {
  border-top: 1px solid rgba(255,255,255,0.08);
  padding-top: 12px;
}

.fold-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 36px;
  border-radius: 8px;
  color: rgba(255,255,255,0.52);
  background: rgba(255,255,255,0.05);
}

.admin-workspace {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  position: sticky;
  top: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 72px;
  padding: 0 26px;
  border-bottom: 1px solid var(--border-lighter);
  background: color-mix(in srgb, var(--bg-card) 92%, transparent);
  backdrop-filter: blur(16px);
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.eyebrow {
  margin: 0;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 700;
}

.topbar-left h1 {
  margin: 0;
  color: var(--text-primary);
  font-size: 20px;
}

.mobile-menu {
  display: none;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: var(--text-primary);
  background: var(--bg-muted);
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 3px 10px 3px 4px;
  border: 1px solid var(--border-lighter);
  border-radius: 999px;
  color: var(--text-primary);
  background: var(--bg-card);
}

.admin-content {
  flex: 1;
  padding: 24px 28px 48px;
}

.loading-panel {
  display: grid;
  place-items: center;
  min-height: 240px;
  color: var(--text-secondary);
}

.admin-shell.collapsed {
  grid-template-columns: 76px minmax(0, 1fr);
}

.admin-shell.collapsed .brand-copy,
.admin-shell.collapsed .operator-copy,
.admin-shell.collapsed .review-entry span,
.admin-shell.collapsed .nav-title,
.admin-shell.collapsed .nav-label,
.admin-shell.collapsed .fold-btn span {
  display: none;
}

.admin-shell.collapsed .operator,
.admin-shell.collapsed .brand,
.admin-shell.collapsed .review-entry,
.admin-shell.collapsed .nav-item {
  justify-content: center;
}

@media (max-width: 920px) {
  .admin-shell,
  .admin-shell.collapsed {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: fixed;
    z-index: 100;
    width: 264px;
    transform: translateX(-100%);
    transition: transform 0.18s ease;
  }

  .admin-shell:not(.collapsed) .admin-sidebar {
    transform: translateX(0);
  }

  .mobile-menu {
    display: grid;
    place-items: center;
  }

  .admin-content {
    padding: 16px 12px 36px;
  }

  .admin-topbar {
    padding: 0 12px;
  }
}
</style>
