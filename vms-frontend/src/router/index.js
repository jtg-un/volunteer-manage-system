import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/welcome',
    name: 'Welcome',
    component: () => import('@/views/welcome.vue'),
    meta: { title: '欢迎', guest: true }
  },
  {
    path: '/activity/list',
    name: 'ActivityList',
    component: () => import('@/views/activity/list.vue'),
    meta: { title: '活动列表', guest: true }
  },
  {
    path: '/activity/detail/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/activity/detail.vue'),
    meta: { title: '活动详情', guest: true }
  },
  {
    path: '/notice/list',
    name: 'NoticeList',
    component: () => import('@/views/notice/list.vue'),
    meta: { title: '公告列表', guest: true }
  },
  {
    path: '/notice/detail/:id',
    name: 'NoticeDetail',
    component: () => import('@/views/notice/detail.vue'),
    meta: { title: '公告详情', guest: true }
  },
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
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人中心' }
      },
      // 志愿者端路由
      {
        path: 'volunteer/my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/volunteer/myRegistrations.vue'),
        meta: { title: '我的报名', role: 0 }
      },
      {
        path: 'volunteer/records',
        name: 'VolunteerRecords',
        component: () => import('@/views/volunteer/records.vue'),
        meta: { title: '时长记录', role: 0 }
      },
      {
        path: 'volunteer/my-evaluations',
        name: 'MyEvaluations',
        component: () => import('@/views/volunteer/myEvaluations.vue'),
        meta: { title: '我的评价', role: 0 }
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
        path: 'org/registrations',
        name: 'OrgRegistrations',
        component: () => import('@/views/org/registrations.vue'),
        meta: { title: '报名管理', role: 1 }
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
      },
      {
        path: 'admin/user-manage',
        name: 'UserManage',
        component: () => import('@/views/admin/userManage.vue'),
        meta: { title: '用户管理', role: 2 }
      },
      {
        path: 'admin/dict-manage',
        name: 'DictManage',
        component: () => import('@/views/admin/dictManage.vue'),
        meta: { title: '字典管理', role: 2 }
      },
      {
        path: 'admin/region-manage',
        name: 'RegionManage',
        component: () => import('@/views/admin/regionManage.vue'),
        meta: { title: '区划管理', role: 2 }
      },
      {
        path: 'admin/notice-manage',
        name: 'NoticeManage',
        component: () => import('@/views/admin/noticeManage.vue'),
        meta: { title: '公告管理', role: 2 }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  document.title = `${to.meta.title || 'VMS'} - 志愿服务管理系统`

  const userStore = useUserStore()

  // 有 token 但 userInfo 为空时，先获取用户信息
  if (userStore.isLoggedIn && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      // 获取用户信息失败，清除 token 并跳转登录页
      userStore.logout()
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 游客可访问的页面
  if (to.meta.guest) {
    next()
    return
  }

  // 欢迎页任何人可访问
  if (to.name === 'Welcome') {
    next()
    return
  }

  // 未登录用户访问需要认证的页面，跳转欢迎页
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Welcome' })
    return
  }

  // 已登录用户访问登录/注册页，跳转首页
  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  // 已登录用户访问欢迎页，跳转首页
  if (to.name === 'Welcome' && userStore.isLoggedIn) {
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