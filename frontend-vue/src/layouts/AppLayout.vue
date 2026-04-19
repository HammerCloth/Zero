<script setup lang="ts">
import { computed, ref, watch } from 'vue'
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

if (typeof window !== 'undefined') {
  window.addEventListener('resize', onResize)
}

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
  <n-layout has-sider position="absolute" style="height: 100vh">
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
      <div style="padding: 16px; font-weight: 600">Project Zero</div>
      <n-menu
        :value="activeKey"
        :options="menuOptions"
        @update:value="onMenuSelect"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered style="height: 56px; padding: 0 16px; display: flex; align-items: center; gap: 12px">
        <n-button v-if="mobile" quaternary @click="mobileOpen = true">菜单</n-button>
        <div style="flex: 1" />
        <span style="opacity: 0.8">{{ auth.user?.username }}</span>
        <n-button size="small" quaternary @click="onLogout">退出</n-button>
      </n-layout-header>
      <n-layout-content content-style="padding: 16px">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>

  <n-drawer v-model:show="mobileOpen" placement="left" width="220">
    <div style="padding: 16px; font-weight: 600">Project Zero</div>
    <n-menu
      :value="activeKey"
      :options="menuOptions"
      @update:value="onMenuSelect"
    />
  </n-drawer>
</template>
