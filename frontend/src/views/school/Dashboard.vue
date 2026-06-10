<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>数据概览</h1>
      <p>查看系统运营数据</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <div class="stat-icon"><el-icon :size="32"><User /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalStudents }}</span>
            <span class="stat-label">学生总数</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <div class="stat-icon"><el-icon :size="32"><UserFilled /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalTeachers }}</span>
            <span class="stat-label">教师总数</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <div class="stat-icon"><el-icon :size="32"><Calendar /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalActivities }}</span>
            <span class="stat-label">活动总数</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
          <div class="stat-icon"><el-icon :size="32"><Document /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalRegistrations }}</span>
            <span class="stat-label">报名总数</span>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <div class="card">
          <h3>最新活动</h3>
          <el-table :data="recentActivities" style="width: 100%">
            <el-table-column prop="title" label="活动名称" />
            <el-table-column prop="currentParticipants" label="报名人数" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '进行中' : '已结束' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <div class="card">
          <h3>待审批报名</h3>
          <el-table :data="pendingRegistrations" style="width: 100%">
            <el-table-column prop="activityTitle" label="活动" />
            <el-table-column prop="userName" label="申请人" width="100" />
            <el-table-column prop="createTime" label="时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="pendingRegistrations.length === 0" class="empty-text">
            暂无待审批报名
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, UserFilled, Calendar, Document } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'

const stats = reactive({
  totalStudents: 0,
  totalTeachers: 0,
  totalActivities: 0,
  totalRegistrations: 0
})

const recentActivities = ref([])
const pendingRegistrations = ref([])

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('MM-DD HH:mm')
}

const loadDashboardData = async () => {
  try {
    // 加载用户统计
    const usersRes = await api.user.getAll()
    if (usersRes.code === 200) {
      const users = usersRes.data || []
      stats.totalStudents = users.filter(u => u.role === 'STUDENT').length
      stats.totalTeachers = users.filter(u => u.role === 'TEACHER').length
    }
    
    // 加载活动
    const activitiesRes = await api.activity.getList()
    if (activitiesRes.code === 200) {
      const activities = activitiesRes.data || []
      stats.totalActivities = activities.length
      recentActivities.value = activities.slice(0, 5)
      
      // 统计报名
      let totalRegs = 0
      for (const activity of activities) {
        totalRegs += activity.currentParticipants || 0
      }
      stats.totalRegistrations = totalRegs
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-cards {
    margin-bottom: 24px;
  }
  
  .stat-card {
    border-radius: 16px;
    padding: 24px;
    color: #fff;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    
    .stat-icon {
      width: 60px;
      height: 60px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .stat-info {
      .stat-value {
        display: block;
        font-size: 32px;
        font-weight: 700;
        line-height: 1.2;
      }
      
      .stat-label {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
  
  .card {
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 16px;
    }
  }
  
  .empty-text {
    text-align: center;
    color: #9ca3af;
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 12px;
    
    .stat-info .stat-value {
      font-size: 24px;
    }
  }
}
</style>
