import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as authApi from '@/api/auth'
import type { User } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('access_token'))
  const user = ref<User | null>(null)
  const needsSetup = ref(false)
  const initialized = ref(false)
  /** 无法拉取 /auth/status 时展示，避免路由抛错导致整页白屏 */
  const bootError = ref<string | null>(null)

  const isLoggedIn = computed(() => !!accessToken.value)
  const mustChangePassword = computed(() => user.value?.must_change_password ?? false)
  const isAdmin = computed(() => user.value?.is_admin ?? false)

  function setSession(token: string | null, u?: User | null) {
    accessToken.value = token
    if (token) {
      localStorage.setItem('access_token', token)
    } else {
      localStorage.removeItem('access_token')
    }
    if (u !== undefined) {
      user.value = u
    }
  }

  async function init() {
    bootError.value = null
    try {
      const s = await authApi.getStatus()
      needsSetup.value = s.needs_setup
      if (!s.needs_setup && accessToken.value) {
        try {
          user.value = await authApi.me()
        } catch {
          setSession(null)
          user.value = null
        }
      }
    } catch {
      bootError.value =
        '请求 /api/v1/auth/status 失败（网络错误、后端未启动或跨域被拦截）。请确认后端已监听 8080，且 Vite 代理 /api 可用。'
      needsSetup.value = false
      user.value = null
    } finally {
      initialized.value = true
    }
  }

  /** 从 BootError 页重试：重置状态后再次探测后端 */
  async function retryBootstrap() {
    initialized.value = false
    bootError.value = null
    await init()
  }

  async function refreshUser() {
    if (!accessToken.value) {
      return
    }
    user.value = await authApi.me()
  }

  async function login(username: string, password: string, remember: boolean) {
    const data = await authApi.login(username, password, remember)
    setSession(data.access_token, data.user)
    needsSetup.value = false
    bootError.value = null
  }

  async function setup(username: string, password: string) {
    const data = await authApi.setup(username, password)
    setSession(data.access_token, data.user)
    needsSetup.value = false
    bootError.value = null
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      setSession(null)
      user.value = null
    }
  }

  return {
    accessToken,
    user,
    needsSetup,
    initialized,
    bootError,
    isLoggedIn,
    mustChangePassword,
    isAdmin,
    setSession,
    init,
    retryBootstrap,
    refreshUser,
    login,
    setup,
    logout,
  }
})
