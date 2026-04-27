<template>
  <div class="homepage">
    <nav class="nav-bar" :class="{ scrolled: data.scrolled }">
      <div class="nav-content">
        <div class="nav-left">
          <div class="nav-logo-wrap">
            <img src="@/assets/imgs/logo.png" alt="Logo" class="nav-logo" />
          </div>
          <span class="nav-title">北冥审核系统</span>
        </div>
        <div class="nav-center">
          <a href="#hero" class="nav-link" @click.prevent="scrollTo('hero')">首页</a>
          <a href="#about" class="nav-link" @click.prevent="scrollTo('about')">服务器介绍</a>
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
                  <el-dropdown-item v-if="data.user.role === 'USER'" command="front">进入玩家中心</el-dropdown-item>
                  <el-dropdown-item command="exam">进入审核中心</el-dropdown-item>
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
      <div class="hero-grid"></div>
      <div class="hero-orbit orbit-a"></div>
      <div class="hero-orbit orbit-b"></div>
      <div class="hero-terminal animate-on-scroll">
        <div class="terminal-line"><span>STATUS</span><b>ONLINE</b></div>
        <div class="terminal-line"><span>VERSION</span><b>Java 1.21.1</b></div>
        <div class="terminal-line"><span>CHECK</span><b>4 ROUTES</b></div>
      </div>
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
            <img src="@/assets/imgs/logo.png" alt="北冥审核系统" />
          </div>
          <div class="particles" ref="particlesRef"></div>
          <div v-if="data.clickCount > 0" class="click-hint">
            {{ data.clickCount >= 10 ? '🎉 解锁隐藏彩蛋！' : '' }}
          </div>
        </div>
        <h1 class="hero-title">北冥审核系统</h1>
        <p class="hero-subtitle">{{ data.typedText }}<span class="typing-cursor" v-if="data.isTyping">|</span></p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="hero-btn hero-btn-primary" @click="scrollTo('about')">
            <el-icon style="margin-right: 6px;"><ArrowDown /></el-icon>服务器介绍
          </el-button>
          <el-button size="large" class="hero-btn ghost" @click="data.user.id ? enterSystem() : showLogin()">进入系统</el-button>
        </div>

      </div>
    </section>

    <section id="about" class="about-section">
      <div class="section-inner">
        <div class="about-content">
          <h2 class="section-title animate-on-scroll"><span class="title-accent-light"></span>服务器介绍</h2>
          <p class="about-text animate-on-scroll">
            北冥审核系统用于北冥群组服进服审核。玩家可在这里提交申请、参加建筑审核、后期审核、红石审核或普通(见习)审核，并查看审核结果。
          </p>
          <div class="server-info animate-on-scroll">
            <button class="info-card tech-card" type="button" @click="copyText('9950X / 48G / 三线直连')">
              <div class="info-label">服务器配置</div>
              <div class="info-text">9950X 物理机，48G 内存；电信、移动、联通三线直连。当前单线路全天候限速 1.5m/s，每天 20G 流量。</div>
              <span class="card-action">点击复制配置</span>
            </button>
            <button class="info-card tech-card" type="button" @click="openLink(links.docs)">
              <div class="info-label">开放内容</div>
              <div class="info-text">原版 1.21.1 生电群组服已开放，包含镜像、虚空创造和小游戏服；整合包服按轮换机制开放。</div>
              <span class="card-action">查看玩家指南</span>
            </button>
            <button class="info-card tech-card" type="button" @click="openLink(links.joinGuide)">
              <div class="info-label">如何加入生电服</div>
              <div class="info-text">跳转到生电群组服文档库第一篇文章，查看客户端、服务器地址和进服流程。</div>
              <span class="card-action">打开加入教程</span>
            </button>
            <button class="info-card tech-card" type="button" @click="copyText('274350103')">
              <div class="info-label">Oopz 语音频道</div>
              <div class="info-text">北冥服在 Oopz 语音软件有频道，域 ID：274350103。</div>
              <span class="card-action">点击复制域 ID</span>
            </button>
            <button class="info-card tech-card" type="button" @click="openLink(links.qqGroup)">
              <div class="info-label">北冥大群</div>
              <div class="info-text">北冥-群组服大群：1041762935。点击后跳转 QQ 入群页面。</div>
              <span class="card-action">加入群聊</span>
            </button>
            <button class="info-card tech-card" type="button" @click="copyText('pd96073114')">
              <div class="info-label">腾讯频道</div>
              <div class="info-text">腾讯频道：pd96073114。复制频道号后在腾讯频道内搜索加入。</div>
              <span class="card-action">点击复制频道号</span>
            </button>
            <div class="info-card wide rule-card">
              <div class="info-label">游玩规则</div>
              <div class="info-text">群内禁止发布不良内容。请友好交流，禁止破坏公平性的行为；发现问题请联系群主、管理员或对应服务器负责人。</div>
            </div>
          </div>
          <div class="about-links animate-on-scroll">
            <button class="about-link" type="button" @click="openLink(links.joinGuide)">
              <el-icon><Link /></el-icon><span>如何加入生电服</span>
            </button>
            <button class="about-link primary-link" type="button" @click="openLink(links.qqGroup)">
              <span>加入北冥大群</span>
            </button>
            <button class="about-link" type="button" @click="copyText('274350103')">
              <span>复制 Oopz 域 ID</span>
            </button>
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
              <div class="footer-brand">北冥审核系统</div>
              <div class="footer-slogan">服务器进服审核管理系统</div>
            </div>
          </div>
          <div class="footer-links">
            <a href="#hero" @click.prevent="scrollTo('hero')">首页</a>
            <a href="#about" @click.prevent="scrollTo('about')">服务器介绍</a>
          </div>
        </div>
        <div class="footer-divider"></div>
        <div class="footer-bottom">
          <p class="footer-copy">&copy; {{ new Date().getFullYear() }} 北冥审核系统 All Rights Reserved</p>
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
import { Link, ArrowDown } from '@element-plus/icons-vue'
import LoginDialog from './Login.vue'
import router from '@/router/index.js'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const logoRef = ref(null)
const heroParticlesRef = ref(null)
let spinAngle = 0
let spinVelocity = 0
let spinRaf = null

