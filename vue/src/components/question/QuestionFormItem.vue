<template>
  <div>
    <el-form-item prop="categoryId" label="题目分类">
      <el-cascader
        v-model="categoryIdPath"
        :options="categoryTree"
        :props="{ checkStrictly: true, emitPath: false, value: 'value', label: 'label' }"
        placeholder="请选择分类"
        clearable
        style="width: 100%"
        @change="handleCategoryChange"
      />
    </el-form-item>

    <el-form-item prop="type" label="题型">
      <el-select v-model="form.type" placeholder="请选择题型" style="width: 100%" @change="handleTypeChange">
        <el-option label="单选题" value="single" />
        <el-option label="多选题" value="multiple" />
        <el-option label="判断题" value="judge" />
        <el-option label="填空题" value="fillin" />
        <el-option label="简答题" value="essay" />
      </el-select>
    </el-form-item>

    <el-form-item prop="content" label="题目内容">
      <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容（填空题请用 ___ 表示空位）"></el-input>
    </el-form-item>

    <!-- 单选题和多选题选项 -->
    <template v-if="form.type === 'single' || form.type === 'multiple'">
      <el-form-item label="选项设置">
        <div v-for="(option, key) in form.options" :key="key" style="margin-bottom: 8px; display: flex; align-items: center">
          <span style="width: 40px; font-weight: bold">{{ key }}.</span>
          <el-input v-model="form.options[key]" :placeholder="'请输入选项' + key" style="flex: 1"></el-input>
          <el-button type="danger" circle :icon="Delete" size="small" style="margin-left: 8px" @click="removeOption(key)"></el-button>
        </div>
        <el-button type="primary" plain @click="addOption" style="margin-top: 8px">+ 添加选项</el-button>
      </el-form-item>
    </template>

    <!-- 填空题空位设置 -->
    <template v-if="form.type === 'fillin'">
      <el-form-item label="空位答案">
        <div v-for="(blank, index) in fillinBlanks" :key="index" style="margin-bottom: 8px; display: flex; align-items: center">
          <span style="width: 70px; color: #666">空位 {{ index + 1 }}:</span>
          <el-input v-model="fillinBlanks[index]" :placeholder="'请输入第 ' + (index + 1) + ' 个空的答案'" style="flex: 1"></el-input>
          <el-button type="danger" circle :icon="Delete" size="small" style="margin-left: 8px" @click="removeBlank(index)"></el-button>
        </div>
        <el-button type="primary" plain @click="addBlank" style="margin-top: 8px">+ 添加空位</el-button>
        <div style="color: #999; font-size: 12px; margin-top: 6px">提示：在题目内容中用 ___ 标记空位位置</div>
      </el-form-item>
    </template>

    <el-form-item prop="answer" label="正确答案">
      <!-- 单选题答案 -->
      <el-select v-if="form.type === 'single'" v-model="form.answer" placeholder="请选择正确答案" style="width: 100%">
        <el-option
          v-for="(value, key) in form.options"
          :key="key"
          :label="key + ' - ' + value"
          :value="key"
        />
      </el-select>
      <!-- 多选题答案 -->
      <el-select v-else-if="form.type === 'multiple'" v-model="multipleAnswers" multiple placeholder="请选择正确答案" style="width: 100%">
        <el-option
          v-for="(value, key) in form.options"
          :key="key"
          :label="key + ' - ' + value"
          :value="key"
        />
      </el-select>
      <!-- 判断题答案 -->
      <el-select v-else-if="form.type === 'judge'" v-model="form.answer" placeholder="请选择正确答案" style="width: 100%">
        <el-option label="正确" value="true" />
        <el-option label="错误" value="false" />
      </el-select>
      <!-- 填空题答案由上方空位管理 -->
      <div v-else-if="form.type === 'fillin'" style="color: #999; font-size: 13px">
        填空题答案请在上方「空位答案」区域填写
      </div>
      <!-- 简答题答案 -->
      <el-input v-else v-model="form.answer" type="textarea" :rows="3" placeholder="请输入参考答案"></el-input>
    </el-form-item>

    <el-form-item prop="analysis" label="答案解析">
      <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入答案解析（可选）"></el-input>
    </el-form-item>

    <!-- 图片上传区域 -->
    <el-form-item label="题目图片">
      <div class="images-area">
        <div v-for="(img, index) in form.images" :key="index" class="image-item">
          <div class="image-preview">
            <img :src="img.url" alt="题目图片" />
            <div class="image-delete" @click="removeImage(index)">
              <el-icon :size="16"><Close /></el-icon>
            </div>
          </div>
          <el-input
            v-model="img.caption"
            size="small"
            placeholder="图片注释（可选）"
            class="image-caption"
          />
        </div>
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
          accept="image/*"
          class="image-upload-btn"
        >
          <div class="upload-placeholder">
            <el-icon :size="28"><Plus /></el-icon>
            <span>添加图片</span>
          </div>
        </el-upload>
      </div>
    </el-form-item>

    <el-form-item prop="difficulty" label="难度">
      <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
        <el-option label="简单" value="easy" />
        <el-option label="中等" value="medium" />
        <el-option label="困难" value="hard" />
      </el-select>
    </el-form-item>

    <el-form-item prop="score" label="分值">
      <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" :precision="1" style="width: 100%"></el-input-number>
    </el-form-item>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { Delete, Plus, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { loadCascaderCategories } from '@/utils/categoryUtils.js'
import { getUploadHeaders } from '@/utils/upload.js'

const props = defineProps({
  form: Object
})

const emit = defineEmits(['update:form'])

const categoryTree = ref([])
const categoryIdPath = ref(null)
const multipleAnswers = ref([])
const fillinBlanks = ref([''])
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const uploadHeaders = getUploadHeaders()

// 确保 form.images 存在
if (!props.form.images) {
  props.form.images = []
}

// 加载分类树
const loadCategories = () => {
  loadCascaderCategories().then(tree => {
    categoryTree.value = tree
    // 编辑模式下回显已有分类
    if (props.form.categoryId) {
      categoryIdPath.value = props.form.categoryId
    }
  })
}

// 级联选择变化时同步到 form
const handleCategoryChange = (val) => {
  props.form.categoryId = val || null
}

onMounted(() => {
  loadCategories()
})

// 监听多选答案变化
watch(multipleAnswers, (newVal) => {
  if (props.form.type === 'multiple') {
    props.form.answer = newVal.sort().join('')
  }
}, { deep: true })

// 监听填空题空位变化，同步到 answer 字段
watch(fillinBlanks, (newVal) => {
  if (props.form.type === 'fillin') {
    props.form.answer = newVal.filter(b => b.trim()).join('|||')
  }
}, { deep: true })

// 监听表单类型变化，初始化数据
const handleTypeChange = (type) => {
  if (type === 'single' || type === 'multiple') {
    if (!props.form.options || Object.keys(props.form.options).length === 0) {
      props.form.options = { 'A': '', 'B': '', 'C': '', 'D': '' }
    }
  }
  if (type === 'fillin') {
    fillinBlanks.value = ['']
    props.form.answer = ''
  }
  props.form.answer = ''
  multipleAnswers.value = []
}

// 添加选项
const addOption = () => {
  const keys = Object.keys(props.form.options)
  const nextKey = String.fromCharCode(65 + keys.length)
  if (nextKey <= 'Z') {
    props.form.options[nextKey] = ''
  }
}

// 删除选项
const removeOption = (key) => {
  const newOptions = {}
  let index = 0
  for (const k in props.form.options) {
    if (k !== key) {
      const newKey = String.fromCharCode(65 + index)
      newOptions[newKey] = props.form.options[k]
      index++
    }
  }
  props.form.options = newOptions
}

// 填空题：添加空位
const addBlank = () => {
  fillinBlanks.value.push('')
}

// 填空题：删除空位
const removeBlank = (index) => {
  if (fillinBlanks.value.length > 1) {
    fillinBlanks.value.splice(index, 1)
  }
}

// 图片上传：上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 图片上传成功
const handleUploadSuccess = (res) => {
  if (res.code === '200') {
    props.form.images.push({ url: res.data, caption: '' })
  } else {
    ElMessage.error('上传失败')
  }
}

// 删除图片
const removeImage = (index) => {
  props.form.images.splice(index, 1)
}

// 初始化
if (props.form.type && (props.form.type === 'single' || props.form.type === 'multiple')) {
  if (!props.form.options || Object.keys(props.form.options).length === 0) {
    props.form.options = { 'A': '', 'B': '', 'C': '', 'D': '' }
  }
}

// 填空题初始化：从 answer 中解析已有空位
if (props.form.type === 'fillin' && props.form.answer) {
  fillinBlanks.value = props.form.answer.split('|||')
  if (fillinBlanks.value.length === 0) {
    fillinBlanks.value = ['']
  }
}
</script>

<style scoped>
.images-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}

.image-item {
  width: 160px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.image-preview {
  width: 160px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e4e7ed;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview:hover .image-delete {
  opacity: 1;
}

.image-caption {
  font-size: 12px;
}

.image-caption :deep(.el-input__wrapper) {
  font-size: 12px;
}

.image-upload-btn {
  width: 160px;
  height: 120px;
}

.image-upload-btn :deep(.el-upload) {
  width: 160px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.image-upload-btn :deep(.el-upload:hover) {
  border-color: #409eff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}
</style>
