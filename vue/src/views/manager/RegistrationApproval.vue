<template>
  <div class="approval-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M9 12l2 2 4-4" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="#ffffff" stroke-width="2"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>注册审批</h1>
          <p>审核玩家注册申请，拒绝或通过新玩家入驻</p>
        </div>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="num">{{ pendingCount }}</span>
          <span class="label">待审批</span>
        </div>
        <div class="hero-divider"></div>
        <div class="hero-stat">
          <span class="num">{{ approvedCount }}</span>
          <span class="label">已通过</span>
        </div>
        <div class="hero-divider"></div>
        <div class="hero-stat">
          <span class="num">{{ rejectedCount }}</span>
          <span class="label">已拒绝</span>
        </div>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input
            v-model="data.searchForm.username"
            placeholder="搜索用户名..."
            clearable
            @keyup.enter="load"
            @clear="load"
            class="search-input"
          />
        </div>
        <el-select v-model="data.searchForm.status" placeholder="全部状态" @change="load" class="filter-sel">
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="全部状态" value="" />
          <el-option label="待审批" value="PENDING">
            <span class="opt-pending">● 待审批</span>
          </el-option>
          <el-option label="已通过" value="APPROVED">
            <span class="opt-approved">● 已通过</span>
          </el-option>
          <el-option label="已拒绝" value="REJECTED">
            <span class="opt-rejected">● 已拒绝</span>
          </el-option>
        </el-select>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 条申请记录</span>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="list-card">
      <div v-loading="data.loading" class="approval-list">
        <transition-group name="list-anim">
          <div
            v-for="row in data.tableData"
            :key="row.id"
            class="approval-item"
            :class="'status-' + row.status.toLowerCase()"
          >
            <!-- 左侧：玩家信息 -->
            <div class="item-left">
              <div class="user-avatar">
                <img v-if="row.avatar" :src="row.avatar" alt="avatar" />
                <span v-else class="avatar-init">{{ (row.name || row.username || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <div class="user-info">
                <div class="user-name-row">
                  <span class="uname">{{ row.name || row.username }}</span>
                  <span class="ustatus-badge" :class="'badge-' + row.status.toLowerCase()">
                    <span class="dot-pulse"></span>
                    {{ statusLabel(row.status) }}
                  </span>
                </div>
                <div class="user-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ row.username }}
                  </span>
                  <span class="meta-item" v-if="row.studentNo">
                    <el-icon><Postcard /></el-icon>
                    {{ row.studentNo }}
                  </span>
                  <span class="meta-item" v-if="row.email">
                    <el-icon><Message /></el-icon>
                    {{ row.email }}
                  </span>
                  <span class="meta-item" v-if="row.phone">
                    <el-icon><Phone /></el-icon>
                    {{ row.phone }}
                  </span>
                  <span class="meta-item" v-if="row.className">
                    <el-icon><OfficeBuilding /></el-icon>
                    {{ row.className }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 中间：时间线 -->
            <div class="item-center">
              <div class="timeline">
                <div class="time-item">
                  <div class="time-dot dot-req"></div>
                  <div class="time-info">
                    <span class="time-label">申请时间</span>
                    <span class="time-val">{{ formatTime(row.requestTime) }}</span>
                  </div>
                </div>
                <div class="time-line"></div>
                <div class="time-item">
                  <div class="time-dot" :class="row.status === 'APPROVED' ? 'dot-appr' : row.status === 'REJECTED' ? 'dot-rej' : 'dot-pend'"></div>
                  <div class="time-info">
                    <span class="time-label">{{ row.status === 'PENDING' ? '待审核' : '处理时间' }}</span>
                    <span class="time-val" :class="{ 'is-pending': row.status === 'PENDING' }">
                      {{ row.approvalTime ? formatTime(row.approvalTime) : '—' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧：操作 -->
            <div class="item-actions">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" class="action-btn approve-btn" @click="handleApprove(row)">
                  <el-icon><Check /></el-icon> 批准
                </el-button>
                <el-button type="danger" size="small" class="action-btn reject-btn" @click="handleReject(row)">
                  <el-icon><Close /></el-icon> 拒绝
                </el-button>
              </template>
              <template v-else>
                <div class="result-tag" :class="'result-' + row.status.toLowerCase()">
                  <el-icon v-if="row.status === 'APPROVED'"><Check /></el-icon>
                  <el-icon v-else><Close /></el-icon>
                  {{ row.status === 'APPROVED' ? '已通过' : '已拒绝' }}
                </div>
                <div v-if="row.rejectionReason" class="reject-reason">
                  <el-icon><InfoFilled /></el-icon>
                  原因：{{ row.rejectionReason }}
                </div>
              </template>
              <div class="minor-actions">
                <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
                <el-popconfirm
                  title="确定删除该记录？"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button link type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </transition-group>

        <!-- 空状态 -->
        <div v-if="!data.loading && data.tableData.length === 0" class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#f1f5f9"/>
            <path d="M8 15s1.5-2 4-2 4 2 4 2M9 9h.01M15 9h.01" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p>暂无申请记录</p>
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="data.total"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="data.dialogVisible" title="申请详情" width="560px" class="detail-dialog">
      <div v-if="data.currentRow" class="detail-body">
        <div class="detail-header">
          <div class="detail-avatar">
            <img v-if="data.currentRow.avatar" :src="data.currentRow.avatar" />
            <span v-else>{{ (data.currentRow.name || 'U').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="detail-name">
            <h3>{{ data.currentRow.name || '—' }}</h3>
            <p>{{ data.currentRow.username }}</p>
          </div>
          <div class="detail-badge" :class="'badge-' + (data.currentRow.status || '').toLowerCase()">
            <span class="dot-pulse"></span>
            {{ statusLabel(data.currentRow.status) }}
          </div>
        </div>
        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="用户名">{{ data.currentRow.username }}</el-descriptions-item>
          <el-descriptions-item label="编号">{{ data.currentRow.studentNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ data.currentRow.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ data.currentRow.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核分组">{{ data.currentRow.className || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(data.currentRow.requestTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批时间" :span="2">{{ data.currentRow.approvalTime ? formatTime(data.currentRow.approvalTime) : '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="data.currentRow.rejectionReason" label="拒绝原因" :span="2">
            <span style="color: #ef4444">{{ data.currentRow.rejectionReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="data.rejectDialogVisible" title="拒绝申请" width="440px" class="reject-dialog">
      <div class="reject-form">
        <div class="reject-tip">
          <el-icon><Warning /></el-icon>
          请填写拒绝原因，以便申请人了解情况
        </div>
        <el-input
          v-model="data.rejectionReason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝原因（选填）"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="data.rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">
          <el-icon><Close /></el-icon> 确认拒绝
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, User, Check, Close, Phone, Message, Postcard, OfficeBuilding, InfoFilled, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const pendingCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)

const data = reactive({
  loading: false,
  tableData: [],
  searchForm: { status: '', username: '' },
  pageNum: 1,
  pageSize: 10,
  total: 0,
  dialogVisible: false,
  rejectDialogVisible: false,
  currentRow: {},
  rejectionReason: '',
  currentRejectRow: null
})

const statusLabel = (s) => {
  if (s === 'PENDING') return '待审批'
  if (s === 'APPROVED') return '已通过'
  if (s === 'REJECTED') return '已拒绝'
  return s
}

const formatTime = (val) => {
  if (!val) return '—'
  return String(val).replace('T', ' ').substring(0, 19)
}

const load = () => {
  data.loading = true
  request.get('/registration/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: data.searchForm.status || undefined,
      username: data.searchForm.username || undefined
    }
  }).then(res => {
    data.loading = false
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total || 0
      // 统计各状态数量
      pendingCount.value = data.tableData.filter(r => r.status === 'PENDING').length
      approvedCount.value = data.tableData.filter(r => r.status === 'APPROVED').length
      rejectedCount.value = data.tableData.filter(r => r.status === 'REJECTED').length
    }
  }).catch(() => { data.loading = false })
}

const reset = () => {
  data.searchForm = { status: '', username: '' }
  data.pageNum = 1
  load()
}

const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确定批准玩家「${row.name || row.username}」的注册申请吗？审批通过后将自动创建账号。`,
    '批准确认',
    { confirmButtonText: '确认批准', cancelButtonText: '取消', type: 'success', icon: Check }
  ).then(() => {
    request.post('/registration/approve', null, { params: { id: row.id } }).then(res => {
      if (res.code === '200') { ElMessage.success('已批准申请，玩家账号已创建'); load() }
      else ElMessage.error(res.msg || '操作失败')
    })
  }).catch(() => {})
}

const handleReject = (row) => {
  data.currentRejectRow = row
  data.rejectionReason = ''
  data.rejectDialogVisible = true
}

const confirmReject = () => {
  request.post('/registration/reject', null, {
    params: { id: data.currentRejectRow.id, reason: data.rejectionReason || undefined }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('已拒绝申请')
      data.rejectDialogVisible = false
      load()
    } else { ElMessage.error(res.msg || '操作失败') }
  })
}

const handleView = (row) => {
  data.currentRow = JSON.parse(JSON.stringify(row))
  data.dialogVisible = true
}

const handleDelete = (id) => {
  request.delete('/registration/delete/' + id).then(res => {
    if (res.code === '200') { ElMessage.success('删除成功'); load() }
    else ElMessage.error(res.msg || '删除失败')
  })
}

const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }

onMounted(() => { load() })
</script>

<style scoped>
.approval-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== 页面标题区 ===== */
.page-hero {
  background: linear-gradient(135deg, #0891b2 0%, #06b6d4 50%, #22d3ee 100%);
  border-radius: 16px;
  padding: 28px 36px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 32px rgba(8, 145, 178, 0.25);
}
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon {
  width: 60px; height: 60px; background: rgba(255,255,255,0.15);
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2);
}
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-stats { display: flex; align-items: center; gap: 24px; }
.hero-stat { text-align: center; }
.hero-stat .num { display: block; font-size: 28px; font-weight: 800; color: #fff; line-height: 1; }
.hero-stat .label { font-size: 12px; color: rgba(255,255,255,0.7); }
.hero-divider { width: 1px; height: 36px; background: rgba(255,255,255,0.3); }

/* ===== 工具栏 ===== */
.toolbar {
  background: #fff; border-radius: 14px; padding: 16px 20px;
  margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter);
}
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 150px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; font-weight: 500; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #4f46e5; }

/* ===== 列表卡片 ===== */
.list-card {
  background: #fff; border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
}
.approval-list { min-height: 200px; }

/* ===== 审批项 ===== */
.approval-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background 0.2s;
  gap: 20px;
}
.approval-item:last-child { border-bottom: none; }
.approval-item:hover { background: #f8f7ff; }
.approval-item.status-rejected { opacity: 0.7; }

/* 玩家信息 */
.item-left { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.user-avatar {
  width: 46px; height: 46px; border-radius: 50%; overflow: hidden;
  background: linear-gradient(135deg, #4f46e5, #7c3aed); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-init { color: #fff; font-size: 18px; font-weight: 700; }
.user-info { flex: 1; min-width: 0; }
.user-name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.uname { font-size: 15px; font-weight: 700; color: #1f2937; }
.ustatus-badge {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 600;
}
.badge-pending { background: #fef3c7; color: #d97706; }
.badge-approved { background: #dcfce7; color: #16a34a; }
.badge-rejected { background: #fee2e2; color: #dc2626; }
.dot-pulse {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor;
}
.badge-pending .dot-pulse { animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }

.user-meta { display: flex; flex-wrap: wrap; gap: 8px 16px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #9ca3af; }
.meta-item .el-icon { font-size: 12px; }

/* 时间线 */
.item-center { width: 160px; flex-shrink: 0; }
.timeline { display: flex; flex-direction: column; gap: 0; }
.time-item { display: flex; align-items: center; gap: 8px; }
.time-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.dot-req { background: #6366f1; }
.dot-appr { background: #10b981; }
.dot-rej { background: #ef4444; }
.dot-pend { background: #d97706; }
.time-line { width: 1px; height: 16px; background: #e5e7eb; margin-left: 3.5px; }
.time-info { display: flex; flex-direction: column; }
.time-label { font-size: 10px; color: #9ca3af; }
.time-val { font-size: 12px; color: #374151; font-weight: 500; }
.time-val.is-pending { color: #d97706; font-style: italic; }

/* 操作区 */
.item-actions { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex-shrink: 0; }
.action-btn { border-radius: 8px !important; font-weight: 600; }
.approve-btn { --el-button-bg-color: #dcfce7; --el-button-border-color: #86efac; --el-button-text-color: #16a34a; }
.approve-btn:hover { --el-button-bg-color: #bbf7d0; }
.reject-btn { --el-button-bg-color: #fee2e2; --el-button-border-color: #fca5a5; --el-button-text-color: #dc2626; }
.reject-btn:hover { --el-button-bg-color: #fecaca; }
.result-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.result-approved { background: #dcfce7; color: #16a34a; }
.result-rejected { background: #fee2e2; color: #dc2626; }
.reject-reason { font-size: 11px; color: #9ca3af; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.minor-actions { display: flex; gap: 8px; }

/* 空状态 */
.empty-state { padding: 60px; text-align: center; }
.empty-state p { margin: 16px 0 0; color: #9ca3af; font-size: 14px; }

/* 分页 */
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* ===== 详情对话框 ===== */
.detail-body { padding: 4px 0; }
.detail-header { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--el-border-color-lighter); }
.detail-avatar { width: 52px; height: 52px; border-radius: 50%; overflow: hidden; background: linear-gradient(135deg, #4f46e5, #7c3aed); display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff; font-weight: 700; flex-shrink: 0; }
.detail-avatar img { width: 100%; height: 100%; object-fit: cover; }
.detail-name { flex: 1; }
.detail-name h3 { margin: 0 0 4px; font-size: 18px; font-weight: 700; color: #1f2937; }
.detail-name p { margin: 0; font-size: 13px; color: #9ca3af; }
.detail-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.detail-desc { margin-top: 4px; }

/* ===== 拒绝对话框 ===== */
.reject-form { padding: 4px 0; }
.reject-tip { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #fef3c7; border-radius: 10px; color: #92400e; font-size: 13px; margin-bottom: 14px; }
.reject-form .el-textarea__inner { border-radius: 10px; }

/* ===== 过渡动画 ===== */
.list-anim-enter-active, .list-anim-leave-active { transition: all 0.3s; }
.list-anim-enter-from { opacity: 0; transform: translateY(10px); }
.list-anim-leave-to { opacity: 0; transform: translateX(-20px); }

@media (max-width: 900px) {
  .approval-item { flex-wrap: wrap; }
  .item-center { width: 100%; }
  .toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
  .hero-stats { display: none; }
}
</style>
