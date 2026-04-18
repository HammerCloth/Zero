import { useState } from 'react'
import type { EventCategory, SnapshotEvent } from '../../api/types'
import { formatCurrency } from '../../lib/format'
import { Button } from '../ui/Button'
import { Input } from '../ui/Input'

const categories: { value: EventCategory; label: string }[] = [
  { value: 'rent', label: '房租' },
  { value: 'travel', label: '旅行' },
  { value: 'medical', label: '医疗' },
  { value: 'appliance', label: '家电装修' },
  { value: 'social', label: '人情往来' },
  { value: 'other', label: '其他' },
]

const categoryLabel = (c: EventCategory) => categories.find((x) => x.value === c)?.label ?? c

export function EventForm({
  events,
  onChange,
}: {
  events: SnapshotEvent[]
  onChange: (next: SnapshotEvent[]) => void
}) {
  const [category, setCategory] = useState<EventCategory>('travel')
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')

  function add() {
    const value = Number(amount)
    if (!description.trim() || Number.isNaN(value) || value <= 0) {
      return
    }
    onChange([
      ...events,
      {
        category,
        description: description.trim(),
        amount: value,
      },
    ])
    setDescription('')
    setAmount('')
  }

  function remove(idx: number) {
    onChange(events.filter((_, i) => i !== idx))
  }

  return (
    <div className="space-y-3">
      <div className="grid gap-3 md:grid-cols-3">
        <label className="block text-sm text-slate-600">
          分类
          <select
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
            value={category}
            onChange={(e) => setCategory(e.target.value as EventCategory)}
          >
            {categories.map((c) => (
              <option key={c.value} value={c.value}>
                {c.label}
              </option>
            ))}
          </select>
        </label>
        <Input label="说明" value={description} onChange={(e) => setDescription(e.target.value)} />
        <Input label="金额（支出，正数）" value={amount} onChange={(e) => setAmount(e.target.value)} inputMode="decimal" />
      </div>
      <Button type="button" variant="secondary" onClick={add}>
        添加大事记
      </Button>

      <div className="space-y-2">
        {events.map((e, idx) => (
          <div key={`${e.category}-${idx}`} className="flex items-center justify-between gap-3 rounded-lg bg-slate-50 px-3 py-2 text-sm">
            <div>
              <span className="font-medium">{categoryLabel(e.category)}</span>
              <span className="text-slate-600"> · {e.description}</span>
            </div>
            <div className="flex items-center gap-3">
              <span>-{formatCurrency(Math.abs(e.amount))}</span>
              <Button type="button" variant="ghost" onClick={() => remove(idx)}>
                移除
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
