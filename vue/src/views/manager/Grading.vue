<template>
  <div class="grading-page">

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-left">
        <div class="hero-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M9 12l2 2 4-4" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="#ffffff" stroke-width="2"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>阅卷管理</h1>
          <p>在线批阅主观题答卷，支持简答题与填空题人工评分</p>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrap">
          <el-icon class="s-icon"><Search /></el-icon>
          <el-select v-model="data.examId" placeholder="选择审核" style="width: 240px" clearable @change="onExamChange" class="exam-sel">
            <template #prefix><el-icon><Document /></el-icon></template>
            <el-option v-for="exam in data.exams" :key="exam.id" :label="exam.name" :value="exam.id" />
          </el-select>
        </div>
      </div>
      <div class="toolbar-right">
        <span class="total-tip" v-if="data.tableData.length">共 <strong>{{ data.tableData.length }}</strong> 份答卷</span>
      </div>
    </div>

    <!-- 进度统计 -->
    <div class="stats-grid" v-if="data.examId">
      <div class="stat-card stat-total"><div class="stat-inner"><div class="stat-num">{{ data.tableData.length }}</div><div class="stat-label">总提交</div></div></div>
      <div class="stat-card stat-pending"><div class="stat-inner"><div class="stat-num">{{ pendingCount }}</div><div class="stat-label">待批阅</div></div></div>
      <div class="stat-card stat-done"><div class="stat-inner"><div class="stat-num">{{ gradedCount }}</div><div class="stat-label">已完成</div></div></div>
      <div class="stat-card stat-progress"><div class="stat-inner">
        <div class="stat-num">{{ gradingProgress }}<span class="stat-unit">%</span></div>
        <div class="stat-label">批阅进度</div>
        <div class="progress-bar"><div class="progress-fill" :style="{ width: gradingProgress + '%' }"></div></div>
      </div></div>
    </div>

    <!-- 空提示 -->
    <div v-if="!data.examId" class="pick-hint">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
        <path d="M9 12l2 2 4-4" stroke="#60a5fa" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="#93c5fd" stroke-width="1.5"/>
      </svg>
      <p>请从上方选择一个审核，开始批阅答卷</p>
    </div>

    <!-- 答卷卡片列表 -->
    <div class="grading-list-card" v-if="data.examId && data.tableData.length">
      <transition-group name="grade-anim">
        <div v-for="row in data.tableData" :key="row.id" class="grade-card-item">
          <div class="grade-left">
            <div class="grade-avatar">{{ (row.studentName || '?')[0] }}</div>
          </div>
          <div class="grade-body">
            <div class="grade-info-row">
              <span class="grade-name">{{ row.studentName }}</span>
              <span class="grade-no">{{ row.studentNo }}</span>
              <span class="grade-exam">{{ row.examName }}</span>
            </div>
            <div class="grade-scores">
              <div class="score-chip obj">
                <span class="chip-label">客观题</span>
                <span class="chip-val">{{ row.autoScore || 0 }}</span>
              </div>
              <div class="score-op">+</div>
              <div class="score-chip sub" :class="{ 'need-grade': row.manualScore == null }">
                <span class="chip-label">主观题</span>
                <span class="chip-val">{{ row.manualScore != null ? row.manualScore : '待阅' }}</span>
              </div>
              <div class="score-op">=</div>
              <div class="score-chip total">
                <span class="chip-label">总分</span>
                <span class="chip-val">{{ row.totalScore || 0 }}</span>
              </div>
            </div>
            <div class="grade-status-row">
              <span class="status-pill" :class="getGradingStatus(row) === 0 ? 'pending' : 'done'">
                {{ getGradingStatus(row) === 0 ? '待批阅' : '已批阅' }}
              </span>
            </div>
          </div>
          <div class="grade-right">
            <el-button type="primary" round @click="handleGrade(row)">
              <el-icon><EditPen /></el-icon> 批阅
            </el-button>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 批阅对话框 -->
    <el-dialog v-model="data.gradingDialogVisible" width="70%" destroy-on-close top="4vh" class="grade-dialog">
      <template #header>
        <div class="dialog-hero">
          <span class="dialog-title">批阅答卷</span>
          <el-tag type="info" size="small" effect="plain">{{ data.currentRecord?.studentName }}</el-tag>
        </div>
      </template>

      <div v-if="data.currentRecord">
        <!-- 玩家信息 -->
        <div class="grade-info-bar">
          <div class="gi-left">
            <div class="gi-avatar">{{ (data.currentRecord.studentName || '?')[0] }}</div>
            <div>
              <div class="gi-name">{{ data.currentRecord.studentName }}</div>
              <div class="gi-no">编号：{{ data.currentRecord.studentNo }}</div>
            </div>
          </div>
          <div class="gi-right">
            <div class="gi-score-item">
              <span class="gi-label">客观题</span>
              <span class="gi-value">{{ data.currentRecord.autoScore || 0 }}</span>
            </div>
            <div class="gi-op">+</div>
            <div class="gi-score-item">
              <span class="gi-label">主观题</span>
              <span class="gi-value" style="color: #f59e0b;">{{ gradingTotalScore }}</span>
            </div>
            <div class="gi-op">=</div>
            <div class="gi-score-item">
              <span class="gi-label">总分</span>
              <span class="gi-value" style="color: #f59e0b; font-size: 26px;">{{ totalScore }}</span>
            </div>
          </div>
        </div>

        <!-- 已有阅卷记录 -->
        <div v-if="data.submissions.length" class="sub-history">
          <div class="sub-label">
            <el-icon><Clock /></el-icon> 已有阅卷记录（{{ data.submissions.length }}人）
          </div>
          <el-table :data="data.submissions" size="small" stripe style="margin-bottom: 12px;">
            <el-table-column prop="graderName" label="阅卷人" width="100" />
            <el-table-column prop="graderRole" label="角色" width="80" />
            <el-table-column prop="manualScore" label="主观题评分" width="100" />
            <el-table-column prop="totalScore" label="总分" width="100" />
            <el-table-column prop="createdAt" label="提交时间" />
          </el-table>
        </div>

        <!-- 答题详情 -->
        <div class="sub-label" style="margin-top: 12px;">
          <el-icon><Document /></el-icon> 答题详情
        </div>

        <div class="answer-list">
          <div v-for="(answer, index) in data.currentAnswers" :key="answer.id" class="answer-card">
            <div class="answer-header">
              <span class="answer-index">{{ index + 1 }}</span>
              <el-tag v-if="isObjective(answer.question?.type)" :type="answer.isCorrect ? 'success' : 'danger'" size="small" round>
                {{ answer.isCorrect ? '正确' : '错误' }}
              </el-tag>
              <el-tag v-else-if="answer.question?.type === 'essay'" type="danger" size="small">简答</el-tag>
              <el-tag v-else type="info" size="small">填空</el-tag>
              <span class="answer-score-info">{{ answer.score || 0 }} / {{ answer.question?.score || 0 }} 分</span>
            </div>
            <div class="answer-content">{{ answer.question?.content }}</div>
            <div class="answer-row">
              <span class="answer-label">玩家答案：</span>
              <span class="answer-value">{{ answer.studentAnswer || '未作答' }}</span>
            </div>
            <div class="answer-row" v-if="answer.question?.answer && needManualGrade(answer.question?.type)">
              <span class="answer-label">参考答案：</span>
              <span class="answer-value correct">{{ answer.question.answer }}</span>
            </div>

            <!-- 手动评分 -->
            <div v-if="needManualGrade(answer.question?.type)" class="grade-row">
              <el-input-number v-model="answer.score" :min="0" :max="answer.question?.score || 10" :step="0.5" :precision="1" size="small" style="width: 120px" />
              <span style="margin: 0 6px; color: #999;">分</span>
              <el-input v-model="answer.comment" placeholder="评语（可选）" style="flex: 1;" size="small" />
            </div>
          </div>
        </div>
      </div>

        <div class="final-vote-card">
          <div class="sub-label"><el-icon><Check /></el-icon> 入服参考表决</div>
          <el-form label-width="96px">
            <el-form-item label="表现评分">
              <el-input-number v-model="data.performanceScore" :min="0" :max="100" :step="5" />
            </el-form-item>
            <el-form-item label="参考表决">
              <el-radio-group v-model="data.advisoryVote">
                <el-radio value="PASS">建议通过</el-radio>
                <el-radio value="FAIL">建议不通过</el-radio>
                <el-radio value="ABSTAIN">弃权</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="data.advisoryVote === 'FAIL'" label="原因">
              <el-checkbox-group v-model="data.rejectionReasons">
                <el-checkbox label="规则理解不足" />
                <el-checkbox label="沟通态度不合适" />
                <el-checkbox label="名声或历史记录不佳" />
                <el-checkbox label="不适合当前服务器氛围" />
              </el-checkbox-group>
            </el-form-item>
            <el-form-item v-if="data.advisoryVote === 'FAIL'" label="其他原因">
              <el-input v-model="data.customReason" placeholder="可手动填写其他原因" />
            </el-form-item>
            <el-form-item label="总评语">
              <el-input v-model="data.gradeComment" type="textarea" :rows="2" placeholder="可选" />
            </el-form-item>
          </el-form>
        </div>

        <template #footer>
        <el-button @click="data.gradingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrading">
          <el-icon><Check /></el-icon> 提交批阅
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, computed } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { Search, EditPen, Clock, Document, Check } from "@element-plus/icons-vue";

