<template>
  <div class="portal">
    <header class="portal-nav" :class="{ scrolled: data.scrolled }">
      <button class="portal-brand" type="button" @click="scrollTo('hero')">
        <img src="@/assets/imgs/logo.png" alt="北冥审核系统" />
        <span>北冥审核系统</span>
      </button>

      <nav class="portal-links">
        <a href="#hero" @click.prevent="scrollTo('hero')">首页</a>
        <a href="#tracks" @click.prevent="scrollTo('tracks')">审核方向</a>
        <a href="#server" @click.prevent="scrollTo('server')">入服须知</a>
      </nav>

      <div class="portal-actions">
        <template v-if="data.user.id">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <button class="portal-user" type="button">
              <img :src="data.user.avatar || defaultAvatar" alt="" />
              <span>{{ data.user.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front">玩家中心</el-dropdown-item>
                <el-dropdown-item v-if="canManage" command="manager">管理后台</el-dropdown-item>
                <el-dropdown-item command="exam">进服审核</el-dropdown-item>
                <el-dropdown-item command="person">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>

    <main>
      <section id="hero" class="portal-hero">
        <div
          v-for="(bg, index) in backgrounds"
          :key="bg"
          class="portal-hero-bg"
          :class="{ active: index === data.currentBgIndex }"
          :style="{ backgroundImage: `url(${bg})` }"
        ></div>
        <div class="portal-hero-shade"></div>

        <div class="portal-hero-content">
          <p class="portal-eyebrow">Minecraft Java 1.21.1 生电群组服</p>
          <h1>北冥审核系统</h1>
          <p class="portal-lead">完成进服申请、方向审核、题库练习、结果查询和后台管理的一体化入口。</p>
          <div class="portal-hero-actions">
            <el-button type="primary" size="large" @click="enterSystem">
              <el-icon><Promotion /></el-icon>
              进入系统
            </el-button>
            <el-button size="large" class="portal-ghost" @click="router.push('/front/examList')">
              <el-icon><EditPen /></el-icon>
              开始审核
            </el-button>
            <el-button size="large" class="portal-ghost" @click="openLink(links.docs)">
              <el-icon><Link /></el-icon>
              玩家文档
            </el-button>
          </div>
        <div class="portal-hero-stats">
            <span><strong>9950X</strong>物理机</span>
            <span><strong>48G</strong>内存</span>
            <span><strong>三线</strong>直连</span>
          </div>
        </div>
      </section>

      <section id="tracks" class="portal-section">
        <div class="portal-section-head">
          <span>审核方向</span>
          <h2>按目标身份完成对应审核</h2>
        </div>
        <div class="track-grid">
          <article v-for="track in tracks" :key="track.title" class="track-card">
            <div class="track-icon">
              <el-icon><component :is="track.icon" /></el-icon>
            </div>
            <h3>{{ track.title }}</h3>
            <p>{{ track.desc }}</p>
          </article>
        </div>
      </section>

      <section id="server" class="portal-section server-section">
        <div class="portal-section-head">
          <span>入服须知</span>
          <h2>加入北冥前先确认这些事项</h2>
        </div>
        <div class="server-grid">
          <button v-for="item in serverInfo" :key="item.title" type="button" class="server-item" @click="item.action">
            <small>{{ item.tag }}</small>
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
          </button>
        </div>
      </section>
    </main>

    <footer class="portal-footer">
      <span>© {{ new Date().getFullYear() }} 北冥审核系统</span>
      <button type="button" @click="openLink(links.qqGroup)">加入北冥大群</button>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import { ArrowDown, Cpu, EditPen, Finished, Link, Promotion, Reading, Trophy } from "@element-plus/icons-vue"
import router from "@/router/index.js"
import bg1 from "@/assets/imgs/bj1.png"
import bg2 from "@/assets/imgs/bj2.png"
import bg3 from "@/assets/imgs/bj3.png"

const defaultAvatar = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
const backgrounds = [bg1, bg2, bg3]

const links = {
  docs: "https://docs.beiming.games",
  qqGroup: "https://qm.qq.com/q/QFk4wtZUoG",
}

const data = reactive({
  user: JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}"),
  scrolled: false,
  currentBgIndex: 0,
})

const canManage = computed(() => ["OWNER", "ADMIN", "HELPER"].includes(data.user.role))

const tracks = [
  { title: "建筑审核", desc: "面向建筑表达、比例、材质和整体完成度的方向审核。", icon: "Reading" },
  { title: "后期审核", desc: "面向项目整理、记录、维护意识和协作规范的方向审核。", icon: "Finished" },
  { title: "红石审核", desc: "面向机器理解、稳定性、安全停机和服务器影响评估。", icon: "Cpu" },
  { title: "见习审核", desc: "面向基础规则、沟通习惯和进服前准备的通用审核。", icon: "Trophy" },
]

const serverInfo = [
  {
    tag: "物理配置",
    title: "9950X / 48G 内存",
    desc: "北冥服务器使用 9950X 物理机与 48G 内存，当前配置不存在明显的日常运营压力。",
    action: () => copyText("9950X 物理机 / 48G 内存"),
  },
  {
    tag: "网络接入",
    title: "电信 / 移动 / 联通三线直连",
    desc: "当前三条线路均为直连。受运营商上传限制，单线全天候限速 1.5m/s，每天 20G 流量。",
    action: () => copyText("电信 / 移动 / 联通三线直连，单线 1.5m/s，每天 20G"),
  },
  {
    tag: "更多信息",
    title: "其余事项查看文档库",
    desc: "客户端、服务器地址、进服流程、群组服说明等内容统一放在玩家文档中维护。",
    action: () => openLink(links.docs),
  },
]

const enterSystem = () => {
  if (!data.user.id) {
    router.push("/login")
    return
  }
  router.push(canManage.value ? "/manager/home" : "/front/home")
}

const handleUserCommand = (cmd) => {
  if (cmd === "front") router.push("/front/home")
  if (cmd === "manager") router.push("/manager/home")
  if (cmd === "exam") router.push("/front/examList")
  if (cmd === "person") router.push(data.user.role === "USER" ? "/front/person" : "/manager/person")
  if (cmd === "logout") {
    localStorage.removeItem("beiming-onlineexam-user")
    data.user = {}
    ElMessage.success("已退出登录")
  }
}

const scrollTo = (id) => document.getElementById(id)?.scrollIntoView({ behavior: "smooth" })
const openLink = (url) => window.open(url, "_blank", "noopener,noreferrer")

const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success("已复制：" + text)
  } catch {
    ElMessage.info(text)
  }
}

