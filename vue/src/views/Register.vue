<template>
  <div class="register-container" @mousemove="handleMouseMove">
    <!-- 动态粒子 Canvas -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <!-- 注册成功大动画 -->
    <div v-if="data.showSuccess" class="success-overlay">
      <canvas ref="successCanvas" class="success-canvas"></canvas>
      <div class="success-content">
        <div class="success-icon">✓</div>
        <div class="success-title">{{ data.successMsg }}</div>
        <div class="success-sub">{{ data.successSub }}</div>
      </div>
    </div>

    <!-- 等待审批互动页面 -->
    <div v-if="data.showPending" class="pending-overlay" @mousemove="handlePendingMouseMove">
      <div class="pending-content" :style="{ transform: `translate(${data.pendingOffX}px, ${data.pendingOffY}px)` }">
        <div class="pending-icon-wrap">
          <div class="pending-ring"></div>
          <div class="pending-ring delay"></div>
          <el-icon :size="48" class="pending-icon"><Clock /></el-icon>
        </div>
        <h2 class="pending-title">注册申请已提交</h2>
        <p class="pending-desc">管理员正在审批中，请耐心等待...</p>
        <div class="pending-hint">你可以移动鼠标与背景互动</div>
        <el-button type="primary" round @click="goLogin" style="margin-top: 24px;">
          前往登录页
        </el-button>
      </div>
      <!-- 互动粒子 -->
      <div
        v-for="dot in data.pendingDots"
        :key="dot.id"
        class="pending-dot"
        :style="{
          left: dot.x + 'px',
          top: dot.y + 'px',
          width: dot.size + 'px',
          height: dot.size + 'px',
          background: dot.color,
          opacity: dot.opacity,
          transform: `scale(${1 + Math.sin(Date.now() / 1000 + dot.phase) * 0.2})`
        }"
      ></div>
    </div>

    <!-- 注册表单 -->
    <div v-if="!data.showPending && !data.showSuccess" class="register-box">
      <div class="register-title-text" @click="handleTitleClick"
           :class="{ 'title-glitch': data.titleGlitch }"
           style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 15px; color: #1d2129; cursor: default; user-select: none;">
        {{ data.titleDisplay }}
      </div>
      <div style="text-align: center; margin-bottom: 20px; color: #8a9199; font-size: 13px; line-height: 1.6;">
        使用邀请码可直接通过审核（阅卷人邀请码将注册为阅卷人角色），无邀请码需等待管理员审批
      </div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input :prefix-icon="User" v-model="data.form.username" placeholder="请输入用户名（登录账号）"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input show-password :prefix-icon="Lock" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input show-password :prefix-icon="Lock" v-model="data.form.confirmPassword" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="data.form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="ID" prop="studentNo">
          <el-input v-model="data.form.studentNo" placeholder="请输入人口普查表中的序号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input :prefix-icon="Message" v-model="data.form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="邀请码" prop="invitationCode">
          <el-input :prefix-icon="Key" v-model="data.form.invitationCode" placeholder="如果有的话"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="data.form.phone" placeholder="请输入手机号（选填）"></el-input>
        </el-form-item>
        <el-form-item label="报考方向" prop="branch">
          <el-select v-model="data.form.branch" placeholder="请选择你想报考的方向" style="width: 100%">
            <el-option label="生电" value="生电" />
            <el-option label="建筑" value="建筑" />
            <el-option label="交流" value="交流" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="submitRequest" :loading="data.loading">
            {{ data.form.invitationCode ? '立即注册（有邀请码）' : '提交申请' }}
          </el-button>
        </el-form-item>
        <div style="text-align: right; margin-top: 10px;">
          已有账号？请 <a href="/login">登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from "vue";
import { User, Lock, Message, Key, Clock } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";

const successCanvas = ref(null)
const particleCanvas = ref(null)
const formRef = ref()

const TITLE_ORIGINAL = '北冥审核系统 注册'

