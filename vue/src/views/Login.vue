<template>
  <div class="login-overlay" @click.self="$emit('close')" @mousemove="handleMouseMove" @keydown="handleKeyDown" tabindex="0" ref="overlayRef">
    <canvas ref="trailCanvas" class="trail-canvas"></canvas>

    <div class="close-btn" @click="$emit('close')">
      <el-icon :size="22"><Close /></el-icon>
    </div>

    <div class="login-container" ref="loginRef">
      <!-- 左侧品牌区 -->
      <div class="login-left">
        <div class="left-inner">
          <div class="left-logo">
            <img src="@/assets/imgs/logo.png" alt="Logo" />
          </div>
          <h1 class="left-title">北冥审核系统</h1>
          <p class="left-desc">
            专业、高效的 Minecraft 进服审核系统。<br />
            支持四项审核、在线审核与结果统计。
          </p>
          <div class="left-divider"></div>
          <div class="left-features">
            <div class="feature-item">
              <span class="feature-icon">📝</span>
              <span>智能题库管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span>数据可视化分析</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🏆</span>
              <span>审核结果管理</span>
            </div>
          </div>
          <div class="left-decoration">
            <div class="deco-circle" :class="{ breathing: data.inputFocused }"></div>
            <div class="deco-circle small" :class="{ breathing: data.inputFocused }"></div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="right-inner">
          <div class="form-header">
            <h2 class="form-title">欢迎登录</h2>
            <p class="form-subtitle">请输入您的账号信息</p>
          </div>

          <el-form ref="formRef" :model="data.form" :rules="data.rules" class="login-form">
            <el-form-item prop="username">
              <el-autocomplete
                v-model="data.form.username"
                :fetch-suggestions="querySearch"
                placeholder="请输入账号"
                size="large"
                style="width: 100%"
                @focus="data.inputFocused = true"
                @blur="data.inputFocused = false"
                @select="handleHistorySelect"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
                <template #default="{ item }">
                  <div class="history-item">
                    <span class="history-name">{{ item.value }}</span>
                    <span class="history-role">{{ item.role }}</span>
                    <span class="history-del" @click.stop="deleteHistory(item.value)">×</span>
                  </div>
                </template>
              </el-autocomplete>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                show-password :prefix-icon="Lock" size="large"
                v-model="data.form.password"
                placeholder="请输入密码"
                @focus="data.inputFocused = true"
                @blur="data.inputFocused = false"
              />
            </el-form-item>

            <el-form-item prop="role">
              <el-select size="large" v-model="data.form.role" placeholder="请选择角色" style="width: 100%">
                <el-option value="OWNER" label="👑 所有者" />
                <el-option value="ADMIN" label="🔧 管理员" />
                <el-option value="HELPER" label="📋 阅卷人" />
                <el-option value="USER" label="👤 玩家" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button
                :loading="data.loading"
                size="large"
                type="primary"
                style="width: 100%; height: 44px; font-size: 15px;"
                @click="login"
                class="login-btn"
                :disabled="data.loginDisabled"
              >
                {{ data.loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            还没有账号？<a href="/register">立即注册</a>
            <span class="login-hint" v-if="data.form.username && data.form.role && data.form.password" @click="login">
              按 Enter 快速登录
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 郑重声明弹窗 -->
    <el-dialog
      title="审核规则声明"
      v-model="data.dialogVisible"
      :show-close="false"
      width="42%"
      :close-on-click-modal="false"
      destroy-on-close
      append-to-body
    >
      <div class="declare-content">
        <p>本平台仅用于 Minecraft Java 生电服务器入服审核，并要求参与者遵守相关审核规范与服务器规则。</p>
        <p><strong>所有参与审核的玩家需提供真实信息，审核过程中<span style="color: #f53f3f;">严禁作弊</span>。违反规定者将会被取消入服审核资格，并且记录在案。</strong></p>
        <p>请务必遵守审核纪律，确保入服审核的公正性和公平性。</p>
        <div class="declare-link">
          本服务指南：<a href="https://docs.beiming.games" target="_blank">docs.beiming.games</a>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="large" @click="confirmAndEnter" style="width: 100%;">我已阅读并同意审核规则</el-button>
      </template>
    </el-dialog>

    <canvas ref="fireworkCanvas" class="firework-canvas" v-if="data.showFirework"></canvas>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, onMounted, onUnmounted } from 'vue'
import { User, Lock, Close } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

defineEmits(['close'])

const fireworkCanvas = ref(null)
const trailCanvas = ref(null)
const overlayRef = ref(null)
const loginRef = ref(null)

const data = reactive({
  dialogVisible: false,
  form: { role: 'USER' },
  loading: false,
  showFirework: false,
  inputFocused: false,
  mouseX: 0,
  mouseY: 0,
  konamiSequence: [],
  loginDisabled: false,
  loginAttempts: 0,
  loginHistory: [],
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  },
})

const formRef = ref()

const loadHistory = () => {
  try {
    const raw = localStorage.getItem('xm-login-history') || '[]'
    data.loginHistory = JSON.parse(raw)
  } catch { data.loginHistory = [] }
}

