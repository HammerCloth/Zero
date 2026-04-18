import { useEffect, useState } from 'react'
import * as api from '../api/api'
import type { EventCategory, EventStatRow } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { formatCurrency } from '../lib/format'

const categoryLabels: Record<EventCategory, string> = {
  rent: '房租',
  travel: '旅行',
  medical: '医疗',
  appliance: '家电装修',
  social: '人情往来',
  other: '其他',
}

export function EventStatsPage() {
  const year = new Date().getFullYear()
  const [selectedYear, setSelectedYear] = useState(year)
  const [rows, setRows] = useState<EventStatRow[]>([])
  const [grandTotal, setGrandTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    ;(async () => {
      try {
        const data = await api.getEventStats(selectedYear)
        if (!cancelled) {
          setRows(data.by_category ?? [])
          setGrandTotal(data.grand_total ?? 0)
        }
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
  }, [selectedYear])

  if (loading) {
    return <Loading />
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="text-xs text-slate-500">大事记</div>
          <h1 className="text-2xl font-semibold text-slate-900">年度统计</h1>
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          年份
          <input
            type="number"
            className="w-28 rounded-lg border border-slate-200 px-3 py-2 text-sm"
            value={selectedYear}
            min={2000}
            max={2100}
            onChange={(e) => setSelectedYear(Number(e.target.value) || year)}
          />
        </label>
      </header>

      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      <Card title="按分类汇总">
        <p className="mb-4 text-sm text-slate-600">
          年度支出合计：<span className="font-semibold text-slate-900">{formatCurrency(grandTotal)}</span>
        </p>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px] text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="py-2">分类</th>
                <th className="py-2">笔数</th>
                <th className="py-2">金额</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.category} className="border-b border-slate-100">
                  <td className="py-3 font-medium text-slate-900">{categoryLabels[r.category] ?? r.category}</td>
                  <td className="py-3 text-slate-600">{r.count}</td>
                  <td className="py-3">{formatCurrency(r.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
