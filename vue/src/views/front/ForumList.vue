<template>
  <div class="main-content">
    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="page-hero-bg"></div>
      <div class="page-hero-content">
        <div class="page-hero-left">
          <div class="page-hero-icon">💬</div>
          <div>
            <div class="page-hero-title">交流论坛</div>
            <div class="page-hero-subtitle">分享经验，交流心得，与小伙伴们一起进步</div>
          </div>
        </div>
        <div class="page-hero-right">
          <div class="page-hero-actions">
            <el-button type="primary" size="large" round @click="handlePost" v-if="user.id">
              <el-icon><EditPen /></el-icon> 发布新帖
            </el-button>
            <el-button
              v-if="isAdmin"
              size="large"
              round
              @click="openPollDialog"
              style="background: rgba(255,255,255,0.12); border-color: rgba(255,255,255,0.2); color: #fff;"
            >
              📊 发起投票
            </el-button>
            <el-button size="large" round @click="data.orderBy = 'hot'; load()" class="hot-btn">
              🔥 热门帖子
            </el-button>
          </div>
          <div class="page-hero-stats">
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.total }}</div>
              <div class="page-hero-stat-lbl">帖子</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.activePolls.length }}</div>
              <div class="page-hero-stat-lbl">进行中投票</div>
            </div>
            <div class="page-hero-stat-div"></div>
            <div class="page-hero-stat">
              <div class="page-hero-stat-val">{{ data.activeUsers }}</div>
              <div class="page-hero-stat-lbl">活跃用户</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 进行中的投票 - 醒目横幅 -->
    <div v-if="data.activePolls.length > 0" class="poll-spotlight">
      <div class="poll-spotlight-header">
        <div class="poll-spotlight-left">
          <span class="poll-spotlight-badge">📊 投票进行中</span>
          <span class="poll-spotlight-count">{{ data.activePolls.length }} 个活跃投票</span>
        </div>
        <el-button size="small" text type="primary" @click="data.showAllPolls = !data.showAllPolls">
          {{ data.showAllPolls ? '收起' : '查看全部' }}
          <el-icon><component :is="data.showAllPolls ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
        </el-button>
      </div>

      <div class="poll-cards-wrap" :class="{ expanded: data.showAllPolls }">
        <div
          v-for="poll in (data.showAllPolls ? data.activePolls : data.activePolls.slice(0, 2))"
          :key="poll.id"
          class="poll-card"
        >
          <div class="poll-card-header">
            <div class="poll-card-title-row">
              <span class="poll-card-badge">{{ poll.maxChoices > 1 ? '多选' : '单选' }}</span>
              <span class="poll-card-title">{{ poll.title }}</span>
            </div>
            <div class="poll-card-meta">
              <span>👤 {{ poll.creatorName }}</span>
              <span v-if="poll.endTime">⏰ {{ formatEndTime(poll.endTime) }}</span>
              <span>👥 {{ poll.totalVotes || 0 }}人参与</span>
            </div>
            <div class="poll-card-desc" v-if="poll.description">{{ poll.description }}</div>
          </div>

          <!-- 已投票 - 显示结果 -->
          <div v-if="poll.hasVoted" class="poll-results">
            <div v-for="opt in poll.options" :key="opt.id" class="poll-result-item">
              <div class="poll-result-header">
                <span class="poll-result-label">
                  <span v-if="poll.votedOptionIds?.includes(opt.id)" class="poll-voted-check">✓</span>
                  {{ opt.content }}
                </span>
                <span class="poll-result-pct">{{ opt.percentage }}%</span>
              </div>
              <div class="poll-result-bar-bg">
                <div
                  class="poll-result-bar"
                  :class="{ voted: poll.votedOptionIds?.includes(opt.id) }"
                  :style="{ width: opt.percentage + '%', background: poll.votedOptionIds?.includes(opt.id) ? (opt.color || 'var(--primary-color)') : undefined }"
                ></div>
              </div>
              <span class="poll-result-count">{{ opt.voteCount }} 票</span>
            </div>
            <div class="poll-voted-footer">
              <span>已投票 ✓</span>
              <span>共 {{ poll.totalVotes }} 人参与</span>
            </div>
          </div>

          <!-- 未投票 - 显示选项 -->
          <div v-else class="poll-options">
            <div
              v-for="opt in poll.options"
              :key="opt.id"
              class="poll-option"
              :class="{ selected: data.selectedOptions[poll.id]?.includes(opt.id) }"
              @click="toggleOption(poll, opt)"
            >
              <div class="poll-option-check">
                <span v-if="data.selectedOptions[poll.id]?.includes(opt.id)" class="poll-option-checked"></span>
              </div>
              <span class="poll-option-text">{{ opt.content }}</span>
            </div>
            <div class="poll-vote-actions">
              <el-button
                type="primary"
                round
                size="small"
                :disabled="!data.selectedOptions[poll.id]?.length"
                :loading="data.votingLoading === poll.id"
                @click.stop="submitVote(poll)"
              >
                投票
              </el-button>
              <span class="poll-hint" v-if="poll.maxChoices > 1">
                最多选 {{ poll.maxChoices }} 项，已选 {{ data.selectedOptions[poll.id]?.length || 0 }}
              </span>
            </div>
          </div>

          <!-- 管理员操作 -->
          <div class="poll-card-actions" v-if="isAdmin">
            <el-button size="small" text type="danger" @click.stop="endPoll(poll.id)">
              <el-icon><CircleClose /></el-icon> 结束投票
            </el-button>
            <el-button size="small" text type="danger" @click.stop="deletePoll(poll.id)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索 + 排序 -->
    <div class="card toolbar-card">
      <div class="toolbar-inner">
        <div class="toolbar-search">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="data.title" placeholder="搜索帖子" @keyup.enter="load" />
            <el-icon v-if="data.title" class="search-clear" @click="data.title = ''; load()"><Close /></el-icon>
          </div>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <div class="toolbar-tabs">
          <div class="tab" :class="{ active: data.orderBy === 'latest' }" @click="data.orderBy = 'latest'; load()">最新</div>
          <div class="tab" :class="{ active: data.orderBy === 'hot' }" @click="data.orderBy = 'hot'; load()">最热</div>
        </div>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div v-if="data.posts.length === 0" class="card">
      <div class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">暂无帖子，快来发帖吧</div>
      </div>
    </div>
    <div v-else class="post-list">
      <div v-for="item in data.posts" :key="item.id" class="post-item" @click="goDetail(item)">
        <div class="post-left">
          <img v-if="item.authorAvatar" :src="item.authorAvatar" class="post-avatar" />
          <div v-else class="post-avatar post-avatar-default">{{ (item.authorName || '?')[0] }}</div>
        </div>
        <div class="post-body">
          <div class="post-header">
            <span class="post-author">{{ item.authorName }}</span>
            <el-tag v-if="item.authorRole === 'OWNER'" type="danger" size="small">站长</el-tag>
            <el-tag v-else-if="item.authorRole === 'ADMIN'" type="warning" size="small">管理</el-tag>
            <el-tag v-else-if="item.authorRole === 'HELPER'" type="success" size="small">助手</el-tag>
            <el-tag v-if="item.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
            <el-tag v-if="item.isLocked" type="warning" size="small">锁定</el-tag>
            <span class="post-time">{{ item.createdAt }}</span>
          </div>
          <div class="post-title">{{ item.title }}</div>
          <div class="post-summary" v-if="item.content">{{ stripHtml(item.content).slice(0, 120) }}...</div>
          <div class="post-footer">
            <span v-if="item.tags" class="post-tags">
              <el-tag v-for="tag in item.tags.split(',')" :key="tag" size="small" effect="plain" style="margin-right: 4px">{{ tag }}</el-tag>
            </span>
            <span class="post-stats">
              <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ item.commentCount || 0 }}</span>
              <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div style="display: flex; justify-content: center; margin-top: 24px">
      <el-pagination @current-change="load" :current-page="data.pageNum" :page-size="data.pageSize" layout="prev, pager, next" :total="data.total" background />
    </div>

    <!-- 发帖弹窗 -->
    <el-dialog v-model="data.formVisible" title="发布帖子" width="700px" destroy-on-close>
      <el-form :model="data.form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="data.form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="data.form.tags" placeholder="多个标签用逗号分隔，如：教程,求助,讨论" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :action="uploadUrl" :on-success="handleCoverSuccess" :show-file-list="false" accept="image/*">
            <img v-if="data.form.coverUrl" :src="data.form.coverUrl" style="width: 200px; height: 120px; object-fit: cover; border-radius: 8px" />
            <el-button v-else type="primary" plain size="small">上传封面</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="data.form.content" type="textarea" :rows="10" placeholder="请输入帖子内容（支持HTML）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>

    <!-- 创建投票弹窗 -->
    <el-dialog v-model="data.pollDialogVisible" title="📊 发起新投票" width="640px" destroy-on-close>
      <el-form :model="data.pollForm" label-width="90px">
        <el-form-item label="投票标题" required>
          <el-input v-model="data.pollForm.title" placeholder="请输入投票标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="投票描述">
          <el-input v-model="data.pollForm.description" type="textarea" :rows="3" placeholder="简要描述这次投票的主题..." />
        </el-form-item>
        <el-form-item label="投票类型">
          <el-radio-group v-model="data.pollForm.maxChoices">
            <el-radio-button :value="1">单选</el-radio-button>
            <el-radio-button :value="2">多选 (最多2项)</el-radio-button>
            <el-radio-button :value="3">多选 (最多3项)</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="data.pollForm.endTime"
            type="datetime"
            placeholder="选择截止时间（可选，不选则永不过期）"
            style="width: 100%"
            :disabled-date="d => d < new Date()"
          />
        </el-form-item>
        <el-form-item label="匿名投票">
          <el-switch v-model="data.pollForm.isAnonymous" active-text="匿名" inactive-text="公开" />
        </el-form-item>
        <el-form-item label="投票选项" required>
          <div class="poll-options-editor">
            <div v-for="(opt, idx) in data.pollForm.options" :key="idx" class="poll-opt-row">
              <span class="poll-opt-num">{{ idx + 1 }}</span>
              <el-input v-model="opt.content" :placeholder="'选项 ' + (idx + 1)" />
              <el-button v-if="data.pollForm.options.length > 2" type="danger" text @click="data.pollForm.options.splice(idx, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button v-if="data.pollForm.options.length < 10" type="primary" plain size="small" @click="data.pollForm.options.push({ content: '' })" style="margin-top: 8px;">
              + 添加选项
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.pollDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPoll" :loading="data.pollSubmitting">发布投票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { EditPen, Search, Close, Delete, ArrowUp, ArrowDown, CircleClose } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
const isAdmin = ['OWNER', 'ADMIN'].includes(user.role)