const querySearch = (queryString, cb) => {
  const results = data.loginHistory.filter(h => h.value.includes(queryString)).slice(0, 5)
  cb(results)
}

const handleHistorySelect = (item) => {
  data.form.username = item.value
  data.form.role = item.role
}

const deleteHistory = (value) => {
  data.loginHistory = data.loginHistory.filter(h => h.value !== value)
  localStorage.setItem('xm-login-history', JSON.stringify(data.loginHistory))
}

const saveToHistory = (username, role) => {
  data.loginHistory = data.loginHistory.filter(h => h.value !== username)
  data.loginHistory.unshift({ value: username, role })
  data.loginHistory = data.loginHistory.slice(0, 5)
  localStorage.setItem('xm-login-history', JSON.stringify(data.loginHistory))
}

const handleKeyDown = (e) => {
  data.konamiSequence.push(e.key)
  if (data.konamiSequence.length > 10) data.konamiSequence.shift()
  if (data.konamiSequence.length === 10) {
    const match = data.konamiSequence.every((k, i) => k === ['ArrowUp','ArrowUp','ArrowDown','ArrowDown','ArrowLeft','ArrowRight','ArrowLeft','ArrowRight','b','a'][i])
    if (match) { triggerKonamiEgg(); data.konamiSequence = [] }
  }
  if (e.key === 'Enter' && data.form.username && data.form.password && data.form.role && !data.loading) {
    login()
  }
}

const trails = []
const MAX_TRAILS = 40
let animFrameId = null

const handleMouseMove = (e) => {
  trails.push({
    x: e.clientX,
    y: e.clientY,
    life: 1,
    vx: (Math.random() - 0.5) * 2,
    vy: (Math.random() - 0.5) * 2,
  })
  if (trails.length > MAX_TRAILS) trails.shift()
}

const initTrailCanvas = () => {
  const canvas = trailCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const colors = [
    [0, 180, 42],   // #00b42a
    [0, 168, 30],   // #00a81e
    [64, 158, 255], // #409eff
    [114, 46, 209], // #722ed1
  ]

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    for (let i = 1; i < trails.length; i++) {
      const prev = trails[i - 1]
      const curr = trails[i]
      const alpha = curr.life * 0.5
      if (alpha < 0.01) continue

      const colorIdx = i % colors.length
      const [r, g, b] = colors[colorIdx]

      ctx.beginPath()
      ctx.moveTo(prev.x, prev.y)
      ctx.lineTo(curr.x, curr.y)
      ctx.strokeStyle = `rgba(${r}, ${g}, ${b}, ${alpha})`
      ctx.lineWidth = 2 * curr.life
      ctx.stroke()
    }

    for (let i = trails.length - 1; i >= 0; i--) {
      const t = trails[i]
      t.x += t.vx
      t.y += t.vy
      t.life -= 0.025

      if (t.life <= 0) { trails.splice(i, 1); continue }

      const colorIdx = i % colors.length
      const [r, g, b] = colors[colorIdx]
      const size = 4 * t.life

      ctx.beginPath()
      ctx.arc(t.x, t.y, size * 2.5, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${t.life * 0.1})`
      ctx.fill()

      ctx.beginPath()
      ctx.arc(t.x, t.y, size, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${t.life * 0.7})`
      ctx.fill()
    }

    animFrameId = requestAnimationFrame(animate)
  }
  animate()
}

const triggerKonamiEgg = () => {
  if (loginRef.value) {
    loginRef.value.style.transition = 'transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
    loginRef.value.style.transform = 'rotateY(360deg) scale(1.05)'
    setTimeout(() => {
      loginRef.value.style.transform = 'rotateY(0deg) scale(1)'
    }, 800)
  }
  launchFireworks()

  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  if (user.id) {
    request.post('/easterEgg/discover', {
      userId: user.id, userName: user.name, userRole: user.role, eggName: 'login_konami'
    }).then(res => {
      if (res.code === '200' && !res.data?.alreadyDiscovered) {
        ElMessage.success({ message: '你发现了隐藏彩蛋！经典永不过时', duration: 3000 })
      }
    })
  } else {
    ElMessage.success({ message: 'Konami Code! 登录后可以计入彩蛋榜', duration: 3000 })
  }
}

