import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'

export function ProfilePage() {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="space-y-6 pb-6">
      <header>
        <div className="text-xs text-slate-500">账户</div>
        <h1 className="text-2xl font-semibold text-slate-900">我的</h1>
      </header>

      <Card title="资料">
        <div className="space-y-2 text-sm">
          <div>
            <span className="text-slate-500">用户名：</span>
            <span className="font-medium text-slate-900">{user?.username}</span>
          </div>
          <div>
            <span className="text-slate-500">角色：</span>
            <span className="text-slate-900">{user?.is_admin ? '管理员' : '成员'}</span>
          </div>
        </div>
        <div className="mt-4 flex flex-wrap gap-3">
          <Link to="/change-password">
            <Button type="button" variant="secondary">
              修改密码
            </Button>
          </Link>
          <Button
            type="button"
            variant="ghost"
            onClick={async () => {
              await signOut()
              navigate('/login', { replace: true })
            }}
          >
            退出登录
          </Button>
        </div>
      </Card>
    </div>
  )
}
