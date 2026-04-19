<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { DataTableColumns } from 'naive-ui'
import * as eventApi from '@/api/event'
import { formatMoney } from '@/lib/format'
import { DIM_EVENT_CATEGORY, useSettingsStore } from '@/stores/settings'

use([CanvasRenderer, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const message = useMessage()
const settings = useSettingsStore()
const year = ref(new Date().getFullYear())
const byCategory = ref<Record<string, number>>({})
const countByCategory = ref<Record<string, number>>({})
const grandTotal = ref(0)
const loading = ref(true)

const rows = computed(() =>
  Object.entries(byCategory.value).map(([category, amount]) => ({ category, amount })),
)

const barOption = computed(() => {
  const r = rows.value
  if (!r.length) {
    return {}
  }
  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown) => {
        const arr = Array.isArray(params) ? params : [params]
        const p = arr[0] as { dataIndex: number; value: number }
        const key = r[p.dataIndex]?.category
        const cnt = key != null ? countByCategory.value[key] : undefined
        const label = settings.label(DIM_EVENT_CATEGORY, key ?? '')
        const extra = cnt != null ? `（${cnt} 笔）` : ''
        return `${label}${extra}<br/>${formatMoney(Number(p.value))}`
      },
    },
    xAxis: {
      type: 'category',
      data: r.map((row) => settings.label(DIM_EVENT_CATEGORY, row.category)),
      axisLabel: { interval: 0, rotate: r.length > 6 ? 30 : 0 },
    },
    yAxis: { type: 'value', axisLabel: { formatter: (v: number) => formatMoney(v) } },
    series: [{ type: 'bar', data: r.map((row) => row.amount) }],
  }
})

const pieOption = computed(() => {
  const r = rows.value
  if (!r.length) {
    return {}
  }
  return {
    tooltip: {
      trigger: 'item',
      valueFormatter: (v: number) => formatMoney(v),
    },
    series: [
      {
        type: 'pie',
        radius: ['38%', '62%'],
        data: r.map((row) => ({
          name: settings.label(DIM_EVENT_CATEGORY, row.category),
          value: row.amount,
        })),
      },
    ],
  }
})

const columns: DataTableColumns<{ category: string; amount: number }> = [
  {
    title: '分类',
    key: 'category',
    render(row) {
      return settings.label(DIM_EVENT_CATEGORY, row.category)
    },
  },
  {
    title: '合计金额',
    key: 'amount',
    render(row) {
      return formatMoney(row.amount)
    },
  },
  {
    title: '笔数',
    key: 'count',
    render(row) {
      const n = countByCategory.value[row.category]
      return n != null ? String(n) : '—'
    },
  },
]

async function load() {
  loading.value = true
  try {
    await settings.load()
    const data = await eventApi.eventStats(year.value)
    byCategory.value = data.byCategory
    grandTotal.value = data.grandTotal ?? 0
    countByCategory.value = data.countByCategory ?? {}
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

watch(year, () => {
  load()
})

onMounted(load)
</script>

<template>
  <n-space vertical size="large">
    <n-space align="center">
      <n-h2 style="margin: 0">大事记统计</n-h2>
      <n-input-number v-model:value="year" :min="2000" :max="2100" />
    </n-space>
    <n-text v-if="rows.length">年度支出合计：<strong>{{ formatMoney(grandTotal) }}</strong></n-text>
    <n-spin :show="loading">
      <n-space vertical size="large">
        <n-grid v-if="rows.length" :cols="2" responsive="screen" :x-gap="16">
          <n-gi>
            <n-card title="按分类（支出）">
              <v-chart style="height: 300px" :option="barOption" autoresize />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="占比">
              <v-chart style="height: 300px" :option="pieOption" autoresize />
            </n-card>
          </n-gi>
        </n-grid>
        <n-card title="明细">
          <n-data-table :columns="columns" :data="rows" />
        </n-card>
        <n-empty v-if="!loading && !rows.length" description="该年暂无支出数据" />
      </n-space>
    </n-spin>
  </n-space>
</template>
