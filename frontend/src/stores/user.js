import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  
  async function login(username, password) {
    const res = await api.auth.login({ username, password })
    if (res.code === 200) {
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return res.data
    }
    throw new Error(res.message)
  }

  async function ssoLogin(identity, password) {
    const res = await api.auth.ssoLogin({ identity, password })
    if (res.code === 200) {
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return res.data
    }
    throw new Error(res.message)
  }
  
  async function register(userData) {
    const res = await api.auth.register(userData)
    if (res.code === 200) {
      return res.data
    }
    throw new Error(res.message)
  }
  
  async function fetchUserInfo() {
    const res = await api.user.getCurrentUser()
    if (res.code === 200) {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    }
    throw new Error(res.message)
  }
  
  async function updateProfile(data) {
    const res = await api.user.updateCurrentUser(data)
    if (res.code === 200) {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    }
    throw new Error(res.message)
  }
  
  async function changePassword(oldPassword, newPassword) {
    const res = await api.auth.changePassword({ oldPassword, newPassword })
    if (res.code === 200) {
      return true
    }
    throw new Error(res.message)
  }
  
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  return {
    token,
    user,
    isLoggedIn,
    login,
    ssoLogin,
    register,
    fetchUserInfo,
    updateProfile,
    changePassword,
    logout
  }
})
