<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/student')">
          <el-icon :size="28"><School /></el-icon>
          <span>假期学生管理系统</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          class="nav-menu"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/student/activities">
            <el-icon><List /></el-icon>
            <span>活动列表</span>
          </el-menu-item>
          <el-menu-item index="/student/my-registrations">
            <el-icon><Document /></el-icon>
            <span>已报名活动</span>
          </el-menu-item>
        </el-menu>
        
        <div class="user-info">
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-avatar :size="36" :src="userStore.user?.avatar">
                {{ userStore.user?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.user?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/student/profile')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    
    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2024 假期学生管理系统 - 学生端</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { School, List, Document, User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 取消退出
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 24px;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #667eea;
  font-size: 18px;
  font-weight: 600;
  margin-right: 48px;
  
  &:hover {
    opacity: 0.9;
  }
}

.nav-menu {
  flex: 1;
  border: none;
  
  :deep(.el-menu-item) {
    height: 64px;
    line-height: 64px;
    font-size: 15px;
    
    &.is-active {
      color: #667eea;
      border-bottom-color: #667eea;
    }
  }
}

.user-info {
  .user-avatar {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
      background: #f5f7fa;
    }
  }
  
  .user-name {
    font-size: 14px;
    color: #374151;
  }
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 24px;
}

.footer {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  background: transparent;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }
  
  .logo span {
    display: none;
  }
  
  .nav-menu {
    :deep(.el-menu-item span) {
      display: none;
    }
  }
  
  .user-name {
    display: none;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>
