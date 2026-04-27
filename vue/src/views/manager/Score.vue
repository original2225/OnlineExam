<template>
  <div class="score-management">
    <!-- 顶部筛选栏 -->
    <div class="score-filter-bar">
      <div class="filter-left">
        <el-select v-model="data.examId" placeholder="选择考试筛选" style="width: 240px" clearable @change="onExamChange">
          <el-option v-for="exam in data.exams" :key="exam.id" :label="exam.name" :value="exam.id" />
        </el-select>
        <el-button type="primary" @click="load" :icon="Search">查询</el-button>
        <el-button @click="resetFilter" :icon="RefreshLeft">重置</el-button>
      </div>
      <div class="filter-right">
        <el-tag type="info" size="large" effect="plain">共 {{ data.tableData.length }} 条记录</el-tag>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards" v-if="data.examId">
      <div class="stat-card" style="--accent: #667eea;">
        <div class="stat-icon"><el-icon :size="24"><User /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ data.statistics.totalCount || 0 }}</div>
          <div class="stat-label">总参考人数</div>
        </div>
      </div>
      <div class="stat-card" style="--accent: #67c23a;">
        <div class="stat-icon" style="background: rgba(103,194,58,0.1); color: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value" style="color: #67c23a;">{{ data.statistics.passCount || 0 }}</div>
          <div class="stat-label">及格人数</div>
        </div>
      </div>
      <div class="stat-card" style="--accent: #f56c6c;">
        <div class="stat-icon" style="background: rgba(245,108,108,0.1); color: #f56c6c;"><el-icon :size="24"><CircleClose /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value" style="color: #f56c6c;">{{ data.statistics.failCount || 0 }}</div>
          <div class="stat-label">不及格人数</div>
        </div>
      </div>
      <div class="stat-card" style="--accent: #e6a23c;">
        <div class="stat-icon" style="background: rgba(230,162,60,0.1); color: #e6a23c;"><el-icon :size="24"><TrendCharts /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value" style="color: #e6a23c;">{{ data.statistics.passRate || 0 }}<span class="stat-unit">%</span></div>
          <div class="stat-label">及格率</div>
        </div>
      </div>
      <div class="stat-card" style="--accent: #409eff;">
        <div class="stat-icon" style="background: rgba(64,158,255,0.1); color: #409eff;"><el-icon :size="24"><DataAnalysis /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value" style="color: #409eff;">{{ data.statistics.avgScore || 0 }}</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
    </div>

    <!-- 分数分布图 -->
    <div class="score-chart-section" v-if="data.examId && data.tableData.length">
      <div class="chart-card">
        <div class="chart-title">分数段分布</div>
        <div class="bar-chart">
          <div v-for="seg in scoreSegments" :key="seg.label" class="bar-item">
            <div class="bar-label">{{ seg.label }}</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: seg.percent + '%', background: seg.color }"></div>
            </div>
            <div class="bar-count">{{ seg.count }}人</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 成绩列表 -->
    <div class="score-table-card">
      <div class="table-header">
        <span class="table-title">成绩列表</span>
      </div>
      <el-table stripe :data="data.tableData" style="width: 100%">
        <el-table-column prop="studentName" label="考生" width="120">
          <template v-slot="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <div class="student-avatar">{{ (scope.row.studentName || '?')[0] }}</div>
              <span>{{ scope.row.studentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="编号" width="100" />
        <el-table-column prop="examName" label="考试名称" show-overflow-tooltip />
        <el-table-column label="总分" width="100" sortable sort-by="totalScore">
          <template v-slot="scope">
            <div class="score-badge" :class="getScoreClass(scope.row.totalScore)">
              {{ scope.row.totalScore || 0 }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="autoScore" label="客观题" width="90" />
        <el-table-column prop="manualScore" label="主观题" width="90" />
        <el-table-column label="用时" width="90">
          <template v-slot="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template v-slot="scope">
            <el-tag :type="scope.row.isPass ? 'success' : 'danger'" size="small" round>
              {{ scope.row.isPass ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" size="small" link @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="data.detailDialogVisible" width="65%" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">考试成绩详情</span>
        </div>
      </template>
      <div v-if="data.currentRecord">
        <!-- 考生信息卡片 -->
        <div class="detail-info-card">
          <div class="detail-avatar">{{ (data.currentRecord.studentName || '?')[0] }}</div>
          <div class="detail-meta">
            <div class="detail-name">{{ data.currentRecord.studentName }}</div>
            <div class="detail-no">编号：{{ data.currentRecord.studentNo }}</div>
          </div>
          <div class="detail-scores">
            <div class="detail-score-item">
              <span class="ds-label">总分</span>
              <span class="ds-value" :class="getScoreClass(data.currentRecord.totalScore)">{{ data.currentRecord.totalScore || 0 }}</span>
            </div>
            <div class="detail-score-item">
              <span class="ds-label">客观题</span>
              <span class="ds-value">{{ data.currentRecord.autoScore || 0 }}</span>
            </div>
            <div class="detail-score-item">
              <span class="ds-label">主观题</span>
              <span class="ds-value">{{ data.currentRecord.manualScore || 0 }}</span>
            </div>
            <div class="detail-score-item">
              <span class="ds-label">用时</span>
              <span class="ds-value">{{ formatDuration(data.currentRecord.duration) }}</span>
            </div>
          </div>
        </div>

        <!-- 逐题详情 -->
        <div class="answer-list">
          <div v-for="(answer, index) in data.currentAnswers" :key="answer.id" class="answer-card">
            <div class="answer-header">
              <span class="answer-index">{{ index + 1 }}</span>
              <el-tag v-if="answer.question?.type === 'single'" type="success" size="small">单选</el-tag>
              <el-tag v-else-if="answer.question?.type === 'multiple'" type="primary" size="small">多选</el-tag>
              <el-tag v-else-if="answer.question?.type === 'judge'" type="warning" size="small">判断</el-tag>
              <el-tag v-else-if="answer.question?.type === 'fill' || answer.question?.type === 'fillin'" type="info" size="small">填空</el-tag>
              <el-tag v-else type="danger" size="small">简答</el-tag>
              <span class="answer-score-badge">得分：{{ answer.score || 0 }} / {{ answer.question?.score || 0 }}</span>
            </div>
            <div class="answer-content">{{ answer.question?.content }}</div>
            <div class="answer-row">
              <span class="answer-label">考生答案：</span>
              <span class="answer-value">{{ answer.studentAnswer || '未作答' }}</span>
            </div>
            <div class="answer-row" v-if="answer.question?.answer">
              <span class="answer-label">正确答案：</span>
              <span class="answer-value correct">{{ answer.question.answer }}</span>
            </div>
            <div class="answer-row" v-if="answer.question?.analysis">
              <span class="answer-label">解析：</span>
              <span class="answer-value">{{ answer.question.analysis }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, computed } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { Search, RefreshLeft, User, CircleCheck, CircleClose, TrendCharts, DataAnalysis } from "@element-plus/icons-vue";

const data = reactive({
  examId: null,
  exams: [],
  tableData: [],
  statistics: {},
  detailDialogVisible: false,
  currentRecord: null,
  currentAnswers: []
});

const scoreSegments = computed(() => {
  const list = data.tableData;
  if (!list.length) return [];
  const ranges = [
    { label: '90-100', min: 90, max: 101, color: 'linear-gradient(90deg, #67c23a, #95d475)' },
    { label: '80-89', min: 80, max: 90, color: 'linear-gradient(90deg, #409eff, #79bbff)' },
    { label: '70-79', min: 70, max: 80, color: 'linear-gradient(90deg, #e6a23c, #eebe77)' },
    { label: '60-69', min: 60, max: 70, color: 'linear-gradient(90deg, #f8983c, #f5c78e)' },
    { label: '<60', min: -1, max: 60, color: 'linear-gradient(90deg, #f56c6c, #fab6b6)' },
  ];
  const total = list.length;
  return ranges.map(r => {
    const count = list.filter(s => (s.totalScore || 0) >= r.min && (s.totalScore || 0) < r.max).length;
    return { ...r, count, percent: total ? Math.round((count / total) * 100) : 0 };
  });
});

const loadExams = () => {
  request.get('/exam/selectAll').then(res => {
    if (res.code === '200') data.exams = res.data || [];
  });
};

const onExamChange = () => {
  if (data.examId) {
    loadStatistics();
    load();
  } else {
    data.statistics = {};
    data.tableData = [];
  }
};

const loadStatistics = () => {
  if (!data.examId) return;
  request.get('/score/getStatistics/' + data.examId).then(res => {
    if (res.code === '200') data.statistics = res.data || {};
  });
};

const load = () => {
  if (!data.examId) {
    ElMessage.warning('请先选择考试');
    return;
  }
  request.get('/score/selectPage', {
    params: { examId: data.examId, pageNum: 1, pageSize: 1000 }
  }).then(res => {
    if (res.code === '200') data.tableData = res.data?.list || [];
  });
  loadStatistics();
};

const resetFilter = () => {
  data.examId = null;
  data.statistics = {};
  data.tableData = [];
};

const getScoreClass = (score) => {
  if (score >= 90) return 'excellent';
  if (score >= 80) return 'good';
  if (score >= 60) return 'average';
  return 'poor';
};

const formatDuration = (seconds) => {
  if (!seconds) return '0分钟';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s > 0 ? `${m}分${s}秒` : `${m}分钟`;
};

const viewDetail = (row) => {
  data.currentRecord = row;
  request.get('/examRecord/detail/' + row.id).then(res => {
    if (res.code === '200') {
      data.currentAnswers = res.data.answers || [];
      data.detailDialogVisible = true;
    }
  });
};

loadExams();
</script>

<style scoped>
.score-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 筛选栏 */
.score-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border-left: 3px solid var(--accent);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(102,126,234,0.1);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #333;
  line-height: 1;
}
.stat-unit {
  font-size: 14px;
  font-weight: 400;
}
.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

/* 分数分布图 */
.score-chart-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bar-label {
  width: 50px;
  font-size: 13px;
  color: #666;
  text-align: right;
}
.bar-track {
  flex: 1;
  height: 22px;
  background: #f5f7fa;
  border-radius: 6px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease;
  min-width: 2px;
}
.bar-count {
  width: 50px;
  font-size: 13px;
  color: #999;
}

/* 成绩表格 */
.score-table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  overflow: hidden;
}
.table-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.table-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.student-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.score-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
}
.score-badge.excellent { background: rgba(103,194,58,0.1); color: #67c23a; }
.score-badge.good { background: rgba(64,158,255,0.1); color: #409eff; }
.score-badge.average { background: rgba(230,162,60,0.1); color: #e6a23c; }
.score-badge.poor { background: rgba(245,108,108,0.1); color: #f56c6c; }

/* 详情对话框 */
.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #f8f9fc, #eef1f8);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}
.detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  flex-shrink: 0;
}
.detail-meta {
  flex: 1;
}
.detail-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}
.detail-no {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}
.detail-scores {
  display: flex;
  gap: 20px;
}
.detail-score-item {
  text-align: center;
}
.ds-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}
.ds-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #333;
}
.ds-value.excellent { color: #67c23a; }
.ds-value.good { color: #409eff; }
.ds-value.average { color: #e6a23c; }
.ds-value.poor { color: #f56c6c; }

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
}
.answer-card {
  background: #fafbfc;
  border-radius: 10px;
  padding: 14px 16px;
  border: 1px solid #f0f0f0;
}
.answer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.answer-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}
.answer-score-badge {
  margin-left: auto;
  font-size: 13px;
  color: #666;
  font-weight: 500;
}
.answer-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.6;
}
.answer-row {
  font-size: 13px;
  margin-top: 4px;
  color: #666;
}
.answer-label {
  color: #999;
}
.answer-value {
  color: #333;
}
.answer-value.correct {
  color: #67c23a;
  font-weight: 500;
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .detail-info-card {
    flex-direction: column;
    text-align: center;
  }
  .detail-scores {
    justify-content: center;
  }
}
</style>
