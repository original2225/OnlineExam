<template>
  <div class="user-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>阅卷人管理</h1>
          <p>管理所有阅卷工作人员，支持提拔为管理员与批量操作</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增阅卷人
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total">
        <div class="stat-inner">
          <div class="stat-num">{{ data.total }}</div>
          <div class="stat-label">阅卷人总数</div>
          <div class="stat-icon"><el-icon><Reading /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-online">
        <div class="stat-inner">
          <div class="stat-num">{{ data.gradingStats.gradedCount || 0 }}</div>
          <div class="stat-label">已批改卷数</div>
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-graded">
        <div class="stat-inner">
          <div class="stat-num">{{ data.gradingStats.gradedThisMonth || 0 }}</div>
          <div class="stat-label">本月已阅卷</div>
          <div class="stat-icon"><el-icon><EditPen /></el-icon></div>
        </div>
      </div>
      <div class="stat-card stat-pending">
        <div class="stat-inner">
          <div class="stat-num">{{ data.gradingStats.pendingCount || 0 }}</div>
          <div class="stat-label">待批改</div>
          <div class="stat-icon"><el-icon><Bell /></el-icon></div>
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
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del-btn">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
      </div>
    </div>

    <!-- 卡片网格视图 -->
    <div class="examiner-grid" v-loading="data.loading">
      <transition-group name="card-anim">
        <div v-for="row in pagedData" :key="row.id" class="examiner-card">
          <!-- 卡片顶部 -->
          <div class="card-top">
            <div class="card-avatar">
              <el-image v-if="row.avatar" :src="row.avatar" fit="cover" class="avatar-img" :preview-src-list="[row.avatar]" preview-teleported />
              <span v-else class="avatar-init">{{ (row.name || 'E').charAt(0).toUpperCase() }}</span>
              <div class="online-dot"></div>
            </div>
            <div class="card-info">
              <div class="card-name">{{ row.name || '—' }}</div>
              <div class="card-role">
                <span class="role-tag">阅卷人</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button type="primary" circle :icon="Edit" size="small" @click="handleEdit(row)" />
              <el-popconfirm title="确定删除？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(row.id)">
                <template #reference>
                  <el-button type="danger" circle :icon="Delete" size="small" />
                </template>
              </el-popconfirm>
            </div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-body">
            <div class="info-row">
              <el-icon><User /></el-icon>
              <span>{{ row.username }}</span>
            </div>
            <div class="info-row" v-if="row.examinerNo">
              <el-icon><Postcard /></el-icon>
              <span>{{ row.examinerNo }}</span>
            </div>
            <div class="info-row" v-if="row.phone">
              <el-icon><Phone /></el-icon>
              <span>{{ row.phone }}</span>
            </div>
            <div class="info-row" v-if="row.email">
              <el-icon><Message /></el-icon>
              <span class="email-text">{{ row.email }}</span>
            </div>
          </div>

          <!-- 卡片底部操作 -->
          <div class="card-footer" v-if="userLevel >= 4">
            <el-button size="small" class="promote-btn" @click="promoteToAdmin(row)">
              <el-icon><Top /></el-icon> 提拔为管理员
            </el-button>
          </div>
        </div>
      </transition-group>

      <!-- 空状态 -->
      <div v-if="!data.loading && pagedData.length === 0" class="empty-state">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" fill="#f1f5f9"/>
          <path d="M9 12h6" stroke="#94a3b8" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <p>暂无阅卷人记录</p>
        <el-button type="primary" size="small" @click="handleAdd">新增阅卷人</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="data.total > 0">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="data.pageNum"
        :page-size="data.pageSize"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next"
        :total="data.total"
      />
    </div>

    <!-- 阅卷人信息弹窗 -->
    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑阅卷人' : '新增阅卷人'" width="500px" destroy-on-close class="user-dialog">
      <el-form :model="data.form" label-width="70px" style="padding: 8px 4px">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
          <el-input v-model="data.form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="编号" prop="examinerNo">
          <el-input v-model="data.form.examinerNo" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <div class="preview-avatar">
              <el-image v-if="data.form.avatar" :src="data.form.avatar" fit="cover" />
              <el-icon v-else><Plus /></el-icon>
            </div>
            <el-upload :action="baseUrl + '/files/upload'" :on-success="handleFileUpload" :show-file-list="false" accept="image/*">
              <el-button size="small" type="primary">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name" :rules="[{ required: true, message: '请输入姓名', trigger: 'blur' }]">
          <el-input v-model="data.form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone" :rules="[{ required: true, message: '请输入电话', trigger: 'blur' }]">
          <el-input v-model="data.form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" :rules="[{ required: true, message: '请输入邮箱', trigger: 'blur' }]">
          <el-input v-model="data.form.email" placeholder="请输入邮箱" />
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
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Top, Plus, Search, User, Postcard, Phone, Message, Reading, CircleCheck, EditPen, Bell } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const baseUrl = import.meta.env.VITE_BASE_URL
const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
const userLevel = user.level || 0

