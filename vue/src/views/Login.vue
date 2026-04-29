<template>
  <div class="auth-page">
    <button class="auth-close" type="button" @click="goBack">
      <el-icon><Close /></el-icon>
    </button>

    <section class="auth-shell">
      <div class="auth-visual">
        <img src="@/assets/imgs/logo.png" alt="北冥审核系统" />
        <p>北冥审核系统</p>
        <h1>进服审核、题库练习、结果管理统一入口</h1>
        <div class="auth-points">
          <span>四项审核方向</span>
          <span>在线答题与阅卷</span>
          <span>后台数据闭环</span>
        </div>
      </div>

      <div class="auth-panel">
        <div class="auth-header">
          <span>账号登录</span>
          <h2>欢迎回来</h2>
          <p>选择你的角色并输入账号信息。</p>
        </div>

        <el-form ref="formRef" :model="data.form" :rules="data.rules" label-position="top" class="auth-form">
          <el-form-item label="账号" prop="username">
            <el-autocomplete
              v-model="data.form.username"
              :fetch-suggestions="querySearch"
              placeholder="请输入账号"
              size="large"
              style="width: 100%"
              @select="handleHistorySelect"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
              <template #default="{ item }">
                <div class="history-item">
                  <span>{{ item.value }}</span>
                  <small>{{ roleText(item.role) }}</small>
                  <button type="button" @click.stop="deleteHistory(item.value)">删除</button>
                </div>
              </template>
            </el-autocomplete>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="data.form.password"
              :prefix-icon="Lock"
              placeholder="请输入密码"
              show-password
              size="large"
              @keyup.enter="login"
            />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-segmented v-model="data.form.role" :options="roleOptions" block />
          </el-form-item>

          <el-button
            :loading="data.loading"
            class="auth-submit"
            size="large"
            type="primary"
            @click="login"
          >
            登录
          </el-button>
        </el-form>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <button type="button" @click="router.push('/register')">提交注册申请</button>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="data.dialogVisible"
      title="审核规则声明"
      width="520px"
      :show-close="false"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="declare-content">
        <p>本平台用于 Minecraft Java 生电服务器入服审核，请按要求提供真实信息并独立完成审核。</p>
        <p>审核过程中严禁作弊、代答或扰乱秩序。违反规则会取消审核资格并记录在案。</p>
        <p>继续进入即表示你已阅读并同意遵守服务器和审核规范。</p>
      </div>
      <template #footer>
        <el-button type="primary" size="large" style="width: 100%" @click="confirmAndEnter">
          我已阅读并同意
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue"
import { useRoute } from "vue-router"
import { Close, Lock, User } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import request from "@/utils/request.js"
import router from "@/router/index.js"

const emit = defineEmits(["close"])
const route = useRoute()
const formRef = ref()

const roleOptions = [
  { label: "管理员", value: "ADMIN" },
  { label: "玩家", value: "USER" },
]

const data = reactive({
  dialogVisible: false,
  loading: false,
  loginHistory: [],
  pendingPath: "",
  form: { username: "", password: "", role: "USER" },
  rules: {
    username: [{ required: true, message: "请输入账号", trigger: "blur" }],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }],
    role: [{ required: true, message: "请选择角色", trigger: "change" }],
  },
})

const loginRole = (role) => (role === "OWNER" || role === "HELPER" || role === "ADMIN" ? "ADMIN" : "USER")
const roleText = (role) => (loginRole(role) === "ADMIN" ? "管理员" : "玩家")

const defaultPathByRole = (role) => {
  if (role === "OWNER" || role === "ADMIN") return "/manager/home"
  if (role === "HELPER") return "/exam/dashboard"
  return "/front/home"
}

const loadHistory = () => {
  try {
    data.loginHistory = JSON.parse(localStorage.getItem("beiming-onlineexam-login-history") || "[]")
      .map(item => ({ ...item, role: loginRole(item.role) }))
  } catch {
    data.loginHistory = []
  }
}

