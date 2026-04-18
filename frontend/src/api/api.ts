import { api, setAccessToken } from './client'
import type {
  Account,
  AccountOwner,
  AccountType,
  Composition,
  DashboardSummary,
  EventCategory,
  EventStatRow,
  MonthlyGrowthPoint,
  Snapshot,
  SnapshotItem,
  SnapshotListItem,
  TrendPoint,
  User,
} from './types'

export async function getAuthStatus() {
  const { data } = await api.get<{ needs_setup: boolean }>('/api/v1/auth/status')
  return data.needs_setup
}

export async function setupAdmin(username: string, password: string) {
  const { data } = await api.post<{ user: User; access_token: string }>(
    '/api/v1/auth/setup',
    { username, password },
  )
  setAccessToken(data.access_token)
  return data
}

export async function login(username: string, password: string, rememberMe: boolean) {
  const { data } = await api.post<{ user: User; access_token: string }>(
    '/api/v1/auth/login',
    { username, password, remember_me: rememberMe },
  )
  setAccessToken(data.access_token)
  return data
}

export async function logout() {
  await api.post('/api/v1/auth/logout')
  setAccessToken(null)
}

export async function fetchMe() {
  const { data } = await api.get<User>('/api/v1/auth/me')
  return data
}

export async function changePassword(currentPassword: string, newPassword: string) {
  await api.put('/api/v1/auth/password', {
    current_password: currentPassword,
    new_password: newPassword,
  })
}

export async function listUsers() {
  const { data } = await api.get<{ users: User[] }>('/api/v1/users')
  return data.users
}

export async function createUser(username: string, password: string, isAdmin: boolean) {
  const { data } = await api.post<{ user: User }>('/api/v1/users', {
    username,
    password,
    is_admin: isAdmin,
  })
  return data.user
}

export async function resetUserPassword(userId: string, password: string) {
  await api.put(`/api/v1/users/${userId}/password`, { password })
}

export async function listAccounts() {
  const { data } = await api.get<{ accounts: Account[] }>('/api/v1/accounts')
  return data.accounts
}

export async function createAccount(name: string, type: AccountType, owner: AccountOwner) {
  const { data } = await api.post<{ account: Account }>('/api/v1/accounts', { name, type, owner })
  return data.account
}

export async function updateAccount(
  id: string,
  payload: { name: string; type: AccountType; owner: AccountOwner; sort_order: number },
) {
  await api.put(`/api/v1/accounts/${id}`, payload)
}

export async function deactivateAccount(id: string) {
  await api.delete(`/api/v1/accounts/${id}`)
}

export async function reorderAccounts(accountIds: string[]) {
  await api.put('/api/v1/accounts/reorder', { account_ids: accountIds })
}

export async function listSnapshots() {
  const { data } = await api.get<{ snapshots: SnapshotListItem[] }>('/api/v1/snapshots')
  return data.snapshots
}

export async function getSnapshot(id: string) {
  const { data } = await api.get<{ snapshot: Snapshot; net_worth: number }>(
    `/api/v1/snapshots/${id}`,
  )
  return data
}

export async function getLatestSnapshot() {
  const { data } = await api.get<{ snapshot: Snapshot | null; net_worth?: number }>(
    '/api/v1/snapshots/latest',
  )
  return data
}

export async function createSnapshot(payload: {
  date: string
  note?: string
  items: { account_id: string; balance: number }[]
  events: { category: EventCategory; description: string; amount: number }[]
}) {
  const { data } = await api.post<{ snapshot: Snapshot; net_worth: number }>(
    '/api/v1/snapshots',
    payload,
  )
  return data
}

export async function updateSnapshot(
  id: string,
  payload: {
    date: string
    note?: string
    items: { account_id: string; balance: number }[]
    events: { category: EventCategory; description: string; amount: number }[]
  },
) {
  const { data } = await api.put<{ snapshot: Snapshot; net_worth: number }>(
    `/api/v1/snapshots/${id}`,
    payload,
  )
  return data
}

export async function deleteSnapshot(id: string) {
  await api.delete(`/api/v1/snapshots/${id}`)
}

export async function getDashboardSummary() {
  const { data } = await api.get<DashboardSummary>('/api/v1/dashboard/summary')
  return data
}

export async function getDashboardTrend(range: string) {
  const { data } = await api.get<{ points: TrendPoint[] }>(`/api/v1/dashboard/trend`, {
    params: { range },
  })
  return data.points
}

export async function getDashboardComposition() {
  const { data } = await api.get<Composition>('/api/v1/dashboard/composition')
  return data
}

export async function getMonthlyGrowth(year: number) {
  const { data } = await api.get<{ points: MonthlyGrowthPoint[]; year: number }>(
    '/api/v1/dashboard/monthly-growth',
    { params: { year } },
  )
  return data.points
}

export async function getEventStats(year: number) {
  const { data } = await api.get<{
    year: number
    by_category: EventStatRow[]
    grand_total: number
  }>('/api/v1/events/stats', { params: { year } })
  return data
}

export async function downloadSnapshotsCsv() {
  const { data } = await api.get<Blob>('/api/v1/export/csv', { responseType: 'blob' })
  return data
}

export function netWorthFromItems(items: SnapshotItem[]) {
  return items.reduce((sum, it) => sum + it.balance, 0)
}

export function normalizeCreditBalance(type: AccountType, balance: number) {
  if (type !== 'credit') {
    return balance
  }
  if (balance > 0) {
    return -balance
  }
  return balance
}
