<template>
  <div class="forum-page">

    <!-- Tab 切换：帖子 / 投票 -->
    <div class="tab-bar">
      <div class="tab-item" :class="{ active: data.activeTab === 'posts' }" @click="data.activeTab = 'posts'">
        <el-icon><ChatLineSquare /></el-icon> 帖子管理
      </div>
      <div class="tab-item" :class="{ active: data.activeTab === 'polls' }" @click="data.activeTab = 'polls'; loadPolls()">
        <el-icon><Histogram /></el-icon> 投票管理
      </div>
    </div>

    <!-- ==================== 帖子管理 ==================== -->
    <template v-if="data.activeTab === 'posts'">
      <div class="page-hero">
        <div class="hero-left">
          <div class="hero-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M8 9h8M8 13h5" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="hero-text">
            <h1>帖子管理</h1>
            <p>社区帖子全览，支持置顶、锁定、精华管理与互动数据监控</p>
          </div>
        </div>
        <div class="hero-right">
          <el-button type="primary" size="large" round @click="handleAdd">
            <el-icon><Plus /></el-icon> 发布帖子
          </el-button>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.total }}</div><div class="stat-label">帖子总数</div><div class="stat-icon"><el-icon><Document /></el-icon></div></div></div>
        <div class="stat-card stat-topped"><div class="stat-inner"><div class="stat-num">{{ data.tableData.filter(p => p.isTop).length }}</div><div class="stat-label">置顶帖</div><div class="stat-icon"><el-icon><Top /></el-icon></div></div></div>
        <div class="stat-card stat-locked"><div class="stat-inner"><div class="stat-num">{{ data.tableData.filter(p => p.isLocked).length }}</div><div class="stat-label">锁定帖</div><div class="stat-icon"><el-icon><Lock /></el-icon></div></div></div>
        <div class="stat-card stat-deleted"><div class="stat-inner"><div class="stat-num">{{ data.tableData.filter(p => p.status === 'deleted').length }}</div><div class="stat-label">已删除</div><div class="stat-icon"><el-icon><Delete /></el-icon></div></div></div>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <div class="search-wrap"><el-icon class="s-icon"><Search /></el-icon><el-input v-model="data.title" placeholder="搜索帖子标题..." clearable @keyup.enter="load" @clear="load" class="search-input" /></div>
          <div class="search-wrap"><el-icon class="s-icon"><User /></el-icon><el-input v-model="data.authorName" placeholder="搜索作者..." clearable @keyup.enter="load" @clear="load" class="search-input" /></div>
          <el-select v-model="data.status" placeholder="状态" @change="load" class="filter-sel" clearable>
            <template #prefix><el-icon><Filter /></el-icon></template>
            <el-option label="全部" value="" /><el-option label="正常" value="active" /><el-option label="已删除" value="deleted" />
          </el-select>
          <el-button plain @click="reset" class="reset-btn">重置</el-button>
        </div>
        <div class="toolbar-right"><span class="total-tip">共 <strong>{{ data.total }}</strong> 篇帖子</span></div>
      </div>

      <div class="post-list" v-loading="data.loading">
        <div v-for="row in data.tableData" :key="row.id" class="post-card" :class="{ 'is-deleted': row.status === 'deleted', 'is-top': row.isTop, 'is-locked': row.isLocked }">
          <div class="post-badges">
            <span v-if="row.isTop" class="pin-badge"><el-icon><Top /></el-icon> 置顶</span>
            <span v-if="row.isLocked" class="lock-badge"><el-icon><Lock /></el-icon> 锁定</span>
          </div>
          <div class="post-main">
            <div class="post-cover" v-if="row.coverUrl"><el-image :src="row.coverUrl" fit="cover" class="cover-img" :preview-src-list="[row.coverUrl]" preview-teleported /></div>
            <div class="post-content">
              <div class="post-title-row">
                <h3 class="post-title">{{ row.title }}</h3>
                <span class="status-chip" :class="'st-' + (row.status || 'active')">{{ row.status === 'deleted' ? '已删除' : '正常' }}</span>
              </div>
              <div class="post-tags" v-if="row.tags"><el-tag v-for="tag in row.tags.split(',')" :key="tag" size="small" class="tag-item">{{ tag }}</el-tag></div>
              <div class="post-meta">
                <span class="meta-item">
                  <div class="author-avatar"><img v-if="row.authorAvatar" :src="row.authorAvatar" /><span v-else>{{ (row.authorName || 'A').charAt(0).toUpperCase() }}</span></div>
                  <span class="author-name">{{ row.authorName || '未知' }}</span>
                </span>
                <span class="meta-sep">·</span>
                <span class="meta-item"><el-icon><Clock /></el-icon> {{ formatTime(row.createdAt) }}</span>
              </div>
            </div>
            <div class="post-stats">
              <div class="stat-item stat-views"><el-icon><View /></el-icon><span>{{ row.viewCount || 0 }}</span></div>
              <div class="stat-item stat-likes"><el-icon><Star /></el-icon><span>{{ row.likeCount || 0 }}</span></div>
              <div class="stat-item stat-comments"><el-icon><ChatLineSquare /></el-icon><span>{{ row.commentCount || 0 }}</span></div>
            </div>
          </div>
          <div class="post-footer">
            <div class="role-chip" :class="'role-' + (row.authorRole || 'USER').toLowerCase()">
              <el-icon><User v-if="row.authorRole === 'USER'" /><Tools v-else-if="row.authorRole === 'ADMIN'" /><Star v-else-if="row.authorRole === 'OWNER'" /><Reading v-else /></el-icon>
              {{ roleLabel(row.authorRole) }}
            </div>
            <div class="post-actions">
              <el-button type="primary" link size="small" @click="viewDetail(row)"><el-icon><View /></el-icon> 查看</el-button>
              <el-button :type="row.isTop ? 'warning' : 'success'" link size="small" @click="toggleTop(row)"><el-icon><Top /></el-icon> {{ row.isTop ? '取消置顶' : '置顶' }}</el-button>
              <el-button :type="row.isLocked ? 'info' : 'danger'" link size="small" @click="toggleLock(row)"><el-icon><Lock /></el-icon> {{ row.isLocked ? '解锁' : '锁定' }}</el-button>
              <el-popconfirm title="确定删除该帖子？" confirm-button-text="确认删除" cancel-button-text="取消" @confirm="del(row.id)">
                <template #reference><el-button type="danger" link size="small"><el-icon><Delete /></el-icon> 删除</el-button></template>
              </el-popconfirm>
            </div>
          </div>
        </div>
        <div v-if="!data.loading && data.tableData.length === 0" class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" fill="#f1f5f9"/><path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/><circle cx="9" cy="10" r="1" fill="#94a3b8"/><circle cx="15" cy="10" r="1" fill="#94a3b8"/></svg>
          <p>暂无帖子内容</p>
          <el-button type="primary" size="small" @click="handleAdd">发布第一篇帖子</el-button>
        </div>
        <div class="pagination-wrap" v-if="data.total > 0">
          <el-pagination background @current-change="load" :current-page="data.pageNum" :page-size="data.pageSize" layout="total, prev, pager, next" :total="data.total" />
        </div>
      </div>

      <el-dialog v-model="data.detailVisible" title="帖子详情" width="720px" destroy-on-close>
        <div v-if="data.currentPost" class="detail-body">
          <div class="detail-header">
            <div class="detail-cover" v-if="data.currentPost.coverUrl"><el-image :src="data.currentPost.coverUrl" fit="cover" /></div>
            <div class="detail-title">
              <div class="detail-tags" v-if="data.currentPost.tags"><el-tag v-for="tag in data.currentPost.tags.split(',')" :key="tag" size="small">{{ tag }}</el-tag></div>
              <h2>{{ data.currentPost.title }}</h2>
              <div class="detail-meta">
                <div class="author-avatar"><img v-if="data.currentPost.authorAvatar" :src="data.currentPost.authorAvatar" /><span v-else>{{ (data.currentPost.authorName || 'A').charAt(0).toUpperCase() }}</span></div>
                <div class="meta-text"><span class="author-name">{{ data.currentPost.authorName }}</span><span class="meta-sep">·</span><span class="post-time">{{ formatTime(data.currentPost.createdAt) }}</span></div>
                <div class="detail-badges">
                  <span v-if="data.currentPost.isTop" class="pin-badge"><el-icon><Top /></el-icon> 置顶</span>
                  <span v-if="data.currentPost.isLocked" class="lock-badge"><el-icon><Lock /></el-icon> 锁定</span>
                </div>
              </div>
              <div class="detail-stats">
                <span><el-icon><View /></el-icon> {{ data.currentPost.viewCount }} 浏览</span>
                <span><el-icon><Star /></el-icon> {{ data.currentPost.likeCount }} 点赞</span>
                <span><el-icon><ChatLineSquare /></el-icon> {{ data.currentPost.commentCount }} 评论</span>
              </div>
            </div>
          </div>
          <el-divider />
          <div class="detail-content" v-html="data.currentPost.content"></div>
        </div>
      </el-dialog>

      <el-dialog v-model="data.formVisible" title="发布帖子" width="680px" destroy-on-close>
        <el-form :model="data.form" label-width="70px" style="padding: 8px 0">
          <el-form-item label="标题"><el-input v-model="data.form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit /></el-form-item>
          <el-form-item label="标签"><el-input v-model="data.form.tags" placeholder="多个标签用逗号分隔"><template #prefix><el-icon><PriceTag /></el-icon></template></el-input></el-form-item>
          <el-form-item label="封面图">
            <div class="cover-upload">
              <el-upload :action="uploadUrl" :on-success="handleCoverSuccess" :show-file-list="false" accept="image/*" class="cover-uploader">
                <img v-if="data.form.coverUrl" :src="data.form.coverUrl" class="cover-preview" />
                <div v-else class="cover-placeholder"><el-icon><Plus /></el-icon><span>上传封面图</span></div>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="内容"><el-input v-model="data.form.content" type="textarea" :rows="10" placeholder="请输入帖子内容（支持HTML格式）" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost"><el-icon><Check /></el-icon> 发布帖子</el-button>
        </template>
      </el-dialog>
    </template>

    <!-- ==================== 投票管理 ==================== -->
    <template v-if="data.activeTab === 'polls'">
      <div class="page-hero">
        <div class="hero-left">
          <div class="hero-icon">📊</div>
          <div class="hero-text">
            <h1>投票管理</h1>
            <p>管理所有投票活动，创建、监控、结束投票</p>
          </div>
        </div>
        <div class="hero-right">
          <el-button type="primary" size="large" round @click="openPollDialog">
            <el-icon><Plus /></el-icon> 创建投票
          </el-button>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card" style="background: linear-gradient(135deg, #0f766e, #14b8a6);"><div class="stat-inner"><div class="stat-num">{{ data.polls.length }}</div><div class="stat-label">投票总数</div><div class="stat-icon"><el-icon><Histogram /></el-icon></div></div></div>
        <div class="stat-card" style="background: linear-gradient(135deg, #059669, #34d399);"><div class="stat-inner"><div class="stat-num">{{ data.polls.filter(p => p.status === 'active').length }}</div><div class="stat-label">进行中</div><div class="stat-icon"><el-icon><CircleCheck /></el-icon></div></div></div>
        <div class="stat-card" style="background: linear-gradient(135deg, #6366f1, #818cf8);"><div class="stat-inner"><div class="stat-num">{{ data.polls.filter(p => p.status === 'ended').length }}</div><div class="stat-label">已结束</div><div class="stat-icon"><el-icon><CircleClose /></el-icon></div></div></div>
        <div class="stat-card" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);"><div class="stat-inner"><div class="stat-num">{{ data.polls.reduce((s, p) => s + (p.totalVotes || 0), 0) }}</div><div class="stat-label">总投票人次</div><div class="stat-icon"><el-icon><User /></el-icon></div></div></div>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="data.pollStatusFilter" placeholder="状态筛选" @change="loadPolls" class="filter-sel" clearable>
            <el-option label="全部" value="" /><el-option label="进行中" value="active" /><el-option label="已结束" value="ended" />
          </el-select>
        </div>
        <div class="toolbar-right"><span class="total-tip">共 <strong>{{ data.polls.length }}</strong> 个投票</span></div>
      </div>

      <div class="poll-list" v-loading="data.pollLoading">
        <div v-for="poll in data.polls" :key="poll.id" class="poll-manage-card">
          <div class="pmc-header">
            <div class="pmc-left">
              <span class="pmc-status" :class="poll.status">{{ poll.status === 'active' ? '进行中' : '已结束' }}</span>
              <span class="pmc-type">{{ poll.maxChoices > 1 ? '多选' : '单选' }}</span>
              <h3 class="pmc-title">{{ poll.title }}</h3>
            </div>
            <div class="pmc-actions">
              <el-button v-if="poll.status === 'active'" type="warning" size="small" plain @click="endPoll(poll.id)">
                <el-icon><CircleClose /></el-icon> 结束投票
              </el-button>
              <el-popconfirm title="确定删除此投票？" @confirm="deletePoll(poll.id)">
                <template #reference><el-button type="danger" size="small" plain><el-icon><Delete /></el-icon> 删除</el-button></template>
              </el-popconfirm>
            </div>
          </div>
          <div class="pmc-desc" v-if="poll.description">{{ poll.description }}</div>
          <div class="pmc-meta">
            <span>👤 {{ poll.creatorName }}</span>
            <span v-if="poll.endTime">⏰ 截止: {{ formatTime(poll.endTime) }}</span>
            <span>👥 {{ poll.totalVotes || 0 }} 人参与</span>
            <span v-if="poll.isAnonymous">🔒 匿名投票</span>
          </div>

          <div class="pmc-results">
            <div v-for="opt in poll.options" :key="opt.id" class="pmc-result-row">
              <div class="pmc-result-head">
                <span class="pmc-opt-text">{{ opt.content }}</span>
                <span class="pmc-opt-pct">{{ opt.percentage || 0 }}%</span>
              </div>
              <div class="pmc-bar-bg">
                <div class="pmc-bar" :style="{ width: (opt.percentage || 0) + '%' }"></div>
              </div>
              <span class="pmc-opt-count">{{ opt.voteCount || 0 }} 票</span>
            </div>
          </div>
        </div>

        <div v-if="!data.pollLoading && data.polls.length === 0" class="empty-state">
          <div style="font-size: 48px; margin-bottom: 12px;">📊</div>
          <p>暂无投票活动</p>
          <el-button type="primary" size="small" @click="openPollDialog">创建第一个投票</el-button>
        </div>
      </div>

      <!-- 创建投票弹窗 -->
      <el-dialog v-model="data.pollDialogVisible" title="创建投票" width="640px" destroy-on-close>
        <el-form :model="data.pollForm" label-width="90px">
          <el-form-item label="投票标题" required><el-input v-model="data.pollForm.title" placeholder="请输入投票标题" maxlength="100" show-word-limit /></el-form-item>
          <el-form-item label="投票描述"><el-input v-model="data.pollForm.description" type="textarea" :rows="3" placeholder="简要描述投票主题..." /></el-form-item>
          <el-form-item label="投票类型">
            <el-radio-group v-model="data.pollForm.maxChoices">
              <el-radio-button :value="1">单选</el-radio-button>
              <el-radio-button :value="2">多选(2项)</el-radio-button>
              <el-radio-button :value="3">多选(3项)</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="截止时间">
            <el-date-picker v-model="data.pollForm.endTime" type="datetime" placeholder="可选，不选则永不过期" style="width: 100%" :disabled-date="d => d < new Date()" />
          </el-form-item>
          <el-form-item label="匿名投票"><el-switch v-model="data.pollForm.isAnonymous" active-text="匿名" inactive-text="公开" /></el-form-item>
          <el-form-item label="投票选项" required>
            <div style="width: 100%;">
              <div v-for="(opt, idx) in data.pollForm.options" :key="idx" style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <span style="width: 24px; height: 24px; border-radius: 50%; background: var(--primary-color); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0;">{{ idx + 1 }}</span>
                <el-input v-model="opt.content" :placeholder="'选项 ' + (idx + 1)" style="flex:1" />
                <el-button v-if="data.pollForm.options.length > 2" type="danger" text @click="data.pollForm.options.splice(idx, 1)"><el-icon><Delete /></el-icon></el-button>
              </div>
              <el-button v-if="data.pollForm.options.length < 10" type="primary" plain size="small" @click="data.pollForm.options.push({ content: '' })" style="margin-top: 4px;">+ 添加选项</el-button>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="data.pollDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPoll" :loading="data.pollSubmitting">创建投票</el-button>
        </template>
      </el-dialog>
    </template>

  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, User, Plus, Top, Lock, Delete, View, Star, ChatLineSquare, Clock, Document, PriceTag, Check, Tools, Reading, Histogram, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const adminUser = JSON.parse(localStorage.getItem('xm-user') || '{}')

