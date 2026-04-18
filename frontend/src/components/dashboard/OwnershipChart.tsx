import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { formatCurrency } from '../../lib/format'

const COLORS = ['#4f46e5', '#10b981', '#f59e0b']

const ownerLabel: Record<string, string> = {
  A: 'A',
  B: 'B',
  shared: '共同',
}

export function OwnershipChart({ data }: { data: Record<string, number> }) {
  const chartData = Object.entries(data)
    .map(([key, value]) => ({
      name: ownerLabel[key] ?? key,
      value: Math.max(value, 0),
    }))
    .filter((d) => d.value > 0)

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={45} outerRadius={75} paddingAngle={2}>
            {chartData.map((_, index) => (
              <Cell key={index} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => formatCurrency(Number(value))} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
