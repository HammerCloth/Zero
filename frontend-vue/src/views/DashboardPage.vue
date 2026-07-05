<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart, SankeyChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import * as accountApi from '@/api/account'
import * as dashboardApi from '@/api/dashboard'
import type { DashboardComposition, DashboardTypeChange } from '@/api/dashboard'
import { formatMoney } from '@/lib/format'
import { DIM_ACCOUNT_OWNER, DIM_ACCOUNT_TYPE, useSettingsStore } from '@/stores/settings'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  SankeyChart,
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

const composition = ref<DashboardComposition>({
  byType: {},
  byOwner: {},
})
const typeChange = ref<DashboardTypeChange>({
  latestDate: null,
  previousDate: null,
  items: [],
})
/** 账户 id → 展示名（用于分账户占比图） */
const accountNameById = ref<Record<string, string>>({})

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

const typeChangeOption = computed(() => {
  const items = typeChange.value.items.filter((item) => Number.isFinite(item.change))
  const sorted = [...items].sort((a, b) => Math.abs(b.change) - Math.abs(a.change))
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: Array<{ dataIndex: number }>) => {
        const first = params[0]
        const item = sorted[first?.dataIndex ?? 0]
        if (!item) {
          return ''
        }
        return [
          settings.label(DIM_ACCOUNT_TYPE, item.type),
          `本次：${formatMoney(item.latest)}`,
          `上次：${formatMoney(item.previous)}`,
          `变化：${formatMoney(item.change)}`,
        ].join('<br/>')
      },
    },
    grid: { left: 12, right: 24, top: 24, bottom: 12, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { formatter: (v: number) => formatAssetAmount(v) },
    },
    yAxis: {
      type: 'category',
      data: sorted.map((item) => settings.label(DIM_ACCOUNT_TYPE, item.type)),
      axisLabel: { width: 96, overflow: 'truncate' },
    },
    series: [
      {
        type: 'bar',
        data: sorted.map((item) => ({
          value: item.change,
          itemStyle: { color: item.change >= 0 ? '#18a058' : '#d03050' },
        })),
        label: {
          show: true,
          position: (p: { value?: number }) => (Number(p.value ?? 0) >= 0 ? 'right' : 'left'),
          formatter: (p: { value?: number }) => formatAssetAmount(Number(p.value ?? 0)),
        },
      },
    ],
  }
})

const typeChangeHasData = computed(() => Boolean(typeChange.value.previousDate && typeChange.value.items.length))

const typeChangeSummary = computed(() => {
  const total = typeChange.value.items.reduce((sum, item) => sum + (Number.isFinite(item.change) ? item.change : 0), 0)
  if (total > 0) {
    return { label: '净流入', value: total, tone: 'positive' }
  }
  if (total < 0) {
    return { label: '净流出', value: Math.abs(total), tone: 'negative' }
  }
  return { label: '净变化', value: 0, tone: 'neutral' }
})

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

const sankeyPalette = ['#44c2a0', '#8b90d8', '#df8f68', '#8f79bb', '#5aa6d8', '#d6a33d', '#6d7d8f']
type SankeyNode = { name: string; labelName: string; raw?: number; itemStyle?: { color: string } }
type SankeyLink = { source: string; target: string; value: number; raw?: number; labelName: string }

function formatAssetAmount(value: number) {
  const abs = Math.abs(value)
  if (abs > 10000) {
    return `¥${(value / 10000).toFixed(2)}w`
  }
  if (abs > 1000) {
    return `¥${(value / 1000).toFixed(2)}k`
  }
  return `¥${value.toFixed(2)}`
}