const handleScroll = () => {
  data.scrolled = window.scrollY > 20
}

let bgTimer = null
onMounted(() => {
  window.addEventListener("scroll", handleScroll)
  bgTimer = setInterval(() => {
    data.currentBgIndex = (data.currentBgIndex + 1) % backgrounds.length
  }, 7000)
})

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
  if (bgTimer) clearInterval(bgTimer)
})
</script>

<style scoped>
.portal {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
}

.portal-nav {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 30;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 42px;
  color: #fff;
  transition: background 0.2s, border-color 0.2s;
}

.portal-nav.scrolled {
  background: rgba(15, 23, 42, 0.84);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
}

.portal-brand,
.portal-user,
.portal-footer button,
.server-item {
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.portal-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-weight: 900;
}

.portal-brand img {
  width: 38px;
  height: 38px;
  border-radius: 9px;
}

.portal-links {
  display: flex;
  gap: 28px;
}

.portal-links a {
  color: rgba(255, 255, 255, 0.78);
  font-size: 14px;
  font-weight: 700;
}

.portal-links a:hover {
  color: #fff;
}

.portal-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.portal-user {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
}

.portal-user img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

.portal-hero {
  position: relative;
  min-height: 92vh;
  display: flex;
  align-items: center;
  padding: 120px 8vw 96px;
  overflow: hidden;
}

.portal-hero-bg,
.portal-hero-shade {
  position: absolute;
  inset: 0;
}

.portal-hero-bg {
  background-position: center;
  background-size: cover;
  opacity: 0;
  transition: opacity 1.2s ease;
}

.portal-hero-bg.active {
  opacity: 1;
}

.portal-hero-shade {
  background:
    linear-gradient(90deg, rgba(6, 12, 20, 0.88), rgba(6, 12, 20, 0.54) 48%, rgba(6, 12, 20, 0.18)),
    linear-gradient(180deg, rgba(6, 12, 20, 0.18), rgba(6, 12, 20, 0.7));
}

.portal-hero-content {
  position: relative;
  z-index: 1;
  max-width: 720px;
  color: #fff;
}

.portal-eyebrow {
  margin: 0 0 14px;
  color: #8ee6b0;
  font-size: 14px;
  font-weight: 900;
}

.portal-hero h1 {
  margin: 0;
  color: #fff;
  font-size: clamp(44px, 7vw, 84px);
  line-height: 1;
  font-weight: 950;
  letter-spacing: 0;
}

.portal-lead {
  max-width: 620px;
  margin: 24px 0 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 18px;
  line-height: 1.8;
}

.portal-hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 34px;
}

.portal-ghost {
  border-color: rgba(255, 255, 255, 0.42) !important;
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

.portal-hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 34px;
  color: rgba(255, 255, 255, 0.72);
}

.portal-hero-stats span {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.portal-hero-stats strong {
  color: #fff;
  font-size: 24px;
}

.portal-section {
  max-width: 1180px;
  margin: 0 auto;
  padding: 76px 24px 24px;
}

.portal-section-head {
  margin-bottom: 24px;
}

.portal-section-head span {
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 900;
}

.portal-section-head h2 {
  margin: 8px 0 0;
  color: var(--text-primary);
  font-size: 30px;
}

.track-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.server-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.track-card,
.server-item {
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
}

.track-card {
  padding: 24px;
}

.track-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 8px;
  background: var(--primary-light);
  color: var(--primary-color);
  font-size: 22px;
}

.track-card h3 {
  margin: 0 0 10px;
  font-size: 17px;
}

.track-card p,
.server-item span {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.server-section {
  padding-bottom: 80px;
}

.server-item {
  min-height: 166px;
  padding: 20px;
  text-align: left;
  transition: transform 0.18s, border-color 0.18s;
}

.server-item:hover {
  transform: translateY(-3px);
  border-color: rgba(var(--primary-rgb), 0.35);
}

.server-item strong {
  display: block;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.server-item small {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 900;
}

.portal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
  border-top: 1px solid var(--border-lighter);
  color: var(--text-secondary);
  font-size: 13px;
}

.portal-footer button {
  color: var(--primary-color);
  font-weight: 800;
}

@media (max-width: 900px) {
  .portal-nav {
    padding: 0 18px;
  }

  .portal-links {
    display: none;
  }

  .track-grid,
  .server-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .portal-actions .el-button:first-child,
  .portal-user span {
    display: none;
  }

  .portal-hero {
    min-height: 88vh;
    padding: 110px 22px 72px;
  }

  .portal-lead {
    font-size: 15px;
  }

  .portal-hero-actions .el-button {
    width: 100%;
  }

  .track-grid,
  .server-grid {
    grid-template-columns: 1fr;
  }

  .portal-footer {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
