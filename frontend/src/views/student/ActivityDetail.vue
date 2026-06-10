<template>
  <div class="activity-detail" v-loading="loading">
    <el-button class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>返回
    </el-button>

    <template v-if="activity">
      <div class="detail-header card">
        <div class="cover">
          <img v-if="activity.coverImage" :src="activity.coverImage" :alt="activity.title" />
          <div v-else class="cover-placeholder">
            <el-icon :size="64"><Calendar /></el-icon>
          </div>
        </div>
        <div class="info">
          <div class="tags">
            <el-tag :type="activity.activityType === 'ONLINE' ? 'primary' : 'success'">
              {{ activity.activityType === 'ONLINE' ? '线上活动' : '线下活动' }}
            </el-tag>
            <el-tag v-if="activity.status === 1" type="success">报名中</el-tag>
            <el-tag v-else type="info">已结束</el-tag>
          </div>
          <h1>{{ activity.title }}</h1>
          <p class="summary">{{ activity.summary }}</p>

          <div class="meta-list">
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>报名时间：{{ formatDateTime(activity.signupStartTime) }} - {{ formatDateTime(activity.signupEndTime) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>活动时间：{{ formatDateTime(activity.activityStartTime) }} - {{ formatDateTime(activity.activityEndTime) }}</span>
            </div>
            <div class="meta-item" v-if="activity.feedbackStartTime || activity.feedbackEndTime">
              <el-icon><EditPen /></el-icon>
              <span>反馈时间：{{ formatDateTime(activity.feedbackStartTime) }} - {{ formatDateTime(activity.feedbackEndTime) }}</span>
            </div>
            <div class="meta-item" v-if="activity.location">
              <el-icon><Location /></el-icon>
              <span>活动地点：{{ activity.location }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>已报名：{{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants || '不限' }} 人</span>
            </div>
          </div>

          <div class="action-area">
            <el-button
              v-if="!hasRegistered"
              type="primary"
              size="large"
              :disabled="!canRegister"
              @click="showRegisterDialog = true"
            >
              {{ canRegister ? '立即报名' : '暂不可报名' }}
            </el-button>
            <el-button v-else type="success" size="large" disabled>
              <el-icon><Check /></el-icon>已报名
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-content card">
        <h2>活动详情</h2>
        <div class="content-body" v-html="safeContent || '暂无详情'" />

        <div class="attachments" v-if="attachments.length > 0">
          <h3>相关附件</h3>
          <div class="attachment-list">
            <div
              v-for="(file, index) in attachments"
              :key="index"
              class="attachment-item"
              @click="downloadFile(file)"
            >
              <el-icon><Document /></el-icon>
              <span>{{ file.name }}</span>
              <el-icon><Download /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="requirements card" v-if="hasRequirements">
        <h2>报名条件</h2>
        <div class="requirement-list">
          <div class="requirement-item" v-if="activity.requiredGpa">
            <span class="label">绩点要求</span>
            <span class="value">≥ {{ activity.requiredGpa }}</span>
          </div>
          <div class="requirement-item" v-if="activity.requiredGrades">
            <span class="label">年级要求</span>
            <span class="value">{{ activity.requiredGrades }}</span>
          </div>
          <div class="requirement-item" v-if="activity.requiredMajors">
            <span class="label">专业要求</span>
            <span class="value">{{ activity.requiredMajors }}</span>
          </div>
        </div>
      </div>
    </template>

    <el-dialog
      v-model="showRegisterDialog"
      title="活动报名"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
      >
        <template v-for="field in registrationFields" :key="field.key">
          <el-form-item
            v-if="field.key === 'targetSchool'"
            :label="field.label"
            prop="targetSchool"
          >
            <el-autocomplete
              v-model="registerForm.targetSchool"
              :fetch-suggestions="searchSchool"
              :placeholder="field.placeholder || '请输入招生学校'"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item
            v-else-if="field.key === 'contactInfo'"
            :label="field.label"
            prop="contactInfo"
          >
            <el-input
              v-model="registerForm.contactInfo"
              :placeholder="field.placeholder || '请输入联系方式'"
            />
          </el-form-item>

          <el-form-item
            v-else-if="field.key === 'remark'"
            :label="field.label"
            prop="remark"
          >
            <el-input
              v-model="registerForm.remark"
              type="textarea"
              :rows="3"
              :placeholder="field.placeholder || '请输入补充说明'"
            />
          </el-form-item>

          <el-form-item
            v-else-if="field.key === 'attachments'"
            :label="field.label"
            prop="attachments"
          >
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
                <div class="el-upload__tip">{{ field.placeholder || '支持 PDF、Word、Excel 等格式' }}</div>
              </template>
            </el-upload>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="submitRegister">
          确认报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Calendar, Check, Clock, Document, Download,
  EditPen, Location, Upload, User
} from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'
import { sanitizeRichText } from '@/utils/richText'
import { downloadProtectedFile } from '@/utils/file'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const activity = ref(null)
const hasRegistered = ref(false)
const showRegisterDialog = ref(false)
const registering = ref(false)
const registerFormRef = ref(null)
const fileList = ref([])

const registerForm = reactive({
  targetSchool: '',
  contactInfo: '',
  remark: '',
  attachments: ''
})

const uploadUrl = '/api/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const attachments = computed(() => {
  if (!activity.value?.attachments) return []
  try {
    return JSON.parse(activity.value.attachments)
  } catch {
    return []
  }
})

