<template>
  <div class="main-content" v-loading="data.loading">
    <div class="back-row">
      <el-button class="back-btn" @click="router.push('/front/forum')">
        <el-icon><ArrowLeft /></el-icon>
        返回论坛
      </el-button>
    </div>

    <div v-if="!data.loading && !data.post" class="card post-not-found">
      <div class="not-found-icon">😕</div>
      <div class="not-found-text">帖子不存在或已被删除</div>
      <el-button type="primary" @click="router.push('/front/forum')">返回论坛</el-button>
    </div>

    <div class="card post-card" v-if="data.post">
      <div class="post-header-row">
        <div class="author-info">
          <img v-if="data.post.authorAvatar" :src="data.post.authorAvatar" class="detail-avatar" />
          <div v-else class="detail-avatar detail-avatar-default">{{ (data.post.authorName || '?')[0] }}</div>
          <div>
            <div class="author-name-row">
              <span class="author-name">{{ data.post.authorName }}</span>
              <el-tag v-if="data.post.authorRole === 'OWNER'" type="danger" size="small">站长</el-tag>
              <el-tag v-else-if="data.post.authorRole === 'ADMIN'" type="warning" size="small">管理</el-tag>
              <el-tag v-else-if="data.post.authorRole === 'HELPER'" type="success" size="small">助手</el-tag>
              <el-tag v-if="data.post.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
              <el-tag v-if="data.post.isLocked" type="warning" size="small">锁定</el-tag>
            </div>
            <div class="author-time">{{ data.post.createdAt }}</div>
          </div>
        </div>
        <div v-if="isAuthor" class="author-actions">
          <el-button type="primary" link size="small" @click="handleEdit">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <h1 class="detail-title">{{ data.post.title }}</h1>

      <div v-if="data.post.tags" class="post-tags">
        <el-tag v-for="tag in data.post.tags.split(',')" :key="tag" size="small" effect="plain" class="tag-spacing">{{ tag }}</el-tag>
      </div>

      <div v-if="data.post.coverUrl" class="post-cover">
        <img :src="data.post.coverUrl" class="cover-img" />
      </div>

      <div class="detail-content" v-html="data.post.content"></div>

      <div class="action-bar">
        <div class="action-btn" :class="{ active: data.liked }" @click="toggleLike('post', data.post.id, 'like')">
          <el-icon><Star /></el-icon>
          <span>{{ data.liked ? '已点赞' : '点赞' }} {{ data.post.likeCount || 0 }}</span>
        </div>
        <div class="action-btn" :class="{ active: data.favorited }" @click="toggleLike('post', data.post.id, 'favorite')">
          <el-icon><CollectionTag /></el-icon>
          <span>{{ data.favorited ? '已收藏' : '收藏' }}</span>
        </div>
        <div class="action-btn">
          <el-icon><View /></el-icon>
          <span>{{ data.post.viewCount || 0 }}</span>
        </div>
        <div class="action-btn">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ data.post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="card" v-if="data.post">
      <div class="comment-header">
        <h3>评论 ({{ data.post.commentCount || 0 }})</h3>
      </div>

      <div v-if="!data.post.isLocked" class="comment-form">
        <el-input v-model="data.commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." maxlength="1000" show-word-limit />
        <div class="comment-form-actions">
          <el-button type="primary" size="small" @click="submitComment(null)">发表评论</el-button>
        </div>
      </div>
      <el-alert v-else title="帖子已锁定，无法评论" type="warning" :closable="false" class="locked-alert" />

      <el-dialog v-model="data.replyVisible" title="回复评论" width="500px" append-to-body destroy-on-close>
        <el-input v-model="data.replyContent" type="textarea" :rows="3" placeholder="写下你的回复..." maxlength="500" show-word-limit />
        <template #footer>
          <el-button @click="data.replyVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">回复</el-button>
        </template>
      </el-dialog>

      <div v-if="data.comments.length === 0" class="no-comments">
        暂无评论，快来抢沙发吧
      </div>
      <div v-for="comment in data.comments" :key="comment.id" class="comment-item">
        <div class="comment-main">
          <div class="comment-left">
            <img v-if="comment.userAvatar" :src="comment.userAvatar" class="comment-avatar" />
            <div v-else class="comment-avatar comment-avatar-default">{{ (comment.userName || '?')[0] }}</div>
          </div>
          <div class="comment-body">
            <div class="comment-user">
              <span class="comment-username">{{ comment.userName }}</span>
              <el-tag v-if="comment.userRole === 'OWNER'" type="danger" size="small">站长</el-tag>
              <el-tag v-else-if="comment.userRole === 'ADMIN'" type="warning" size="small">管理</el-tag>
              <el-tag v-else-if="comment.userRole === 'HELPER'" type="success" size="small">助手</el-tag>
              <span class="comment-time">{{ comment.createdAt }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-actions">
              <span class="comment-action" :class="{ active: comment._liked }" @click="toggleLike('comment', comment.id, 'like')">
                <el-icon><Star /></el-icon> {{ comment.likeCount || 0 }}
              </span>
              <span class="comment-action" @click="openReply(comment)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </span>
              <span v-if="canDeleteComment(comment)" class="comment-action delete" @click="deleteComment(comment.id)">
                <el-icon><Delete /></el-icon> 删除
              </span>
            </div>
          </div>
        </div>
        <div v-if="comment.children && comment.children.length" class="comment-children">
          <div v-for="child in comment.children" :key="child.id" class="comment-item child-comment">
            <div class="comment-main">
              <div class="comment-left">
                <img v-if="child.userAvatar" :src="child.userAvatar" class="comment-avatar small" />
                <div v-else class="comment-avatar comment-avatar-default small">{{ (child.userName || '?')[0] }}</div>
              </div>
              <div class="comment-body">
                <div class="comment-user">
                  <span class="child-username">{{ child.userName }}</span>
                  <span class="comment-time">{{ child.createdAt }}</span>
                </div>
                <div class="comment-text child-comment-text">{{ child.content }}</div>
                <div class="comment-actions">
                  <span class="comment-action" :class="{ active: child._liked }" @click="toggleLike('comment', child.id, 'like')">
                    <el-icon><Star /></el-icon> {{ child.likeCount || 0 }}
                  </span>
                  <span class="comment-action" @click="openReply(child)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </span>
                  <span v-if="canDeleteComment(child)" class="comment-action delete" @click="deleteComment(child.id)">
                    <el-icon><Delete /></el-icon> 删除
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, computed } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute()
const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const data = reactive({
  loading: false,
  post: null,
  comments: [],
  liked: false,
  favorited: false,
  commentContent: '',
  replyVisible: false,
  replyContent: '',
  replyTo: null
})