const data = reactive({
  activeTab: 'posts',
  loading: false,
  title: '',
  authorName: '',
  status: '',
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  detailVisible: false,
  currentPost: null,
  formVisible: false,
  form: {},
  pollLoading: false,
  polls: [],
  pollStatusFilter: '',
  pollDialogVisible: false,
  pollSubmitting: false,
  pollForm: { title: '', description: '', maxChoices: 1, endTime: null, isAnonymous: false, options: [{ content: '' }, { content: '' }] }
})

const formatTime = (val) => {
  if (!val) return '—'
  return String(val).replace('T', ' ').substring(0, 19)
}

const roleLabel = (role) => {
  if (role === 'OWNER') return '站长'
  if (role === 'ADMIN') return '管理员'
  if (role === 'HELPER') return '助手'
  return '用户'
}

const load = () => {
  data.loading = true
  request.get('/forumPost/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, title: data.title || null, authorName: data.authorName || null, status: data.status || null }
  }).then(res => {
    data.loading = false
    if (res.code === '200') { data.tableData = res.data?.list || []; data.total = res.data?.total || 0 }
  }).catch(() => { data.loading = false })
}

const reset = () => { data.title = ''; data.authorName = ''; data.status = ''; data.pageNum = 1; load() }

const handleAdd = () => { data.form = { title: '', content: '', tags: '', coverUrl: '' }; data.formVisible = true }

