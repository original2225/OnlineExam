<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ fold: data.fold }">
      <!-- Logo -->
      <div class="sb-logo" @click="goHome">
        <img src="@/assets/imgs/logo.png" alt="">
        <transition name="fade">
          <div v-if="!data.fold" class="sb-logo-text">
            <b>北冥</b>
            <span>管理后台</span>
          </div>
        </transition>
      </div>

      <!-- 用户卡片 -->
      <div class="sb-profile" v-if="!data.fold">
        <img :src="data.user.avatar || defaultAvatar" class="sb-avatar" alt="">
        <div class="sb-profile-info">
          <strong>{{ data.user.name }}</strong>
          <span class="sb-profile-role" :style="{ color: roleColor, borderColor: roleColor }">{{ getRoleLabel(data.user.role) }}</span>
        </div>
      </div>
      <div v-else class="sb-profile-mini">
        <img :src="data.user.avatar || defaultAvatar" alt="">
      </div>

      <!-- 阅卷入口 -->
      <div class="sb-exam-btn" @click="router.push('/exam/dashboard')">
        <div class="sb-exam-shine"></div>
        <el-icon :size="15"><Checked /></el-icon>
        <span v-if="!data.fold">进入阅卷系统</span>
        <el-icon v-if="!data.fold" style="margin-left:auto;font-size:12px;opacity:.5"><ArrowRight /></el-icon>
      </div>

      <!-- 导航列表 -->
      <nav class="sb-nav">
        <div class="sb-group">
          <div class="sb-item" :class="{ active: isCurrent('/manager/home') }" @click="nav('/manager/home')">
            <div class="sb-item-icon"><el-icon><HomeFilled /></el-icon></div>
            <span v-if="!data.fold">控制台</span>
          </div>
        </div>

        <div class="sb-group">
          <div v-if="!data.fold" class="sb-group-title">内容</div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/notice') }" @click="nav('/manager/notice')">
            <div class="sb-item-icon"><el-icon><Bell /></el-icon></div>
            <span v-if="!data.fold">系统公告</span>
          </div>
        </div>

        <div class="sb-group" v-if="isAdmin">
          <div v-if="!data.fold" class="sb-group-title">用户</div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/admin') }" @click="nav('/manager/admin')">
            <div class="sb-item-icon blue"><el-icon><UserFilled /></el-icon></div>
            <span v-if="!data.fold">管理员</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/examiner') }" @click="nav('/manager/examiner')">
            <div class="sb-item-icon cyan"><el-icon><Checked /></el-icon></div>
            <span v-if="!data.fold">阅卷人</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/student') }" @click="nav('/manager/student')">
            <div class="sb-item-icon green"><el-icon><User /></el-icon></div>
            <span v-if="!data.fold">用户</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/registrationApproval') }" @click="nav('/manager/registrationApproval')">
            <div class="sb-item-icon orange"><el-icon><Stamp /></el-icon></div>
            <span v-if="!data.fold">注册审批</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/invitationCode') }" @click="nav('/manager/invitationCode')">
            <div class="sb-item-icon purple"><el-icon><Ticket /></el-icon></div>
            <span v-if="!data.fold">邀请码</span>
          </div>
        </div>

        <div class="sb-group">
          <div v-if="!data.fold" class="sb-group-title">题库</div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/questionCategory') }" @click="nav('/manager/questionCategory')" v-if="isAdmin">
            <div class="sb-item-icon teal"><el-icon><FolderOpened /></el-icon></div>
            <span v-if="!data.fold">题目分类</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/question') }" @click="nav('/manager/question')">
            <div class="sb-item-icon"><el-icon><Collection /></el-icon></div>
            <span v-if="!data.fold">题目列表</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/questionReview') }" @click="nav('/manager/questionReview')">
            <div class="sb-item-icon"><el-icon><DocumentChecked /></el-icon></div>
            <span v-if="!data.fold">题目审核</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/tutorial') }" @click="nav('/manager/tutorial')">
            <div class="sb-item-icon gold"><el-icon><Reading /></el-icon></div>
            <span v-if="!data.fold">例题教程</span>
          </div>
        </div>

        <div class="sb-group">
          <div v-if="!data.fold" class="sb-group-title">考试</div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/examPaper') }" @click="nav('/manager/examPaper')">
            <div class="sb-item-icon"><el-icon><Document /></el-icon></div>
            <span v-if="!data.fold">试卷管理</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/exam') }" @click="nav('/manager/exam')">
            <div class="sb-item-icon"><el-icon><EditPen /></el-icon></div>
            <span v-if="!data.fold">考试管理</span>
          </div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/score') }" @click="nav('/manager/score')">
            <div class="sb-item-icon"><el-icon><DataAnalysis /></el-icon></div>
            <span v-if="!data.fold">成绩管理</span>
          </div>
        </div>

        <div class="sb-group">
          <div v-if="!data.fold" class="sb-group-title">社区</div>
          <div class="sb-item" :class="{ active: isCurrent('/manager/forumManage') }" @click="nav('/manager/forumManage')">
            <div class="sb-item-icon"><el-icon><ChatLineSquare /></el-icon></div>
            <span v-if="!data.fold">帖子管理</span>
          </div>
        </div>
      </nav>

      <!-- 底部折叠 -->
      <div class="sb-footer">
        <div class="sb-fold" @click="data.fold = !data.fold">
          <el-icon><component :is="data.fold ? 'DArrowRight' : 'DArrowLeft'" /></el-icon>
          <span v-if="!data.fold">{{ data.fold ? '展开' : '收起' }}</span>
        </div>
      </div>
    </aside>

    <!-- 右侧 -->
    <div class="right">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="tb-left">
          <span class="tb-hi">{{ greeting }}，</span>
          <span class="tb-who">{{ data.user.name }}</span>
          <template v-if="router.currentRoute.value.meta.name">
            <span class="tb-sep">|</span>
            <span class="tb-page">{{ router.currentRoute.value.meta.name }}</span>
          </template>
        </div>
        <div class="tb-right">
          <ThemeSwitcher />
          <NotificationBell />
          <el-dropdown trigger="click">
            <div class="tb-setting"><el-icon><Setting /></el-icon></div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/front/home')"><el-icon><Monitor /></el-icon> 用户前台</el-dropdown-item>
                <el-dropdown-item @click="router.push('/manager/person')"><el-icon><User /></el-icon> 个人资料</el-dropdown-item>
                <el-dropdown-item @click="router.push('/manager/password')"><el-icon><Lock /></el-icon> 修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="logout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容 -->
      <main class="content">
        <router-view v-slot="{ Component, route: currentRoute }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="currentRoute.path" @updateUser="updateUser" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, watch } from "vue"
