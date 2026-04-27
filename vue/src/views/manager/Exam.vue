<template>
  <div class="exam-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            <rect x="9" y="3" width="6" height="4" rx="1" stroke="#ffffff" stroke-width="2"/>
            <path d="M9 12h6M9 16h4" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>审核管理</h1>
          <p>创建与管理入服审核，支持统一审核与常驻审核两种模式</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新建审核
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">审核总数</div></div></div>
      <div class="stat-card stat-published"><div class="stat-inner"><div class="stat-num">{{ publishedCount }}</div><div class="stat-label">已发布</div></div></div>
      <div class="stat-card stat-ongoing"><div class="stat-inner"><div class="stat-num">{{ ongoingCount }}</div><div class="stat-label">进行中</div></div></div>
      <div class="stat-card stat-finished"><div class="stat-inner"><div class="stat-num">{{ finishedCount }}</div><div class="stat-label">已结束</div></div></div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.name" placeholder="搜索审核名称..." clearable @keyup.enter="load" @clear="load" class="search-input" />
        </div>
        <el-select v-model="data.status" placeholder="状态" @change="load" class="filter-sel" clearable>
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已结束" value="finished" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-select v-model="data.examType" placeholder="审核类型" @change="load" class="filter-sel" clearable>
          <template #prefix><el-icon><Document /></el-icon></template>
          <el-option label="统一审核" value="scheduled" />
          <el-option label="常驻审核" value="permanent" />
        </el-select>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch" class="batch-del">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
        <span class="total-tip">共 <strong>{{ data.total }}</strong> 场审核</span>
      </div>
    </div>

    <!-- 审核列表卡片 -->
    <div class="table-card">
      <el-table :data="data.tableData" stripe v-loading="data.loading" row-key="id"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        @selection-change="handleSelectionChange" class="exam-table">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="审核名称" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span class="exam-name" @click="handleEdit(scope.row)">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paperName" label="试卷" width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="scope">
            <span class="type-pill" :class="'type-' + scope.row.examType">
              {{ scope.row.examType === 'permanent' ? '常驻' : '统一' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="scope">
            <span class="time-str">{{ scope.row.startTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="75">
          <template #default="scope"><span class="dur-val">{{ scope.row.duration }}分钟</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <span class="status-badge" :class="'status-' + scope.row.status">{{ statusLabel(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未批阅" width="90">
          <template #default="scope">
            <span v-if="scope.row.ungradedCount > 0" class="ungraded-pill">{{ scope.row.ungradedCount }}份</span>
            <span v-else class="graded-ok">已阅</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-button type="success" link size="small" @click="handlePermissions(scope.row)"><el-icon><Key /></el-icon> 权限</el-button>
            <el-button v-if="scope.row.examType === 'scheduled'" type="warning" link size="small" @click="handleMakeup(scope.row)"><el-icon><RefreshRight /></el-icon> 补考</el-button>
            <el-button v-if="scope.row.status === 'draft'" type="primary" link size="small" @click="publishExam(scope.row.id)">发布</el-button>
            <el-button v-else-if="scope.row.status === 'published'" type="danger" link size="small" @click="cancelExam(scope.row.id)">取消</el-button>
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
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 审核表单对话框 -->
    <el-dialog :title="data.form.id ? '编辑审核' : '新建审核'" v-model="data.formVisible" width="55%" class="exam-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="110px" style="padding: 8px 0">
        <el-form-item label="审核名称">
          <el-input v-model="data.form.name" placeholder="请输入审核名称" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="审核类型">
          <el-radio-group v-model="data.form.examType">
            <el-radio value="scheduled">
              <span class="radio-label">统一审核</span>
              <span class="radio-desc">规定开始结束时间</span>
            </el-radio>
            <el-radio value="permanent">
              <span class="radio-label">常驻审核</span>
              <span class="radio-desc">玩家随时可参加</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择试卷">
          <el-select v-model="data.form.paperId" placeholder="请选择试卷" style="width: 100%">
            <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分支">
          <el-select v-model="data.form.branch" placeholder="请选择分支" style="width: 100%">
            <el-option label="后勤见习" value="后勤见习" />
            <el-option label="红石见习" value="红石见习" />
            <el-option label="建筑" value="建筑" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核描述">
          <el-input v-model="data.form.description" type="textarea" :rows="2" placeholder="请输入审核描述" />
        </el-form-item>
        <template v-if="data.form.examType === 'scheduled'">
          <el-form-item label="开始时间">
            <el-date-picker v-model="data.form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="data.form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </template>
        <el-form-item label="审核时长(分钟)">
          <el-input-number v-model="data.form.duration" :min="1" :max="600" :step="10" style="width: 100%" />
        </el-form-item>
        <template v-if="data.form.examType === 'scheduled'">
          <el-form-item label="允许迟到">
            <el-switch v-model="data.form.allowLate" />
          </el-form-item>
          <el-form-item v-if="data.form.allowLate" label="迟到时间(分钟)">
            <el-input-number v-model="data.form.lateTime" :min="1" :max="120" :step="5" style="width: 100%" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog title="设置审核权限" v-model="data.permissionDialogVisible" width="65%" destroy-on-close>
      <div class="perm-toolbar">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.studentSearch" placeholder="搜索玩家姓名或编号..." clearable @keyup.enter="loadStudents" class="search-input" />
        </div>
        <el-button type="info" plain @click="loadStudents">查询</el-button>
        <el-button type="warning" plain @click="resetStudentSearch">重置</el-button>
      </div>
      <div class="perm-info">
        <el-icon><InfoFilled /></el-icon>
        当前有 <strong>{{ data.currentPermissions.length }}</strong> 名玩家有权限参加审核
      </div>
      <div class="perm-add">
        <el-button type="success" plain :disabled="!data.selectedStudents.length" @click="addSelectedStudents">
          <el-icon><Plus /></el-icon> 添加选中玩家
        </el-button>
      </div>
      <el-table stripe :data="data.studentList" @selection-change="handleStudentSelection" max-height="380">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="studentNo" label="编号" width="120" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="className" label="分组" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background @current-change="loadStudents"
          :current-page="data.studentPageNum" :page-size="data.studentPageSize"
          layout="total, prev, pager, next" :total="data.studentTotal" />
      </div>
    </el-dialog>

    <!-- 补考设置对话框 -->
    <el-dialog title="开放补考" v-model="data.makeupDialogVisible" width="60%" destroy-on-close>
      <div class="makeup-tip">
        <el-icon><WarningFilled /></el-icon>
        <div>
          <div class="tip-title">补考说明</div>
          <div class="tip-desc">将基于「{{ data.makeupExam?.name }}」的试卷创建补审，选择的玩家可以参加</div>
        </div>
      </div>
      <el-form label-width="90px" style="margin-top: 16px;">
        <el-form-item label="补考时间">
          <el-date-picker v-model="data.makeupStartTime" type="datetime" placeholder="开始时间" style="width: 38%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
          <span style="margin: 0 10px; color: #999;">至</span>
          <el-date-picker v-model="data.makeupEndTime" type="datetime" placeholder="结束时间" style="width: 38%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="审核时长">
          <el-input-number v-model="data.makeupDuration" :min="5" :max="600" :step="10" style="width: 38%" />
          <span style="margin-left: 8px; color: #999;">分钟</span>
        </el-form-item>
      </el-form>
      <el-divider content-position="left"><el-icon><User /></el-icon> 选择补审玩家</el-divider>
      <div style="margin-bottom: 10px;">
        <div class="search-wrap" style="width: 220px;">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.makeupStudentSearch" placeholder="搜索姓名或编号..." clearable @keyup.enter="loadMakeupStudents" class="search-input" />
        </div>
        <el-button type="info" plain @click="loadMakeupStudents">查询</el-button>
      </div>
      <el-table stripe :data="data.makeupStudentList" @selection-change="handleMakeupStudentSelection" max-height="280">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentNo" label="编号" width="100" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="branch" label="方向" width="100" />
      </el-table>
      <div style="margin-top: 10px;" v-if="data.makeupStudentTotal > data.makeupStudentPageSize">
        <el-pagination background @current-change="loadMakeupStudents"
          :current-page="data.makeupStudentPageNum" :page-size="data.makeupStudentPageSize"
          layout="total, prev, pager, next" :total="data.makeupStudentTotal" />
      </div>
      <template #footer>
        <el-button @click="data.makeupDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitMakeup" :disabled="!data.makeupSelectedStudents.length">
          确认开放补考（已选 {{ data.makeupSelectedStudents.length }} 人）
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Filter, Document, Key, RefreshRight, InfoFilled, WarningFilled, User } from "@element-plus/icons-vue";

const data = reactive({
  loading: false,
  formVisible: false,
  permissionDialogVisible: false,
  form: {},
  tableData: [],
  pageNum: 1, pageSize: 10, total: 0,
  name: null, status: null, examType: null,
  ids: [],
  papers: [],
  currentExamId: null,
  studentList: [], studentPageNum: 1, studentPageSize: 10, studentTotal: 0, studentSearch: null, selectedStudents: [], currentPermissions: [],
  makeupDialogVisible: false, makeupExam: null, makeupStartTime: null, makeupEndTime: null, makeupDuration: 30,
  makeupStudentList: [], makeupStudentPageNum: 1, makeupStudentPageSize: 10, makeupStudentTotal: 0, makeupStudentSearch: null, makeupSelectedStudents: [],
})

const papers = computed(() => data.papers)

const publishedCount = computed(() => data.tableData.filter(r => r.status === 'published').length)
const ongoingCount = computed(() => data.tableData.filter(r => r.status === 'ongoing').length)
const finishedCount = computed(() => data.tableData.filter(r => r.status === 'finished').length)

const statusLabel = (s) => ({ draft: '草稿', published: '已发布', ongoing: '进行中', finished: '已结束', cancelled: '已取消' })[s] || s

const load = () => {
  data.loading = true
  request.get('/exam/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, name: data.name || undefined, status: data.status || undefined, examType: data.examType || undefined } })
    .then(res => { data.loading = false; if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total } })
    .catch(() => { data.loading = false })
}

const loadPapers = () => { request.get('/examPaper/selectAll').then(res => { if (res.code === '200') data.papers = res.data || [] }) }

const handleAdd = () => { data.form = { duration: 120, allowLate: false, examType: 'scheduled' }; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); if (!data.form.examType) data.form.examType = 'scheduled'; data.formVisible = true }
const save = () => { (data.form.id ? request.put('/exam/update', data.form) : request.post('/exam/add', data.form)).then(res => { if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() } else ElMessage.error(res.msg) }) }
const del = (id) => { ElMessageBox.confirm('删除后数据无法恢复，确定删除吗？', '删除确认', { type: 'warning' }).then(() => { request.delete('/exam/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const delBatch = () => { if (!data.ids.length) return; ElMessageBox.confirm('删除后数据无法恢复，确定删除选中的审核吗？', '批量删除', { type: 'warning' }).then(() => { request.delete('/exam/delete/batch', { data: data.ids }).then(res => { if (res.code === '200') { ElMessage.success('批量删除成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const reset = () => { data.name = null; data.status = null; data.examType = null; data.pageNum = 1; load() }
const handleSizeChange = (size) => { data.pageSize = size; load() }
const handleCurrentChange = (page) => { data.pageNum = page; load() }
const publishExam = (id) => { ElMessageBox.confirm('发布后玩家将可以看到并参加审核，确定发布吗？', '发布确认', { type: 'warning' }).then(() => { request.put('/exam/publish/' + id).then(res => { if (res.code === '200') { ElMessage.success('发布成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }
const cancelExam = (id) => { ElMessageBox.confirm('取消后玩家将无法参加审核，确定取消吗？', '取消确认', { type: 'warning' }).then(() => { request.put('/exam/cancel/' + id).then(res => { if (res.code === '200') { ElMessage.success('取消成功'); load() } else ElMessage.error(res.msg) }) }).catch(() => {}) }

const handlePermissions = (row) => { data.currentExamId = row.id; data.permissionDialogVisible = true; data.studentPageNum = 1; data.selectedStudents = []; loadStudents(); loadCurrentPermissions() }
const loadStudents = () => { request.get('/student/selectPage', { params: { pageNum: data.studentPageNum, pageSize: data.studentPageSize, name: data.studentSearch } }).then(res => { if (res.code === '200') { data.studentList = res.data?.list || []; data.studentTotal = res.data?.total } }) }
const resetStudentSearch = () => { data.studentSearch = null; loadStudents() }
const handleStudentSelection = (rows) => { data.selectedStudents = rows }
const loadCurrentPermissions = () => { request.get('/exam/getPermissions/' + data.currentExamId).then(res => { if (res.code === '200') data.currentPermissions = res.data || [] }) }
const addSelectedStudents = () => { if (!data.selectedStudents.length) { ElMessage.warning('请选择要添加的玩家'); return }; const studentIds = data.selectedStudents.map(s => s.id); request.post('/exam/setPermissions?examId=' + data.currentExamId, studentIds).then(res => { if (res.code === '200') { ElMessage.success('添加成功'); data.selectedStudents = []; loadCurrentPermissions() } else ElMessage.error(res.msg) }) }

const handleMakeup = (row) => { data.makeupExam = row; data.makeupDialogVisible = true; data.makeupStartTime = null; data.makeupEndTime = null; data.makeupDuration = row.duration || 30; data.makeupSelectedStudents = []; data.makeupStudentSearch = null; data.makeupStudentPageNum = 1; loadMakeupStudents() }
const loadMakeupStudents = () => { request.get('/student/selectPage', { params: { pageNum: data.makeupStudentPageNum, pageSize: data.makeupStudentPageSize, name: data.makeupStudentSearch } }).then(res => { if (res.code === '200') { data.makeupStudentList = res.data?.list || []; data.makeupStudentTotal = res.data?.total } }) }
const handleMakeupStudentSelection = (rows) => { data.makeupSelectedStudents = rows }
const submitMakeup = () => { if (!data.makeupSelectedStudents.length) { ElMessage.warning('请选择补审玩家'); return }; if (!data.makeupStartTime || !data.makeupEndTime) { ElMessage.warning('请设置补审时间'); return }; const studentIds = data.makeupSelectedStudents.map(s => s.id); request.post('/exam/makeup', { originalExamId: data.makeupExam.id, studentIds, startTime: data.makeupStartTime, endTime: data.makeupEndTime, duration: data.makeupDuration }).then(res => { if (res.code === '200') { ElMessage.success(`补审已开放，共 ${res.data?.studentCount || studentIds.length} 名玩家`); data.makeupDialogVisible = false; load() } else ElMessage.error(res.msg || '操作失败') }) }

onMounted(() => { loadPapers(); load() })
</script>

<style scoped>
.exam-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #4338ca 0%, #6366f1 50%, #818cf8 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(67, 56, 202, 0.25); }
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
.stat-total .stat-inner { background: linear-gradient(135deg, #4338ca, #6366f1); }
.stat-published .stat-inner { background: linear-gradient(135deg, #059669, #10b981); }
.stat-ongoing .stat-inner { background: linear-gradient(135deg, #d97706, #f59e0b); }
.stat-finished .stat-inner { background: linear-gradient(135deg, #6b7280, #9ca3af); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 130px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; }
.batch-del { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #4338ca; }

/* ===== 表格 ===== */
.table-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.exam-table :deep(.el-table__row:hover > td) { background: #eef2ff !important; }
.exam-name { font-weight: 600; color: #4338ca; cursor: pointer; transition: color 0.2s; }
.exam-name:hover { color: #6366f1; }
.type-pill { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.type-scheduled { background: #dbeafe; color: #2563eb; }
.type-permanent { background: #dcfce7; color: #16a34a; }
.time-str { font-size: 13px; color: #6b7280; }
.dur-val { font-weight: 600; color: #6b7280; }
.status-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-draft { background: #f1f5f9; color: #6b7280; }
.status-published { background: #dcfce7; color: #16a34a; }
.status-ongoing { background: #fef3c7; color: #d97706; }
.status-finished { background: #e5e7eb; color: #6b7280; }
.status-cancelled { background: #fee2e2; color: #dc2626; }
.ungraded-pill { background: #fef3c7; color: #d97706; padding: 2px 8px; border-radius: 8px; font-size: 12px; font-weight: 600; }
.graded-ok { color: #9ca3af; font-size: 12px; }
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--el-border-color-lighter); }

/* 权限弹窗 */
.perm-toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.perm-info { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 10px; padding: 10px 14px; margin-bottom: 12px; font-size: 13px; color: #0369a1; display: flex; align-items: center; gap: 6px; }
.perm-info strong { color: #0ea5e9; }
.perm-add { margin-bottom: 12px; }

/* 补考弹窗 */
.makeup-tip { background: #fffbeb; border: 1px solid #fde68a; border-radius: 12px; padding: 14px 16px; display: flex; align-items: flex-start; gap: 10px; color: #92400e; }
.makeup-tip .el-icon { color: #d97706; margin-top: 2px; flex-shrink: 0; }
.tip-title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.tip-desc { font-size: 13px; color: #b45309; }

/* 单选样式 */
.radio-label { font-weight: 600; margin-right: 4px; }
.radio-desc { font-size: 12px; color: #9ca3af; margin-left: 4px; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .toolbar { flex-direction: column; align-items: flex-start; } }
</style>