const submitPost = () => {
  if (!data.form.title?.trim()) return ElMessage.warning('请输入标题')
  if (!data.form.content?.trim()) return ElMessage.warning('请输入内容')
  request.post('/forumPost/add', data.form).then(res => {
    if (res.code === '200') { ElMessage.success('发布成功'); data.formVisible = false; load() }
    else ElMessage.error(res.msg)
  })
}

const handleCoverSuccess = (res) => { if (res.code === '200') data.form.coverUrl = res.data }

const viewDetail = (row) => { data.currentPost = JSON.parse(JSON.stringify(row)); data.detailVisible = true }

const toggleTop = (row) => {
  request.put('/forumPost/toggleTop/' + row.id).then(res => {
    if (res.code === '200') { ElMessage.success(row.isTop ? '已取消置顶' : '已置顶'); load() }
    else ElMessage.error(res.msg)
  })
}

const toggleLock = (row) => {
  request.put('/forumPost/toggleLock/' + row.id).then(res => {
    if (res.code === '200') { ElMessage.success(row.isLocked ? '已解锁' : '已锁定'); load() }
    else ElMessage.error(res.msg)
  })
}

const del = (id) => {
  request.delete('/forumPost/adminDelete/' + id).then(res => {
    if (res.code === '200') { ElMessage.success('删除成功'); load() }
    else ElMessage.error(res.msg)
  })
}

