<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>
    
    <el-row :gutter="24">
      <el-col :xs="24" :md="8">
        <!-- 头像卡片 -->
        <div class="avatar-card card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userStore.user?.avatar">
              {{ userStore.user?.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <h3>{{ userStore.user?.realName || '用户' }}</h3>
            <p>{{ userStore.user?.studentNo || '' }}</p>
            <el-tag>学生</el-tag>
          </div>
          <div class="info-section">
            <div class="info-item">
              <el-icon><School /></el-icon>
              <span>{{ userStore.user?.school || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ userStore.user?.department || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Notebook /></el-icon>
              <span>{{ userStore.user?.major || '未设置' }} · {{ userStore.user?.grade || '' }}</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="16">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="基本信息" name="info">
            <div class="card">
              <el-form
                ref="infoFormRef"
                :model="infoForm"
                :rules="infoRules"
                label-width="100px"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="真实姓名" prop="realName">
                      <el-input v-model="infoForm.realName" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="学号" prop="studentNo">
                      <el-input v-model="infoForm.studentNo" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="infoForm.email" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="phone">
                      <el-input v-model="infoForm.phone" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="所属学校" prop="school">
                      <el-input v-model="infoForm.school" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="院系" prop="department">
                      <el-input v-model="infoForm.department" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="专业" prop="major">
                      <el-input v-model="infoForm.major" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="年级" prop="grade">
                      <el-select v-model="infoForm.grade" style="width: 100%">
                        <el-option label="大一" value="大一" />
                        <el-option label="大二" value="大二" />
                        <el-option label="大三" value="大三" />
                        <el-option label="大四" value="大四" />
                        <el-option label="研一" value="研一" />
                        <el-option label="研二" value="研二" />
                        <el-option label="研三" value="研三" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item>
                  <el-button type="primary" :loading="savingInfo" @click="saveInfo">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="修改密码" name="password">
            <div class="card">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input 
                    v-model="pwdForm.oldPassword" 
                    type="password" 
                    show-password
                    placeholder="请输入原密码"
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="pwdForm.newPassword" 
                    type="password" 
                    show-password
                    placeholder="请输入新密码"
                  />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="pwdForm.confirmPassword" 
                    type="password" 
                    show-password
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" :loading="savingPwd" @click="savePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { School, OfficeBuilding, Notebook } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref('info')
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const savingInfo = ref(false)
const savingPwd = ref(false)

const infoForm = reactive({
  realName: '',
  studentNo: '',
  email: '',
  phone: '',
  school: '',
  department: '',
  major: '',
  grade: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const loadUserInfo = () => {
  const user = userStore.user
  if (user) {
    infoForm.realName = user.realName || ''
    infoForm.studentNo = user.studentNo || ''
    infoForm.email = user.email || ''
    infoForm.phone = user.phone || ''
    infoForm.school = user.school || ''
    infoForm.department = user.department || ''
    infoForm.major = user.major || ''
    infoForm.grade = user.grade || ''
  }
}

const saveInfo = async () => {
  if (!infoFormRef.value) return
  
  await infoFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    savingInfo.value = true
    try {
      await userStore.updateProfile(infoForm)
      ElMessage.success('保存成功')
    } catch (error) {
      ElMessage.error(error.message || '保存失败')
    } finally {
      savingInfo.value = false
    }
  })
}

const savePassword = async () => {
  if (!pwdFormRef.value) return
  
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    savingPwd.value = true
    try {
      await userStore.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
      pwdFormRef.value.resetFields()
    } catch (error) {
      ElMessage.error(error.message || '修改失败')
    } finally {
      savingPwd.value = false
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
}

.avatar-card {
  text-align: center;
  
  .avatar-section {
    padding-bottom: 20px;
    border-bottom: 1px solid #e5e7eb;
    margin-bottom: 20px;
    
    .el-avatar {
      margin-bottom: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    p {
      color: #6b7280;
      font-size: 14px;
      margin-bottom: 12px;
    }
  }
  
  .info-section {
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;
      color: #4b5563;
      font-size: 14px;
      
      .el-icon {
        color: #667eea;
      }
    }
  }
}

.profile-tabs {
  :deep(.el-tabs__content) {
    padding: 0;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
}

@media (max-width: 768px) {
  .avatar-card {
    margin-bottom: 20px;
  }
  
  .el-col {
    margin-bottom: 20px;
  }
}
</style>
