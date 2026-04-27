<template>
  <div class="announcements">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h1>审核公告</h1>
          <p>查看与发布入服审核相关通知公告</p>
        </div>
      </div>
      <el-button v-if="canEdit" type="primary" size="default" @click="openCreate" style="background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.3); color: #fff;">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" v-if="data.list.length">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.list.length }}</div><div class="stat-label">全部公告</div></div></div>
      <div class="stat-card stat-exam"><div class="stat-inner"><div class="stat-num">{{ examCount }}</div><div class="stat-label">审核通知</div></div></div>
      <div class="stat-card stat-result"><div class="stat-inner"><div class="stat-num">{{ resultCount }}</div><div class="stat-label">结果公告</div></div></div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar" v-if="canEdit">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.query.title" placeholder="搜索标题" clearable style="width: 200px;" @keyup.enter="loadList" />
        </div>
        <el-select v-model="data.query.type" placeholder="类型筛选" clearable style="width: 130px;" @change="loadList">
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="通用" value="general" />
          <el-option label="审核" value="exam" />
          <el-option label="结果" value="result" />
          <el-option label="通知" value="notice" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="loadList" size="small">
          <el-icon><Search /></el-icon> 查询
        </el-button>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="anno-list-card" v-loading="data.loading">
      <transition-group name="anno-anim">
        <div v-for="item in data.list" :key="item.id" class="anno-card-item" @click="viewDetail(item)">
          <div class="anno-card-left">
            <div class="anno-type-icon" :class="item.type">
              <el-icon v-if="item.type === 'exam'"><EditPen /></el-icon>
              <el-icon v-else-if="item.type === 'result'"><DataLine /></el-icon>
              <el-icon v-else-if="item.type === 'notice'"><Bell /></el-icon>
              <el-icon v-else><ChatLineSquare /></el-icon>
            </div>
          </div>
          <div class="anno-card-body">
            <div class="anno-header-row">
              <span class="anno-title">{{ item.title }}</span>
              <span class="anno-type-badge" :class="item.type">{{ typeLabel(item.type) }}</span>
            </div>
            <div class="anno-card-meta">
              <span v-if="item.examName" class="meta-tag exam-tag">
                <el-icon><Document /></el-icon> {{ item.examName }}
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon> {{ item.authorName }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon> {{ item.createdAt?.substring(0, 16).replace('T', ' ') }}
              </span>
            </div>
            <div class="anno-preview" v-if="item.content">
              {{ item.content.length > 120 ? item.content.substring(0, 120) + '…' : item.content }}
            </div>
          </div>
          <div class="anno-card-actions" v-if="canEdit" @click.stop>
            <el-button text type="primary" size="small" @click="openEdit(item)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text type="danger" size="small" @click="deleteAnno(item)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </transition-group>

      <div v-if="!data.loading && !data.list.length" class="empty-state">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#fcd34d" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="#fcd34d" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>暂无公告</p>
        <el-button v-if="canEdit" type="primary" @click="openCreate" size="small">
          <el-icon><Plus /></el-icon> 发布公告
        </el-button>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="data.dialogVisible" :title="data.isEdit ? '编辑公告' : '发布公告'" width="620px" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 0 20px;">
        <el-form-item label="标题" required>
          <el-input v-model="data.form.title" placeholder="公告标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="data.form.type" style="width: 100%;">
                <el-option label="通用公告" value="general" />
                <el-option label="审核通知" value="exam" />
                <el-option label="结果公告" value="result" />
                <el-option label="系统通知" value="notice" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number v-model="data.form.priority" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联审核">
          <el-select v-model="data.form.examId" placeholder="可选" clearable style="width: 100%;">
            <el-option v-for="e in data.examList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="data.form.content" type="textarea" :rows="6" placeholder="公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogVisible = false" size="default">取消</el-button>
        <el-button type="primary" @click="saveAnno" size="default">
          <el-icon><Check /></el-icon> {{ data.isEdit ? '更新' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="data.detailVisible" :title="data.detail.title" width="620px">
      <div class="detail-meta">
        <span class="anno-type-badge" :class="data.detail.type">{{ typeLabel(data.detail.type) }}</span>
        <span class="meta-item"><el-icon><User /></el-icon> {{ data.detail.authorName }}</span>
        <span class="meta-item"><el-icon><Clock /></el-icon> {{ data.detail.createdAt?.replace('T', ' ') }}</span>
        <span v-if="data.detail.examName" class="meta-tag exam-tag">
          <el-icon><Document /></el-icon> {{ data.detail.examName }}
        </span>
      </div>
      <div class="detail-content">{{ data.detail.content }}</div>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, Plus, Edit, Delete, User, Clock, Document, Check, Bell, ChatLineSquare, EditPen, DataLine } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  query: { title: '', type: '' },
  list: [],
  examList: [],
  loading: false,
  dialogVisible: false,
  detailVisible: false,
  isEdit: false,
  form: { id: null, title: '', content: '', type: 'general', priority: 0, examId: null },
  detail: {}
})

