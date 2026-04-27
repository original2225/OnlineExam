<template>
  <div class="approval-center">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M9 12l2 2 4-4" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="10" stroke="#ffffff" stroke-width="2"/>
          </svg>
        </div>
        <div>
          <h1>审批中心</h1>
          <p>查看阅卷表决结果，由主考官作出入服最终判定</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-pending"><div class="stat-inner"><div class="stat-num">{{ data.stats.pendingApproval }}</div><div class="stat-label">待审批</div></div></div>
      <div class="stat-card stat-approved"><div class="stat-inner"><div class="stat-num">{{ data.stats.approved }}</div><div class="stat-label">已通过</div></div></div>
      <div class="stat-card stat-rejected"><div class="stat-inner"><div class="stat-num">{{ data.stats.rejected }}</div><div class="stat-label">未通过</div></div></div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-select v-model="data.selectedExamId" placeholder="选择审核" clearable @change="loadData" style="width: 220px" class="exam-sel">
            <template #prefix><el-icon><Document /></el-icon></template>
            <el-option v-for="e in data.examList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 待审批列表 -->
    <div class="review-list-card" v-loading="data.loading">
      <transition-group name="review-anim">
        <div v-for="row in data.pendingList" :key="row.id" class="review-card-item">
          <div class="review-left">
            <div class="review-avatar">{{ (row.studentName || '?')[0] }}</div>
          </div>
          <div class="review-body">
            <div class="review-header-row">
              <span class="review-name">{{ row.studentName }}</span>
              <span class="review-no">{{ row.studentNo }}</span>
              <el-tag type="info" size="small">{{ row.examName }}</el-tag>
            </div>
            <div class="review-meta">
              <div class="score-chip total">
                <span class="sc-label">总分</span>
                <span class="sc-val">{{ row.totalScore || 0 }}</span>
              </div>
              <span class="is-pass-tag" :class="row.isPass ? 'pass' : 'fail'">
                {{ row.isPass ? '分数达标' : '分数未达标' }}
              </span>
              <span class="chief-chip">主考官：{{ row.chiefExaminerName || '未指定' }}</span>
            </div>
          </div>
          <div class="review-actions">
            <el-button type="primary" round size="small" @click="openDecision(row)">
              <el-icon><Document /></el-icon> 查看表决
            </el-button>
          </div>
        </div>
      </transition-group>

      <div v-if="!data.loading && data.pendingList.length === 0" class="empty-state">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <path d="M9 12l2 2 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="12" cy="12" r="10" stroke="#94a3b8" stroke-width="1.5"/>
        </svg>
        <p>暂无待审批记录</p>
      </div>

      <div class="pagination-wrap" v-if="data.pendingTotal > data.pageSize">
        <el-pagination background @current-change="loadData"
          :current-page="data.pendingPage" :page-size="data.pageSize"
          layout="total, prev, pager, next" :total="data.pendingTotal" />
      </div>
    </div>

    <el-dialog v-model="data.decisionVisible" title="入服最终判定" width="720px" destroy-on-close>
      <div v-if="data.currentRecord" class="decision-panel">
        <div class="decision-head">
          <strong>{{ data.currentRecord.studentName }}</strong>
          <span>{{ data.currentRecord.examName }}</span>
          <el-tag type="warning">主考官：{{ data.currentRecord.chiefExaminerName || '未指定' }}</el-tag>
        </div>

        <div class="chief-form" v-if="isAdmin">
          <el-input-number v-model="data.chiefExaminerId" :min="1" placeholder="主考官ID" />
          <el-select v-model="data.chiefExaminerRole" style="width: 130px">
            <el-option label="阅卷人" value="HELPER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="所有者" value="OWNER" />
          </el-select>
          <el-button type="primary" plain @click="assignChief">任命/更换主考官</el-button>
        </div>

        <el-table :data="data.submissions" size="small" stripe>
          <el-table-column prop="graderName" label="阅卷人" width="110" />
          <el-table-column prop="performanceScore" label="表现分" width="90" />
          <el-table-column label="参考表决" width="100">
            <template #default="scope">{{ voteLabel(scope.row.advisoryVote) }}</template>
          </el-table-column>
          <el-table-column prop="rejectionReasons" label="不通过原因" show-overflow-tooltip />
          <el-table-column prop="customReason" label="其他原因" show-overflow-tooltip />
        </el-table>

        <el-input v-model="data.finalReason" type="textarea" :rows="3" placeholder="最终判定理由（可选）" style="margin-top: 12px" />
      </div>
      <template #footer>
        <el-button @click="data.decisionVisible = false">关闭</el-button>
        <el-button type="danger" @click="rejectOne(data.currentRecord)">最终不通过</el-button>
        <el-button type="success" @click="approveOne(data.currentRecord)">最终通过</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Search, Document } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
const isAdmin = ['OWNER', 'ADMIN'].includes(user.role)
const voteLabel = (vote) => ({ PASS: '建议通过', FAIL: '建议不通过', ABSTAIN: '弃权' })[vote] || '未表决'

const data = reactive({
  examList: [],
  selectedExamId: null,
  pendingList: [],
  approvedList: [],
  loading: false,
  pendingPage: 1,
  approvedPage: 1,
  pageSize: 10,
  pendingTotal: 0,
  approvedTotal: 0,
  stats: { pendingApproval: 0, approved: 0, rejected: 0 },
  decisionVisible: false,
  currentRecord: null,
  submissions: [],
  chiefExaminerId: null,
  chiefExaminerRole: 'HELPER',
  finalReason: ''
})

