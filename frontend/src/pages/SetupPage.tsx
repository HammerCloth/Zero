import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'
import { Loading } from '../components/ui/Loading'

export function SetupPage() {
  const { ready, needsSetup, signUp } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  if (!ready) {
    return <Loading label="检查系统状态…" />
  }

  if (!needsSetup) {
    return <Navigate to="/login" replace />
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setPending(true)
    try {
      await signUp(username.trim(), password)
      // 下一帧再跳转，确保 AuthContext 已提交后再进受保护路由
      setTimeout(() => navigate('/dashboard', { replace: true }), 0)
    } catch (err) {
      setError(err instanceof Error ? err.message : '初始化失败')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-lg items-center px-4 py-10">
      <Card title="首次设置管理员">
        <p className="mb-4 text-sm text-slate-600">
          创建第一个账户用于登录系统，并自动初始化默认资产账户。
        </p>
        <form className="space-y-4" onSubmit={onSubmit}>
          <Input label="用户名" value={username} onChange={(e) => setUsername(e.target.value)} required />
          <Input
            label="密码"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            hint="至少 8 位字符"
          />
          {error ? <p className="text-sm text-red-600">{error}</p> : null}
          <Button type="submit" disabled={pending} className="w-full">
            {pending ? '创建中…' : '创建并进入'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
