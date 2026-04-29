<template>
  <div class="admin-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <circle cx="9" cy="7" r="4" stroke="#ffffff" stroke-width="2"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>管理员信息</h1>
          <p>管理系统管理员账号，支持新增、编辑与权限配置</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增管理员
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">管理员总数</div></div></div>
      <div class="stat-card stat-admin"><div class="stat-inner"><div class="stat-num">{{ data.tableData.length }}</div><div class="stat-label">当前页账号</div></div></div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.name" placeholder="搜索姓名或账号..." clearable @keyup.enter="load" @clear="load" class="search-input" />
        </div>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 位管理员</span>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="data.tableData" stripe v-loading="data.loading" row-key="id"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        @selection-change="handleSelectionChange" class="admin-table">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="adminNo" label="编号" width="90" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="scope">
            <el-avatar v-if="scope.row.avatar" :src="scope.row.avatar" :size="40" />
            <el-avatar v-else :size="40" style="background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; font-weight: 700;">
              {{ (scope.row.name || scope.row.username || '?')[0].toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="level" label="权限" width="110">
          <template #default="scope">
            <span class="level-badge" :class="'level-' + scope.row.level">{{ levelLabel(scope.row.level) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-popconfirm title="确定删除该管理员？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small"><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap" v-if="data.total">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="data.form.id ? '编辑管理员' : '新增管理员'" v-model="data.formVisible" width="500px" class="admin-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 8px 0">
        <el-form-item label="账号">
          <el-input v-model="data.form.username" placeholder="请输入账号" :disabled="!!data.form.id" maxlength="30" />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="data.form.adminNo" placeholder="请输入管理员编号" maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="data.form.name" placeholder="请输入姓名" maxlength="30" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="data.form.phone" placeholder="请输入电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="data.form.email" placeholder="请输入邮箱" maxlength="60" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-row">
            <el-upload :action="baseUrl + '/files/upload'" :headers="uploadHeaders" :on-success="handleFileUpload" :show-file-list="false" accept="image/*">
              <img v-if="data.form.avatar" :src="data.form.avatar" class="form-avatar-preview" />
              <el-button v-else type="primary" plain>
                <el-icon><Upload /></el-icon> 上传头像
              </el-button>
            </el-upload>
            <el-button v-if="data.form.avatar" size="small" type="danger" text @click="data.form.avatar = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="权限身份">
          <el-select v-model="data.form.level" placeholder="请选择权限身份" style="width: 100%">
            <el-option v-if="currentUserLevel >= 4" label="高级管理员（Lv.4）" :value="4" />
            <el-option label="管理员（Lv.3）" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Upload } from "@element-plus/icons-vue";
import { getUploadHeaders } from '@/utils/upload.js'

const baseUrl = import.meta.env.VITE_BASE_URL
const uploadHeaders = getUploadHeaders()
const currentUser = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
const currentUserLevel = currentUser.level || 1

const data = reactive({
  loading: false,
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1, pageSize: 10, total: 0,
  name: null,
  ids: []
})

const levelLabel = (level) => {
  if (level >= 4) return '高级管理员'
  return '管理员'
}

const load = () => {
  data.loading = true
  request.get('/admin/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, name: data.name || undefined } })
    .then(res => { data.loading = false; if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total || 0 } })
    .catch(() => { data.loading = false })
}

const handleAdd = () => { data.form = { level: 3 }; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); data.formVisible = true }
const save = () => { data.form.id ? update() : add() }

const add = () => {
  request.post('/admin/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('新增成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  })
}

const update = () => {
  request.put('/admin/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  })
}

const del = (id) => {
  request.delete('/admin/delete/' + id).then(res => {
    if (res.code === '200') { ElMessage.success('删除成功'); load() }
    else { ElMessage.error(res.msg || '删除失败') }
  })
}

const delBatch = () => {
  if (!data.ids.length) return
  ElMessageBox.confirm('确定批量删除选中的管理员吗？', '批量删除', { type: 'warning' })
    .then(() => request.delete('/admin/delete/batch', { data: data.ids }).then(res => {
      if (res.code === '200') { ElMessage.success('批量删除成功'); load() }
      else { ElMessage.error(res.msg || '批量删除失败') }
    }))
    .catch(() => {})
}

const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const handleFileUpload = (res) => { if (res.code === '200') data.form.avatar = res.data; else ElMessage.error('上传失败') }
const reset = () => { data.name = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }

onMounted(() => { load() })
</script>

<style scoped>
.admin-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 50%, #60a5fa 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(37, 99, 235, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-bottom: 20px; max-width: 420px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 22px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #1e3a5f, #2563eb); }
.stat-admin .stat-inner { background: linear-gradient(135deg, #d97706, #f59e0b); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.reset-btn { border-radius: 10px; }
.batch-del { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #2563eb; }

/* ===== 表格 ===== */
.table-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.admin-table :deep(.el-table__row:hover > td) { background: #eff6ff !important; }
.level-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 700; }
.level-4, .level-\ 4 { background: #fee2e2; color: #dc2626; }
.level-3 { background: #fef3c7; color: #d97706; }
.level-2 { background: #f1f5f9; color: #6b7280; }
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 头像上传 */
.avatar-upload-row { display: flex; align-items: center; gap: 12px; }
.form-avatar-preview { width: 72px; height: 72px; border-radius: 12px; object-fit: cover; border: 2px solid #e4e7ed; }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
