<template>
  <div class="pwd-page">
    <div class="page-hero-sm">
      <div class="hero-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="#ffffff" stroke-width="2"/>
          <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <div>
        <h2>修改密码</h2>
        <p>定期更换密码，保障账号安全</p>
      </div>
    </div>

    <div class="pwd-card">
      <div class="pwd-icon-wrap">
        <div class="pwd-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
      </div>

      <el-form ref="formRef" :rules="data.rules" :model="data.user" label-width="100px" class="pwd-form">
        <el-form-item label="原密码" prop="password">
          <el-input v-model="data.user.password" placeholder="请输入原密码" show-password size="large" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="data.user.newPassword" placeholder="请输入新密码" show-password size="large" />
          <div class="field-hint">密码长度 6-20 位</div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="data.user.confirmPassword" placeholder="请再次输入新密码" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="updatePassword" class="save-btn">
            <el-icon><Key /></el-icon> 保存并重新登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import { Key } from "@element-plus/icons-vue";

const formRef = ref()
const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== data.user.newPassword) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  rules: {
    password: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
    ],
    confirmPassword: [{ validator: validatePass, trigger: 'blur' }]
  }
})

const updatePassword = () => {
  formRef.value.validate(valid => {
    if (valid) {
      request.put('/updatePassword', data.user).then(res => {
        if (res.code === '200') {
          ElMessage.success('密码修改成功，即将跳转到登录页')
          setTimeout(() => {
            localStorage.removeItem('xm-user')
            router.push('/login')
          }, 1500)
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      })
    }
  })
}
</script>

<style scoped>
.pwd-page { padding: 24px; max-width: 600px; margin: 0 auto; }

.page-hero-sm { background: linear-gradient(135deg, #1e3a5f 0%, #1d4ed8 50%, #3b82f6 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 24px; display: flex; align-items: center; gap: 16px; box-shadow: 0 8px 32px rgba(29, 78, 216, 0.2); }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero-sm h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; color: #fff; }
.page-hero-sm p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.7); }

.pwd-card { background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.06); border: 1px solid var(--el-border-color-lighter); }
.pwd-icon-wrap { background: linear-gradient(135deg, #eff6ff, #dbeafe); padding: 24px; text-align: center; border-bottom: 1px solid var(--el-border-color-lighter); }
.pwd-icon { width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg, #1d4ed8, #3b82f6); color: #fff; display: flex; align-items: center; justify-content: center; margin: 0 auto; box-shadow: 0 4px 16px rgba(29, 78, 216, 0.3); }
.pwd-form { padding: 28px 32px 24px; }
.field-hint { font-size: 12px; color: #9ca3af; margin-top: 4px; }
.save-btn { width: 100%; border-radius: 10px; font-weight: 600; height: 44px; font-size: 15px; }
</style>
