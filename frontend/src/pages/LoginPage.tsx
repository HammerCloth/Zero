import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'
import { Loading } from '../components/ui/Loading'

export function LoginPage() {
  const { ready, needsSetup, user, signIn } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  if (!ready) {
    return <Loading />
  }

  if (needsSetup) {
    return <Navigate to="/setup" replace />
  }

  if (user?.must_change_password) {
    return <Navigate to="/change-password" replace />
  }

  if (user) {
    return <Navigate to="/dashboard" replace />
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setPending(true)
    try {
      await signIn(username.trim(), password, remember)
      setTimeout(() => navigate('/dashboard', { replace: true }), 0)
    } catch {
      setError('用户名或密码错误')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-lg items-center px-4 py-10">
      <Card title="登录">
        <form className="space-y-4" onSubmit={onSubmit}>
          <Input label="用户名" autoComplete="username" value={username} onChange={(e) => setUsername(e.target.value)} required />
          <Input
            label="密码"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
            记住我（30 天）
          </label>
          {error ? <p className="text-sm text-red-600">{error}</p> : null}
          <Button type="submit" disabled={pending} className="w-full">
            {pending ? '登录中…' : '登录'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
