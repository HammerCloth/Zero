import http from './http'

export interface EventStatsResponse {
  year: number
  byCategory: Record<string, number>
  grandTotal: number
  countByCategory: Record<string, number>
}

export async function eventStats(year?: number) {
  const { data } = await http.get<EventStatsResponse>('/api/v1/events/stats', { params: { year } })
  return data
}
