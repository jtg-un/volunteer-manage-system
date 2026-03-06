import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/dashboard/home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'activity/list',
        name: 'ActivityList',
        component: () => import('@/views/activity/list.vue'),
        meta: { title: '活动列表' }
      },
      {
        path: 'org/profile',
        name: 'OrgProfile',
        component: () => import('@/views/org/profile.vue'),
        meta: { title: '组织信息', role: 1 }
      },
      {
        path: 'org/publish-activity',
        name: 'PublishActivity',
        component: () => import('@/views/org/publishActivity.vue'),
        meta: { title: '发布活动', role: 1 }
      },
      {
        path: 'org/my-activity',
        name: 'MyActivity',
        component: () => import('@/views/org/myActivity.vue'),
        meta: { title: '我的活动', role: 1 }
      },
      {
        path: 'admin/org-audit',
        name: 'OrgAudit',
        component: () => import('@/views/admin/orgAudit.vue'),
        meta: { title: '组织审核', role: 2 }
      },
      {
        path: 'admin/activity-audit',
        name: 'ActivityAudit',
        component: () => import('@/views/admin/activityAudit.vue'),
        meta: { title: '活动审核', role: 2 }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'VMS'} - 志愿者管理系统`

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  // 角色权限检查
  if (to.meta.role !== undefined) {
    const userRole = userStore.userInfo?.role
    if (userRole !== to.meta.role) {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router