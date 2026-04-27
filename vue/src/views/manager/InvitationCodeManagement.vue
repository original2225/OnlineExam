<template>
  <div class="invite-page">

    <!-- 页面标题区 -->
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-icon">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 8H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V10a2 2 0 0 0-2-2h-3" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 12h6M9 16h6" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1>邀请码管理</h1>
          <p>统一管理玩家及阅卷人注册邀请码，支持批量生成与自动清理</p>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" round @click="showGenerateDrawer = true">
          <span class="btn-icon">+</span> 生成邀请码
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-grid">
      <div class="stat-item stat-total">
        <div class="stat-inner">
          <div class="stat-num" v-count-up="{ val: data.statistics.total }">{{ data.statistics.total || 0 }}</div>
          <div class="stat-label">邀请码总数</div>
          <div class="stat-bar"></div>
        </div>
      </div>
      <div class="stat-item stat-user">
        <div class="stat-inner">
          <div class="stat-num" v-count-up="{ val: data.statistics.studentUnused }">{{ data.statistics.studentUnused || 0 }}</div>
          <div class="stat-label">玩家邀请码</div>
          <div class="stat-desc">未使用</div>
        </div>
      </div>
      <div class="stat-item stat-examiner">
        <div class="stat-inner">
          <div class="stat-num" v-count-up="{ val: data.statistics.examinerUnused }">{{ data.statistics.examinerUnused || 0 }}</div>
          <div class="stat-label">阅卷人邀请码</div>
          <div class="stat-desc">未使用</div>
        </div>
      </div>
      <div class="stat-item stat-used">
        <div class="stat-inner">
          <div class="stat-num" v-count-up="{ val: data.statistics.used }">{{ data.statistics.used || 0 }}</div>
          <div class="stat-label">已使用</div>
          <div class="stat-desc">历史统计</div>
        </div>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="data.searchForm.code"
            placeholder="搜索邀请码..."
            clearable
            @keyup.enter="load"
            @clear="load"
            class="search-input"
          />
        </div>
        <el-select v-model="data.searchForm.status" placeholder="状态" @change="load" class="filter-select">
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="全部状态" value=""></el-option>
          <el-option label="未使用" value="UNUSED" />
          <el-option label="已使用" value="USED" />
        </el-select>
        <el-select v-model="data.searchForm.targetRole" placeholder="类型" @change="load" class="filter-select">
          <template #prefix><el-icon><User /></el-icon></template>
          <el-option label="全部类型" value=""></el-option>
          <el-option label="玩家" value="USER" />
          <el-option label="阅卷人" value="HELPER" />
        </el-select>
        <el-button plain @click="reset" class="reset-btn">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="danger" plain size="small" @click="deleteAllUnused" :loading="data.deleting" class="cleanup-btn">
          <el-icon><Delete /></el-icon> 清理未使用
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="data.tableData"
        stripe
        v-loading="data.loading"
        class="invite-table"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)', fontWeight: '600' }"
        row-class-name="table-row"
      >
        <el-table-column prop="code" label="邀请码" min-width="140">
          <template #default="scope">
            <div class="code-cell">
              <span class="code-text">{{ scope.row.code }}</span>
              <el-tooltip content="复制" placement="top">
                <el-icon class="copy-icon" @click="copyOne(scope.row.code)"><CopyDocument /></el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetRole" label="类型" width="110">
          <template #default="scope">
            <span class="type-badge" :class="scope.row.targetRole === 'HELPER' ? 'type-examiner' : 'type-user'">
              {{ scope.row.targetRole === 'HELPER' ? '阅卷人' : '玩家' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="scope">
            <span class="status-dot" :class="'status-' + scope.row.status.toLowerCase()">
              <span class="dot"></span>
              {{ scope.row.status === 'UNUSED' ? '未使用' : '已使用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="usedByUsername" label="使用玩家" width="130">
          <template #default="scope">
            <span class="user-name" v-if="scope.row.usedByUsername">{{ scope.row.usedByUsername }}</span>
            <span class="user-empty" v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedTime" label="使用时间" width="170">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.usedTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="170">
          <template #default="scope">
            <span class="expire-text" :class="{ 'is-expired': isExpired(scope.row.expireTime) }">
              {{ scope.row.expireTime ? formatTime(scope.row.expireTime) : '永不过期' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="130">
          <template #default="scope">
            <span class="remark-text">{{ scope.row.remark || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-popconfirm
              v-if="scope.row.status === 'UNUSED'"
              title="确定删除该邀请码？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              :icon="InfoFilled"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" text size="small" class="del-btn">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="data.pageNum"
          :page-size="data.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="data.total"
        />
      </div>
    </div>

    <!-- 生成邀请码抽屉 -->
    <el-drawer v-model="showGenerateDrawer" title="生成邀请码" size="420px" :with-header="true" class="generate-drawer">
      <div class="drawer-content">
        <div class="form-section">
          <div class="form-section-title">类型设置</div>
          <div class="type-selector">
            <div
              class="type-option"
              :class="{ active: data.generateForm.targetRole === 'USER' }"
              @click="data.generateForm.targetRole = 'USER'"
            >
              <div class="type-icon type-icon-user">
                <el-icon><User /></el-icon>
              </div>
              <div class="type-info">
                <div class="type-name">玩家邀请码</div>
                <div class="type-desc">注册为服务器玩家</div>
              </div>
              <div class="type-check">
                <el-icon><Check /></el-icon>
              </div>
            </div>
            <div
              class="type-option"
              :class="{ active: data.generateForm.targetRole === 'HELPER' }"
              @click="data.generateForm.targetRole = 'HELPER'"
            >
              <div class="type-icon type-icon-examiner">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="type-info">
                <div class="type-name">阅卷人邀请码</div>
                <div class="type-desc">注册为阅卷工作人员</div>
              </div>
              <div class="type-check">
                <el-icon><Check /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="form-section">
          <div class="form-section-title">生成参数</div>
          <div class="form-row">
            <label>生成数量</label>
            <el-input-number v-model="data.generateForm.count" :min="1" :max="100" size="large" />
            <span class="form-tip">最多 100 个/次</span>
          </div>
          <div class="form-row">
            <label>有效天数</label>
            <el-input-number v-model="data.generateForm.daysValid" :min="0" :max="365" size="large" />
            <span class="form-tip">0 表示永不过期</span>
          </div>
          <div class="form-row">
            <label>备注信息</label>
            <el-input
              v-model="data.generateForm.remark"
              type="textarea"
              :rows="2"
              placeholder="选填，便于备注用途..."
              resize="none"
            />
          </div>
        </div>

        <div class="drawer-footer">
          <el-button @click="showGenerateDrawer = false" size="large">取消</el-button>
          <el-button type="primary" size="large" @click="generateCodes" :loading="data.generating" class="gen-btn">
            立即生成
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 生成结果对话框 -->
    <el-dialog
      v-model="data.resultDialogVisible"
      title="邀请码生成成功"
      width="560px"
      class="result-dialog"
      :show-close="false"
    >
      <div class="result-body">
        <div class="result-header">
          <div class="result-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="12" fill="#e8f5e9"/>
              <path d="M8 12l3 3 5-5" stroke="#4caf50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="result-info">
            <div class="result-count">{{ generatedCount }} 个邀请码</div>
            <div class="result-type">{{ data.generateForm.targetRole === 'USER' ? '玩家邀请码' : '阅卷人邀请码' }}</div>
          </div>
        </div>
        <el-alert
          title="请复制并妥善保存，关闭后无法再次查看"
          type="warning"
          :closable="false"
          show-icon
          class="result-alert"
        />
        <el-input
          type="textarea"
          :rows="8"
          v-model="data.generatedCodes"
          readonly
          class="result-textarea"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.resultDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="copyCodes" class="copy-btn">
            <el-icon><CopyDocument /></el-icon> 复制全部
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, User, Delete, CopyDocument, Check, Reading, InfoFilled } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const showGenerateDrawer = ref(false)
const generatedCount = ref(0)

const data = reactive({
  loading: false,
  generating: false,
  deleting: false,
  tableData: [],
  searchForm: {
    status: '',
    code: '',
    targetRole: ''
  },
  generateForm: {
    count: 10,
    daysValid: 0,
    remark: '',
    targetRole: 'USER'
  },
  statistics: {
    total: 0,
    studentUnused: 0,
    examinerUnused: 0,
    unused: 0,
    used: 0
  },
  pageNum: 1,
  pageSize: 10,
  total: 0,
  resultDialogVisible: false,
  generatedCodes: ''
})

// 加载统计数据
const loadStatistics = () => {
  request.get('/invitationCode/statistics').then(res => {
    if (res.code === '200') {
      Object.assign(data.statistics, res.data)
    }
  })
}

// 加载数据
const load = () => {
  data.loading = true
  request.get('/invitationCode/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: data.searchForm.status || undefined,
      code: data.searchForm.code || undefined,
      targetRole: data.searchForm.targetRole || undefined
    }
  }).then(res => {
    data.loading = false
    if (res.code === '200') {
      data.tableData = res.data.list
      data.total = res.data.total
      loadStatistics()
    } else {
      ElMessage.error(res.msg || '加载数据失败')
    }
  }).catch(() => {
    data.loading = false
    ElMessage.error('网络错误，请稍后再试')
  })
}

// 重置搜索
const reset = () => {
  data.searchForm = { status: '', code: '', targetRole: '' }
  data.pageNum = 1
  load()
}

// 生成邀请码
const generateCodes = () => {
  if (!data.generateForm.count || data.generateForm.count <= 0) {
    ElMessage.error('请输入有效的生成数量')
    return
  }
  if (data.generateForm.count > 100) {
    ElMessage.error('单次最多生成100个邀请码')
    return
  }
  data.generating = true
  request.post('/invitationCode/generate', data.generateForm).then(res => {
    data.generating = false
    if (res.code === '200') {
      generatedCount.value = res.data.length
      data.generatedCodes = res.data.join('\n')
      data.resultDialogVisible = true
      showGenerateDrawer.value = false
      load()
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  }).catch(() => {
    data.generating = false
    ElMessage.error('网络错误，请稍后再试')
  })
}

// 复制单条
const copyOne = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('已复制: ' + code)
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 复制全部
const copyCodes = () => {
  navigator.clipboard.writeText(data.generatedCodes).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 删除
const handleDelete = (id) => {
  request.delete('/invitationCode/delete/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  })
}

// 清理所有未使用
const deleteAllUnused = () => {
  const totalUnused = (data.statistics.studentUnused || 0) + (data.statistics.examinerUnused || 0)
  if (totalUnused === 0) {
    ElMessage.warning('当前没有未使用的邀请码')
    return
  }
  ElMessageBox.confirm(
    `确定要删除所有未使用的邀请码吗？共 ${totalUnused} 个，删除后无法恢复。`,
    '批量清理确认',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      icon: InfoFilled
    }
  ).then(() => {
    data.deleting = true
    request.delete('/invitationCode/deleteAllUnused').then(res => {
      data.deleting = false
      if (res.code === '200') {
        ElMessage.success('成功删除 ' + res.data + ' 个未使用的邀请码')
        load()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(() => {
      data.deleting = false
    })
  }).catch(() => {})
}

// 格式化时间
const formatTime = (val) => {
  if (!val) return '—'
  if (typeof val === 'string') {
    return val.replace('T', ' ').substring(0, 19)
  }
  return String(val).replace('T', ' ').substring(0, 19)
}

// 是否过期
const isExpired = (val) => {
  if (!val) return false
  const t = typeof val === 'string' ? val.replace(' ', 'T') : String(val).replace(' ', 'T')
  return new Date(t) < new Date()
}

// 分页
const handleSizeChange = (size) => {
  data.pageSize = size
  load()
}
const handleCurrentChange = (page) => {
  data.pageNum = page
  load()
}

onMounted(() => {
  load()
  loadStatistics()
})
</script>

<style scoped>
.invite-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== 页面标题区 ===== */
.page-hero {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #a855f7 100%);
  border-radius: 16px;
  padding: 32px 36px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.3);
  position: relative;
  overflow: hidden;
}
.page-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}
.hero-content {
  display: flex;
  align-items: center;
  gap: 20px;
}
.hero-icon {
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
}
.hero-text h1 {
  margin: 0 0 4px 0;
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}
.hero-text p {
  margin: 0;
  font-size: 14px;
  color: rgba(255,255,255,0.75);
}
.hero-actions :deep(.el-button) {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.35) !important;
  color: #fff !important;
  font-weight: 600;
  backdrop-filter: blur(10px);
  padding: 12px 24px;
  font-size: 15px;
  transition: all 0.3s;
}
.hero-actions :deep(.el-button:hover) {
  background: rgba(255,255,255,0.35) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(255,255,255,0.2);
}
.btn-icon {
  font-size: 18px;
  margin-right: 4px;
}

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-item {
  border-radius: 14px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}
.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
}
.stat-inner {
  padding: 24px 28px;
  position: relative;
}
.stat-total .stat-inner {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
}
.stat-user .stat-inner {
  background: linear-gradient(135deg, #059669, #10b981);
  color: #fff;
}
.stat-examiner .stat-inner {
  background: linear-gradient(135deg, #d97706, #f59e0b);
  color: #fff;
}
.stat-used .stat-inner {
  background: linear-gradient(135deg, #64748b, #94a3b8);
  color: #fff;
}
.stat-num {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -1px;
}
.stat-label {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.85;
}
.stat-desc {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 3px;
}
.stat-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(255,255,255,0.3);
}

/* ===== 工具栏 ===== */
.toolbar {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--el-border-color-lighter);
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.search-input-wrap {
  position: relative;
  flex: 1;
  max-width: 280px;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  z-index: 1;
  pointer-events: none;
}
.search-input :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: 10px;
}
.filter-select {
  width: 140px;
}
.filter-select :deep(.el-select__wrapper) {
  border-radius: 10px;
}
.reset-btn {
  border-radius: 10px;
  font-weight: 500;
}
.cleanup-btn {
  border-radius: 10px;
  font-weight: 500;
}

/* ===== 数据表格 ===== */
.table-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--el-border-color-lighter);
}
.invite-table {
  border-radius: 14px 14px 0 0;
}
.invite-table :deep(.el-table__row) {
  transition: background 0.2s;
}
.invite-table :deep(.el-table__row:hover > td) {
  background: #f5f3ff !important;
}

/* 邀请码单元格 */
.code-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.code-text {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  font-weight: 600;
  color: #4f46e5;
  background: #ede9fe;
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: 1px;
}
.copy-icon {
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;
  font-size: 14px;
}
.copy-icon:hover {
  color: #4f46e5;
}

/* 类型标签 */
.type-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.type-user {
  background: #dcfce7;
  color: #16a34a;
}
.type-examiner {
  background: #fef3c7;
  color: #d97706;
}

/* 状态 */
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}
.status-unused .dot { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.status-unused { color: #10b981; }
.status-used .dot { background: #94a3b8; }
.status-used { color: #64748b; }

.user-name {
  font-weight: 500;
  color: #374151;
}
.user-empty {
  color: #d1d5db;
}
.time-text, .expire-text {
  font-size: 13px;
  color: #6b7280;
}
.expire-text.is-expired {
  color: #ef4444;
}
.remark-text {
  font-size: 13px;
  color: #6b7280;
}
.del-btn {
  font-size: 15px;
}

/* 分页 */
.pagination-wrap {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* ===== 生成抽屉 ===== */
.drawer-content {
  padding: 0 4px;
}
.form-section {
  margin-bottom: 8px;
}
.form-section-title {
  font-size: 13px;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 14px;
}

/* 类型选择器 */
.type-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.type-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid var(--el-border-color-light);
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
}
.type-option:hover {
  border-color: #a5b4fc;
  background: #f5f3ff;
}
.type-option.active {
  border-color: #4f46e5;
  background: #ede9fe;
}
.type-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.type-icon-user {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
}
.type-icon-examiner {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
}
.type-info {
  flex: 1;
}
.type-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 2px;
}
.type-desc {
  font-size: 12px;
  color: #9ca3af;
}
.type-check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #d1d5db;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.25s;
}
.type-option.active .type-check {
  background: #4f46e5;
}

.form-row {
  margin-bottom: 16px;
}
.form-row label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}
.form-tip {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
  display: block;
}

.drawer-footer {
  padding: 16px 0 0;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.gen-btn {
  min-width: 120px;
}

/* ===== 结果对话框 ===== */
.result-body {
  padding: 4px 0;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.result-count {
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
}
.result-type {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 2px;
}
.result-alert {
  margin-bottom: 12px;
  border-radius: 10px;
}
.result-textarea :deep(.el-textarea__inner) {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
  border-radius: 10px;
  background: #f9fafb;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .page-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .toolbar {
    flex-direction: column;
    gap: 12px;
  }
  .toolbar-left {
    flex-wrap: wrap;
  }
  .search-input-wrap {
    max-width: 100%;
  }
}
</style>
