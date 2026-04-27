<template>
  <div class="notice-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>公告管理</h1>
          <p>发布与管理系统公告，支持富文本内容与优先级排序</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 发布公告
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">公告总数</div></div></div>
      <div class="stat-card stat-active"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">已发布</div></div></div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.title" placeholder="搜索公告标题..." clearable @keyup.enter="load" @clear="load" class="search-input" />
        </div>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 条公告</span>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="notice-list-card">
      <div v-loading="data.loading">
        <transition-group name="notice-anim">
          <div v-for="row in data.tableData" :key="row.id" class="notice-card-item">
            <div class="notice-icon-wrap">
              <div class="notice-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="notice-body">
              <div class="notice-title-row">
                <h3 class="notice-title">{{ row.title }}</h3>
              </div>
              <div class="notice-content-preview">{{ row.content }}</div>
              <div class="notice-footer">
                <span class="notice-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(row.time) }}
                </span>
              </div>
            </div>
            <div class="notice-actions">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-popconfirm title="确定删除该公告？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </transition-group>

        <!-- 空状态 -->
        <div v-if="!data.loading && data.tableData.length === 0" class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#f1f5f9"/>
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p>暂无公告内容</p>
          <el-button type="primary" size="small" @click="handleAdd">发布第一条公告</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="data.total">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 公告表单弹窗 -->
    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑公告' : '发布公告'" width="560px" class="notice-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 8px 0">
        <el-form-item label="公告标题">
          <el-input v-model="data.form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="data.form.content" type="textarea" :rows="6" placeholder="请输入公告内容（支持富文本）" class="content-textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认发布</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Clock } from "@element-plus/icons-vue";
import request from "@/utils/request.js";

const data = reactive({
  loading: false, formVisible: false, form: {}, tableData: [],
  pageNum: 1, pageSize: 10, total: 0, title: null, ids: []
})

const formatTime = (val) => {
  if (!val) return '—'
  return String(val).replace('T', ' ').substring(0, 19)
}

const load = () => {
  data.loading = true
  request.get('/notice/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, title: data.title || undefined } })
    .then(res => { data.loading = false; if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total || 0 } })
    .catch(() => { data.loading = false })
}

const handleAdd = () => { data.form = {}; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); data.formVisible = true }
const save = () => {
  const action = data.form.id ? request.put('/notice/update', data.form) : request.post('/notice/add', data.form)
  action.then(res => { if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() } else ElMessage.error(res.msg) })
}
const del = (id) => { request.delete('/notice/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } else ElMessage.error(res.msg) }) }
const reset = () => { data.title = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }
onMounted(() => { load() })
</script>

<style scoped>
.notice-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #0369a1 0%, #0ea5e9 50%, #38bdf8 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(3, 105, 161, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); color: #fff; }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; margin-bottom: 20px; max-width: 500px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 22px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #0369a1, #0ea5e9); }
.stat-active .stat-inner { background: linear-gradient(135deg, #059669, #10b981); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 260px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.reset-btn { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #0369a1; }

/* ===== 公告列表卡片 ===== */
.notice-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.notice-card-item { display: flex; align-items: flex-start; gap: 16px; padding: 20px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.notice-card-item:last-child { border-bottom: none; }
.notice-card-item:hover { background: #f0f9ff; }
.notice-icon-wrap { flex-shrink: 0; }
.notice-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #0369a1, #0ea5e9); color: #fff; display: flex; align-items: center; justify-content: center; }
.notice-body { flex: 1; min-width: 0; }
.notice-title-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.notice-title { margin: 0; font-size: 16px; font-weight: 700; color: #1f2937; cursor: pointer; transition: color 0.2s; }
.notice-title:hover { color: #0369a1; }
.notice-content-preview { font-size: 13px; color: #6b7280; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 10px; line-height: 1.6; }
.notice-footer { display: flex; align-items: center; gap: 16px; }
.notice-time { font-size: 12px; color: #9ca3af; display: flex; align-items: center; gap: 4px; }
.notice-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin: 16px 0 12px; color: #9ca3af; font-size: 14px; }

/* 分页 */
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 弹窗 */
.content-textarea :deep(.el-textarea__inner) { border-radius: 10px; font-size: 14px; line-height: 1.8; }

/* 动画 */
.notice-anim-enter-active, .notice-anim-leave-active { transition: all 0.3s; }
.notice-anim-enter-from { opacity: 0; transform: translateY(10px); }
.notice-anim-leave-to { opacity: 0; }

@media (max-width: 600px) { .notice-card-item { flex-wrap: wrap; } .notice-actions { flex-direction: row; } }
</style>
