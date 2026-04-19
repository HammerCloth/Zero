<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDialog, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type { SnapshotDetail } from '@/types/models'
import * as snapshotApi from '@/api/snapshot'
import { formatMoney } from '@/lib/format'
import { DIM_EVENT_CATEGORY, DIM_ACCOUNT_OWNER, DIM_ACCOUNT_TYPE, useSettingsStore } from '@/stores/settings'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const settings = useSettingsStore()
const snap = ref<SnapshotDetail | null>(null)
const loading = ref(true)

const id = route.params.id as string

const itemColumns: DataTableColumns = [
  { title: '账户', key: 'accountName' },
  {
    title: '类型',
    key: 'type',
    render(row) {
      const r = row as { type?: string }
      return settings.label(DIM_ACCOUNT_TYPE, r.type ?? '')
    },
  },
  {
    title: '归属',
    key: 'owner',
    render(row) {
      const r = row as { owner?: string }
      return settings.label(DIM_ACCOUNT_OWNER, r.owner ?? '')
    },
  },
  {
    title: '余额',
    key: 'balance',
    render(row) {
      const r = row as { balance: number }
      return formatMoney(r.balance)
    },
  },
]

const eventColumns: DataTableColumns = [
  {
    title: '分类',
    key: 'category',
    render(row) {
      const r = row as { category: string }
      return settings.label(DIM_EVENT_CATEGORY, r.category)
    },
  },
  { title: '说明', key: 'description' },
  {
    title: '收支',
    key: 'flow',
    render(row) {
      const r = row as { amount: number }
      return r.amount < 0 ? '支出' : '收入'
    },
  },
  {
    title: '金额',
    key: 'amount',
    render(row) {
      const r = row as { amount: number }
      return formatMoney(r.amount)
    },
  },
]

onMounted(async () => {
  try {
    await settings.load()
    snap.value = await snapshotApi.getSnapshot(id)
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
})

function onEdit() {
  router.push(`/snapshots/${id}/edit`)
}

function onDelete() {
  dialog.warning({
    title: '删除快照',
    content: '确定删除？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await snapshotApi.deleteSnapshot(id)
      message.success('已删除')
      await router.replace('/snapshots')
    },
  })
}
</script>

<template>
  <n-spin :show="loading">
    <template v-if="snap">
      <n-space justify="space-between" style="margin-bottom: 16px">
        <n-h2 style="margin: 0">快照 {{ snap.date }}</n-h2>
        <n-space>
          <n-button @click="onEdit">编辑</n-button>
          <n-button type="error" @click="onDelete">删除</n-button>
        </n-space>
      </n-space>
      <n-descriptions bordered label-placement="top" :column="2">
        <n-descriptions-item label="备注">{{ snap.note || '—' }}</n-descriptions-item>
        <n-descriptions-item label="创建时间">{{ snap.createdAt }}</n-descriptions-item>
      </n-descriptions>
      <n-h3 style="margin-top: 24px">账户余额</n-h3>
      <n-data-table :columns="itemColumns" :data="snap.items" />
      <n-h3 style="margin-top: 24px">大事记</n-h3>
      <n-data-table :columns="eventColumns" :data="snap.events" />
    </template>
  </n-spin>
</template>
