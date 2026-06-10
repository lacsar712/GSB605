<template>
  <div class="my-registrations">
    <div class="page-header">
      <h1>已报名活动</h1>
      <p>查看您已报名的所有活动及审批进度</p>
    </div>
    
    <div class="registrations-list" v-loading="loading">
      <template v-if="registrations.length > 0">
        <div 
          v-for="reg in registrations" 
          :key="reg.id" 
          class="registration-card card"
        >
          <div class="card-header">
            <div class="activity-info">
              <h3>{{ reg.activityTitle || '活动' }}</h3>
              <span :class="['status-tag', reg.approvalStatus?.toLowerCase()]">
                {{ getStatusText(reg.approvalStatus) }}
              </span>
            </div>
            <div class="card-actions">
              <el-button
                type="info"
                size="small"
                plain
                @click="viewActivityDetail(reg)"
              >
                查看活动详情
              </el-button>
              <el-button 
                v-if="reg.approvalStatus === 'APPROVED'" 
                type="primary" 
                size="small"
                @click="goToFeedback(reg)"
              >
                <el-icon><EditPen /></el-icon>提交反馈
              </el-button>
              <el-button 
                v-if="reg.approvalStatus === 'PENDING'" 
                type="danger" 
                size="small"
                plain
                @click="cancelRegistration(reg)"
              >
                取消报名
              </el-button>
            </div>
          </div>
          
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">报名时间</span>
                <span class="value">{{ formatDateTime(reg.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">招生学校</span>
                <span class="value">{{ reg.targetSchool || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">联系方式</span>
                <span class="value">{{ reg.contactInfo || '-' }}</span>
              </div>
              <div class="info-item" v-if="reg.groupId">
                <span class="label">所属分组</span>
                <span class="value">{{ reg.groupName || '分组 ' + reg.groupId }}</span>
              </div>
            </div>
            
            <!-- 审批信息 -->
            <div class="approval-info" v-if="reg.approvalStatus !== 'PENDING'">
              <el-divider />
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">审批时间</span>
                  <span class="value">{{ formatDateTime(reg.approvalTime) }}</span>
                </div>
                <div class="info-item" v-if="reg.approvalComment">
                  <span class="label">审批意见</span>
                  <span class="value">{{ reg.approvalComment }}</span>
                </div>
              </div>
            </div>
            
            <!-- 附件 -->
            <div class="attachments" v-if="getAttachments(reg).length > 0">
              <el-divider />
              <div class="attachment-header">
                <span>已上传附件</span>
              </div>
              <div class="attachment-list">
                <el-tag 
                  v-for="(file, index) in getAttachments(reg)" 
                  :key="index"
                  @click="downloadFile(file)"
                  class="attachment-tag"
                >
                  <el-icon><Document /></el-icon>
                  {{ file.name }}
                </el-tag>
              </div>
            </div>
            
            <!-- 反馈列表 -->
            <div class="feedbacks" v-if="reg.approvalStatus === 'APPROVED'">
              <el-divider />
              <div class="feedback-header">
                <span>工作反馈记录</span>
                <el-button type="primary" text size="small" @click="goToFeedback(reg)">
                  <el-icon><Plus /></el-icon>添加反馈
                </el-button>
              </div>
              <div class="feedback-list" v-if="reg.feedbacks && reg.feedbacks.length > 0">
                <div 
                  v-for="fb in reg.feedbacks" 
                  :key="fb.id" 
                  class="feedback-item"
                >
                  <div class="feedback-title">
                    <span>{{ fb.title }}</span>
                    <el-tag size="small">{{ getFeedbackTypeText(fb.feedbackType) }}</el-tag>
                  </div>
                  <div class="feedback-time">{{ formatDateTime(fb.createTime) }}</div>
                </div>
              </div>
              <div v-else class="no-feedback">
                <p>暂无反馈记录</p>
              </div>
            </div>
          </div>
        </div>
      </template>
      
      <div v-else class="empty-state">
        <el-icon class="icon"><FolderOpened /></el-icon>
        <p>您还没有报名任何活动</p>
        <el-button type="primary" @click="router.push('/student/activities')">
          浏览活动
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Document, Plus, FolderOpened } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'
import { downloadProtectedFile } from '@/utils/file'

const router = useRouter()

const loading = ref(false)
const registrations = ref([])

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

const getFeedbackTypeText = (type) => {
  const map = {
    'SUMMARY': '工作总结',
    'PROGRESS': '进度汇报',
    'OTHER': '其他'
  }
  return map[type] || type
}

const getAttachments = (reg) => {
  if (!reg.attachments) return []
  try {
    return JSON.parse(reg.attachments)
  } catch {
    return []
  }
}

const loadRegistrations = async () => {
  loading.value = true
  try {
    const res = await api.registration.getMy()
    if (res.code === 200) {
      registrations.value = res.data || []
      
      // 加载每个报名的反馈
      for (const reg of registrations.value) {
        if (reg.approvalStatus === 'APPROVED') {
          try {
            const fbRes = await api.feedback.getByRegistration(reg.id)
            if (fbRes.code === 200) {
              reg.feedbacks = fbRes.data || []
            }
          } catch {
            reg.feedbacks = []
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载报名记录失败')
  } finally {
    loading.value = false
  }
}

const goToFeedback = (reg) => {
  router.push(`/student/feedback/${reg.id}`)
}

const viewActivityDetail = (reg) => {
  router.push(`/student/activities/${reg.activityId}`)
}

const cancelRegistration = async (reg) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.registration.cancel(reg.id)
    if (res.code === 200) {
      ElMessage.success('取消报名成功')
      loadRegistrations()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 取消操作
  }
}

const downloadFile = async (file) => {
  try {
    await downloadProtectedFile(file, localStorage.getItem('token'))
  } catch (error) {
    ElMessage.error(error.message || '附件下载失败')
  }
}

onMounted(() => {
  loadRegistrations()
})
</script>

<style lang="scss" scoped>
.my-registrations {
  max-width: 900px;
  margin: 0 auto;
}

.registration-card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .activity-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
      }
    }
  }
  
  .card-body {
    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
    }
    
    .info-item {
      .label {
        display: block;
        font-size: 13px;
        color: #9ca3af;
        margin-bottom: 4px;
      }
      
      .value {
        font-size: 14px;
        color: #374151;
      }
    }
  }
}

.attachments {
  .attachment-header {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 12px;
  }
  
  .attachment-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .attachment-tag {
    cursor: pointer;
    
    &:hover {
      color: #667eea;
    }
  }
}

.feedbacks {
  .feedback-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 12px;
  }
  
  .feedback-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .feedback-item {
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 8px;
    
    .feedback-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      
      span:first-child {
        font-weight: 500;
        color: #374151;
      }
    }
    
    .feedback-time {
      font-size: 12px;
      color: #9ca3af;
    }
  }
  
  .no-feedback {
    text-align: center;
    padding: 20px;
    color: #9ca3af;
  }
}

.status-tag {
  &.pending {
    background: #fef3c7;
    color: #d97706;
  }
  
  &.approved {
    background: #d1fae5;
    color: #059669;
  }
  
  &.rejected {
    background: #fee2e2;
    color: #dc2626;
  }
}

@media (max-width: 640px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    
    .card-actions {
      width: 100%;
      
      .el-button {
        flex: 1;
      }
    }
  }
  
  .info-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
