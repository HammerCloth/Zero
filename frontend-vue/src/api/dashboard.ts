import type { DashboardSummary } from '@/types/models'
import http from './http'

export interface MonthlyGrowthPoint {
  month: string
  change: number
  cumulativeChange: number
}

export async function fetchSummary() {
  const { data } = await http.get<DashboardSummary>('/api/v1/dashboard/summary')
  return data
}

export async function fetchTrend(range?: string) {
  const { data } = await http.get<{ points: { date: string; netWorth: number }[] }>(
    '/api/v1/dashboard/trend',
    { params: { range } },
  )
  return data
}

export async function fetchComposition() {
  const { data } = await http.get<{
    byType: Record<string, number>
    byOwner: Record<string, number>
  }>('/api/v1/dashboard/composition')
  return data
}

export async function fetchMonthlyGrowth(year?: number) {
  const { data } = await http.get<{
    year: number
    points: MonthlyGrowthPoint[]
  }>('/api/v1/dashboard/monthly-growth', { params: { year } })
  return data
}

export async function fetchStackedByType(range?: string) {
  const { data } = await http.get<{
    points: { date: string; byType: Record<string, number> }[]
  }>('/api/v1/dashboard/stacked-by-type', { params: { range } })
  return data
}

export interface AccountTrendSeries {
  accountId: string
  name: string
  type: string
  points: { date: string; balance: number }[]
}

export async function fetchAccountTrends(range?: string) {
  const { data } = await http.get<{ accounts: AccountTrendSeries[] }>(
    '/api/v1/dashboard/account-trends',
    { params: { range } },
  )
  return data
}