const isAuthor = computed(() => {
  return data.post && user.id && data.post.authorId === user.id
})

const isAdminUser = computed(() => {
  return ['OWNER', 'ADMIN', 'HELPER'].includes(user.role)
})

const canDeleteComment = (comment) => {
  if (!user.id) return false
  return (comment.authorId === user.id) || isAdminUser.value
}

const loadPost = () => {
  const id = route.query.id
  if (!id) {
    router.push('/front/forum')
    return
  }
  data.loading = true
  request.get('/forumPost/selectById/' + id).then(res => {
    data.post = res.data
    data.loading = false
    if (data.post && user.id) {
      checkLike('post', data.post.id, 'like', v => data.liked = v)
      checkLike('post', data.post.id, 'favorite', v => data.favorited = v)
    }
  }).catch(() => {
    data.loading = false
  })
}

const loadComments = () => {
  const id = route.query.id
  request.get('/forumComment/tree/' + id).then(res => {
    data.comments = res.data || []
  })
}

const checkLike = (targetType, targetId, type, cb) => {
  request.get('/forumLike/check', {
    params: { targetType, targetId, type }
  }).then(res => {
    cb(res.data)
  })
}

const toggleLike = (targetType, targetId, type) => {
  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }
  request.post('/forumLike/toggle', { targetType, targetId, type }).then(res => {
    if (res.code === '200') {
      const liked = res.data
      if (targetType === 'post' && type === 'like') {
        data.liked = liked
        data.post.likeCount = (data.post.likeCount || 0) + (liked ? 1 : -1)
      } else if (targetType === 'post' && type === 'favorite') {
        data.favorited = liked
      } else if (targetType === 'comment') {
        const comment = findComment(data.comments, targetId)
        if (comment) {
          comment._liked = liked
          comment.likeCount = (comment.likeCount || 0) + (liked ? 1 : -1)
        }
      }
    }
  })
}

