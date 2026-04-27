<template>
  <div class="exam-manage">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <rect x="9" y="3" width="6" height="4" rx="1" stroke="#ffffff" stroke-width="2"/>
            <path d="M9 12h6M9 16h4" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <h1>考试管理</h1>
          <p>创建、发布与管控所有考试安排</p>
        </div>
      </div>
      <el-button type="primary" size="default" @click="openCreate" style="background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.3); color: #fff;">
        <el-icon><Plus /></el-icon> 创建考试
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" v-if="data.examList.length">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">总考试</div></div></div>
      <div class="stat-card stat-published"><div class="stat-inner"><div class="stat-num">{{ publishedCount }}</div><div class="stat-label">已发布</div></div></div>
      <div class="stat-card stat-ongoing"><div class="stat-inner"><div class="stat-num">{{ ongoingCount }}</div><div class="stat-label">进行中</div></div></div>
      <div class="stat-card stat-finished"><div class="stat-inner"><div class="stat-num">{{ finishedCount }}</div><div class="stat-label">已结束</div></div></div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.query.name" placeholder="搜索考试名称" clearable style="width: 200px;" @keyup.enter="loadExams" />
        </div>
        <el-select v-model="data.query.status" placeholder="状态筛选" clearable style="width: 140px;" @change="loadExams">
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已结束" value="finished" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-select v-model="data.query.examType" placeholder="考试类型" clearable style="width: 140px;" @change="loadExams">
          <el-option label="统一考试" value="scheduled" />
          <el-option label="常驻考试" value="permanent" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="loadExams" size="small">
          <el-icon><Search /></el-icon> 查询
        </el-button>
      </div>
    </div>

    <!-- 考试列表 -->
    <div class="exam-list-card" v-loading="data.loading">
      <transition-group name="exam-anim">
        <div v-for="row in data.examList" :key="row.id" class="exam-card-item">
          <div class="exam-card-left">
            <div class="exam-card-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
          </div>
          <div class="exam-card-body">
            <div class="exam-card-header">
              <span class="exam-name">{{ row.name }}</span>
              <div class="exam-badges">
                <span class="exam-status-badge" :class="row.status">{{ statusLabel(row.status) }}</span>
                <span class="exam-type-badge" :class="row.examType">{{ row.examType === 'scheduled' ? '统一考试' : '常驻考试' }}</span>
                <el-tag v-if="row.enableRecording" type="warning" size="small" effect="plain">录屏</el-tag>
                <el-tag v-if="row.autoPublish" type="success" size="small" effect="plain">自动公示</el-tag>
              </div>
            </div>
            <div class="exam-card-meta">
              <div class="meta-item">
                <el-icon><Document /></el-icon>
                <span>{{ row.paperName || '未关联试卷' }}</span>
              </div>
              <div class="meta-item" v-if="row.startTime">
                <el-icon><Clock /></el-icon>
                <span>{{ row.startTime?.replace('T', ' ') }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Timer /></el-icon>
                <span>{{ row.duration }}分钟</span>
              </div>
              <div class="meta-item" v-if="row.branch">
                <el-icon><Folder /></el-icon>
                <span>{{ row.branch }}</span>
              </div>
            </div>
            <div class="exam-card-desc" v-if="row.description">{{ row.description }}</div>
          </div>
          <div class="exam-card-actions">
            <el-button v-if="row.status === 'draft'" type="success" round size="small" @click="publishExam(row)">
              <el-icon><Promotion /></el-icon> 发布
            </el-button>
            <el-button v-if="row.status === 'published' || row.status === 'ongoing'" type="warning" round size="small" @click="cancelExam(row)">
              <el-icon><Close /></el-icon> 取消
            </el-button>
            <el-button type="primary" plain round size="small" @click="openEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" plain round size="small" @click="deleteExam(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </transition-group>

      <div v-if="!data.loading && !data.examList.length" class="empty-state">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" stroke="#93c5fd" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <rect x="9" y="3" width="6" height="4" rx="1" stroke="#93c5fd" stroke-width="1.5"/>
        </svg>
        <p>暂无考试，创建一个开始吧</p>
        <el-button type="primary" @click="openCreate" size="small">
          <el-icon><Plus /></el-icon> 创建考试
        </el-button>
      </div>

      <div class="pagination-wrap" v-if="data.total > data.pageSize">
        <el-pagination
          background
          @current-change="loadExams"
          :current-page="data.pageNum"
          :page-size="data.pageSize"
          layout="total, prev, pager, next"
          :total="data.total"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="data.dialogVisible" :title="data.isEdit ? '编辑考试' : '创建考试'" width="680px" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding: 0 20px;">
        <el-form-item label="考试名称" required>
          <el-input v-model="data.form.name" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="关联试卷" required>
          <el-select v-model="data.form.paperId" placeholder="选择试卷" style="width: 100%;">
            <el-option v-for="p in data.paperList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试描述">
          <el-input v-model="data.form.description" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="考试类型">
              <el-select v-model="data.form.examType" style="width: 100%;">
                <el-option label="统一考试" value="scheduled" />
                <el-option label="常驻考试" value="permanent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试时长">
              <el-input-number v-model="data.form.duration" :min="5" :max="600" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="data.form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="data.form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="允许迟到">
              <el-switch v-model="data.form.allowLate" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="data.form.allowLate">
            <el-form-item label="迟到时间">
              <el-input-number v-model="data.form.lateTime" :min="1" :max="60" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="自动公示">
              <el-switch v-model="data.form.autoPublish" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开启录屏">
              <el-switch v-model="data.form.enableRecording" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="考试分支">
              <el-select v-model="data.form.branch" placeholder="选择分支" clearable style="width: 100%;">
                <el-option label="红石" value="红石" />
                <el-option label="生电" value="生电" />
                <el-option label="建筑" value="建筑" />
                <el-option label="交流" value="交流" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogVisible = false" size="default">取消</el-button>
        <el-button type="primary" @click="saveExam" size="default">
          <el-icon><Check /></el-icon> 确定
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, Plus, Edit, Delete, Document, Clock, Timer, Folder, Promotion, Close, Check } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  query: { name: '', status: '', examType: '' },
  pageNum: 1,
  pageSize: 10,
  total: 0,
  examList: [],
  paperList: [],
  loading: false,
  dialogVisible: false,
  isEdit: false,
  form: {
    id: null, name: '', paperId: null, description: '',
    examType: 'scheduled', duration: 60, startTime: null, endTime: null,
    allowLate: false, lateTime: 10, autoPublish: false, enableRecording: false,
    branch: null
  }
})

