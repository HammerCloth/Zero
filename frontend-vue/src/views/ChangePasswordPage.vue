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
  <div style="max-width: 400px; margin: 80px auto">
    <n-h2>修改密码</n-h2>
    <n-alert type="info" style="margin-bottom: 16px">登录后需修改密码方可继续使用。</n-alert>
    <n-form @submit.prevent="submit">
      <n-form-item label="当前密码">
        <n-input v-model:value="current" type="password" show-password-on="click" />
      </n-form-item>
      <n-form-item label="新密码（至少 8 位）">
        <n-input v-model:value="next" type="password" show-password-on="click" />
      </n-form-item>
      <n-button type="primary" block :loading="loading" attr-type="submit">保存</n-button>
    </n-form>
  </div>
</template>
