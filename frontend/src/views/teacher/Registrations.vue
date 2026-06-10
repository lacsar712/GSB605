<template>
  <div class="registrations-page">
    <el-button class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>返回
    </el-button>
    
    <div class="page-header">
      <h1>报名管理</h1>
      <p>审批活动报名申请</p>
    </div>
    
    <div class="filter-card card">
      <el-radio-group v-model="filterStatus" @change="loadRegistrations">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待审批</el-radio-button>
        <el-radio-button value="APPROVED">已通过</el-radio-button>
        <el-radio-button value="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="card" v-loading="loading">
      <el-table :data="registrations" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="targetSchool" label="招生学校" min-width="150" />
        <el-table-column prop="contactInfo" label="联系方式" width="150" />
        <el-table-column prop="approvalStatus" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', row.approvalStatus?.toLowerCase()]">
              {{ getStatusText(row.approvalStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.approvalStatus === 'PENDING'">
              <el-button type="success" text size="small" @click="approve(row, 'APPROVED')">
                通过
              </el-button>
              <el-button type="danger" text size="small" @click="approve(row, 'REJECTED')">
                拒绝
              </el-button>
            </template>
            <el-button type="primary"  style="color:#fff;" text size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetail" title="报名详情" width="500px">
      <div class="detail-content" v-if="currentReg">
        <div class="info-item">
          <span class="label">招生学校：</span>
          <span>{{ currentReg.targetSchool || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系方式：</span>
          <span>{{ currentReg.contactInfo || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">备注：</span>
          <span>{{ currentReg.remark || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">报名时间：</span>
          <span>{{ formatDateTime(currentReg.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const registrations = ref([])
const filterStatus = ref('')
const showDetail = ref(false)
const currentReg = ref(null)

const activityId = route.params.id

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const loadRegistrations = async () => {
  loading.value = true
  try {
    const res = await api.registration.getByActivity(activityId)
    if (res.code === 200) {
      let data = res.data || []
      if (filterStatus.value) {
        data = data.filter(r => r.approvalStatus === filterStatus.value)
      }
      registrations.value = data
    }
  } catch (error) {
    ElMessage.error('加载报名列表失败')
  } finally {
    loading.value = false
  }
}

const approve = async (row, status) => {
  const action = status === 'APPROVED' ? '通过' : '拒绝'
  try {
    const { value: comment } = await ElMessageBox.prompt(`请输入${action}意见（可选）`, '审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '审批意见'
    })
    
    const res = await api.registration.approve(row.id, { status, comment: comment || '' })
    if (res.code === 200) {
      ElMessage.success('审批完成')
      loadRegistrations()
    }
  } catch {}
}

const viewDetail = (row) => {
  currentReg.value = row
  showDetail.value = true
}

onMounted(() => {
  loadRegistrations()
})
</script>

<style lang="scss" scoped>
.registrations-page {
  max-width: 1000px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.detail-content {
  .info-item {
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    .label {
      color: #6b7280;
      margin-right: 8px;
    }
  }
}
</style>
