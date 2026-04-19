import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/boot-error',
      name: 'boot-error',
      component: () => import('@/views/BootErrorPage.vue'),
      meta: { layout: 'blank', skipAuth: true },
    },
    {
      path: '/setup',
      name: 'setup',
      component: () => import('@/views/SetupPage.vue'),
      meta: { layout: 'blank' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginPage.vue'),
      meta: { layout: 'blank', guest: true },
    },
    {
      path: '/change-password',
      name: 'change-password',
      component: () => import('@/views/ChangePasswordPage.vue'),
      meta: { layout: 'blank' },
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      children: [
        { path: '', redirect: { name: 'dashboard' } },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardPage.vue'),
        },
        {
          path: 'snapshots',
          name: 'snapshots',
          component: () => import('@/views/SnapshotsPage.vue'),
        },
        {
          path: 'snapshots/calendar',
          name: 'snapshot-calendar',
          component: () => import('@/views/SnapshotCalendarPage.vue'),
        },
        {
          path: 'snapshots/new',
          name: 'snapshot-new',
          component: () => import('@/views/SnapshotFormPage.vue'),
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/SettingsPage.vue'),
        },
        {
          path: 'snapshots/:id',
          name: 'snapshot-detail',
          component: () => import('@/views/SnapshotDetailPage.vue'),
          props: true,
        },
        {
          path: 'snapshots/:id/edit',
          name: 'snapshot-edit',
          component: () => import('@/views/SnapshotFormPage.vue'),
          props: true,
        },
        {
          path: 'accounts',
          name: 'accounts',
          component: () => import('@/views/AccountsPage.vue'),
        },
        {
          path: 'events',
          name: 'events',
          component: () => import('@/views/EventStatsPage.vue'),
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/UsersPage.vue'),
          meta: { admin: true },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  try {
    if (!auth.initialized) {
      await auth.init()
    }

    if (auth.bootError) {
      if (to.name === 'boot-error') {
        return true
      }
      return { name: 'boot-error' }
    }

    if (auth.needsSetup) {
      if (to.path === '/setup') {
        return true
      }
      if (to.path === '/login') {
        return { path: '/setup' }
      }
      return { path: '/setup' }
    }

    if (to.path === '/setup') {
      return { path: '/login' }
    }

    if (to.meta.guest) {
      if (auth.isLoggedIn) {
        return { path: '/dashboard' }
      }
      return true
    }

    if (to.path === '/login') {
      return true
    }

    if (!auth.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    if (!auth.user) {
      await auth.refreshUser()
    }

    if (auth.mustChangePassword && to.path !== '/change-password') {
      return { path: '/change-password' }
    }

    if (to.path === '/change-password' && !auth.mustChangePassword) {
      return { path: '/dashboard' }
    }

    if (to.meta.admin && !auth.isAdmin) {
      return { path: '/dashboard' }
    }

    return true
  } catch (e) {
    console.error('router guard', e)
    auth.bootError =
      (e as Error)?.message ?? '路由初始化失败'
    if (to.name === 'boot-error') {
      return true
    }
    return { name: 'boot-error' }
  }
})

router.onError((err) => {
  console.error('router error', err)
})

export default router
