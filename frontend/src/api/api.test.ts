import { describe, expect, it } from 'vitest'
import { netWorthFromItems, normalizeCreditBalance } from './api'

describe('netWorthFromItems', () => {
  it('sums balances', () => {
    expect(
      netWorthFromItems([
        { account_id: 'a', balance: 100 },
        { account_id: 'b', balance: -30 },
      ]),
    ).toBe(70)
  })

  it('empty is zero', () => {
    expect(netWorthFromItems([])).toBe(0)
  })
})

describe('normalizeCreditBalance', () => {
  it('passes through non-credit', () => {
    expect(normalizeCreditBalance('cash', 50)).toBe(50)
  })

  it('flips positive credit to negative liability', () => {
    expect(normalizeCreditBalance('credit', 5000)).toBe(-5000)
  })

  it('keeps non-positive credit as-is', () => {
    expect(normalizeCreditBalance('credit', -100)).toBe(-100)
    expect(normalizeCreditBalance('credit', 0)).toBe(0)
  })
})
