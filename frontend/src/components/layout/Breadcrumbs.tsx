import { Link, useMatches } from 'react-router-dom'

export type RouteCrumb = {
  crumb?: string
}

export function Breadcrumbs() {
  const matches = useMatches() as Array<{ id: string; pathname: string; handle: unknown }>

  const crumbs = matches
    .map((m) => {
      const h = m.handle as RouteCrumb | undefined
      return h?.crumb ? { label: h.crumb, to: m.pathname } : null
    })
    .filter((x): x is { label: string; to: string } => x !== null)

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
