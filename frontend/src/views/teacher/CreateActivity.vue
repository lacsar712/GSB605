<template>
  <div class="create-activity">
    <el-button class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>返回
    </el-button>

    <div class="page-header">
      <h1>{{ isEdit ? '编辑活动' : '创建活动' }}</h1>
    </div>

    <div class="card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-divider content-position="left">基本信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="activityType">
              <el-radio-group v-model="form.activityType">
                <el-radio value="ONLINE">线上活动</el-radio>
                <el-radio value="OFFLINE">线下活动</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动简介" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入活动简介" />
        </el-form-item>

        <el-form-item label="活动详情" prop="content">
          <RichTextEditor
            v-model="form.content"
            placeholder="请输入活动详情，支持标题、列表、链接等富文本内容"
          />
        </el-form-item>

        <el-form-item label="活动附件">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon>上传附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">可上传活动通知、报名说明等附件</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider content-position="left">时间地点</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名时间" prop="signupTime">
              <el-date-picker
                v-model="form.signupTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动时间" prop="activityTime">
              <el-date-picker
                v-model="form.activityTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="反馈时间">
              <el-date-picker
                v-model="form.feedbackTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="反馈开始"
                end-placeholder="反馈截止"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动地点">
              <el-input v-model="form.location" placeholder="线下活动请填写地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人数限制">
              <el-input-number v-model="form.maxParticipants" :min="0" placeholder="0表示不限" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">报名条件</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="绩点要求">
              <el-input-number v-model="form.requiredGpa" :min="0" :max="5" :step="0.1" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年级要求">
              <el-select v-model="form.requiredGradesList" multiple placeholder="不限" style="width: 100%">
                <el-option label="大一" value="大一" />
                <el-option label="大二" value="大二" />
                <el-option label="大三" value="大三" />
                <el-option label="大四" value="大四" />
                <el-option label="研一" value="研一" />
                <el-option label="研二" value="研二" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="需要审批">
              <el-switch v-model="form.needApproval" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">报名信息配置</el-divider>

        <div class="registration-config">
          <div
            v-for="field in registrationFields"
            :key="field.key"
            class="config-item"
          >
            <div class="config-row">
              <el-switch v-model="field.enabled" />
              <span class="field-label">{{ field.label }}</span>
              <el-checkbox v-model="field.required" :disabled="!field.enabled">
                必填
              </el-checkbox>
            </div>
            <el-input
              v-model="field.placeholder"
              :disabled="!field.enabled"
              :placeholder="`${field.label}输入提示`"
            />
          </div>
        </div>

        <el-divider content-position="left">展示设置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设为轮播">
              <el-switch v-model="form.isBanner" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveActivity">
            {{ isEdit ? '保存修改' : '创建活动' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Upload } from '@element-plus/icons-vue'
import api from '@/api'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { sanitizeRichText } from '@/utils/richText'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const saving = ref(false)
const fileList = ref([])

const defaultRegistrationFields = () => [
  { key: 'targetSchool', label: '招生学校', enabled: true, required: true, placeholder: '请输入招生学校' },
  { key: 'contactInfo', label: '联系方式', enabled: true, required: true, placeholder: '请输入联系电话或邮箱' },
  { key: 'remark', label: '补充说明', enabled: true, required: false, placeholder: '请输入补充说明' },
  { key: 'attachments', label: '报名附件', enabled: true, required: false, placeholder: '请上传报名附件' }
]

const registrationFields = ref(defaultRegistrationFields())
const isEdit = computed(() => !!route.query.id)
const uploadUrl = '/api/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const form = reactive({
  title: '',
  summary: '',
  content: '',
  activityType: 'OFFLINE',
  signupTime: [],
  activityTime: [],
  feedbackTime: [],
  location: '',
  maxParticipants: 0,
  requiredGpa: 0,
  requiredGradesList: [],
  needApproval: true,
  isBanner: false,
  status: 0
})

