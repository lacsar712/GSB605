import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

// API模块
const api = {
  // 认证相关
  auth: {
    login: data => request.post('/auth/login', data),
    ssoLogin: data => request.post('/auth/sso-login', data),
    register: data => request.post('/auth/register', data),
    changePassword: data => request.post('/auth/change-password', data),
    resetPassword: data => request.post('/auth/reset-password', data),
    forgotPassword: data => request.post('/auth/forgot-password', data)
  },
  
  // 用户相关
  user: {
    getCurrentUser: () => request.get('/users/me'),
    updateCurrentUser: data => request.put('/users/me', data),
    getAll: () => request.get('/users'),
    getByRole: role => request.get(`/users/role/${role}`),
    updateStatus: (id, status) => request.put(`/users/${id}/status?status=${status}`),
    delete: id => request.delete(`/users/${id}`)
  },
  
  // 活动相关
  activity: {
    getList: (params) => request.get('/activities', { params }),
    getPublished: (params) => request.get('/activities/published', { params }),
    getBanners: () => request.get('/activities/banners'),
    getById: id => request.get(`/activities/${id}`),
    create: data => request.post('/activities', data),
    update: (id, data) => request.put(`/activities/${id}`, data),
    updateStatus: (id, status) => request.put(`/activities/${id}/status?status=${status}`),
    delete: id => request.delete(`/activities/${id}`)
  },
  
  // 报名相关
  registration: {
    getMy: () => request.get('/registrations/my'),
    getById: id => request.get(`/registrations/${id}`),
    getByActivity: activityId => request.get(`/registrations/activity/${activityId}`),
    getByGroup: groupId => request.get(`/registrations/group/${groupId}`),
    check: activityId => request.get(`/registrations/check/${activityId}`),
    register: data => request.post('/registrations', data),
    approve: (id, data) => request.put(`/registrations/${id}/approve`, data),
    cancel: id => request.delete(`/registrations/${id}`)
  },
  
  // 反馈相关
  feedback: {
    getMy: () => request.get('/feedbacks/my'),
    getById: id => request.get(`/feedbacks/${id}`),
    getByActivity: activityId => request.get(`/feedbacks/activity/${activityId}`),
    getByRegistration: registrationId => request.get(`/feedbacks/registration/${registrationId}`),
    create: data => request.post('/feedbacks', data),
    update: (id, data) => request.put(`/feedbacks/${id}`, data),
    delete: id => request.delete(`/feedbacks/${id}`)
  },
  
  // 文件相关
  file: {
    upload: file => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    download: storedName => request.get(`/files/download/${storedName}`, { responseType: 'blob' }),
    delete: filename => request.delete(`/files/${filename}`)
  },
  
  // 学校名称建议
  school: {
    suggest: keyword => request.get('/schools/suggest', { params: { keyword } })
  }
}

export default api
