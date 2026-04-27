<template>
  <div class="main-content">
    <!-- 未选择审核库：展示审核库卡片网格 -->
    <template v-if="!currentSubject">
      <div class="page-hero">
        <div class="page-hero-icon">📚</div>
        <div class="page-hero-title">审核题库</div>
        <div class="page-hero-subtitle">按建筑、后期、红石、普通(见习)四项浏览进服审核内容</div>
        <div class="page-hero-stats">
          <div class="page-hero-stat">
            <div class="page-hero-stat-val">{{ data.subjects.length }}</div>
            <div class="page-hero-stat-lbl">审核库</div>
          </div>
          <div class="page-hero-stat-div"></div>
          <div class="page-hero-stat">
            <div class="page-hero-stat-val">{{ totalCategories }}</div>
            <div class="page-hero-stat-lbl">板块</div>
          </div>
          <div class="page-hero-stat-div"></div>
          <div class="page-hero-stat">
            <div class="page-hero-stat-val">{{ totalQuestions }}</div>
            <div class="page-hero-stat-lbl">题目</div>
          </div>
        </div>
      </div>

      <!-- 搜索和排序 -->
      <div class="card" style="margin-bottom: 20px">
        <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
          <el-input v-model="data.searchText" prefix-icon="Search" placeholder="搜索审核方向..." style="width: 260px" clearable />
          <el-radio-group v-model="data.sortMode" size="small">
            <el-radio-button value="default">默认</el-radio-button>
            <el-radio-button value="questions">题目数</el-radio-button>
            <el-radio-button value="name">名称</el-radio-button>
          </el-radio-group>
          <div style="margin-left: auto; display: flex; align-items: center; gap: 8px;">
            <el-button :class="{ active: data.viewMode === 'grid' }" size="small" @click="data.viewMode = 'grid'" circle title="网格视图">
              <el-icon><Grid /></el-icon>
            </el-button>
            <el-button :class="{ active: data.viewMode === 'list' }" size="small" @click="data.viewMode = 'list'" circle title="列表视图">
              <el-icon><List /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="data.loading && data.subjects.length === 0" class="card">
        <div class="empty-state">
          <el-icon class="spin" :size="28"><Loading /></el-icon>
          <div style="margin-top: 8px;">加载中...</div>
        </div>
      </div>
      <div v-else-if="filteredSubjects.length === 0" class="card">
        <div class="empty-state">
          <div class="empty-icon">🔍</div>
          <div class="empty-text">{{ data.searchText ? '未找到匹配的审核方向' : '暂无审核分类' }}</div>
        </div>
      </div>

      <!-- 网格视图 -->
      <div v-else-if="data.viewMode === 'grid'" class="subject-grid">
        <div
          v-for="subject in filteredSubjects"
          :key="subject.id"
          class="subject-card"
          @click="selectSubject(subject)"
        >
          <div class="subject-card-accent" :style="{ background: getSubjectColor(subject.icon) }"></div>
          <div class="subject-icon" :class="!subject.icon || subject.icon.startsWith('http') || subject.icon.includes('/files/') ? '' : (subject.icon || 'default')">
            <img v-if="subject.icon && (subject.icon.startsWith('http') || subject.icon.includes('/files/'))" :src="subject.icon" class="subject-img-icon" />
            <img v-else-if="getSubjectImg(subject.icon)" :src="getSubjectImg(subject.icon)" class="subject-img-icon" />
            <span v-else class="subject-emoji">{{ getSubjectEmoji(subject.icon) }}</span>
          </div>
          <div class="subject-name">{{ subject.name }}</div>
          <div class="subject-desc">{{ subject.description || '暂无描述' }}</div>
          <div class="subject-stats-row">
            <span class="ss-badge"><el-icon><Folder /></el-icon> {{ subject.children?.length || 0 }} 板块</span>
            <span class="ss-badge questions"><el-icon><EditPen /></el-icon> {{ getQuestionCount(subject) }} 题</span>
          </div>
          <div class="subject-tags" v-if="subject.children?.length">
            <span v-for="child in subject.children.slice(0, 3)" :key="child.id" class="subject-tag">{{ child.name }}</span>
            <span v-if="subject.children.length > 3" class="subject-tag more">+{{ subject.children.length - 3 }}</span>
          </div>
          <div class="subject-enter">进入题库 <el-icon><ArrowRight /></el-icon></div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="subject-list-view">
        <div
          v-for="subject in filteredSubjects"
          :key="subject.id"
          class="subject-list-item"
          @click="selectSubject(subject)"
        >
          <div class="sli-icon" :style="{ background: getSubjectColor(subject.icon) }">
            <img v-if="subject.icon && (subject.icon.startsWith('http') || subject.icon.includes('/files/'))" :src="subject.icon" style="width: 32px; height: 32px; border-radius: 6px; object-fit: cover;" />
            <img v-else-if="getSubjectImg(subject.icon)" :src="getSubjectImg(subject.icon)" style="width: 32px; height: 32px; border-radius: 6px; object-fit: cover;" />
            <span v-else style="font-size: 24px;">{{ getSubjectEmoji(subject.icon) }}</span>
          </div>
          <div class="sli-body">
            <div class="sli-name">{{ subject.name }}</div>
            <div class="sli-desc">{{ subject.description || '暂无描述' }}</div>
            <div class="sli-tags" v-if="subject.children?.length">
              <span v-for="child in subject.children.slice(0, 4)" :key="child.id" class="sli-tag">{{ child.name }}</span>
              <span v-if="subject.children.length > 4" class="sli-tag">+{{ subject.children.length - 4 }}</span>
            </div>
          </div>
          <div class="sli-stats">
            <div class="sli-stat">
              <span class="sli-stat-val">{{ subject.children?.length || 0 }}</span>
              <span class="sli-stat-lbl">板块</span>
            </div>
            <div class="sli-stat">
              <span class="sli-stat-val">{{ getQuestionCount(subject) }}</span>
              <span class="sli-stat-lbl">题目</span>
            </div>
          </div>
          <el-icon class="sli-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </template>

    <!-- 选择了审核库：展示子分类和审核 -->
    <template v-else>
      <!-- 审核库头部信息 -->
      <div class="page-hero" style="text-align: left; padding: 40px 48px;">
        <div style="display: flex; align-items: center; gap: 16px; position: relative; z-index: 1;">
          <div class="sli-icon" :style="{ background: getSubjectColor(currentSubject.icon), width: '56px', height: '56px' }">
            <img v-if="currentSubject.icon && (currentSubject.icon.startsWith('http') || currentSubject.icon.includes('/files/'))" :src="currentSubject.icon" style="width: 40px; height: 40px; border-radius: 8px; object-fit: cover;" />
            <img v-else-if="getSubjectImg(currentSubject.icon)" :src="getSubjectImg(currentSubject.icon)" style="width: 40px; height: 40px; border-radius: 8px; object-fit: cover;" />
            <span v-else style="font-size: 30px;">{{ getSubjectEmoji(currentSubject.icon) }}</span>
          </div>
          <div>
            <div class="page-hero-title" style="text-align: left; margin-bottom: 4px;">{{ currentSubject.name }}</div>
            <div class="page-hero-subtitle" style="margin: 0;">{{ currentSubject.description || '欢迎来到进服审核题库' }}</div>
          </div>
        </div>
        <div style="display: flex; gap: 10px; margin-top: 20px; position: relative; z-index: 1;">
          <el-button type="primary" round @click="startPracticeForSubject">
            <el-icon><EditPen /></el-icon> 查看审核题
          </el-button>
          <el-button round @click="goBack" style="background: rgba(255,255,255,0.12); border-color: rgba(255,255,255,0.2); color: #fff;">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
        </div>
      </div>

      <!-- 子分类卡片 -->
      <div class="card" style="margin-bottom: 24px" v-if="currentSubject.children?.length">
        <div class="section-header">
          <div class="section-title">板块分类</div>
          <el-button v-if="data.selectedCategoryId" size="small" text type="primary" @click="clearCategory">
            清除筛选
          </el-button>
        </div>
        <div class="subcategory-grid">
          <div
            v-for="child in currentSubject.children"
            :key="child.id"
            class="subcategory-card"
            :class="{ active: data.selectedCategoryId === child.id }"
            @click="selectCategory(child)"
          >
            <div class="sub-icon">
              <el-icon :size="20"><Folder /></el-icon>
            </div>
            <div class="sub-info">
              <div class="sub-name">{{ child.name }}</div>
              <div class="sub-count">{{ child.description || '点击查看相关审核' }}</div>
            </div>
            <el-icon v-if="data.selectedCategoryId === child.id" color="#409eff" :size="16" style="margin-left: auto;"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 审核列表 -->
      <div class="card">
        <div class="section-header">
          <div class="section-title">
            {{ data.selectedCategoryName || '全部审核' }}
            <span style="font-size: 13px; font-weight: normal; color: var(--text-secondary); margin-left: 8px;">
              ({{ filteredExams.length }} 场审核)
            </span>
          </div>
          <el-button size="small" circle @click="loadExams" :loading="data.loadingExams">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <!-- 状态筛选 -->
        <div class="filter-tags" style="margin-bottom: 16px;">
          <span
            class="filter-tag"
            :class="{ active: data.statusFilter === '' }"
            @click="data.statusFilter = ''"
          >
            全部 <span class="tag-count">{{ filteredExams.length }}</span>
          </span>
          <span
            class="filter-tag ongoing"
            :class="{ active: data.statusFilter === 'ongoing' }"
            @click="data.statusFilter = 'ongoing'"
          >
            进行中 <span class="tag-count">{{ statusCounts.ongoing }}</span>
          </span>
          <span
            class="filter-tag notStarted"
            :class="{ active: data.statusFilter === 'notStarted' }"
            @click="data.statusFilter = 'notStarted'"
          >
            未开始 <span class="tag-count">{{ statusCounts.notStarted }}</span>
          </span>
          <span
            class="filter-tag ended"
            :class="{ active: data.statusFilter === 'ended' }"
            @click="data.statusFilter = 'ended'"
          >
            已结束 <span class="tag-count">{{ statusCounts.ended }}</span>
          </span>
        </div>

        <div v-if="data.loadingExams" class="empty-state">
          <el-icon class="spin" :size="28"><Loading /></el-icon>
          <div style="margin-top: 8px;">加载审核列表...</div>
        </div>
        <div v-else-if="filteredExams.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-text">该分类下暂无审核</div>
          <el-button type="primary" size="small" round style="margin-top: 12px;" @click="startPracticeForSubject">
            <el-icon><EditPen /></el-icon> 查看审核题
          </el-button>
        </div>
        <div v-else class="exam-card-list">
          <div
            v-for="exam in filteredExams"
            :key="exam.id"
            class="exam-card"
            :class="getExamStatus(exam)"
          >
            <!-- 状态角标 -->
            <div class="exam-status-corner" :class="getExamStatus(exam)">
              <el-icon v-if="getExamStatus(exam) === 'ongoing'" :size="11"><VideoPlay /></el-icon>
              <el-icon v-else-if="getExamStatus(exam) === 'notStarted'" :size="11"><Clock /></el-icon>
              <el-icon v-else :size="11"><CircleClose /></el-icon>
              {{ getStatusText(exam) }}
            </div>

            <div class="exam-info">
              <div class="exam-name">{{ exam.name }}
                <el-tag v-if="exam.examType === 'permanent'" size="small" type="success" style="margin-left: 8px;">常驻</el-tag>
                <el-tag v-else size="small" style="margin-left: 8px;">统一</el-tag>
              </div>
              <div class="exam-meta">
                <span><el-icon><Document /></el-icon> {{ exam.paperName || '未知试卷' }}</span>
                <span><el-icon><Clock /></el-icon> {{ exam.duration || 60 }} 分钟</span>
                <span v-if="exam.examType !== 'permanent' && exam.startTime">
                  <el-icon><Calendar /></el-icon> {{ formatTime(exam.startTime) }} ~ {{ formatTime(exam.endTime) }}
                </span>
                <span v-else>
                  <el-icon><Timer /></el-icon> 长期有效
                </span>
              </div>
            </div>
            <div class="exam-action">
              <el-button
                v-if="getExamStatus(exam) === 'ongoing'"
                type="success"
                round
                @click="startExam(exam)"
              >
                <el-icon><VideoPlay /></el-icon> 进入审核
              </el-button>
              <el-button
                v-else-if="getExamStatus(exam) === 'notStarted'"
                type="warning"
                round
                disabled
              >
                <el-icon><Clock /></el-icon> 等待开始
              </el-button>
              <el-button v-else type="info" round disabled>
                <el-icon><CircleClose /></el-icon> 已结束
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, watch } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { useRoute } from "vue-router";