const data = reactive({
  loading: false,
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 9,
  total: 0,
  name: null,
  ids: [],
  gradingStats: { gradedCount: 0, pendingCount: 0, gradedThisMonth: 0 }
})

const pagedData = computed(() => data.tableData)

const load = () => {
  data.loading = true
  request.get('/examiner/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, name: data.name || undefined }
  }).then(res => {
    data.loading = false
    if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total || 0 }
    else ElMessage.error(res.msg)
  }).catch(() => { data.loading = false })
}

const loadGradingStats = () => {
  request.get('/examRecord/gradingStats').then(res => {
    if (res.code === '200') data.gradingStats = res.data
  }).catch(() => {})
}

const handleAdd = () => { data.form = {}; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); data.formVisible = true }

const add = () => {
  request.post('/examiner/add', data.form).then(res => {
    if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() }
    else ElMessage.error(res.msg)
  })
}

const update = () => {
  request.put('/examiner/update', data.form).then(res => {
    if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() }
    else ElMessage.error(res.msg)
  })
}

const save = () => { data.form.id ? update() : add() }

const del = (id) => {
  request.delete('/examiner/delete/' + id).then(res => {
    if (res.code === '200') { ElMessage.success('删除成功'); load() }
    else ElMessage.error(res.msg)
  })
}

