<template>
  <div class="main-content">
    <div class="page-hero">
      <div class="page-hero-icon">✏️</div>
      <div class="page-hero-title">提交审核题目</div>
      <div class="page-hero-subtitle">由阅卷人或管理员补充入服审核题，经管理员通过后进入题库并可用于组卷</div>
      <div class="page-hero-stats">
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ data.myList.length }}</div>
          <div class="page-hero-stat-lbl">累计贡献</div>
        </div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ approvedCount }}</div>
          <div class="page-hero-stat-lbl">已通过</div>
        </div>
        <div class="page-hero-stat-div"></div>
        <div class="page-hero-stat">
          <div class="page-hero-stat-val">{{ pendingCount }}</div>
          <div class="page-hero-stat-lbl">待审核</div>
        </div>
      </div>
    </div>

    <el-tabs v-model="data.activeTab" class="contribute-tabs">
      <el-tab-pane name="submit">
        <template #label>
          <span class="tab-label"><el-icon><EditPen /></el-icon> 提交题目</span>
        </template>
        <div class="submit-layout">
          <div class="form-card">
            <div class="card-header">
              <div class="card-header-icon"><el-icon><EditPen /></el-icon></div>
              <div class="card-header-text">
                <div class="card-title">编写题目</div>
                <div class="card-desc">填写题目信息并提交审核</div>
              </div>
            </div>
            <el-form :model="data.form" label-width="100px" class="contribute-form">
              <div class="form-row">
                <el-form-item label="题目类型" class="form-item-half">
                  <el-select v-model="data.form.type" placeholder="请选择" class="full-width">
                    <el-option label="单选题" value="single" />
                    <el-option label="多选题" value="multiple" />
                    <el-option label="判断题" value="judge" />
                    <el-option label="填空题" value="fillin" />
                    <el-option label="简答题" value="essay" />
                  </el-select>
                </el-form-item>
                <el-form-item label="题目分类" class="form-item-half">
                  <el-select v-model="data.form.categoryId" placeholder="请选择分类" class="full-width">
                    <el-option v-for="c in data.categories" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="难度">
                <div class="difficulty-group">
                  <label
                    class="difficulty-btn"
                    :class="{ active: data.form.difficulty === 'easy', 'diff-easy': data.form.difficulty === 'easy' }"
                    @click="data.form.difficulty = 'easy'"
                  >
                    <el-icon><Star /></el-icon> 简单
                  </label>
                  <label
                    class="difficulty-btn"
                    :class="{ active: data.form.difficulty === 'medium', 'diff-medium': data.form.difficulty === 'medium' }"
                    @click="data.form.difficulty = 'medium'"
                  >
                    <el-icon><Star /></el-icon> 中等
                  </label>
                  <label
                    class="difficulty-btn"
                    :class="{ active: data.form.difficulty === 'hard', 'diff-hard': data.form.difficulty === 'hard' }"
                    @click="data.form.difficulty = 'hard'"
                  >
                    <el-icon><Star /></el-icon> 困难
                  </label>
                </div>
              </el-form-item>
              <el-form-item label="题目内容">
                <el-input v-model="data.form.content" type="textarea" :rows="4" placeholder="请输入题目内容" class="full-width" />
              </el-form-item>
              <el-form-item v-if="['single', 'multiple'].includes(data.form.type)" label="选项">
                <div class="options-list">
                  <div v-for="(opt, idx) in data.form.optionList" :key="idx" class="option-row">
                    <span class="option-letter" :class="{ 'letter-selected': data.form.answer === String.fromCharCode(65 + idx) }">
                      {{ String.fromCharCode(65 + idx) }}
                    </span>
                    <el-input v-model="data.form.optionList[idx]" placeholder="选项内容" class="option-input" />
                    <el-button
                      v-if="data.form.optionList.length > 2"
                      type="danger"
                      text
                      @click="data.form.optionList.splice(idx, 1)"
                      class="option-delete"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button
                    size="small"
                    @click="data.form.optionList.push('')"
                    v-if="data.form.optionList.length < 6"
                    class="add-option-btn"
                  >
                    <el-icon><Plus /></el-icon> 添加选项
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="正确答案">
                <el-input v-model="data.form.answer" placeholder="单选填A，多选填ABC，判断填对/错，填空和简答填写参考答案" class="answer-input" />
              </el-form-item>
              <el-form-item label="解析">
                <el-input v-model="data.form.analysis" type="textarea" :rows="3" placeholder="选填，说明题目考察的服务器规则、审核要点或判断依据" class="full-width" />
              </el-form-item>
              <el-form-item class="form-actions">
                <el-button type="primary" @click="submitQuestion" :loading="data.submitting" class="submit-btn">
                  <el-icon><Document /></el-icon> 提交审核
                </el-button>
                <el-button @click="resetForm" class="reset-btn">
                  <el-icon><RefreshRight /></el-icon> 重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="preview-card">
            <div class="card-header">
              <div class="card-header-icon preview-icon"><el-icon><View /></el-icon></div>
              <div class="card-header-text">
                <div class="card-title">题目预览</div>
                <div class="card-desc">实时预览题目效果</div>
              </div>
            </div>
            <div class="preview-content" v-if="data.form.content">
              <div class="preview-badge-row">
                <span class="preview-type-badge">{{ typeLabel(data.form.type) }}</span>
                <span class="preview-diff-badge" :class="'diff-' + data.form.difficulty">
                  {{ data.form.difficulty === 'easy' ? '简单' : data.form.difficulty === 'medium' ? '中等' : '困难' }}
                </span>
              </div>
              <div class="preview-question">{{ data.form.content }}</div>
              <div class="preview-options" v-if="['single', 'multiple'].includes(data.form.type) && data.form.optionList.length">
                <div v-for="(opt, idx) in data.form.optionList" :key="idx" class="preview-option" :class="{ 'preview-option-correct': data.form.answer === String.fromCharCode(65 + idx) }">
                  <span class="preview-option-letter">{{ String.fromCharCode(65 + idx) }}</span>
                  <span class="preview-option-text">{{ opt || '(空)' }}</span>
                </div>
              </div>
              <div class="preview-answer" v-if="data.form.answer">
                <el-icon><CircleCheck /></el-icon> 答案：{{ data.form.answer }}
              </div>
              <div class="preview-analysis" v-if="data.form.analysis">
                <div class="preview-analysis-label">解析</div>
                <div class="preview-analysis-text">{{ data.form.analysis }}</div>
              </div>
            </div>
            <div class="preview-empty" v-else>
              <div class="preview-empty-icon"><el-icon><Document /></el-icon></div>
              <div class="preview-empty-text">在左侧填写题目内容后<br/>这里将实时显示预览</div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="my">
        <template #label>
          <span class="tab-label"><el-icon><Trophy /></el-icon> 我的贡献</span>
        </template>
        <div class="contributions-list" v-if="data.myList.length">
          <div v-for="item in data.myList" :key="item.id" class="contribution-card">
            <div class="contrib-status-bar" :class="'status-' + item.status.toLowerCase()"></div>
            <div class="contrib-body">
              <div class="contrib-top">
                <span class="contrib-type-badge">{{ item.type }}</span>
                <span class="contrib-status-badge" :class="'badge-' + item.status.toLowerCase()">
                  {{ item.status === 'APPROVED' ? '已通过' : item.status === 'REJECTED' ? '被拒绝' : '待审核' }}
                </span>
              </div>
              <div class="contrib-content">{{ item.content }}</div>
              <div class="contrib-meta">
                <span class="contrib-meta-item">
                  <el-icon><Clock /></el-icon> {{ item.createdAt }}
                </span>
                <span class="contrib-meta-item" v-if="item.reviewComment">
                  <el-icon><ChatLineSquare /></el-icon> {{ item.reviewComment }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <div class="empty-icon"><el-icon><Document /></el-icon></div>
          <div class="empty-text">暂无提交记录，可以先补充一道入服审核题</div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Plus, Delete, Document, Star, Trophy, View, RefreshRight, CircleCheck, Clock, ChatLineSquare } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const user = JSON.parse(localStorage.getItem('beiming-onlineexam-user') || '{}')
