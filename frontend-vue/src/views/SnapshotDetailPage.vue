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
    <div v-if="snap" class="page-stack">
      <section class="page-header">
        <div class="page-header__copy">
          <h2 class="page-header__title">快照 {{ snap.date }}</h2>
          <p class="page-header__desc">查看该日期下的账户余额和关联大事记，也可以继续编辑或删除这次记录。</p>
        </div>
        <div class="inline-control">
          <n-button @click="onEdit">编辑</n-button>
          <n-button type="error" @click="onDelete">删除</n-button>
        </div>
      </section>

      <div class="detail-meta-grid">
        <div class="detail-meta-card">
          <span class="stat-card__label">备注</span>
          <strong class="detail-meta-card__value">{{ snap.note || '—' }}</strong>
        </div>
        <div class="detail-meta-card">
          <span class="stat-card__label">创建时间</span>
          <strong class="detail-meta-card__value">{{ snap.createdAt }}</strong>
        </div>
      </div>

      <section class="page-stack" style="gap: 12px">
        <h3 class="section-title">账户余额</h3>
        <div class="data-table-shell">
          <n-data-table :columns="itemColumns" :data="snap.items" />
        </div>
      </section>

      <section class="page-stack" style="gap: 12px">
        <h3 class="section-title">大事记</h3>
        <div class="data-table-shell">
          <n-data-table :columns="eventColumns" :data="snap.events" />
        </div>
      </section>
    </div>
  </n-spin>
</template>