const registrationFields = computed(() => {
  const defaults = {
    targetSchool: { key: 'targetSchool', label: '招生学校', required: true, placeholder: '请输入招生学校' },
    contactInfo: { key: 'contactInfo', label: '联系方式', required: true, placeholder: '请输入联系电话或邮箱' },
    remark: { key: 'remark', label: '补充说明', required: false, placeholder: '请输入补充说明' },
    attachments: { key: 'attachments', label: '报名附件', required: false, placeholder: '支持 PDF、Word、Excel 等格式' }
  }

  try {
    const parsed = JSON.parse(activity.value?.registrationConfig || '[]')
    if (parsed.length > 0) {
      return parsed.map(item => ({ ...defaults[item.key], ...item })).filter(Boolean)
    }
  } catch {
    // ignore invalid config
  }

  return [defaults.targetSchool, defaults.contactInfo, defaults.remark, defaults.attachments]
})

const registerRules = computed(() => {
  const rules = {}
  registrationFields.value.forEach(field => {
    if (!field.required) return
    rules[field.key] = [{
      required: true,
      message: `请填写${field.label}`,
      trigger: field.key === 'attachments' ? 'change' : 'blur'
    }]
  })
  if (registrationFields.value.some(field => field.key === 'attachments' && field.required)) {
    rules.attachments = [{
      validator: (_rule, _value, callback) => {
        if (fileList.value.length === 0) {
          callback(new Error('请上传报名附件'))
          return
        }
        callback()
      },
      trigger: 'change'
    }]
  }
  return rules
})

const safeContent = computed(() => sanitizeRichText(activity.value?.content || ''))

const hasRequirements = computed(() => (
  activity.value?.requiredGpa || activity.value?.requiredGrades || activity.value?.requiredMajors
))

const canRegister = computed(() => {
  if (!activity.value) return false
  if (activity.value.status !== 1) return false
  const now = new Date()
  return now >= new Date(activity.value.signupStartTime) && now <= new Date(activity.value.signupEndTime)
})

const formatDateTime = (date) => {
  if (!date) return '未设置'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadActivity = async () => {
  loading.value = true
  try {
    const res = await api.activity.getById(route.params.id)
    if (res.code === 200) {
      activity.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    ElMessage.error('加载活动详情失败')
  } finally {
    loading.value = false
  }
}

const checkRegistration = async () => {
  try {
    const res = await api.registration.check(route.params.id)
    if (res.code === 200) {
      hasRegistered.value = res.data
    }
  } catch {
    // ignore
  }
}

const searchSchool = async (query, cb) => {
  if (!query) {
    cb([])
    return
  }
  try {
    const res = await api.school.suggest(query)
    cb(res.code === 200 ? res.data.map(name => ({ value: name })) : [])
  } catch {
    cb([])
  }
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    fileList.value.push({
      name: file.name,
      url: response.data.url,
      storedName: response.data.storedName
    })
    registerForm.attachments = JSON.stringify(fileList.value)
  } else {
    ElMessage.error('文件上传失败')
  }
}

const handleUploadRemove = (file) => {
  const index = fileList.value.findIndex(item => item.name === file.name || item.url === file.url)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  registerForm.attachments = JSON.stringify(fileList.value)
}

const submitRegister = async () => {
  if (!registerFormRef.value) return

  registerForm.attachments = JSON.stringify(fileList.value)
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    registering.value = true
    try {
      const data = {
        activityId: Number(route.params.id),
        targetSchool: registerForm.targetSchool,
        contactInfo: registerForm.contactInfo,
        remark: registerForm.remark,
        attachments: fileList.value.length > 0 ? JSON.stringify(fileList.value) : ''
      }

      const res = await api.registration.register(data)
      if (res.code === 200) {
        ElMessage.success('报名成功')
        showRegisterDialog.value = false
        hasRegistered.value = true
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '报名失败')
    } finally {
      registering.value = false
    }
  })
}

const downloadFile = async (file) => {
  try {
    await downloadProtectedFile(file, localStorage.getItem('token'))
  } catch (error) {
    ElMessage.error(error.message || '附件下载失败')
  }
}

onMounted(() => {
  loadActivity()
  checkRegistration()
})
</script>

<style lang="scss" scoped>
.activity-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-header {
  display: flex;
  gap: 32px;
  
  .cover {
    flex-shrink: 0;
    width: 300px;
    height: 200px;
    border-radius: 12px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .cover-placeholder {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.5);
    }
  }
  
  .info {
    flex: 1;
    
    .tags {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
    }
    
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 12px;
    }
    
    .summary {
      color: #6b7280;
      margin-bottom: 20px;
      line-height: 1.6;
    }
    
    .meta-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #4b5563;
        font-size: 14px;
        
        .el-icon {
          color: #667eea;
        }
      }
    }
  }
}

.detail-content {
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .content-body {
    line-height: 1.8;
    color: #374151;
    
    :deep(img) {
      max-width: 100%;
      border-radius: 8px;
    }
  }
  
  .attachments {
    margin-top: 24px;
    
    h3 {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 12px;
    }
    
    .attachment-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .attachment-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: #f9fafb;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f3f4f6;
      }
      
      span {
        flex: 1;
      }
    }
  }
}

.requirements {
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }
  
  .requirement-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .requirement-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: #f9fafb;
    border-radius: 8px;
    
    .label {
      color: #6b7280;
    }
    
    .value {
      color: #667eea;
      font-weight: 500;
    }
  }
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    
    .cover {
      width: 100%;
    }
  }
}
</style>