const launchFireworks = () => {
  data.showFirework = true
  nextTick(() => {
    const canvas = fireworkCanvas.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const particles = []
    const colors = ['#00b42a', '#ffd93d', '#6bcb77', '#4d96ff', '#ff9ff3', '#54a0ff', '#722ed1']

    const createBurst = (x, y) => {
      const color = colors[Math.floor(Math.random() * colors.length)]
      for (let i = 0; i < 40; i++) {
        const angle = (Math.PI * 2 / 40) * i
        const speed = 2 + Math.random() * 4
        particles.push({
          x, y, vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed,
          life: 1, decay: 0.015 + Math.random() * 0.01, color,
          size: 2 + Math.random() * 2
        })
      }
    }

    const centers = [
      { x: canvas.width * 0.3, y: canvas.height * 0.3 },
      { x: canvas.width * 0.7, y: canvas.height * 0.35 },
      { x: canvas.width * 0.5, y: canvas.height * 0.25 },
    ]
    centers.forEach((c, i) => { setTimeout(() => createBurst(c.x, c.y), i * 400) })

    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      let alive = false
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i]
        p.x += p.vx
        p.y += p.vy
        p.vy += 0.06
        p.life -= p.decay
        if (p.life <= 0) { particles.splice(i, 1); continue }
        alive = true
        ctx.globalAlpha = p.life
        ctx.fillStyle = p.color
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2)
        ctx.fill()
      }
      ctx.globalAlpha = 1
      if (alive) { requestAnimationFrame(animate) }
      else { data.showFirework = false }
    }
    requestAnimationFrame(animate)
  })
}

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.loading = true
      request.post('/login', data.form)
        .then((res) => {
          data.loading = false
          if (res.code === '200') {
            saveToHistory(data.form.username, data.form.role)
            localStorage.setItem('xm-user', JSON.stringify(res.data))
            launchFireworks()
            if (res.data.role === 'USER') {
              ElMessage.success('登录成功')
              data.dialogVisible = true
              setTimeout(() => { location.href = '/front/home' }, 2000)
            } else {
              ElMessage.success('登录成功')
              setTimeout(() => { router.push('/manager/home') }, 1500)
            }
          } else {
            ElMessage.error(res.msg)
          }
        })
        .catch(() => {
          data.loading = false
          ElMessage.error('网络错误，请稍后再试')
        })
    }
  })
}

const confirmAndEnter = () => {
  data.dialogVisible = false
}

onMounted(() => {
  initTrailCanvas()
  loadHistory()
  if (overlayRef.value) overlayRef.value.focus()
})

onUnmounted(() => {
  if (animFrameId) cancelAnimationFrame(animFrameId)
})
</script>

<style scoped>
.login-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
  outline: none;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.trail-canvas {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.close-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;
}

.close-btn:hover { background: rgba(0, 0, 0, 0.3); }

.login-container {
  display: flex;
  perspective: 1500px;
  animation: loginOpen 0.5s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  z-index: 5;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

@keyframes loginOpen {
  from { opacity: 0; transform: scale(0.9) rotateX(5deg); }
  to { opacity: 1; transform: scale(1) rotateX(0); }
}

.login-left {
  width: 380px;
  background: linear-gradient(145deg, #1d2129 0%, #2b2f38 100%);
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}

.left-inner { position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; }

.left-logo { margin-bottom: 20px; }
.left-logo img { width: 56px; height: 56px; border-radius: 12px; border: 2px solid rgba(255,255,255,0.15); }

.left-title {
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: -0.5px;
}

.left-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 24px;
}

.left-divider {
  width: 40px;
  height: 2px;
  background: #00b42a;
  margin-bottom: 24px;
  border-radius: 1px;
}

.left-features { display: flex; flex-direction: column; gap: 12px; }

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.feature-icon { font-size: 16px; }

.left-decoration {
  position: absolute;
  bottom: 30px;
  right: 30px;
}

.deco-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid rgba(0, 180, 42, 0.15);
  position: absolute;
  bottom: 0;
  right: 0;
  transition: all 0.5s ease;
}

.deco-circle.small {
  width: 28px;
  height: 28px;
  bottom: 40px;
  right: -10px;
}

.deco-circle.breathing {
  animation: breathe 1s ease-in-out infinite alternate;
}

@keyframes breathe {
  from { transform: scale(1); border-color: rgba(0, 180, 42, 0.15); }
  to { transform: scale(1.15); border-color: rgba(0, 180, 42, 0.4); box-shadow: 0 0 20px rgba(0, 180, 42, 0.2); }
}

.login-right {
  width: 400px;
  background: #fff;
  padding: 48px 40px;
}

.right-inner {}

.form-header { margin-bottom: 28px; }

.form-title {
  font-size: 24px;
  font-weight: 800;
  color: #1d2129;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.login-form { margin-bottom: 16px; }

.login-btn {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 180, 42, 0.3);
}

.form-footer {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.form-footer a {
  color: #00b42a;
  font-weight: 600;
}

.form-footer a:hover { text-decoration: underline; }

.login-hint {
  font-size: 12px;
  color: #00b42a;
  cursor: pointer;
  font-weight: 500;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0;
}
.history-name { flex: 1; font-size: 14px; }
.history-role { font-size: 11px; color: #999; background: #f5f5f5; padding: 2px 6px; border-radius: 6px; }
.history-del { font-size: 15px; color: #ccc; cursor: pointer; transition: color 0.2s; }
.history-del:hover { color: #f53f3f; }

.declare-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
}

.declare-content p { margin: 0 0 12px; }

.declare-link {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-lighter);
}

.declare-link a { color: #00b42a; }

.firework-canvas {
  position: fixed !important;
  inset: 0 !important;
  z-index: 9999 !important;
  pointer-events: none !important;
}

@media (max-width: 800px) {
  .login-left { display: none; }
  .login-right { width: 100%; }
  .login-container { border-radius: 12px; margin: 0 12px; }
}
</style>
