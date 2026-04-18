import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import * as api from '../api/api'
import type { Account, AccountOwner, AccountType } from '../api/types'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { Modal } from '../components/ui/Modal'

const typeOptions: { value: AccountType; label: string }[] = [
  { value: 'cash', label: '现金' },
  { value: 'deposit', label: '固收' },
  { value: 'fund', label: '基金' },
  { value: 'pension', label: '养老' },
  { value: 'housing_fund', label: '公积金' },
  { value: 'credit', label: '负债' },
]

const ownerOptions: { value: AccountOwner; label: string }[] = [
  { value: 'A', label: 'A' },
  { value: 'B', label: 'B' },
  { value: 'shared', label: '共同' },
]

export function AccountsPage() {
  const [loading, setLoading] = useState(true)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [error, setError] = useState<string | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [dragId, setDragId] = useState<string | null>(null)

  const [name, setName] = useState('')
  const [type, setType] = useState<AccountType>('cash')
  const [owner, setOwner] = useState<AccountOwner>('shared')

  async function reload() {
    const list = await api.listAccounts()
    setAccounts(list)
  }

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        await reload()
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

  const ordered = useMemo(() => [...accounts].sort((a, b) => a.sort_order - b.sort_order), [accounts])

  function openCreate() {
    setEditing(null)
    setName('')
    setType('cash')
    setOwner('shared')
    setModalOpen(true)
  }

  function openEdit(account: Account) {
    setEditing(account)
    setName(account.name)
    setType(account.type)
    setOwner(account.owner)
    setModalOpen(true)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      if (editing) {
        await api.updateAccount(editing.id, {
          name,
          type,
          owner,
          sort_order: editing.sort_order,
        })
      } else {
        await api.createAccount(name, type, owner)
      }
      setModalOpen(false)
      await reload()
    } catch (e) {
      setError(e instanceof Error ? e.message : '保存失败')
    }
  }

  async function onDeactivate(id: string) {
    if (!confirm('确定停用该账户？历史快照仍会保留。')) {
      return
    }
    await api.deactivateAccount(id)
    await reload()
  }

  async function onDrop(targetId: string) {
    if (!dragId || dragId === targetId) {
      return
    }
    const ids = ordered.map((a) => a.id)
    const from = ids.indexOf(dragId)
    const to = ids.indexOf(targetId)
    if (from < 0 || to < 0) {
      return
    }
    const next = [...ids]
    next.splice(from, 1)
    next.splice(to, 0, dragId)
    await api.reorderAccounts(next)
    setDragId(null)
    await reload()
  }

  if (loading) {
    return <Loading />
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-xs text-slate-500">账户</div>
          <h1 className="text-2xl font-semibold text-slate-900">账户管理</h1>
        </div>
        <Button type="button" onClick={openCreate}>
          新建账户
        </Button>
      </header>

      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      <Card title="账户列表（拖拽排序）">
        <div className="space-y-2">
          {ordered.map((a) => (
            <div
              key={a.id}
              draggable
              onDragStart={() => setDragId(a.id)}
              onDragOver={(e) => e.preventDefault()}
              onDrop={() => onDrop(a.id)}
              className="flex items-center justify-between gap-3 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2"
            >
              <div>
                <div className="font-medium text-slate-900">{a.name}</div>
                <div className="text-xs text-slate-500">
                  {a.type} · {a.owner} · 排序 {a.sort_order}
                </div>
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="secondary" onClick={() => openEdit(a)}>
                  编辑
                </Button>
                <Button type="button" variant="ghost" onClick={() => onDeactivate(a.id)}>
                  停用
                </Button>
              </div>
            </div>
          ))}
        </div>
        <p className="mt-3 text-xs text-slate-500">提示：拖拽左侧行到目标位置即可调整排序。</p>
      </Card>

      <Modal open={modalOpen} title={editing ? '编辑账户' : '新建账户'} onClose={() => setModalOpen(false)}>
        <form className="space-y-4" onSubmit={onSubmit}>
          <Input label="名称" value={name} onChange={(e) => setName(e.target.value)} required />
          <label className="block text-sm text-slate-600">
            类型
            <select
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
              value={type}
              onChange={(e) => setType(e.target.value as AccountType)}
            >
              {typeOptions.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm text-slate-600">
            归属
            <select
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
              value={owner}
              onChange={(e) => setOwner(e.target.value as AccountOwner)}
            >
              {ownerOptions.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>
              取消
            </Button>
            <Button type="submit">保存</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