const canEdit = computed(() => ['OWNER', 'ADMIN', 'HELPER'].includes(data.user.role))

const examCount = computed(() => data.list.filter(i => i.type === 'exam').length)
const resultCount = computed(() => data.list.filter(i => i.type === 'result').length)

const typeLabel = (type) => ({ general: '公告', exam: '审核', result: '结果', notice: '通知' }[type] || '公告')

const loadList = () => {
  data.loading = true
  const url = canEdit.value ? '/examAnnouncement/selectAll' : '/examAnnouncement/selectPublished'
  request.get(url, { params: data.query }).then(res => {
    if (res.code === '200') data.list = res.data || []
  }).catch(() => {}).finally(() => { data.loading = false })
}

const loadExams = () => {
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') data.examList = res.data || []
  }).catch(() => {})
}

const resetForm = () => ({ id: null, title: '', content: '', type: 'general', priority: 0, examId: null })

const openCreate = () => {
  data.form = resetForm()
  data.isEdit = false
  data.dialogVisible = true
}

const openEdit = (item) => {
  data.form = { ...item }
  data.isEdit = true
  data.dialogVisible = true
}

const saveAnno = () => {
  if (!data.form.title || !data.form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  const url = data.isEdit ? '/examAnnouncement/update' : '/examAnnouncement/add'
  if (!data.isEdit) {
    data.form.authorId = data.user.id
    data.form.authorName = data.user.name
    data.form.status = 'published'
  }
  const req = data.isEdit ? request.put(url, data.form) : request.post(url, data.form)
  req.then(res => {
    if (res.code === '200') {
      ElMessage.success(data.isEdit ? '更新成功' : '发布成功')
      data.dialogVisible = false
      loadList()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(() => {})
}

const deleteAnno = (item) => {
  ElMessageBox.confirm('确定删除此公告？', '提示').then(() => {
    request.delete('/examAnnouncement/delete/' + item.id).then(res => {
      if (res.code === '200') { ElMessage.success('已删除'); loadList() }
      else ElMessage.error(res.msg)
    }).catch(() => {})
  }).catch(() => {})
}

const viewDetail = (item) => {
  data.detail = item
  data.detailVisible = true
}

onMounted(() => { loadList(); if (canEdit.value) loadExams() })
</script>

<style scoped>
.announcements { padding: 0; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #c2410c 0%, #ea580c 50%, #fb923c 100%); border-radius: 16px; padding: 24px 32px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(194, 65, 12, 0.25); }
.hero-left { display: flex; align-items: center; gap: 14px; }
.hero-icon { width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.2); flex-shrink: 0; }
.page-hero h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #fff; }
.page-hero p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 16px 18px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #c2410c, #ea580c); }
.stat-exam .stat-inner { background: linear-gradient(135deg, #0284c7, #38bdf8); }
.stat-result .stat-inner { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.stat-num { font-size: 26px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { display: flex; align-items: center; gap: 8px; }
.s-icon { color: #9ca3af; }

/* ===== 列表卡片 ===== */
.anno-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.anno-card-item { display: flex; align-items: flex-start; gap: 14px; padding: 20px 24px; border-bottom: 1px solid var(--el-border-color-lighter); cursor: pointer; transition: background 0.2s; }
.anno-card-item:last-child { border-bottom: none; }
.anno-card-item:hover { background: #fff7ed; }
.anno-card-left { flex-shrink: 0; }
.anno-type-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.anno-type-icon.general { background: #fef3c7; color: #d97706; }
.anno-type-icon.exam { background: #dbeafe; color: #2563eb; }
.anno-type-icon.result { background: #ede9fe; color: #7c3aed; }
.anno-type-icon.notice { background: #dcfce7; color: #16a34a; }
.anno-card-body { flex: 1; min-width: 0; }
.anno-header-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.anno-title { font-size: 16px; font-weight: 700; color: #1f2937; }
.anno-type-badge { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.anno-type-badge.general { background: #fef3c7; color: #92400e; }
.anno-type-badge.exam { background: #dbeafe; color: #1e40af; }
.anno-type-badge.result { background: #ede9fe; color: #5b21b6; }
.anno-type-badge.notice { background: #dcfce7; color: #14532d; }
.anno-card-meta { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin-bottom: 6px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #9ca3af; }
.meta-tag { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; padding: 2px 8px; border-radius: 6px; }
.exam-tag { background: #dbeafe; color: #1e40af; }
.anno-preview { font-size: 13px; color: #6b7280; line-height: 1.6; }
.anno-card-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

/* 详情 */
.detail-meta { display: flex; gap: 12px; margin-bottom: 16px; font-size: 13px; color: #6b7280; align-items: center; flex-wrap: wrap; }
.detail-content { font-size: 14px; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }

/* 动画 */
.anno-anim-enter-active, .anno-anim-leave-active { transition: all 0.3s; }
.anno-anim-enter-from { opacity: 0; transform: translateY(8px); }
.anno-anim-leave-to { opacity: 0; }
</style>
