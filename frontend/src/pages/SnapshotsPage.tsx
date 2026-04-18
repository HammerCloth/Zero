import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as api from '../api/api'
import type { SnapshotListItem } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Button } from '../components/ui/Button'
import { formatCurrency } from '../lib/format'

export function SnapshotsPage() {
  const [loading, setLoading] = useState(true)
  const [items, setItems] = useState<SnapshotListItem[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const list = await api.listSnapshots()
        if (!cancelled) {
          setItems(list)
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
  }, [])

  if (loading) {
    return <Loading />
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-xs text-slate-500">快照</div>
          <h1 className="text-2xl font-semibold text-slate-900">历史记录</h1>
        </div>
        <Link to="/snapshots/new">
          <Button type="button">新建快照</Button>
        </Link>
      </header>

      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      <Card title="快照列表">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="py-2">日期</th>
                <th className="py-2">净资产</th>
                <th className="py-2">备注</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {items.map((s) => (
                <tr key={s.id} className="border-b border-slate-100">
                  <td className="py-3 font-medium text-slate-900">{s.date}</td>
                  <td className="py-3">{formatCurrency(s.net_worth)}</td>
                  <td className="py-3 text-slate-600">{s.note || '—'}</td>
                  <td className="py-3 text-right">
                    <Link className="text-indigo-600" to={`/snapshots/${s.id}`}>
                      查看
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
