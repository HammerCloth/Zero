import { useEffect, useMemo, useState } from 'react'
import * as api from '../api/api'
import type { Account, AccountType, Snapshot } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { MetricsCards } from '../components/dashboard/MetricsCards'
import { TimeRangeSelector } from '../components/dashboard/TimeRangeSelector'
import { TrendChart } from '../components/dashboard/TrendChart'
import { CompositionChart } from '../components/dashboard/CompositionChart'
import { OwnershipChart } from '../components/dashboard/OwnershipChart'
import { MonthlyGrowthChart } from '../components/dashboard/MonthlyGrowthChart'
import { StackedAreaChart, type StackedRow } from '../components/dashboard/StackedAreaChart'
import { AccountTrendChart, type AccountTrendRow } from '../components/dashboard/AccountTrendChart'
import { ChartExportButton } from '../components/dashboard/ChartExportButton'
import { Button } from '../components/ui/Button'

function buildStackedRows(snapshots: Snapshot[], accounts: Account[]): StackedRow[] {
  const map = new Map(accounts.map((a) => [a.id, a.type]))
  return snapshots.map((s) => {
    const row: StackedRow = {
      date: s.date,
      cash: 0,
      deposit: 0,
      fund: 0,
      pension: 0,
      housing_fund: 0,
      credit: 0,
    }
    for (const it of s.items ?? []) {
      const t = map.get(it.account_id)
      if (!t) {
        continue
      }
      row[t] = (row[t] as number) + it.balance
    }
    return row
  })
}

function buildAccountTrend(
  snapshots: Snapshot[],
  accounts: Account[],
  filter: AccountType | 'all',
): { rows: AccountTrendRow[]; keys: string[] } {
  const accs = filter === 'all' ? accounts : accounts.filter((a) => a.type === filter)
  const names = accs.map((a) => a.name)
  const idToName = new Map(accs.map((a) => [a.id, a.name]))
  const rows = snapshots.map((s) => {
    const row: AccountTrendRow = { date: s.date }
    for (const n of names) {
      row[n] = 0
    }
    for (const it of s.items ?? []) {
      const name = idToName.get(it.account_id)
      if (!name) {
        continue
      }
      row[name] = it.balance
    }
    return row
  })
  return { rows, keys: names }
}

export function DashboardPage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [range, setRange] = useState('1y')
  const [accountFilter, setAccountFilter] = useState<AccountType | 'all'>('all')
  const [summary, setSummary] = useState<Awaited<ReturnType<typeof api.getDashboardSummary>> | null>(null)
  const [trend, setTrend] = useState<Awaited<ReturnType<typeof api.getDashboardTrend>>>([])
  const [composition, setComposition] = useState<Awaited<ReturnType<typeof api.getDashboardComposition>> | null>(null)
  const [monthly, setMonthly] = useState<Awaited<ReturnType<typeof api.getMonthlyGrowth>>>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [detailSnapshots, setDetailSnapshots] = useState<Snapshot[]>([])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const year = new Date().getFullYear()
        const [sum, comp, monthlyPoints, accs, snaps, trInitial] = await Promise.all([
          api.getDashboardSummary(),
          api.getDashboardComposition(),
          api.getMonthlyGrowth(year),
          api.listAccounts(),
          api.listSnapshots(),
          api.getDashboardTrend('1y'),
        ])
        if (cancelled) {
          return
        }
        setSummary(sum)
        setComposition(comp)
        setMonthly(monthlyPoints)
        setAccounts(accs)
        setTrend(trInitial)

        const recent = snaps.slice(0, 36)
        const details = await Promise.all(recent.map((s) => api.getSnapshot(s.id)))
        if (cancelled) {
          return
        }
        setDetailSnapshots(details.map((d) => d.snapshot).sort((a, b) => a.date.localeCompare(b.date)))
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : '加载失败')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const tr = await api.getDashboardTrend(range)
        if (!cancelled) {
          setTrend(tr)
        }
      } catch {
        /* ignore */
      }
    })()
    return () => {
      cancelled = true
    }
  }, [range])

  const stackedData = useMemo(
    () => buildStackedRows(detailSnapshots, accounts),
    [detailSnapshots, accounts],
  )

  const accountTrend = useMemo(
    () => buildAccountTrend(detailSnapshots, accounts, accountFilter),
    [detailSnapshots, accounts, accountFilter],
  )

  async function exportCsv() {
    const blob = await api.downloadSnapshotsCsv()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'snapshots.csv'
    a.click()
    URL.revokeObjectURL(url)
  }

  if (loading) {
    return <Loading />
  }

  if (error) {
    return <p className="p-6 text-sm text-red-600">{error}</p>
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="space-y-1">
        <div className="text-xs text-slate-500">资产总览</div>
        <h1 className="text-2xl font-semibold text-slate-900">仪表盘</h1>
      </header>

      <MetricsCards summary={summary} />

      <Card
        title="净资产趋势"
        actions={
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <TimeRangeSelector value={range} onChange={setRange} />
            <Button type="button" variant="secondary" onClick={exportCsv}>
              导出数据(CSV)
            </Button>
          </div>
        }
      >
        <ChartExportButton title="净资产趋势">
          <TrendChart points={trend} />
        </ChartExportButton>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card title="资产构成（类型）">
          {composition ? <CompositionChart data={composition.by_type} /> : null}
        </Card>
        <Card title="资产构成（归属）">
          {composition ? <OwnershipChart data={composition.by_owner} /> : null}
        </Card>
      </div>

      <Card title="月度净资产变化">
        <ChartExportButton title="月度增长">
          <MonthlyGrowthChart points={monthly} />
        </ChartExportButton>
      </Card>

      <Card title="资产堆叠（按类型）">
        <ChartExportButton title="资产堆叠">
          <StackedAreaChart data={stackedData} />
        </ChartExportButton>
      </Card>

      <Card
        title="账户明细趋势"
        actions={
          <select
            className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
            value={accountFilter}
            onChange={(e) => setAccountFilter(e.target.value as AccountType | 'all')}
          >
            <option value="all">全部</option>
            <option value="cash">现金</option>
            <option value="deposit">固收</option>
            <option value="fund">基金</option>
            <option value="pension">养老</option>
            <option value="housing_fund">公积金</option>
            <option value="credit">负债</option>
          </select>
        }
      >
        <ChartExportButton title="账户明细">
          <AccountTrendChart data={accountTrend.rows} keys={accountTrend.keys} />
        </ChartExportButton>
      </Card>
    </div>
  )
}