const data = reactive({
  loading: false,
  form: {},
  showSuccess: false,
  showPending: false,
  successMsg: '',
  successSub: '',
  mouseX: window.innerWidth / 2,
  mouseY: window.innerHeight / 2,
  pendingOffX: 0,
  pendingOffY: 0,
  pendingDots: [],
  titleClickCount: 0,
  titleClickTimer: null,
  titleGlitch: false,
  titleDisplay: TITLE_ORIGINAL,
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      {
        validator: (rule, value, callback) => {
          if (!value) {
            callback(new Error('请确认密码'))
          } else if (value !== data.form.password) {
            callback(new Error("两次密码不一致!"))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ],
    studentNo: [
      { required: true, message: '请输入编号', trigger: 'blur' }
    ],
    email: [
      {
        validator: (rule, value, callback) => {
          const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
          if (!value) {
            callback(new Error('请输入邮箱'))
          } else if (!emailRegex.test(value)) {
            callback(new Error('请输入有效的邮箱地址'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
})

// ===== Canvas 粒子系统（连线 + 鼠标互动） =====
let pAnimFrameId = null
const particles = []
const PARTICLE_COUNT = 60
const CONNECT_DISTANCE = 150
const MOUSE_RADIUS = 120

const initCanvasParticles = () => {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  // 初始化粒子
  particles.length = 0
  for (let i = 0; i < PARTICLE_COUNT; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.8,
      vy: (Math.random() - 0.5) * 0.8,
      size: 2 + Math.random() * 2,
      color: Math.random() > 0.5 ? 'rgba(0, 180, 42, 0.8)' : 'rgba(64, 158, 255, 0.8)',
    })
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 更新粒子位置
    for (const p of particles) {
      // 鼠标排斥效果
      const dx = p.x - data.mouseX
      const dy = p.y - data.mouseY
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < MOUSE_RADIUS && dist > 0) {
        const force = (MOUSE_RADIUS - dist) / MOUSE_RADIUS * 0.5
        p.vx += (dx / dist) * force
        p.vy += (dy / dist) * force
      }

      // 阻尼
      p.vx *= 0.98
      p.vy *= 0.98

      p.x += p.vx
      p.y += p.vy

      // 边界反弹
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
      p.x = Math.max(0, Math.min(canvas.width, p.x))
      p.y = Math.max(0, Math.min(canvas.height, p.y))

      // 画粒子
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = p.color
      ctx.fill()
    }

    // 画连线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < CONNECT_DISTANCE) {
          const alpha = (1 - dist / CONNECT_DISTANCE) * 0.35
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = `rgba(0, 180, 42, ${alpha})`
          ctx.lineWidth = 1
          ctx.stroke()
        }
      }
    }

    // 鼠标到粒子的连线
    for (const p of particles) {
      const dx = p.x - data.mouseX
      const dy = p.y - data.mouseY
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < MOUSE_RADIUS) {
        const alpha = (1 - dist / MOUSE_RADIUS) * 0.5
        ctx.beginPath()
        ctx.moveTo(data.mouseX, data.mouseY)
        ctx.lineTo(p.x, p.y)
        ctx.strokeStyle = `rgba(0, 180, 42, ${alpha})`
        ctx.lineWidth = 1
        ctx.stroke()
      }
    }

    pAnimFrameId = requestAnimationFrame(animate)
  }
  animate()
}

const handleMouseMove = (e) => {
  data.mouseX = e.clientX
  data.mouseY = e.clientY
}

// ===== 三击标题彩蛋 =====
const handleTitleClick = () => {
  data.titleClickCount++
  clearTimeout(data.titleClickTimer)

  if (data.titleClickCount >= 3) {
    data.titleClickCount = 0
    triggerTitleEgg()
  } else {
    data.titleClickTimer = setTimeout(() => {
      data.titleClickCount = 0
    }, 500)
  }
}

const triggerTitleEgg = () => {
  data.titleGlitch = true

  // 文字逐字闪烁变成摩斯码效果
  const morseMap = { '北': '-...', '冥': '--.', '·': '.-.-', '群': '--.-', '组': '--..', '服': '...-.', ' ': ' ', '注': '-..-', '册': '.--.' }
  const original = TITLE_ORIGINAL
  const chars = original.split('')

  let step = 0
  const interval = setInterval(() => {
    if (step < chars.length) {
      // 逐个变成摩斯码
      const display = chars.map((c, i) => i <= step ? (morseMap[c] || c) : c).join(' ')
      data.titleDisplay = display
      step++
    } else if (step < chars.length + 3) {
      step++
    } else {
      // 恢复原文字
      data.titleDisplay = original
      data.titleGlitch = false
      clearInterval(interval)
    }
  }, 150)

  // 记录彩蛋
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  if (user.id) {
    request.post('/easterEgg/discover', {
      userId: user.id,
      userName: user.name,
      userRole: user.role,
      eggName: 'register_triple_click'
    }).then(res => {
      if (res.code === '200' && !res.data?.alreadyDiscovered) {
        ElMessage.success({ message: '你发现了隐藏彩蛋！摩斯密码大师', duration: 3000 })
      }
    })
  } else {
    ElMessage.success({ message: '摩斯密码？注册登录后可以计入彩蛋榜！', duration: 3000 })
  }
}

// 初始化等待审批页的互动粒子
const initPendingDots = () => {
  const colors = ['rgba(0, 180, 42, 0.6)', 'rgba(64, 158, 255, 0.5)', 'rgba(255, 125, 0, 0.5)', 'rgba(255, 255, 255, 0.4)']
  data.pendingDots = []
  for (let i = 0; i < 50; i++) {
    data.pendingDots.push({
      id: i,
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      size: 6 + Math.random() * 20,
      color: colors[Math.floor(Math.random() * colors.length)],
      opacity: 0.3 + Math.random() * 0.5,
      phase: Math.random() * Math.PI * 2
    })
  }
}

const handlePendingMouseMove = (e) => {
  const cx = window.innerWidth / 2
  const cy = window.innerHeight / 2
  data.pendingOffX = (e.clientX - cx) * 0.03
  data.pendingOffY = (e.clientY - cy) * 0.03
}

const goLogin = () => {
  router.push('/login')
}

// 注册成功大动画
const showSuccessAnimation = (msg, sub) => {
  data.successMsg = msg
  data.successSub = sub
  data.showSuccess = true
  nextTick(() => {
    const canvas = successCanvas.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const particles = []
    const colors = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff9ff3', '#54a0ff', '#5f27cd', '#48dbfb']

    for (let wave = 0; wave < 5; wave++) {
      setTimeout(() => {
        const cx = canvas.width * (0.2 + Math.random() * 0.6)
        const cy = canvas.height * (0.15 + Math.random() * 0.4)
        const color = colors[Math.floor(Math.random() * colors.length)]
        for (let i = 0; i < 60; i++) {
          const angle = (Math.PI * 2 / 60) * i + Math.random() * 0.2
          const speed = 3 + Math.random() * 5
          particles.push({
            x: cx, y: cy,
            vx: Math.cos(angle) * speed,
            vy: Math.sin(angle) * speed,
            life: 1,
            decay: 0.008 + Math.random() * 0.012,
            color,
            size: 2 + Math.random() * 3
          })
        }
      }, wave * 500)
    }

    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      let alive = false
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i]
        p.x += p.vx
        p.y += p.vy
        p.vy += 0.05
        p.vx *= 0.99
        p.life -= p.decay
        if (p.life <= 0) {
          particles.splice(i, 1)
          continue
        }
        alive = true
        ctx.globalAlpha = p.life
        ctx.fillStyle = p.color
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2)
        ctx.fill()
      }
      ctx.globalAlpha = 1
      if (alive) {
        requestAnimationFrame(animate)
      }
    }
    requestAnimationFrame(animate)

    setTimeout(() => {
      router.push('/login')
    }, 2500)
  })
}

