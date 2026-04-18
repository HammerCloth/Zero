import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import * as api from '../api/api'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'

export function ChangePasswordPage() {
  const { user, refreshUser } = useAuth()
  const navigate = useNavigate()
  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setPending(true)
    try {
      await api.changePassword(current, next)
      await refreshUser()
      navigate('/dashboard', { replace: true })
    } catch {
      setError('请检查当前密码是否符合要求（新密码至少 8 位）')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-8">
      <Card title="修改密码">
        <p className="mb-4 text-sm text-slate-600">
          {user?.must_change_password ? '首次登录需要修改密码后才能继续使用。' : '更新登录密码。'}
        </p>
        <form className="space-y-4" onSubmit={onSubmit}>
          <Input label="当前密码" type="password" value={current} onChange={(e) => setCurrent(e.target.value)} required />
          <Input
            label="新密码"
            type="password"
            value={next}
            onChange={(e) => setNext(e.target.value)}
            required
            minLength={8}
          />
          {error ? <p className="text-sm text-red-600">{error}</p> : null}
          <Button type="submit" disabled={pending} className="w-full">
            {pending ? '保存中…' : '保存'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
