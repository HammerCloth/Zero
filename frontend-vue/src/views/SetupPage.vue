<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  try {
    await auth.setup(username.value.trim(), password.value)
    message.success('初始化完成')
    await router.replace('/dashboard')
  } catch (e: unknown) {
    message.error((e as { response?: { data?: { error?: string } } })?.response?.data?.error ?? '失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 400px; margin: 80px auto">
    <n-h2>首次设置管理员</n-h2>
    <n-form @submit.prevent="submit">
      <n-form-item label="用户名">
        <n-input v-model:value="username" placeholder="管理员用户名" />
      </n-form-item>
      <n-form-item label="密码（至少 8 位）">
        <n-input v-model:value="password" type="password" show-password-on="click" />
      </n-form-item>
      <n-button type="primary" block :loading="loading" attr-type="submit">创建</n-button>
    </n-form>
  </div>
</template>