const links = {
  docs: 'https://docs.beiming.games',
  joinGuide: 'https://docs.beiming.games/2-%E7%94%9F%E7%94%B5%E7%BE%A4%E7%BB%84%E6%9C%8D/2.1-%E5%A6%82%E4%BD%95%E5%8A%A0%E5%85%A5%E7%94%9F%E7%94%B5%E6%9C%8D.html',
  qqGroup: 'https://qm.qq.com/q/QFk4wtZUoG',
}

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
    '服务器进服审核管理系统',
    '提交申请，参加审核，查看结果',
  ],
  currentTextIdx: 0,
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

const openLink = (url) => window.open(url, '_blank', 'noopener,noreferrer')

const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制：' + text)
  } catch {
    ElMessage.info(text)
  }
}

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
.hero-overlay { position: absolute; inset: 0; z-index: 1; background: linear-gradient(135deg, rgba(29, 33, 41, 0.82) 0%, rgba(10, 20, 18, 0.72) 50%, rgba(0, 180, 42, 0.18) 100%); }
.hero-grid { position: absolute; inset: 0; z-index: 2; pointer-events: none; background-image: linear-gradient(rgba(74,222,128,0.12) 1px, transparent 1px), linear-gradient(90deg, rgba(74,222,128,0.12) 1px, transparent 1px); background-size: 64px 64px; mask-image: linear-gradient(to bottom, rgba(0,0,0,0.75), transparent 80%); animation: gridDrift 18s linear infinite; }
.hero-grid::after { content: ''; position: absolute; inset: 0; background: linear-gradient(180deg, transparent 0%, rgba(74,222,128,0.16) 50%, transparent 100%); transform: translateY(-100%); animation: scanLine 5s ease-in-out infinite; }
.hero-orbit { position: absolute; z-index: 2; border: 1px solid rgba(74,222,128,0.28); border-radius: 50%; pointer-events: none; filter: drop-shadow(0 0 14px rgba(74,222,128,0.18)); }
.orbit-a { width: 440px; height: 440px; left: 8%; top: 20%; animation: orbitPulse 8s ease-in-out infinite; }
.orbit-b { width: 260px; height: 260px; right: 12%; bottom: 14%; animation: orbitPulse 7s ease-in-out infinite reverse; }
.hero-terminal { position: absolute; left: 7%; bottom: 12%; z-index: 3; width: 230px; padding: 14px 16px; border-radius: 16px; background: rgba(8, 18, 16, 0.58); border: 1px solid rgba(74,222,128,0.28); box-shadow: 0 18px 50px rgba(0,0,0,0.25), inset 0 0 30px rgba(74,222,128,0.05); backdrop-filter: blur(14px); }
.terminal-line { display: flex; justify-content: space-between; gap: 16px; color: rgba(255,255,255,0.55); font-size: 12px; letter-spacing: 1px; line-height: 1.9; }
.terminal-line b { color: #4ade80; font-weight: 700; }
@keyframes gridDrift { from { background-position: 0 0, 0 0; } to { background-position: 64px 64px, 64px 64px; } }
@keyframes scanLine { 0%, 25% { transform: translateY(-100%); opacity: 0; } 45%, 55% { opacity: 1; } 80%, 100% { transform: translateY(100%); opacity: 0; } }
@keyframes orbitPulse { 0%, 100% { transform: scale(1); opacity: 0.35; } 50% { transform: scale(1.08); opacity: 0.65; } }

.hero-particles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.hero-particle { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.25); animation: heroFloat linear infinite; }
@keyframes heroFloat { 0% { transform: translateY(0) scale(1); opacity: 0; } 10% { opacity: 0.5; } 90% { opacity: 0.5; } 100% { transform: translateY(-100vh) scale(0.4); opacity: 0; } }