const route = useRoute()

const data = reactive({
  subjects: [],
  exams: [],
  currentSubjectId: null,
  selectedCategoryId: null,
  selectedCategoryName: '',
  statusFilter: '',
  loading: false,
  loadingExams: false,
  searchText: '',
  sortMode: 'default',
  viewMode: 'grid',
  questionStats: {}
})

const totalCategories = computed(() => data.subjects.reduce((s, sub) => s + (sub.children?.length || 0), 0))
const totalQuestions = computed(() => {
  let total = 0
  data.subjects.forEach(sub => {
    total += data.questionStats[sub.id] || 0
    if (sub.children) {
      sub.children.forEach(c => { total += data.questionStats[c.id] || 0 })
    }
  })
  return total
})

const getQuestionCount = (subject) => {
  let count = data.questionStats[subject.id] || 0
  if (subject.children) {
    subject.children.forEach(c => { count += data.questionStats[c.id] || 0 })
  }
  return count
}

const getSubjectColor = (icon) => {
  const map = {
    minecraft: 'linear-gradient(135deg, #4CAF50, #8BC34A)',
    math: 'linear-gradient(135deg, #409eff, #53a8ff)',
    writing: 'linear-gradient(135deg, #ff7d00, #ff9500)',
    computer: 'linear-gradient(135deg, #607D8B, #90A4AE)',
    programming: 'linear-gradient(135deg, #722ed1, #9b59b6)',
    redstone: 'linear-gradient(135deg, #f53f3f, #ff4d4f)'
  }
  return map[icon] || 'linear-gradient(135deg, #00b42a, #00d034)'
}

