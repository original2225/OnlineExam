<template>
  <div class="question-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="#ffffff" stroke-width="2"/>
            <path d="M9 9a3 3 0 1 1 4 2.83V14" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="18" r="0.75" fill="#ffffff"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>题目管理</h1>
          <p>题库维护，支持单选题、多选题、判断题、填空题、简答题</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增题目
        </el-button>
      </div>
    </div>

    <!-- 统计 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">题目总数</div></div></div>
      <div class="stat-card stat-single"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">单选题</div></div></div>
      <div class="stat-card stat-multi"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">多选题</div></div></div>
      <div class="stat-card stat-judge"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">判断题</div></div></div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.content" placeholder="搜索题目内容..." clearable @keyup.enter="handleSearch" @clear="handleSearch" class="search-input" />
        </div>
        <el-select v-model="data.type" placeholder="题型" @change="handleSearch" class="filter-sel" clearable>
          <template #prefix><el-icon><Document /></el-icon></template>
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
          <el-option label="填空题" value="fillin" />
          <el-option label="简答题" value="essay" />
        </el-select>
        <el-cascader v-model="data.categoryId" :options="categoryTree" :props="{ checkStrictly: true, emitPath: false, value: 'value', label: 'label' }" placeholder="分类" @change="handleSearch" class="filter-sel" clearable />
        <el-select v-model="data.difficulty" placeholder="难度" @change="handleSearch" class="filter-sel" clearable>
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 题</span>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="data.tableData" stripe v-loading="data.loading" row-key="id"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        @selection-change="handleSelectionChange" class="q-table">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <span class="type-badge" :class="'type-' + scope.row.type">{{ typeLabel(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="scope"><span class="cat-tag">{{ scope.row.categoryName || '—' }}</span></template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="scope">
            <span class="diff-badge" :class="'diff-' + scope.row.difficulty">{{ diffLabel(scope.row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70">
          <template #default="scope"><span class="score-val">{{ scope.row.score }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-popconfirm title="确定删除？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small"><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap" v-if="data.total">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 初始化按钮 -->
    <div class="init-bar" v-if="!data.total">
      <el-button type="success" size="large" @click="initSample" class="init-btn">
        <el-icon><Lightning /></el-icon> 一键初始化示例题目
      </el-button>
      <span class="init-tip">点击后将初始化四项进服审核示例题目</span>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="data.form.id ? '编辑题目' : '新增题目'" v-model="data.formVisible" width="65%" class="q-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="90px" style="padding: 8px 0">
        <QuestionFormItem :form="data.form" />
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Filter, Lightning, Document } from "@element-plus/icons-vue";
import QuestionFormItem from "@/components/question/QuestionFormItem.vue";
import { loadCascaderCategories } from "@/utils/categoryUtils.js";
import request from "@/utils/request.js";

const data = reactive({
  formVisible: false, form: {}, tableData: [], pageNum: 1, pageSize: 10, total: 0,
  content: null, type: null, categoryId: null, difficulty: null, ids: [], loading: false
})
const categoryTree = ref([])

const loadCategories = () => loadCascaderCategories().then(tree => { categoryTree.value = tree })
const typeLabel = (t) => ({ single: '单选', multiple: '多选', judge: '判断', fillin: '填空', essay: '简答' })[t] || t
const diffLabel = (d) => ({ easy: '简单', medium: '中等', hard: '困难' })[d] || d

const load = () => {
  data.loading = true
  request.get('/question/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, content: data.content, type: data.type, categoryId: data.categoryId, difficulty: data.difficulty } })
    .then(res => { data.loading = false; if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total } })
    .catch(() => { data.loading = false })
}
const handleSearch = () => { data.pageNum = 1; load() }
const handleAdd = () => { data.form = { type: 'single', difficulty: 'medium', score: 2.0, options: { 'A': '', 'B': '', 'C': '', 'D': '' }, images: [] }; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); if (!data.form.options) data.form.options = { 'A': '', 'B': '', 'C': '', 'D': '' }; if (!data.form.images) data.form.images = []; data.formVisible = true }
const save = () => { (data.form.id ? request.put('/question/update', data.form) : request.post('/question/add', data.form)).then(res => { if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() } else ElMessage.error(res.msg) }) }
const del = (id) => { request.delete('/question/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } else ElMessage.error(res.msg) }) }
const delBatch = () => { if (!data.ids.length) return; ElMessageBox.confirm(`确定删除选中的 ${data.ids.length} 道题目吗？`, '批量删除', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(() => { request.delete('/question/delete/batch', { data: data.ids }).then(res => { if (res.code === '200') { ElMessage.success('批量删除成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const initSample = () => { ElMessageBox.confirm('将初始化四项进服审核示例题目，确定继续吗？', '初始化确认', { type: 'info' }).then(() => { request.post('/question/initSample').then(res => { if (res.code === '200') { ElMessage.success('初始化成功'); load(); loadCategories() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const reset = () => { data.content = null; data.type = null; data.categoryId = null; data.difficulty = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }
onMounted(() => { loadCategories(); load() })
</script>

<style scoped>
.question-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #dc2626 0%, #ef4444 50%, #f87171 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(220,38,38,0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 20px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #dc2626, #ef4444); }
.stat-single .stat-inner { background: linear-gradient(135deg, #10b981, #34d399); }
.stat-multi .stat-inner { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.stat-judge .stat-inner { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 140px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; }
.batch-del { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #dc2626; }

/* ===== 表格 ===== */
.table-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.q-table :deep(.el-table__row:hover > td) { background: #fef2f2 !important; }
.type-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.type-single { background: #dcfce7; color: #16a34a; }
.type-multiple { background: #dbeafe; color: #2563eb; }
.type-judge { background: #fef3c7; color: #d97706; }
.type-fillin { background: #ede9fe; color: #7c3aed; }
.type-essay { background: #fce7f3; color: #db2777; }
.cat-tag { display: inline-block; padding: 2px 8px; background: #f1f5f9; color: #475569; border-radius: 6px; font-size: 12px; }
.diff-badge { display: inline-block; padding: 2px 8px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.diff-easy { background: #dcfce7; color: #16a34a; }
.diff-medium { background: #fef3c7; color: #d97706; }
.diff-hard { background: #fee2e2; color: #dc2626; }
.score-val { font-weight: 700; color: #dc2626; }
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* ===== 初始化 ===== */
.init-bar { display: flex; align-items: center; gap: 14px; margin-top: 16px; padding: 20px 24px; background: #fff; border-radius: 14px; border: 1px solid var(--el-border-color-lighter); }
.init-btn { border-radius: 10px !important; }
.init-tip { font-size: 13px; color: #9ca3af; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .toolbar { flex-direction: column; align-items: flex-start; } }
</style>
