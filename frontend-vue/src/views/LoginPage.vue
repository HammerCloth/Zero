<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const remember = ref(true)
const loading = ref(false)

async function submit() {
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value, remember.value)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    await router.replace(redirect)
  } catch {
    message.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 400px; margin: 80px auto">
    <n-h2>登录</n-h2>
    <n-form @submit.prevent="submit">
      <n-form-item label="用户名">
        <n-input v-model:value="username" />
      </n-form-item>
      <n-form-item label="密码">
        <n-input v-model:value="password" type="password" show-password-on="click" />
      </n-form-item>
      <n-form-item label="记住我">
        <n-switch v-model:value="remember" />
      </n-form-item>
      <n-button type="primary" block :loading="loading" attr-type="submit">登录</n-button>
    </n-form>
  </div>
</template>