const data = reactive({
  title: '',
  orderBy: 'latest',
  posts: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  activeUsers: 0,
  activePolls: [],
  showAllPolls: false,
  selectedOptions: {},
  votingLoading: null,
  formVisible: false,
  form: {},
  pollDialogVisible: false,
  pollSubmitting: false,
  pollForm: { title: '', description: '', maxChoices: 1, endTime: null, isAnonymous: false, options: [{ content: '' }, { content: '' }] }
})

const load = () => {
  request.get('/forumPost/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, title: data.title || null, status: 'active' }
  }).then(res => {
    let list = res.data?.list || []
    if (data.orderBy === 'hot') {
      list.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
    }
    data.posts = list
    data.total = res.data?.total || 0
    const authorSet = new Set(list.map(p => p.authorId).filter(Boolean))
    data.activeUsers = authorSet.size
  })
}

const loadPolls = () => {
  request.get('/poll/activePolls', { params: { userId: user.id || null } }).then(res => {
    if (res.code === '200') {
      data.activePolls = res.data || []
    }
  }).catch(() => {})
}

const toggleOption = (poll, opt) => {
  if (!data.selectedOptions[poll.id]) data.selectedOptions[poll.id] = []
  const arr = data.selectedOptions[poll.id]
  const idx = arr.indexOf(opt.id)
  if (idx >= 0) {
    arr.splice(idx, 1)
  } else {
    if (poll.maxChoices > 1) {
      if (arr.length >= poll.maxChoices) {
        ElMessage.warning(`最多只能选择${poll.maxChoices}项`)
        return
      }
      arr.push(opt.id)
    } else {
      data.selectedOptions[poll.id] = [opt.id]
    }
  }
}