const data = reactive({
  examId: null,
  exams: [],
  tableData: [],
  gradingDialogVisible: false,
  currentRecord: null,
  currentAnswers: [],
  submissions: [],
  performanceScore: 80,
  advisoryVote: 'PASS',
  rejectionReasons: [],
  customReason: '',
  gradeComment: ''
});

const submissionCounts = reactive({});

const pendingCount = computed(() => data.tableData.filter(r => r.manualScore == null).length);
const gradedCount = computed(() => data.tableData.filter(r => r.manualScore != null).length);
const gradingProgress = computed(() => {
  if (!data.tableData.length) return 0;
  return Math.round((gradedCount.value / data.tableData.length) * 100);
});

const gradingTotalScore = computed(() => {
  return data.currentAnswers
    .filter(a => needManualGrade(a.question?.type))
    .reduce((sum, a) => sum + (parseFloat(a.score) || 0), 0)
    .toFixed(1);
});

const totalScore = computed(() => {
  return (parseFloat(data.currentRecord?.autoScore || 0) + parseFloat(gradingTotalScore.value)).toFixed(1);
});

const isObjective = (type) => type === 'single' || type === 'multiple' || type === 'judge';
const needManualGrade = (type) => type === 'essay' || type === 'fill' || type === 'fillin';
const getGradingStatus = (row) => submissionCounts[row.id] || 0;

