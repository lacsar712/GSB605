<template>
  <div class="activities-page">
    <!-- 轮播图 -->
    <div class="banner-section" v-if="banners.length > 0">
      <el-carousel height="280px" :interval="5000" class="banner-carousel">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" @click="viewDetail(banner.id)">
            <img v-if="banner.bannerImage" :src="banner.bannerImage" :alt="banner.title" />
            <div v-else class="banner-placeholder">
              <el-icon :size="64"><Picture /></el-icon>
            </div>
            <div class="banner-content">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.summary }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 筛选区域 -->
    <div class="filter-section card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="活动类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="线上活动" value="ONLINE" />
            <el-option label="线下活动" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索活动名称" 
            clearable 
            style="width: 200px"
            @keyup.enter="loadActivities"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadActivities">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 活动分类标签 -->
    <div class="type-tabs">
      <el-radio-group v-model="activeType" @change="handleTypeChange">
        <el-radio-button value="">全部活动</el-radio-button>
        <el-radio-button value="ONLINE">线上活动</el-radio-button>
        <el-radio-button value="OFFLINE">线下活动</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 活动列表 -->
    <div class="activities-list" v-loading="loading">
      <template v-if="activities.length > 0">
        <el-row :gutter="20">
          <el-col 
            v-for="activity in activities" 
            :key="activity.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <div class="activity-card" @click="viewDetail(activity.id)">
              <div class="cover">
                <img v-if="activity.coverImage" :src="activity.coverImage" :alt="activity.title" />
                <el-icon v-else class="placeholder"><Calendar /></el-icon>
                <span :class="['type-tag', activity.activityType?.toLowerCase()]">
                  {{ activity.activityType === 'ONLINE' ? '线上' : '线下' }}
                </span>
              </div>
              <div class="content">
                <h3>{{ activity.title }}</h3>
                <p>{{ activity.summary }}</p>
                <div class="meta">
                  <span><el-icon><Clock /></el-icon>{{ formatDate(activity.signupEndTime) }}截止</span>
                  <span><el-icon><User /></el-icon>{{ activity.currentParticipants || 0 }}人报名</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
      
      <div v-else class="empty-state">
        <el-icon class="icon"><FolderOpened /></el-icon>
        <p>暂无符合条件的活动</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Search, Calendar, Clock, User, FolderOpened } from '@element-plus/icons-vue'
import api from '@/api'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const activities = ref([])
const banners = ref([])
const activeType = ref('')

const filters = reactive({
  type: '',
  keyword: ''
})

const formatDate = (date) => {
  if (!date) return '未设置'
  return dayjs(date).format('MM月DD日')
}

const loadBanners = async () => {
  try {
    const res = await api.activity.getBanners()
    if (res.code === 200) {
      banners.value = res.data || []
    }
  } catch (error) {
    console.error('加载轮播图失败', error)
  }
}

const loadActivities = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.type) params.type = filters.type
    if (filters.keyword) params.keyword = filters.keyword
    
    const res = await api.activity.getList(params)
    if (res.code === 200) {
      activities.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = (type) => {
  filters.type = type
  loadActivities()
}

const resetFilters = () => {
  filters.type = ''
  filters.keyword = ''
  activeType.value = ''
  loadActivities()
}

const viewDetail = (id) => {
  router.push(`/student/activities/${id}`)
}

onMounted(() => {
  loadBanners()
  loadActivities()
})
</script>

<style lang="scss" scoped>
.activities-page {
  max-width: 1200px;
  margin: 0 auto;
}

.banner-section {
  margin-bottom: 24px;
  
  .banner-carousel {
    border-radius: 16px;
    overflow: hidden;
  }
  
  .banner-item {
    position: relative;
    height: 100%;
    cursor: pointer;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .banner-placeholder {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.5);
    }
    
    .banner-content {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 24px;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
      color: #fff;
      
      h2 {
        font-size: 24px;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}

.filter-section {
  margin-bottom: 20px;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.type-tabs {
  margin-bottom: 20px;
  
  :deep(.el-radio-button__inner) {
    border-radius: 20px;
  }
}

.activities-list {
  min-height: 300px;
}

.activity-card {
  .cover {
    position: relative;
    
    .type-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;
      
      &.online {
        background: rgba(37, 99, 235, 0.9);
        color: #fff;
      }
      
      &.offline {
        background: rgba(124, 58, 237, 0.9);
        color: #fff;
      }
    }
  }
}

@media (max-width: 768px) {
  .banner-section {
    :deep(.el-carousel) {
      height: 180px !important;
    }
    
    .banner-content h2 {
      font-size: 18px;
    }
  }
  
  .filter-form {
    :deep(.el-form-item) {
      width: 100%;
      
      .el-input, .el-select {
        width: 100% !important;
      }
    }
  }
}
</style>
