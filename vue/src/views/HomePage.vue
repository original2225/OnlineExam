<template>
  <div class="homepage">
    <nav class="nav-bar" :class="{ scrolled: data.scrolled }">
      <div class="nav-content">
        <div class="nav-left">
          <div class="nav-logo-wrap">
            <img src="@/assets/imgs/logo.png" alt="Logo" class="nav-logo" />
          </div>
          <span class="nav-title">北冥</span>
        </div>
        <div class="nav-center">
          <a href="#hero" class="nav-link" @click.prevent="scrollTo('hero')">首页</a>
          <a href="#features" class="nav-link" @click.prevent="scrollTo('features')">平台特色</a>
          <a href="#stats" class="nav-link" @click.prevent="scrollTo('stats')">数据概览</a>
          <a href="#roles" class="nav-link" @click.prevent="scrollTo('roles')">角色权限</a>
          <a href="#about" class="nav-link" @click.prevent="scrollTo('about')">关于平台</a>
          <a href="https://docs.beiming.games" target="_blank" class="nav-link">文档库</a>
        </div>
        <div class="nav-right">
          <template v-if="data.user.id">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div style="display: flex; align-items: center; cursor: pointer; gap: 8px;">
                <img :src="data.user.avatar || defaultAvatar" style="width: 36px; height: 36px; border-radius: 50%; border: 2px solid rgba(255,255,255,0.3);" />
                <span style="color: white; font-weight: 500;">{{ data.user.name }}</span>
                <el-icon color="#fff"><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="data.user.role === 'OWNER' || data.user.role === 'ADMIN' || data.user.role === 'HELPER'" command="manager">进入管理后台</el-dropdown-item>
                  <el-dropdown-item v-if="data.user.role === 'USER'" command="front">进入用户中心</el-dropdown-item>
                  <el-dropdown-item command="exam">进入考试中心</el-dropdown-item>
                  <el-dropdown-item command="person">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <div class="login-hint-icon" @click="showLogin">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="4"/>
                <path d="M5 20c0-4 3.5-7 7-7s7 3 7 7"/>
              </svg>
              <span class="login-hint-text">点击登录 / 注册</span>
            </div>
          </template>
        </div>
      </div>
    </nav>

    <section id="hero" class="hero-section">
      <div class="hero-bg">
        <div
          v-for="(bg, index) in data.backgrounds"
          :key="index"
          class="hero-bg-item"
          :class="{ active: index === data.currentBgIndex }"
          :style="{ backgroundImage: `url(${bg})` }"
        ></div>
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-particles" ref="heroParticlesRef"></div>
      <div class="hero-content">
        <div class="hero-logo-wrapper">
          <div
            class="hero-logo"
            :class="{ 'spinning': data.isSpinning, 'bounce': data.isBouncing }"
            @mouseenter="startSpin"
            @mouseleave="stopSpin"
            @click="logoClick"
            @dblclick="logoDoubleClick"
            ref="logoRef"
          >
            <img src="@/assets/imgs/logo.png" alt="北冥" />
          </div>
          <div class="particles" ref="particlesRef"></div>
          <div v-if="data.clickCount > 0" class="click-hint">
            {{ data.clickCount >= 10 ? '🎉 解锁隐藏彩蛋！' : '' }}
          </div>
        </div>
        <h1 class="hero-title">
          <span class="title-char" v-for="(char, i) in '北冥'" :key="i" :style="{ animationDelay: (i * 0.15) + 's' }">{{ char }}</span>
        </h1>
        <p class="hero-subtitle">{{ data.typedText }}<span class="typing-cursor" v-if="data.isTyping">|</span></p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="hero-btn hero-btn-primary" @click="scrollTo('features')">
            <el-icon style="margin-right: 6px;"><ArrowDown /></el-icon>了解更多
          </el-button>
          <el-button v-if="!data.user.id" size="large" class="hero-btn ghost" @click="showLogin">立即登录</el-button>
          <el-button v-else size="large" class="hero-btn ghost" @click="enterSystem">进入系统</el-button>
        </div>

      </div>
    </section>

    <section id="features" class="features-section">
      <div class="section-inner">
        <h2 class="section-title animate-on-scroll"><span class="title-accent"></span>平台特色</h2>
        <p class="section-desc animate-on-scroll">为不同角色提供量身定制的功能体验</p>
        <div class="features-grid">
          <div class="feature-card animate-on-scroll" v-for="(feature, idx) in features" :key="idx" :style="{ animationDelay: (idx * 0.1) + 's' }">
            <div class="feature-icon" :style="{ background: feature.gradient }">
              <el-icon :size="36"><component :is="feature.icon" /></el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
            <div class="feature-tags">
              <span class="feature-tag" v-for="tag in feature.tags" :key="tag">{{ tag }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="stats" class="stats-section">
      <div class="section-inner">
        <h2 class="section-title animate-on-scroll"><span class="title-accent"></span>数据概览</h2>
        <p class="section-desc animate-on-scroll">平台运营核心数据</p>
        <div class="stats-showcase">
          <div class="stat-item animate-on-scroll" v-for="(stat, idx) in platformStats" :key="idx" :style="{ animationDelay: (idx * 0.1) + 's' }">
            <div class="stat-number">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-bar"><div class="stat-bar-fill" :style="{ background: stat.color }" :class="{ fill: data.statVisible }"></div></div>
          </div>
        </div>
      </div>
    </section>

    <section id="roles" class="roles-section">
      <div class="section-inner">
        <h2 class="section-title animate-on-scroll"><span class="title-accent"></span>多角色协作</h2>
        <p class="section-desc animate-on-scroll">清晰的权限分级，各司其职</p>
        <div class="roles-grid">
          <div class="role-card animate-on-scroll" v-for="(role, idx) in roles" :key="idx" :style="{ animationDelay: (idx * 0.08) + 's' }">
            <div class="role-icon-wrap" :style="{ background: role.gradient }"><span class="role-emoji">{{ role.emoji }}</span></div>
            <div class="role-badge-text" :style="{ color: role.textColor }">{{ role.tag }}</div>
            <h3>{{ role.title }}</h3>
            <p>{{ role.desc }}</p>
            <ul class="role-permissions">
              <li v-for="perm in role.permissions" :key="perm">
                <el-icon size="14" color="#67c23a"><CircleCheck /></el-icon><span>{{ perm }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <section id="about" class="about-section">
      <div class="section-inner">
        <div class="about-content">
          <h2 class="section-title animate-on-scroll"><span class="title-accent-light"></span>关于北冥平台</h2>
          <p class="about-text animate-on-scroll">
            北冥是一个专业的在线考试与成绩管理系统。平台致力于为组织提供公平、公正、高效的考核解决方案，
            支持多种题型的灵活组卷、智能防作弊机制、多角色协同工作，以及全面的成绩统计分析。
          </p>
          <div class="about-highlights animate-on-scroll">
            <div class="highlight-item">
              <el-icon :size="28" color="#667eea"><Lock /></el-icon>
              <div>
                <div class="highlight-title">安全可靠</div>
                <div class="highlight-desc">严格的权限控制与数据加密</div>
              </div>
            </div>
            <div class="highlight-item">
              <el-icon :size="28" color="#43e97b"><Timer /></el-icon>
              <div>
                <div class="highlight-title">高效便捷</div>
                <div class="highlight-desc">智能组卷与自动批阅系统</div>
              </div>
            </div>
            <div class="highlight-item">
              <el-icon :size="28" color="#fa709a"><DataAnalysis /></el-icon>
              <div>
                <div class="highlight-title">数据洞察</div>
                <div class="highlight-desc">多维度成绩统计与可视化分析</div>
              </div>
            </div>
          </div>
          <div class="about-links animate-on-scroll">
            <a href="https://docs.beiming.games" target="_blank" class="about-link">
              <el-icon><Link /></el-icon><span>访问文档库</span>
            </a>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-top">
          <div class="footer-brand-section">
            <img src="@/assets/imgs/logo.png" alt="Logo" class="footer-logo" />
            <div>
              <div class="footer-brand">北冥</div>
              <div class="footer-slogan">专业、公正、高效的在线考试平台</div>
            </div>
          </div>
          <div class="footer-links">
            <a href="#hero" @click.prevent="scrollTo('hero')">首页</a>
            <a href="#features" @click.prevent="scrollTo('features')">平台特色</a>
            <a href="https://docs.beiming.games" target="_blank">文档库</a>
          </div>
        </div>
        <div class="footer-divider"></div>
        <div class="footer-bottom">
          <p class="footer-copy">&copy; {{ new Date().getFullYear() }} 北冥 All Rights Reserved</p>
        </div>
      </div>
    </footer>

    <LoginDialog v-if="data.loginVisible" @close="data.loginVisible = false" />

    <img v-if="data.logoEscaped" ref="escapedLogoRef" src="@/assets/imgs/logo.png" class="escaped-logo"
      :style="{ left: data.escapedLogo.x + 'px', top: data.escapedLogo.y + 'px', transform: `rotate(${data.escapedLogo.rotation}deg) scale(${data.escapedLogo.scale})` }"
      @mouseenter="catchEscapedLogo"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { EditPen, Document, DataAnalysis, Link, ArrowDown, CircleCheck, Lock, Timer } from '@element-plus/icons-vue'
import LoginDialog from './Login.vue'
import router from '@/router/index.js'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const logoRef = ref(null)
const heroParticlesRef = ref(null)
let spinAngle = 0
let spinVelocity = 0
let spinRaf = null

const features = [
  { title: '在线考试', desc: '支持单选、多选、判断、简答多种题型，灵活组卷，智能防作弊机制，为用户提供便捷的在线答题体验。', icon: EditPen, gradient: 'linear-gradient(135deg, #00b42a, #00d034)', tags: ['多题型支持', '自动保存', '实时计时'] },
  { title: '智能阅卷', desc: '阅卷人可高效批阅试卷，系统自动统计分数，客观题自动评分，减少人工操作，提升阅卷效率。', icon: Document, gradient: 'linear-gradient(135deg, #409eff, #53a8ff)', tags: ['自动评分', '批量批阅', '成绩统计'] },
  { title: '成绩管理', desc: '多维度成绩统计分析，直观的数据图表展示，通过率、平均分等核心指标一目了然，帮助管理者全面掌握考核情况。', icon: DataAnalysis, gradient: 'linear-gradient(135deg, #fa709a, #fee140)', tags: ['可视化分析', '通过率追踪', '成绩导出'] },
]

const platformStats = [
  { value: '4', label: '角色权限', percent: 80, color: 'linear-gradient(90deg, #667eea, #764ba2)' },
  { value: '4+', label: '题型支持', percent: 70, color: 'linear-gradient(90deg, #43e97b, #38f9d7)' },
  { value: '6', label: '主题皮肤', percent: 90, color: 'linear-gradient(90deg, #fa709a, #fee140)' },
  { value: '24h', label: '全天候服务', percent: 100, color: 'linear-gradient(90deg, #a18cd1, #fbc2eb)' },
]

const roles = [
  { tag: 'Owner', title: '最高管理者', desc: '拥有平台所有权限，系统全局配置与管理', emoji: '👑', gradient: 'linear-gradient(135deg, #f5576c, #ff6b6b)', textColor: '#f5576c', permissions: ['全局系统配置', '用户角色管理', '数据备份恢复'] },
  { tag: 'Helper', title: '管理员', desc: '管理用户、题目、考试等核心数据', emoji: '🛡️', gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)', textColor: '#4facfe', permissions: ['题库管理维护', '考试安排发布', '成绩统计分析'] },
  { tag: '阅卷人', title: '阅卷批改', desc: '专注于试卷批阅与评分工作', emoji: '✏️', gradient: 'linear-gradient(135deg, #43e97b, #38f9d7)', textColor: '#059669', permissions: ['试卷批阅评分', '主观题评阅', '分数审核确认'] },
  { tag: '用户', title: '参加考试', desc: '在线答题、查看成绩与考试结果', emoji: '📚', gradient: 'linear-gradient(135deg, #a18cd1, #fbc2eb)', textColor: '#9b59b6', permissions: ['在线答题考试', '刷题练习', '成绩查询'] },
]

const data = reactive({
  scrolled: false,
  loginVisible: false,
  backgrounds: [],
  currentBgIndex: 0,
  isSpinning: false,
  isBouncing: false,
  clickCount: 0,
  hue: 0,
  logoEscaped: false,
  escapedLogo: { x: 0, y: 0, vx: 0, vy: 0, rotation: 0, rotSpeed: 0, scale: 1 },
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  typedText: '',
  isTyping: true,
  typeIndex: 0,
  typeTexts: [
    '专业、公正、高效的在线考试与成绩管理系统',
    '多角色协作，智能阅卷，全面数据统计',
    '灵活组卷，多种题型，智能防作弊',
  ],
  currentTextIdx: 0,
  statVisible: false,
})

const startTypeEffect = () => {
  const text = data.typeTexts[data.currentTextIdx]
  if (data.typeIndex < text.length) {
    data.typedText += text[data.typeIndex]
    data.typeIndex++
    setTimeout(startTypeEffect, 50 + Math.random() * 40)
  } else {
    data.isTyping = false
    setTimeout(() => {
      data.typedText = ''
      data.typeIndex = 0
      data.isTyping = true
      data.currentTextIdx = (data.currentTextIdx + 1) % data.typeTexts.length
      startTypeEffect()
    }, 3000)
  }
}

const initHeroParticles = () => {
  const container = heroParticlesRef.value
  if (!container) return
  for (let i = 0; i < 30; i++) {
    const dot = document.createElement('div')
    dot.className = 'hero-particle'
    dot.style.left = Math.random() * 100 + '%'
    dot.style.top = Math.random() * 100 + '%'
    dot.style.animationDelay = Math.random() * 6 + 's'
    dot.style.animationDuration = (4 + Math.random() * 4) + 's'
    dot.style.width = dot.style.height = (2 + Math.random() * 4) + 'px'
    container.appendChild(dot)
  }
}

const startSpin = () => { data.isSpinning = true; spinVelocity = 8; if (spinRaf) cancelAnimationFrame(spinRaf); animateSpin() }
const stopSpin = () => { data.isSpinning = false }

const animateSpin = () => {
  if (data.isSpinning) { spinVelocity = 8 } else { spinVelocity *= 0.97 }
  spinAngle += spinVelocity
  if (logoRef.value) logoRef.value.style.transform = `rotate(${spinAngle}deg)`
  if (Math.abs(spinVelocity) > 0.1) { spinRaf = requestAnimationFrame(animateSpin) }
  else { spinVelocity = 0; snapBack() }
}

const snapBack = () => {
  const normalizedAngle = spinAngle % 360
  const target = Math.round(normalizedAngle / 360) * 360
  const diff = target - spinAngle
  const snapAnimate = () => {
    spinAngle += diff * 0.1
    if (logoRef.value) logoRef.value.style.transform = `rotate(${spinAngle}deg)`
    if (Math.abs(target - spinAngle) > 0.5) requestAnimationFrame(snapAnimate)
    else { spinAngle = target; if (logoRef.value) logoRef.value.style.transform = `rotate(${spinAngle}deg)` }
  }
  requestAnimationFrame(snapAnimate)
}

const logoClick = () => {
  data.clickCount++
  data.isBouncing = true
  setTimeout(() => { data.isBouncing = false }, 600)
  createParticles()
  if (data.clickCount === 10) {
    data.hue = (data.hue + 60) % 360
    if (logoRef.value) logoRef.value.style.filter = `hue-rotate(${data.hue}deg)`
    if (data.user.id) {
      request.post('/easterEgg/discover', { userId: data.user.id, userName: data.user.name, userRole: data.user.role, eggName: 'logo_click_10' }).then(res => {
        if (res.code === '200' && res.data) {
          if (res.data.alreadyDiscovered) ElMessage.success(`🎉 你之前已发现彩蛋！你是第 ${res.data.rank} 位探索者（共 ${res.data.total} 人）`)
          else ElMessage.success(`🎉 恭喜发现彩蛋！你是第 ${res.data.rank} 位探索者！`)
        }
      })
    } else { ElMessage.success('🎉 恭喜发现彩蛋！登录后可记录你的名次') }
  }
}

const logoDoubleClick = () => {
  data.hue = (data.hue + 72) % 360
  if (logoRef.value) logoRef.value.style.filter = `hue-rotate(${data.hue}deg) saturate(1.5)`
  ElMessage.info('🎨 双击变色！再试试看')
}

const particlesRef = ref(null)
const createParticles = () => {
  if (!particlesRef.value) return
  const container = particlesRef.value
  const colors = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff6bcb', '#c56bff']
  for (let i = 0; i < 12; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.background = colors[Math.floor(Math.random() * colors.length)]
    const angle = (Math.PI * 2 * i) / 12
    const distance = 60 + Math.random() * 40
    particle.style.setProperty('--tx', `${Math.cos(angle) * distance}px`)
    particle.style.setProperty('--ty', `${Math.sin(angle) * distance}px`)
    particle.style.left = '50%'; particle.style.top = '50%'
    container.appendChild(particle)
    setTimeout(() => particle.remove(), 800)
  }
}

const showLogin = () => { data.loginVisible = true }

const enterSystem = () => {
  if (data.user.role === 'OWNER' || data.user.role === 'ADMIN' || data.user.role === 'HELPER') router.push('/manager/home')
  else router.push('/front/home')
}

const scrollTo = (id) => { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }) }

