<template>
  <div class="users-page">
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理系统用户</p>
    </div>
    
    <div class="filter-card card">
      <el-radio-group v-model="filterRole" @change="loadUsers">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="STUDENT">学生</el-radio-button>
        <el-radio-button value="TEACHER">教师</el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="card" v-loading="loading">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号/工号" width="120" />
        <el-table-column prop="school" label="学校" min-width="150" />
        <el-table-column prop="department" label="院系" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              text 
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" text size="small" @click="deleteUser(row)">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const users = ref([])
const filterRole = ref('')

const getRoleText = (role) => {
  const map = { STUDENT: '学生', TEACHER: '教师', SCHOOL: '管理员' }
  return map[role] || role
}

const getRoleType = (role) => {
  const map = { STUDENT: 'success', TEACHER: 'warning', SCHOOL: 'danger' }
  return map[role] || 'info'
}

const loadUsers = async () => {
  loading.value = true
  try {
    let res
    if (filterRole.value) {
      res = await api.user.getByRole(filterRole.value)
    } else {
      res = await api.user.getAll()
    }
    if (res.code === 200) {
      users.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await api.user.updateStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', { type: 'warning' })
    const res = await api.user.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch {}
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.users-page {
  .filter-card {
    margin-bottom: 20px;
  }
}
</style>
