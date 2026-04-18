import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { Breadcrumbs } from './Breadcrumbs'
import { Button } from '../ui/Button'

const nav = [
  { to: '/dashboard', label: '总览', icon: '📊' },
  { to: '/snapshots', label: '快照', icon: '📝' },
  { to: '/accounts', label: '账户', icon: '🏦' },
  { to: '/events/stats', label: '大事记', icon: '📌' },
  { to: '/profile', label: '我的', icon: '👤' },
]

export function AppLayout() {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-slate-50 pb-20 md:pb-0">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-4 md:flex-row md:px-6">
        <aside className="hidden w-56 shrink-0 md:block">
          <div className="sticky top-4 rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
            <div className="mb-4">
              <div className="text-xs text-slate-500">Project Zero</div>
              <div className="text-sm font-semibold text-slate-900">{user?.username}</div>
            </div>
            <nav className="flex flex-col gap-1">
              {nav.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2 text-sm ${
                      isActive ? 'bg-indigo-50 text-indigo-700' : 'text-slate-700 hover:bg-slate-50'
                    }`
                  }
                >
                  <span className="mr-2">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
              {user?.is_admin ? (
                <NavLink
                  to="/users"
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2 text-sm ${
                      isActive ? 'bg-indigo-50 text-indigo-700' : 'text-slate-700 hover:bg-slate-50'
                    }`
                  }
                >
                  <span className="mr-2">🛡️</span>
                  用户管理
                </NavLink>
              ) : null}
            </nav>
            <div className="mt-4 border-t border-slate-100 pt-4">
              <Button
                variant="ghost"
                className="w-full justify-start"
                onClick={async () => {
                  await signOut()
                  navigate('/login', { replace: true })
                }}
              >
                退出登录
              </Button>
            </div>
          </div>
        </aside>

        <main className="min-w-0 flex-1">
          <Breadcrumbs />
          <Outlet />
        </main>
      </div>

      <nav className="fixed bottom-0 left-0 right-0 z-40 border-t border-slate-200 bg-white/95 px-2 py-2 backdrop-blur md:hidden">
        <div className="mx-auto flex max-w-lg items-center justify-between">
          {nav.slice(0, 4).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex flex-1 flex-col items-center justify-center rounded-lg px-4 py-2 text-xs ${
                  isActive ? 'text-indigo-600' : 'text-slate-700'
                }`
              }
            >
              <span className="text-lg">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `flex flex-1 flex-col items-center justify-center rounded-lg px-4 py-2 text-xs ${
                isActive ? 'text-indigo-600' : 'text-slate-700'
              }`
            }
          >
            <span className="text-lg">👤</span>
            <span>我的</span>
          </NavLink>
        </div>
      </nav>
    </div>
  )
}
