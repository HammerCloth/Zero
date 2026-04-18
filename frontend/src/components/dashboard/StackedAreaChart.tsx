import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatCurrency } from '../../lib/format'

const keys = ['cash', 'deposit', 'fund', 'pension', 'housing_fund', 'credit'] as const

const colors: Record<(typeof keys)[number], string> = {
  cash: '#4f46e5',
  deposit: '#10b981',
  fund: '#f59e0b',
  pension: '#ec4899',
  housing_fund: '#06b6d4',
  credit: '#94a3b8',
}

const labels: Record<(typeof keys)[number], string> = {
  cash: '现金',
  deposit: '固收',
  fund: '基金',
  pension: '养老',
  housing_fund: '公积金',
  credit: '负债',
}

export type StackedRow = Record<string, number | string>

export function StackedAreaChart({ data }: { data: StackedRow[] }) {
  return (
    <div className="h-80 w-full min-w-0 overflow-x-auto">
      <ResponsiveContainer width="100%" height="100%" minWidth={360}>
        <AreaChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="date" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `${Math.round(v / 10000)}万`} />
          <Tooltip formatter={(value) => formatCurrency(Number(value))} />
          {keys.map((k) => (
            <Area
              key={k}
              type="monotone"
              dataKey={k}
              stackId="1"
              stroke={colors[k]}
              fill={colors[k]}
              fillOpacity={0.35}
              name={labels[k]}
            />
          ))}
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}
