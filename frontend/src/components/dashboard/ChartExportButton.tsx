import html2canvas from 'html2canvas'
import { useRef } from 'react'
import { Button } from '../ui/Button'

export function ChartExportButton({ title, children }: { title: string; children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null)

  async function exportPng() {
    if (!ref.current) {
      return
    }
    const canvas = await html2canvas(ref.current, { backgroundColor: '#ffffff', scale: 2 })
    const link = document.createElement('a')
    link.href = canvas.toDataURL('image/png')
    link.download = `${title}.png`
    link.click()
  }

  return (
    <div>
      <div className="mb-2 flex justify-end">
        <Button type="button" variant="secondary" onClick={exportPng}>
          导出图片
        </Button>
      </div>
      <div ref={ref} className="rounded-xl bg-white p-3 ring-1 ring-slate-200">
        {children}
      </div>
    </div>
  )
}
