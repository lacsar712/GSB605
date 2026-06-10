<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48"><School /></el-icon>
        </div>
        <h1>假期学生管理系统</h1>
        <p>统一身份认证</p>
      </div>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="支持用户名、学号/工号或邮箱登录"
        class="login-tip"
      />

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="identity">
          <el-input
            v-model="form.identity"
            placeholder="请输入统一身份标识"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '认证中...' : '统一身份认证登录' }}
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <el-link type="primary" @click="router.push('/register')">
            注册账号
          </el-link>
          <el-link type="info" @click="router.push('/forgot-password')">
            忘记密码？
          </el-link>
        </div>
      </el-form>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  identity: '',
  password: ''
})

const rules = {
  identity: [
    { required: true, message: '请输入统一身份标识', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const demoAccounts = [
  { identity: 'student', password: '123456', label: '学生', type: 'success' },
  { identity: 'teacher', password: '123456', label: '教师', type: 'warning' },
  { identity: 'admin', password: '123456', label: '管理员', type: 'danger' }
]

const fillAccount = (account) => {
  form.identity = account.identity
  form.password = account.password
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const data = await userStore.ssoLogin(form.identity, form.password)
      ElMessage.success('统一身份认证成功')

      const roleRoutes = {
        STUDENT: '/student',
        TEACHER: '/teacher',
        SCHOOL: '/school'
      }
      router.push(roleRoutes[data.user.role] || '/student')
    } catch (error) {
      ElMessage.error(error.message || '认证失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo {
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }
  
  p {
    color: #6b7280;
    font-size: 14px;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 12px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.login-tip {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  }
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.demo-accounts {
  margin-top: 24px;
  
  :deep(.el-divider__text) {
    font-size: 12px;
    color: #9ca3af;
  }
  
  .account-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
  }
  
  .account-tag {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 20px;
  }
}
</style>
