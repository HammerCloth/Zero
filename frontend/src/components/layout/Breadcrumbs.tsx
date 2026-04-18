import { Link, useLocation } from 'react-router-dom'

/** 与 BrowserRouter 兼容：不使用 useMatches（需 Data Router） */
function buildCrumbs(pathname: string): { label: string; to: string }[] {
  if (pathname === '/dashboard') {
    return [{ label: '总览', to: '/dashboard' }]
  }
  if (pathname === '/accounts') {
    return [{ label: '账户', to: '/accounts' }]
  }
  if (pathname === '/profile') {
    return [{ label: '我的', to: '/profile' }]
  }
  if (pathname === '/events/stats') {
    return [{ label: '大事记统计', to: '/events/stats' }]
  }
  if (pathname === '/users') {
    return [{ label: '用户管理', to: '/users' }]
  }

  if (pathname === '/snapshots') {
    return [{ label: '快照', to: '/snapshots' }]
  }
  if (pathname === '/snapshots/new') {
    return [
      { label: '快照', to: '/snapshots' },
      { label: '新建快照', to: '/snapshots/new' },
    ]
  }

  if (/^\/snapshots\/[^/]+\/edit$/.test(pathname)) {
    return [
      { label: '快照', to: '/snapshots' },
      { label: '编辑快照', to: pathname },
    ]
  }

  if (/^\/snapshots\/[^/]+$/.test(pathname)) {
    return [
      { label: '快照', to: '/snapshots' },
      { label: '详情', to: pathname },
    ]
  }

  return []
}

export function Breadcrumbs() {
  const { pathname } = useLocation()
  const crumbs = buildCrumbs(pathname)

  if (crumbs.length === 0) {
    return null
  }

  return (
    <nav aria-label="面包屑" className="mb-4 text-sm text-slate-500">
      <ol className="flex flex-wrap items-center gap-1.5">
        <li>
          <Link to="/dashboard" className="hover:text-indigo-600">
            首页
          </Link>
        </li>
        {crumbs.map((c, i) => (
          <li key={`${c.to}-${i}`} className="flex items-center gap-1.5">
            <span className="text-slate-300" aria-hidden>
              /
            </span>
            {i < crumbs.length - 1 ? (
              <Link to={c.to} className="hover:text-indigo-600">
                {c.label}
              </Link>
            ) : (
              <span className="font-medium text-slate-700">{c.label}</span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  )
}
