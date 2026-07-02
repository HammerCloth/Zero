<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import * as authApi from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const current = ref('')
const next = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  try {
    await authApi.changePassword(current.value, next.value)
    message.success('已更新密码')
    await auth.refreshUser()
    await router.replace('/dashboard')
  } catch {
    message.error('当前密码不正确或请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="screen-shell">
    <div class="screen-card">
      <div class="page-stack">
        <div>
          <span class="login-hero__badge">安全设置</span>
          <h1 class="page-header__title" style="margin-top: 18px">修改密码</h1>
          <p class="page-header__desc" style="margin-top: 8px">
            首次登录后需要先更新密码，完成后会自动返回总览页。
          </p>
        </div>
        <n-alert type="info">登录后需修改密码方可继续使用。</n-alert>
        <n-form @submit.prevent="submit">
          <n-form-item label="当前密码">
            <n-input v-model:value="current" type="password" show-password-on="click" />
          </n-form-item>
          <n-form-item label="新密码（至少 8 位）">
            <n-input v-model:value="next" type="password" show-password-on="click" />
          </n-form-item>
          <n-button type="primary" block :loading="loading" attr-type="submit">保存新密码</n-button>
        </n-form>
      </div>
    </div>
  </div>
</template>
