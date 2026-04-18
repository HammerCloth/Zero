import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import * as api from '../api/api'
import type { User } from '../api/types'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'
import { Loading } from '../components/ui/Loading'
import { Modal } from '../components/ui/Modal'

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [resetFor, setResetFor] = useState<User | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isAdmin, setIsAdmin] = useState(false)
  const [resetPassword, setResetPassword] = useState('')

  async function load() {
    const list = await api.listUsers()
    setUsers(list)
  }

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        await load()
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

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await api.createUser(username.trim(), password, isAdmin)
      setCreateOpen(false)
      setUsername('')
      setPassword('')
      setIsAdmin(false)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建失败')
    }
  }

  async function onReset(e: FormEvent) {
    e.preventDefault()
    if (!resetFor) {
      return
    }
    setError(null)
    try {
      await api.resetUserPassword(resetFor.id, resetPassword)
      setResetFor(null)
      setResetPassword('')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '重置失败')
    }
  }

  if (loading) {
    return <Loading />
  }

  return (
    <div className="space-y-6 pb-6">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-xs text-slate-500">管理</div>
          <h1 className="text-2xl font-semibold text-slate-900">用户</h1>
        </div>
        <Button type="button" onClick={() => setCreateOpen(true)}>
          新建用户
        </Button>
      </header>

      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      <Card title="用户列表">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px] text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="py-2">用户名</th>
                <th className="py-2">角色</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="border-b border-slate-100">
                  <td className="py-3 font-medium text-slate-900">{u.username}</td>
                  <td className="py-3 text-slate-600">{u.is_admin ? '管理员' : '成员'}</td>
                  <td className="py-3 text-right">
                    <Button type="button" variant="secondary" onClick={() => setResetFor(u)}>
                      重置密码
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      <Modal open={createOpen} title="新建用户" onClose={() => setCreateOpen(false)}>
        <form className="space-y-4" onSubmit={onCreate}>
          <Input label="用户名" value={username} onChange={(e) => setUsername(e.target.value)} required />
          <Input
            label="初始密码"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input type="checkbox" checked={isAdmin} onChange={(e) => setIsAdmin(e.target.checked)} />
            管理员
          </label>
          <Button type="submit" className="w-full">
            创建
          </Button>
        </form>
      </Modal>

      <Modal
        open={Boolean(resetFor)}
        title={resetFor ? `重置密码：${resetFor.username}` : '重置密码'}
        onClose={() => {
          setResetFor(null)
          setResetPassword('')
        }}
      >
        <form className="space-y-4" onSubmit={onReset}>
          <Input
            label="新密码"
            type="password"
            value={resetPassword}
            onChange={(e) => setResetPassword(e.target.value)}
            required
            minLength={8}
          />
          <Button type="submit" className="w-full">
            保存
          </Button>
        </form>
      </Modal>
    </div>
  )
}
