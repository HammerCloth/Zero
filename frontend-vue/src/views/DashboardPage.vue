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
import * as accountApi from '@/api/account'
import * as dashboardApi from '@/api/dashboard'
import type { AccountTrendSeries, DashboardComposition, MonthlyGrowthPoint } from '@/api/dashboard'
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

const composition = ref<DashboardComposition>({
  byType: {},
  byOwner: {},
})
/** 账户 id → 展示名（用于分账户占比图） */
const accountNameById = ref<Record<string, string>>({})
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
  legend: {
    type: 'scroll',
    bottom: 0,
    left: 'center',
  },
  series: [
    {
      type: 'pie',
      radius: ['32%', '56%'],
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
  legend: {
    type: 'scroll',
    bottom: 0,
    left: 'center',
  },
  series: [
    {
      type: 'pie',
      radius: ['32%', '56%'],
      data: Object.entries(composition.value.byOwner).map(([key, value]) => ({
        name: settings.label(DIM_ACCOUNT_OWNER, key),
        value,
      })),
    },
  ],
}))

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

/** 有分账户数据的类型，供下拉选择 */
const accountShareTypeOptions = computed(() => {
  const bta = composition.value.byTypeAccounts
  if (!bta) {
    return [] as { label: string; value: string }[]
  }
  return Object.keys(bta)
    .filter((t) => {
      const row = bta[t] ?? {}
      return Object.values(row).some((v) => v !== 0 && !Number.isNaN(v))
    })
    .sort((a, b) => a.localeCompare(b))
    .map((t) => ({ label: settings.label(DIM_ACCOUNT_TYPE, t), value: t }))
})

const accountShareTypeKey = ref<string | null>(null)

watch(
  accountShareTypeOptions,
  (opts) => {
    if (!opts.length) {
      accountShareTypeKey.value = null
      return
    }
    if (!accountShareTypeKey.value || !opts.some((o) => o.value === accountShareTypeKey.value)) {
      accountShareTypeKey.value = opts[0].value
    }
  },
  { immediate: true },
)

/** 当前选中类型下，各账户占比（饼图扇区用绝对值，便于负债等类型展示；tooltip 显示带符号金额） */
const typeAccountSharePieOption = computed(() => {
  const bta = composition.value.byTypeAccounts
  const t = accountShareTypeKey.value
  if (!bta || !t) {
    return {
      tooltip: { trigger: 'item' as const },
      legend: { type: 'scroll' as const, bottom: 0, left: 'center' },
      series: [{ type: 'pie' as const, radius: ['34%', '58%'], data: [] as { name: string; value: number }[] }],
    }
  }
  const row = bta[t] ?? {}
  const entries = Object.entries(row).filter(([, v]) => v !== 0 && !Number.isNaN(v))
  const data = entries.map(([id, raw], i) => ({
    name: accountNameById.value[id] ?? id,
    value: Math.abs(raw),
    raw,
    itemStyle: { color: palette[i % palette.length] },
  }))
  return {
    tooltip: {
      trigger: 'item' as const,
      formatter: (p: { name?: string; data?: { raw?: number } }) => {
        const raw = Number(p.data?.raw ?? 0)
        return `${p.name ?? ''}<br/>${formatMoney(raw)}`
      },
    },
    legend: { type: 'scroll' as const, bottom: 0, left: 'center' },
    series: [
      {
        type: 'pie' as const,
        radius: ['34%', '58%'],
        data,
      },
    ],
  }
})

const typeAccountSharePieHasData = computed(() => {
  const bta = composition.value.byTypeAccounts
  const t = accountShareTypeKey.value
  if (!bta || !t) {
    return false
  }
  const row = bta[t] ?? {}
  return Object.values(row).some((v) => v !== 0 && !Number.isNaN(v))
})

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
    try {
      const accs = await accountApi.listAccounts()
      accountNameById.value = Object.fromEntries(accs.map((a) => [a.id, a.name]))
    } catch {
      accountNameById.value = {}
    }
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
      <n-card title="资产概览" size="small">
        <n-grid :cols="4" :x-gap="10" :y-gap="4" responsive="screen">
          <n-gi span="1">
            <n-statistic label="净资产" :value="formatMoney(summary.netWorth)" tabular-nums />
          </n-gi>
          <n-gi span="1">
            <n-statistic label="月度变化" :value="formatMoney(summary.monthlyChange)" tabular-nums />
          </n-gi>
          <n-gi span="1">
            <n-statistic label="年度变化" :value="formatMoney(summary.annualChange)" tabular-nums />
          </n-gi>
          <n-gi span="1">
            <n-statistic
              label="年化收益率"
              :value="(summary.annualizedReturn * 100).toFixed(2) + '%'"
              tabular-nums
            />
          </n-gi>
        </n-grid>
      </n-card>

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

      <n-grid :cols="4" responsive="screen" :x-gap="12" style="margin-top: 16px">
        <n-gi span="4 m:2">
          <n-card title="资产构成（类型）">
            <v-chart v-if="Object.keys(composition.byType).length" style="height: 300px" :option="typePie" autoresize />
            <n-empty v-else />
          </n-card>
        </n-gi>
        <n-gi span="4 m:2">
          <n-card title="资产构成（归属）">
            <v-chart
              v-if="Object.keys(composition.byOwner).length"
              style="height: 300px"
              :option="ownerPie"
              autoresize
            />
            <n-empty v-else />
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="各类型内账户占比" style="margin-top: 16px">
        <n-space vertical size="small" style="margin-bottom: 12px">
          <n-text depth="3">基于最新快照；选择资产类型后，以饼图查看该类型下各账户金额占比</n-text>
          <n-select
            v-model:value="accountShareTypeKey"
            :options="accountShareTypeOptions"
            placeholder="选择资产类型"
            style="max-width: 320px"
            :disabled="!accountShareTypeOptions.length"
          />
        </n-space>
        <v-chart
          v-if="accountShareTypeKey && typeAccountSharePieHasData"
          style="height: 320px"
          :option="typeAccountSharePieOption"
          autoresize
        />
        <n-empty v-else-if="!accountShareTypeOptions.length" description="暂无分账户数据（需后端返回 byTypeAccounts）" />
        <n-empty v-else description="该类型下暂无可展示的账户余额" />
      </n-card>

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