const statusLabel = (s) => ({ draft: '草稿', published: '已发布', ongoing: '进行中', finished: '已结束', cancelled: '已取消' }[s] || s)
const statusClass = (s) => ({ draft: 'badge-draft', published: 'badge-published', ongoing: 'badge-ongoing', finished: 'badge-finished', cancelled: 'badge-cancelled' }[s] || '')

const publishedCount = computed(() => data.examList.filter(r => r.status === 'published').length)
const ongoingCount = computed(() => data.examList.filter(r => r.status === 'ongoing').length)
const finishedCount = computed(() => data.examList.filter(r => r.status === 'finished').length)

const loadExams = () => {
  data.loading = true
  request.get('/exam/selectPage', { params: { ...data.query, pageNum: data.pageNum, pageSize: data.pageSize } }).then(res => {
    if (res.code === '200') {
      data.examList = res.data?.list || []
      data.total = res.data?.total || 0
    }
  }).catch(() => {}).finally(() => { data.loading = false })
}

const loadPapers = () => {
  request.get('/examPaper/selectAll').then(res => {
    if (res.code === '200') data.paperList = res.data || []
  }).catch(() => {})
}

const resetForm = () => ({
  id: null, name: '', paperId: null, description: '',
  examType: 'scheduled', duration: 60, startTime: null, endTime: null,
  allowLate: false, lateTime: 10, autoPublish: false, enableRecording: false,
  branch: null
})

const openCreate = () => {
  data.form = resetForm()
  data.isEdit = false
  data.dialogVisible = true
  loadPapers()
}

