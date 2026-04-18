import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatCurrency } from '../../lib/format'

export type AccountTrendRow = Record<string, string | number>

export function AccountTrendChart({
  data,
  keys,
}: {
  data: AccountTrendRow[]
  keys: string[]
}) {
  const palette = ['#4f46e5', '#10b981', '#f59e0b', '#ec4899', '#06b6d4', '#64748b', '#a855f7']
  return (
    <div className="h-80 w-full min-w-0 overflow-x-auto">
      <ResponsiveContainer width="100%" height="100%" minWidth={360}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="date" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `${Math.round(v / 10000)}万`} />
          <Tooltip formatter={(value) => formatCurrency(Number(value))} />
          <Legend />
          {keys.map((k, idx) => (
            <Line key={k} type="monotone" dataKey={k} stroke={palette[idx % palette.length]} dot={false} strokeWidth={2} />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
