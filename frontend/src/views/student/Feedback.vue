<template>
  <div class="feedback-page">
    <el-button class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>返回
    </el-button>
    
    <div class="page-header">
      <h1>工作反馈</h1>
      <p>提交您的活动工作反馈与总结</p>
    </div>
    
    <!-- 新增反馈 -->
    <div class="feedback-form card">
      <h2>提交反馈</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="反馈类型" prop="feedbackType">
          <el-radio-group v-model="form.feedbackType">
            <el-radio value="SUMMARY">工作总结</el-radio>
            <el-radio value="PROGRESS">进度汇报</el-radio>
            <el-radio value="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="反馈标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入反馈标题" />
        </el-form-item>
        
        <el-form-item label="反馈内容" prop="content">
          <RichTextEditor
            v-model="form.content"
            placeholder="请输入反馈内容，支持标题、列表、链接等富文本内容"
          />
        </el-form-item>
        
        <el-form-item label="附件上传">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>上传附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">支持PDF、Word、Excel、图片、视频等格式</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitFeedback">
            提交反馈
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 历史反馈列表 -->
    <div class="feedback-history card" v-if="feedbacks.length > 0">
      <h2>历史反馈</h2>
      <div class="feedback-list">
        <div 
          v-for="fb in feedbacks" 
          :key="fb.id" 
          class="feedback-item"
        >
          <div class="feedback-header">
            <div class="title-row">
              <h3>{{ fb.title }}</h3>
              <el-tag size="small" :type="getTypeTagType(fb.feedbackType)">
                {{ getFeedbackTypeText(fb.feedbackType) }}
              </el-tag>
            </div>
            <div class="feedback-time">
              {{ formatDateTime(fb.createTime) }}
            </div>
          </div>
          <div class="feedback-content" v-html="getSafeContent(fb.content)" />
          <div class="feedback-attachments" v-if="getAttachments(fb).length > 0">
            <span class="label">附件：</span>
            <el-tag 
              v-for="(file, index) in getAttachments(fb)" 
              :key="index"
              size="small"
              @click="downloadFile(file)"
              class="attachment-tag"
            >
              {{ file.name }}
            </el-tag>
          </div>
          <div class="feedback-actions">
            <el-button type="primary" text size="small" @click="editFeedback(fb)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" text size="small" @click="deleteFeedback(fb)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Upload, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { sanitizeRichText } from '@/utils/richText'
import { downloadProtectedFile } from '@/utils/file'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)
const feedbacks = ref([])
const fileList = ref([])
const editingId = ref(null)

const form = reactive({
  feedbackType: 'SUMMARY',
  title: '',
  content: ''
})

const rules = {
  feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

const uploadUrl = '/api/files/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
}))

const registrationId = computed(() => route.params.registrationId)

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getFeedbackTypeText = (type) => {
  const map = {
    'SUMMARY': '工作总结',
    'PROGRESS': '进度汇报',
    'OTHER': '其他'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    'SUMMARY': 'success',
    'PROGRESS': 'primary',
    'OTHER': 'info'
  }
  return map[type] || 'info'
}

const getAttachments = (fb) => {
  if (!fb.attachments) return []
  try {
    return JSON.parse(fb.attachments)
  } catch {
    return []
  }
}

const getSafeContent = (content) => sanitizeRichText(content || '')

const loadFeedbacks = async () => {
  try {
    const res = await api.feedback.getByRegistration(registrationId.value)
    if (res.code === 200) {
      feedbacks.value = res.data || []
    }
  } catch (error) {
    console.error('加载反馈列表失败', error)
  }
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    fileList.value.push({
      name: file.name,
      url: response.data.url,
      storedName: response.data.storedName
    })
  } else {
    ElMessage.error('文件上传失败')
  }
}

const handleUploadRemove = (file) => {
  const index = fileList.value.findIndex(f => f.name === file.name)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const submitFeedback = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 获取报名记录详情以获取 activityId
      const regRes = await api.registration.getById(registrationId.value)
      if (regRes.code !== 200) {
        ElMessage.error('获取报名信息失败')
        return
      }
      
      const data = {
        activityId: regRes.data.activityId,
        registrationId: Number(registrationId.value),
        feedbackType: form.feedbackType,
        title: form.title,
        content: sanitizeRichText(form.content),
        attachments: fileList.value.length > 0 ? JSON.stringify(fileList.value) : ''
      }
      
      let res
      if (editingId.value) {
        res = await api.feedback.update(editingId.value, data)
      } else {
        res = await api.feedback.create(data)
      }
      
      if (res.code === 200) {
        ElMessage.success(editingId.value ? '更新成功' : '提交成功')
        resetForm()
        loadFeedbacks()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const editFeedback = (fb) => {
  editingId.value = fb.id
  form.feedbackType = fb.feedbackType
  form.title = fb.title
  form.content = fb.content
  fileList.value = getAttachments(fb)
  
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const deleteFeedback = async (fb) => {
  try {
    await ElMessageBox.confirm('确定要删除这条反馈吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.feedback.delete(fb.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadFeedbacks()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 取消删除
  }
}

const resetForm = () => {
  editingId.value = null
  form.feedbackType = 'SUMMARY'
  form.title = ''
  form.content = ''
  fileList.value = []
  formRef.value?.resetFields()
}

const downloadFile = async (file) => {
  try {
    await downloadProtectedFile(file, localStorage.getItem('token'))
  } catch (error) {
    ElMessage.error(error.message || '附件下载失败')
  }
}

onMounted(() => {
  loadFeedbacks()
})
</script>

<style lang="scss" scoped>
.feedback-page {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.feedback-form, .feedback-history {
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e7eb;
  }
}

.feedback-history {
  .feedback-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .feedback-item {
    padding: 20px;
    background: #f9fafb;
    border-radius: 12px;
    
    .feedback-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
      
      .title-row {
        display: flex;
        align-items: center;
        gap: 8px;
        
        h3 {
          font-size: 16px;
          font-weight: 500;
          color: #1f2937;
        }
      }
      
      .feedback-time {
        font-size: 12px;
        color: #9ca3af;
      }
    }
    
    .feedback-content {
      color: #4b5563;
      line-height: 1.6;
      margin-bottom: 12px;

      :deep(p:first-child) {
        margin-top: 0;
      }
    }
    
    .feedback-attachments {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;
      
      .label {
        font-size: 13px;
        color: #6b7280;
      }
      
      .attachment-tag {
        cursor: pointer;
      }
    }
    
    .feedback-actions {
      display: flex;
      gap: 8px;
      justify-content: flex-end;
      padding-top: 12px;
      border-top: 1px solid #e5e7eb;
    }
  }
}
</style>
