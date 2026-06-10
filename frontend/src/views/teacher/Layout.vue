<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/teacher')">
          <el-icon :size="28"><School /></el-icon>
          <span>假期学生管理系统</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          class="nav-menu"
          router
        >
          <el-menu-item index="/teacher/activities">
            <el-icon><List /></el-icon>
            <span>活动管理</span>
          </el-menu-item>
        </el-menu>
        
        <div class="user-info">
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-avatar :size="36">
                {{ userStore.user?.realName?.charAt(0) || 'T' }}
              </el-avatar>
              <span class="user-name">{{ userStore.user?.realName || '教师' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/teacher/profile')">
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
    
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <el-footer class="footer">
      <p>© 2024 假期学生管理系统 - 教师端</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { School, List, User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {}
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
}

.nav-menu {
  flex: 1;
  border: none;
}

.user-info {
  .user-avatar {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 8px;
    
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
}
</style>
