<template>
  <el-popover placement="bottom" :width="380" trigger="click">
    <template #reference>
      <div class="theme-trigger" :title="'切换主题 — ' + themeName">
        <el-icon :size="18"><Brush /></el-icon>
      </div>
    </template>
    <div class="theme-panel">
      <div class="tp-header">
        <span class="tp-title">选择主题风格</span>
        <span class="tp-count">{{ themes.length }} 款主题</span>
      </div>
      <div class="tp-grid">
        <div v-for="t in themes" :key="t.id"
             class="tp-card"
             :class="{ active: currentTheme === t.id }"
             @click="switchTheme(t.id)">
          <div class="tp-preview" :style="{ background: t.headerBg }">
            <div class="tp-accent" :style="{ background: t.accent }"></div>
            <div class="tp-lines">
              <div class="tp-line" :style="{ background: t.accent, opacity: 0.3 }"></div>
              <div class="tp-line" :style="{ background: t.accent, opacity: 0.2, width: '60%' }"></div>
              <div class="tp-line" :style="{ background: t.accent, opacity: 0.15, width: '40%' }"></div>
            </div>
          </div>
          <div class="tp-name">{{ t.name }}</div>
          <div v-if="currentTheme === t.id" class="tp-check">
            <el-icon :size="14"><Check /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed } from 'vue'

const themes = [
  { id: 'default',      name: '力扣绿',     headerBg: 'linear-gradient(135deg, #1d2129, #2b2f38)', accent: '#00b42a' },
  { id: 'ocean',        name: '海洋蓝',     headerBg: 'linear-gradient(135deg, #0c2d48, #145da0)', accent: '#2196f3' },
  { id: 'dark-purple',  name: '暗夜紫',     headerBg: 'linear-gradient(135deg, #2e1065, #1e1b4b)', accent: '#8b5cf6' },
  { id: 'emerald',      name: '翡翠绿',     headerBg: 'linear-gradient(135deg, #064e3b, #065f46)', accent: '#059669' },
  { id: 'sunset',       name: '暖阳橙',     headerBg: 'linear-gradient(135deg, #7c2d12, #9a3412)', accent: '#ea580c' },
  { id: 'rose',         name: '玫瑰红',     headerBg: 'linear-gradient(135deg, #4a0528, #881337)', accent: '#f43f5e' },
  { id: 'sakura',       name: '樱花粉',     headerBg: 'linear-gradient(135deg, #5b2343, #8b3a62)', accent: '#f9a8d4' },
  { id: 'cyber',        name: '赛博朋克',   headerBg: 'linear-gradient(135deg, #0a0a1a, #1a0a2e)', accent: '#06f9e0' },
  { id: 'aurora',       name: '极光',       headerBg: 'linear-gradient(135deg, #0f172a, #1e3a5f)', accent: '#38bdf8' },
  { id: 'dark',         name: '深色模式',   headerBg: 'linear-gradient(135deg, #111827, #1f2937)', accent: '#4ade80' },
  { id: 'glass',        name: '毛玻璃',     headerBg: 'linear-gradient(135deg, #e0e5ec, #c5ccd6)', accent: '#00b42a' },
  { id: 'gold',         name: '黑金',       headerBg: 'linear-gradient(135deg, #1a1a1a, #2d2d2d)', accent: '#fbbf24' },
]

const currentTheme = ref(localStorage.getItem('theme') || 'default')

const themeName = computed(() => {
  const t = themes.find(t => t.id === currentTheme.value)
  return t ? t.name : '力扣绿'
})

const emit = defineEmits(['change'])

const switchTheme = (id) => {
  currentTheme.value = id
  localStorage.setItem('theme', id)
  document.documentElement.setAttribute('data-theme', id)
  emit('change', id)
}

if (!document.documentElement.getAttribute('data-theme') || document.documentElement.getAttribute('data-theme') !== currentTheme.value) {
  document.documentElement.setAttribute('data-theme', currentTheme.value)
}
</script>

<style scoped>
.theme-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  color: inherit;
  opacity: 0.7;
}
.theme-trigger:hover {
  opacity: 1;
  background: rgba(255,255,255,0.1);
}
.tp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.tp-title { font-size: 14px; font-weight: 600; color: #333; }
.tp-count { font-size: 11px; color: #999; }
.tp-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.tp-card {
  position: relative;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 3px;
  cursor: pointer;
  transition: all 0.2s;
}
.tp-card:hover {
  border-color: var(--primary-color, #00b42a);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.tp-card.active {
  border-color: var(--primary-color, #00b42a);
  box-shadow: 0 0 0 2px rgba(0,180,42,0.2);
}
.tp-preview {
  height: 36px;
  border-radius: 5px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 6px;
}
.tp-accent {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 6px currentColor;
}
.tp-lines {
  flex: 1;
  margin-left: 6px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.tp-line { height: 2px; border-radius: 1px; }
.tp-name {
  text-align: center;
  font-size: 10px;
  color: #666;
  margin-top: 4px;
  line-height: 1.2;
}
.tp-check {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--primary-color, #00b42a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
