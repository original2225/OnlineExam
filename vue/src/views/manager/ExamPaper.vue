<template>
  <div class="paper-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>试卷管理</h1>
          <p>创建与管理审核试卷，支持手动组卷与随机组卷两种模式</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新建试卷
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">试卷总数</div></div></div>
      <div class="stat-card stat-manual"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">手动组卷</div></div></div>
      <div class="stat-card stat-random"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">随机组卷</div></div></div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.name" placeholder="搜索试卷名称..." clearable @keyup.enter="load" @clear="load" class="search-input" />
        </div>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 份试卷</span>
      </div>
    </div>

    <!-- 试卷卡片列表 -->
    <div class="paper-list-card">
      <div v-loading="data.loading">
        <transition-group name="paper-anim">
          <div v-for="row in data.tableData" :key="row.id" class="paper-card-item">
            <div class="paper-card-left">
              <div class="paper-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
            </div>
            <div class="paper-body">
              <div class="paper-title-row">
                <h3 class="paper-title" @click="handleEdit(row)">{{ row.name }}</h3>
                <span class="paper-type-badge" :class="'type-' + row.type">{{ row.type === 'random' ? '随机' : '手动' }}</span>
              </div>
              <div class="paper-meta">
                <span class="meta-item">
                  <el-icon><Document /></el-icon>
                  {{ row.questionCount || 0 }} 题
                </span>
                <span class="meta-sep">·</span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ row.totalTime || 0 }} 分钟
                </span>
                <span class="meta-sep">·</span>
                <span class="meta-item">
                  <el-icon><Star /></el-icon>
                  满分 {{ row.totalScore || 0 }} 分
                </span>
                <span class="meta-sep">·</span>
                <span class="meta-item pass">
                  <el-icon><CircleCheck /></el-icon>
                  及格 {{ row.passScore || 0 }} 分
                </span>
              </div>
              <div class="paper-desc" v-if="row.description">{{ row.description }}</div>
            </div>
            <div class="paper-actions">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="success" link size="small" @click="handleQuestions(row)">
                <el-icon><SetUp /></el-icon> 题目
              </el-button>
              <el-popconfirm title="确定删除该试卷？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(row.id)">
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
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M14 2v6h6" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p>暂无试卷内容</p>
          <el-button type="primary" size="small" @click="handleAdd">创建第一份试卷</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="data.total">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 试卷表单对话框 -->
    <el-dialog :title="data.form.id ? '编辑试卷' : '新建试卷'" v-model="data.formVisible" width="50%" class="paper-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding: 8px 0">
        <el-form-item label="试卷名称">
          <el-input v-model="data.form.name" placeholder="请输入试卷名称" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="试卷描述">
          <el-input v-model="data.form.description" type="textarea" :rows="2" placeholder="请输入试卷描述" />
        </el-form-item>
        <el-form-item label="试卷类型">
          <el-radio-group v-model="data.form.type">
            <el-radio value="manual">
              <span class="radio-label">手动组卷</span>
              <span class="radio-desc">手动选择题目组合</span>
            </el-radio>
            <el-radio value="random">
              <span class="radio-label">随机组卷</span>
              <span class="radio-desc">按规则随机抽取</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="data.form.totalScore" :min="0" :max="1000" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="审核时长(分钟)">
          <el-input-number v-model="data.form.totalTime" :min="1" :max="600" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="及格分">
          <el-input-number v-model="data.form.passScore" :min="0" :max="100" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 题目设置对话框 -->
    <el-dialog title="设置试卷题目" v-model="data.questionDialogVisible" width="78%" class="q-dialog" destroy-on-close>
      <div class="q-toolbar">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.questionSearch" placeholder="搜索题目内容..." clearable @keyup.enter="loadQuestions" class="search-input" />
        </div>
        <el-select v-model="data.questionType" placeholder="题型" @change="loadQuestions" class="filter-sel" clearable>
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
          <el-option label="填空题" value="fillin" />
          <el-option label="简答题" value="essay" />
        </el-select>
        <el-button plain @click="resetQuestionSearch" class="reset-btn">重置</el-button>
      </div>
      <div class="q-add-bar">
        <el-button type="success" plain :disabled="!data.selectedQuestions.length" @click="addSelectedQuestions">
          <el-icon><Plus /></el-icon> 添加选中题目
        </el-button>
        <span class="total-tip">已选 <strong>{{ data.selectedQuestions.length }}</strong> 道题</span>
      </div>
      <el-table stripe :data="data.questionList" @selection-change="handleQuestionSelection" max-height="380"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="type" label="题型" width="90">
          <template #default="scope">
            <span class="type-badge" :class="'type-' + scope.row.type">{{ typeLabel(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80">
          <template #default="scope"><span class="score-val">{{ scope.row.score }}</span></template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background @size-change="loadQuestions" @current-change="loadQuestions"
          :current-page="data.questionPageNum" :page-size="data.questionPageSize"
          layout="total, prev, pager, next" :total="data.questionTotal" />
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Document, Clock, Star, CircleCheck, SetUp } from "@element-plus/icons-vue";

const data = reactive({
  loading: false, formVisible: false, questionDialogVisible: false, form: {}, tableData: [],
  pageNum: 1, pageSize: 10, total: 0, name: null, ids: [],
  currentPaperId: null, questionList: [], questionPageNum: 1, questionPageSize: 10, questionTotal: 0, questionSearch: null, questionType: null, selectedQuestions: []
})

const typeLabel = (t) => ({ single: '单选', multiple: '多选', judge: '判断', fillin: '填空', essay: '简答' })[t] || t

const load = () => {
  data.loading = true
  request.get('/examPaper/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, name: data.name || undefined } })
    .then(res => { data.loading = false; if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total } })
    .catch(() => { data.loading = false })
}
const handleAdd = () => { data.form = { totalScore: 100, totalTime: 120, passScore: 60, type: 'manual' }; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); data.formVisible = true }
const save = () => { (data.form.id ? request.put('/examPaper/update', data.form) : request.post('/examPaper/add', data.form)).then(res => { if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() } else ElMessage.error(res.msg) }) }
const del = (id) => { ElMessageBox.confirm('删除后数据无法恢复，确定删除吗？', '删除确认', { type: 'warning' }).then(() => { request.delete('/examPaper/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const delBatch = () => { if (!data.ids.length) return; ElMessageBox.confirm('删除后数据无法恢复，确定批量删除吗？', '批量删除', { type: 'warning' }).then(() => { request.delete('/examPaper/delete/batch', { data: data.ids }).then(res => { if (res.code === '200') { ElMessage.success('批量删除成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const reset = () => { data.name = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }

const handleQuestions = (row) => { data.currentPaperId = row.id; data.questionDialogVisible = true; data.questionPageNum = 1; data.selectedQuestions = []; loadQuestions() }
const loadQuestions = () => { request.get('/question/selectPage', { params: { pageNum: data.questionPageNum, pageSize: data.questionPageSize, content: data.questionSearch || undefined, type: data.questionType || undefined, status: 'active' } }).then(res => { if (res.code === '200') { data.questionList = res.data?.list || []; data.questionTotal = res.data?.total } }) }
const resetQuestionSearch = () => { data.questionSearch = null; data.questionType = null; loadQuestions() }
const handleQuestionSelection = (rows) => { data.selectedQuestions = rows }
const addSelectedQuestions = () => { if (!data.selectedQuestions.length) { ElMessage.warning('请选择题目'); return }; request.post('/examPaper/addQuestions?paperId=' + data.currentPaperId, data.selectedQuestions.map(q => q.id)).then(res => { if (res.code === '200') { ElMessage.success('添加成功'); data.selectedQuestions = []; load() } else ElMessage.error(res.msg) }) }

onMounted(() => { load() })
</script>

<style scoped>
.paper-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #0f766e 0%, #14b8a6 50%, #2dd4bf 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(15, 118, 110, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 20px; max-width: 500px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 22px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #0f766e, #14b8a6); }
.stat-manual .stat-inner { background: linear-gradient(135deg, #0891b2, #22d3ee); }
.stat-random .stat-inner { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 120px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; }
.batch-del { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #0f766e; }

/* ===== 试卷列表卡片 ===== */
.paper-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.paper-card-item { display: flex; align-items: flex-start; gap: 16px; padding: 20px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.paper-card-item:last-child { border-bottom: none; }
.paper-card-item:hover { background: #f0fdfa; }
.paper-card-left { flex-shrink: 0; }
.paper-icon { width: 48px; height: 48px; border-radius: 12px; background: linear-gradient(135deg, #0f766e, #14b8a6); color: #fff; display: flex; align-items: center; justify-content: center; }
.paper-body { flex: 1; min-width: 0; }
.paper-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.paper-title { margin: 0; font-size: 16px; font-weight: 700; color: #1f2937; cursor: pointer; transition: color 0.2s; }
.paper-title:hover { color: #0f766e; }
.paper-type-badge { display: inline-block; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 700; }
.type-manual { background: #dbeafe; color: #2563eb; }
.type-random { background: #ede9fe; color: #7c3aed; }
.paper-meta { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 3px; font-size: 13px; color: #6b7280; }
.meta-item .el-icon { font-size: 13px; }
.meta-item.pass { color: #059669; }
.meta-sep { color: #d1d5db; }
.paper-desc { font-size: 12px; color: #9ca3af; margin-top: 6px; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.paper-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }

/* 空状态 */
.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin: 16px 0 12px; color: #9ca3af; font-size: 14px; }

/* 分页 */
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 题目设置 */
.q-toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.q-add-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.type-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.type-single { background: #dcfce7; color: #16a34a; }
.type-multiple { background: #dbeafe; color: #2563eb; }
.type-judge { background: #fef3c7; color: #d97706; }
.type-fillin { background: #ede9fe; color: #7c3aed; }
.type-essay { background: #fce7f3; color: #db2777; }
.score-val { font-weight: 700; color: #0f766e; }

/* 弹窗 */
.radio-label { font-weight: 600; margin-right: 4px; }
.radio-desc { font-size: 12px; color: #9ca3af; margin-left: 4px; }

/* 动画 */
.paper-anim-enter-active, .paper-anim-leave-active { transition: all 0.3s; }
.paper-anim-enter-from { opacity: 0; transform: translateY(10px); }
.paper-anim-leave-to { opacity: 0; }

@media (max-width: 700px) { .paper-card-item { flex-wrap: wrap; } .paper-actions { flex-direction: row; } }
</style>
