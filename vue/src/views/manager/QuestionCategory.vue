<template>
  <div class="cat-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>题目分类</h1>
          <p>管理题目分类体系，支持树形结构与多级分类</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" size="large" round @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增分类
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ totalCount }}</div><div class="stat-label">分类总数</div></div></div>
      <div class="stat-card stat-root"><div class="stat-inner"><div class="stat-num">{{ rootCount }}</div><div class="stat-label">顶级分类</div></div></div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-input v-model="data.name" placeholder="搜索分类名称..." clearable @keyup.enter="load" @clear="load" class="search-input" />
        </div>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
    </div>

    <!-- 分类表格 -->
    <div class="table-card">
      <el-table :data="data.tableData" stripe v-loading="data.loading" row-key="id"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        @selection-change="handleSelectionChange" class="cat-table"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all>
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="180">
          <template #default="scope">
            <div class="cat-name-cell">
              <div class="cat-icon-wrap">
                <img v-if="scope.row.icon" :src="scope.row.icon" class="cat-icon-img" />
                <div v-else class="cat-icon-default">
                  <el-icon><Folder /></el-icon>
                </div>
              </div>
              <span class="cat-name-text">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="分类描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <span class="status-pill" :class="scope.row.status === 'active' ? 'active' : 'inactive'">
              {{ scope.row.status === 'active' ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="子分类" width="90" align="center">
          <template #default="scope">
            <span class="child-count" v-if="scope.row.children && scope.row.children.length">
              {{ scope.row.children.length }} 个
            </span>
            <span class="child-count zero" v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-popconfirm title="确定删除该分类？" confirm-button-text="删除" cancel-button-text="取消" @confirm="del(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small"><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="data.form.id ? '编辑分类' : '新增分类'" v-model="data.formVisible" width="500px" class="cat-dialog" destroy-on-close>
      <el-form :model="data.form" label-width="85px" style="padding: 8px 0">
        <el-form-item label="分类名称">
          <el-input v-model="data.form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="父分类">
          <el-cascader v-model="data.form.parentId" :options="parentCategoryOptions" :props="{ checkStrictly: true, emitPath: false, value: 'value', label: 'label' }" placeholder="无（顶级分类）" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="data.form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="分类图标">
          <div style="display: flex; align-items: center; gap: 12px;">
            <el-upload :action="baseUrl + '/files/upload'" :headers="uploadHeaders" :show-file-list="false" :on-success="handleIconUpload" accept="image/*">
              <div v-if="data.form.icon" style="position: relative; width: 64px; height: 64px;">
                <img :src="data.form.icon" style="width: 64px; height: 64px; border-radius: 12px; object-fit: cover; border: 2px solid #e4e7ed;" />
                <div class="icon-replace-hint">更换</div>
              </div>
              <el-button v-else size="small" type="primary" plain>
                <el-icon><Upload /></el-icon> 上传图标
              </el-button>
            </el-upload>
            <el-button v-if="data.form.icon" size="small" type="danger" text @click="data.form.icon = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="data.form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">停用</el-radio>
          </el-radio-group>
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
import { reactive, ref, computed, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search, Folder, Upload } from "@element-plus/icons-vue";
import { loadCascaderCategories } from "@/utils/categoryUtils.js";
import { getUploadHeaders } from '@/utils/upload.js'

const baseUrl = import.meta.env.VITE_BASE_URL
const uploadHeaders = getUploadHeaders()

const data = reactive({
  formVisible: false, form: {}, tableData: [], name: null, ids: [], loading: false
})
const parentCategoryOptions = ref([])

const totalCount = computed(() => {
  let count = 0
  const countNodes = (nodes) => { for (const n of nodes) { count++; if (n.children) countNodes(n.children) } }
  countNodes(data.tableData)
  return count
})
const rootCount = computed(() => data.tableData.length)

const filterTree = (tree, keyword) => {
  const result = []
  for (const node of tree) {
    const match = node.name && node.name.includes(keyword)
    const children = node.children ? filterTree(node.children, keyword) : []
    if (match || children.length > 0) result.push({ ...node, children: children.length > 0 ? children : (match ? node.children : []) })
  }
  return result
}

const load = () => {
  data.loading = true
  request.get('/questionCategory/selectTree', { params: { name: data.name || null } }).then(res => {
    data.loading = false
    if (res.code === '200') {
      let tree = res.data || []
      if (data.name) tree = filterTree(tree, data.name)
      data.tableData = tree
    }
  }).catch(() => { data.loading = false })
}

const loadParentOptions = (excludeId) => {
  loadCascaderCategories().then(tree => {
    if (excludeId) {
      const filterExclude = (nodes, id) => nodes.filter(n => n.value !== id).map(n => ({ ...n, children: n.children ? filterExclude(n.children, id) : [] }))
      parentCategoryOptions.value = filterExclude(tree, excludeId)
    } else {
      parentCategoryOptions.value = tree
    }
  })
}

const handleAdd = () => { data.form = { status: 'active', parentId: null }; loadParentOptions(); data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); if (!data.form.parentId) data.form.parentId = null; loadParentOptions(row.id); data.formVisible = true }
const save = () => {
  (data.form.id ? request.put('/questionCategory/update', data.form) : request.post('/questionCategory/add', data.form))
    .then(res => { if (res.code === '200') { ElMessage.success('操作成功'); data.formVisible = false; load() } else ElMessage.error(res.msg || '操作失败') })
}
const del = (id) => { request.delete('/questionCategory/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } else ElMessage.error(res.msg || '删除失败') }) }
const handleSelectionChange = (rows) => { data.ids = rows.map(v => v.id) }
const handleIconUpload = (response) => { if (response.code === '200' || response.data) { data.form.icon = response.data || response.msg; ElMessage.success('图标上传成功') } else ElMessage.error('上传失败') }
const reset = () => { data.name = null; load() }

onMounted(() => { load() })
</script>

<style scoped>
.cat-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #7c3aed 0%, #9333ea 50%, #c084fc 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(124, 58, 237, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; margin-bottom: 20px; max-width: 380px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 22px; color: #fff; }
.stat-total .stat-inner { background: linear-gradient(135deg, #7c3aed, #9333ea); }
.stat-root .stat-inner { background: linear-gradient(135deg, #4f46e5, #818cf8); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { position: relative; width: 240px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.reset-btn { border-radius: 10px; }

/* ===== 表格 ===== */
.table-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.cat-table :deep(.el-table__row:hover > td) { background: #f5f3ff !important; }
.cat-name-cell { display: flex; align-items: center; gap: 10px; }
.cat-icon-img { width: 28px; height: 28px; border-radius: 6px; object-fit: cover; }
.cat-icon-default { width: 28px; height: 28px; border-radius: 6px; background: #ede9fe; color: #7c3aed; display: flex; align-items: center; justify-content: center; }
.cat-icon-default .el-icon { font-size: 14px; }
.cat-name-text { font-weight: 600; color: #1f2937; }
.status-pill { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-pill.active { background: #dcfce7; color: #16a34a; }
.status-pill.inactive { background: #f1f5f9; color: #6b7280; }
.child-count { font-size: 13px; color: #7c3aed; font-weight: 600; }
.child-count.zero { color: #9ca3af; }

/* 上传图标 */
.icon-replace-hint { position: absolute; bottom: 0; left: 0; right: 0; text-align: center; font-size: 10px; color: #fff; background: rgba(0,0,0,0.5); border-radius: 0 0 10px 10px; padding: 2px 0; opacity: 0; transition: opacity 0.2s; cursor: pointer; }
.icon-replace-hint:hover { opacity: 1; }
</style>
