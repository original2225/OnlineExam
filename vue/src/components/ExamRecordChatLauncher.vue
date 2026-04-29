<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" @show="loadRooms">
    <template #reference>
      <div class="chat-launcher">
        <el-icon :size="18"><ChatDotRound /></el-icon>
        <span v-if="totalUnread > 0" class="chat-badge">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
      </div>
    </template>

    <div class="chat-room-panel">
      <div class="chat-room-header">
        <strong>追问消息</strong>
        <el-button text type="primary" size="small" @click="loadRooms">刷新</el-button>
      </div>
      <div class="chat-room-list" v-loading="data.loading">
        <div v-if="!data.rooms.length" class="chat-room-empty">暂无追问消息</div>
        <button
          v-for="room in data.rooms"
          :key="room.recordId"
          type="button"
          class="chat-room-item"
          @click="openRoom(room)"
        >
          <span class="room-main">
            <strong>{{ room.examName || '审核答卷' }}</strong>
            <small>{{ room.studentName || '考生' }}：{{ room.lastMessageContent || '暂无内容' }}</small>
          </span>
          <span v-if="room.unreadCount" class="room-unread">{{ room.unreadCount }}</span>
        </button>
      </div>
    </div>
  </el-popover>

  <ExamRecordChatDrawer
    v-model="data.drawerVisible"
    :record-id="data.activeRecordId"
    :record="data.activeRoom"
    @read="loadRooms"
    @sent="loadRooms"
  />
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive } from "vue"
import { ChatDotRound } from "@element-plus/icons-vue"
import request from "@/utils/request.js"
import ExamRecordChatDrawer from "@/components/ExamRecordChatDrawer.vue"

const data = reactive({
  loading: false,
  rooms: [],
  drawerVisible: false,
  activeRecordId: null,
  activeRoom: null,
})

let timer = null
const totalUnread = computed(() => data.rooms.reduce((sum, item) => sum + (item.unreadCount || 0), 0))

const loadRooms = () => {
  data.loading = data.rooms.length === 0
  request.get("/examRecordChat/myRooms", { silentError: true }).then(res => {
    if (res.code === "200") data.rooms = res.data || []
  }).finally(() => {
    data.loading = false
  })
}

const openRoom = (room) => {
  data.activeRoom = room
  data.activeRecordId = room.recordId
  data.drawerVisible = true
}

onMounted(() => {
  loadRooms()
  timer = setInterval(loadRooms, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-launcher {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  color: inherit;
  opacity: 0.82;
  transition: all 0.2s;
}

.chat-launcher:hover {
  opacity: 1;
  background: rgba(255,255,255,0.1);
}

.chat-badge,
.room-unread {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
}

.chat-badge {
  position: absolute;
  top: 0;
  right: 0;
}

.chat-room-panel {
  margin: -12px;
}

.chat-room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-lighter, #f0f0f0);
}

.chat-room-list {
  max-height: 360px;
  overflow-y: auto;
}

.chat-room-empty {
  padding: 40px 0;
  color: var(--text-secondary, #999);
  text-align: center;
  font-size: 14px;
}

.chat-room-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border: 0;
  border-bottom: 1px solid var(--border-lighter, #f5f5f5);
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.chat-room-item:hover {
  background: var(--bg-page, #f6f8fa);
}

.room-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.room-main strong {
  overflow: hidden;
  color: var(--text-primary, #333);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-main small {
  overflow: hidden;
  color: var(--text-secondary, #666);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