const submitVote = (poll) => {
  const optionIds = data.selectedOptions[poll.id]
  if (!optionIds?.length) return ElMessage.warning('请选择选项')
  data.votingLoading = poll.id
  request.post('/poll/vote', {
    pollId: poll.id,
    userId: user.id,
    userRole: user.role,
    optionIds
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('投票成功！')
      const idx = data.activePolls.findIndex(p => p.id === poll.id)
      if (idx >= 0) data.activePolls[idx] = res.data
      delete data.selectedOptions[poll.id]
    } else {
      ElMessage.error(res.msg || '投票失败')
    }
  }).finally(() => { data.votingLoading = null })
}

const endPoll = (id) => {
  ElMessageBox.confirm('确定要结束此投票吗？结束后用户将无法继续投票。', '结束投票', { type: 'warning' }).then(() => {
    request.put('/poll/end/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('投票已结束')
        loadPolls()
      }
    })
  }).catch(() => {})
}

const deletePoll = (id) => {
  ElMessageBox.confirm('确定要删除此投票吗？此操作不可撤销。', '删除投票', { type: 'warning' }).then(() => {
    request.delete('/poll/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('已删除')
        loadPolls()
      }
    })
  }).catch(() => {})
}

const formatEndTime = (dt) => {
  if (!dt) return '无限期'
  const d = new Date(dt)
  const now = new Date()
  const diff = d - now
  if (diff <= 0) return '已截止'
  if (diff < 3600000) return Math.ceil(diff / 60000) + '分钟后截止'
  if (diff < 86400000) return Math.ceil(diff / 3600000) + '小时后截止'
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')} 截止`
}

