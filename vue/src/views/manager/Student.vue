<template>
  <div class="player-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <circle cx="9" cy="7" r="4" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <path d="M21 21v-2a4 4 0 0 0-3-3.87" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>玩家管理</h1>
          <p>统一管理所有玩家信息，支持身份调整与批量操作</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增玩家
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-grid">
      <div class="stat-card stat-all">
        <div class="stat-inner">
          <div class="stat-num">{{ data.allUsers.length }}</div>
          <div class="stat-label">成员总数</div>
          <div class="stat-icon"><el-icon color="rgba(255,255,255,0.4)"><User /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-owner">
        <div class="stat-inner">
          <div class="stat-num">{{ countByRole('OWNER') }}</div>
          <div class="stat-label">站长</div>
          <div class="stat-icon"><el-icon color="rgba(255,255,255,0.4)"><Star /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-admin">
        <div class="stat-inner">
          <div class="stat-num">{{ countByRole('ADMIN') }}</div>
          <div class="stat-label">管理员</div>
          <div class="stat-icon"><el-icon color="rgba(255,255,255,0.4)"><Tools /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-helper">
        <div class="stat-inner">
          <div class="stat-num">{{ countByRole('HELPER') }}</div>
          <div class="stat-label">阅卷人</div>
          <div class="stat-icon"><el-icon color="rgba(255,255,255,0.4)"><Reading /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-user">
        <div class="stat-inner">
          <div class="stat-num">{{ countByRole('USER') }}</div>
          <div class="stat-label">玩家</div>
          <div class="stat-icon"><el-icon color="rgba(255,255,255,0.4)"><Avatar /></el-icon></div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input
            v-model="data.name"
            placeholder="搜索姓名或账号..."
            clearable
            @keyup.enter="load"
            @clear="load"
            class="search-input"
          />
        </div>
        <el-select v-model="data.roleFilter" placeholder="筛选身份" clearable @change="load" class="filter-sel">
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="全部身份" value="" />
          <el-option label="站长" value="OWNER" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="阅卷人" value="HELPER" />
          <el-option label="玩家" value="USER" />
        </el-select>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del-btn">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="pagedData"
        stripe
        v-loading="data.loading"
        class="user-table"
        row-key="id"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userNo" label="编号" width="80" sortable />
        <el-table-column prop="avatar" label="头像" width="72">
          <template #default="scope">
            <div class="table-avatar">
              <el-image
                v-if="scope.row.avatar"
                :src="scope.row.avatar"
                :preview-src-list="[scope.row.avatar]"
                preview-teleported
                fit="cover"
                class="avatar-img"
              />
              <span v-else class="avatar-init">{{ (scope.row.name || 'U').charAt(0).toUpperCase() }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="source" label="身份" width="120">
          <template #default="scope">
            <div class="role-badge" :class="'role-' + scope.row.source.toLowerCase()">
              <el-icon>
                <Star v-if="scope.row.source === 'OWNER'" />
                <Tools v-else-if="scope.row.source === 'ADMIN'" />
                <Reading v-else-if="scope.row.source === 'HELPER'" />
                <User v-else />
              </el-icon>
              {{ roleLabel(scope.row.source) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="branch" label="分支" width="110">
          <template #default="scope">
            <span class="branch-tag" v-if="scope.row.branch">{{ scope.row.branch }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="分组" width="110">
          <template #default="scope">
            <span>{{ scope.row.className || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130">
          <template #default="scope">
            <span class="phone-text">{{ scope.row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="120">
          <template #default="scope">
            <span class="status-badge" :class="'st-' + (scope.row.status || 'approved').toLowerCase()">
              {{ statusLabel(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-popconfirm
              title="确定删除该玩家？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="del(scope.row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="data.pageNum"
          :page-size="data.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="filteredData.length"
        />
      </div>
    </div>

    <!-- 玩家信息弹窗 -->
    <el-dialog
      v-model="data.formVisible"
      :title="data.form.id ? '编辑玩家' : '新增玩家'"
      width="540px"
      class="user-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <div class="form-section">
          <div class="form-section-title">基本信息</div>
          <div class="form-grid">
            <div class="form-item">
              <label>用户名 <span class="required">*</span></label>
              <el-input v-model="data.form.username" placeholder="请输入用户名" />
            </div>
            <div class="form-item">
              <label>编号</label>
              <el-input v-model="data.form.userNo" placeholder="请输入编号" />
            </div>
          </div>
          <div class="form-item full">
            <label>头像</label>
            <div class="avatar-upload">
              <div class="preview-avatar">
                <el-image v-if="data.form.avatar" :src="data.form.avatar" fit="cover" />
                <el-icon v-else><Plus /></el-icon>
              </div>
              <el-upload
                :action="baseUrl + '/files/upload'"
                :headers="uploadHeaders"
                :on-success="handleFileUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <el-button size="small" type="primary">上传头像</el-button>
              </el-upload>
            </div>
          </div>
          <div class="form-grid">
            <div class="form-item">
              <label>姓名 <span class="required">*</span></label>
              <el-input v-model="data.form.name" placeholder="请输入姓名" />
            </div>
            <div class="form-item">
              <label>身份</label>
              <el-select v-model="data.form.source" placeholder="选择身份" style="width:100%">
                <el-option v-if="currentUserLevel >= 4" label="站长 (Owner)" value="OWNER" />
                <el-option v-if="currentUserLevel >= 4" label="管理员 (Admin)" value="ADMIN" />
                <el-option v-if="currentUserLevel >= 3" label="阅卷人 (Helper)" value="HELPER" />
                <el-option label="玩家 (User)" value="USER" />
              </el-select>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="form-section">
          <div class="form-section-title">详细信息</div>
          <div class="form-grid">
            <div class="form-item">
              <label>分支</label>
              <el-select v-model="data.form.branch" placeholder="选择分支" style="width:100%" clearable>
                <el-option label="后勤见习" value="后勤见习" />
                <el-option label="红石见习" value="红石见习" />
                <el-option label="建筑" value="建筑" />
              </el-select>
            </div>
            <div class="form-item">
              <label>分组</label>
              <el-input v-model="data.form.className" placeholder="请输入分组" />
            </div>
          </div>
          <div class="form-grid">
            <div class="form-item">
              <label>电话</label>
              <el-input v-model="data.form.phone" placeholder="请输入电话" />
            </div>
            <div class="form-item">
              <label>账号状态</label>
              <el-select v-model="data.form.status" placeholder="选择状态" style="width:100%">
                <el-option label="审核通过" value="APPROVED" />
                <el-option label="审核中" value="PENDING" />
                <el-option label="审核未通过" value="REJECTED" />
              </el-select>
            </div>
          </div>
          <div class="form-item full">
            <label>邮箱</label>
            <el-input v-model="data.form.email" placeholder="请输入邮箱" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Search, Filter, Plus, Star, Tools, Reading, Avatar, User } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { getUploadHeaders } from '@/utils/upload.js'

const baseUrl = import.meta.env.VITE_BASE_URL
const uploadHeaders = getUploadHeaders()
const currentUser = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
const currentUserLevel = currentUser.level || 1

const data = reactive({
  loading: false,
  formVisible: false,
  form: {},
  allUsers: [],
  pageNum: 1,
  pageSize: 15,
  name: null,
  roleFilter: null,
  ids: []
})

const filteredData = computed(() => {
  let list = data.allUsers
  if (data.name) {
    const kw = data.name.toLowerCase()
    list = list.filter(u =>
      (u.name && u.name.toLowerCase().includes(kw)) ||
      (u.username && u.username.toLowerCase().includes(kw))
    )
  }
  if (data.roleFilter) list = list.filter(u => data.roleFilter === u.source)
  return list
})

const pagedData = computed(() => {
  const start = (data.pageNum - 1) * data.pageSize
  return filteredData.value.slice(start, start + data.pageSize)
})

const countByRole = (role) => data.allUsers.filter(u => u.source === role).length

const roleLabel = (src) => {
  if (src === 'OWNER') return '站长'
  if (src === 'ADMIN') return '管理员'
  if (src === 'HELPER') return '阅卷人'
  return '玩家'
}

const statusLabel = (s) => {
  if (s === 'APPROVED') return '审核通过'
  if (s === 'PENDING') return '审核中'
  if (s === 'REJECTED') return '审核未通过'
  return s || '—'
}

const load = () => {
  data.loading = true
  request.get('/admin/selectAllUnified', {
    params: { name: data.name || undefined, role: data.roleFilter || undefined }
  }).then(res => {
    data.loading = false
    if (res.code === '200') { data.allUsers = res.data || [] }
    else ElMessage.error(res.msg || '加载失败')
  }).catch(() => { data.loading = false })
}

const handleAdd = () => {
  data.form = { status: 'APPROVED', source: 'USER' }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.form._originalSource = row.source
  data.formVisible = true
}

const save = () => {
  const form = data.form
  const originalSource = form._originalSource
  const newSource = form.source
  const tableOf = (s) => (s === 'OWNER' || s === 'ADMIN') ? 'ADMIN' : s
  const needsTransfer = form.id && originalSource && tableOf(originalSource) !== tableOf(newSource)

  if (needsTransfer) {
    const transferForm = {
      id: form.id, _originalSource: tableOf(originalSource), source: newSource,
      userNo: form.userNo, name: form.name, avatar: form.avatar,
      phone: form.phone, email: form.email, branch: form.branch,
      className: form.className, status: form.status
    }
    request.post('/admin/transferUser', transferForm).then(res => {
      if (res.code === '200') { ElMessage.success('身份转移成功'); data.formVisible = false; load() }
      else ElMessage.error(res.msg || '转移失败')
    })
    return
  }

  const source = tableOf(originalSource || newSource)
  if (source === 'ADMIN') {
    const isOwner = newSource === 'OWNER'
    const payload = { ...form, adminNo: form.userNo, level: isOwner ? 4 : (form.level || 3) }
    const action = form.id ? request.put('/admin/update', payload) : request.post('/admin/add', payload)
    action.then(res => {
      if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() }
      else ElMessage.error(res.msg || '操作失败')
    })
  } else if (source === 'HELPER') {
    const payload = { ...form, examinerNo: form.userNo }
    const action = form.id ? request.put('/examiner/update', payload) : request.post('/examiner/add', payload)
    action.then(res => {
      if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() }
      else ElMessage.error(res.msg || '操作失败')
    })
  } else {
    const payload = { ...form, studentNo: form.userNo }
    const action = form.id ? request.put('/student/update', payload) : request.post('/student/add', payload)
    action.then(res => {
      if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() }
      else ElMessage.error(res.msg || '操作失败')
    })
  }
}

const del = (row) => {
  const endpoint = (row.source === 'OWNER' || row.source === 'ADMIN') ? '/admin/delete/' :
    row.source === 'HELPER' ? '/examiner/delete/' : '/student/delete/'
  request.delete(endpoint + row.id).then(res => {
    if (res.code === '200') { ElMessage.success('删除成功'); load() }
    else ElMessage.error(res.msg || '删除失败')
  })
}

const delBatch = () => {
  if (!data.ids.length) return
  ElMessageBox.confirm(`确定删除选中的 ${data.ids.length} 个成员吗？`, '批量删除确认', {
    confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    const grouped = {}
    data.ids.forEach(key => {
      const [src, id] = key.split(':')
      if (!grouped[src]) grouped[src] = []
      grouped[src].push(parseInt(id))
    })
    const promises = []
    if (grouped.OWNER || grouped.ADMIN) promises.push(request.delete('/admin/delete/batch', { data: [...(grouped.OWNER || []), ...(grouped.ADMIN || [])] }))
    if (grouped.HELPER) promises.push(request.delete('/examiner/delete/batch', { data: grouped.HELPER }))
    if (grouped.USER) promises.push(request.delete('/student/delete/batch', { data: grouped.USER }))
    Promise.all(promises).then(() => { ElMessage.success('批量删除成功'); load() })
  }).catch(() => {})
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.source + ':' + v.id)
}

const handleFileUpload = (res) => { if (res.code === '200') data.form.avatar = res.data }

const reset = () => { data.name = null; data.roleFilter = null; data.pageNum = 1; load() }

const handleSizeChange = (size) => { data.pageSize = size }
const handleCurrentChange = (page) => { data.pageNum = page }

onMounted(() => { load() })
</script>

<style scoped>
.player-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 16px; padding: 28px 36px; margin-bottom: 20px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.3);
}
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon {
  width: 60px; height: 60px; background: rgba(255,255,255,0.15);
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2);
}
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) {
  background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important;
  color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px;
}
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s, box-shadow 0.3s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px rgba(0,0,0,0.1); }
.stat-inner { padding: 20px 22px; color: #fff; position: relative; overflow: hidden; }
.stat-all .stat-inner { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-owner .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-admin .stat-inner { background: linear-gradient(135deg, #d97706, #f59e0b); }
.stat-helper .stat-inner { background: linear-gradient(135deg, #059669, #10b981); }
.stat-user .stat-inner { background: linear-gradient(135deg, #64748b, #94a3b8); }
.stat-num { font-size: 34px; font-weight: 800; line-height: 1; margin-bottom: 4px; letter-spacing: -1px; }
.stat-label { font-size: 12px; font-weight: 600; opacity: 0.85; }
.stat-icon { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); font-size: 28px; opacity: 0.2; }

/* ===== 工具栏 ===== */
.toolbar {
  background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter);
}
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 260px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 160px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; font-weight: 500; }
.batch-del-btn { border-radius: 10px; }

/* ===== 数据表格 ===== */
.table-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter);
}
.user-table :deep(.el-table__row:hover > td) { background: #f8f7ff !important; }
.table-avatar { width: 40px; height: 40px; border-radius: 50%; overflow: hidden; background: linear-gradient(135deg, #4f46e5, #7c3aed); display: flex; align-items: center; justify-content: center; }
.avatar-img { border-radius: 50%; }
.avatar-init { color: #fff; font-size: 15px; font-weight: 700; }
.role-badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.role-owner { background: #fee2e2; color: #dc2626; }
.role-admin { background: #fef3c7; color: #d97706; }
.role-helper { background: #dcfce7; color: #16a34a; }
.role-user { background: #f1f5f9; color: #64748b; }
.branch-tag { display: inline-block; padding: 2px 8px; background: #f1f5f9; color: #475569; border-radius: 6px; font-size: 12px; }
.text-muted { color: #d1d5db; }
.phone-text { font-family: 'SF Mono', monospace; font-size: 12px; }
.status-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.st-approved { background: #dcfce7; color: #16a34a; }
.st-pending { background: #fef3c7; color: #d97706; }
.st-rejected { background: #fee2e2; color: #dc2626; }
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* ===== 弹窗 ===== */
.dialog-body { padding: 4px 0; }
.form-section { margin-bottom: 4px; }
.form-section-title { font-size: 13px; font-weight: 700; color: #6b7280; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 14px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px; }
.form-item.full { margin-bottom: 14px; }
.form-item label { display: block; font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 6px; }
.required { color: #ef4444; }
.avatar-upload { display: flex; align-items: center; gap: 14px; }
.preview-avatar { width: 60px; height: 60px; border-radius: 12px; overflow: hidden; background: #f1f5f9; border: 2px dashed #e5e7eb; display: flex; align-items: center; justify-content: center; color: #9ca3af; font-size: 20px; }
.preview-avatar .el-image { width: 100%; height: 100%; }

@media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .page-hero { flex-direction: column; align-items: flex-start; gap: 16px; }
  .toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
}
</style>