const loadPolls = () => {
  data.pollLoading = true
  request.get('/poll/selectPage', {
    params: { pageNum: 1, pageSize: 100, status: data.pollStatusFilter || undefined }
  }).then(res => {
    data.pollLoading = false
    if (res.code === '200') data.polls = res.data?.list || []
  }).catch(() => { data.pollLoading = false })
}

const openPollDialog = () => {
  data.pollForm = { title: '', description: '', maxChoices: 1, endTime: null, isAnonymous: false, options: [{ content: '' }, { content: '' }] }
  data.pollDialogVisible = true
}

const submitPoll = () => {
  if (!data.pollForm.title) return ElMessage.warning('请输入投票标题')
  const validOpts = data.pollForm.options.filter(o => o.content.trim())
  if (validOpts.length < 2) return ElMessage.warning('至少需要2个有效选项')
  data.pollSubmitting = true
  request.post('/poll/add', {
    title: data.pollForm.title,
    description: data.pollForm.description,
    maxChoices: data.pollForm.maxChoices,
    endTime: data.pollForm.endTime ? new Date(data.pollForm.endTime).toISOString().slice(0, 19) : null,
    isAnonymous: data.pollForm.isAnonymous,
    creatorId: adminUser.id,
    creatorName: adminUser.name,
    creatorRole: adminUser.role,
    options: validOpts
  }).then(res => {
    if (res.code === '200') { ElMessage.success('创建成功'); data.pollDialogVisible = false; loadPolls() }
    else ElMessage.error(res.msg || '创建失败')
  }).finally(() => { data.pollSubmitting = false })
}

