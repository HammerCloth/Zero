<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import * as dashboardApi from '@/api/dashboard'
import type { AccountTrendSeries, MonthlyGrowthPoint } from '@/api/dashboard'
import * as exportApi from '@/api/export'
import { formatMoney } from '@/lib/format'
import { DIM_ACCOUNT_OWNER, DIM_ACCOUNT_TYPE, useSettingsStore } from '@/stores/settings'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
])

const message = useMessage()
const settings = useSettingsStore()
const loading = ref(true)
const summary = ref({
  netWorth: 0,
  monthlyChange: 0,
  annualChange: 0,
  annualizedReturn: 0,
})
const range = ref<'3m' | '6m' | '1y' | 'all'>('1y')
const trendPoints = ref<{ date: string; netWorth: number }[]>([])
const stackedPoints = ref<{ date: string; byType: Record<string, number> }[]>([])
const accountTrends = ref<AccountTrendSeries[]>([])
const accountTypeFilter = ref('')

const composition = ref<{ byType: Record<string, number>; byOwner: Record<string, number> }>({
  byType: {},
  byOwner: {},
})
const year = ref(new Date().getFullYear())
const monthly = ref<MonthlyGrowthPoint[]>([])

const typeSelectOptions = computed(() => {
  const types = new Set(accountTrends.value.map((a) => a.type))
  return [{ label: '全部类型', value: '' }].concat(
    [...types].sort().map((t) => ({ label: settings.label(DIM_ACCOUNT_TYPE, t), value: t })),
  )
})

const filteredAccountTrends = computed(() => {
  if (!accountTypeFilter.value) {
    return accountTrends.value
  }
  return accountTrends.value.filter((a) => a.type === accountTypeFilter.value)
})

const trendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    valueFormatter: (v: number) => formatMoney(v),
  },
  xAxis: { type: 'category', data: trendPoints.value.map((p) => p.date) },
  yAxis: {
    type: 'value',
    scale: true,
    axisLabel: { formatter: (v: number) => formatMoney(v) },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: trendPoints.value.map((p) => p.netWorth),
      areaStyle: {},
    },
  ],
}))

const typePie = computed(() => ({
  tooltip: {
    trigger: 'item',
    valueFormatter: (v: number) => formatMoney(v as number),
  },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      data: Object.entries(composition.value.byType).map(([key, value]) => ({
        name: settings.label(DIM_ACCOUNT_TYPE, key),
        value,
      })),
    },
  ],
}))

const ownerPie = computed(() => ({
  tooltip: {
    trigger: 'item',
    valueFormatter: (v: number) => formatMoney(v as number),
  },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      data: Object.entries(composition.value.byOwner).map(([key, value]) => ({
        name: settings.label(DIM_ACCOUNT_OWNER, key),
        value,
      })),
    },
  ],
}))

const stackedByTypeOption = computed(() => {
  const pts = stackedPoints.value
  if (!pts.length) {
    return { xAxis: { type: 'category' as const, data: [] }, yAxis: { type: 'value' as const }, series: [] }
  }
  const dates = pts.map((p) => p.date)
  const typeKeys = new Set<string>()
  for (const p of pts) {
    Object.keys(p.byType).forEach((k) => typeKeys.add(k))
  }
  const keys = [...typeKeys].sort()
  if (!keys.length) {
    return {
      xAxis: { type: 'category' as const, data: dates },
      yAxis: { type: 'value' as const },
      series: [],
    }
  }
  const series = keys.map((key) => ({
    name: settings.label(DIM_ACCOUNT_TYPE, key),
    type: 'line' as const,
    stack: 'nw',
    areaStyle: {},
    data: pts.map((p) => p.byType[key] ?? 0),
  }))
  return {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v: number) => formatMoney(v),
    },
    legend: { type: 'scroll', bottom: 0 },
    xAxis: { type: 'category', data: dates },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (v: number) => formatMoney(v) },
    },
    series,
  }
})

const accountTrendOption = computed(() => {
  const accs = filteredAccountTrends.value.filter((a) => a.points.length)
  if (!accs.length) {
    return { xAxis: { type: 'category' as const, data: [] }, yAxis: { type: 'value' as const }, series: [] }
  }
  const dateSet = new Set<string>()
  for (const a of accs) {
    for (const p of a.points) {
      dateSet.add(p.date)
    }
  }
  const dates = [...dateSet].sort()
  const palette = [
    '#6366f1',
    '#22c55e',
    '#f97316',
    '#ec4899',
    '#14b8a6',
    '#a855f7',
    '#eab308',
    '#64748b',
  ]
  const series = accs.map((a, i) => ({
    name: a.name,
    type: 'line' as const,
    smooth: true,
    data: dates.map((d) => {
      const pt = a.points.find((x) => x.date === d)
      return pt ? pt.balance : null
    }),
    itemStyle: { color: palette[i % palette.length] },
  }))
  return {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v: number) => (v == null ? '' : formatMoney(v as number)),
    },
    legend: { type: 'scroll', bottom: 0 },
    xAxis: { type: 'category', data: dates },
    yAxis: {
      type: 'value',
      scale: true,
      axisLabel: { formatter: (v: number) => formatMoney(v) },
    },
    series,
  }
})

const monthlyBar = computed(() => {
  const labels = monthly.value.map((p) => `${year.value}-${p.month}`)
  const changes = monthly.value.map((p) => p.change)
  let run = 0
  const cum = monthly.value.map((p) => {
    if (p.cumulativeChange != null && !Number.isNaN(p.cumulativeChange)) {
      return p.cumulativeChange
    }
    run += p.change
    return run
  })
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: [
      {
        type: 'value',
        name: '月度变化',
        axisLabel: { formatter: (v: number) => formatMoney(v) },
      },
      {
        type: 'value',
        name: '累计',
        position: 'right',
        axisLabel: { formatter: (v: number) => formatMoney(v) },
      },
    ],
    series: [
      {
        type: 'bar',
        name: '月度变化',
        data: changes.map((c) => ({
          value: c,
          itemStyle: { color: c >= 0 ? '#18a058' : '#d03050' },
        })),
      },
      {
        type: 'line',
        name: '年内累计',
        yAxisIndex: 1,
        smooth: true,
        data: cum,
      },
    ],
  }
})

