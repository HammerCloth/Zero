import { Button } from '../ui/Button'

const options: { key: string; label: string }[] = [
  { key: '3m', label: '近3月' },
  { key: '6m', label: '近6月' },
  { key: '1y', label: '近1年' },
  { key: '2y', label: '近2年' },
  { key: 'all', label: '全部' },
]

export function TimeRangeSelector({
  value,
  onChange,
}: {
  value: string
  onChange: (v: string) => void
}) {
  return (
    <div className="flex flex-wrap gap-2">
      {options.map((opt) => (
        <Button
          key={opt.key}
          type="button"
          variant={value === opt.key ? 'primary' : 'secondary'}
          onClick={() => onChange(opt.key)}
        >
          {opt.label}
        </Button>
      ))}
    </div>
  )
}