const filteredSubjects = computed(() => {
  let list = data.subjects
  if (data.searchText) {
    const kw = data.searchText.toLowerCase()
    list = list.filter(s =>
      s.name.toLowerCase().includes(kw) ||
      (s.description || '').toLowerCase().includes(kw) ||
      (s.children || []).some(c => c.name.toLowerCase().includes(kw))
    )
  }
  if (data.sortMode === 'questions') {
    list = [...list].sort((a, b) => getQuestionCount(b) - getQuestionCount(a))
  } else if (data.sortMode === 'name') {
    list = [...list].sort((a, b) => a.name.localeCompare(b.name, 'zh'))
  }
  return list
})

const currentSubject = computed(() => {
  if (!data.currentSubjectId) return null
  return data.subjects.find(s => s.id === data.currentSubjectId) || null
})

const filteredExams = computed(() => {
  let list = data.exams
  // 按审核库筛选
  if (data.currentSubjectId) {
    const subject = currentSubject.value
    const catIds = subject?.children?.map(c => c.id) || []
    list = list.filter(e => {
      if (!e.categoryId) return false
      // categoryId 直接匹配子分类
      if (catIds.includes(e.categoryId)) return true
      // 或者 categoryId 就是审核库本身
      return e.categoryId === data.currentSubjectId
    })
  }
  // 状态筛选
  if (data.statusFilter) {
    list = list.filter(e => getExamStatus(e) === data.statusFilter)
  }
  return list
})

