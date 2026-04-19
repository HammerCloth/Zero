import type { Account } from '@/types/models'
import http from './http'

export async function listAccounts() {
  const { data } = await http.get<{ accounts: Account[] }>('/api/v1/accounts')
  return data.accounts
}

export async function createAccount(body: { name: string; type: string; owner: string }) {
  const { data } = await http.post<{ account: Account }>('/api/v1/accounts', body)
  return data.account
}

export async function updateAccount(id: string, body: { name: string; type: string; owner: string }) {
  await http.put(`/api/v1/accounts/${id}`, body)
}

export async function deactivateAccount(id: string) {
  await http.delete(`/api/v1/accounts/${id}`)
}

export async function reorderAccounts(accountIds: string[]) {
  await http.put('/api/v1/accounts/reorder', { accountIds })
}
