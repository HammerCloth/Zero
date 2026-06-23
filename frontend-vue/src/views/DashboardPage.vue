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
  <div class="page-stack">
    <section class="page-header">
      <div class="page-header__copy">
        <h2 class="page-header__title">总览</h2>
        <p class="page-header__desc">
          用一个视图同时观察净资产、资产构成、账户走势和年度变化。手机访问时保留关键数字和核心图表，不依赖额外静态素材。
        </p>
      </div>
      <n-button type="primary" @click="downloadCsv">导出 CSV</n-button>
    </section>

    <n-spin :show="loading">
      <section class="stat-grid">
        <article class="stat-card">
          <div class="stat-card__label">净资产</div>
          <div class="stat-card__value">{{ formatMoney(summary.netWorth) }}</div>
          <div class="stat-card__hint">当前账户与资产快照汇总</div>
        </article>
        <article class="stat-card">
          <div class="stat-card__label">月度变化</div>
          <div class="stat-card__value">{{ formatMoney(summary.monthlyChange) }}</div>
          <div class="stat-card__hint">最近一个统计周期变化</div>
        </article>
        <article class="stat-card">
          <div class="stat-card__label">年度变化</div>
          <div class="stat-card__value">{{ formatMoney(summary.annualChange) }}</div>
          <div class="stat-card__hint">当前年度累计增减</div>
        </article>
        <article class="stat-card">
          <div class="stat-card__label">年化收益率</div>
          <div class="stat-card__value">{{ (summary.annualizedReturn * 100).toFixed(2) }}%</div>
          <div class="stat-card__hint">按已记录数据估算</div>
        </article>
      </section>

      <n-card class="surface-panel" title="净资产趋势">
        <div class="table-toolbar" style="margin-bottom: 12px">
          <span class="section-note">查看不同时间窗口下的整体走势</span>
          <n-radio-group v-model:value="range">
            <n-radio-button value="3m">3 个月</n-radio-button>
            <n-radio-button value="6m">6 个月</n-radio-button>
            <n-radio-button value="1y">1 年</n-radio-button>
            <n-radio-button value="all">全部</n-radio-button>
          </n-radio-group>
        </div>
        <v-chart v-if="trendPoints.length" class="chart-frame" :option="trendOption" autoresize />
        <n-empty v-else description="暂无数据" />
      </n-card>

      <n-card class="surface-panel" title="资产堆叠（按类型）">
        <span class="section-note" style="display: block; margin-bottom: 8px">与上方时间范围一致</span>
        <v-chart v-if="stackedPoints.length" class="chart-frame" :option="stackedByTypeOption" autoresize />
        <n-empty v-else description="暂无数据" />
      </n-card>

      <n-card class="surface-panel" title="账户余额趋势">
        <div class="inline-control" style="margin-bottom: 12px">
          <span class="section-note">筛选类型</span>
          <n-select
            v-model:value="accountTypeFilter"
            :options="typeSelectOptions"
            style="width: min(220px, 100%)"
            clearable
            placeholder="全部"
          />
        </div>
        <v-chart
          v-if="filteredAccountTrends.some((a) => a.points.length)"
          class="chart-frame"
          :option="accountTrendOption"
          autoresize
        />
        <n-empty v-else description="暂无账户或快照数据" />
      </n-card>

      <section class="section-grid section-grid--two">
        <n-card class="surface-panel" title="资产构成（类型）">
          <v-chart
            v-if="Object.keys(composition.byType).length"
            class="chart-frame--compact"
            :option="typePie"
            autoresize
          />
          <n-empty v-else />
        </n-card>
        <n-card class="surface-panel" title="资产构成（归属）">
          <v-chart
            v-if="Object.keys(composition.byOwner).length"
            class="chart-frame--compact"
            :option="ownerPie"
            autoresize
          />
          <n-empty v-else />
        </n-card>
      </section>

      <n-card class="surface-panel" title="各类型内账户占比">
        <div class="page-stack" style="gap: 12px">
          <span class="section-note">基于最新快照；选择资产类型后，以饼图查看该类型下各账户金额占比。</span>
          <n-select
            v-model:value="accountShareTypeKey"
            :options="accountShareTypeOptions"
            placeholder="选择资产类型"
            style="max-width: 320px"
            :disabled="!accountShareTypeOptions.length"
          />
        </div>
        <v-chart
          v-if="accountShareTypeKey && typeAccountSharePieHasData"
          class="chart-frame--compact"
          :option="typeAccountSharePieOption"
          autoresize
        />
        <n-empty v-else-if="!accountShareTypeOptions.length" description="暂无分账户数据（需后端返回 byTypeAccounts）" />
        <n-empty v-else description="该类型下暂无可展示的账户余额" />
      </n-card>

      <n-card class="surface-panel" title="月度净资产变化">
        <div class="inline-control" style="margin-bottom: 12px">
          <span class="section-note">统计年份</span>
          <n-input-number v-model:value="year" :min="2000" :max="2100" />
        </div>
        <v-chart v-if="monthly.length" class="chart-frame--compact" :option="monthlyBar" autoresize />
        <n-empty v-else />
      </n-card>
    </n-spin>
  </div>
</template>