const loadExams = () => { request.get('/exam/selectAll').then(res => { if (res.code === '200') data.exams = res.data || [] }) };

const onExamChange = () => { if (data.examId) load(); else data.tableData = [] };

const load = () => {
  if (!data.examId) { ElMessage.warning('请先选择审核'); return; }
  request.get('/examRecord/getByExamId/' + data.examId).then(res => {
    if (res.code === '200') {
      data.tableData = res.data || [];
      data.tableData.forEach(row => {
        request.get('/grading/getSubmissions/' + row.id).then(subRes => {
          if (subRes.code === '200') submissionCounts[row.id] = (subRes.data || []).length;
        });
      });
    }
  });
};

const handleGrade = (row) => {
  data.currentRecord = row;
  data.performanceScore = 80;
  data.advisoryVote = 'PASS';
  data.rejectionReasons = [];
  data.customReason = '';
  data.gradeComment = '';
  const detailPromise = request.get('/examRecord/detail/' + row.id);
  const submissionsPromise = request.get('/grading/getSubmissions/' + row.id);
  detailPromise.then(res => { if (res.code === '200') data.currentAnswers = res.data.answers || [] });
  submissionsPromise.then(res => { if (res.code === '200') data.submissions = res.data || [] });
  Promise.all([detailPromise, submissionsPromise]).then(() => { data.gradingDialogVisible = true });
};

const submitGrading = () => {
  const answers = data.currentAnswers.map(a => ({ id: a.id, score: a.score || 0, comment: a.comment }));
  request.post('/grading/batchGrade?recordId=' + data.currentRecord.id, {
    answers,
    performanceScore: data.performanceScore,
    advisoryVote: data.advisoryVote,
    rejectionReasons: data.rejectionReasons.join(','),
    customReason: data.customReason,
    comment: data.gradeComment
  }).then(res => {
    if (res.code === '200') { ElMessage.success('批阅与表决已提交'); data.gradingDialogVisible = false; load(); }
    else { ElMessage.error(res.msg || '批阅失败'); }
  });
};

loadExams();
</script>

<style scoped>
.grading-page { padding: 24px; max-width: 1400px; margin: 0 auto; }

