<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type { SnapshotListItem } from '@/types/models'
import * as snapshotApi from '@/api/snapshot'
import { formatMoney } from '@/lib/format'

const router = useRouter()
const message = useMessage()
const rows = ref<SnapshotListItem[]>([])
const loading = ref(true)

const columns: DataTableColumns<SnapshotListItem> = [
  { title: '日期', key: 'date' },
  {
    title: '净资产',
    key: 'netWorth',
    render(row) {
      return formatMoney(row.netWorth)
    },
  },
  { title: '创建时间', key: 'createdAt' },
]

onMounted(async () => {
  try {
    rows.value = await snapshotApi.listSnapshots()
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
})

function rowProps(row: SnapshotListItem) {
  return {
    style: 'cursor: pointer',
    onClick: () => router.push(`/snapshots/${row.id}`),
  }
}
</script>

<template>
  <n-space vertical size="large">
    <n-space justify="space-between">
      <n-h2 style="margin: 0">快照</n-h2>
      <n-space>
        <n-button @click="router.push('/snapshots/calendar')">日历</n-button>
        <n-button type="primary" @click="router.push('/snapshots/new')">新建快照</n-button>
      </n-space>
    </n-space>
    <n-data-table
      :loading="loading"
      :columns="columns"
      :data="rows"
      :row-props="rowProps"
    />
  </n-space>
</template>
