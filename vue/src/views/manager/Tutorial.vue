<template>
  <div class="tutorial-page">
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">📚</div>
        <div class="hero-text">
          <h1>教程管理</h1>
          <p>发布视频、图文、文档或 Markdown 教程</p>
        </div>
      </div>
      <el-button type="primary" size="large" round @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增教程
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card green"><div class="sc-num">{{ data.total }}</div><div class="sc-lbl">教程总数</div></div>
      <div class="stat-card blue"><div class="sc-num">{{ videoCount }}</div><div class="sc-lbl">视频教程</div></div>
      <div class="stat-card purple"><div class="sc-num">{{ imageCount }}</div><div class="sc-lbl">图文教程</div></div>
      <div class="stat-card orange"><div class="sc-num">{{ docCount }}</div><div class="sc-lbl">文档/MD</div></div>
    </div>

    <div class="toolbar card">
      <div class="tb-left">
        <el-input v-model="data.title" placeholder="搜索教程标题..." clearable @keyup.enter="load" @clear="load" style="width: 220px;">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="data.contentType" placeholder="类型筛选" @change="load" clearable style="width: 130px;">
          <el-option label="视频教程" value="video" />
          <el-option label="图文教程" value="image_text" />
          <el-option label="文档教程" value="document" />
          <el-option label="Markdown" value="markdown" />
        </el-select>
        <el-button plain @click="reset">重置</el-button>
      </div>
      <div class="tb-right">
        <el-button type="danger" plain size="small" :disabled="!data.ids.length" @click="delBatch">
          <el-icon><Delete /></el-icon> 批量删除 {{ data.ids.length ? `(${data.ids.length})` : '' }}
        </el-button>
        <span class="tb-total">共 <strong>{{ data.total }}</strong> 篇</span>
      </div>
    </div>

    <div class="tutorial-list">
      <transition-group name="fade">
        <div v-for="row in data.tableData" :key="row.id" class="tl-item card">
          <div class="tl-cover">
            <img v-if="row.coverUrl" :src="row.coverUrl" />
            <div v-else class="tl-cover-ph">{{ typeEmoji(row.contentType) }}</div>
            <div class="tl-type-badge" :class="'t-' + row.contentType">{{ typeLabel(row.contentType) }}</div>
          </div>
          <div class="tl-body">
            <h3>{{ row.title }}</h3>
            <div class="tl-meta">
              <span><el-icon><User /></el-icon> {{ row.creatorName || '未知' }}</span>
              <span><el-icon><View /></el-icon> {{ row.viewCount || 0 }}</span>
              <span class="tl-status" :class="row.status === 'active' ? 'published' : 'draft'">
                {{ row.status === 'active' ? '已发布' : '草稿' }}
              </span>
            </div>
            <p class="tl-desc" v-if="row.description">{{ row.description }}</p>
          </div>
          <div class="tl-actions">
            <el-button type="primary" link size="small" @click="handleEdit(row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="del(row.id)">
              <template #reference><el-button type="danger" link size="small"><el-icon><Delete /></el-icon> 删除</el-button></template>
            </el-popconfirm>
          </div>
        </div>
      </transition-group>

      <div v-if="!data.loading && data.tableData.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无教程内容</p>
        <el-button type="primary" size="small" @click="handleAdd">创建第一个教程</el-button>
      </div>

      <div class="pagination-wrap" v-if="data.total">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="data.pageNum" :page-size="data.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :total="data.total" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="data.form.id ? '编辑教程' : '新增教程'" v-model="data.formVisible" width="720px" destroy-on-close>
      <el-form :model="data.form" label-width="90px" style="padding: 8px 0">
        <el-form-item label="标题">
          <el-input v-model="data.form.title" placeholder="请输入教程标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="data.form.description" type="textarea" :rows="2" placeholder="简要描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in data.categories" :key="cat.id" :label="cat.fullName || cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="教程类型">
          <div class="type-selector">
            <div v-for="t in typeOptions" :key="t.value"
                 class="ts-item" :class="{ active: data.form.contentType === t.value }"
                 @click="data.form.contentType = t.value">
              <span class="ts-emoji">{{ t.emoji }}</span>
              <span class="ts-name">{{ t.label }}</span>
              <span class="ts-desc">{{ t.desc }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="封面图">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleCoverSuccess" :show-file-list="false" accept="image/*">
            <img v-if="data.form.coverUrl" :src="data.form.coverUrl" class="cover-preview" />
            <el-button v-else type="primary" plain><el-icon><Upload /></el-icon> 上传封面</el-button>
          </el-upload>
        </el-form-item>

        <!-- 视频上传 -->
        <template v-if="data.form.contentType === 'video'">
          <el-form-item label="视频文件">
            <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleVideoSuccess" :show-file-list="false" accept="video/*">
              <video v-if="data.form.videoUrl" :src="data.form.videoUrl" class="video-preview" controls></video>
              <el-button v-else type="success" plain><el-icon><Upload /></el-icon> 上传视频</el-button>
            </el-upload>
          </el-form-item>
        </template>

        <!-- 图文教程 -->
        <template v-if="data.form.contentType === 'image_text'">
          <el-form-item label="图文内容">
            <el-input v-model="data.form.content" type="textarea" :rows="10" placeholder="支持 HTML 格式的图文内容" />
          </el-form-item>
          <el-form-item label="上传图片">
             <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleImageInsert" :show-file-list="false" accept="image/*" multiple>
              <el-button plain><el-icon><Picture /></el-icon> 插入图片</el-button>
            </el-upload>
          </el-form-item>
        </template>

        <!-- 文档上传 -->
        <template v-if="data.form.contentType === 'document'">
          <el-form-item label="上传文档">
            <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleDocSuccess" :show-file-list="false" accept=".pdf,.doc,.docx,.ppt,.pptx">
              <div v-if="data.form.documentUrl" class="doc-uploaded">
                <el-icon :size="24"><Document /></el-icon>
                <span>文档已上传</span>
              </div>
              <el-button v-else type="primary" plain><el-icon><Upload /></el-icon> 上传文档（PDF/Word/PPT）</el-button>
            </el-upload>
          </el-form-item>
        </template>

        <!-- Markdown 教程 -->
        <template v-if="data.form.contentType === 'markdown'">
          <el-form-item label="Markdown">
            <div class="md-editor-wrap">
              <div class="md-tabs">
                <span :class="{ active: data.mdTab === 'write' }" @click="data.mdTab = 'write'">编辑</span>
                <span :class="{ active: data.mdTab === 'preview' }" @click="data.mdTab = 'preview'">预览</span>
              </div>
              <el-input v-if="data.mdTab === 'write'" v-model="data.form.content" type="textarea" :rows="14" placeholder="在此输入 Markdown 内容..." />
              <div v-else class="md-preview" v-html="renderMarkdown(data.form.content || '')"></div>
            </div>
          </el-form-item>
          <el-form-item label="上传MD">
            <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleMdFileSuccess" :show-file-list="false" accept=".md,.markdown,.txt">
              <el-button plain><el-icon><Upload /></el-icon> 导入 .md 文件</el-button>
            </el-upload>
          </el-form-item>
        </template>

        <el-form-item label="状态">
          <el-radio-group v-model="data.form.status">
            <el-radio value="active">发布</el-radio>
            <el-radio value="draft">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue"
import request from "@/utils/request.js"
import { ElMessage, ElMessageBox } from "element-plus"
import { getUploadHeaders } from '@/utils/upload.js'

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const uploadHeaders = getUploadHeaders()

const typeOptions = [
  { value: 'video', label: '视频教程', desc: '上传视频文件', emoji: '🎬' },
  { value: 'image_text', label: '图文教程', desc: '富文本+图片', emoji: '🖼️' },
  { value: 'document', label: '文档教程', desc: 'PDF/Word/PPT', emoji: '📄' },
  { value: 'markdown', label: 'Markdown', desc: 'MD文档生成网页', emoji: '📝' },
]

const typeEmoji = (t) => ({ video: '🎬', image_text: '🖼️', document: '📄', markdown: '📝' }[t] || '📚')
const typeLabel = (t) => ({ video: '视频', image_text: '图文', document: '文档', markdown: 'MD' }[t] || '其他')

const data = reactive({
  title: '', contentType: '', formVisible: false, form: {}, tableData: [],
  categories: [], pageNum: 1, pageSize: 10, total: 0, ids: [], loading: false, mdTab: 'write'
})

const videoCount = computed(() => data.tableData.filter(r => r.contentType === 'video').length)
const imageCount = computed(() => data.tableData.filter(r => r.contentType === 'image_text').length)
const docCount = computed(() => data.tableData.filter(r => r.contentType === 'document' || r.contentType === 'markdown').length)

const renderMarkdown = (md) => {
  if (!md) return '<p style="color:#999">暂无内容</p>'
  let html = md
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/gim, '<em>$1</em>')
    .replace(/`(.*?)`/gim, '<code>$1</code>')
    .replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')
  return '<p>' + html + '</p>'
}

const load = () => {
  data.loading = true
  request.get('/tutorial/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, title: data.title || undefined, contentType: data.contentType || undefined }
  }).then(res => {
    data.loading = false
    if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total || 0 }
  }).catch(() => { data.loading = false })
}

const reset = () => { data.title = ''; data.contentType = ''; data.pageNum = 1; load() }
const handleAdd = () => { data.form = { contentType: 'video', status: 'active' }; data.mdTab = 'write'; data.formVisible = true }
const handleEdit = (row) => { data.form = JSON.parse(JSON.stringify(row)); data.mdTab = 'write'; data.formVisible = true }
const save = () => {
  (data.form.id ? request.put('/tutorial/update', data.form) : request.post('/tutorial/add', data.form))
    .then(res => { if (res.code === '200') { ElMessage.success('保存成功'); data.formVisible = false; load() } else ElMessage.error(res.msg || '操作失败') })
}
const del = (id) => { request.delete('/tutorial/delete/' + id).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } }) }
const delBatch = () => { if (!data.ids.length) return; ElMessageBox.confirm('确定批量删除？', '提示', { type: 'warning' }).then(() => { request.delete('/tutorial/delete/batch', { data: data.ids }).then(res => { if (res.code === '200') { ElMessage.success('删除成功'); load() } }) }).catch(() => {}) }
const handleCoverSuccess = (r) => { if (r.code === '200') data.form.coverUrl = r.data }
const handleVideoSuccess = (r) => { if (r.code === '200') data.form.videoUrl = r.data }
const handleImageInsert = (r) => { if (r.code === '200') data.form.content = (data.form.content || '') + '<img src="' + r.data + '" style="max-width:100%;border-radius:8px;margin:8px 0"/>' }
const handleDocSuccess = (r) => { if (r.code === '200') data.form.documentUrl = r.data }
const handleMdFileSuccess = (r) => {
  if (r.code === '200') {
    const url = r.data
    fetch(url).then(r => r.text()).then(text => { data.form.content = text; ElMessage.success('MD 文件已导入') }).catch(() => ElMessage.error('读取文件失败'))
  }
}
const handleSizeChange = (s) => { data.pageSize = s; load() }
const handleCurrentChange = (p) => { data.pageNum = p; load() }

onMounted(() => {
  import('@/utils/categoryUtils.js').then(m => m.loadFlatCategories().then(flat => { data.categories = flat }))
  load()
})
</script>

<style scoped>
.tutorial-page { max-width: 1200px; margin: 0 auto; }

.page-hero {
  background: var(--gradient-header);
  border-radius: var(--radius-lg);
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  color: #fff;
}
.hero-left { display: flex; align-items: center; gap: 16px; }
.hero-icon { font-size: 36px; }
.hero-text h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.65); }

.stats-row { display: grid; grid-template-columns: repeat(4,1fr); gap: 14px; margin-bottom: 20px; }
.stat-card {
  border-radius: var(--radius-md); padding: 16px 20px;
  color: #fff; text-align: center;
}
.stat-card.green { background: linear-gradient(135deg, #00b42a, #00d034); }
.stat-card.blue { background: linear-gradient(135deg, #409eff, #53a8ff); }
.stat-card.purple { background: linear-gradient(135deg, #722ed1, #9b59b6); }
.stat-card.orange { background: linear-gradient(135deg, #ff7d00, #ff9500); }
.sc-num { font-size: 26px; font-weight: 800; }
.sc-lbl { font-size: 12px; opacity: 0.85; margin-top: 2px; }

.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.tb-left { display: flex; gap: 10px; align-items: center; }
.tb-total { font-size: 13px; color: var(--text-secondary); }
.tb-total strong { color: var(--primary-color); }

.tutorial-list { display: flex; flex-direction: column; gap: 10px; }

.tl-item {
  display: flex; gap: 16px; align-items: center;
  padding: 14px 20px; transition: all 0.2s; cursor: default;
}
.tl-item:hover { transform: translateX(3px); }

.tl-cover { width: 120px; height: 80px; flex-shrink: 0; border-radius: 8px; overflow: hidden; position: relative; }
.tl-cover img { width: 100%; height: 100%; object-fit: cover; }
.tl-cover-ph { width: 100%; height: 100%; background: var(--bg-page); display: flex; align-items: center; justify-content: center; font-size: 28px; }
.tl-type-badge {
  position: absolute; top: 4px; left: 4px;
  padding: 1px 6px; border-radius: 4px;
  font-size: 10px; font-weight: 700; color: #fff;
}
.tl-type-badge.t-video { background: rgba(64,158,255,0.9); }
.tl-type-badge.t-image_text { background: rgba(114,46,209,0.9); }
.tl-type-badge.t-document { background: rgba(255,125,0,0.9); }
.tl-type-badge.t-markdown { background: rgba(0,180,42,0.9); }

.tl-body { flex: 1; min-width: 0; }
.tl-body h3 { margin: 0 0 6px; font-size: 15px; font-weight: 700; color: var(--text-primary); }
.tl-meta { display: flex; gap: 14px; font-size: 12px; color: var(--text-secondary); align-items: center; }
.tl-meta span { display: flex; align-items: center; gap: 3px; }
.tl-status { padding: 1px 8px; border-radius: 10px; font-weight: 600; }
.tl-status.published { background: var(--primary-light); color: var(--primary-color); }
.tl-status.draft { background: var(--bg-page); color: var(--text-secondary); }
.tl-desc { font-size: 13px; color: var(--text-secondary); margin: 6px 0 0; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }

.tl-actions { display: flex; gap: 4px; flex-shrink: 0; }

.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 42px; margin-bottom: 12px; opacity: 0.4; }
.empty-state p { color: var(--text-secondary); margin-bottom: 12px; }

.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; }

/* 类型选择器 */
.type-selector { display: flex; gap: 10px; flex-wrap: wrap; }
.ts-item {
  border: 2px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 100px;
}
.ts-item:hover { border-color: var(--primary-color); }
.ts-item.active { border-color: var(--primary-color); background: var(--primary-light); }
.ts-emoji { font-size: 22px; }
.ts-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.ts-desc { font-size: 10px; color: var(--text-secondary); }

.cover-preview { width: 200px; height: 120px; object-fit: cover; border-radius: 8px; border: 2px solid var(--border-light); }
.video-preview { width: 100%; max-height: 240px; border-radius: 8px; }

.doc-uploaded { display: flex; align-items: center; gap: 8px; padding: 12px 16px; background: var(--primary-light); border-radius: 8px; color: var(--primary-color); font-weight: 600; }

/* Markdown 编辑器 */
.md-editor-wrap {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
  width: 100%;
}
.md-tabs {
  display: flex; border-bottom: 1px solid var(--border-light);
  background: var(--bg-page);
}
.md-tabs span {
  padding: 8px 20px; font-size: 13px; font-weight: 500;
  color: var(--text-secondary); cursor: pointer; transition: all 0.2s;
}
.md-tabs span.active { color: var(--primary-color); border-bottom: 2px solid var(--primary-color); background: var(--bg-card); }
.md-preview {
  padding: 16px 20px; min-height: 200px; max-height: 360px; overflow-y: auto;
  font-size: 14px; line-height: 1.7; color: var(--text-primary);
}
.md-preview :deep(h1) { font-size: 20px; margin: 8px 0; }
.md-preview :deep(h2) { font-size: 18px; margin: 8px 0; }
.md-preview :deep(h3) { font-size: 16px; margin: 6px 0; }
.md-preview :deep(code) { background: var(--bg-page); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.md-preview :deep(blockquote) { border-left: 3px solid var(--primary-color); padding-left: 12px; color: var(--text-secondary); margin: 8px 0; }

@media (max-width: 700px) {
  .tl-item { flex-direction: column; align-items: flex-start; }
  .tl-cover { width: 100%; height: 140px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