const submitRequest = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.loading = true
      const requestData = {
        username: data.form.username,
        password: data.form.password,
        name: data.form.name,
        studentNo: data.form.studentNo,
        email: data.form.email,
        phone: data.form.phone || null,
        branch: data.form.branch || '其他',
        approvalToken: data.form.invitationCode || null
      }

      request.post('/registration/register', requestData).then(res => {
        data.loading = false
        if (res.code === '200') {
          if (data.form.invitationCode) {
            showSuccessAnimation('注册成功！', '正在跳转到登录页面...')
          } else {
            initPendingDots()
            data.showPending = true
          }
        } else {
          ElMessage.error(res.msg || '提交失败，请稍后重试')
        }
      }).catch(() => {
        data.loading = false
        ElMessage.error('网络错误，请稍后再试')
      })
    }
  })
}

onMounted(() => {
  initCanvasParticles()
})

onUnmounted(() => {
  if (pAnimFrameId) cancelAnimationFrame(pAnimFrameId)
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1d2129 0%, #2b2f38 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.register-box {
  width: 450px;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.95);
  position: relative;
  z-index: 10;
}

/* 标题 glitch 效果 */
.title-glitch {
  animation: glitch 0.3s ease-in-out;
}

@keyframes glitch {
  0% { transform: translate(0); }
  20% { transform: translate(-3px, 2px); }
  40% { transform: translate(3px, -1px); }
  60% { transform: translate(-1px, -2px); }
  80% { transform: translate(2px, 1px); }
  100% { transform: translate(0); }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

a {
  color: #00b42a;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

/* ========== 成功动画 ========== */
.success-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
}

.success-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.success-content {
  text-align: center;
  color: white;
  z-index: 10;
  animation: successPop 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes successPop {
  from { transform: scale(0.5); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.success-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00b42a, #00d034);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: 700;
  color: white;
  margin: 0 auto 24px;
  box-shadow: 0 8px 32px rgba(0, 180, 42, 0.4);
}

.success-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.success-sub {
  font-size: 16px;
  opacity: 0.8;
}

/* ========== 等待审批互动页 ========== */
.pending-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d2129 0%, #2b2f38 100%);
  overflow: hidden;
}

.pending-content {
  text-align: center;
  color: white;
  z-index: 10;
  transition: transform 0.15s ease-out;
}

.pending-icon-wrap {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pending-ring {
  position: absolute;
  inset: 0;
  border: 3px solid rgba(0, 180, 42, 0.3);
  border-top-color: #00b42a;
  border-radius: 50%;
  animation: pendingSpin 1.5s linear infinite;
}

.pending-ring.delay {
  inset: -10px;
  border-top-color: rgba(0, 180, 42, 0.15);
  animation-duration: 2.5s;
  animation-direction: reverse;
}

@keyframes pendingSpin {
  to { transform: rotate(360deg); }
}

.pending-icon {
  color: #00b42a;
}

.pending-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.pending-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.pending-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.pending-dot {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  transition: transform 0.5s ease;
}
</style>
