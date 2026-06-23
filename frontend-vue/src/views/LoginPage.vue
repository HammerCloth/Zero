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
  <div class="login-shell">
    <div class="login-wrap">
      <section class="login-hero">
        <span class="login-hero__badge">Project Zero</span>
        <h1 class="login-hero__title">把资产变化、账户结构和年度事件放到同一个视角。</h1>
        <p class="login-hero__desc">
          更清楚地看净资产走势、账户构成和时间节点，桌面端和手机端都能快速进入核心数据。
        </p>
        <div class="login-hero__highlights">
          <div class="login-hero__tile">
            <strong>净资产追踪</strong>
            <span>集中查看阶段涨跌和长期趋势。</span>
          </div>
          <div class="login-hero__tile">
            <strong>账户拆解</strong>
            <span>按账户、类型与归属快速切换观察。</span>
          </div>
          <div class="login-hero__tile">
            <strong>手机兼容</strong>
            <span>外出时也能直接查看关键数字。</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card__brand">
          <span class="app-brand__mark">Z</span>
          <div class="login-card__brand-text">
            <strong>Project Zero</strong>
            <span class="section-note">Personal finance workspace</span>
          </div>
        </div>
        <h2 class="login-card__title">登录</h2>
        <p class="login-card__desc">输入账号后进入你的财务工作台。</p>
        <n-form @submit.prevent="submit">
          <n-form-item label="用户名">
            <n-input v-model:value="username" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item label="密码">
            <n-input
              v-model:value="password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
            />
          </n-form-item>
          <div class="login-card__actions">
            <span class="section-note">保持登录状态可减少重复验证。</span>
            <n-switch v-model:value="remember">
              <template #checked>记住我</template>
              <template #unchecked>记住我</template>
            </n-switch>
          </div>
          <n-button type="primary" block :loading="loading" attr-type="submit">登录</n-button>
        </n-form>
      </section>
    </div>
  </div>
</template>
