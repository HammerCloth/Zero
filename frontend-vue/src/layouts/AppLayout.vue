<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuOption } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const settings = useSettingsStore()
const collapsed = ref(false)
const mobileOpen = ref(false)
const mobile = ref(typeof window !== 'undefined' && window.innerWidth < 768)

function onResize() {
  mobile.value = window.innerWidth < 768
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', onResize)
  }
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', onResize)
  }
})

watch(
  () => auth.isLoggedIn,
  (v) => {
    if (v) {
      settings.load().catch(() => {})
    }
  },
  { immediate: true },
)

const menuOptions = computed<MenuOption[]>(() => {
  const base: MenuOption[] = [
    { label: '总览', key: '/dashboard' },
    { label: '快照', key: '/snapshots' },
    { label: '账户', key: '/accounts' },
    { label: '大事记', key: '/events' },
    { label: '设置', key: '/settings' },
  ]
  if (auth.isAdmin) {
    base.push({ label: '用户管理', key: '/users' })
  }
  return base
})

const activeKey = computed(() => {
  const p = route.path
  if (p.startsWith('/snapshots')) {
    return '/snapshots'
  }
  if (p.startsWith('/settings')) {
    return '/settings'
  }
  return p
})

const pageTitle = computed(() => {
  switch (activeKey.value) {
    case '/dashboard':
      return '资产总览'
    case '/snapshots':
      return '快照与时间线'
    case '/accounts':
      return '账户管理'
    case '/events':
      return '年度支出观察'
    case '/settings':
      return '系统设置'
    case '/users':
      return '用户与权限'
    default:
      return 'Project Zero'
  }
})

const pageEyeline = computed(() => (mobile.value ? 'Zero' : 'Project Zero Workspace'))
const userInitial = computed(() => (auth.user?.username?.slice(0, 1) || 'Z').toUpperCase())

function onMenuSelect(key: string) {
  router.push(key)
  mobileOpen.value = false
}

async function onLogout() {
  await auth.logout()
  await router.push('/login')
}
</script>

<template>
  <n-layout class="app-shell" has-sider position="absolute" style="height: 100vh">
    <n-layout-sider
      v-if="!mobile"
      bordered
      collapse-mode="width"
      :collapsed="collapsed"
      :collapsed-width="64"
      :width="220"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="app-sidebar">
        <div class="app-brand">
          <span class="app-brand__mark">Z</span>
          <div v-if="!collapsed" class="app-brand__text">
            <span class="app-brand__title">Project Zero</span>
            <span class="app-brand__subtitle">Personal finance cockpit</span>
          </div>
        </div>
        <n-menu
          :collapsed="collapsed"
          :collapsed-width="64"
          :value="activeKey"
          :options="menuOptions"
          @update:value="onMenuSelect"
        />
        <div v-if="!collapsed" class="app-sidebar__footer">
          统一查看净资产、账户结构和关键年度事件。
        </div>
      </div>
    </n-layout-sider>

    <n-layout class="app-main">
      <n-layout-header bordered class="app-header">
        <n-button v-if="mobile" class="mobile-only" secondary @click="mobileOpen = true">菜单</n-button>
        <div class="app-header__title">
          <span class="app-header__eyeline">{{ pageEyeline }}</span>
          <h1 class="app-header__name">{{ pageTitle }}</h1>
        </div>
        <div class="app-header__spacer" />
        <div class="app-user">
          <span class="app-user__avatar">{{ userInitial }}</span>
          <div class="app-user__meta">
            <span class="app-user__label">当前账号</span>
            <span class="app-user__name">{{ auth.user?.username || '未登录' }}</span>
          </div>
          <n-button size="small" quaternary @click="onLogout">退出</n-button>
        </div>
      </n-layout-header>
      <n-layout-content class="app-content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>

  <n-drawer v-model:show="mobileOpen" placement="left" width="220">
    <div class="app-sidebar">
      <div class="app-brand">
        <span class="app-brand__mark">Z</span>
        <div class="app-brand__text">
          <span class="app-brand__title">Project Zero</span>
          <span class="app-brand__subtitle">Mobile workspace</span>
        </div>
      </div>
      <n-menu
        :value="activeKey"
        :options="menuOptions"
        @update:value="onMenuSelect"
      />
    </div>
  </n-drawer>
</template>
