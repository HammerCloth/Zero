export function formatCurrency(n: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 0,
  }).format(n)
}

export function formatPercent(n: number) {
  return `${n >= 0 ? '+' : ''}${n.toFixed(2)}%`
}
