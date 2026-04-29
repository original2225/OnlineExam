<template>
  <div class="review-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <path d="M9 12l2 2 4-4" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>题目审核</h1>
          <p>审核管理员提交的题目，支持通过、拒绝与详情查看</p>
        </div>
      </div>
    </div>

    <!-- 统计 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.list.length }}</div><div class="stat-label">全部提交</div></div></div>
      <div class="stat-card stat-pending"><div class="stat-inner"><div class="stat-num">{{ pendingCount }}</div><div class="stat-label">待审核</div></div></div>
      <div class="stat-card stat-approved"><div class="stat-inner"><div class="stat-num">{{ approvedCount }}</div><div class="stat-label">已通过</div></div></div>
      <div class="stat-card stat-rejected"><div class="stat-inner"><div class="stat-num">{{ rejectedCount }}</div><div class="stat-label">已拒绝</div></div></div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-segmented v-model="data.statusFilter" :options="statusOptions" @change="loadList" class="status-seg" />
      </div>
      <div class="toolbar-right">
        <span class="total-tip">共 <strong>{{ data.list.length }}</strong> 条</span>
      </div>
    </div>

    <!-- 审核列表 -->
    <div class="review-list-card" v-loading="data.loading">
      <transition-group name="review-anim">
        <div v-for="row in data.list" :key="row.id" class="review-card-item">
          <div class="review-card-left">
            <div class="review-avatar">{{ (row.userName || '?')[0] }}</div>
            <div class="review-pulse" v-if="row.status === 'PENDING'"></div>
          </div>
          <div class="review-body">
            <div class="review-header-row">
              <span class="review-user">{{ row.userName }}</span>
              <span class="type-badge" :class="'type-' + row.type">{{ typeLabel(row.type) }}</span>
              <span class="diff-badge" :class="'diff-' + row.difficulty">{{ diffLabel(row.difficulty) }}</span>
              <span class="status-badge" :class="'status-' + row.status">{{ statusLabel(row.status) }}</span>
            </div>
            <div class="review-content">{{ row.content }}</div>
            <div class="review-meta">
              <span class="meta-chip">
                <el-icon><Folder /></el-icon>
                {{ row.categoryName || '未分类' }}
              </span>
              <span class="meta-chip" v-if="row.answer">
                <el-icon><CircleCheck /></el-icon>
                答案：{{ row.answer }}
              </span>
            </div>
          </div>
          <div class="review-actions">
            <el-button type="primary" link size="small" @click="openDetail(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link size="small" @click="approve(row)">
                <el-icon><Check /></el-icon> 通过
              </el-button>
              <el-button type="danger" link size="small" @click="openReject(row)">
                <el-icon><Close /></el-icon> 拒绝
              </el-button>
            </template>
          </div>
        </div>
      </transition-group>

      <!-- 空状态 -->
      <div v-if="!data.loading && data.list.length === 0" class="empty-state">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <path d="M9 12l2 2 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="#94a3b8" stroke-width="1.5"/>
        </svg>
        <p>暂无待审核题目</p>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="data.detailVisible" title="题目详情" width="580px" class="review-dialog" destroy-on-close>
      <div v-if="data.current" class="detail-content">
        <div class="detail-header">
          <el-tag type="info">{{ data.current.userName }}</el-tag>
          <el-tag :type="diffTagType(data.current.difficulty)" size="small">{{ diffLabel(data.current.difficulty) }}</el-tag>
          <el-tag type="info" size="small">{{ typeLabel(data.current.type) }}</el-tag>
        </div>
        <div class="detail-section">
          <div class="detail-label"><el-icon><EditPen /></el-icon> 题目内容</div>
          <div class="detail-box">{{ data.current.content }}</div>
        </div>
        <div class="detail-section" v-if="data.current.options">
          <div class="detail-label"><el-icon><List /></el-icon> 选项</div>
          <div class="detail-box">
            <div v-for="(val, key) in parseOptions(data.current.options)" :key="key" class="option-item">
              <span class="option-key">{{ key }}.</span>
              <span>{{ val }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label"><el-icon><CircleCheck /></el-icon> 正确答案</div>
          <div class="detail-box correct">{{ data.current.answer }}</div>
        </div>
        <div class="detail-section" v-if="data.current.analysis">
          <div class="detail-label"><el-icon><InfoFilled /></el-icon> 题目解析</div>
          <div class="detail-box">{{ data.current.analysis }}</div>
        </div>
      </div>
      <template #footer v-if="data.current && data.current.status === 'PENDING'">
        <el-button @click="data.detailVisible = false">关闭</el-button>
        <el-button type="danger" @click="openReject(data.current)">拒绝</el-button>
        <el-button type="success" @click="approve(data.current)">通过审核</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="data.rejectVisible" title="填写拒绝原因" width="450px" destroy-on-close>
      <div class="reject-tip">
        <el-icon><WarningFilled /></el-icon>
        请说明拒绝原因，帮助贡献者改进题目质量
      </div>
      <el-input v-model="data.rejectComment" type="textarea" :rows="4" placeholder="请填写拒绝原因，如：答案错误、题目歧义、格式不规范等" />
      <template #footer>
        <el-button @click="data.rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="reject">确认拒绝</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import { View, Check, Close, Folder, CircleCheck, EditPen, List, InfoFilled, WarningFilled } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
]