import router from "@/router/index.js"
import { ElMessage } from "element-plus"
import ThemeSwitcher from "@/components/ThemeSwitcher.vue"
import NotificationBell from "@/components/NotificationBell.vue"

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  fold: false,
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const transitionName = ref('page')
const routeOrder = [
  '/manager/home', '/manager/notice',
  '/manager/admin', '/manager/examiner', '/manager/student', '/manager/registrationApproval', '/manager/invitationCode',
  '/manager/questionCategory', '/manager/question', '/manager/tutorial',
  '/manager/examPaper', '/manager/exam', '/manager/score',
  '/manager/forumManage',
]
watch(() => router.currentRoute.value.path, (to, from) => {
  if (!from || from === to) { transitionName.value = 'page'; return }
  const toIdx = routeOrder.indexOf(to)
  const fromIdx = routeOrder.indexOf(from)
  transitionName.value = (toIdx >= 0 && fromIdx >= 0 && toIdx < fromIdx) ? 'page-back' : 'page'
})

const isCurrent = (p) => router.currentRoute.value.path === p
const nav = (p) => router.push(p)
const isAdmin = computed(() => data.user.role === 'ADMIN' || data.user.role === 'OWNER')
const roleColors = { OWNER: '#f87171', ADMIN: '#fbbf24', HELPER: '#60a5fa', USER: '#4ade80' }
const roleColor = computed(() => roleColors[data.user.role] || '#4ade80')
const getRoleLabel = (role) => ({ OWNER: '所有者', ADMIN: '管理员', HELPER: '阅卷人', USER: '用户' }[role] || '用户')

const goHome = () => router.push('/')
const logout = () => { localStorage.removeItem('xm-user'); router.push('/login') }
const updateUser = () => { data.user = JSON.parse(localStorage.getItem('xm-user') || '{}') }

if (!data.user.id) { logout(); ElMessage.error('请登录！') }
</script>

<style scoped>
@import "@/assets/css/transitions.css";

/* ===== 布局 ===== */
.layout { display: flex; min-height: 100vh; }

/* ===== 侧边栏 ===== */
.sidebar {
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
.sidebar.fold { width: 62px; }

/* Logo */
.sb-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 16px 14px;
  cursor: pointer;
}
.sb-logo:hover { opacity: 0.85; }
.sb-logo img { width: 34px; height: 34px; border-radius: 10px; flex-shrink: 0; }
.sb-logo-text { display: flex; flex-direction: column; line-height: 1.15; white-space: nowrap; }
.sb-logo-text b { font-size: 16px; color: #fff; letter-spacing: 1px; }
.sb-logo-text span { font-size: 10px; color: rgba(255,255,255,0.3); letter-spacing: 2px; }

/* 用户卡 */
.sb-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 14px 10px;
  padding: 10px 12px;
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.05);
}
.sb-avatar { width: 34px; height: 34px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.sb-profile-info { display: flex; flex-direction: column; min-width: 0; }
.sb-profile-info strong { font-size: 13px; color: rgba(255,255,255,0.9); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sb-profile-role {
  font-size: 10px; font-weight: 700; margin-top: 3px;
  border: 1px solid; border-radius: 4px; padding: 0 5px; width: fit-content;
}
.sb-profile-mini { padding: 6px 0; text-align: center; }
.sb-profile-mini img { width: 30px; height: 30px; border-radius: 50%; }

/* 阅卷按钮 */
.sb-exam-btn {
  position: relative;
  margin: 0 14px 14px;
  padding: 9px 14px;
  background: linear-gradient(135deg, rgba(0,180,42,0.25), rgba(0,180,42,0.1));
  border: 1px solid rgba(0,180,42,0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #4ade80;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s;
  white-space: nowrap;
}
.sb-exam-btn:hover { background: linear-gradient(135deg, rgba(0,180,42,0.35), rgba(0,180,42,0.15)); }
.sb-exam-shine {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, transparent, rgba(74,222,128,0.08), transparent);
  animation: shine 4s ease-in-out infinite;
}
@keyframes shine { 0%,100%{transform:translateX(-100%)} 50%{transform:translateX(100%)} }

/* 导航 */
.sb-nav { flex: 1; overflow-y: auto; padding: 0 10px; }
.sb-nav::-webkit-scrollbar { width: 3px; }
.sb-nav::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.sb-group { margin-bottom: 2px; }
.sb-group-title {
  font-size: 10px; font-weight: 700;
  color: rgba(255,255,255,0.2);
  text-transform: uppercase;
  letter-spacing: 2px;
  padding: 14px 10px 4px;
}

.sb-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
  height: 36px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
  position: relative;
}
.sb-item:hover { color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.05); }

