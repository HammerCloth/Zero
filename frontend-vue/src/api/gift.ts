import type { GiftRecipient, GiftRecord } from '@/types/models'
import http from './http'

export interface GiftStats {
  year: number
  grandTotal: number
  count: number
  recipientCount: number
  byOccasion: Record<string, number>
  countByOccasion: Record<string, number>
}

export type RecipientBody = { name: string; relationship?: string | null; note?: string | null }
export type GiftBody = {
  recipientId: string
  occasion: string
  giftDate: string
  amount: number | null
  paymentMethod?: string | null
  note?: string | null
}

export async function listRecipients(includeInactive = false) {
  const { data } = await http.get<{ recipients: GiftRecipient[] }>('/api/v1/gifts/recipients', {
    params: { includeInactive },
  })
  return data.recipients
}

export async function createRecipient(body: RecipientBody) {
  const { data } = await http.post<{ recipient: GiftRecipient }>('/api/v1/gifts/recipients', body)
  return data.recipient
}

export async function updateRecipient(id: string, body: RecipientBody) {
  await http.put(`/api/v1/gifts/recipients/${id}`, body)
}

export async function deactivateRecipient(id: string) {
  await http.delete(`/api/v1/gifts/recipients/${id}`)
}

export async function listRecords(params: { year?: number; recipientId?: string; keyword?: string } = {}) {
  const { data } = await http.get<{ records: GiftRecord[] }>('/api/v1/gifts', { params })
  return data.records
}

export async function createRecord(body: GiftBody) {
  const { data } = await http.post<{ record: GiftRecord }>('/api/v1/gifts', body)
  return data.record
}

export async function updateRecord(id: string, body: GiftBody) {
  await http.put(`/api/v1/gifts/${id}`, body)
}

export async function deleteRecord(id: string) {
  await http.delete(`/api/v1/gifts/${id}`)
}

export async function giftStats(year: number) {
  const { data } = await http.get<GiftStats>('/api/v1/gifts/stats', { params: { year } })
  return data
}