const typeLabel = (t) => ({ single: '单选题', multiple: '多选题', judge: '判断题', fillin: '填空题', essay: '简答题' })[t] || t

const data = reactive({
  activeTab: 'submit',
  categories: [],
  submitting: false,
  form: {
    type: 'single',
    categoryId: '',
    difficulty: 'medium',
    content: '',
    optionList: ['', '', '', ''],
    answer: '',
    analysis: '',
  },
  myList: [],
})

const approvedCount = computed(() => data.myList.filter(i => i.status === 'APPROVED').length)
const pendingCount = computed(() => data.myList.filter(i => i.status === 'PENDING').length)

onMounted(() => {
  loadCategories()
  loadMyContributions()
})

const loadCategories = () => {
  request.get('/questionCategory/selectAll').then(res => {
    data.categories = res.data || []
  })
}

const loadMyContributions = () => {
  request.get('/questionContribution/myContributions').then(res => {
    data.myList = res.data || []
  })
}

const submitQuestion = () => {
  if (!data.form.content) { ElMessage.warning('请输入题目内容'); return }
  if (!data.form.categoryId) { ElMessage.warning('请选择分类'); return }
  if (!data.form.answer) { ElMessage.warning('请输入正确答案'); return }

  data.submitting = true
  const options = ['single', 'multiple'].includes(data.form.type)
    ? JSON.stringify(data.form.optionList.reduce((m, v, i) => { m[String.fromCharCode(65 + i)] = v; return m }, {}))
    : null

  request.post('/questionContribution/submit', {
    ...data.form,
    options,
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('提交成功，等待审核')
      resetForm()
      loadMyContributions()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  }).finally(() => { data.submitting = false })
}