const findComment = (list, id) => {
  for (const c of list) {
    if (c.id === id) return c
    if (c.children) {
      const found = findComment(c.children, id)
      if (found) return found
    }
  }
  return null
}

const submitComment = (parentId) => {
  if (!user.id) return ElMessage.warning('请先登录')
  if (!data.commentContent.trim()) return ElMessage.warning('请输入评论内容')
  request.post('/forumComment/add', {
    postId: data.post.id,
    content: data.commentContent,
    parentId: parentId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.commentContent = ''
      loadComments()
      data.post.commentCount = (data.post.commentCount || 0) + 1
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const openReply = (comment) => {
  if (!user.id) return ElMessage.warning('请先登录')
  data.replyTo = comment
  data.replyContent = ''
  data.replyVisible = true
}

const submitReply = () => {
  if (!data.replyContent.trim()) return ElMessage.warning('请输入回复内容')
  request.post('/forumComment/add', {
    postId: data.post.id,
    content: data.replyContent,
    parentId: data.replyTo.id
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('回复成功')
      data.replyVisible = false
      loadComments()
      data.post.commentCount = (data.post.commentCount || 0) + 1
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const deleteComment = (id) => {
  ElMessageBox.confirm('确定删除这条评论吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/forumComment/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('已删除')
        loadComments()
        data.post.commentCount = Math.max(0, (data.post.commentCount || 0) - 1)
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleEdit = () => {
  router.push('/front/forumEdit?id=' + data.post.id)
}

const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这篇帖子吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/forumPost/delete/' + data.post.id).then(res => {
      if (res.code === '200') {
        ElMessage.success('已删除')
        router.push('/front/forum')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.back-row {
  margin-bottom: 16px;
}

.back-btn {
  color: var(--text-secondary);
  border: 1px solid var(--border-lighter);
  background: var(--bg-card);
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.2s;
}

.back-btn:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.post-not-found {
  text-align: center;
  padding: 60px;
}

.not-found-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.not-found-text {
  color: var(--text-secondary);
  font-size: 16px;
  margin-bottom: 20px;
}

.post-card {
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-name {
  font-weight: 600;
}

.author-time {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 2px;
}

.author-actions {
  display: flex;
  gap: 8px;
}

.post-tags {
  margin-bottom: 12px;
}

.tag-spacing {
  margin-right: 6px;
}

.post-cover {
  margin-bottom: 16px;
}

.cover-img {
  max-width: 100%;
  border-radius: 8px;
}

.detail-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.detail-avatar-default {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: var(--bg-card);
  font-weight: 600;
  font-size: 20px;
}

.post-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.detail-content {
  line-height: 1.8;
  font-size: 15px;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.detail-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

.action-bar {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-top: 1px solid var(--border-lighter);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
  user-select: none;
}

.action-btn:hover {
  color: var(--primary-color);
}

.action-btn.active {
  color: var(--primary-color);
}

.comment-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-lighter);
}

.comment-header h3 {
  margin: 0;
  font-size: 16px;
}

.comment-form {
  margin-bottom: 20px;
}

.comment-form-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.locked-alert {
  margin-bottom: 16px;
}

.no-comments {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px 0;
}

.comment-item {
  padding: 12px 0;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-avatar.small {
  width: 32px;
  height: 32px;
}

.comment-avatar-default {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: var(--bg-card);
  font-weight: 600;
  font-size: 16px;
}

.comment-avatar-default.small {
  font-size: 14px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.comment-username {
  font-weight: 600;
  font-size: 14px;
}

.child-username {
  font-weight: 600;
  font-size: 13px;
}

.comment-time {
  color: var(--text-secondary);
  font-size: 12px;
  margin-left: 8px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.child-comment-text {
  font-size: 13px;
}

.comment-actions {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 3px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.comment-action:hover {
  color: var(--primary-color);
}

.comment-action.active {
  color: var(--primary-color);
}

.comment-action.delete:hover {
  color: #f56c6c;
}

.comment-children {
  margin-left: 52px;
  padding-left: 16px;
  border-left: 2px solid var(--border-lighter);
}

.child-comment {
  padding: 8px 0;
}
</style>
