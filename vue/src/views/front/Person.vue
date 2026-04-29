<template>
  <div class="main-content">
    <div class="person-layout">
      <!-- 左侧：头像 + 基本信息 -->
      <div class="person-sidebar">
        <el-upload
          :action="baseUrl + '/files/upload'"
          :headers="uploadHeaders"
          :on-success="handleFileUpload"
          :on-error="handleFileError"
          :show-file-list="false"
          class="avatar-uploader"
        >
          <div class="avatar-wrapper">
            <img v-if="data.user.avatar" :src="data.user.avatar" />
            <el-icon v-else style="font-size: 40px; color: #ccc;"><Plus /></el-icon>
          </div>
          <div style="font-size: 12px; color: var(--text-secondary); margin-top: 8px;">点击更换头像</div>
        </el-upload>

        <div class="username">{{ data.user.name || '未设置' }}</div>
        <div class="role-tag">{{ getRoleLabel(data.user.role) }}</div>

        <div class="person-stats">
          <div class="person-stat-item">
            <div class="stat-val">{{ data.stats.completed }}</div>
            <div class="stat-lbl">已参加审核</div>
          </div>
          <div class="person-stat-item">
            <div class="stat-val">{{ data.stats.avg }}</div>
            <div class="stat-lbl">平均分</div>
          </div>
          <div class="person-stat-item">
            <div class="stat-val">{{ data.stats.passRate }}%</div>
            <div class="stat-lbl">通过率</div>
          </div>
          <div class="person-stat-item">
            <div class="stat-val">{{ data.stats.highest }}</div>
            <div class="stat-lbl">最高分</div>
          </div>
        </div>
      </div>

      <!-- 右侧：编辑表单 -->
      <div class="person-form">
        <div class="section-header">
          <div class="section-title">个人资料</div>
        </div>
        <el-form
          ref="formRef"
          :model="data.user"
          :rules="rules"
          label-width="80px"
          style="max-width: 500px; padding-top: 10px;"
        >
          <el-form-item prop="username" label="用户名">
            <el-input disabled v-model="data.user.username" placeholder="用户名不可修改">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="name" label="姓名">
            <el-input v-model="data.user.name" placeholder="请输入真实姓名">
              <template #prefix>
                <el-icon><Edit /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="phone" label="电话">
            <el-input
              v-model="data.user.phone"
              placeholder="请输入手机号码"
              maxlength="11"
              @input="data.user.phone = data.user.phone.replace(/\D/g, '')"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="email" label="邮箱">
            <el-input v-model="data.user.email" placeholder="请输入邮箱地址">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码修改 -->
          <el-divider content-position="left">
            <span style="font-size: 13px; color: var(--text-secondary);">安全管理</span>
          </el-divider>
          <el-form-item prop="password" label="新密码">
            <el-input
              v-model="data.newPassword"
              type="password"
              placeholder="留空则不修改密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword" label="确认密码" v-if="data.newPassword">
            <el-input
              v-model="data.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="update"
              round
              :loading="data.saving"
              :disabled="data.saving"
            >
              <el-icon v-if="!data.saving"><Check /></el-icon>
              {{ data.saving ? '保存中...' : '保存修改' }}
            </el-button>
            <el-button @click="resetForm" :disabled="data.saving" round>
              <el-icon><RefreshLeft /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { getUploadHeaders } from '@/utils/upload.js';

const baseUrl = import.meta.env.VITE_BASE_URL
const uploadHeaders = getUploadHeaders()

const data = reactive({
  user: JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}'),
  stats: { completed: 0, avg: 0, passRate: 0, highest: 0 },
  saving: false,
  newPassword: '',
  confirmPassword: ''
})

const formRef = ref(null)

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入有效的手机号码'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

const validateConfirm = (rule, value, callback) => {
  if (!data.newPassword) {
    callback()
    return
  }
  if (value !== data.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度 2-20 个字符', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const getRoleLabel = (role) => {
  const map = { OWNER: '所有者', ADMIN: '管理员', HELPER: '阅卷人', USER: '玩家' }
  return map[role] || '玩家'
}

const handleFileUpload = (res) => {
  data.user.avatar = res.data
  ElMessage.success('头像上传成功')
  localStorage.setItem('beiming-onlineexam-user', JSON.stringify(data.user))
}

const handleFileError = () => {
  ElMessage.error('头像上传失败，请重试')
}

const emit = defineEmits(['updateUser'])

const update = () => {
  formRef.validate((valid) => {
    if (!valid) return
    data.saving = true

    const payload = { ...data.user }
    if (data.newPassword) {
      payload.password = data.newPassword
    }

    const api = data.user.role === 'USER'
      ? '/student/update'
      : data.user.role === 'HELPER'
        ? '/examiner/update'
        : '/admin/update'

    request.put(api, payload).then(res => {
      if (res.code === '200') {
        ElMessage.success('保存成功')
        localStorage.setItem('beiming-onlineexam-user', JSON.stringify(data.user))
        emit('updateUser')
        data.newPassword = ''
        data.confirmPassword = ''
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    }).finally(() => {
      data.saving = false
    })
  })
}

const resetForm = () => {
  data.user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
  data.newPassword = ''
  data.confirmPassword = ''
  formRef.resetFields()
}

const loadStats = () => {
  if (data.user.role === 'USER' || data.user.role === 'OWNER' || data.user.role === 'ADMIN') {
    request.get('/score/getByStudentId/' + data.user.id).then(res => {
      if (res.code === '200') {
        const scores = res.data || []
        data.stats.completed = scores.length
        if (scores.length) {
          const allScores = scores.map(s => s.totalScore || 0)
          data.stats.highest = Math.max(...allScores)
          data.stats.avg = Math.round(allScores.reduce((a, b) => a + b, 0) / scores.length)
          const passed = scores.filter(s => s.isPass).length
          data.stats.passRate = Math.round((passed / scores.length) * 100)
        }
      }
    })
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.avatar-uploader :deep(.el-upload) {
  border-radius: 50%;
}

.person-layout {
  gap: 28px;
}

.person-sidebar {
  position: relative;
  overflow: hidden;
}

.person-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.person-sidebar .avatar-wrapper {
  position: relative;
  z-index: 1;
  width: 90px;
  height: 90px;
  margin: 20px auto 12px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #00b42a;
  box-shadow: 0 0 0 4px var(--primary-light), 0 4px 16px rgba(0,0,0,0.1);
}

.person-sidebar .avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.person-sidebar .username {
  position: relative;
  z-index: 1;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
}

.person-sidebar .role-tag {
  position: relative;
  z-index: 1;
  background: var(--primary-color);
  color: #fff;
  padding: 3px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 20px;
}

.person-sidebar .person-stats {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-lighter);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.person-sidebar .person-stat-item {
  text-align: center;
  padding: 8px;
  border-radius: 8px;
  background: var(--bg-page);
}

.person-sidebar .person-stat-item .stat-val {
  font-size: 22px;
  font-weight: 800;
  color: var(--primary-color);
  line-height: 1.2;
}

.person-sidebar .person-stat-item .stat-lbl {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.person-form {
  position: relative;
}

.person-form .section-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.person-form .section-title::before {
  background: var(--primary-color);
}
</style>