.hero-content { position: relative; z-index: 4; text-align: center; color: #fff; display: flex; flex-direction: column; align-items: center; padding: 42px 54px; border-radius: 32px; background: linear-gradient(135deg, rgba(255,255,255,0.08), rgba(255,255,255,0.025)); border: 1px solid rgba(255,255,255,0.12); box-shadow: 0 24px 80px rgba(0,0,0,0.28); backdrop-filter: blur(8px); }
.hero-content::before { content: ''; position: absolute; inset: 0; border-radius: inherit; padding: 1px; background: linear-gradient(135deg, rgba(74,222,128,0.6), rgba(96,165,250,0.2), transparent); mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0); mask-composite: exclude; pointer-events: none; }
.hero-logo { width: 120px; height: 120px; border-radius: 50%; overflow: hidden; margin-bottom: 30px; box-shadow: 0 0 40px rgba(0, 180, 42, 0.4), 0 0 80px rgba(0, 180, 42, 0.15); border: 3px solid rgba(255, 255, 255, 0.3); cursor: pointer; will-change: transform; transition: box-shadow 0.3s ease, filter 0.3s ease; }
.hero-logo:hover { box-shadow: 0 0 60px rgba(0, 180, 42, 0.5), 0 0 100px rgba(0, 180, 42, 0.2); filter: saturate(1.15); }
.hero-logo img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; pointer-events: none; }