const openPollDialog = () => {
  data.pollForm = {
    title: '', description: '', maxChoices: 1, endTime: null, isAnonymous: false,
    options: [{ content: '' }, { content: '' }]
  }
  data.pollDialogVisible = true
}

const submitPoll = () => {
  if (!data.pollForm.title) return ElMessage.warning('请输入投票标题')
  const validOptions = data.pollForm.options.filter(o => o.content.trim())
  if (validOptions.length < 2) return ElMessage.warning('至少需要2个有效选项')

  data.pollSubmitting = true
  request.post('/poll/add', {
    title: data.pollForm.title,
    description: data.pollForm.description,
    maxChoices: data.pollForm.maxChoices,
    endTime: data.pollForm.endTime ? new Date(data.pollForm.endTime).toISOString().slice(0, 19) : null,
    isAnonymous: data.pollForm.isAnonymous,
    creatorId: user.id,
    creatorName: user.name,
    creatorRole: user.role,
    options: validOptions
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('投票创建成功！')
      data.pollDialogVisible = false
      loadPolls()
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  }).finally(() => { data.pollSubmitting = false })
}

const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html || ''
  return tmp.textContent || ''
}

const goDetail = (item) => {
  router.push('/front/forumDetail?id=' + item.id)
}

const handlePost = () => {
  if (!user.id) { ElMessage.warning('请先登录'); router.push('/login'); return }
  data.form = { title: '', content: '', tags: '', coverUrl: '' }
  data.formVisible = true
}

