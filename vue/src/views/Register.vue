<template>
  <div class="register-page">
    <section class="register-shell">
      <aside class="register-guide">
        <button class="guide-brand" type="button" @click="router.push('/')">
          <img src="@/assets/imgs/logo.png" alt="北冥审核系统" />
          <span>北冥审核系统</span>
        </button>
        <h1>提交进服审核注册申请</h1>
        <p>邀请码会直接完成对应身份注册；没有邀请码时，申请会进入管理员审批流程。</p>
        <div class="guide-steps">
          <span>1. 填写身份信息</span>
          <span>2. 选择报考方向</span>
          <span>3. 等待审批或直接登录</span>
        </div>
      </aside>

      <main class="register-panel">
        <div class="form-heading">
          <span>注册申请</span>
          <h2>创建账号</h2>
        </div>

        <el-form ref="formRef" :model="data.form" :rules="data.rules" label-position="top" class="register-form">
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="data.form.username" :prefix-icon="User" placeholder="登录账号" size="large" />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="data.form.name" placeholder="用于审核展示" size="large" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="data.form.password" :prefix-icon="Lock" placeholder="至少 6 位" show-password size="large" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="data.form.confirmPassword" :prefix-icon="Lock" placeholder="再次输入密码" show-password size="large" />
            </el-form-item>
            <el-form-item label="人口普查序号" prop="studentNo">
              <el-input v-model="data.form.studentNo" placeholder="请输入人口普查表中的序号" size="large" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="data.form.email" :prefix-icon="Message" placeholder="用于接收通知" size="large" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="data.form.phone" placeholder="选填" size="large" />
            </el-form-item>
            <el-form-item label="邀请码" prop="invitationCode">
              <el-input v-model="data.form.invitationCode" :prefix-icon="Key" placeholder="没有可留空" size="large" />
            </el-form-item>
          </div>

          <el-form-item label="报考方向" prop="branch">
            <el-radio-group v-model="data.form.branch" class="branch-group">
              <el-radio-button label="生电" value="生电" />
              <el-radio-button label="建筑" value="建筑" />
              <el-radio-button label="交流" value="交流" />
              <el-radio-button label="其他" value="其他" />
            </el-radio-group>
          </el-form-item>

          <el-button :loading="data.loading" class="register-submit" size="large" type="primary" @click="submitRequest">
            {{ data.form.invitationCode ? "立即注册" : "提交审批申请" }}
          </el-button>

          <div class="register-footer">
            <span>已有账号？</span>
            <button type="button" @click="router.push('/login')">去登录</button>
          </div>
        </el-form>
      </main>
    </section>

    <el-result
      v-if="data.showResult"
      class="result-overlay"
      :icon="data.resultType"
      :title="data.resultTitle"
      :sub-title="data.resultSub"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/login')">前往登录页</el-button>
        <el-button @click="router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import { Key, Lock, Message, User } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"
import request from "@/utils/request.js"
import router from "@/router/index.js"

const formRef = ref()

const data = reactive({
  loading: false,
  showResult: false,
  resultType: "success",
  resultTitle: "",
  resultSub: "",
  form: { branch: "其他" },
  rules: {
    username: [
      { required: true, message: "请输入用户名", trigger: "blur" },
      { min: 3, max: 20, message: "用户名长度为 3-20 个字符", trigger: "blur" },
    ],
    password: [
      { required: true, message: "请输入密码", trigger: "blur" },
      { min: 6, message: "密码长度不能少于 6 位", trigger: "blur" },
    ],
    confirmPassword: [
      {
        validator: (rule, value, callback) => {
          if (!value) callback(new Error("请确认密码"))
          else if (value !== data.form.password) callback(new Error("两次密码不一致"))
          else callback()
        },
        trigger: "blur",
      },
    ],
    name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
    studentNo: [{ required: true, message: "请输入编号", trigger: "blur" }],
    branch: [{ required: true, message: "请选择报考方向", trigger: "change" }],
    email: [
      {
        validator: (rule, value, callback) => {
          const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
          if (!value) callback(new Error("请输入邮箱"))
          else if (!emailRegex.test(value)) callback(new Error("请输入有效的邮箱地址"))
          else callback()
        },
        trigger: "blur",
      },
    ],
  },
})

const showResult = (title, sub, type = "success") => {
  data.resultTitle = title
  data.resultSub = sub
  data.resultType = type
  data.showResult = true
}

const submitRequest = () => {
  formRef.value.validate(valid => {
    if (!valid) return

    data.loading = true
    const requestData = {
      username: data.form.username,
      password: data.form.password,
      name: data.form.name,
      studentNo: data.form.studentNo,
      email: data.form.email,
      phone: data.form.phone || null,
      branch: data.form.branch || "其他",
      approvalToken: data.form.invitationCode || null,
    }

    request.post("/registration/register", requestData).then(res => {
      data.loading = false
      if (res.code !== "200") {
        ElMessage.error(res.msg || "提交失败，请稍后重试")
        return
      }
      if (data.form.invitationCode) {
        showResult("注册成功", "你的邀请码已通过校验，现在可以登录系统。")
      } else {
        showResult("申请已提交", "管理员审批通过后即可登录，请留意通知。", "info")
      }
    }).catch(() => {
      data.loading = false
      ElMessage.error("网络错误，请稍后再试")
    })
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background-image:
    linear-gradient(115deg, rgba(7, 16, 28, 0.86), rgba(7, 16, 28, 0.36)),
    url("@/assets/imgs/review/review-scenes.jpg");
  background-size: auto, 400% 200%;
  background-position: center, 33.333% 0%;
}

.register-shell {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: 360px 1fr;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 10px;
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.34);
}

.register-guide {
  padding: 38px;
  display: flex;
  flex-direction: column;
  background: rgba(9, 17, 28, 0.62);
  color: #fff;
  backdrop-filter: blur(6px);
}

.guide-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  align-self: flex-start;
  border: 0;
  background: transparent;
  color: #fff;
  font: inherit;
  font-weight: 900;
  cursor: pointer;
}

.guide-brand img {
  width: 38px;
  height: 38px;
  border-radius: 9px;
}

.register-guide h1 {
  margin: auto 0 16px;
  color: #fff;
  font-size: 34px;
  line-height: 1.2;
}

.register-guide p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.8;
}

.guide-steps {
  display: grid;
  gap: 10px;
  margin-top: 28px;
}

.guide-steps span {
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.register-panel {
  padding: 36px 40px;
  background: var(--bg-card);
}

.form-heading span {
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 900;
}

.form-heading h2 {
  margin: 8px 0 24px;
  color: var(--text-primary);
  font-size: 28px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
}

.branch-group {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.branch-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.register-submit {
  width: 100%;
  margin-top: 8px;
}

.register-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.register-footer button {
  border: 0;
  background: transparent;
  color: var(--primary-color);
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.result-overlay {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.94);
}

[data-theme="dark"] .result-overlay {
  background: rgba(17, 24, 39, 0.94);
}

@media (max-width: 900px) {
  .register-shell {
    grid-template-columns: 1fr;
  }

  .register-guide {
    display: none;
  }
}

@media (max-width: 620px) {
  .register-page {
    padding: 14px;
  }

  .register-panel {
    padding: 28px 18px;
  }

  .form-grid,
  .branch-group {
    grid-template-columns: 1fr;
  }
}
</style>
