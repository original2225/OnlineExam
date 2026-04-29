<template>
  <div class="front-shell">
    <header class="front-topbar">
      <button class="front-brand" type="button" @click="handleBrandClick">
        <img src="@/assets/imgs/logo.png" alt="北冥审核系统" />
        <span>
          <strong>北冥审核系统</strong>
          <small>玩家审核中心</small>
        </span>
      </button>

      <nav class="front-nav" aria-label="学生前台导航">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="front-nav-link"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="front-actions">
        <ThemeSwitcher />
        <ExamRecordChatLauncher v-if="data.user.id" />
        <NotificationBell />

        <template v-if="!data.user.id">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>

        <el-dropdown v-else trigger="click" @command="handleCommand">
          <button class="front-user" type="button">
            <img :src="data.user.avatar || defaultAvatar" alt="" />
            <span>
              <strong>{{ data.user.name }}</strong>
              <small>{{ roleLabel }}</small>
            </span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="person">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="scores">
                <el-icon><DataAnalysis /></el-icon>审核结果
              </el-dropdown-item>
              <el-dropdown-item v-if="canManage" command="manager" divided>
                <el-icon><Setting /></el-icon>管理后台
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="front-main">
      <router-view v-slot="{ Component, route: currentRoute }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="currentRoute.path" @updateUser="updateUser" />
        </transition>
      </router-view>
    </main>

    <nav class="front-mobile-nav" aria-label="移动端学生前台导航">
      <router-link
        v-for="item in mobileNavItems"
        :key="item.path"
        :to="item.path"
        class="front-mobile-link"
        :class="{ active: isActive(item.path) }"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import router from "@/router/index.js"
import { computed, onUnmounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import request from "@/utils/request.js"
import ThemeSwitcher from "@/components/ThemeSwitcher.vue"
import NotificationBell from "@/components/NotificationBell.vue"
import ExamRecordChatLauncher from "@/components/ExamRecordChatLauncher.vue"
import {
  ArrowDown,
  Collection,
  DataAnalysis,
  Edit,
  EditPen,
  HomeFilled,
  Setting,
  SwitchButton,
  Trophy,
  User,
  WarnTriangleFilled,
} from "@element-plus/icons-vue"

const defaultAvatar = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"

const data = reactive({
  user: JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}"),
  brandClickCount: 0,
  brandClickTimer: null,
})

const canManage = computed(() => ["OWNER", "ADMIN", "HELPER"].includes(data.user.role))
const roleLabel = computed(() => (data.user.role === "USER" ? "玩家" : data.user.id ? "管理员" : "访客"))

const navItems = computed(() => [
  { path: "/front/home", label: "首页", icon: "HomeFilled" },
  { path: "/front/subjects", label: "题库", icon: "Collection" },
  { path: "/front/examList", label: "审核", icon: "EditPen" },
  { path: "/front/practiceMode", label: "模拟", icon: "Edit" },
  { path: "/front/wrongQuestions", label: "错题", icon: "WarnTriangleFilled" },
  ...(canManage.value ? [{ path: "/front/contributeQuestion", label: "供题", icon: "EditPen" }] : []),
  { path: "/front/myScores", label: "结果", icon: "DataAnalysis" },
  { path: "/front/leaderboard", label: "榜单", icon: "Trophy" },
])

const mobileNavItems = computed(() => navItems.value.filter(item => ["/front/home", "/front/examList", "/front/practiceMode", "/front/myScores"].includes(item.path)))

const isActive = (path) => {
  const current = router.currentRoute.value.path
  return current === path || current.startsWith(path + "/")
}

const discoverEgg = (eggName) => {
  if (!data.user.id) return
  request.post("/easterEgg/discover", {
    userId: data.user.id,
    userName: data.user.name,
    userRole: data.user.role,
    eggName,
  }).then(res => {
    if (res.code === "200") {
      ElMessage.success(res.data?.alreadyDiscovered ? "你已经发现过北冥入口彩蛋" : "发现彩蛋：北冥入口守门人")
    }
  }).catch(() => {})
}

const handleBrandClick = () => {
  data.brandClickCount++
  clearTimeout(data.brandClickTimer)
  data.brandClickTimer = setTimeout(() => { data.brandClickCount = 0 }, 1800)
  if (data.brandClickCount >= 7) {
    data.brandClickCount = 0
    discoverEgg("beiming_entry_gate")
    return
  }
  router.push("/")
}

const handleCommand = (cmd) => {
  const map = { person: "/front/person", scores: "/front/myScores", manager: "/manager/home" }
  if (cmd === "logout") {
    localStorage.removeItem("beiming-onlineexam-user")
    router.push("/login")
    return
  }
  if (map[cmd]) router.push(map[cmd])
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}")
}

onUnmounted(() => {
  if (data.brandClickTimer) clearTimeout(data.brandClickTimer)
})
</script>

<style scoped>
.front-shell {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(18, 24, 32, 0.04), transparent 260px),
    var(--bg-page);
}

.front-topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: 68px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.88);
  border-bottom: 1px solid var(--border-lighter);
  backdrop-filter: blur(18px);
}

[data-theme="dark"] .front-topbar {
  background: rgba(17, 24, 39, 0.88);
}

.front-brand,
.front-user {
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.front-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-radius: 8px;
  color: var(--text-primary);
  flex-shrink: 0;
}

.front-brand:hover {
  background: var(--bg-page);
}

.front-brand img {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  object-fit: cover;
}

.front-brand span,
.front-user span {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.front-brand strong {
  font-size: 15px;
  font-weight: 800;
}

.front-brand small,
.front-user small {
  margin-top: 2px;
  color: var(--text-secondary);
  font-size: 11px;
}

.front-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 4px;
  min-width: 0;
}

.front-nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 38px;
  padding: 0 12px;
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.front-nav-link:hover,
.front-nav-link.active {
  color: var(--primary-color);
  background: var(--primary-light);
}

.front-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.front-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 8px 5px 5px;
  border: 1px solid var(--border-lighter);
  border-radius: 999px;
  color: var(--text-primary);
}

.front-user:hover {
  border-color: rgba(var(--primary-rgb), 0.35);
  background: var(--primary-light);
}

.front-user img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.front-user strong {
  max-width: 84px;
  overflow: hidden;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.front-main {
  min-height: calc(100vh - 68px);
}

.front-mobile-nav {
  display: none;
}

@media (max-width: 1080px) {
  .front-nav {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .front-nav::-webkit-scrollbar {
    display: none;
  }
}

@media (max-width: 760px) {
  .front-topbar {
    height: 60px;
    padding: 0 12px;
  }

  .front-brand small,
  .front-nav,
  .front-user span,
  .front-actions .el-button {
    display: none;
  }

  .front-actions {
    margin-left: auto;
  }

  .front-mobile-nav {
    position: fixed;
    right: 12px;
    bottom: 12px;
    left: 12px;
    z-index: 60;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 4px;
    padding: 6px;
    border: 1px solid var(--border-lighter);
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.92);
    box-shadow: var(--shadow-lg);
    backdrop-filter: blur(16px);
  }

  [data-theme="dark"] .front-mobile-nav {
    background: rgba(17, 24, 39, 0.92);
  }

  .front-mobile-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 3px;
    padding: 8px 4px;
    border-radius: 8px;
    color: var(--text-secondary);
    font-size: 11px;
    font-weight: 700;
  }

  .front-mobile-link.active {
    color: var(--primary-color);
    background: var(--primary-light);
  }
}
</style>