const submitPost = () => {
  if (!data.form.title) return ElMessage.warning('请输入标题')
  if (!data.form.content) return ElMessage.warning('请输入内容')
  request.post('/forumPost/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('发布成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleCoverSuccess = (res) => {
  if (res.code === '200') data.form.coverUrl = res.data
}

onMounted(() => {
  load()
  loadPolls()
})
</script>

<style scoped>
.page-hero {
  position: relative;
  background: var(--gradient-header);
  border-radius: 18px;
  padding: 32px 36px;
  margin-bottom: 24px;
  overflow: hidden;
}
.page-hero-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 80% 50%, rgba(var(--primary-rgb), 0.15) 0%, transparent 60%);
  pointer-events: none;
}
.page-hero-content { display: flex; justify-content: space-between; align-items: center; position: relative; gap: 24px; }
.page-hero-left { display: flex; align-items: center; gap: 16px; }
.page-hero-icon { font-size: 40px; line-height: 1; }
.page-hero-title { margin: 0 0 6px; font-size: 26px; font-weight: 800; color: #fff; }
.page-hero-subtitle { margin: 0; font-size: 13px; color: rgba(255,255,255,0.6); }
.page-hero-right { display: flex; flex-direction: column; align-items: flex-end; gap: 16px; }
.page-hero-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.hot-btn { background: rgba(255,255,255,0.1) !important; border-color: rgba(255,255,255,0.2) !important; color: #fff !important; }
.hot-btn:hover { background: rgba(255,255,255,0.15) !important; }
.page-hero-stats { display: flex; gap: 24px; }
.page-hero-stat { text-align: center; }
.page-hero-stat-val { font-size: 24px; font-weight: 800; color: #fff; line-height: 1; }
.page-hero-stat-lbl { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; }
.page-hero-stat-div { width: 1px; height: 32px; background: rgba(255,255,255,0.15); }

/* ========== 投票醒目区 ========== */
.poll-spotlight {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  border: 1px solid var(--border-lighter);
  position: relative;
  overflow: hidden;
}
.poll-spotlight::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(var(--primary-rgb), 0.06);
}
.poll-spotlight-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.poll-spotlight-left { display: flex; align-items: center; gap: 12px; }
.poll-spotlight-badge {
  background: var(--gradient-primary);
  color: #fff;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  animation: pollPulse 2s ease-in-out infinite;
}
@keyframes pollPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(var(--primary-rgb), 0.3); }
  50% { box-shadow: 0 0 0 8px rgba(var(--primary-rgb), 0); }
}
.poll-spotlight-count { font-size: 13px; color: var(--text-secondary); }

.poll-cards-wrap { display: grid; grid-template-columns: repeat(auto-fill, minmax(480px, 1fr)); gap: 16px; }
.poll-cards-wrap:not(.expanded) { max-height: 350px; overflow: hidden; }

.poll-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px;
  border: 1px solid var(--border-lighter);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}
.poll-card:hover { box-shadow: var(--shadow-md); }

.poll-card-header { margin-bottom: 16px; }
.poll-card-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.poll-card-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
  flex-shrink: 0;
}
.poll-card-title { font-size: 16px; font-weight: 700; color: var(--text-primary); }
.poll-card-meta { display: flex; gap: 14px; font-size: 12px; color: var(--text-secondary); flex-wrap: wrap; }
.poll-card-desc { font-size: 13px; color: var(--text-secondary); margin-top: 6px; line-height: 1.5; }

/* 投票选项 */
.poll-options { display: flex; flex-direction: column; gap: 8px; }
.poll-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1.5px solid var(--border-light);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-page);
}
.poll-option:hover { border-color: var(--primary-color); background: var(--primary-light); }
.poll-option.selected { border-color: var(--primary-color); background: var(--primary-light); }
.poll-option-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}
.poll-option.selected .poll-option-check { border-color: var(--primary-color); background: var(--primary-color); }
.poll-option-checked {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}
.poll-option-text { font-size: 14px; color: var(--text-primary); }

