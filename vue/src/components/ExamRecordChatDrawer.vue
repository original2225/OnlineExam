<template>
  <el-drawer
    v-model="visible"
    title="答卷追问室"
    size="420px"
    :append-to-body="true"
    class="chat-drawer"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <div class="chat-wrap" v-loading="data.loading">
      <div class="chat-record-card" v-if="data.record">
        <div>
          <strong>{{ data.record.examName || '审核答卷' }}</strong>
          <span>{{ data.record.studentName || '考生' }} <em v-if="data.record.studentNo">#{{ data.record.studentNo }}</em></span>
        </div>
        <el-tag :type="data.locked ? 'info' : 'success'" size="small">
          {{ data.locked ? '已归档' : '可追问' }}
        </el-tag>
      </div>

      <el-alert
        v-if="data.locked"
        title="终审已完成，追问室已只读归档。"
        type="info"
        :closable="false"
        show-icon
        class="chat-alert"
      />

      <div ref="messageListRef" class="chat-message-list">
        <div v-if="!data.messages.length" class="chat-empty">
          <el-icon><ChatDotRound /></el-icon>
          <span>暂无消息，可以在这里补充说明或进行口头追问的文字记录。</span>
        </div>

        <div
          v-for="message in data.messages"
          :key="message.id"
          class="chat-message"
          :class="{ mine: isMine(message) }"
        >
          <div class="chat-avatar">{{ (message.senderName || '?').slice(0, 1) }}</div>
          <div class="chat-bubble">
            <div class="chat-meta">
              <strong>{{ message.senderName || roleText(message.senderRole) }}</strong>
              <span>{{ roleText(message.senderRole) }} · {{ formatTime(message.createdAt) }}</span>
            </div>
            <div class="chat-content">{{ message.content }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="data.content"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          resize="none"
          :disabled="data.locked || data.sending || !recordId"
          placeholder="输入追问、补充说明或口头问答记录..."
          @keydown.ctrl.enter.prevent="send"
        />
        <div class="chat-actions">
          <span>Ctrl + Enter 发送</span>
          <el-button type="primary" :disabled="data.locked || !data.content.trim()" :loading="data.sending" @click="send">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from "vue"
import { ElMessage } from "element-plus"
import { ChatDotRound } from "@element-plus/icons-vue"
import request from "@/utils/request.js"

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  recordId: { type: [Number, String], default: null },
  record: { type: Object, default: null },
})

const emit = defineEmits(["update:modelValue", "sent", "read"])

const visible = computed({
  get: () => props.modelValue,
  set: value => emit("update:modelValue", value),
})

const user = JSON.parse(localStorage.getItem("beiming-onlineexam-user") || "{}")
const messageListRef = ref(null)
let timer = null

const data = reactive({
  loading: false,
  sending: false,
  content: "",
  messages: [],
  record: null,
  locked: false,
})

const recordId = computed(() => props.recordId ? Number(props.recordId) : null)

watch(() => props.record, value => {
  if (value) setRecord(value)
}, { immediate: true })

watch(recordId, () => {
  if (visible.value) handleOpen()
})

const roleText = (role) => ({ OWNER: "所有者", ADMIN: "管理员", HELPER: "阅卷人", USER: "考生" }[role] || role || "成员")
const isMine = (message) => message.senderId === user.id && message.senderRole === user.role
const isFinal = (status) => status === "PASSED" || status === "FAILED"

const setRecord = (record) => {
  data.record = record
  data.locked = isFinal(record.examStatus)
}

const handleOpen = () => {
  if (!recordId.value) return
  loadRecord()
  loadMessages()
  stopPolling()
  timer = setInterval(loadMessages, 5000)
}

const handleClosed = () => {
  stopPolling()
  data.content = ""
}

const stopPolling = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const loadRecord = () => {
  if (props.record) {
    setRecord(props.record)
    return
  }
  request.get("/examRecord/detail/" + recordId.value, { silentError: true }).then(res => {
    if (res.code === "200") setRecord(res.data || {})
  }).catch(() => {})
}

const loadMessages = () => {
  if (!recordId.value) return
  const afterId = data.messages.length ? data.messages[data.messages.length - 1].id : null
  data.loading = data.messages.length === 0
  request.get("/examRecordChat/messages", {
    silentError: true,
    params: { recordId: recordId.value, afterId },
  }).then(res => {
    if (res.code === "200") {
      const incoming = res.data || []
      if (incoming.length) {
        const existing = new Set(data.messages.map(item => item.id))
        data.messages.push(...incoming.filter(item => !existing.has(item.id)))
        markRead()
        nextTick(scrollToBottom)
      } else if (data.messages.length) {
        markRead()
      }
      emit("read")
    }
  }).finally(() => {
    data.loading = false
  })
}

const markRead = () => {
  const last = data.messages[data.messages.length - 1]
  if (!last) return
  request.put("/examRecordChat/read", null, {
    silentError: true,
    params: { recordId: recordId.value, messageId: last.id },
  }).catch(() => {})
}

const send = () => {
  const content = data.content.trim()
  if (!content || data.locked || !recordId.value) return
  data.sending = true
  request.post("/examRecordChat/send", { recordId: recordId.value, content }).then(res => {
    if (res.code === "200") {
      data.content = ""
      data.messages.push(res.data)
      emit("sent", res.data)
      nextTick(scrollToBottom)
    } else {
      ElMessage.error(res.msg || "发送失败")
    }
  }).finally(() => {
    data.sending = false
  })
}

const scrollToBottom = () => {
  const el = messageListRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const formatTime = (value) => {
  if (!value) return ""
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace("T", " ").substring(0, 16)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`
}
</script>

<style scoped>
.chat-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-record-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-page);
}

.chat-record-card div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-record-card strong {
  overflow: hidden;
  color: var(--text-primary);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-record-card span {
  color: var(--text-secondary);
  font-size: 12px;
}

.chat-record-card em {
  font-style: normal;
  opacity: 0.75;
}

.chat-alert {
  flex-shrink: 0;
}

.chat-message-list {
  flex: 1;
  min-height: 260px;
  overflow-y: auto;
  padding: 4px 2px 8px;
}

.chat-empty {
  height: 100%;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.7;
}

.chat-empty .el-icon {
  font-size: 32px;
  color: var(--primary-color);
}

.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.chat-message.mine {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 800;
}

.chat-bubble {
  max-width: 78%;
  padding: 10px 12px;
  border: 1px solid var(--border-lighter);
  border-radius: 8px;
  background: var(--bg-card);
}

.chat-message.mine .chat-bubble {
  border-color: rgba(var(--primary-rgb), 0.28);
  background: var(--primary-light);
}

.chat-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.chat-meta strong {
  color: var(--text-primary);
  font-size: 12px;
}

.chat-meta span {
  color: var(--text-secondary);
  font-size: 11px;
}

.chat-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-input-area {
  flex-shrink: 0;
  padding-top: 12px;
  border-top: 1px solid var(--border-lighter);
}

.chat-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.chat-actions span {
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
