import { describe, expect, it } from 'vitest'
import { formatCurrency, formatPercent } from './format'

describe('formatCurrency', () => {
  it('formats CNY without decimals', () => {
    expect(formatCurrency(1234)).toMatch(/1[,，]?234/)
    expect(formatCurrency(1234)).toContain('¥')
  })

  it('handles zero', () => {
    expect(formatCurrency(0)).toBeTruthy()
  })
})

describe('formatPercent', () => {
  it('adds plus for non-negative', () => {
    expect(formatPercent(1.5)).toBe('+1.50%')
  })

  it('negative has no plus', () => {
    expect(formatPercent(-2)).toBe('-2.00%')
  })
})