async function loadRangeCharts() {
  try {
    const tr = await dashboardApi.fetchTrend(range.value)
    trendPoints.value = tr.points
  } catch {
    trendPoints.value = []
  }
  try {
    const st = await dashboardApi.fetchStackedByType(range.value)
    stackedPoints.value = st.points
  } catch {
    stackedPoints.value = []
  }
  try {
    const at = await dashboardApi.fetchAccountTrends(range.value)
    accountTrends.value = at.accounts
  } catch {
    accountTrends.value = []
  }
}

async function load() {
  loading.value = true
  try {
    try {
      await settings.load()
    } catch {
      /* 标签映射降级为 key，不阻塞总览 */
    }
    summary.value = await dashboardApi.fetchSummary()
    await loadRangeCharts()
    composition.value = await dashboardApi.fetchComposition()
    const mg = await dashboardApi.fetchMonthlyGrowth(year.value)
    monthly.value = mg.points
  } catch (e) {
    console.error('dashboard load', e)
    message.error('加载仪表盘失败')
  } finally {
    loading.value = false
  }
}

watch(range, async () => {
  try {
    await loadRangeCharts()
  } catch (e) {
    console.error('dashboard range charts', e)
    message.error('加载图表失败')
  }
})

watch(year, async (y) => {
  if (y == null) {
    return
  }
  try {
    const mg = await dashboardApi.fetchMonthlyGrowth(y)
    monthly.value = mg.points
  } catch {
    message.error('加载月度数据失败')
  }
})

async function downloadCsv() {
  try {
    const { data } = await exportApi.downloadCsv()
    const blob = new Blob([data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'zero-export.csv'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    message.error('导出失败')
  }
}

onMounted(load)
</script>

<template>
  <n-space vertical size="large">
    <n-space justify="space-between" align="center">
      <n-h2 style="margin: 0">总览</n-h2>
      <n-button @click="downloadCsv">导出 CSV</n-button>
    </n-space>

    <n-spin :show="loading">
      <n-grid :cols="4" responsive="screen" :x-gap="12" :y-gap="12">
        <n-gi span="4 m:2 l:1">
          <n-card title="净资产">
            <n-statistic :value="formatMoney(summary.netWorth)" />
          </n-card>
        </n-gi>
        <n-gi span="4 m:2 l:1">
          <n-card title="月度变化">
            <n-statistic :value="formatMoney(summary.monthlyChange)" />
          </n-card>
        </n-gi>
        <n-gi span="4 m:2 l:1">
          <n-card title="年度变化">
            <n-statistic :value="formatMoney(summary.annualChange)" />
          </n-card>
        </n-gi>
        <n-gi span="4 m:2 l:1">
          <n-card title="年化收益率">
            <n-statistic :value="(summary.annualizedReturn * 100).toFixed(2) + '%'" />
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="净资产趋势" style="margin-top: 16px">
        <n-space style="margin-bottom: 12px">
          <n-radio-group v-model:value="range">
            <n-radio-button value="3m">3 个月</n-radio-button>
            <n-radio-button value="6m">6 个月</n-radio-button>
            <n-radio-button value="1y">1 年</n-radio-button>
            <n-radio-button value="all">全部</n-radio-button>
          </n-radio-group>
        </n-space>
        <v-chart v-if="trendPoints.length" style="height: 320px" :option="trendOption" autoresize />
        <n-empty v-else description="暂无数据" />
      </n-card>

      <n-card title="资产堆叠（按类型）" style="margin-top: 16px">
        <n-text depth="3" style="display: block; margin-bottom: 8px">与上方时间范围一致</n-text>
        <v-chart v-if="stackedPoints.length" style="height: 360px" :option="stackedByTypeOption" autoresize />
        <n-empty v-else description="暂无数据" />
      </n-card>

      <n-card title="账户余额趋势" style="margin-top: 16px">
        <n-space style="margin-bottom: 12px" align="center">
          <span>筛选类型</span>
          <n-select
            v-model:value="accountTypeFilter"
            :options="typeSelectOptions"
            style="width: 200px"
            clearable
            placeholder="全部"
          />
        </n-space>
        <v-chart
          v-if="filteredAccountTrends.some((a) => a.points.length)"
          style="height: 380px"
          :option="accountTrendOption"
          autoresize
        />
        <n-empty v-else description="暂无账户或快照数据" />
      </n-card>

      <n-grid :cols="2" :x-gap="12" style="margin-top: 16px">
        <n-gi>
          <n-card title="资产构成（类型）">
            <v-chart v-if="Object.keys(composition.byType).length" style="height: 280px" :option="typePie" autoresize />
            <n-empty v-else />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="资产构成（归属）">
            <v-chart
              v-if="Object.keys(composition.byOwner).length"
              style="height: 280px"
              :option="ownerPie"
              autoresize
            />
            <n-empty v-else />
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="月度净资产变化" style="margin-top: 16px">
        <n-space style="margin-bottom: 12px">
          <n-input-number v-model:value="year" :min="2000" :max="2100" />
        </n-space>
        <v-chart v-if="monthly.length" style="height: 300px" :option="monthlyBar" autoresize />
        <n-empty v-else />
      </n-card>
    </n-spin>
  </n-space>
</template>