const handleUserCommand = (cmd) => {
  if (cmd === 'manager') router.push('/manager/home')
  else if (cmd === 'front') router.push('/front/home')
  else if (cmd === 'exam') router.push('/front/examList')
  else if (cmd === 'person') { router.push(data.user.role === 'USER' ? '/front/person' : '/manager/person') }
  else if (cmd === 'logout') { localStorage.removeItem('xm-user'); data.user = {}; ElMessage.success('已退出登录') }
}

const catchEscapedLogo = () => { data.logoEscaped = false; data.escapedLogo = { x: 0, y: 0, vx: 0, vy: 0, rotation: 0, rotSpeed: 0, scale: 1 } }

const handleScroll = () => {
  data.scrolled = window.scrollY > 50
  document.querySelectorAll('.animate-on-scroll').forEach(el => {
    if (el.getBoundingClientRect().top < window.innerHeight * 0.85) el.classList.add('visible')
  })
  const statsEl = document.getElementById('stats')
  if (statsEl && statsEl.getBoundingClientRect().top < window.innerHeight * 0.8) data.statVisible = true
}

const loadBackgrounds = () => {
  const defaultBg = new URL('../assets/imgs/bj1.png', import.meta.url).href
  data.backgrounds = [defaultBg]
  for (let i = 2; i <= 20; i++) {
    const imgUrl = new URL(`../assets/imgs/bj${i}.png`, import.meta.url).href
    const img = new Image()
    img.onload = () => { if (!data.backgrounds.includes(imgUrl)) data.backgrounds.push(imgUrl) }
    img.src = imgUrl
  }
  setInterval(() => { if (data.backgrounds.length > 1) data.currentBgIndex = (data.currentBgIndex + 1) % data.backgrounds.length }, 8000)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  loadBackgrounds()
  startTypeEffect()
  initHeroParticles()
  setTimeout(handleScroll, 100)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (spinRaf) cancelAnimationFrame(spinRaf)
})
</script>