.hero-title { font-size: 60px; font-weight: 700; letter-spacing: 6px; margin-bottom: 22px; color: #fff; text-shadow: 0 8px 28px rgba(0, 0, 0, 0.35); }

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

.section-inner { max-width: 1100px; margin: 0 auto; }
.section-title { text-align: center; font-size: 36px; font-weight: 800; color: #1a1a2e; margin-bottom: 12px; display: flex; align-items: center; justify-content: center; gap: 12px; }
.title-accent-light { width: 40px; height: 4px; border-radius: 2px; background: rgba(255,255,255,0.5); }

.about-section { position: relative; padding: 120px 30px; overflow: hidden; background: radial-gradient(circle at 15% 20%, rgba(74,222,128,0.16), transparent 28%), radial-gradient(circle at 85% 12%, rgba(96,165,250,0.14), transparent 26%), linear-gradient(135deg, #10151f 0%, #1d2129 48%, #101820 100%); }
.about-section::before { content: ''; position: absolute; inset: 0; background: linear-gradient(120deg, transparent 0%, rgba(255,255,255,0.06) 45%, transparent 60%); transform: translateX(-100%); animation: sectionSweep 10s ease-in-out infinite; pointer-events: none; }
.about-section .section-title { color: #fff; }
@keyframes sectionSweep { 0%, 35% { transform: translateX(-100%); } 65%, 100% { transform: translateX(100%); } }
.about-content { max-width: 860px; margin: 0 auto; text-align: center; }
.about-text { color: rgba(255, 255, 255, 0.82); font-size: 16px; line-height: 2; margin-bottom: 34px; }
.server-info { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 34px; }
.info-card { position: relative; overflow: hidden; text-align: left; padding: 20px 22px; border-radius: 18px; background: rgba(255,255,255,0.065); border: 1px solid rgba(255,255,255,0.1); color: inherit; font: inherit; }
.tech-card { cursor: pointer; transition: transform 0.28s ease, border-color 0.28s ease, box-shadow 0.28s ease, background 0.28s ease; }
.tech-card::before { content: ''; position: absolute; inset: -1px; background: linear-gradient(120deg, transparent, rgba(74,222,128,0.28), transparent); transform: translateX(-120%); transition: transform 0.55s ease; }
.tech-card:hover { transform: translateY(-6px) scale(1.01); border-color: rgba(74,222,128,0.38); background: rgba(255,255,255,0.09); box-shadow: 0 18px 46px rgba(0,0,0,0.25), 0 0 26px rgba(74,222,128,0.08); }
.tech-card:hover::before { transform: translateX(120%); }
.info-card.wide { grid-column: 1 / -1; }
.info-label { position: relative; color: #fff; font-size: 15px; font-weight: 700; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.info-label::before { content: ''; width: 8px; height: 8px; border-radius: 50%; background: #4ade80; box-shadow: 0 0 12px rgba(74,222,128,0.8); }
.info-text { position: relative; color: rgba(255,255,255,0.68); font-size: 13px; line-height: 1.8; }
.card-action { position: relative; display: inline-flex; margin-top: 14px; color: #86efac; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; }
.rule-card { background: linear-gradient(135deg, rgba(255,255,255,0.07), rgba(74,222,128,0.05)); }
.about-links { display: flex; justify-content: center; gap: 14px; flex-wrap: wrap; }
.about-link { display: flex; align-items: center; gap: 8px; color: #fff; padding: 12px 24px; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 999px; transition: all 0.3s; font-weight: 500; background: rgba(255,255,255,0.04); cursor: pointer; }
.about-link:hover { background: rgba(255, 255, 255, 0.1); border-color: rgba(255,255,255,0.5); transform: translateY(-3px); box-shadow: 0 12px 34px rgba(0,0,0,0.22); }
.primary-link { border-color: rgba(74,222,128,0.48); background: linear-gradient(135deg, rgba(0,180,42,0.35), rgba(74,222,128,0.16)); }

.footer { background: #0a0a1a; padding: 48px 30px 24px; }
.footer-content { max-width: 1100px; margin: 0 auto; }
.footer-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.footer-brand-section { display: flex; align-items: center; gap: 12px; }
.footer-logo { width: 36px; height: 36px; border-radius: 50%; }
.footer-brand { color: #fff; font-weight: bold; font-size: 18px; }
.footer-slogan { color: #555; font-size: 13px; margin-top: 2px; }
.footer-links { display: flex; gap: 18px; align-items: center; flex-wrap: wrap; }
.footer-links a, .footer-links button { color: #777; font-size: 14px; transition: color 0.3s; background: none; border: 0; padding: 0; cursor: pointer; font: inherit; }
.footer-links a:hover, .footer-links button:hover { color: #fff; }
.footer-divider { height: 1px; background: #1a1a2e; margin-bottom: 20px; }
.footer-bottom { text-align: center; }
.footer-copy { color: #444; font-size: 13px; }

.escaped-logo { position: fixed; width: 40px; height: 40px; border-radius: 50%; z-index: 9999; pointer-events: auto; cursor: pointer; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25); }

.animate-on-scroll { opacity: 0; transform: translateY(30px); transition: all 0.7s cubic-bezier(0.22, 1, 0.36, 1); }
.animate-on-scroll.visible { opacity: 1; transform: translateY(0); }

@media (max-width: 900px) {
  .server-info { grid-template-columns: 1fr; }
  .hero-title { font-size: 48px; letter-spacing: 4px; }
  .hero-terminal, .hero-orbit { display: none; }
  .hero-content { padding: 34px 28px; }
}
@media (max-width: 600px) {
  .nav-center { display: none; }
  .hero-title { font-size: 38px; letter-spacing: 2px; }
  .hero-actions { flex-direction: column; width: 100%; }
  .hero-btn { width: 100%; }
  .footer-top { flex-direction: column; gap: 18px; align-items: flex-start; }
}
</style>