.poll-vote-actions { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.poll-hint { font-size: 12px; color: var(--text-secondary); }

/* 投票结果 */
.poll-results { display: flex; flex-direction: column; gap: 12px; }
.poll-result-item {}
.poll-result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.poll-result-label { font-size: 14px; color: var(--text-primary); display: flex; align-items: center; gap: 6px; }
.poll-voted-check { color: var(--primary-color); font-weight: 700; font-size: 14px; }
.poll-result-pct { font-size: 13px; font-weight: 700; color: var(--primary-color); }
.poll-result-bar-bg {
  height: 10px;
  background: var(--bg-page);
  border-radius: 5px;
  overflow: hidden;
}
.poll-result-bar {
  height: 100%;
  border-radius: 5px;
  background: var(--border-light);
  transition: width 0.6s ease;
}
.poll-result-bar.voted { background: var(--primary-color); }
.poll-result-count { font-size: 11px; color: var(--text-secondary); margin-top: 2px; }
.poll-voted-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-lighter);
}

.poll-card-actions { display: flex; gap: 8px; margin-top: 12px; padding-top: 10px; border-top: 1px dashed var(--border-lighter); }

/* 投票编辑器 */
.poll-options-editor { width: 100%; }
.poll-opt-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.poll-opt-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

/* 工具栏 */
.toolbar-card { margin-bottom: 16px; }
.toolbar-inner { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.toolbar-search { display: flex; align-items: center; gap: 10px; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg-page); border-radius: 10px; padding: 0 12px;
  border: 1px solid var(--border-lighter); height: 36px; width: 260px;
}
.search-box input { border: none; outline: none; flex: 1; font-size: 14px; background: transparent; color: var(--text-primary); }
.search-box input::placeholder { color: var(--text-secondary); }
.search-icon { color: var(--text-secondary); font-size: 14px; }
.search-clear { color: var(--text-secondary); font-size: 14px; cursor: pointer; }
.toolbar-tabs { display: flex; gap: 6px; background: var(--bg-page); border-radius: 8px; padding: 4px; border: 1px solid var(--border-lighter); }
.tab { padding: 5px 16px; border-radius: 6px; font-size: 13px; cursor: pointer; color: var(--text-secondary); transition: all 0.2s; }
.tab:hover { color: var(--text-primary); }
.tab.active { background: var(--primary-color); color: #fff; font-weight: 500; }

/* 帖子列表 */
.post-list { display: flex; flex-direction: column; gap: 10px; }
.post-item {
  background: var(--bg-card); border-radius: 14px; padding: 18px 20px;
  display: flex; gap: 14px; cursor: pointer; transition: all 0.2s;
  border: 1px solid var(--border-lighter);
}
.post-item:hover { border-color: var(--primary-color); box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.post-avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.post-avatar-default {
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #6366f1, #a78bfa); color: #fff; font-weight: 600; font-size: 18px;
}
.post-body { flex: 1; min-width: 0; }
.post-header { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; flex-wrap: wrap; }
.post-author { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.post-title { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-summary { font-size: 13px; color: var(--text-secondary); margin-bottom: 10px; line-height: 1.5; }
.post-footer { display: flex; align-items: center; gap: 12px; font-size: 12px; color: var(--text-secondary); flex-wrap: wrap; }
.post-stats { display: flex; gap: 12px; margin-left: auto; }
.post-stats span { display: flex; align-items: center; gap: 3px; }
.post-time { color: var(--text-secondary); font-size: 12px; margin-left: auto; }

.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: var(--text-secondary); font-size: 14px; }

@media (max-width: 768px) {
  .page-hero-content { flex-direction: column; align-items: flex-start; }
  .page-hero-right { align-items: flex-start; width: 100%; }
  .page-hero-stats { width: 100%; justify-content: space-around; }
  .poll-cards-wrap { grid-template-columns: 1fr; }
}
</style>