const delBatch = () => {
  if (!data.ids.length) return
  ElMessageBox.confirm(`确定删除选中的 ${data.ids.length} 个阅卷人吗？`, '批量删除', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    .then(() => { request.delete('/examiner/delete/batch', { data: data.ids }).then(res => { if (res.code === '200') { ElMessage.success('批量删除成功'); load() } else ElMessage.error(res.msg) }) })
    .catch(() => {})
}

const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const handleFileUpload = (res) => { if (res.code === '200') data.form.avatar = res.data }

const promoteToAdmin = (row) => {
  ElMessageBox.confirm(`确定将「${row.name}」提拔为管理员吗？提拔后该用户将拥有管理员权限。`, '提拔确认', { confirmButtonText: '确认提拔', cancelButtonText: '取消', type: 'warning' })
    .then(() => {
      const transferForm = {
        id: row.id, _originalSource: 'HELPER', source: 'ADMIN',
        userNo: row.examinerNo, name: row.name, avatar: row.avatar,
        phone: row.phone, email: row.email, status: 'APPROVED'
      }
      request.post('/admin/transferUser', transferForm).then(res => {
        if (res.code === '200') { ElMessage.success('已提拔为管理员'); load() }
        else ElMessage.error(res.msg || '操作失败')
      })
    }).catch(() => {})
}

const reset = () => { data.name = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }

onMounted(() => { load(); loadGradingStats() })
</script>

<style scoped>
.user-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero {
  background: linear-gradient(135deg, #d97706 0%, #f59e0b 50%, #fbbf24 100%);
  border-radius: 16px; padding: 28px 36px; margin-bottom: 20px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 8px 32px rgba(217, 119, 6, 0.25);
}
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.2); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.25); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.8); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s, box-shadow 0.3s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px rgba(0,0,0,0.1); }
.stat-inner { padding: 20px 22px; color: #fff; position: relative; overflow: hidden; }
.stat-total .stat-inner { background: linear-gradient(135deg, #d97706, #f59e0b); }
.stat-online .stat-inner { background: linear-gradient(135deg, #059669, #10b981); }
.stat-graded .stat-inner { background: linear-gradient(135deg, #4f46e5, #6366f1); }
.stat-pending .stat-inner { background: linear-gradient(135deg, #dc2626, #ef4444); }
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
.reset-btn { border-radius: 10px; font-weight: 500; }
.batch-del-btn { border-radius: 10px; }

/* ===== 卡片网格 ===== */
.examiner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
  min-height: 200px;
}

.examiner-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  transition: transform 0.3s, box-shadow 0.3s;
}
.examiner-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(0,0,0,0.1);
  border-color: #fbbf24;
}

/* 卡片顶部 */
.card-top {
  display: flex; align-items: center; gap: 12px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.card-avatar {
  position: relative; width: 48px; height: 48px; flex-shrink: 0;
  border-radius: 50%; overflow: visible;
}
.avatar-img { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
.avatar-init {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, #d97706, #f59e0b);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: 700;
}
.online-dot {
  position: absolute; bottom: 1px; right: 1px;
  width: 12px; height: 12px; border-radius: 50%;
  background: #10b981; border: 2px solid #fff;
}
.card-info { flex: 1; min-width: 0; }
.card-name { font-size: 15px; font-weight: 700; color: #1f2937; margin-bottom: 4px; }
.card-role { display: flex; align-items: center; gap: 6px; }
.role-tag { display: inline-flex; align-items: center; padding: 2px 8px; background: #fef3c7; color: #d97706; border-radius: 20px; font-size: 11px; font-weight: 600; }
.card-actions { display: flex; gap: 4px; }

/* 卡片内容 */
.card-body { padding: 14px 18px; }
.info-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 13px; color: #6b7280; }
.info-row:last-child { margin-bottom: 0; }
.info-row .el-icon { color: #9ca3af; flex-shrink: 0; font-size: 14px; }
.info-row span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.email-text { font-size: 12px; }

/* 卡片底部 */
.card-footer { padding: 12px 18px; border-top: 1px solid var(--el-border-color-lighter); }
.promote-btn {
  width: 100%; border-radius: 8px !important;
  background: linear-gradient(135deg, #fef3c7, #fde68a) !important;
  color: #d97706 !important; border-color: #f59e0b !important; font-weight: 600;
}
.promote-btn:hover { background: linear-gradient(135deg, #fde68a, #fcd34d) !important; }

/* 空状态 */
.empty-state { grid-column: 1 / -1; padding: 80px; text-align: center; }
.empty-state p { margin: 16px 0 12px; color: #9ca3af; font-size: 14px; }

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; background: #fff; border-radius: 14px; padding: 14px 20px; border: 1px solid var(--el-border-color-lighter); }

/* 弹窗 */
.avatar-upload { display: flex; align-items: center; gap: 14px; }
.preview-avatar { width: 60px; height: 60px; border-radius: 12px; overflow: hidden; background: #f1f5f9; border: 2px dashed #e5e7eb; display: flex; align-items: center; justify-content: center; color: #9ca3af; font-size: 20px; }
.preview-avatar .el-image { width: 100%; height: 100%; }

/* 动画 */
.card-anim-enter-active, .card-anim-leave-active { transition: all 0.3s; }
.card-anim-enter-from { opacity: 0; transform: scale(0.95); }
.card-anim-leave-to { opacity: 0; transform: scale(0.9); }

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .page-hero { flex-direction: column; align-items: flex-start; gap: 16px; }
  .toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
}
</style>
