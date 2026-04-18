import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatCurrency } from '../../lib/format'
import type { MonthlyGrowthPoint } from '../../api/types'

export function MonthlyGrowthChart({ points }: { points: MonthlyGrowthPoint[] }) {
  return (
    <div className="h-72 w-full min-w-0 overflow-x-auto">
      <ResponsiveContainer width="100%" height="100%" minWidth={320}>
        <BarChart data={points}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="month" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `${Math.round(v / 10000)}万`} />
          <Tooltip formatter={(value) => formatCurrency(Number(value))} />
          <Bar dataKey="change" fill="#4f46e5" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