const resetForm = () => {
  data.form = { type: 'single', categoryId: '', difficulty: 'medium', content: '', optionList: ['', '', '', ''], answer: '', analysis: '' }
}

watch(() => data.activeTab, (tab) => {
  if (tab === 'my') loadMyContributions()
})
</script>

<style scoped>
@import "@/assets/css/front.css";

.contribute-tabs {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-lighter);
  padding: 20px 28px 28px;
}

.contribute-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.contribute-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.submit-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 960px) {
  .submit-layout {
    grid-template-columns: 1fr;
  }
}

.form-card,
.preview-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-lighter);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-lighter);
}

.card-header-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: var(--primary-light);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.card-header-icon.preview-icon {
  background: rgba(var(--primary-rgb), 0.06);
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.contribute-form .form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
}

.contribute-form .full-width {
  width: 100%;
}

.form-actions {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--border-lighter);
}

.submit-btn {
  min-width: 140px;
}

.reset-btn {
  min-width: 100px;
}

.difficulty-group {
  display: flex;
  gap: 10px;
}

.difficulty-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 20px;
  border-radius: var(--radius-md);
  border: 2px solid var(--border-lighter);
  background: var(--bg-card);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  transition: all 0.25s;
  user-select: none;
}

.difficulty-btn:hover {
  border-color: var(--text-secondary);
}

.difficulty-btn.diff-easy {
  border-color: var(--success-color);
  background: rgba(0, 180, 42, 0.06);
  color: var(--success-color);
}

.difficulty-btn.diff-medium {
  border-color: var(--warning-color);
  background: rgba(255, 125, 0, 0.06);
  color: var(--warning-color);
}

.difficulty-btn.diff-hard {
  border-color: var(--danger-color);
  background: rgba(245, 63, 63, 0.06);
  color: var(--danger-color);
}