const openEdit = (row) => {
  data.form = { ...row }
  data.isEdit = true
  data.dialogVisible = true
  loadPapers()
}

const saveExam = () => {
  if (!data.form.name || !data.form.paperId) {
    ElMessage.warning('请填写必要信息')
    return
  }
  const url = data.isEdit ? '/exam/update' : '/exam/add'
  const req = data.isEdit ? request.put(url, data.form) : request.post(url, data.form)
  req.then(res => {
    if (res.code === '200') {
      ElMessage.success(data.isEdit ? '更新成功' : '创建成功')
      data.dialogVisible = false
      loadExams()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(() => {})
}

const publishExam = (row) => {
  ElMessageBox.confirm('确定发布此考试？', '提示').then(() => {
    request.put('/exam/publish/' + row.id).then(res => {
      if (res.code === '200') { ElMessage.success('发布成功'); loadExams() }
      else ElMessage.error(res.msg)
    }).catch(() => {})
  }).catch(() => {})
}

const cancelExam = (row) => {
  ElMessageBox.confirm('确定取消此考试？', '提示').then(() => {
    request.put('/exam/cancel/' + row.id).then(res => {
      if (res.code === '200') { ElMessage.success('已取消'); loadExams() }
      else ElMessage.error(res.msg)
    }).catch(() => {})
  }).catch(() => {})
}

const deleteExam = (row) => {
  ElMessageBox.confirm('确定删除此考试？此操作不可恢复', '警告').then(() => {
    request.delete('/exam/delete/' + row.id).then(res => {
      if (res.code === '200') { ElMessage.success('已删除'); loadExams() }
      else ElMessage.error(res.msg)
    }).catch(() => {})
  }).catch(() => {})
}

onMounted(() => { loadExams() })
</script>

<style scoped>
.exam-manage { padding: 0; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #059669 0%, #10b981 50%, #34d399 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(5, 150, 105, 0.25); }
.hero-left { display: flex; align-items: center; gap: 14px; }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #fff; }
.page-hero p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 16px 18px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #059669, #10b981); }
.stat-published .stat-inner { background: linear-gradient(135deg, #0284c7, #38bdf8); }
.stat-ongoing .stat-inner { background: linear-gradient(135deg, #d97706, #fbbf24); }
.stat-finished .stat-inner { background: linear-gradient(135deg, #6b7280, #9ca3af); }
.stat-num { font-size: 26px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { display: flex; align-items: center; gap: 8px; }
.s-icon { color: #9ca3af; }

/* ===== 列表卡片 ===== */
.exam-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.exam-card-item { display: flex; align-items: flex-start; gap: 14px; padding: 20px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.exam-card-item:last-child { border-bottom: none; }
.exam-card-item:hover { background: #f0fdf4; }
.exam-card-left { flex-shrink: 0; }
.exam-card-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #059669, #10b981); color: #fff; display: flex; align-items: center; justify-content: center; }
.exam-card-body { flex: 1; min-width: 0; }
.exam-card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
.exam-name { font-size: 16px; font-weight: 700; color: #1f2937; }
.exam-badges { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.exam-status-badge { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-draft { background: #f1f5f9; color: #6b7280; }
.badge-published { background: #dbeafe; color: #1d4ed8; }
.badge-ongoing { background: #fef3c7; color: #b45309; }
.badge-finished { background: #dcfce7; color: #15803d; }
.badge-cancelled { background: #fee2e2; color: #b91c1c; }
.exam-type-badge { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #ecfdf5; color: #059669; }
.exam-card-meta { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 5px; font-size: 13px; color: #6b7280; }
.meta-item .el-icon { font-size: 14px; }
.exam-card-desc { margin-top: 6px; font-size: 13px; color: #9ca3af; }
.exam-card-actions { display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

/* 分页 */
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 动画 */
.exam-anim-enter-active, .exam-anim-leave-active { transition: all 0.3s; }
.exam-anim-enter-from { opacity: 0; transform: translateY(8px); }
.exam-anim-leave-to { opacity: 0; }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