/* ===== 页面标题 ===== */
.page-hero { background: linear-gradient(135deg, #92400e 0%, #d97706 50%, #fbbf24 100%); border-radius: 16px; padding: 28px 36px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 8px 32px rgba(217, 119, 6, 0.25); }
.hero-left { display: flex; align-items: center; gap: 18px; }
.hero-icon { width: 60px; height: 60px; background: rgba(255,255,255,0.15); border-radius: 16px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.hero-text h1 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: #fff; }
.hero-text p { margin: 0; font-size: 13px; color: rgba(255,255,255,0.75); }

/* ===== 工具栏 ===== */
.toolbar { background: #fff; border-radius: 14px; padding: 16px 20px; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-wrap { display: flex; align-items: center; gap: 10px; }
.s-icon { color: #9ca3af; }
.exam-sel :deep(.el-select__wrapper) { border-radius: 10px; }
.total-tip { font-size: 13px; color: #6b7280; }
.total-tip strong { color: #d97706; }

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { border-radius: 14px; overflow: hidden; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { padding: 18px 20px; color: #fff; position: relative; }
.stat-total .stat-inner { background: linear-gradient(135deg, #92400e, #d97706); }
.stat-pending .stat-inner { background: linear-gradient(135deg, #dc2626, #f87171); }
.stat-done .stat-inner { background: linear-gradient(135deg, #16a34a, #4ade80); }
.stat-progress .stat-inner { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-unit { font-size: 14px; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 4px; }
.progress-bar { height: 5px; background: rgba(255,255,255,0.2); border-radius: 3px; margin-top: 8px; overflow: hidden; }
.progress-fill { height: 100%; background: #fff; border-radius: 3px; transition: width 0.6s ease; }

/* 空提示 */
.pick-hint { text-align: center; padding: 80px; background: #fff; border-radius: 14px; border: 1px solid var(--el-border-color-lighter); box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.pick-hint p { margin-top: 16px; color: #9ca3af; font-size: 14px; }

/* ===== 批阅列表 ===== */
.grading-list-card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--el-border-color-lighter); }
.grade-card-item { display: flex; align-items: center; gap: 16px; padding: 18px 24px; border-bottom: 1px solid var(--el-border-color-lighter); transition: background 0.2s; }
.grade-card-item:last-child { border-bottom: none; }
.grade-card-item:hover { background: #fffbeb; }
.grade-left { flex-shrink: 0; }
.grade-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #d97706, #fbbf24); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; }
.grade-body { flex: 1; min-width: 0; }
.grade-info-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
.grade-name { font-size: 15px; font-weight: 700; color: #1f2937; }
.grade-no { font-size: 12px; color: #9ca3af; }
.grade-exam { font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 1px 8px; border-radius: 6px; }
.grade-scores { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.score-chip { display: flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 8px; font-size: 13px; }
.score-chip.obj { background: #dbeafe; color: #2563eb; }
.score-chip.sub { background: #fef3c7; color: #d97706; }
.score-chip.sub.need-grade { background: #fee2e2; color: #dc2626; }
.score-chip.total { background: #f3e8ff; color: #7c3aed; }
.score-chip .chip-label { font-size: 11px; opacity: 0.8; }
.score-chip .chip-val { font-weight: 700; }
.score-op { color: #d1d5db; font-weight: 300; font-size: 14px; }
.grade-status-row { display: flex; align-items: center; gap: 8px; }
.status-pill { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-pill.pending { background: #fee2e2; color: #dc2626; }
.status-pill.done { background: #dcfce7; color: #16a34a; }
.grade-right { flex-shrink: 0; }

/* 弹窗 */
.dialog-hero { display: flex; align-items: center; gap: 10px; }
.dialog-title { font-size: 18px; font-weight: 600; color: #1a1a2e; }
.grade-info-bar { display: flex; align-items: center; justify-content: space-between; background: linear-gradient(135deg, #fffbeb, #fef3c7); border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
.gi-left { display: flex; align-items: center; gap: 12px; }
.gi-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #d97706, #fbbf24); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; }
.gi-name { font-size: 16px; font-weight: 600; color: #1a1a2e; }
.gi-no { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.gi-right { display: flex; align-items: center; gap: 10px; }
.gi-score-item { text-align: center; }
.gi-label { display: block; font-size: 12px; color: #9ca3af; }
.gi-value { display: block; font-size: 20px; font-weight: 700; color: #333; }
.gi-op { font-size: 18px; color: #ccc; font-weight: 300; }

.sub-history { background: #fafbfc; border-radius: 10px; padding: 14px; margin-bottom: 8px; }
.sub-label { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600; color: #666; margin-bottom: 10px; }

.answer-list { display: flex; flex-direction: column; gap: 10px; max-height: 420px; overflow-y: auto; }
.answer-card { background: #fafbfc; border-radius: 10px; padding: 14px 16px; border: 1px solid #f0f0f0; }
.answer-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.answer-index { width: 24px; height: 24px; border-radius: 50%; background: linear-gradient(135deg, #d97706, #fbbf24); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
.answer-score-info { margin-left: auto; font-size: 13px; color: #666; font-weight: 500; }
.answer-content { font-size: 14px; color: #333; margin-bottom: 8px; line-height: 1.6; }
.answer-row { font-size: 13px; color: #666; margin-top: 4px; }
.answer-label { color: #999; }
.answer-value { color: #333; }
.answer-value.correct { color: #16a34a; font-weight: 500; }
.grade-row { display: flex; align-items: center; margin-top: 10px; padding-top: 10px; border-top: 1px dashed #eee; }

/* 动画 */
.grade-anim-enter-active, .grade-anim-leave-active { transition: all 0.3s; }
.grade-anim-enter-from { opacity: 0; transform: translateY(10px); }
.grade-anim-leave-to { opacity: 0; }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .grade-card-item { flex-wrap: wrap; } .grade-right { width: 100%; } .grade-right .el-button { width: 100%; } }
</style>
