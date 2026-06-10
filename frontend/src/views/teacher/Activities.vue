<template>
  <div class="teacher-activities">
    <div class="page-header">
      <div>
        <h1>活动管理</h1>
        <p>管理您创建的活动</p>
      </div>
      <el-button type="primary" @click="router.push('/teacher/activities/create')">
        <el-icon><Plus /></el-icon>创建活动
      </el-button>
    </div>
    
    <div class="card" v-loading="loading">
      <el-table :data="activities" style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="200">
          <template #default="{ row }">
            <div class="activity-title">
              <span>{{ row.title }}</span>
              <el-tag v-if="row.isBanner" size="small" type="warning">轮播</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="activityType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.activityType === 'ONLINE' ? 'primary' : 'success'" size="small">
              {{ row.activityType === 'ONLINE' ? '线上' : '线下' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentParticipants" label="报名人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }} / {{ row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signupEndTime" label="报名截止" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.signupEndTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" style="color:#fff;" text size="small" @click="viewRegistrations(row)">
              报名管理
            </el-button>
            <el-button type="primary" style="color:#fff;" text size="small" @click="editActivity(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              text 
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button type="danger" text size="small" @click="deleteActivity(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const activities = ref([])

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '发布中', 2: '已结束' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await api.activity.getList()
    if (res.code === 200) {
      activities.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const viewRegistrations = (row) => {
  router.push(`/teacher/activities/${row.id}/registrations`)
}

const editActivity = (row) => {
  router.push(`/teacher/activities/create?id=${row.id}`)
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await api.activity.updateStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已发布' : '已下架')
      loadActivities()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteActivity = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '提示', { type: 'warning' })
    const res = await api.activity.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadActivities()
    }
  } catch {}
}

onMounted(() => {
  loadActivities()
})
</script>

<style lang="scss" scoped>
.teacher-activities {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .activity-title {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