.options-list {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.option-letter {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--bg-page);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
  transition: all 0.2s;
  border: 2px solid var(--border-lighter);
}

.option-letter.letter-selected {
  background: var(--primary-light);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.option-input {
  flex: 1;
}

.option-delete {
  flex-shrink: 0;
}

.add-option-btn {
  margin-top: 4px;
  border-style: dashed;
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.answer-input {
  width: 300px;
}

@media (max-width: 768px) {
  .answer-input {
    width: 100%;
  }
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.preview-badge-row {
  display: flex;
  gap: 8px;
}

.preview-type-badge {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  background: var(--bg-page);
  color: var(--text-secondary);
  border: 1px solid var(--border-lighter);
}

.preview-diff-badge {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.preview-diff-badge.diff-easy {
  background: rgba(0, 180, 42, 0.08);
  color: var(--success-color);
  border: 1px solid rgba(0, 180, 42, 0.2);
}

.preview-diff-badge.diff-medium {
  background: rgba(255, 125, 0, 0.08);
  color: var(--warning-color);
  border: 1px solid rgba(255, 125, 0, 0.2);
}

.preview-diff-badge.diff-hard {
  background: rgba(245, 63, 63, 0.08);
  color: var(--danger-color);
  border: 1px solid rgba(245, 63, 63, 0.2);
}

.preview-question {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.6;
}

.preview-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--bg-page);
  border: 1px solid var(--border-lighter);
  transition: all 0.2s;
}

.preview-option-correct {
  background: var(--primary-light);
  border-color: var(--primary-color);
}

.preview-option-letter {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: var(--bg-card);
  border: 1px solid var(--border-lighter);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.preview-option-correct .preview-option-letter {
  background: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}

.preview-option-text {
  font-size: 14px;
  color: var(--text-primary);
}

.preview-option-correct .preview-option-text {
  font-weight: 600;
  color: var(--primary-color);
}

.preview-answer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
  font-size: 14px;
}

.preview-analysis {
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  background: var(--bg-page);
  border: 1px solid var(--border-lighter);
}

.preview-analysis-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.preview-analysis-text {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.6;
}

.preview-empty {
  text-align: center;
  padding: 40px 20px;
}

.preview-empty-icon {
  font-size: 36px;
  color: var(--text-tertiary);
  margin-bottom: 12px;
}

.preview-empty-text {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.6;
}

.contributions-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.contribution-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-lighter);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  display: flex;
  transition: all 0.2s;
}

.contribution-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateX(2px);
}

.contrib-status-bar {
  width: 4px;
  flex-shrink: 0;
}

.contrib-status-bar.status-approved {
  background: var(--success-color);
}

.contrib-status-bar.status-pending {
  background: var(--warning-color);
}

.contrib-status-bar.status-rejected {
  background: var(--danger-color);
}

.contrib-body {
  flex: 1;
  padding: 16px 20px;
  min-width: 0;
}

.contrib-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.contrib-type-badge {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  background: var(--bg-page);
  color: var(--text-secondary);
  border: 1px solid var(--border-lighter);
}

.contrib-status-badge {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.contrib-status-badge.badge-approved {
  background: rgba(0, 180, 42, 0.08);
  color: var(--success-color);
}

.contrib-status-badge.badge-pending {
  background: rgba(255, 125, 0, 0.08);
  color: var(--warning-color);
}

.contrib-status-badge.badge-rejected {
  background: rgba(245, 63, 63, 0.08);
  color: var(--danger-color);
}

.contrib-content {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contrib-meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--text-secondary);
}

.contrib-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--text-secondary);
}

.empty-state .empty-icon {
  font-size: 42px;
  margin-bottom: 12px;
  opacity: 0.3;
  color: var(--text-tertiary);
}

.empty-state .empty-text {
  font-size: 14px;
  color: var(--text-tertiary);
  line-height: 1.6;
}
</style>