const loadExams = () => {
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') data.examList = res.data || []
  })
}

const loadStats = () => {
  request.get('/examApproval/getPending', { params: { pageNum: 1, pageSize: 1 } }).then(res => {
    if (res.code === '200') data.stats.pendingApproval = res.data?.total || 0
  }).catch(() => {})
  request.get('/examApproval/approved', { params: { pageNum: 1, pageSize: 1 } }).then(res => {
    if (res.code === '200') data.stats.approved = res.data?.total || 0
  }).catch(() => {})
  request.get('/examApproval/rejected', { params: { pageNum: 1, pageSize: 1 } }).then(res => {
    if (res.code === '200') data.stats.rejected = res.data?.total || 0
  }).catch(() => {})
}

const loadData = () => {
  data.loading = true
  request.get('/examApproval/getPending', {
    params: { examId: data.selectedExamId || undefined, pageNum: 1, pageSize: 100 }
  }).then(res => {
    if (res.code === '200') data.pendingList = res.data?.list || []
  }).finally(() => { data.loading = false })
}

const openDecision = (row) => {
  data.currentRecord = row
  data.finalReason = ''
  data.chiefExaminerId = row.chiefExaminerId || null
  data.chiefExaminerRole = 'HELPER'
  request.get('/examApproval/detail/' + row.id).then(res => {
    if (res.code === '200') {
      data.currentRecord = res.data?.record || row
      data.submissions = res.data?.submissions || []
      data.chiefExaminerId = data.currentRecord.chiefExaminerId || null
      data.decisionVisible = true
    }
  })
}

const assignChief = () => {
  if (!data.chiefExaminerId) { ElMessage.warning('请输入主考官ID'); return }
  request.put('/examApproval/chiefExaminer/' + data.currentRecord.id, {
    chiefExaminerId: data.chiefExaminerId,
    chiefExaminerRole: data.chiefExaminerRole
  }).then(res => {
    if (res.code === '200') { ElMessage.success('主考官已更新'); data.decisionVisible = false; loadData() }
    else ElMessage.error(res.msg || '操作失败')
  })
}

const approveOne = (row) => {
  request.put('/examApproval/approve/' + row.id, { reason: data.finalReason }).then(res => {
    if (res.code === '200') { ElMessage.success('最终判定通过'); data.decisionVisible = false; loadData(); loadStats() }
    else ElMessage.error(res.msg || '操作失败')
  })
}

const rejectOne = (row) => {
  request.put('/examApproval/reject/' + row.id, { reason: data.finalReason }).then(res => {
    if (res.code === '200') { ElMessage.success('最终判定不通过'); data.decisionVisible = false; loadData(); loadStats() }
    else ElMessage.error(res.msg || '操作失败')
  })
}

onMounted(() => { loadExams(); loadStats(); loadData() })
</script>

<style scoped>
.approval-center { padding: 0; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #6d28d9 0%, #8b5cf6 50%, #a78bfa 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 8px 32px rgba(109, 40, 217, 0.25); }
.hero-left { display: flex; align-items: center; gap: 14px; }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #fff; }
.page-hero p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 20px; color: #fff; }
.stat-pending .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-approved .stat-inner { background: linear-gradient(135deg, #16a34a, #4ade80); }
.stat-rejected .stat-inner { background: linear-gradient(135deg, #6b7280, #9ca3af); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { display: flex; align-items: center; gap: 10px; }
.s-icon { color: #9ca3af; }
.exam-sel :deep(.el-select__wrapper) { border-radius: 10px; }

/* ===== 列表卡片 ===== */
.review-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.review-card-item { display: flex; align-items: center; gap: 14px; padding: 18px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.review-card-item:last-child { border-bottom: none; }
.review-card-item:hover { background: #f5f3ff; }
.review-left { flex-shrink: 0; }
.review-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #8b5cf6, #a78bfa); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; }
.review-body { flex: 1; min-width: 0; }
.review-header-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; flex-wrap: wrap; }
.review-name { font-size: 15px; font-weight: 700; color: #1f2937; }
.review-no { font-size: 12px; color: #9ca3af; }
.review-meta { display: flex; align-items: center; gap: 10px; }
.score-chip { display: flex; align-items: center; gap: 5px; padding: 3px 10px; border-radius: 8px; font-size: 13px; }
.score-chip.total { background: #f3e8ff; color: #7c3aed; }
.score-chip .sc-label { font-size: 11px; opacity: 0.8; }
.score-chip .sc-val { font-weight: 700; }
.is-pass-tag { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.is-pass-tag.pass { background: #dcfce7; color: #16a34a; }
.is-pass-tag.fail { background: #fee2e2; color: #dc2626; }
.review-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

.chief-chip { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #fef3c7; color: #b45309; }
.decision-panel { display: flex; flex-direction: column; gap: 14px; }
.decision-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.chief-form { display: flex; align-items: center; gap: 10px; padding: 12px; border-radius: 10px; background: var(--el-fill-color-light); }

/* 分页 */
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 动画 */
.review-anim-enter-active, .review-anim-leave-active { transition: all 0.3s; }
.review-anim-enter-from { opacity: 0; transform: translateY(10px); }
.review-anim-leave-to { opacity: 0; }
</style>