<style scoped>
.nav-bar { position: fixed; top: 0; left: 0; right: 0; z-index: 100; height: 64px; background: transparent; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.nav-bar.scrolled { background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); box-shadow: 0 1px 20px rgba(0, 0, 0, 0.08); }
.nav-bar.scrolled .nav-title { color: #1a1a2e; }
.nav-bar.scrolled .nav-link { color: #555; }
.nav-bar.scrolled .nav-link:hover { color: #00b42a; }
.nav-content { max-width: 1200px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; }
.nav-left { display: flex; align-items: center; gap: 10px; }
.nav-logo-wrap { display: flex; align-items: center; cursor: pointer; }
.nav-logo { width: 36px; height: 36px; border-radius: 50%; transition: transform 0.3s ease; }
.nav-title { font-size: 20px; font-weight: bold; color: #fff; transition: color 0.3s; }
.nav-center { display: flex; gap: 28px; }
.nav-link { color: rgba(255, 255, 255, 0.85); font-size: 15px; font-weight: 500; transition: color 0.3s; cursor: pointer; position: relative; }
.nav-link::after { content: ''; position: absolute; bottom: -4px; left: 0; width: 0; height: 2px; background: #fff; border-radius: 1px; transition: width 0.3s ease; }
.nav-link:hover { color: #fff; }
.nav-link:hover::after { width: 100%; }
.nav-bar.scrolled .nav-link::after { background: #00b42a; }
.nav-right { display: flex; align-items: center; }

.login-hint-icon { position: relative; display: flex; align-items: center; justify-content: center; width: 42px; height: 42px; border-radius: 50%; border: 2px solid rgba(255,255,255,0.5); color: rgba(255,255,255,0.8); cursor: pointer; transition: all 0.3s; }
.login-hint-icon svg { width: 22px; height: 22px; }
.login-hint-icon:hover { border-color: #fff; color: #fff; background: rgba(255,255,255,0.12); transform: scale(1.08); }
.login-hint-text { position: absolute; top: calc(100% + 10px); left: 50%; transform: translateX(-50%); white-space: nowrap; padding: 6px 14px; border-radius: 8px; background: rgba(0,0,0,0.75); color: #fff; font-size: 13px; pointer-events: none; opacity: 0; transition: opacity 0.25s, top 0.25s; }
.login-hint-icon:hover .login-hint-text { opacity: 1; top: calc(100% + 8px); }

.hero-section { height: 100vh; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.hero-bg { position: absolute; inset: 0; z-index: 0; }
.hero-bg-item { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0; transition: opacity 2s ease-in-out; }
.hero-bg-item.active { opacity: 1; }
.hero-overlay { position: absolute; inset: 0; z-index: 1; background: linear-gradient(135deg, rgba(29, 33, 41, 0.8) 0%, rgba(43, 47, 56, 0.7) 50%, rgba(0, 180, 42, 0.15) 100%); }

.hero-particles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.hero-particle { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.25); animation: heroFloat linear infinite; }
@keyframes heroFloat { 0% { transform: translateY(0) scale(1); opacity: 0; } 10% { opacity: 0.5; } 90% { opacity: 0.5; } 100% { transform: translateY(-100vh) scale(0.4); opacity: 0; } }

.hero-content { position: relative; z-index: 3; text-align: center; color: #fff; display: flex; flex-direction: column; align-items: center; }
.hero-logo { width: 120px; height: 120px; border-radius: 50%; overflow: hidden; margin-bottom: 30px; box-shadow: 0 0 40px rgba(0, 180, 42, 0.4), 0 0 80px rgba(0, 180, 42, 0.15); border: 3px solid rgba(255, 255, 255, 0.3); cursor: pointer; will-change: transform; transition: box-shadow 0.3s ease; }
.hero-logo:hover { box-shadow: 0 0 60px rgba(0, 180, 42, 0.5), 0 0 100px rgba(0, 180, 42, 0.2); }
.hero-logo img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; pointer-events: none; }

.hero-title { font-size: 72px; font-weight: 800; letter-spacing: 12px; margin-bottom: 24px; display: flex; gap: 4px; }
.title-char { display: inline-block; background: linear-gradient(135deg, #ffecd2, #fcb69f, #ff9a9e, #fecfef, #a18cd1, #fbc2eb, #ffecd2); background-size: 200% 200%; -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; animation: gradientMove 6s ease infinite, charBounce 0.6s cubic-bezier(0.22, 1, 0.36, 1) both; }
@keyframes charBounce { from { opacity: 0; transform: translateY(40px) scale(0.8); } to { opacity: 1; transform: translateY(0) scale(1); } }

.hero-subtitle { font-size: 18px; color: rgba(255, 255, 255, 0.85); margin-bottom: 40px; letter-spacing: 2px; min-height: 30px; }
.typing-cursor { animation: blink 0.8s step-end infinite; color: rgba(255,255,255,0.7); }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.hero-actions { display: flex; gap: 16px; justify-content: center; }
.hero-btn { min-width: 140px; border-radius: 25px; font-size: 16px; height: 46px; font-weight: 600; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.hero-btn-primary { box-shadow: 0 4px 20px rgba(0, 180, 42, 0.4); }
.hero-btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 30px rgba(0, 180, 42, 0.5); }
.hero-btn.ghost { background: transparent; border: 2px solid rgba(255, 255, 255, 0.6); color: #fff; }
.hero-btn.ghost:hover { background: rgba(255, 255, 255, 0.15); border-color: #fff; transform: translateY(-2px); }

.hero-scroll-hint { position: absolute; bottom: 40px; display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; opacity: 0.6; transition: opacity 0.3s; animation: scrollHintBounce 2s ease-in-out infinite; }
.hero-scroll-hint:hover { opacity: 1; }
.hero-scroll-hint span { font-size: 12px; color: rgba(255,255,255,0.7); }
.scroll-mouse { width: 24px; height: 38px; border-radius: 12px; border: 2px solid rgba(255,255,255,0.5); display: flex; justify-content: center; padding-top: 8px; }
.scroll-dot { width: 4px; height: 8px; border-radius: 2px; background: rgba(255,255,255,0.8); animation: scrollDot 2s ease-in-out infinite; }
@keyframes scrollDot { 0%, 100% { transform: translateY(0); opacity: 1; } 50% { transform: translateY(10px); opacity: 0.3; } }
@keyframes scrollHintBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(6px); } }
@keyframes gradientMove { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }

.features-section { padding: 120px 30px; background: #f7f8fa; }
.section-inner { max-width: 1100px; margin: 0 auto; }
.section-title { text-align: center; font-size: 36px; font-weight: 800; color: #1a1a2e; margin-bottom: 12px; display: flex; align-items: center; justify-content: center; gap: 12px; }
.title-accent { width: 40px; height: 4px; border-radius: 2px; background: linear-gradient(90deg, #00b42a, #00d034); }
.title-accent-light { width: 40px; height: 4px; border-radius: 2px; background: rgba(255,255,255,0.5); }
.section-desc { text-align: center; color: #888; font-size: 16px; margin-bottom: 60px; }

.features-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 30px; }
.feature-card { background: #fff; border-radius: 16px; padding: 40px 30px; text-align: center; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); border: 1px solid rgba(0,0,0,0.04); transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.feature-card:hover { transform: translateY(-6px); box-shadow: 0 16px 48px rgba(0, 0, 0, 0.1); }
.feature-icon { width: 72px; height: 72px; margin: 0 auto 20px; border-radius: 20px; display: flex; align-items: center; justify-content: center; color: #fff; transition: transform 0.3s ease; }
.feature-card:hover .feature-icon { transform: scale(1.08) rotate(3deg); }
.feature-card h3 { font-size: 20px; color: #1a1a2e; margin-bottom: 12px; font-weight: 700; }
.feature-card p { color: #777; line-height: 1.8; font-size: 14px; margin-bottom: 16px; }
.feature-tags { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.feature-tag { padding: 4px 12px; border-radius: 20px; font-size: 12px; background: #e6f7eb; color: #00b42a; font-weight: 500; }

.stats-section { padding: 100px 30px; background: #fff; }
.stats-showcase { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.stat-item { text-align: center; padding: 32px 20px; border-radius: 16px; background: var(--bg-page); transition: all 0.3s ease; }
.stat-item:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.stat-number { font-size: 42px; font-weight: 800; background: linear-gradient(135deg, #00b42a, #00d034); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; margin-bottom: 8px; }
.stat-label { font-size: 15px; color: #666; margin-bottom: 16px; font-weight: 500; }
.stat-bar { height: 4px; background: #e8e8e8; border-radius: 2px; overflow: hidden; margin: 0 auto; max-width: 80px; }
.stat-bar-fill { height: 100%; border-radius: 2px; transition: width 1.2s cubic-bezier(0.4, 0, 0.2, 1); width: 0; }
.stat-bar-fill.fill { width: 80%; }

.roles-section { padding: 120px 30px; background: #f7f8fa; }
.roles-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.role-card { background: #fff; border-radius: 16px; padding: 32px 24px; text-align: center; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); border: 1px solid rgba(0,0,0,0.04); position: relative; overflow: hidden; }
.role-card:hover { transform: translateY(-6px); box-shadow: 0 16px 48px rgba(0, 0, 0, 0.08); }
.role-icon-wrap { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; transition: transform 0.3s; }
.role-card:hover .role-icon-wrap { transform: scale(1.1); }
.role-emoji { font-size: 28px; }
.role-badge-text { font-size: 12px; font-weight: 700; margin-bottom: 16px; letter-spacing: 1px; text-transform: uppercase; }
.role-card h3 { font-size: 17px; color: #1a1a2e; margin-bottom: 8px; font-weight: 700; }
.role-card p { font-size: 13px; color: #999; line-height: 1.6; margin-bottom: 16px; }
.role-permissions { list-style: none; padding: 0; margin: 0; text-align: left; display: flex; flex-direction: column; gap: 6px; }
.role-permissions li { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #666; }

.about-section { padding: 120px 30px; background: linear-gradient(135deg, #1d2129 0%, #2b2f38 50%, #1d2129 100%); }
.about-section .section-title { color: #fff; }
.about-content { max-width: 700px; margin: 0 auto; text-align: center; }
.about-text { color: rgba(255, 255, 255, 0.8); font-size: 16px; line-height: 2; margin-bottom: 40px; }
.about-highlights { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-bottom: 40px; }
.highlight-item { display: flex; align-items: flex-start; gap: 12px; text-align: left; padding: 20px; border-radius: 12px; background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1); transition: all 0.3s; }
.highlight-item:hover { background: rgba(255,255,255,0.1); transform: translateY(-2px); }
.highlight-title { font-size: 15px; font-weight: 600; color: #fff; margin-bottom: 4px; }
.highlight-desc { font-size: 12px; color: rgba(255,255,255,0.6); }
.about-links { display: flex; justify-content: center; gap: 20px; }
.about-link { display: flex; align-items: center; gap: 8px; color: #fff; padding: 12px 28px; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 25px; transition: all 0.3s; font-weight: 500; }
.about-link:hover { background: rgba(255, 255, 255, 0.1); border-color: rgba(255,255,255,0.5); transform: translateY(-2px); }

.footer { background: #0a0a1a; padding: 48px 30px 24px; }
.footer-content { max-width: 1100px; margin: 0 auto; }
.footer-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.footer-brand-section { display: flex; align-items: center; gap: 12px; }
.footer-logo { width: 36px; height: 36px; border-radius: 50%; }
.footer-brand { color: #fff; font-weight: bold; font-size: 18px; }
.footer-slogan { color: #555; font-size: 13px; margin-top: 2px; }
.footer-links { display: flex; gap: 24px; }
.footer-links a { color: #777; font-size: 14px; transition: color 0.3s; }
.footer-links a:hover { color: #fff; }
.footer-divider { height: 1px; background: #1a1a2e; margin-bottom: 20px; }
.footer-bottom { text-align: center; }
.footer-copy { color: #444; font-size: 13px; }

.escaped-logo { position: fixed; width: 40px; height: 40px; border-radius: 50%; z-index: 9999; pointer-events: auto; cursor: pointer; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25); }

.animate-on-scroll { opacity: 0; transform: translateY(30px); transition: all 0.7s cubic-bezier(0.22, 1, 0.36, 1); }
.animate-on-scroll.visible { opacity: 1; transform: translateY(0); }

@media (max-width: 900px) {
  .features-grid { grid-template-columns: 1fr; max-width: 500px; margin: 0 auto; }
  .roles-grid { grid-template-columns: repeat(2, 1fr); }
  .stats-showcase { grid-template-columns: repeat(2, 1fr); }
  .about-highlights { grid-template-columns: 1fr; }
  .hero-title { font-size: 48px; letter-spacing: 6px; }
}
@media (max-width: 600px) {
  .roles-grid { grid-template-columns: 1fr; }
  .stats-showcase { grid-template-columns: 1fr; }
  .nav-center { display: none; }
  .hero-title { font-size: 40px; }
}
</style>