const querySearch = (queryString, cb) => {
  const keyword = (queryString || "").toLowerCase()
  cb(data.loginHistory.filter(item => item.value.toLowerCase().includes(keyword)).slice(0, 5))
}

const handleHistorySelect = (item) => {
  data.form.username = item.value
  data.form.role = loginRole(item.role)
}

const deleteHistory = (value) => {
  data.loginHistory = data.loginHistory.filter(item => item.value !== value)
  localStorage.setItem("beiming-onlineexam-login-history", JSON.stringify(data.loginHistory))
}

const saveToHistory = (username, role) => {
  data.loginHistory = data.loginHistory.filter(item => item.value !== username)
  data.loginHistory.unshift({ value: username, role: loginRole(role) })
  data.loginHistory = data.loginHistory.slice(0, 5)
  localStorage.setItem("beiming-onlineexam-login-history", JSON.stringify(data.loginHistory))
}

const login = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    data.loading = true
    request.post("/login", data.form).then(res => {
      data.loading = false
      if (res.code !== "200") {
        ElMessage.error(res.msg || "登录失败")
        return
      }

      saveToHistory(data.form.username, data.form.role)
      localStorage.setItem("beiming-onlineexam-user", JSON.stringify(res.data))
      ElMessage.success("登录成功")

      const target = data.pendingPath || defaultPathByRole(res.data.role)
      if (res.data.role === "USER") {
        data.pendingPath = target
        data.dialogVisible = true
        return
      }
      router.push(target)
    }).catch(() => {
      data.loading = false
      ElMessage.error("网络错误，请稍后再试")
    })
  })
}

const confirmAndEnter = () => {
  data.dialogVisible = false
  router.push(data.pendingPath || "/front/home")
}

const goBack = () => {
  emit("close")
  router.push("/")
}

onMounted(() => {
  loadHistory()
  data.pendingPath = route.query.redirect ? decodeURIComponent(String(route.query.redirect)) : ""
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background-image:
    linear-gradient(115deg, rgba(13, 24, 34, 0.82), rgba(13, 24, 34, 0.42)),
    url("@/assets/imgs/review/review-scenes.png");
  background-size: auto, 400% 200%;
  background-position: center, 66.666% 0%;
}

.auth-close {
  position: fixed;
  top: 22px;
  right: 22px;
  z-index: 3;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
}

.auth-shell {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1fr 420px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 10px;
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.34);
}

.auth-visual {
  min-height: 540px;
  padding: 44px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: rgba(9, 17, 28, 0.54);
  color: #fff;
  backdrop-filter: blur(6px);
}

.auth-visual img {
  width: 64px;
  height: 64px;
  margin-bottom: auto;
  border-radius: 14px;
}

.auth-visual p {
  margin: 0 0 10px;
  color: #8ee6b0;
  font-weight: 900;
}

.auth-visual h1 {
  max-width: 440px;
  margin: 0;
  color: #fff;
  font-size: 36px;
  line-height: 1.18;
}

.auth-points {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.auth-points span {
  padding: 7px 10px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 12px;
}

.auth-panel {
  padding: 44px 38px;
  background: var(--bg-card);
}

.auth-header span {
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 900;
}

.auth-header h2 {
  margin: 8px 0 6px;
  color: var(--text-primary);
  font-size: 30px;
}

.auth-header p {
  margin: 0 0 28px;
  color: var(--text-secondary);
  font-size: 14px;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
}

.auth-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 18px;
  color: var(--text-secondary);
  font-size: 13px;
}

.auth-footer button,
.history-item button {
  border: 0;
  background: transparent;
  color: var(--primary-color);
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-item span {
  flex: 1;
}

.history-item small {
  color: var(--text-secondary);
}

.history-item button {
  font-size: 12px;
}

.declare-content {
  color: var(--text-regular);
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 860px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-visual {
    min-height: 260px;
  }
}

@media (max-width: 560px) {
  .auth-page {
    padding: 14px;
  }

  .auth-visual {
    display: none;
  }

  .auth-panel {
    padding: 30px 20px;
  }
}
</style>
