import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import * as api from '../api/api'
import type { Account, SnapshotEvent } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { EventForm } from '../components/snapshot/EventForm'
import { formatCurrency } from '../lib/format'

export function SnapshotFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [loading, setLoading] = useState(true)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [balances, setBalances] = useState<Record<string, number>>({})
  const [prevBalances, setPrevBalances] = useState<Record<string, number>>({})
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [note, setNote] = useState('')
  const [events, setEvents] = useState<SnapshotEvent[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const accs = await api.listAccounts()
        if (cancelled) {
          return
        }
        setAccounts(accs)

        if (isEdit && id) {
          const detail = await api.getSnapshot(id)
          const snap = detail.snapshot
          setDate(snap.date)
          setNote(snap.note ?? '')
          const map: Record<string, number> = {}
          for (const it of snap.items ?? []) {
            map[it.account_id] = it.balance
          }
          for (const a of accs) {
            if (map[a.id] === undefined) {
              map[a.id] = 0
            }
          }
          setBalances(map)
          setEvents(
            (snap.events ?? []).map((e) => ({
              category: e.category,
              description: e.description,
              amount: e.amount > 0 ? -e.amount : e.amount,
            })),
          )
        } else {
          const latest = await api.getLatestSnapshot()
          const map: Record<string, number> = {}
          const prev: Record<string, number> = {}
          for (const a of accs) {
            map[a.id] = 0
            prev[a.id] = 0
          }
          if (latest.snapshot) {
            for (const it of latest.snapshot.items ?? []) {
              map[it.account_id] = it.balance
              prev[it.account_id] = it.balance
            }
          }
          setBalances(map)
          setPrevBalances(prev)
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
  }, [id, isEdit])

  const netWorth = useMemo(() => {
    const items = accounts.map((a) => ({
      account_id: a.id,
      balance: api.normalizeCreditBalance(a.type, balances[a.id] ?? 0),
    }))
    return api.netWorthFromItems(items)
  }, [accounts, balances])

  const prevNetWorth = useMemo(() => {
    const items = accounts.map((a) => ({
      account_id: a.id,
      balance: api.normalizeCreditBalance(a.type, prevBalances[a.id] ?? 0),
    }))
    return api.netWorthFromItems(items)
  }, [accounts, prevBalances])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const payloadItems = accounts.map((a) => ({
        account_id: a.id,
        balance: api.normalizeCreditBalance(a.type, balances[a.id] ?? 0),
      }))
      const payloadEvents = events.map((ev) => ({
        category: ev.category,
        description: ev.description,
        amount: Math.abs(ev.amount),
      }))

      if (isEdit && id) {
        await api.updateSnapshot(id, {
          date,
          note,
          items: payloadItems,
          events: payloadEvents,
        })
        navigate(`/snapshots/${id}`, { replace: true })
      } else {
        const created = await api.createSnapshot({
          date,
          note,
          items: payloadItems,
          events: payloadEvents,
        })
        navigate(`/snapshots/${created.snapshot.id}`, { replace: true })
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败')
    }
  }

  if (loading) {
    return <Loading />
  }

  return (
    <div className="space-y-6 pb-6">
      <header>
        <div className="text-xs text-slate-500">快照</div>
        <h1 className="text-2xl font-semibold text-slate-900">{isEdit ? '编辑快照' : '新建快照'}</h1>
      </header>

      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      <form className="space-y-6" onSubmit={onSubmit}>
        <Card title="基本信息">
          <div className="grid gap-4 md:grid-cols-2">
            <Input type="date" label="日期" value={date} onChange={(e) => setDate(e.target.value)} required />
            <Input label="备注（可选）" value={note} onChange={(e) => setNote(e.target.value)} />
          </div>
        </Card>

        <Card title="账户余额">
          <div className="space-y-3">
            {accounts.map((a) => {
              const prev = prevBalances[a.id]
              const cur = balances[a.id] ?? 0
              const delta = prev === undefined ? null : cur - prev
              return (
                <div key={a.id} className="grid gap-3 md:grid-cols-3 md:items-end">
                  <div>
                    <div className="text-sm font-medium text-slate-900">{a.name}</div>
                    <div className="text-xs text-slate-500">
                      {a.type} · {a.owner}
                    </div>
                  </div>
                  <Input
                    label={a.type === 'credit' ? '待还金额（填正数，将存为负债）' : '余额'}
                    inputMode="decimal"
                    value={String(balances[a.id] ?? '')}
                    onChange={(e) => {
                      const raw = e.target.value
                      const next = raw === '' ? 0 : Number(raw)
                      setBalances((prevMap) => ({ ...prevMap, [a.id]: Number.isNaN(next) ? 0 : next }))
                    }}
                  />
                  <div className="text-sm text-slate-600">
                    {delta === null ? (
                      <span>较上次：—</span>
                    ) : (
                      <span>
                        较上次：
                        <span className={delta >= 0 ? 'text-emerald-600' : 'text-red-600'}>
                          {delta >= 0 ? '+' : ''}
                          {formatCurrency(delta)}
                        </span>
                      </span>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </Card>

        <Card title="大事记（可选）">
          <EventForm
            events={events}
            onChange={(next) =>
              setEvents(
                next.map((ev) => ({
                  ...ev,
                  amount: ev.amount > 0 ? -Math.abs(ev.amount) : ev.amount,
                })),
              )
            }
          />
        </Card>

        <Card title="预览">
          <div className="grid gap-3 md:grid-cols-2">
            <div>
              <div className="text-xs text-slate-500">当前净资产</div>
              <div className="text-2xl font-semibold text-slate-900">{formatCurrency(netWorth)}</div>
            </div>
            {!isEdit ? (
              <div>
                <div className="text-xs text-slate-500">较上次净资产变化</div>
                <div className={`text-2xl font-semibold ${netWorth - prevNetWorth >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                  {formatCurrency(netWorth - prevNetWorth)}
                </div>
              </div>
            ) : null}
          </div>
        </Card>

        <div className="flex flex-wrap gap-3">
          <Button type="submit">{isEdit ? '保存' : '创建'}</Button>
          <Button type="button" variant="secondary" onClick={() => navigate(-1)}>
            返回
          </Button>
        </div>
      </form>
    </div>
  )
}
