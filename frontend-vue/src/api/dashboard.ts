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

/** 最新快照：各账户类型下，账户 id → 有效余额（与 byType 聚合口径一致） */
export type CompositionByTypeAccounts = Record<string, Record<string, number>>

export interface DashboardComposition {
  byType: Record<string, number>
  byOwner: Record<string, number>
  /** 新字段；旧后端可能缺失 */
  byTypeAccounts?: CompositionByTypeAccounts
}

export async function fetchComposition() {
  const { data } = await http.get<DashboardComposition>('/api/v1/dashboard/composition')
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