const endPoll = (id) => {
  ElMessageBox.confirm('确定结束此投票？结束后用户将无法投票。', '结束投票', { type: 'warning' }).then(() => {
    request.put('/poll/end/' + id).then(res => {
      if (res.code === '200') { ElMessage.success('投票已结束'); loadPolls() }
    })
  }).catch(() => {})
}

const deletePoll = (id) => {
  request.delete('/poll/delete/' + id).then(res => {
    if (res.code === '200') { ElMessage.success('已删除'); loadPolls() }
  })
}

onMounted(() => { load() })
</script>

<style scoped>
.forum-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== Tab切换 ===== */
.tab-bar { display: flex; gap: 4px; background: var(--bg-card); border-radius: 12px; padding: 4px; margin-bottom: 20px; border: 1px solid var(--border-lighter); box-shadow: var(--shadow-sm); }
.tab-item { padding: 10px 24px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; color: var(--text-secondary); transition: all 0.2s; display: flex; align-items: center; gap: 6px; }
.tab-item:hover { color: var(--text-primary); background: var(--bg-page); }
.tab-item.active { background: var(--primary-color); color: #fff; box-shadow: 0 2px 8px rgba(var(--primary-rgb), 0.3); }

/* ===== 页面标题 ===== */
.page-hero {
  background: var(--gradient-header);
  border-radius: 16px; padding: 28px 36px; margin-bottom: 20px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: var(--shadow-lg);
}
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); font-size: 28px; }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }
.hero-right :deep(.el-button) { background: rgba(255,255,255,0.2) !important; border: 1px solid rgba(255,255,255,0.35) !important; color: #fff !important; font-weight: 600; padding: 12px 24px; font-size: 15px; }
.hero-right :deep(.el-button:hover) { background: rgba(255,255,255,0.35) !important; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s, box-shadow 0.3s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px rgba(0,0,0,0.1); }
.stat-inner { padding: 20px 22px; color: #fff; position: relative; overflow: hidden; }
.stat-total .stat-inner { background: linear-gradient(135deg, #0f766e, #14b8a6); }
.stat-topped .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-locked .stat-inner { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-deleted .stat-inner { background: linear-gradient(135deg, #475569, #64748b); }
.stat-num { font-size: 34px; font-weight: 800; line-height: 1; margin-bottom: 4px; letter-spacing: -1px; }
.stat-label { font-size: 12px; font-weight: 600; opacity: 0.85; }
.stat-icon { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); font-size: 28px; opacity: 0.2; }

/* ===== 工具栏 ===== */
.toolbar {
  background: var(--bg-card); border-radius: 14px; padding: 16px 20px; margin-bottom: 16px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-lighter);
}
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.search-wrap { position: relative; width: 220px; }
.s-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--text-secondary); z-index: 1; pointer-events: none; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.filter-sel { width: 140px; }
.filter-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.reset-btn { border-radius: 10px; font-weight: 500; }
.total-tip { font-size: 13px; color: var(--text-secondary); }
.total-tip strong { color: var(--primary-color); }

/* ===== 帖子列表 ===== */
.post-list { background: var(--bg-card); border-radius: 14px; overflow: hidden; box-shadow: var(--shadow-sm); border: 1px solid var(--border-lighter); }
.post-card { padding: 20px 24px; border-bottom: 1px solid var(--border-lighter); transition: background 0.2s; }
.post-card:last-child { border-bottom: none; }
.post-card:hover { background: var(--bg-page); }
.post-card.is-deleted { opacity: 0.6; }
.post-card.is-top { background: rgba(234, 179, 8, 0.06); }
.post-card.is-top:hover { background: rgba(234, 179, 8, 0.1); }
.post-card.is-locked { background: rgba(99, 102, 241, 0.04); }
.post-card.is-locked:hover { background: rgba(99, 102, 241, 0.08); }
.post-badges { display: flex; gap: 6px; margin-bottom: 12px; }
.pin-badge, .lock-badge { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 700; }
.pin-badge { background: #fee2e2; color: #dc2626; }
.lock-badge { background: #e0e7ff; color: #4f46e5; }
.post-main { display: flex; align-items: flex-start; gap: 16px; }
.post-cover { width: 120px; height: 80px; border-radius: 10px; overflow: hidden; flex-shrink: 0; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.post-content { flex: 1; min-width: 0; }
.post-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.post-title { margin: 0; font-size: 16px; font-weight: 700; color: var(--text-primary); cursor: pointer; transition: color 0.2s; }
.post-title:hover { color: var(--primary-color); }
.status-chip { padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.st-active { background: var(--primary-light); color: var(--primary-color); }
.st-deleted { background: var(--bg-page); color: var(--text-secondary); }
.post-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
.tag-item { border-radius: 6px !important; font-size: 11px !important; }
.post-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 5px; font-size: 12px; color: var(--text-secondary); }
.meta-item .el-icon { font-size: 12px; }
.meta-sep { color: var(--border-light); }
.author-avatar { width: 20px; height: 20px; border-radius: 50%; overflow: hidden; background: var(--gradient-primary); display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; }
.author-avatar img { width: 100%; height: 100%; object-fit: cover; }
.author-avatar span { color: #fff; font-size: 10px; font-weight: 700; }
.author-name { font-weight: 600; color: var(--text-primary); }
.post-stats { display: flex; flex-direction: column; gap: 10px; flex-shrink: 0; min-width: 60px; }
.stat-item { display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--text-secondary); }
.post-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--border-lighter); }
.role-chip { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.role-owner { background: #fee2e2; color: #dc2626; }
.role-admin { background: #fef3c7; color: #d97706; }
.role-helper { background: var(--primary-light); color: var(--primary-color); }
.role-user { background: var(--bg-page); color: var(--text-secondary); }
.post-actions { display: flex; align-items: center; gap: 4px; }

.empty-state { padding: 80px; text-align: center; }
.empty-state p { margin: 16px 0 12px; color: var(--text-secondary); font-size: 14px; }
.pagination-wrap { padding: 16px 20px; display: flex; justify-content: flex-end; border-top: 1px solid var(--border-lighter); }

.detail-body { padding: 4px 0; }
.detail-header { display: flex; gap: 20px; margin-bottom: 20px; }
.detail-cover { width: 240px; height: 160px; border-radius: 12px; overflow: hidden; flex-shrink: 0; }
.detail-cover .el-image { width: 100%; height: 100%; }
.detail-title { flex: 1; }
.detail-tags { display: flex; gap: 6px; margin-bottom: 10px; }
.detail-title h2 { margin: 0 0 12px; font-size: 22px; font-weight: 800; color: var(--text-primary); }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.detail-stats { display: flex; gap: 16px; }
.detail-stats span { display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--text-secondary); }
.detail-badges { display: flex; gap: 6px; }

.cover-upload { display: flex; align-items: center; gap: 14px; }
.cover-uploader { cursor: pointer; }
.cover-preview { width: 200px; height: 120px; object-fit: cover; border-radius: 10px; display: block; }
.cover-placeholder { width: 200px; height: 120px; border: 2px dashed var(--border-light); border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; color: var(--text-secondary); font-size: 13px; transition: all 0.2s; }
.cover-placeholder:hover { border-color: var(--primary-color); color: var(--primary-color); }

/* ===== 投票管理卡片 ===== */
.poll-list { display: flex; flex-direction: column; gap: 16px; }

.poll-manage-card {
  background: var(--bg-card); border-radius: 14px; padding: 24px;
  border: 1px solid var(--border-lighter); box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}
.poll-manage-card:hover { box-shadow: var(--shadow-md); }

.pmc-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.pmc-left { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.pmc-status { padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.pmc-status.active { background: var(--primary-light); color: var(--primary-color); }
.pmc-status.ended { background: var(--bg-page); color: var(--text-secondary); }
.pmc-type { padding: 2px 8px; border-radius: 10px; font-size: 10px; font-weight: 600; background: rgba(var(--primary-rgb), 0.08); color: var(--primary-color); flex-shrink: 0; }
.pmc-title { font-size: 17px; font-weight: 700; color: var(--text-primary); margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pmc-actions { display: flex; gap: 8px; flex-shrink: 0; }
.pmc-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; line-height: 1.6; }
.pmc-meta { display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); margin-bottom: 16px; flex-wrap: wrap; }

.pmc-results { display: flex; flex-direction: column; gap: 10px; }
.pmc-result-row {}
.pmc-result-head { display: flex; justify-content: space-between; margin-bottom: 4px; }
.pmc-opt-text { font-size: 14px; color: var(--text-primary); }
.pmc-opt-pct { font-size: 13px; font-weight: 700; color: var(--primary-color); }
.pmc-bar-bg { height: 10px; background: var(--bg-page); border-radius: 5px; overflow: hidden; }
.pmc-bar { height: 100%; border-radius: 5px; background: var(--primary-color); transition: width 0.5s ease; min-width: 2px; }
.pmc-opt-count { font-size: 11px; color: var(--text-secondary); }

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .page-hero { flex-direction: column; align-items: flex-start; gap: 16px; }
  .post-main { flex-wrap: wrap; }
  .post-cover { display: none; }
  .toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
}
</style>
