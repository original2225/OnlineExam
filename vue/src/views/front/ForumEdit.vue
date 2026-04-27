<template>
  <div class="main-content" v-loading="data.loading">
    <div class="card" style="max-width: 800px; margin: 0 auto">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 24px">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 style="margin: 0">编辑帖子</h2>
      </div>

      <el-form :model="data.form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="data.form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="data.form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :action="uploadUrl" :on-success="handleCoverSuccess" :show-file-list="false" accept="image/*">
            <img v-if="data.form.coverUrl" :src="data.form.coverUrl" style="width: 200px; height: 120px; object-fit: cover; border-radius: 8px" />
            <el-button v-else type="primary" plain size="small">上传封面</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="data.form.content" type="textarea" :rows="12" placeholder="请输入帖子内容（支持HTML）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";

const route = useRoute()
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  loading: false,
  form: {}
})

const loadPost = () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('帖子ID不存在')
    router.push('/front/forum')
    return
  }
  data.loading = true
  request.get('/forumPost/selectById/' + id).then(res => {
    if (res.code === '200' && res.data) {
      data.form = {
        id: res.data.id,
        title: res.data.title,
        content: res.data.content,
        tags: res.data.tags || '',
        coverUrl: res.data.coverUrl || ''
      }
    } else {
      ElMessage.error('帖子不存在')
      router.push('/front/forum')
    }
    data.loading = false
  }).catch(() => {
    data.loading = false
    ElMessage.error('加载失败')
  })
}

const save = () => {
  if (!data.form.title?.trim()) return ElMessage.warning('标题不能为空')
  if (!data.form.content?.trim()) return ElMessage.warning('内容不能为空')
  request.put('/forumPost/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      router.push('/front/forumDetail?id=' + data.form.id)
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  })
}

const handleCoverSuccess = (res) => {
  if (res.code === '200') {
    data.form.coverUrl = res.data
  }
}

onMounted(() => {
  loadPost()
})
</script>
