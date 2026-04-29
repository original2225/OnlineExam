<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" @show="onOpen">
    <template #reference>
      <div class="bell-trigger">
        <el-icon :size="18"><Bell /></el-icon>
        <span v-if="data.unreadCount > 0" class="bell-badge">{{ data.unreadCount > 99 ? '99+' : data.unreadCount }}</span>
      </div>
    </template>
    <div class="notif-panel">
      <div class="notif-header">
        <span class="notif-title">消息通知</span>
        <el-button type="primary" text size="small" @click="markAllRead" :disabled="data.unreadCount === 0">全部已读</el-button>
      </div>
      <div class="notif-list">
        <div v-if="data.list.length === 0" class="notif-empty">暂无通知</div>
        <div v-for="n in data.list" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }" @click="handleNotif(n)">
          <div class="notif-dot" v-if="!n.isRead"></div>
          <div class="notif-body">
            <div class="notif-item-title">{{ n.title }}</div>
            <div class="notif-item-content">{{ n.content }}</div>
            <div class="notif-time">{{ formatTime(n.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')

const data = reactive({
  list: [],
  unreadCount: 0,
})
const silent = { silentError: true }

let timer = null

onMounted(() => {
  loadUnread()
  timer = setInterval(loadUnread, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const loadUnread = () => {
  if (!user.id) return
  request.get('/notification/unreadCount', {
    ...silent,
    params: { userId: user.id, userRole: user.role }
  }).then(res => {
    data.unreadCount = res.data || 0
  }).catch(() => {})
}

const onOpen = () => {
  loadList()
}

const loadList = () => {
  request.get('/notification/selectByUser', {
    ...silent,
    params: { userId: user.id, userRole: user.role }
  }).then(res => {
    data.list = res.data || []
  })
}

const markAllRead = () => {
  request.put('/notification/readAll', null, {
    ...silent,
    params: { userId: user.id, userRole: user.role }
  }).then(res => {
    if (res.code === '200') {
      data.unreadCount = 0
      data.list.forEach(n => n.isRead = true)
    }
  })
}

const handleNotif = (n) => {
  if (!n.isRead) {
    request.put('/notification/read/' + n.id, null, silent).then(() => {
      n.isRead = true
      data.unreadCount = Math.max(0, data.unreadCount - 1)
    })
  }
  if (n.link) {
    router.push(n.link)
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return Math.floor(diff / 86400) + '天前'
}
</script>

<style scoped>
.bell-trigger {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  color: inherit;
  opacity: 0.8;
}
.bell-trigger:hover {
  opacity: 1;
  background: rgba(255,255,255,0.1);
}
.bell-badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  font-size: 10px;
  text-align: center;
  background: #f56c6c;
  color: #fff;
  border-radius: 8px;
  padding: 0 4px;
}
.notif-panel { margin: -12px; }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f0f0f0; }
.notif-title { font-size: 15px; font-weight: 600; }
.notif-list { max-height: 360px; overflow-y: auto; }
.notif-empty { text-align: center; padding: 40px 0; color: #999; font-size: 14px; }
.notif-item { display: flex; gap: 10px; padding: 12px 16px; cursor: pointer; transition: background 0.2s; border-bottom: 1px solid #f5f5f5; }
.notif-item:hover { background: #f6f8fa; }
.notif-item.unread { background: #ecf5ff; }
.notif-item.unread:hover { background: #d9ecff; }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: #409eff; flex-shrink: 0; margin-top: 6px; }
.notif-body { flex: 1; min-width: 0; }
.notif-item-title { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 4px; }
.notif-item-content { font-size: 12px; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notif-time { font-size: 11px; color: #999; margin-top: 4px; }
</style>
