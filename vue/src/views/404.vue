<template>
  <div class="not-found-page">
    <canvas ref="bgCanvas" class="bg-canvas"></canvas>
    <div class="nf-content">
      <div class="nf-code" :class="{ 'glitch': data.glitching }">
        <span class="nf-4">4</span>
        <span class="nf-0">
          <span class="nf-eye">{{ data.eyeSymbol }}</span>
        </span>
        <span class="nf-4">4</span>
      </div>
      <h1 class="nf-title">哎呀，页面走丢了</h1>
      <p class="nf-desc">你访问的页面不存在，或许是被末影人搬走了</p>

      <div class="nf-actions">
        <el-button type="primary" size="large" round @click="goHome">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button size="large" round @click="goBack">
          <el-icon><Back /></el-icon> 返回上页
        </el-button>
      </div>

      <div class="nf-fun">
        <div class="nf-cat" @click="petCat">
          <div class="cat-body">{{ data.catMood }}</div>
        </div>
        <span class="nf-cat-hint">{{ data.catHint }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const bgCanvas = ref(null)
let animFrame = null

const data = reactive({
  glitching: false,
  eyeSymbol: 'o',
  catMood: '(=^・・^=)',
  catHint: '戳一下猫咪试试',
  clickCount: 0,
})

const catMoods = [
  { mood: '(=^・・^=)', hint: '戳一下猫咪试试' },
  { mood: '(=^・ω・^=)', hint: '猫咪很开心！' },
  { mood: '(=^・∇・^=)', hint: '喵~' },
  { mood: '(=^≧∀≦^=)', hint: '猫咪好兴奋！' },
  { mood: '(=^・ｪ・^=)', hint: '呼噜呼噜...' },
  { mood: '( ∩^・ω・^)⊃━☆', hint: '猫咪施了魔法！' },
  { mood: 'ฅ^•ﻌ•^ฅ', hint: '猫咪在看你' },
  { mood: '(=^･ｪ･^=)∫', hint: '猫咪要抱抱' },
]

const petCat = () => {
  data.clickCount++
  const idx = data.clickCount % catMoods.length
  data.catMood = catMoods[idx].mood
  data.catHint = catMoods[idx].hint

  // 每5次触发 glitch
  if (data.clickCount % 5 === 0) {
    data.glitching = true
    setTimeout(() => { data.glitching = false }, 800)
  }
}

const goHome = () => {
  try {
    const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
    if (user.role === 'OWNER' || user.role === 'ADMIN' || user.role === 'HELPER') {
      window.location.href = '/manager/home'
    } else if (user.role === 'USER') {
      window.location.href = '/front/home'
    } else {
      window.location.href = '/'
    }
  } catch {
    window.location.href = '/'
  }
}

const goBack = () => {
  window.history.back()
}

// 背景粒子
const initBg = () => {
  const canvas = bgCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const particles = []
  for (let i = 0; i < 40; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      size: 2 + Math.random() * 3,
      alpha: 0.2 + Math.random() * 0.3,
    })
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    for (const p of particles) {
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(102, 126, 234, ${p.alpha})`
      ctx.fill()
    }
    // 连线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 150) {
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = `rgba(102, 126, 234, ${(1 - dist / 150) * 0.15})`
          ctx.lineWidth = 1
          ctx.stroke()
        }
      }
    }
    animFrame = requestAnimationFrame(animate)
  }
  animate()
}

onMounted(initBg)
onUnmounted(() => { if (animFrame) cancelAnimationFrame(animFrame) })
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.bg-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.nf-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.nf-code {
  font-size: 120px;
  font-weight: 900;
  letter-spacing: 16px;
  margin-bottom: 16px;
  user-select: none;
}

.nf-4 {
  display: inline-block;
  background: linear-gradient(135deg, #ff6b6b, #ffd93d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nf-0 {
  display: inline-block;
  width: 1em;
  height: 1em;
  position: relative;
}

.nf-eye {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 48px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: blink 4s ease-in-out infinite;
}

@keyframes blink {
  0%, 95%, 100% { transform: translate(-50%, -50%) scaleY(1); }
  97% { transform: translate(-50%, -50%) scaleY(0.1); }
}

.nf-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.9);
}

.nf-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 32px;
}

.nf-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 48px;
}

.nf-fun {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.nf-cat {
  cursor: pointer;
  transition: transform 0.2s;
  user-select: none;
}
.nf-cat:hover {
  transform: scale(1.1);
}
.nf-cat:active {
  transform: scale(0.95);
}

.cat-body {
  font-size: 32px;
  transition: all 0.3s;
}

.nf-cat-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

/* Glitch effect */
.glitch .nf-4 {
  animation: glitchNum 0.15s ease-in-out 4;
}
@keyframes glitchNum {
  0% { transform: translate(0); }
  25% { transform: translate(-4px, 2px); }
  50% { transform: translate(4px, -2px); }
  75% { transform: translate(-2px, -4px); }
  100% { transform: translate(0); }
}
</style>
