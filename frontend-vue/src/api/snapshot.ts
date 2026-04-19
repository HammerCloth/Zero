import type { SnapshotDetail, SnapshotListItem } from '@/types/models'
import http from './http'

export interface SnapshotItemInput {
  accountId: string
  balance: number
}

export interface SnapshotEventInput {
  category: string
  description: string
  amount: number
}

export async function listSnapshots() {
  const { data } = await http.get<{ snapshots: SnapshotListItem[] }>('/api/v1/snapshots')
  return data.snapshots
}

export async function latestSnapshot() {
  const { data } = await http.get<{ snapshot: SnapshotDetail | null }>('/api/v1/snapshots/latest')
  return data.snapshot
}

export async function getSnapshot(id: string) {
  const { data } = await http.get<{ snapshot: SnapshotDetail }>(`/api/v1/snapshots/${id}`)
  return data.snapshot
}

export async function createSnapshot(body: {
  date: string
  note?: string | null
  items: SnapshotItemInput[]
  events: SnapshotEventInput[]
}) {
  const { data } = await http.post<{ snapshot: SnapshotDetail }>('/api/v1/snapshots', body)
  return data.snapshot
}

export async function updateSnapshot(
  id: string,
  body: {
    date: string
    note?: string | null
    items: SnapshotItemInput[]
    events: SnapshotEventInput[]
  },
) {
  const { data } = await http.put<{ snapshot: SnapshotDetail }>(`/api/v1/snapshots/${id}`, body)
  return data.snapshot
}

export async function deleteSnapshot(id: string) {
  await http.delete(`/api/v1/snapshots/${id}`)
}

export async function snapshotForDate(date: string) {
  const { data } = await http.get<{ snapshot: { id: string; date: string } | null }>(
    '/api/v1/snapshots/for-date',
    { params: { date } },
  )
  return data.snapshot
}

export async function snapshotDatesInRange(from: string, to: string) {
  const { data } = await http.get<{ dates: string[] }>('/api/v1/snapshots/dates-in-range', {
    params: { from, to },
  })
  return data.dates
}
