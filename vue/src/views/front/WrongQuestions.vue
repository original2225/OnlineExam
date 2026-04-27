<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">📝</div>
      <div class="page-hero-title">错题本</div>
      <div class="page-hero-subtitle">温故知新，从错误中学习</div>
      <div class="page-hero-stats">
        <div class="page-hero-stat"><div class="page-hero-stat-val">{{ data.wrongList.length }}</div><div class="page-hero-stat-lbl">错题总数</div></div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat"><div class="page-hero-stat-val">{{ data.reviewCount }}</div><div class="page-hero-stat-lbl">已复习</div></div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <el-input v-model="data.searchText" placeholder="搜索题目内容" clearable style="width: 240px;" size="default">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="data.diffFilter" placeholder="难度" clearable style="width: 110px;" size="default">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
        <el-select v-model="data.typeFilter" placeholder="题型" clearable style="width: 110px;" size="default">
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
          <el-option label="填空题" value="fillin" />
          <el-option label="简答题" value="essay" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button type="primary" @click="startReviewMode" :disabled="filteredQuestions.length === 0">
          <el-icon><EditPen /></el-icon> 错题重练
        </el-button>
      </div>
    </div>

    <div v-if="filteredQuestions.length === 0" class="empty-state">
      <div class="empty-icon">🎉</div>
      <div class="empty-text">{{ data.wrongList.length === 0 ? '暂无错题，继续保持！' : '没有符合条件的错题' }}</div>
    </div>

    <div v-else class="wrong-list">
      <div v-for="(wq, idx) in filteredQuestions" :key="wq.id" class="wrong-card">
        <div class="wrong-header">
          <span class="wrong-index">{{ idx + 1 }}</span>
          <el-tag v-if="wq.question?.type === 'single'" type="success" size="small">单选</el-tag>
          <el-tag v-else-if="wq.question?.type === 'multiple'" type="primary" size="small">多选</el-tag>
          <el-tag v-else-if="wq.question?.type === 'judge'" type="warning" size="small">判断</el-tag>
          <el-tag v-else-if="wq.question?.type === 'fillin'" type="info" size="small">填空</el-tag>
          <el-tag v-else type="info" size="small">简答</el-tag>
          <el-tag v-if="wq.question?.difficulty === 'easy'" type="success" size="small" effect="light">简单</el-tag>
          <el-tag v-else-if="wq.question?.difficulty === 'medium'" type="warning" size="small" effect="light">中等</el-tag>
          <el-tag v-else-if="wq.question?.difficulty === 'hard'" type="danger" size="small" effect="light">困难</el-tag>
          <span class="wrong-source">
            {{ wq.source === 'exam' ? '来自考试' : wq.source === 'practice' ? '来自练习' : '手动添加' }}
          </span>
          <span class="wrong-time">{{ formatTime(wq.createdAt) }}</span>
          <el-button size="small" type="danger" text @click="removeWrong(wq.id)">
            <el-icon><Delete /></el-icon> 移除
          </el-button>
        </div>

        <div class="wrong-content">{{ wq.question?.content }}</div>

        <div v-if="wq.question?.options && ['single', 'multiple'].includes(wq.question?.type)" class="options-preview">
          <div v-for="(text, key) in wq.question.options" :key="key"
               class="opt-row" :class="{
                 'opt-correct': wq.question.answer?.includes(key),
                 'opt-wrong': wq.wrongAnswer?.includes(key)
               }">
            <span class="opt-key">{{ key }}.</span>
            <span>{{ text }}</span>
            <el-tag v-if="wq.question.answer?.includes(key)" type="success" size="small" style="margin-left: auto;">正确</el-tag>
            <el-tag v-if="wq.wrongAnswer?.includes(key) && !wq.question.answer?.includes(key)" type="danger" size="small" style="margin-left: 4px;">你的选择</el-tag>
          </div>
        </div>

        <div v-if="wq.question?.type === 'fillin'" class="answer-rows">
          <div class="ans-row"><span class="ans-label">你的答案：</span><span class="wrong-text">{{ formatFillin(wq.wrongAnswer) }}</span></div>
          <div class="ans-row"><span class="ans-label">正确答案：</span><span class="correct-text">{{ formatFillin(wq.question?.answer) }}</span></div>
        </div>
        <div v-else-if="wq.question?.type !== 'essay'" class="answer-rows">
          <div class="ans-row"><span class="ans-label">你的答案：</span><span class="wrong-text">{{ wq.wrongAnswer || '未作答' }}</span></div>
          <div class="ans-row"><span class="ans-label">正确答案：</span><span class="correct-text">{{ wq.question?.answer }}</span></div>
        </div>

        <div v-if="wq.question?.analysis" class="analysis-box">
          <strong>解析：</strong>{{ wq.question.analysis }}
        </div>

        <div class="annotations-section" v-if="wq.annotations?.length">
          <div class="anno-header">💡 满分考生批注</div>
          <div v-for="anno in wq.annotations" :key="anno.id" class="anno-item">
            <div class="anno-meta">
              <span class="anno-user">{{ anno.userName }}</span>
              <span class="anno-time">{{ formatTime(anno.createdAt) }}</span>
            </div>
            <div class="anno-content">{{ anno.content }}</div>
            <div class="anno-actions">
              <span class="anno-like" @click="likeAnnotation(anno)">
                👍 {{ anno.likeCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { ElMessage, ElMessageBox } from "element-plus";

const data = reactive({
  wrongList: [],
  searchText: '',
  diffFilter: '',
  typeFilter: '',
  reviewCount: 0,
})

const user = computed(() => JSON.parse(localStorage.getItem('xm-user') || '{}'))

const filteredQuestions = computed(() => {
  let list = data.wrongList
  if (data.searchText) {
    const s = data.searchText.toLowerCase()
    list = list.filter(q => q.question?.content?.toLowerCase().includes(s))
  }
  if (data.diffFilter) {
    list = list.filter(q => q.question?.difficulty === data.diffFilter)
  }
  if (data.typeFilter) {
    list = list.filter(q => q.question?.type === data.typeFilter)
  }
  return list
})

const loadWrongQuestions = () => {
  request.get('/wrongQuestion/selectByUser', {
    params: { userId: user.value.id, userRole: user.value.role }
  }).then(res => {
    if (res.code === '200') {
      data.wrongList = res.data || []
      data.wrongList.forEach(wq => {
        if (wq.questionId) {
          request.get('/questionAnnotation/selectByQuestion', {
            params: { questionId: wq.questionId }
          }).then(r => {
            if (r.code === '200') wq.annotations = r.data || []
          })
        }
      })
    }
  })
}

const removeWrong = (id) => {
  ElMessageBox.confirm('确定要从错题集中移除吗？', '确认', { type: 'warning' }).then(() => {
    request.delete('/wrongQuestion/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('已移除')
        loadWrongQuestions()
      }
    })
  }).catch(() => {})
}

