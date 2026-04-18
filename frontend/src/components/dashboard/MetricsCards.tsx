import { formatCurrency, formatPercent } from '../../lib/format'
import type { DashboardSummary } from '../../api/types'

export function MetricsCards({ summary }: { summary: DashboardSummary | null }) {
  if (!summary) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-24 animate-pulse rounded-xl bg-slate-100" />
        ))}
      </div>
    )
  }

  const prevNet = summary.net_worth - summary.monthly_change
  const monthlyPct = prevNet !== 0 ? (summary.monthly_change / prevNet) * 100 : 0

  const cards = [
    { label: '当前净资产', value: formatCurrency(summary.net_worth), tone: 'text-slate-900' },
    {
      label: '本期变化',
      value: formatCurrency(summary.monthly_change),
      sub: formatPercent(monthlyPct),
      tone: summary.monthly_change >= 0 ? 'text-emerald-600' : 'text-red-600',
    },
    {
      label: '年初至今',
      value: formatCurrency(summary.annual_change),
      tone: summary.annual_change >= 0 ? 'text-emerald-600' : 'text-red-600',
    },
    { label: '年化收益率（估算）', value: formatPercent(summary.annualized_return), tone: 'text-indigo-700' },
  ]

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {cards.map((c) => (
        <article key={c.label} className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <p className="text-xs text-slate-500">{c.label}</p>
          <p className={`mt-2 text-xl font-semibold ${c.tone}`}>{c.value}</p>
          {c.sub ? <p className="mt-1 text-xs text-slate-500">{c.sub}</p> : null}
        </article>
      ))}
    </div>
  )
}
