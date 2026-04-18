import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import * as api from '../api/api'
import type { Account, Snapshot } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Button } from '../components/ui/Button'
import { formatCurrency } from '../lib/format'

export function SnapshotDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [snapshot, setSnapshot] = useState<Snapshot | null>(null)
  const [netWorth, setNetWorth] = useState(0)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      if (!id) {
        return
      }
      try {
        const [detail, accs] = await Promise.all([api.getSnapshot(id), api.listAccounts()])
        if (cancelled) {
          return
        }
        setSnapshot(detail.snapshot)
        setNetWorth(detail.net_worth)
        setAccounts(accs)
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
  }, [id])

  const accountName = useMemo(() => {
    const map = new Map(accounts.map((a) => [a.id, a.name]))
    return (accountId: string) => map.get(accountId) ?? accountId
  }, [accounts])

  const eventTotal = useMemo(() => {
    return (snapshot?.events ?? []).reduce((sum, e) => sum + e.amount, 0)
  }, [snapshot])

  async function onDelete() {
    if (!id) {
      return
    }
    if (!confirm('确定删除该快照？')) {
      return
    }
    await api.deleteSnapshot(id)
    navigate('/snapshots', { replace: true })
  }

  if (loading) {
    return <Loading />
  }

  if (error || !snapshot) {
    return <p className="p-6 text-sm text-red-600">{error ?? '未找到快照'}</p>
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="text-xs text-slate-500">快照</div>
          <h1 className="text-2xl font-semibold text-slate-900">{snapshot.date}</h1>
          <p className="mt-1 text-sm text-slate-600">净资产 {formatCurrency(netWorth)}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link to={`/snapshots/${snapshot.id}/edit`}>
            <Button type="button" variant="secondary">
              编辑
            </Button>
          </Link>
          <Button type="button" variant="ghost" onClick={onDelete}>
            删除
          </Button>
        </div>
      </header>

      <Card title="账户余额">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[520px] text-sm">
            <tbody>
              {(snapshot.items ?? []).map((it) => (
                <tr key={it.id ?? it.account_id} className="border-b border-slate-100">
                  <td className="py-2">{accountName(it.account_id)}</td>
                  <td className="py-2 text-right">{formatCurrency(it.balance)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      <Card title="大事记">
        {(snapshot.events ?? []).length === 0 ? (
          <p className="text-sm text-slate-500">暂无记录</p>
        ) : (
          <div className="space-y-2">
            {(snapshot.events ?? []).map((e) => (
              <div key={e.id ?? `${e.category}-${e.description}`} className="flex justify-between gap-3 text-sm">
                <div>
                  <span className="font-medium text-slate-900">{e.category}</span>
                  <span className="text-slate-600"> · {e.description}</span>
                </div>
                <div className="text-slate-900">{formatCurrency(e.amount)}</div>
              </div>
            ))}
            <div className="border-t border-slate-100 pt-2 text-sm text-slate-600">
              大额支出合计：{formatCurrency(eventTotal)}
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}