const likeAnnotation = (anno) => {
  request.put('/questionAnnotation/like/' + anno.id).then(res => {
    if (res.code === '200') {
      anno.likeCount = (anno.likeCount || 0) + 1
    }
  })
}

const startReviewMode = () => {
  const questionIds = filteredQuestions.value.map(q => q.questionId).filter(Boolean)
  if (questionIds.length === 0) return
  router.push({ path: '/front/practiceMode', query: { mode: 'wrongReview' } })
}

const formatTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const formatFillin = (ans) => {
  if (!ans) return '未作答'
  return ans.split('|||').map((a, i) => `空${i + 1}: ${a}`).join('；')
}

onMounted(() => {
  loadWrongQuestions()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.filter-bar {
  display: flex; justify-content: space-between; align-items: center;
  gap: 12px; margin-bottom: 20px; flex-wrap: wrap;
}
.filter-left { display: flex; gap: 8px; flex-wrap: wrap; }

.empty-state {
  text-align: center; padding: 60px 0;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: var(--text-secondary); font-size: 16px; }

.wrong-list { display: flex; flex-direction: column; gap: 16px; }
.wrong-card {
  background: var(--bg-card); border-radius: 12px; padding: 20px;
  box-shadow: var(--shadow-sm);
}
.wrong-header {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 12px; flex-wrap: wrap;
}
.wrong-index {
  background: var(--primary-color); color: var(--bg-card); padding: 2px 10px;
  border-radius: 6px; font-size: 13px; font-weight: 600;
}
.wrong-source { font-size: 12px; color: var(--text-secondary); }
.wrong-time { font-size: 12px; color: var(--text-secondary); margin-left: auto; }
.wrong-content { font-size: 15px; line-height: 1.7; margin-bottom: 12px; color: var(--text-primary); }

.options-preview { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.opt-row {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 12px; border-radius: 8px; font-size: 14px;
  background: var(--bg-page);
}
.opt-row.opt-correct { background: rgba(103,194,58,0.08); }
.opt-row.opt-wrong { background: rgba(245,108,108,0.08); }
.opt-key { font-weight: 600; margin-right: 4px; }

.answer-rows { margin-bottom: 10px; }
.ans-row { font-size: 14px; margin-bottom: 4px; }
.ans-label { color: var(--text-secondary); }
.correct-text { color: #67c23a; }
.wrong-text { color: #f56c6c; }

.analysis-box {
  color: var(--text-secondary); font-size: 14px; background: var(--bg-page);
  padding: 10px; border-radius: 6px; margin-top: 8px;
}

.annotations-section {
  margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--border-lighter);
}
.anno-header { font-size: 14px; font-weight: 600; color: var(--text-secondary); margin-bottom: 10px; }
.anno-item {
  padding: 10px; background: var(--bg-page); border-radius: 8px; margin-bottom: 8px;
}
.anno-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.anno-user { font-size: 13px; font-weight: 500; color: var(--primary-color); }
.anno-time { font-size: 11px; color: var(--text-secondary); }
.anno-content { font-size: 14px; color: var(--text-primary); line-height: 1.6; }
.anno-actions { margin-top: 6px; text-align: right; }
.anno-like {
  cursor: pointer; font-size: 13px; color: var(--text-secondary);
  transition: color 0.2s;
}
.anno-like:hover { color: var(--primary-color); }
</style>
