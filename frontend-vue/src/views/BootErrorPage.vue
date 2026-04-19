<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

async function retry() {
  await auth.retryBootstrap()
  if (!auth.bootError) {
    if (auth.needsSetup) {
      await router.replace('/setup')
    } else if (auth.isLoggedIn) {
      await router.replace('/dashboard')
    } else {
      await router.replace('/login')
    }
  }
}
</script>

<template>
  <div style="max-width: 480px; margin: 80px auto; padding: 24px">
    <n-result status="error" title="无法连接后端">
      <template #footer>
        <p style="text-align: left; margin-bottom: 16px; opacity: 0.85">
          {{ auth.bootError }}
        </p>
        <n-space vertical>
          <n-text depth="3">
            请在本机先启动 Spring Boot（默认端口 8080），再点击重试。开发时可在 <code>zero/backend</code> 执行：
          </n-text>
          <n-code style="display: block; padding: 12px">
            ./mvnw spring-boot:run
          </n-code>
          <n-text depth="3">或使用已打包的 JAR，并确保已创建 <code>backend/data</code> 目录。</n-text>
          <n-button type="primary" @click="retry">重试连接</n-button>
        </n-space>
      </template>
    </n-result>
  </div>
</template>
