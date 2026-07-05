<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NButton, NTag, useDialog, useMessage } from 'naive-ui'
import * as oauthApi from '@/api/oauth'
import type { OAuthAuthorization } from '@/api/oauth'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const rows = ref<OAuthAuthorization[]>([])

const columns = [
  {
    title: '客户端',
    key: 'clientName',
    render: (row: OAuthAuthorization) =>
      h('div', { class: 'cell-stack' }, [
        h('span', { class: 'cell-main' }, row.clientName || 'AI 客户端'),
        h('span', { class: 'cell-muted' }, row.clientId),
      ]),
  },
  { title: '权限', key: 'scope' },
  { title: '授权时间', key: 'createdAt' },
  {
    title: '最后使用',
    key: 'lastUsedAt',
    render: (row: OAuthAuthorization) => row.lastUsedAt || '尚未使用',
  },
  { title: '过期时间', key: 'expiresAt' },
  {
    title: '状态',
    key: 'status',
    render: (row: OAuthAuthorization) =>
      h(
        NTag,
        { size: 'small', type: row.revokedAt ? 'default' : 'success' },
        { default: () => (row.revokedAt ? '已撤销' : '有效') },
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row: OAuthAuthorization) =>
      h(
        NButton,
        {
          size: 'small',
          quaternary: true,
          disabled: Boolean(row.revokedAt),
          onClick: () => revoke(row),
        },
        { default: () => '撤销' },
      ),
  },
]

async function refresh() {
  loading.value = true
  try {
    rows.value = await oauthApi.listAuthorizations()
  } finally {
    loading.value = false
  }
}

function revoke(row: OAuthAuthorization) {
  dialog.warning({
    title: '撤销 AI 客户端',
    content: `撤销后 ${row.clientName || '该客户端'} 需要重新授权才能访问 MCP。`,
    positiveText: '撤销',
    negativeText: '取消',
    onPositiveClick: async () => {
      await oauthApi.revokeAuthorization(row.id)
      message.success('已撤销')
      await refresh()
    },
  })
}

function revokeAll() {
  dialog.warning({
    title: '撤销全部 AI 客户端',
    content: '所有 AI 客户端都需要重新授权后才能访问 MCP。',
    positiveText: '全部撤销',
    negativeText: '取消',
    onPositiveClick: async () => {
      await oauthApi.revokeAllAuthorizations()
      message.success('已全部撤销')
      await refresh()
    },
  })
}

onMounted(() => {
  refresh().catch(() => message.error('加载失败'))
})
</script>

<template>
  <div class="page-stack">
    <section class="page-header">
      <div class="page-header__copy">
        <h2 class="page-header__title">AI 客户端</h2>
        <p class="page-header__desc">查看和撤销已授权访问 MCP 的 AI Agent 客户端。</p>
      </div>
      <div class="inline-control">
        <n-button @click="refresh">刷新</n-button>
        <n-button type="warning" :disabled="rows.length === 0" @click="revokeAll">全部撤销</n-button>
      </div>
    </section>

    <n-card class="surface-panel">
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        :row-key="(row: OAuthAuthorization) => row.id"
      />
    </n-card>
  </div>
</template>
