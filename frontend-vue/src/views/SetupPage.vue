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
  <div class="screen-shell">
    <div class="screen-card">
      <div class="page-stack">
        <div>
          <span class="login-hero__badge">首次配置</span>
          <h1 class="page-header__title" style="margin-top: 18px">创建管理员账号</h1>
          <p class="page-header__desc" style="margin-top: 8px">
            初始化完成后，就可以开始记录快照、账户和年度事件。
          </p>
        </div>
        <n-form @submit.prevent="submit">
          <n-form-item label="用户名">
            <n-input v-model:value="username" placeholder="管理员用户名" />
          </n-form-item>
          <n-form-item label="密码（至少 8 位）">
            <n-input v-model:value="password" type="password" show-password-on="click" />
          </n-form-item>
          <n-button type="primary" block :loading="loading" attr-type="submit">创建管理员</n-button>
        </n-form>
      </div>
    </div>
  </div>
</template>