const rules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  summary: [{ required: true, message: '请输入活动简介', trigger: 'blur' }],
  content: [{ required: true, message: '请输入活动详情', trigger: 'blur' }],
  signupTime: [{ required: true, message: '请选择报名时间', trigger: 'change' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const parseAttachments = (attachments) => {
  if (!attachments) return []
  try {
    return JSON.parse(attachments)
  } catch {
    return []
  }
}

const parseRegistrationConfig = (config) => {
  const fields = defaultRegistrationFields()
  if (!config) return fields

  try {
    const parsed = JSON.parse(config)
    return fields.map(field => {
      const matched = parsed.find(item => item.key === field.key)
      return matched
        ? {
            ...field,
            enabled: true,
            required: !!matched.required,
            placeholder: matched.placeholder || field.placeholder
          }
        : { ...field, enabled: false, required: false }
    })
  } catch {
    return fields
  }
}

const serializeRegistrationConfig = () => JSON.stringify(
  registrationFields.value
    .filter(field => field.enabled)
    .map(field => ({
      key: field.key,
      label: field.label,
      required: field.required,
      placeholder: field.placeholder
    }))
)

const loadActivity = async () => {
  if (!route.query.id) return

  try {
    const res = await api.activity.getById(route.query.id)
    if (res.code === 200) {
      const data = res.data
      form.title = data.title
      form.summary = data.summary
      form.content = data.content
      form.activityType = data.activityType
      form.location = data.location
      form.maxParticipants = data.maxParticipants || 0
      form.requiredGpa = data.requiredGpa || 0
      form.requiredGradesList = data.requiredGrades ? data.requiredGrades.split(',') : []
      form.needApproval = data.needApproval === 1
      form.isBanner = data.isBanner === 1
      form.status = data.status
      fileList.value = parseAttachments(data.attachments)
      registrationFields.value = parseRegistrationConfig(data.registrationConfig)

      if (data.signupStartTime && data.signupEndTime) {
        form.signupTime = [new Date(data.signupStartTime), new Date(data.signupEndTime)]
      }
      if (data.activityStartTime && data.activityEndTime) {
        form.activityTime = [new Date(data.activityStartTime), new Date(data.activityEndTime)]
      }
      if (data.feedbackStartTime && data.feedbackEndTime) {
        form.feedbackTime = [new Date(data.feedbackStartTime), new Date(data.feedbackEndTime)]
      }
    }
  } catch {
    ElMessage.error('加载活动信息失败')
  }
}

const saveActivity = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      const data = {
        title: form.title,
        summary: form.summary,
        content: sanitizeRichText(form.content),
        activityType: form.activityType,
        location: form.location,
        maxParticipants: form.maxParticipants,
        requiredGpa: form.requiredGpa,
        requiredGrades: form.requiredGradesList.join(','),
        needApproval: form.needApproval ? 1 : 0,
        isBanner: form.isBanner ? 1 : 0,
        attachments: fileList.value.length ? JSON.stringify(fileList.value) : '',
        registrationConfig: serializeRegistrationConfig(),
        status: form.status
      }

      if (form.signupTime?.length === 2) {
        data.signupStartTime = form.signupTime[0]
        data.signupEndTime = form.signupTime[1]
      }
      if (form.activityTime?.length === 2) {
        data.activityStartTime = form.activityTime[0]
        data.activityEndTime = form.activityTime[1]
      }
      if (form.feedbackTime?.length === 2) {
        data.feedbackStartTime = form.feedbackTime[0]
        data.feedbackEndTime = form.feedbackTime[1]
      }

      const res = isEdit.value
        ? await api.activity.update(route.query.id, data)
        : await api.activity.create(data)

      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
        router.push('/teacher/activities')
      } else {
        ElMessage.error(res.message)
      }
    } catch {
      ElMessage.error('操作失败')
    } finally {
      saving.value = false
    }
  })
}

const handleUploadSuccess = (response, file) => {
  if (response.code !== 200) {
    ElMessage.error(response.message || '上传失败')
    return
  }
  fileList.value.push({
    name: file.name,
    url: response.data.url,
    storedName: response.data.storedName
  })
}

const handleUploadRemove = (file) => {
  fileList.value = fileList.value.filter(item => item.url !== file.url && item.name !== file.name)
}

onMounted(() => {
  loadActivity()
})
</script>

<style lang="scss" scoped>
.create-activity {
  max-width: 980px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.registration-config {
  display: grid;
  gap: 16px;
  margin-bottom: 20px;
}

.config-item {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
}

.config-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.field-label {
  flex: 1;
  font-weight: 500;
}
</style>
