import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { formatCurrency } from '../../lib/format'

const COLORS = ['#4f46e5', '#10b981', '#f59e0b', '#ec4899', '#06b6d4', '#64748b']

const typeLabel: Record<string, string> = {
  cash: '现金',
  deposit: '固收',
  fund: '基金',
  pension: '养老',
  housing_fund: '公积金',
  credit: '负债',
}

export function CompositionChart({ data }: { data: Record<string, number> }) {
  const chartData = Object.entries(data)
    .map(([key, value]) => ({
      name: typeLabel[key] ?? key,
      value: Math.abs(value),
      raw: value,
    }))
    .filter((d) => d.value > 0)

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={2}>
            {chartData.map((_, index) => (
              <Cell key={index} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip
            formatter={(_value, _name, item) =>
              formatCurrency(Number((item?.payload as { raw: number } | undefined)?.raw ?? 0))
            }
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