const statusCounts = computed(() => {
  let list = data.exams
  if (data.currentSubjectId) {
    const subject = currentSubject.value
    const catIds = subject?.children?.map(c => c.id) || []
    list = list.filter(e => {
      if (!e.categoryId) return false
      return catIds.includes(e.categoryId) || e.categoryId === data.currentSubjectId
    })
  }
  return {
    ongoing: list.filter(e => getExamStatus(e) === 'ongoing').length,
    notStarted: list.filter(e => getExamStatus(e) === 'notStarted').length,
    ended: list.filter(e => getExamStatus(e) === 'ended').length
  }
})

const getSubjectEmoji = (icon) => {
  const map = { math: '📐', writing: '📝', computer: '💻', programming: '⌨️' }
  return map[icon] || '📚'
}

import creeperImg from '@/assets/imgs/creeper.png'
import redstoneImg from '@/assets/imgs/redstone.png'

const getSubjectImg = (icon) => {
  const map = { minecraft: creeperImg, redstone: redstoneImg }
  return map[icon] || ''
}

const getExamStatus = (exam) => {
  if (exam.examType === 'permanent') return 'ongoing'
  const now = new Date()
  const start = exam.startTime ? new Date(exam.startTime) : null
  const end = exam.endTime ? new Date(exam.endTime) : null
  if (start && now < start) return 'notStarted'
  if (end && now > end) return 'ended'
  return 'ongoing'
}

