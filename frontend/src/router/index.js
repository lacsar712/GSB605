import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/common/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/common/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/common/ForgotPassword.vue'),
    meta: { requiresAuth: false }
  },
  // 学生端路由
  {
    path: '/student',
    component: () => import('@/views/student/Layout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        redirect: '/student/activities'
      },
      {
        path: 'activities',
        name: 'StudentActivities',
        component: () => import('@/views/student/Activities.vue')
      },
      {
        path: 'activities/:id',
        name: 'StudentActivityDetail',
        component: () => import('@/views/student/ActivityDetail.vue')
      },
      {
        path: 'my-registrations',
        name: 'StudentRegistrations',
        component: () => import('@/views/student/MyRegistrations.vue')
      },
      {
        path: 'feedback/:registrationId',
        name: 'StudentFeedback',
        component: () => import('@/views/student/Feedback.vue')
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue')
      }
    ]
  },
  // 教师端路由
  {
    path: '/teacher',
    component: () => import('@/views/teacher/Layout.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: '',
        redirect: '/teacher/activities'
      },
      {
        path: 'activities',
        name: 'TeacherActivities',
        component: () => import('@/views/teacher/Activities.vue')
      },
      {
        path: 'activities/create',
        name: 'TeacherCreateActivity',
        component: () => import('@/views/teacher/CreateActivity.vue')
      },
      {
        path: 'activities/:id/registrations',
        name: 'TeacherRegistrations',
        component: () => import('@/views/teacher/Registrations.vue')
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/teacher/Profile.vue')
      }
    ]
  },
  // 学校端路由
  {
    path: '/school',
    component: () => import('@/views/school/Layout.vue'),
    meta: { requiresAuth: true, role: 'SCHOOL' },
    children: [
      {
        path: '',
        redirect: '/school/dashboard'
      },
      {
        path: 'dashboard',
        name: 'SchoolDashboard',
        component: () => import('@/views/school/Dashboard.vue')
      },
      {
        path: 'activities',
        name: 'SchoolActivities',
        component: () => import('@/views/school/Activities.vue')
      },
      {
        path: 'users',
        name: 'SchoolUsers',
        component: () => import('@/views/school/Users.vue')
      },
      {
        path: 'profile',
        name: 'SchoolProfile',
        component: () => import('@/views/school/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.role && userStore.user?.role !== to.meta.role) {
      // 重定向到对应角色的首页
      const roleRoutes = {
        'STUDENT': '/student',
        'TEACHER': '/teacher',
        'SCHOOL': '/school'
      }
      next(roleRoutes[userStore.user?.role] || '/login')
      return
    }
  }
  
  next()
})

export default router