const assetSankeyData = computed(() => {
  const nodes: SankeyNode[] = []
  const links: SankeyLink[] = []
  const nodeNames = new Set<string>()
  const positiveTypes: [string, number][] = []

  function addNode(name: string, labelName: string, color: string, raw?: number) {
    if (nodeNames.has(name)) {
      return
    }
    nodeNames.add(name)
    nodes.push({ name, labelName, raw, itemStyle: { color } })
  }

  for (const [type, value] of Object.entries(composition.value.byType)) {
    if (!Number.isFinite(value) || value <= 0) {
      continue
    }
    positiveTypes.push([type, value])
  }

  const totalAssets = positiveTypes.reduce((sum, [, value]) => sum + value, 0)

  addNode('summary:netWorth', '净资产', sankeyPalette[0], summary.value.netWorth)
  addNode('summary:totalAssets', '总资产', sankeyPalette[1], totalAssets)
  if (totalAssets > 0) {
    links.push({
      source: 'summary:netWorth',
      target: 'summary:totalAssets',
      value: totalAssets,
      raw: totalAssets,
      labelName: '净资产 → 总资产',
    })
  }

  positiveTypes
    .sort((a, b) => b[1] - a[1])
    .forEach(([type, value], index) => {
      const typeName = settings.label(DIM_ACCOUNT_TYPE, type)
      const typeNodeName = `type:${type}`
      addNode(typeNodeName, typeName, sankeyPalette[(index + 2) % sankeyPalette.length], value)
      links.push({
        source: 'summary:totalAssets',
        target: typeNodeName,
        value,
        raw: value,
        labelName: `总资产 → ${typeName}`,
      })

      const accounts = composition.value.byTypeAccounts?.[type] ?? {}
      Object.entries(accounts)
        .filter(([, accountValue]) => Number.isFinite(accountValue) && accountValue > 0)
        .sort((a, b) => b[1] - a[1])
        .forEach(([accountId, accountValue]) => {
          const accountName = accountNameById.value[accountId] ?? accountId
          const accountNodeName = `account:${accountId}`
          addNode(accountNodeName, accountName, sankeyPalette[(index + 3) % sankeyPalette.length], accountValue)
          links.push({
            source: typeNodeName,
            target: accountNodeName,
            value: accountValue,
            raw: accountValue,
            labelName: `${typeName} → ${accountName}`,
          })
        })
    })

  return { nodes, links, totalAssets }
})

const assetSankeyOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (p: {
      dataType?: string
      name?: string
      data?: { raw?: number; value?: number; labelName?: string }
    }) => {
      const raw = Number(p.data?.raw ?? p.data?.value ?? 0)
      if (p.dataType === 'edge') {
        return `${p.data?.labelName ?? p.name ?? ''}<br/>${formatAssetAmount(raw)}`
      }
      if (p.data?.labelName && Number.isFinite(raw)) {
        return `${p.data.labelName}<br/>${formatAssetAmount(raw)}`
      }
      return p.data?.labelName ?? p.name ?? ''
    },
  },
  series: [
    {
      type: 'sankey',
      left: 12,
      right: 120,
      top: 18,
      bottom: 18,
      nodeWidth: 10,
      nodeGap: 14,
      draggable: false,
      emphasis: { focus: 'adjacency' },
      lineStyle: {
        color: 'gradient',
        curveness: 0.56,
        opacity: 0.34,
      },
      label: {
        color: '#334155',
        fontSize: 12,
        formatter: (p: { name: string; data?: { labelName?: string; raw?: number } }) => {
          if (p.name === 'summary:netWorth') {
            return `净资产  ${formatAssetAmount(summary.value.netWorth)}`
          }
          if (p.name === 'summary:totalAssets') {
            return `总资产  ${formatAssetAmount(assetSankeyData.value.totalAssets)}`
          }
          if (p.data?.labelName && Number.isFinite(p.data.raw)) {
            return `${p.data.labelName}  ${formatAssetAmount(Number(p.data.raw))}`
          }
          return p.data?.labelName ?? p.name
        },
      },
      data: assetSankeyData.value.nodes,
      links: assetSankeyData.value.links,
    },
  ],
}))

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
    try {
      typeChange.value = await dashboardApi.fetchTypeChange()
    } catch {
      typeChange.value = { latestDate: null, previousDate: null, items: [] }
    }
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

onMounted(load)
</script>

<template>
  <div class="page-stack">
    <n-spin :show="loading">
      <n-card class="surface-panel" title="资产全貌">
        <div class="asset-sankey-summary">
          <span class="asset-sankey-summary__primary">净资产 {{ formatAssetAmount(summary.netWorth) }}</span>
        </div>
        <v-chart
          v-if="assetSankeyData.links.length"
          class="chart-frame chart-frame--sankey"
          :option="assetSankeyOption"
          autoresize
        />
        <n-empty v-else description="暂无资产快照数据" />
      </n-card>

      <n-card class="surface-panel" title="资金变化">
        <div class="type-change-summary">
          <div
            class="type-change-summary__value"
            :class="`type-change-summary__value--${typeChangeSummary.tone}`"
          >
            {{ typeChangeSummary.label }} {{ formatAssetAmount(typeChangeSummary.value) }}
          </div>
          <span v-if="typeChange.latestDate && typeChange.previousDate" class="section-note">
            {{ typeChange.previousDate }} → {{ typeChange.latestDate }}
          </span>
        </div>
        <v-chart
          v-if="typeChangeHasData"
          class="chart-frame--compact"
          :option="typeChangeOption"
          autoresize
        />
        <n-empty v-else description="暂无可对比的类型变化" />
      </n-card>

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

    </n-spin>
  </div>
</template>