const getStatusText = (exam) => {
  const s = getExamStatus(exam)
  const map = { ongoing: '进行中', notStarted: '未开始', ended: '已结束' }
  return map[s]
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

const goHome = () => {
  data.currentSubjectId = null
  data.selectedCategoryId = null
  data.statusFilter = ''
  router.push('/front/subjects')
}

const goBack = () => {
  data.currentSubjectId = null
  data.selectedCategoryId = null
  data.selectedCategoryName = ''
  data.statusFilter = ''
}

const selectSubject = (subject) => {
  data.currentSubjectId = subject.id
  data.selectedCategoryId = null
  data.selectedCategoryName = ''
  data.statusFilter = ''
}

const selectCategory = (child) => {
  if (data.selectedCategoryId === child.id) {
    data.selectedCategoryId = null
    data.selectedCategoryName = ''
  } else {
    data.selectedCategoryId = child.id
    data.selectedCategoryName = child.name
  }
}

const clearCategory = () => {
  data.selectedCategoryId = null
  data.selectedCategoryName = ''
}

const startExam = (exam) => {
  router.push({ path: '/front/examTaking', query: { examId: exam.id } })
}

const startPracticeForSubject = () => {
  router.push('/front/examList')
}

const loadSubjects = () => {
  data.loading = true
  request.get('/questionCategory/selectTree').then(res => {
    if (res.code === '200') {
      data.subjects = res.data || []
      if (route.query.subjectId) {
        const id = parseInt(route.query.subjectId)
        const found = data.subjects.find(s => s.id === id)
        if (found) selectSubject(found)
      }
    }
  }).finally(() => {
    data.loading = false
  })
}

const loadExams = () => {
  data.loadingExams = true
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  request.get('/exam/getAvailableExams/' + user.id).then(res => {
    if (res.code === '200') {
      data.exams = res.data || []
    }
  }).finally(() => {
    data.loadingExams = false
  })
}

const loadQuestionStats = () => {
  request.get('/questionCategory/questionStats').then(res => {
    if (res.code === '200') {
      data.questionStats = res.data || {}
    }
  }).catch(() => {})
}

watch(() => route.query.subjectId, (newId) => {
  if (newId) {
    const id = parseInt(newId)
    const found = data.subjects.find(s => s.id === id)
    if (found) selectSubject(found)
    else {
      data.currentSubjectId = id
    }
  } else {
    data.currentSubjectId = null
  }
})

onMounted(() => {
  loadSubjects()
  loadExams()
  loadQuestionStats()
})
</script>

<script>
export default { name: 'SubjectBrowse' }
</script>

<style scoped>
@import "@/assets/css/front.css";

.spin { animation: spin 1s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.subject-card-accent {
  height: 4px;
  border-radius: 2px;
  margin: -4px -4px 16px -4px;
  width: calc(100% + 8px);
}

.subject-emoji {
  font-size: 28px;
}

.subject-stats-row {
  display: flex;
  gap: 8px;
  margin: 10px 0;
}

.ss-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 3px 10px;
  border-radius: 10px;
}

.ss-badge.questions {
  color: var(--primary-color);
  background: var(--primary-light);
}

.subject-tag.more {
  background: var(--bg-page);
  color: var(--text-secondary);
}

/* 列表视图 */
.subject-list-view {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.subject-list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 18px 24px;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid var(--border-lighter);
  box-shadow: var(--shadow-sm);
}

.subject-list-item:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.sli-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sli-body {
  flex: 1;
  min-width: 0;
}

.sli-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 3px;
}