const data = reactive({
  list: [],
  loading: false,
  statusFilter: 'PENDING',
  detailVisible: false,
  rejectVisible: false,
  rejectComment: '',
  current: null,
})

const pendingCount = computed(() => data.list.filter(r => r.status === 'PENDING').length)
const approvedCount = computed(() => data.list.filter(r => r.status === 'APPROVED').length)
const rejectedCount = computed(() => data.list.filter(r => r.status === 'REJECTED').length)

const diffLabel = (d) => ({ easy: '简单', medium: '中等', hard: '困难' })[d] || d
const diffTagType = (d) => ({ easy: 'success', medium: 'warning', hard: 'danger' })[d] || 'info'
const typeLabel = (t) => ({ single: '单选题', multiple: '多选题', judge: '判断题', fillin: '填空题', essay: '简答题', '选择题': '单选题', '判断题': '判断题', '填空题': '填空题' })[t] || t
const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' })[s] || s

const parseOptions = (opts) => {
  try { return typeof opts === 'string' ? JSON.parse(opts) : opts } catch { return {} }
}

onMounted(() => { loadList() })

const loadList = () => {
  data.loading = true
  request.get('/questionContribution/selectPage', {
    params: { status: data.statusFilter || undefined, pageNum: 1, pageSize: 50 }
  }).then(res => {
    data.loading = false
    data.list = res.data?.list || res.data || []
  }).catch(() => { data.loading = false })
}

const openDetail = (row) => { data.current = row; data.detailVisible = true }

const approve = (row) => {
  request.put('/questionContribution/review/' + row.id, { action: 'approve' }).then(res => {
    if (res.code === '200') { ElMessage.success('已通过，题目已自动入库'); data.detailVisible = false; loadList() }
    else ElMessage.error(res.msg || '操作失败')
  })
}

const openReject = (row) => { data.current = row; data.rejectComment = ''; data.rejectVisible = true }

const reject = () => {
  if (!data.rejectComment) { ElMessage.warning('请填写拒绝原因'); return }
  request.put('/questionContribution/review/' + data.current.id, {
    action: 'reject',
    comment: data.rejectComment
  }).then(res => {
    if (res.code === '200') { ElMessage.success('已拒绝'); data.rejectVisible = false; data.detailVisible = false; loadList() }
    else ElMessage.error(res.msg || '操作失败')
  })
}
</script>

<style scoped>
.review-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #b45309 0%, #d97706 50%, #fbbf24 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(217, 119, 6, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 20px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #b45309, #d97706); }
.stat-pending .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-approved .stat-inner { background: linear-gradient(135deg, #16a34a, #4ade80); }
.stat-rejected .stat-inner { background: linear-gradient(135deg, #6b7280, #9ca3af); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-right { }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #d97706; }

/* ===== 审核列表卡片 ===== */
.review-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.review-card-item { display: flex; align-items: flex-start; gap: 14px; padding: 18px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; position: relative; }
.review-card-item:last-child { border-bottom: none; }
.review-card-item:hover { background: #fffbeb; }
.review-card-left { flex-shrink: 0; position: relative; }
.review-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, #d97706, #fbbf24); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; }
.review-pulse { position: absolute; top: -2px; right: -2px; width: 12px; height: 12px; background: #f87171; border-radius: 50%; border: 2px solid #fff; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.3); opacity: 0.7; } }
.review-body { flex: 1; min-width: 0; }
.review-header-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.review-user { font-size: 14px; font-weight: 600; color: #1f2937; }
.type-badge { display: inline-block; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; background: #dbeafe; color: #2563eb; }
.diff-badge { display: inline-block; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.diff-easy { background: #dcfce7; color: #16a34a; }
.diff-medium { background: #fef3c7; color: #d97706; }
.diff-hard { background: #fee2e2; color: #dc2626; }
.status-badge { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.status-PENDING { background: #fef3c7; color: #d97706; }
.status-APPROVED { background: #dcfce7; color: #16a34a; }
.status-REJECTED { background: #fee2e2; color: #dc2626; }
.review-content { font-size: 14px; color: #374151; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 8px; }
.review-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.meta-chip { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #6b7280; }
.meta-chip .el-icon { font-size: 12px; }
.review-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin: 16px 0 12px; color: #9ca3af; font-size: 14px; }

/* 详情弹窗 */
.detail-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.detail-section { margin-bottom: 16px; }
.detail-label { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #6b7280; margin-bottom: 8px; }
.detail-box { background: #f9fafb; padding: 12px 14px; border-radius: 10px; line-height: 1.8; font-size: 14px; color: #374151; border: 1px solid #f0f0f0; }
.detail-box.correct { color: #16a34a; font-weight: 600; background: #f0fdf4; border-color: #bbf7d0; }
.option-item { display: flex; gap: 8px; }
.option-key { font-weight: 700; color: #374151; }

/* 拒绝弹窗 */
.reject-tip { display: flex; align-items: center; gap: 8px; background: #fef3c7; border: 1px solid #fde68a; border-radius: 10px; padding: 10px 14px; margin-bottom: 14px; font-size: 13px; color: #92400e; }

/* 动画 */
.review-anim-enter-active, .review-anim-leave-active { transition: all 0.3s; }
.review-anim-enter-from { opacity: 0; transform: translateY(10px); }
.review-anim-leave-to { opacity: 0; }

@media (max-width: 700px) { .review-card-item { flex-wrap: wrap; } .review-actions { flex-direction: row; } }
</style>