.sb-item.active {
  color: #fff;
  background: rgba(0,180,42,0.15);
  font-weight: 600;
}
.sb-item.active::before {
  content: '';
  position: absolute;
  left: 0; top: 6px; bottom: 6px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--primary-color, #00b42a);
}

.sb-item-icon {
  width: 28px; height: 28px;
  border-radius: 7px;
  background: rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: rgba(255,255,255,0.5);
  flex-shrink: 0;
  transition: all 0.15s;
}
.sb-item:hover .sb-item-icon { color: rgba(255,255,255,0.8); background: rgba(255,255,255,0.1); }
.sb-item.active .sb-item-icon { background: rgba(0,180,42,0.25); color: #4ade80; }

.sb-item-icon.blue { background: rgba(64,158,255,0.12); color: #60a5fa; }
.sb-item-icon.cyan { background: rgba(34,211,238,0.12); color: #22d3ee; }
.sb-item-icon.green { background: rgba(74,222,128,0.12); color: #4ade80; }
.sb-item-icon.orange { background: rgba(251,146,60,0.12); color: #fb923c; }
.sb-item-icon.purple { background: rgba(167,139,250,0.12); color: #a78bfa; }
.sb-item-icon.teal { background: rgba(45,212,191,0.12); color: #2dd4bf; }
.sb-item-icon.gold { background: rgba(251,191,36,0.12); color: #fbbf24; }

/* 底部 */
.sb-footer {
  padding: 10px 14px;
  border-top: 1px solid rgba(255,255,255,0.04);
}
.sb-fold {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 10px; border-radius: 6px;
  font-size: 12px; color: rgba(255,255,255,0.25);
  cursor: pointer; transition: all 0.2s;
}
.sb-fold:hover { color: rgba(255,255,255,0.6); background: rgba(255,255,255,0.04); }

/* 折叠态 */
.sidebar.fold .sb-logo { justify-content: center; padding: 14px 0; }
.sidebar.fold .sb-exam-btn { justify-content: center; padding: 9px; margin: 0 8px 14px; }
.sidebar.fold .sb-item { justify-content: center; padding: 0; }
.sidebar.fold .sb-fold { justify-content: center; }
.sidebar.fold .sb-nav { padding: 0 6px; }

/* ===== 右侧 ===== */
.right { flex: 1; display: flex; flex-direction: column; min-width: 0; background: var(--bg-page); }

/* 顶栏 */
.topbar {
  height: 52px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky; top: 0; z-index: 50;
}
.tb-left { font-size: 13px; display: flex; align-items: center; gap: 6px; }
.tb-hi { color: var(--text-secondary); }
.tb-who { font-weight: 700; color: var(--text-primary); }
.tb-sep { color: var(--border-light); margin: 0 4px; }
.tb-page { color: var(--text-primary); font-weight: 600; }
.tb-right { display: flex; align-items: center; gap: 6px; }
.tb-setting {
  width: 30px; height: 30px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary); cursor: pointer; transition: all 0.15s;
}
.tb-setting:hover { background: var(--bg-page); color: var(--text-primary); }

/* 内容 */
.content { flex: 1; padding: 20px 24px; overflow-y: auto; }

@media (max-width: 768px) {
  .sidebar { width: 62px; }
  .sidebar .sb-logo-text,
  .sidebar .sb-profile,
  .sidebar .sb-group-title,
  .sidebar .sb-item span,
  .sidebar .sb-exam-btn span,
  .sidebar .sb-fold span { display: none; }
  .sidebar .sb-logo { justify-content: center; }
  .sidebar .sb-exam-btn { justify-content: center; }
  .sidebar .sb-item { justify-content: center; }
  .sidebar .sb-fold { justify-content: center; }
  .content { padding: 12px; }
}
</style>