.sli-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sli-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.sli-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  background: var(--bg-page);
  color: var(--text-secondary);
}

.sli-stats {
  display: flex;
  gap: 20px;
  flex-shrink: 0;
}

.sli-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sli-stat-val {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
}

.sli-stat-lbl {
  font-size: 11px;
  color: var(--text-secondary);
}

.sli-arrow {
  color: var(--text-secondary);
  font-size: 18px;
  transition: transform 0.2s;
  flex-shrink: 0;
}

.subject-list-item:hover .sli-arrow {
  transform: translateX(4px);
  color: var(--primary-color);
}

/* 筛选和审核卡片样式 */
.filter-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.filter-tag {
  padding: 6px 16px; border-radius: 20px; font-size: 13px;
  cursor: pointer; border: 1px solid var(--border-light);
  background: var(--bg-page); color: var(--text-secondary);
  transition: all 0.2s; display: flex; align-items: center; gap: 4px;
}
.filter-tag:hover { border-color: var(--primary-color); color: var(--primary-color); }
.filter-tag.active { background: var(--primary-color); color: white; border-color: var(--primary-color); }
.filter-tag.ongoing.active { background: #67a23a; border-color: #67a23a; }
.filter-tag.notStarted.active { background: #e6a23c; border-color: #e6a23c; }
.filter-tag.ended.active { background: #909399; border-color: #909399; }
.tag-count {
  background: rgba(var(--primary-rgb), 0.1); border-radius: 10px;
  padding: 0 6px; font-size: 11px; min-width: 18px; text-align: center;
}
.filter-tag.active .tag-count { background: rgba(255,255,255,0.25); }

.exam-card {
  position: relative; overflow: hidden;
}
.exam-status-corner {
  position: absolute; top: 0; right: 0;
  padding: 4px 12px; border-radius: 0 10px 0 10px;
  font-size: 11px; display: flex; align-items: center; gap: 4px;
  color: white;
}
.exam-status-corner.ongoing { background: #67a23a; }
.exam-status-corner.notStarted { background: #e6a23c; }
.exam-status-corner.ended { background: #909399; }

.subcategory-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}
.subcategory-card {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px; border-radius: 10px;
  border: 1.5px solid var(--border-light);
  background: var(--bg-page); cursor: pointer;
  transition: all 0.2s;
}
.subcategory-card:hover { border-color: var(--primary-color); background: var(--primary-light); }
.subcategory-card.active { border-color: var(--primary-color); background: var(--primary-light); }
.sub-icon { width: 36px; height: 36px; border-radius: 8px; background: var(--bg-card); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.sub-info { flex: 1; min-width: 0; }
.sub-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.sub-count { font-size: 12px; color: var(--text-secondary); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

@media (max-width: 768px) {
  .sli-stats { gap: 12px; }
  .sli-tags { display: none; }
}
</style>
